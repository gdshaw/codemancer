// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.analysis;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.ArrayDeque;
import java.io.IOException;
import javax.persistence.EntityManager;

import org.codemancer.loader.ObjectFile;
import org.codemancer.loader.ObjectFileReader;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Register;
import org.codemancer.cpudl.expr.Constant;
import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.Architecture;
import org.codemancer.cpudl.FeatureSet;
import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.ShortBitString;
import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.BitStringReader;
import org.codemancer.db.Fact;
import org.codemancer.db.BasicBlock;
import org.codemancer.db.ExtendedBasicBlock;
import org.codemancer.db.Subroutine;
import org.codemancer.db.Database;

/** A class for mapping registers to SSA expressions. */
public class SsaMapper {
	/** A class to represent an unexplored path that the flow of control can take. */
	private static class ControlPath {
		/** The basic block at which the path begins. */
		public BasicBlock block;

		/** The machine state to use when following this path.
		 * This may contain live registers from previous basic blocks
		 * within the same extended basic block.
		 */
		public SsaState state;

		/** Construct path.
		 * @param block the basic block at which the path begins
		 * @param state the machine state to use when following this path
		 */
		public ControlPath(BasicBlock block, SsaState state) {
			this.block = block;
			this.state = state;
		}
	}

	/** The object file to be disassembled. */
	private ObjectFile obj;

	/** A reader for the object file. */
	private ObjectFileReader reader;

	/** A database corresponding to the object file. */
	private Database db;

	/** The entity manager for the database. */
	EntityManager em;

	/** The architecture to be used when disassembling. */
	private Architecture arch;

	/** The feature set to be used when disassembling. */
	private FeatureSet features;

	/** The set of basic blocks in the current extended basic block
	 * for which paths have not yet been created. */
	private Set<Long> unencounteredBlocks = new HashSet<Long>();

	/** A queue of unprocessed paths. */
	private Queue<ControlPath> pendingPaths = new ArrayDeque<ControlPath>();

	/** A queue of unprocessed extended basic blocks. */
	private Queue<ExtendedBasicBlock> pendingBlocks = new ArrayDeque<ExtendedBasicBlock>();

	/** The current basic block. */
	BasicBlock curBlock;

	/** The address of the current instruction. */
	long curAddr;

	/** Construct SSA mapper.
	 * @param obj the object file to be mapped
	 * @param db the database corresponding to the object file
	 * @param arch the architecture
	 */
	public SsaMapper(ObjectFile obj, Database db, Architecture arch) throws IOException {
		this.obj = obj;
		this.reader = new ObjectFileReader(obj);
		this.db = db;
		this.em = db.getEntityManager();
		this.arch = arch;
		this.features = new FeatureSet(arch);
	}

