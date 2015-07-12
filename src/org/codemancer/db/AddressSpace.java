// This file is part of Codemancer.
// Copyright 2015 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import javax.persistence.Entity;

/** A class to represent an address space.
 * Some processor architectures use the same numerical address to refer to
 * multiple memory locations. Examples include:
 *
 * - code and data memory on the MCS-51
 * - input/output ports on the Z80
 *
 * Such addresses are distinguished by qualifying them with an address space.
 */
@Entity
public class AddressSpace extends Fact {
	/** The name of the address space. */
	private String name;

	/** Construct empty address space.
	 * A default constructor is required by the JPA.
	 */
	protected AddressSpace() {
		super();
		this.name = null;
	}

	/** Construct address space.
	 * @param minRev the lowest database revision to which this mapping is applicable
	 * @param maxRev the highest database revision to which this mapping is applicable,
	 *  or -1 for all higher revisions
	 * @param name the name of this address space
	 */
	public AddressSpace(long minRev, long maxRev, String name) {
		super(minRev, maxRev);
		this.name = name;
	}

	/** Get name.
	 * @return the name of this address space
	 */
	public String getName() {
		return name;
	}
}
