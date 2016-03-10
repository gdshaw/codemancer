// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db.jpa;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;

/** A class for describing an SSA expression. */
@Entity
public class SsaExpression extends Fact implements org.codemancer.db.SsaExpression {
	/** The subroutine to which this SSA expression belongs. */
	@ManyToOne(fetch=FetchType.LAZY)
	private Subroutine subroutine;

	/** The name of this SSA expression.
	 * Names are unique within a given subroutine, but not globally.
	 */
	private String name;

	/** Construct empty SSA expression.
	 * A default constructor is required by the JPA.
	 */
	protected SsaExpression() {
		super();
		this.subroutine = null;
		this.name = null;
	}

	/** Construct SSA expression.
	 * @param minRev the lowest revision number for which this description applies
	 * @param maxRev the highest revision number for which this description applies
	 * @param subroutine the subroutine to which this SSA expression belongs
	 * @param name the name of this SSA expression
	 */
	public SsaExpression(long minRev, long maxRev, org.codemancer.db.Subroutine subroutine, String name) {
		super(minRev, maxRev);
		this.subroutine = (Subroutine)subroutine;
		this.name = name;
	}

	/** Get the subroutine to which this SSA expression belongs.
	 * @return the subroutine
	 */
	public org.codemancer.db.Subroutine getSubroutine() {
		return subroutine;
	}

	/** Get the name of this SSA expression.
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
