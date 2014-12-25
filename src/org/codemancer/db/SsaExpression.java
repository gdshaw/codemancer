// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

/** A class for describing an SSA expression. */
@Entity
public class SsaExpression extends Fact {
	/** The name of this SSA expression. */
	private String name;

	/** Construct empty SSA expression.
	 * A default constructor is required by the JPA.
	 */
	protected SsaExpression() {
		super();
		this.name = null;
	}

	/** Construct SSA expression.
	 * @param minRev the lowest revision number for which this description applies
	 * @param maxRev the highest revision number for which this description applies
	 * @param name the name of this SSA expression
	 */
	public SsaExpression(long minRev, long maxRev, String name) {
		super(minRev, maxRev);
		this.name = name;
	}

	/** Get the name of this SSA expression.
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
