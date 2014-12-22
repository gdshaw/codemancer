// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.analysis;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.EntityManager;

import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Register;
import org.codemancer.cpudl.Architecture;
import org.codemancer.cpudl.BitReader;
import org.codemancer.db.Fact;
import org.codemancer.db.Line;
import org.codemancer.db.Reference;
import org.codemancer.db.BasicBlock;
import org.codemancer.db.ExtendedBasicBlock;
import org.codemancer.db.Database;

/** A class for detecting extended basic blocks. */
public class ExtendedBasicBlockDetector {
	/** The binary image to be disassembled. */
	private ByteBuffer image;

	/** A database corresponding to the binary image. */
	private Database db;

	/** The architecture to be used when disassembling. */
	private Architecture arch;

	/** The lowest address within the binary image. */
	private long minAddr;

	/** The highest address within the binary image. */
	private long maxAddr;

	/** A list of pending unprocessed blocks. */
	private List<BasicBlock> pendingList = new ArrayList<BasicBlock>();

	/** The index of the first unprocessed line in the pending list.
	 * If no lines are pending then this is equal to the length of the list.
	 */
	private int pendingIndex = 0;

	/** True if processing will be finished once the end of the pending list
	 * is reached, otherwise false. */
	private boolean done = false;

	/** Construct extended basic block detector.
	 * @param image the binary image to be disassembled
	 * @param db the database corresponding to the binary image
	 * @param arch the architecture
	 * @param minAddr the lowest address within the binary image
	 * @param maxAddr the highest address within the binary image
	 */
	public ExtendedBasicBlockDetector(ByteBuffer image, Database db, Architecture arch, long minAddr, long maxAddr) {
		this.image = image;
		this.db = db;
		this.arch = arch;
		this.minAddr = minAddr;
		this.maxAddr = maxAddr;
	}

	/** Process the next unprocessed basic block.
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 * @return true if all pending blocks have been processed, otherwise false
	 */
	public boolean detectNext(Register pc, List<Expression> links) {
		EntityManager em = db.getEntityManager();

		if (pendingIndex == pendingList.size()) {
			if (done) return true;
			pendingList = db.getUnprocessedBasicBlocks(Fact.DONE_EXTENDED_BASIC_BLOCK_DETECTOR);
			pendingIndex = 0;
			if (pendingList.size() == 0) return true;
			done = true;
		}

		BasicBlock block = pendingList.get(pendingIndex);
		if (!block.isProcessed(Fact.DONE_EXTENDED_BASIC_BLOCK_DETECTOR)) {
			long addr = block.getMinAddr();
			List<Reference> references = db.getReferences(addr, addr);

			// Determine whether this basic block is part of an existing basic block.
			ExtendedBasicBlock ebb = null;
			BasicBlock prevBlock = db.getPreviousBasicBlock(addr);
			if ((prevBlock != null) && prevBlock.canFallThrough()) {
				ebb = prevBlock.getExtendedBasicBlock();
			}
			for (Reference reference: references) {
				if (reference.isCodeRef()) {
					ebb = null;
				}
			}

			if (ebb == null) {
				ebb = new ExtendedBasicBlock(0, -1, addr);
				em.persist(ebb);
			}
			block.setExtendedBasicBlock(ebb);
			block.setProcessed(Fact.DONE_EXTENDED_BASIC_BLOCK_DETECTOR);
			done = false;
		}
		pendingIndex += 1;
		return false;
	}
}
