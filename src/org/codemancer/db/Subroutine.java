// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import javax.persistence.Entity;

/** A class to represent a subroutine. */
@Entity
public class Subroutine extends Fact {
	/** The entry point for this subroutine. */
	private long entryAddr;

	/** Construct empty subroutine.
	 * A default constructor is required by the JPA.
	 */
	protected Subroutine() {
		super();
		this.entryAddr = 0;
	}

	/** Construct subroutine.
	 * @param minRev the lowest revision number for which this subroutine is present
	 * @param maxRev the highest revision number for which this subroutine is present
	 * @param entryAddr the entry point for this subroutine
	 */
	public Subroutine(long minRev, long maxRev, long entryAddr) {
		super(minRev, maxRev);
		this.entryAddr = entryAddr;
	}

	/** Get entry point
	 * @return the entry point for this subroutine
	 */
	public long getEntryAddr() {
		return entryAddr;
	}
}
