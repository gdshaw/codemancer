// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db.jpa;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.EntityManager;

import org.codemancer.db.AddressRangeSet;

/** A class to represent the collection of lines in a Codemancer database. */
class Lines implements org.codemancer.db.Lines {
	/** The entity manager for the database. */
	private final EntityManager em;

	/** Construct collection of lines.
	 * @param em the entity manager for the database
	 */
	protected Lines(EntityManager em) {
		this.em = em;
	}

	public final Line make(long minRev, long maxRev, long minAddr, long maxAddr, String instruction) {
		Line line = new Line(minRev, maxRev, minAddr, maxAddr, instruction);
		em.persist(line);
		return line;
	}

	public final List<org.codemancer.db.Line> getMembersOf(org.codemancer.db.BasicBlock bb) {
		List<Line> lines = em.createQuery(
			"FROM Line WHERE maxRev = -1 AND minAddr >= :minAddr AND minAddr <= :maxAddr ORDER BY minAddr", Line.class)
			.setParameter("minAddr", bb.getMinAddr())
			.setParameter("maxAddr", bb.getMaxAddr())
			.getResultList();
		return new ArrayList<org.codemancer.db.Line>(lines);
	}

	public final List<org.codemancer.db.Line> getChanges(long minRev, long maxRev, long minAddr, long maxAddr) {
		List<Line> lines = em.createQuery(
			"FROM Line where (minRev >= :minRev) AND (minRev <= :maxRev) AND (maxRev = -1) AND (minAddr >= :minAddr) AND (maxAddr <= :maxAddr) ORDER BY minAddr", Line.class)
			.setParameter("minRev", minRev)
			.setParameter("maxRev", maxRev)
			.setParameter("minAddr", minAddr)
			.setParameter("maxAddr", maxAddr)
			.getResultList();
		return new ArrayList<org.codemancer.db.Line>(lines);
	}

	public final List<org.codemancer.db.Line> getChanges(long minRev, long maxRev, AddressRangeSet ranges) {
		List<Line> lines = em.createQuery(
			"FROM Line where (minRev >= :minRev) AND (minRev <= :maxRev) AND (maxRev = -1) AND " + ranges.asJPQL("minAddr") + " ORDER BY minAddr", Line.class)
			.setParameter("minRev", minRev)
			.setParameter("maxRev", maxRev)
			.getResultList();
		return new ArrayList<org.codemancer.db.Line>(lines);
	}

	public final List<org.codemancer.db.Line> getUnprocessed(int requiredLevel) {
		List<Line> lines = em.createQuery(
			"FROM Line WHERE processedLevel < :requiredLevel", Line.class)
			.setParameter("requiredLevel", requiredLevel)
			.getResultList();
		return new ArrayList<org.codemancer.db.Line>(lines);
	}

	public final Long findFirstAddr(long addr) {
		List<Line> existingLines = em.createQuery(
			"FROM Line WHERE maxRev = -1 AND minAddr >= :addr ORDER BY minAddr", Line.class)
			.setParameter("addr", addr)
			.setMaxResults(1)
			.getResultList();
		Long stopAddr = null;
		if (!existingLines.isEmpty()) {
			stopAddr = existingLines.get(0).getMinAddr();
		}
		return stopAddr;
	}

	public final long count(long rev) {
		return em.createQuery("SELECT COUNT(minAddr) FROM Line WHERE (minRev <= :rev) AND ((maxRev >= :rev) OR (maxRev = -1))", Long.class)
			.setParameter("rev", rev)
			.getSingleResult();
	}
}
