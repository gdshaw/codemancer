// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db.jpa;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.EntityManager;

/** A class to represent the collection of references in a Codemancer database. */
class References implements org.codemancer.db.References {
	/** The entity manager for the database. */
	private final EntityManager em;

	/** Construct collection of references.
	 * @param em the entity manager for the database
	 */
	protected References(EntityManager em) {
		this.em = em;
	}

	public final Reference make(long minRev, long maxRev, long srcAddr, long dstAddr, boolean internal,
		boolean dataRef, boolean codeRef, boolean subRef) {
		Reference reference = new Reference(minRev, maxRev, srcAddr, dstAddr, internal, dataRef, codeRef, subRef);
		em.persist(reference);
		return reference;
	}

	public final List<org.codemancer.db.Reference> getByDstAddr(long minAddr, long maxAddr) {
		List<Reference> references = em.createQuery(
			"FROM Reference WHERE (dstAddr >= :minAddr) AND (dstAddr <= :maxAddr) ORDER BY minAddr", Reference.class)
			.setParameter("minAddr", minAddr)
			.setParameter("maxAddr", maxAddr)
			.getResultList();
		return new ArrayList<org.codemancer.db.Reference>(references);
	}

	public final List<org.codemancer.db.Reference> getUnprocessed(int requiredLevel) {
		List<Reference> references = em.createQuery(
			"FROM Reference WHERE processedLevel < :requiredLevel", Reference.class)
			.setParameter("requiredLevel", requiredLevel)
			.getResultList();
		return new ArrayList<org.codemancer.db.Reference>(references);
	}
}
