// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db.jpa;

import javax.persistence.EntityManager;

/** A class to represent the collection of SSA expressions in a Codemancer database. */
class SsaExpressions implements org.codemancer.db.SsaExpressions {
	/** The entity manager for the database. */
	private final EntityManager em;

	/** Construct collection of SSA expressions.
	 * @param em the entity manager for the database
	 */
	protected SsaExpressions(EntityManager em) {
		this.em = em;
	}

	public final SsaExpression make(long minRev, long maxRev, org.codemancer.db.Subroutine subroutine, String name) {
		SsaExpression expr = new SsaExpression(minRev, maxRev, subroutine, name);
		em.persist(expr);
		return expr;
	}

	public final org.codemancer.db.SsaExpression get(org.codemancer.db.Subroutine subroutine, String name) {
		return em.createQuery(
			"FROM SsaExpression WHERE subroutine = :subroutine AND name = :name", SsaExpression.class)
			.setParameter("subroutine", subroutine)
			.setParameter("name", name)
			.getSingleResult();
	}
}
