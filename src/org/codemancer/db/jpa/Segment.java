// This file is part of Codemancer.
// Copyright 2015-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db.jpa;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;

/** A class to represent an object file segment that is mapped into an address space. */
@Entity
public class Segment extends Fact implements org.codemancer.db.Segment {
	/** The address space to which this segment is mapped. */
        @ManyToOne(fetch=FetchType.EAGER)
        private AddressSpace addrSpace;

	/** The first address mapped. */
	private long firstAddr;

	/** The last address mapped. */
	private long lastAddr;

	/** Construct empty segment.
	 * A default constructor is required by the JPA.
	 */
	protected Segment() {
		super();
		this.addrSpace = null;
		this.firstAddr = 0;
		this.lastAddr = 0;
	}

	/** Construct segment.
	 * @param minRev the lowest database revision to which this segment is applicable
	 * @param maxRev the highest database revision to which this segment is applicable,
	 *  or -1 for all higher revisions
	 * @param addrSpace the address space to which this segment is mapped
	 * @param firstAddr the first address mapped
	 * @param lastAddr the last address mapped
	 */
	protected Segment(long minRev, long maxRev, org.codemancer.db.AddressSpace addrSpace, long firstAddr, long lastAddr) {
		super(minRev, maxRev);
		this.addrSpace = (AddressSpace)addrSpace;
		this.firstAddr = firstAddr;
		this.lastAddr = lastAddr;
	}

	/** Get address space.
	 * @return the address space to which this segment refers
	 */
	public org.codemancer.db.AddressSpace getAddrSpace() {
		return addrSpace;
	}

	/** Get the first address mapped.
	 * @return the first address mapped
	 */
	public long getFirstAddr() {
		return firstAddr;
	}

	/** Get the last address mapped.
	 * @return the last address mapped
	 */
	public long getLastAddr() {
		return lastAddr;
	}
}

