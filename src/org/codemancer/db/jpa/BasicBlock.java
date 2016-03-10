// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db.jpa;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;

/** A class to represent a basic block.
 * A basic block is defined to be a sequence of instructions for which
 * the only entry point is the first instruction, and execution of the
 * first instruction necessarily implies execution of the remaining
 * instructions once in the order listed (disregarding the effect
 * of exceptions and anything else that does not form part of the
 * normal flow of control).
 * Note that by this definition, a subroutine call does not terminate
 * the basic block in which it occurs (since in the normal course of
 * events the subroutine will return and execution will continue with
 * the following instruction).
 */
@Entity
public class BasicBlock extends Fact implements org.codemancer.db.BasicBlock {
	/** The first address that is part of this basic block. */
	private long minAddr;

	/** The last address that is part of this basic block. */
	private long maxAddr;

	/** True if execution can fall through to the next basic block, otherwise false. */
	private boolean fallThrough;

	/** The extended basic block to which this basic block belongs, or null if none. */
	@ManyToOne(fetch=FetchType.EAGER)
	private ExtendedBasicBlock ebb = null;

	/** Construct empty basic block.
	 * A default constructor is required by the JPA.
	 */
	protected BasicBlock() {
		super();
		this.minAddr = 0;
		this.maxAddr = 0;
		this.fallThrough = false;
	}

	/** Construct basic block.
	 * @param minRev the lowest revision number for which this basic block is present
	 * @param maxRev the highest revision number for which this basic block is present
	 * @param minAddr the first address that is part of this basic block
	 * @param maxAddr the last address that is part of this basic block
	 * @param fallThrough true if execution call fall through to the next basic block,
	 *  otherwise false
	 */
	public BasicBlock(long minRev, long maxRev, long minAddr, long maxAddr, boolean fallThrough) {
		super(minRev, maxRev);
		this.minAddr = minAddr;
		this.maxAddr = maxAddr;
		this.fallThrough = fallThrough;
	}

	/** Get first address.
	 * @return the first address that is part of this basic block
	 */
	public long getMinAddr() {
		return minAddr;
	}

	/** Get last address.
	 * @return the last address that is part of this basic block
	 */
	public long getMaxAddr() {
		return maxAddr;
	}

	/** Test whether execution can fall through to the next basic block.
	 * @return true if execution can fall through, otherwise false
	 */
	public boolean canFallThrough() {
		return fallThrough;
	}

	/** Get the extended basic block to which this basic block belongs.
	 * @return the extended basic block, or null if none
	 */
	public org.codemancer.db.ExtendedBasicBlock getExtendedBasicBlock() {
		return ebb;
	}

	/** Set the extended basic block to which this basic block belongs.
	 * @param ebb the extended basic block, or null if none
	 */
	public void setExtendedBasicBlock(org.codemancer.db.ExtendedBasicBlock ebb) {
		this.ebb = (ExtendedBasicBlock)ebb;
	}

	/** Get the subroutine to which this basic block belongs.
	 * @return the subroutine, or null if none
	 */
	public org.codemancer.db.Subroutine getSubroutine() {
		if (this.ebb != null) {
			return this.ebb.getSubroutine();
		}
		return null;
	}
}
