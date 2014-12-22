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
	/** A constant to indicate that this fact has not been processed at all. */
	public static final int DONE_NOTHING = 0;

	/** A constant to indicate that this fact has been processed by the iterative disassembler. */
	public static final int DONE_ITERATIVE_DISASSEMBLER = 1;

	/** A constant to indicate that this fact has been processed by the basic block detector. */
	public static final int DONE_BASIC_BLOCK_DETECTOR = 2;

	/** A constant to indicate that this fact has been processed by the basic block detector. */
	public static final int DONE_EXTENDED_BASIC_BLOCK_DETECTOR = 3;

	/** The unique ID for this fact. */
	@Id
	@GeneratedValue
	private final long id = 0;

	/** The lowest database revision to which this fact is applicable. */
	private long minRev;

	/** The highest database revision to which this fact is applicable,
	 * or -1 for all higher revisions. */
	private long maxRev;

	/** The level to which this fact has been processed. */
	private int processedLevel = DONE_NOTHING;

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

	/** Check whether this fact has been processed to a given level. */
	public final boolean isProcessed(int requiredLevel) {
		return (processedLevel >= requiredLevel);
	}

	/** Mark that this fact has been processed to a given level.
	 * The level is set irrespective of its previous value.
	 * @param processedLevel the level that has been completed
	 */
	public final void setProcessed(int processedLevel) {
		this.processedLevel = processedLevel;
	}

	/** Mark that this fact has not been processed to a given level.
	 * The level may be reduced by this operation but is never increased.
	 * @param processedLevel the level that has not been completed
	 */
	public final void setNotProcessed(int notProcessedLevel) {
		if (this.processedLevel >= notProcessedLevel) {
			this.processedLevel = notProcessedLevel - 1;
		}
	}
}
