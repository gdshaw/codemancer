// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

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
public interface ExtendedBasicBlock extends Fact {
	/** Get entry point
	 * @return the entry point for this extended basic block
	 */
	long getEntryAddr();

	/** Test whether execution can fall through to the next block.
	 * @return true if execution can fall through, otherwise false
	 */
	boolean canFallThrough();

	/** Get the subroutine to which this extended basic block belongs.
	 * @return the subroutine, or null if none
	 */
	Subroutine getSubroutine();

	/** Set the subroutine to which this extended basic block belongs.
	 * @param subroutine the subroutine, or null if none
	 */
	void setSubroutine(Subroutine subroutine);
}
