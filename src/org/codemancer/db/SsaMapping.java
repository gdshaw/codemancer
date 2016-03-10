// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

/** A class for mapping a register to a given SSA expression. */
public interface SsaMapping extends Fact {
	/** Get the address to which this mapping applies.
	 * @return the address
	 */
	long getAddr();

	/** Check whether this mapping applies on entry or exit.
	 * @return true on entry, false on exit
	 */
	boolean isInbound();

	/** Get the name of the register or address space containing the variable mapped.
	 * @return the name
	 */
	String getName();

	/** Get the SSA expression to which the variable is mapped.
	 * @return the expression
	 */
	SsaExpression getValue();
}
