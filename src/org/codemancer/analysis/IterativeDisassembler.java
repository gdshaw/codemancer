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
import javax.persistence.EntityManager;

import org.codemancer.loader.ObjectFile;
import org.codemancer.loader.ObjectFileReader;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Constant;
import org.codemancer.cpudl.expr.Register;
import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.Architecture;
import org.codemancer.cpudl.FeatureSet;
import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.ShortBitString;
import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.BitStringReader;
import org.codemancer.cpudl.EphemeralState;
import org.codemancer.db.Fact;
import org.codemancer.db.Line;
import org.codemancer.db.Reference;
import org.codemancer.db.Database;

/** A class for iteratively disassembling a supplied object file.
 * Usage is to repeatedly call process() until it returns true.
 */
public class IterativeDisassembler {
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

	/** A list of pending unprocessed references. */
	private List<Reference> pendingList = new ArrayList<Reference>();

	/** The index of the first unprocessed reference in the pending list.
	 * If no references are pending then this is equal to the length of the list.
	 */
	private int pendingIndex = 0;

	/** Construct iterative disassembler object.
	 * @param obj the object file to be disassembled
	 * @param db the database corresponding to the object file
	 * @param arch the architecture
	 */
	public IterativeDisassembler(ObjectFile obj, Database db, Architecture arch) throws IOException {
		this.obj = obj;
		this.reader = new ObjectFileReader(obj);
		this.db = db;
		this.arch = arch;
		this.features = new FeatureSet(arch);
	}

	/** Disassemble from a given address.
	 * The caller is responsible for embedding this operation within a transaction.
	 * The sequence ends when an instruction is encountered which does not fall through,
	 * or which cannot be decoded, or which has already been decoded.
	 * @param addr the start address from which to disassemble
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 */
	private void disassemble(long addr, Register pc, List<Expression> links) {
		EntityManager em = db.getEntityManager();
		Type start = arch.getStart();

		// Initialise buffer.
		BitString buffer = new ShortBitString();
		reader.seek(addr);

		// Determine address at which disassembly should stop as a result of
		// reaching an address which has already been disassembled.
		Long stopAddr = db.getLines().findFirstAddr(addr);

		// Disassemble until one of the termination conditions is met.
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

			// Resolve the instruction counter.
			Map<String, Expression> registers = new HashMap<String, Expression>();
			registers.put("PC", new Constant(null, addr));
			instr = instr.resolveRegisters(registers).simplify();

			// Disassemble the instruction and calculate its length.
			String asm = start.unparse(0, instr) + "\t" + start.unparse(1, instr);
			long bitCount = codeReader.tell();
			if ((bitCount & 7) != 0) {
				throw new IllegalArgumentException("instruction not a whole number of bytes");
			}
			long byteCount = bitCount >> 3;

			// Record the instruction as a line object.
			db.getLines().make(0, -1, addr, addr + byteCount - 1, asm);

			// Record branches and subroutine calls.
			for (Expression dst: classifier.getDestinationAddresses()) {
				dst = dst.resolveRegisters(registers).simplify();
				if (dst instanceof Constant) {
					Constant dstConstant = (Constant)dst;
					long dstAddr = dstConstant.getValue();
					boolean isSub = classifier.isCall();
					boolean isCode = isSub | classifier.isBranch();
					db.getReferences().make(0, -1, addr, dstAddr, true, false, isCode, isSub);
				}
			}

			// Advance the address to the next instruction.
			// (The reader will already have been advanced while decoding the instruction.)
			addr += byteCount;

			// Break out of the loop of this instruction cannot fall through to the next one.
			if (!classifier.canFallThrough()) {
				break;
			}

			// Remove any bits which have been disassembled.
			buffer = buffer.substring(bitCount, buffer.length());
		}
	}

	/** Disassemble next unprocessed reference.
	 * This function should be called repeatedly until it returns true.
	 * The caller is responsible for embedding this operation within a transaction.
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 * @return true if all pending references have been processed, otherwise false.
	 */
	public boolean process(Register pc, List<Expression> links) {
		if (pendingIndex == pendingList.size()) {
			pendingList = db.getReferences().getUnprocessed(Fact.DONE_ITERATIVE_DISASSEMBLER);
			pendingIndex = 0;
		}
		if (pendingIndex == pendingList.size()) {
			return true;
		}
		Reference reference = pendingList.get(pendingIndex);
		long addr = reference.getDstAddr();
		if (reference.isCodeRef() && reader.isMapped(addr)) {
			disassemble(reference.getDstAddr(), pc, links);
		}
		reference.setProcessed(Fact.DONE_ITERATIVE_DISASSEMBLER);
		pendingIndex += 1;
		return false;
	}
}
