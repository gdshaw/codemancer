// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db.jpa;

import java.util.List;
import java.util.Properties;
import javax.persistence.EntityTransaction;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.NoResultException;

import org.codemancer.db.AddressRangeSet;

/** A class to represent a Codemancer database. */
public class Database implements org.codemancer.db.Database {
	/** The entity manager for this database. */
	private final EntityManager em;

	/** The collection of references for this database. */
	private References references = null;

	/** The collection of lines for this database. */
	private Lines lines = null;

	/** The collection of basic blocks for this database. */
	private BasicBlocks basicBlocks = null;

	/** The collection of extended basic blocks for this database. */
	private ExtendedBasicBlocks extendedBasicBlocks = null;

	/** The collection of subroutines for this database. */
	private Subroutines subroutines = null;

	/** The collection of comments for this database. */
	private Comments comments = null;

	/** The collection of SSA expressions for this database. */
	private SsaExpressions ssaExpressions = null;

	/** The collection of SSA mappings for this database. */
	private SsaMappings ssaMappings = null;

	/** Open database.
	 * @param url the url of the database to be opened.
	 */
	public Database(String url) {
		Properties props = new Properties();
		props.setProperty("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.ClientDriver");
		props.setProperty("javax.persistence.jdbc.url", url);
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.codemancer", props);
		em = emf.createEntityManager();
	}

	public final EntityTransaction getTransaction() {
		return em.getTransaction();
	}

	public final org.codemancer.db.Revision getRevision(long rev) {
		List<Revision> revisions = em.createQuery(
			"FROM Revision WHERE rev = :rev", Revision.class)
			.setParameter("rev", rev)
			.getResultList();
		Revision revision = revisions.isEmpty() ? null : revisions.get(0);
		if (revision == null) {
			revision = new Revision(rev, (rev == 0));
			em.getTransaction().begin();
			em.persist(revision);
			em.getTransaction().commit();
		}
		return revision;
	}

	public final org.codemancer.db.Revision getCurrentRevision() {
		List<Revision> revisions = em.createQuery(
			"FROM Revision WHERE committed = TRUE ORDER BY rev DESC LIMIT 1", Revision.class)
			.getResultList();
		return (revisions.isEmpty()) ? getRevision(0) : revisions.get(0);
	}

	public final org.codemancer.db.Revision getNextRevision() {
		List<Revision> revisions = em.createQuery(
			"FROM Revision WHERE committed = FALSE ORDER BY rev ASC LIMIT 1", Revision.class)
			.getResultList();
		return revisions.isEmpty() ? getRevision(getCurrentRevision().get() + 1) : revisions.get(0);
	}

	public final org.codemancer.db.Lines getLines() {
		if (lines == null) {
			lines = new Lines(em);
		}
		return lines;
	}

	public final org.codemancer.db.References getReferences() {
		if (references == null) {
			references = new References(em);
		}
		return references;
	}

	public final org.codemancer.db.Comments getComments() {
		if (comments == null) {
			comments = new Comments(em);
		}
		return comments;
	}

	public final org.codemancer.db.BasicBlocks getBasicBlocks() {
		if (basicBlocks == null) {
			basicBlocks = new BasicBlocks(em);
		}
		return basicBlocks;
	}

	public final org.codemancer.db.ExtendedBasicBlocks getExtendedBasicBlocks() {
		if (extendedBasicBlocks == null) {
			extendedBasicBlocks = new ExtendedBasicBlocks(em);
		}
		return extendedBasicBlocks;
	}

	public final org.codemancer.db.Subroutines getSubroutines() {
		if (subroutines == null) {
			subroutines = new Subroutines(em);
		}
		return subroutines;
	}

	public final org.codemancer.db.SsaExpressions getSsaExpressions() {
		if (ssaExpressions == null) {
			ssaExpressions = new SsaExpressions(em);
		}
		return ssaExpressions;
	}

	public final org.codemancer.db.SsaMappings getSsaMappings() {
		if (ssaMappings == null) {
			ssaMappings = new SsaMappings(em);
		}
		return ssaMappings;
	}
}
