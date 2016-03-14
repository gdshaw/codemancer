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
	/** The database to which this collection belongs. */
	private final Database db;

	/** The entity manager for the database. */
	private final EntityManager em;

	/** Construct collection of references.
	 * @param db the database
	 * @param em the entity manager for the database
	 */
	protected References(Database db, EntityManager em) {
		this.db = db;
		this.em = em;
	}

	public final Reference make(long srcAddr, long dstAddr, boolean internal,
		boolean dataRef, boolean codeRef, boolean subRef) {
		Reference reference = new Reference(db.getNextRevision().get(), -1, srcAddr, dstAddr, internal, dataRef, codeRef, subRef);
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

	public final Long findNextDestination(long addr) {
		List<Reference> existingLines = em.createQuery(
			"FROM Reference WHERE maxRev = -1 AND dstAddr > :addr ORDER BY dstAddr", Reference.class)
			.setParameter("addr", addr)
			.setMaxResults(1)
			.getResultList();
		Long stopAddr = null;
		if (!existingLines.isEmpty()) {
			stopAddr = existingLines.get(0).getDstAddr();
		}
		return stopAddr;
	}
}
