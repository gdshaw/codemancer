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
import org.codemancer.db.Database;

/** A class for tracking the lifetimes of register values. */
public class RegisterLifetimeTracker {
	/** A class to represent an unexplored path that the flow of control can take. */
	private static class ControlPath {
		/** The basic block at which the path begins. */
		public BasicBlock block;

		/** Construct path.
		 * @param block the basic block at which the path begins
		 */
		public ControlPath(BasicBlock block) {
			this.block = block;
		}
	}

	/** The object file to be disassembled. */
	private ObjectFile obj;

	/** A reader for the object file. */
	private ObjectFileReader reader;

	/** A database corresponding to the object file. */
	private Database db;

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

	/** Construct register lifetime tracker.
	 * @param obj the object file to be tracked
	 * @param db the database corresponding to the object file
	 * @param arch the architecture
	 */
	public RegisterLifetimeTracker(ObjectFile obj, Database db, Architecture arch) throws IOException {
		this.obj = obj;
		this.reader = new ObjectFileReader(obj);
		this.db = db;
		this.arch = arch;
		this.features = new FeatureSet(arch);
	}

	/** Track registers values in a given basic block
	 * @param block the basic block to be tracked
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 */
	public void track(BasicBlock block, Register pc, List<Expression> links) {
		EntityManager em = db.getEntityManager();
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
							ControlPath newPath = new ControlPath(newBlock);
							pendingPaths.add(newPath);
							unencounteredBlocks.remove(dstAddr);
						}
					}
				}
			}

			// Advance the address to the next instruction.
			addr += byteCount;
		}

		// If this block can fall through, and if the next block is part of the current
		// extended basic block that has not been encountered yet, then add it to the queue.
		if (block.canFallThrough() && unencounteredBlocks.contains(addr)) {
			BasicBlock newBlock = db.getBasicBlock(addr);
			ControlPath newPath = new ControlPath(newBlock);
			pendingPaths.add(newPath);
			unencounteredBlocks.remove(addr);
		}
	}

	/** Track the next unprocessed basic block.
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 * @return true if all pending blocks have been processed, otherwise false
	 */
	public boolean trackNext(Register pc, List<Expression> links) {
		EntityManager em = db.getEntityManager();

		// If the pending blocks queue is empty then attempt to refill it.
		if (pendingBlocks.isEmpty()) {
			// Attempt to refill the blocks queue.
			pendingBlocks.addAll(db.getUnprocessedExtendedBasicBlocks(Fact.DONE_REGISTER_LIFETIME));

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
			ControlPath path = new ControlPath(firstBlock);
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
			track(path.block, pc, links);

			// If the pending paths queue is now empty as a result of processing a path
			// then the corresponding EBB can be removed from the queue and marked as done.
			if (pendingPaths.isEmpty()) {
				ExtendedBasicBlock ebb = pendingBlocks.remove();
				ebb.setProcessed(Fact.DONE_REGISTER_LIFETIME);
			}
		}
		return false;
	}
}
