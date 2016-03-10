// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

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
public interface BasicBlock extends Fact {
	/** Get first address.
	 * @return the first address that is part of this basic block
	 */
	long getMinAddr();

	/** Get last address.
	 * @return the last address that is part of this basic block
	 */
	long getMaxAddr();

	/** Test whether execution can fall through to the next basic block.
	 * @return true if execution can fall through, otherwise false
	 */
	boolean canFallThrough();

	/** Get the extended basic block to which this basic block belongs.
	 * @return the extended basic block, or null if none
	 */
	ExtendedBasicBlock getExtendedBasicBlock();

	/** Set the extended basic block to which this basic block belongs.
	 * @param ebb the extended basic block, or null if none
	 */
	void setExtendedBasicBlock(ExtendedBasicBlock ebb);

	/** Get the subroutine to which this basic block belongs.
	 * @return the subroutine, or null if none
	 */
	Subroutine getSubroutine();
}