	/** Map registers values for a given path of control
	 * @param path the path to be followed
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 */
	private void map(ControlPath path, Register pc, List<Expression> links) {
		BasicBlock block = path.block;
		SsaState state = path.state;
		Type start = arch.getStart();

		// Initialise buffer.
		BitString buffer = new ShortBitString();
		reader.seek(block.getMinAddr());

		// Disassemble each instruction in the basic block.
		long addr = block.getMinAddr();
		while (addr <= block.getMaxAddr()) {
			// Fill/refill buffer.
			while ((buffer.length() < 64) && (reader.tell() <= block.getMaxAddr())) {
				byte newByte = reader.get();
				BitString newBits = new ShortBitString(newByte, 8, arch.isBigEndian());
				buffer = buffer.concat(newBits);
			}

			// Decode the next instruction.
			BitReader codeReader = new BitStringReader(buffer);
			List<BitReader> codeReaders = new ArrayList<BitReader>();
			codeReaders.add(codeReader);
			Expression instr = start.decode(codeReaders, features);
			if (instr == null) {
				break;
			}

			// Resolve references within instruction.
			instr = instr.resolveReferences(null, null);

			// Classify this instruction.
			// This must be done /before/ resolving any registers.
			InstructionClassifier classifier = new InstructionClassifier(instr, pc, links);

			// Calculate the length of the instruction.
			long bitCount = codeReader.tell();
			if ((bitCount & 7) != 0) {
				throw new IllegalArgumentException("instruction not a whole number of bytes");
			}
			long byteCount = bitCount >> 3;

			// Evaluate the effect of this instruction.
			state.setAddr(addr, addr + byteCount);
			instr.evaluate(state);

			// For subroutine call instructions, invalidate all registers.
			// It will normally be possible to do better than this if information
			// about the calling convention is available.
			if (classifier.isCall()) {
				state.invalidate();
			}

			// For branches to basic blocks with this extended basic block,
			// create a path record if this has not already been done
			// then add it to the queue.
			if (classifier.isBranch()) {
				Map<String, Expression> registers = new HashMap<String, Expression>();
				registers.put("PC", new Constant(null, addr));
				registers.put("PC+", new Constant(null, addr + byteCount));
				for (Expression dst: classifier.getDestinationAddresses()) {
					dst = dst.resolveRegisters(registers).simplify();
					if (dst instanceof Constant) {
						Constant dstConstant = (Constant)dst;
						long dstAddr = dstConstant.getValue();
						if (unencounteredBlocks.contains(dstAddr)) {
							BasicBlock newBlock = db.getBasicBlock(dstAddr);
							SsaState newState = new SsaState(state);
							ControlPath newPath = new ControlPath(newBlock, newState);
							pendingPaths.add(newPath);
							unencounteredBlocks.remove(dstAddr);
						}
					}
				}
			}

			// Advance the address to the next instruction.
			addr += byteCount;

			// Remove any bits which have been disassembled.
			buffer = buffer.substring(bitCount, buffer.length());
		}

		// If this block can fall through, and if the next block is part of the current
		// extended basic block that has not been encountered yet, then add it to the queue.
		if (block.canFallThrough() && unencounteredBlocks.contains(addr)) {
			BasicBlock newBlock = db.getBasicBlock(addr);
			SsaState newState = new SsaState(state);
			ControlPath newPath = new ControlPath(newBlock, newState);
			pendingPaths.add(newPath);
			unencounteredBlocks.remove(addr);
		}

		// Finish creating mappings for any registers which are still live.
		state.invalidate();
	}

	/** Map the next unprocessed basic block.
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 * @return true if all pending blocks have been processed, otherwise false
	 */
	public boolean mapNext(Register pc, List<Expression> links) {
		EntityManager em = db.getEntityManager();

		// If the pending blocks queue is empty then attempt to refill it.
		if (pendingBlocks.isEmpty()) {
			// Attempt to refill the blocks queue.
			pendingBlocks.addAll(db.getUnprocessedExtendedBasicBlocks(Fact.DONE_SSA_MAPPER));

			// If the queue is still empty then stop because there is nothing to do.
			if (pendingBlocks.isEmpty()) return true;
		}

		// If the pending paths queue is empty then prime it by adding the first
		// basic block of the current extended basic block.
		if (pendingPaths.isEmpty()) {
			// If there are no pending paths then all basic blocks should have been processed.
			if (!unencounteredBlocks.isEmpty()) {
				throw new IllegalStateException("failed to find all basic blocks in extended basic block");
			}

			// Get the first basic block of the current extended basic block.
			ExtendedBasicBlock ebb = pendingBlocks.peek();
			long addr = ebb.getEntryAddr();
			BasicBlock firstBlock = db.getBasicBlock(addr);

			// Add this path to the queue.
			SsaState state = new SsaState(db, firstBlock.getSubroutine());
			ControlPath path = new ControlPath(firstBlock, state);
			pendingPaths.add(path);

			// Record the addresses of the other basic blocks which are part of this EBB.
			unencounteredBlocks.clear();
			for (BasicBlock block: db.getBasicBlocksIn(ebb)) {
				unencounteredBlocks.add(block.getMinAddr());
			}
			unencounteredBlocks.remove(addr);
		}

		// If the pending paths queue is now non-empty then process one path.
		if (!pendingPaths.isEmpty()) {
			ControlPath path = pendingPaths.remove();
			map(path, pc, links);

			// If the pending paths queue is now empty as a result of processing a path
			// then the corresponding EBB can be removed from the queue and marked as done.
			if (pendingPaths.isEmpty()) {
				ExtendedBasicBlock ebb = pendingBlocks.remove();
				ebb.setProcessed(Fact.DONE_SSA_MAPPER);
			}
		}
		return false;
	}
}
