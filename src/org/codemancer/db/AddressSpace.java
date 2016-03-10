// This file is part of Codemancer.
// Copyright 2015-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

/** An interface to represent an address space.
 * Some processor architectures use the same numerical address to refer to
 * multiple memory locations. Examples include:
 *
 * - code and data memory on the MCS-51
 * - input/output ports on the Z80
 *
 * Such addresses are distinguished by qualifying them with an address space.
 */
public interface AddressSpace extends Fact {
	/** Get name.
	 * @return the name of this address space
	 */
	String getName();
}
