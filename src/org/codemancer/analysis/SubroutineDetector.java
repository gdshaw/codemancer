// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.analysis;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import javax.persistence.EntityManager;

import org.codemancer.loader.ObjectFile;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Register;
import org.codemancer.cpudl.Architecture;
import org.codemancer.db.Fact;
import org.codemancer.db.Line;
import org.codemancer.db.Reference;
import org.codemancer.db.BasicBlock;
import org.codemancer.db.ExtendedBasicBlock;
import org.codemancer.db.Subroutine;
import org.codemancer.db.Database;

/** A class for detecting subroutines. */
public class SubroutineDetector {
	/** The object file to be disassembled. */
	private ObjectFile obj;

	/** A database corresponding to the object file. */
	private Database db;

	/** The architecture to be used when disassembling. */
	private Architecture arch;

	/** A list of pending unprocessed blocks. */
	private List<ExtendedBasicBlock> pendingList = new ArrayList<ExtendedBasicBlock>();

	/** The index of the first unprocessed line in the pending list.
	 * If no lines are pending then this is equal to the length of the list.
	 */
	private int pendingIndex = 0;

	/** True if processing will be finished once the end of the pending list
	 * is reached, otherwise false. */
	private boolean done = false;

	/** Construct subroutine detector.
	 * @param obj the object file to be disassembled
	 * @param db the database corresponding to the object file
	 * @param arch the architecture
	 */
	public SubroutineDetector(ObjectFile obj, Database db, Architecture arch) {
		this.obj = obj;
		this.db = db;
		this.arch = arch;
	}


	/** Ensure that a given block is marked as a subroutine entry block.
	 * @param block the block to be marked
	 */
	private void makeSubroutineEntryBlock(ExtendedBasicBlock block) {
		EntityManager em = db.getEntityManager();

		// Determine whether this block is already marked as part of a subroutine.
		Subroutine subroutine = block.getSubroutine();
		if (subroutine != null) {
			// If it is already the entry point of a subroutine
			// then no further action is necessary.
			if (subroutine.getEntryAddr() == block.getEntryAddr()) {
				return;
			}

			// If it is part of a subroutine, but not the entry block,
			// then extricate it from that subroutine. To do this, all
			// blocks of the subroutine (except the entry block) must
			// be reprocessed.
			List<ExtendedBasicBlock> subBlocks = db.getExtendedBasicBlocksIn(subroutine);
			for (ExtendedBasicBlock subBlock: subBlocks) {
				if (subBlock.getEntryAddr() != subroutine.getEntryAddr()) {
					subBlock.setSubroutine(null);
					subBlock.setProcessed(Fact.DONE_SUBROUTINE_DETECTOR);
				}
			}
		}

		// Now create a new subroutine object for the current block.
		subroutine = new Subroutine(0, -1, block.getEntryAddr());
		em.persist(subroutine);
		block.setSubroutine(subroutine);
	}

	/** Process the next unprocessed extended basic block.
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 * @return true if all pending blocks have been processed, otherwise false
	 */
	public boolean detectNext(Register pc, List<Expression> links) {
		EntityManager em = db.getEntityManager();

		if (pendingIndex == pendingList.size()) {
			if (done) return true;
			pendingList = db.getUnprocessedExtendedBasicBlocks(Fact.DONE_SUBROUTINE_DETECTOR);
			pendingIndex = 0;
			if (pendingList.size() == 0) return true;
			done = true;
		}

		ExtendedBasicBlock block = pendingList.get(pendingIndex);
		if (!block.isProcessed(Fact.DONE_SUBROUTINE_DETECTOR)) {
			// Determine whether there are any references to this block which
			// directly indicate that it the entry point to a subroutine.
			// These could be:
			// - internal references from subroutine calls, or
			// - external references to what are assumed to be subroutines.
			long addr = block.getEntryAddr();
			List<Reference> references = db.getReferences(addr, addr);
			boolean subEntry = false;
			for (Reference reference: references) {
				if (reference.isSubRef()) {
					subEntry = true;
				}
			}

			if (subEntry) {
				// If this block is directly referred to as a subroutine entry
				// point then ensure that it is marked as such.
				makeSubroutineEntryBlock(block);
				block.setProcessed(Fact.DONE_SUBROUTINE_DETECTOR);
				done = false;
			} else {
				// If this block is not directly referred to as a subroutine
				// entry point then determine whether it is reachable from any
				// other blocks that are known to be parts of subroutines.

				// First, consider fallthrough from a previous block.
				Subroutine subroutine = null;
				BasicBlock prevBlock = db.getPreviousBasicBlock(addr);
				if ((prevBlock != null) && prevBlock.canFallThrough()) {
					subroutine = prevBlock.getSubroutine();
				}

				// Then, consider explicit branches from other blocks to this block.
				boolean conflict = false;
				for (Reference reference: references) {
					// Interested only in internal references.
					if (!reference.isInternal()) continue;

					// Interested only in code references which are not subroutine calls.
					if (!reference.isCodeRef() || reference.isSubRef()) continue;

					// Disregard jumps originating from within this block.
					BasicBlock refBlock = db.getBasicBlock(reference.getSrcAddr());
					if (refBlock.getExtendedBasicBlock() == block) continue;

					// Disregard jumps originating from a block which as not yet been
					// assigned to a subroutine.
					if (refBlock.getSubroutine() == null) continue;

					// Either make this part of the same subroutine or declare a conflict.
					if (subroutine == null) {
						subroutine = refBlock.getSubroutine();
					} else if (refBlock.getSubroutine() != null) {
						if (subroutine != refBlock.getSubroutine()) {
							conflict = true;
						}
					}
				}

				if (conflict) {
					// If this block is part of more than one subroutine then assume
					// that this was caused by tail call optimisation, in which case
					// it should be considered a separate subroutine (even if it is
					// not itself the subject of a subroutine call).
					makeSubroutineEntryBlock(block);
					block.setProcessed(Fact.DONE_SUBROUTINE_DETECTOR);
					done = false;
				} else if (subroutine != null) {
					// Otherwise, if this block has been found to be part of an
					// existing subroutine then mark it accordingly.
					block.setSubroutine(subroutine);
					block.setProcessed(Fact.DONE_SUBROUTINE_DETECTOR);
					done = false;
				}
			}
		}
		pendingIndex += 1;
		return false;
	}
}
