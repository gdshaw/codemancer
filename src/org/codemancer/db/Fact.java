// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

/** A class to represent a fact stored within the database. */
@Entity
public class Fact {
	/** The unique ID for this fact. */
	@Id
	@GeneratedValue
	private final long id = 0;

	/** The lowest database revision to which this fact is applicable. */
	public long minRev;

	/** The highest database revision to which this fact is applicable,
	 * or -1 for all higher revisions. */
	public long maxRev;

	/** Construct empty fact.
	 * A default constructor is required by the JPA.
	 */
	protected Fact() {
		this.minRev = -1;
		this.maxRev = -1;
	}

	/** Construct fact.
	 * @param minRev the lowest database revision to which this fact is applicable
	 * @param maxRev the highest database revision to which this fact is applicable,
	 *  or -1 for all higher revisions
	 */
	public Fact(long minRev, long maxRev) {
		this.minRev = minRev;
		this.maxRev = maxRev;
	}

	/** Get minimum database revision.
	 * @return the lowest database revision to which this fact is applicable
	 */
	public final long getMinRev() {
		return minRev;
	}

	/** Get maximum database revision.
	 * @return the highest database revision to which this fact is applicable,
	 *  or -1 for all higher revisions
	 */
	public final long getMaxRev() {
		return maxRev;
	}
}
