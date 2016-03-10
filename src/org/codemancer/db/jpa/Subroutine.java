// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db.jpa;

import javax.persistence.Entity;

/** A class to represent a subroutine. */
@Entity
public class Subroutine extends Fact implements org.codemancer.db.Subroutine {
	/** The entry point for this subroutine. */
	private long entryAddr;

	/** The numerical suffix to use when naming the next local SSA expression. */
	private long nextSsaName = 0;

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

	/** Allocate SSA expression name.
	 * @return the name
	 */
	public String allocateSsaName() {
		String name = String.format("v%d", nextSsaName);
		nextSsaName += 1;
		return name;
	}
}
