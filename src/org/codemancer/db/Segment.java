// This file is part of Codemancer.
// Copyright 2015-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

/** An interface to represent an object file segment that is mapped into an address space. */
public interface Segment extends Fact {
	/** Get address space.
	 * @return the address space to which this segment refers
	 */
	AddressSpace getAddrSpace();

	/** Get the first address mapped.
	 * @return the first address mapped
	 */
	long getFirstAddr();

	/** Get the last address mapped.
	 * @return the last address mapped
	 */
	long getLastAddr();
}
