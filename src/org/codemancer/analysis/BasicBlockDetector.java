// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.analysis;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.nio.ByteBuffer;
import javax.persistence.EntityManager;

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
import org.codemancer.db.Line;
import org.codemancer.db.Reference;
import org.codemancer.db.BasicBlock;
import org.codemancer.db.Database;

/** A class for detecting basic blocks. */
public class BasicBlockDetector {
	/** The binary image to be disassembled. */
	private ByteBuffer image;

	/** A database corresponding to the binary image. */
	private Database db;

	/** The architecture to be used when disassembling. */
	private Architecture arch;

	/** The feature set to be used when disassembling. */
	private FeatureSet features;

	/** The lowest address that is part of the binary image. */
	private long minAddr;

	/** The highest address that is part of the binary image. */
	private long maxAddr;

	/** A list of pending unprocessed lines. */
	private List<Line> pendingList = new ArrayList<Line>();

	/** The index of the first unprocessed line in the pending list.
	 * If no lines are pending then this is equal to the length of the list.
	 */
	private int pendingIndex = 0;

	/** Construct basic block detector.
	 * @param image the binary image to be disassembled
	 * @param db the database corresponding to the binary image
	 * @param arch the architecture
	 * @param minAddr the lowest address that is part of the binary image
	 * @param maxAddr the highest address that is part of the binary image
	 */
	public BasicBlockDetector(ByteBuffer image, Database db, Architecture arch, long minAddr, long maxAddr) {
		this.image = image;
		this.db = db;
		this.arch = arch;
		this.features = new FeatureSet(arch);
		this.minAddr = minAddr;
		this.maxAddr = maxAddr;
	}

	/** Make a basic block starting at a given address.
	 * @param addr the start address for the block
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 */
	public void detect(long addr, Register pc, List<Expression> links) {
		EntityManager em = db.getEntityManager();
		Type start = arch.getStart();

		// Initialise buffer.
		long bufferAddr = addr;
		BitString buffer = new ShortBitString();

		// Determine address at which disassembly would stop as a result of
		// reaching the destination of a branch or call instruction.
		List<Reference> existingLines = em.createQuery(
			"FROM Reference " +
			"WHERE maxRev = -1 " +
			"AND dstAddr > :addr " +
			"ORDER BY dstAddr", Reference.class)
			.setParameter("addr", addr)
			.setMaxResults(1)
			.getResultList();
		long maxAddr = 0;
		if (!existingLines.isEmpty()) {
			maxAddr = existingLines.get(0).getDstAddr();
		}

		// Disassemble until one of the termination conditions is met.
		long startAddr = addr;
		boolean fallThrough = false;
		while ((addr < maxAddr) || (maxAddr == 0)) {
			// Fill/refill buffer.
			while ((buffer.length() < 64) && ((bufferAddr < maxAddr) || (maxAddr == 0))) {
				byte newByte = image.get((int)(bufferAddr - minAddr));
				BitString newBits = new ShortBitString(newByte, 8, arch.isBigEndian());
				buffer = buffer.concat(newBits);
				bufferAddr += 1;
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
			String asm = start.unparse(0, instr) + "\t" + start.unparse(1, instr);
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
			BasicBlock block = new BasicBlock(0, -1, startAddr, addr - 1, fallThrough);
			em.persist(block);

			List<Line> lines = em.createQuery(
				"FROM Line " +
				"WHERE maxRev = -1 " +
				"AND minAddr >= :minAddr " +
				"AND minAddr <= :maxAddr " +
				"ORDER BY minAddr", Line.class)
				.setParameter("minAddr", block.getMinAddr())
				.setParameter("maxAddr", block.getMaxAddr())
				.getResultList();
			for (Line line: lines) {
				line.setProcessed(true);
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
			pendingList = db.getUnprocessedLines();
			pendingIndex = 0;
		}

		if (pendingIndex == pendingList.size()) {
			return true;
		}

		Line line = pendingList.get(pendingIndex);
		if (!line.isProcessed()) {
			long addr = line.getMinAddr();
			detect(addr, pc, links);
		}
		pendingIndex += 1;
		return false;
	}
}