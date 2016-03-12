// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db.jpa;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.persistence.EntityManager;

/** A class to represent the collection of subroutines in a Codemancer database. */
class Subroutines implements org.codemancer.db.Subroutines {
	/** The entity manager for the database. */
	private final EntityManager em;

	/** Construct collection of subroutines.
	 * @param em the entity manager for the database
	 */
	protected Subroutines(EntityManager em) {
		this.em = em;
	}

	public final Subroutine make(long minRev, long maxRev, long entryAddr) {
		Subroutine subroutine = new Subroutine(minRev, maxRev, entryAddr);
		em.persist(subroutine);
		return subroutine;
	}

	public final org.codemancer.db.Subroutine getStarting(long entryAddr, long rev) {
		List<Subroutine> subroutines = em.createQuery(
			"FROM Subroutine WHERE (entryAddr = :entryAddr) AND (minRev <= :rev) AND ((maxRev >= :rev) OR (maxRev = -1))", Subroutine.class)
			.setParameter("entryAddr", entryAddr)
			.setParameter("rev", rev)
			.getResultList();
		if (subroutines.isEmpty()) {
			return null;
		} else {
			return subroutines.get(0);
		}
	}

	public final List<org.codemancer.db.Subroutine> get() {
		List<Subroutine> subroutines = em.createQuery(
			"FROM Subroutine ORDER BY entryAddr", Subroutine.class)
			.getResultList();
		return new ArrayList<org.codemancer.db.Subroutine>(subroutines);
	}

	public final Map<Long, org.codemancer.db.Subroutine> getChanged(long minRev, long maxRev) {
		List<Subroutine> subroutines = em.createQuery(
			"FROM Subroutine where ((minRev >= :minRev) AND (minRev <= :maxRev)) OR ((maxRev >= :minRev) AND (maxRev <= :maxRev)) ORDER BY minRev", Subroutine.class)
			.setParameter("minRev", minRev)
			.setParameter("maxRev", maxRev)
			.getResultList();

		Map<Long, org.codemancer.db.Subroutine> filteredSubroutines = new HashMap<Long, org.codemancer.db.Subroutine>();
		for (Subroutine subroutine: subroutines) {
			if ((subroutine.getMaxRev() >= maxRev) || (subroutine.getMaxRev() == -1)) {
				filteredSubroutines.put(subroutine.getEntryAddr(), subroutine);
			} else {
				filteredSubroutines.put(subroutine.getEntryAddr(), null);
			}
		}

		return filteredSubroutines;
	}

	public final long count(long rev) {
		return em.createQuery("SELECT COUNT(entryAddr) FROM Subroutine WHERE (minRev <= :rev) AND ((maxRev >= :rev) OR (maxRev = -1))", Long.class)
			.setParameter("rev", rev)
			.getSingleResult();
	}
}
