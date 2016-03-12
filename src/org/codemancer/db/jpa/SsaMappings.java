// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db.jpa;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.EntityManager;

/** A class to represent the collection of SSA mappings in a Codemancer database. */
class SsaMappings implements org.codemancer.db.SsaMappings {
	/** The entity manager for the database. */
	private final EntityManager em;

	/** Construct collection of SSA mappings.
	 * @param em the entity manager for the database
	 */
	protected SsaMappings(EntityManager em) {
		this.em = em;
	}

	public final SsaMapping make(long minRev, long maxRev, long addr, boolean inbound, String name, org.codemancer.db.SsaExpression value) {
		SsaMapping mapping = new SsaMapping(minRev, maxRev, addr, inbound, name, value);
		em.persist(mapping);
		return mapping;
	}

	public final List<org.codemancer.db.SsaMapping> get(long addr) {
		List<SsaMapping> mappings = em.createQuery(
			"FROM SsaMapping WHERE addr = :addr", SsaMapping.class)
			.setParameter("addr", addr)
			.getResultList();
		return new ArrayList<org.codemancer.db.SsaMapping>(mappings);
	}
}
