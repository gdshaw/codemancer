// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import javax.persistence.Entity;

/** A class to represent an extended basic block.
 * An extended basic block is defined to be a sequence of instructions
 * for which the only entry point is the first instruction, and where
 * the execution of any given instruction implies the execution of all
 * preceding instructions in the order listed.
 * Note that by this definition, a subroutine call does not terminate
 * the extended basic block in which it occurs (since in the normal
 * course of events the subroutine will return and execution will
 * continue with the following instruction).
 */
@Entity
public class ExtendedBasicBlock extends Fact {
	/** The entry point for this extended basic block. */
	private long entryAddr;

	/** True if execution can fall through to the next block, otherwise false. */
	private boolean fallThrough;

	/** Construct empty extended basic block.
	 * A default constructor is required by the JPA.
	 */
	protected ExtendedBasicBlock() {
		super();
		this.entryAddr = 0;
		this.fallThrough = false;
	}

	/** Construct extended basic block.
	 * @param minRev the lowest revision number for which this extended basic block is present
	 * @param maxRev the highest revision number for which this extended basic block is present
	 * @param entryAddr the entry point for this extended basic block
	 */
	public ExtendedBasicBlock(long minRev, long maxRev, long entryAddr) {
		super(minRev, maxRev);
		this.entryAddr = entryAddr;
		this.fallThrough = fallThrough;
	}

	/** Get entry point
	 * @return the entry point for this extended basic block
	 */
	public long getEntryAddr() {
		return entryAddr;
	}

	/** Test whether execution can fall through to the next block.
	 * @return true if execution can fall through, otherwise false
	 */
	public boolean canFallThrough() {
		return fallThrough;
	}
}
