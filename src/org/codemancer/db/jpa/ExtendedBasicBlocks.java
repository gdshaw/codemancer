// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db.jpa;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.EntityManager;

/** A class to represent the collection of basic blocks in a Codemancer database. */
class ExtendedBasicBlocks implements org.codemancer.db.ExtendedBasicBlocks {
	/** The entity manager for the database. */
	private final EntityManager em;

	/** Construct collection of extended basic blocks.
	 * @param em the entity manager for the database
	 */
	protected ExtendedBasicBlocks(EntityManager em) {
		this.em = em;
	}

	public final List<org.codemancer.db.ExtendedBasicBlock> get() {
		List<ExtendedBasicBlock> ebbs = em.createQuery(
			"FROM ExtendedBasicBlock ORDER BY entryAddr", ExtendedBasicBlock.class)
			.getResultList();
		return new ArrayList<org.codemancer.db.ExtendedBasicBlock>(ebbs);

	}

	public final List<org.codemancer.db.ExtendedBasicBlock> getMembersOf(org.codemancer.db.Subroutine sub) {
		List<ExtendedBasicBlock> ebbs = em.createQuery(
			"FROM ExtendedBasicBlock WHERE subroutine = :sub", ExtendedBasicBlock.class)
			.setParameter("sub", sub)
			.getResultList();
		return new ArrayList<org.codemancer.db.ExtendedBasicBlock>(ebbs);
	}

	public final List<org.codemancer.db.ExtendedBasicBlock> getUnprocessed(int requiredLevel) {
		List<ExtendedBasicBlock> ebbs = em.createQuery(
			"FROM ExtendedBasicBlock WHERE processedLevel < :requiredLevel", ExtendedBasicBlock.class)
			.setParameter("requiredLevel", requiredLevel)
			.getResultList();
		return new ArrayList<org.codemancer.db.ExtendedBasicBlock>(ebbs);
	}

	public final long count(long rev) {
		return em.createQuery("SELECT COUNT(entryAddr) FROM ExtendedBasicBlock WHERE (minRev <= :rev) AND ((maxRev >= :rev) OR (maxRev = -1))", Long.class)
			.setParameter("rev", rev)
			.getSingleResult();
	}
}
