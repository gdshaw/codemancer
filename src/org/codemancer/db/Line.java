// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

/** An interface to represent a disassembled line of assembly language. */
public interface Line extends Fact {
	/** Get first addr.
	 * @return the first address occupied by this line
	 */
	long getMinAddr();

	/** Get last addr.
	 * @return the last address occupied by this line
	 */
	long getMaxAddr();

	/** Get instruction.
	 * @return the disassembled instruction
	 */
	String getInstruction();

	/** Convert to JSON.
	 * @return this line, as JSON.
	 */
	String asJSON();
}
