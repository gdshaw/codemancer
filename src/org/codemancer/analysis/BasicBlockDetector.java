// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.analysis;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.nio.ByteBuffer;

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
import org.codemancer.db.Line;
import org.codemancer.db.Reference;
import org.codemancer.db.BasicBlock;
import org.codemancer.db.Database;

/** A class for detecting basic blocks. */
public class BasicBlockDetector {
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

	/** A list of pending unprocessed lines. */
	private List<Line> pendingList = new ArrayList<Line>();

	/** The index of the first unprocessed line in the pending list.
	 * If no lines are pending then this is equal to the length of the list.
	 */
	private int pendingIndex = 0;

	/** Construct basic block detector.
	 * @param obj the object file to be disassembled
	 * @param db the database corresponding to the object file
	 * @param arch the architecture
	 */
	public BasicBlockDetector(ObjectFile obj, Database db, Architecture arch) throws IOException {
		this.obj = obj;
		this.reader = new ObjectFileReader(obj);
		this.db = db;
		this.arch = arch;
		this.features = new FeatureSet(arch);
	}

	/** Make a basic block starting at a given address.
	 * @param addr the start address for the block
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 */
	public void detect(long addr, Register pc, List<Expression> links) {
		Type start = arch.getStart();

		// Initialise buffer.
		BitString buffer = new ShortBitString();
		reader.seek(addr);

		// Determine address at which disassembly would stop as a result of
		// reaching the destination of a branch or call instruction.
		Long stopAddr = db.getReferences().findNextDestination(addr);

		// Disassemble until one of the termination conditions is met.
		long startAddr = addr;
		boolean fallThrough = false;
		while ((stopAddr == null) || (addr < stopAddr)) {
			// Fill/refill buffer.
			while ((buffer.length() < 64) && ((stopAddr == null) || (reader.tell() < stopAddr))) {
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
			fallThrough = classifier.canFallThrough();

			// Resolve the instruction counter.
			Map<String, Expression> registers = new HashMap<String, Expression>();
			registers.put("PC", new Constant(null, addr));
			instr = instr.resolveRegisters(registers).simplify();

			// Calculate the length of this instruction.
			long bitCount = codeReader.tell();
			if ((bitCount & 7) != 0) {
				throw new IllegalArgumentException("instruction not a whole number of bytes");
			}
			long byteCount = bitCount >> 3;

			// Advance the address to the next instruction.
			// (The reader will already have been advanced while decoding the instruction.)
			addr += byteCount;

			// Detect instructions which would terminate the basic block.
			if (classifier.isBranch() || classifier.isReturn()) {
				break;
			}

			// Remove any bits which have been disassembled.
			buffer = buffer.substring(bitCount, buffer.length());
		}

		if (addr > startAddr) {
			BasicBlock bb = db.getBasicBlocks().make(0, -1, startAddr, addr - 1, fallThrough);
			List<Line> lines = db.getLines().getMembersOf(bb);
			for (Line line: lines) {
				line.setProcessed(Fact.DONE_BASIC_BLOCK_DETECTOR);
			}
		}
	}

	/** Make a basic block starting at the first instruction that is not already part of one.
	 * The caller is responsible for embedding this operation within a transaction.
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 * @return true if all pending instructions have been processed, otherwise false
	 */
	public boolean detectNext(Register pc, List<Expression> links) {
		if (pendingIndex == pendingList.size()) {
			pendingList = db.getLines().getUnprocessed(Fact.DONE_BASIC_BLOCK_DETECTOR);
			pendingIndex = 0;
		}

		if (pendingIndex == pendingList.size()) {
			return true;
		}

		Line line = pendingList.get(pendingIndex);
		if (!line.isProcessed(Fact.DONE_BASIC_BLOCK_DETECTOR)) {
			long addr = line.getMinAddr();
			detect(addr, pc, links);
		}
		pendingIndex += 1;
		return false;
	}
}
