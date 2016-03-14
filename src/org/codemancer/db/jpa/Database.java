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
		em.getTransaction().begin();
	}

	public final org.codemancer.db.Revision getRevision(long rev) {
		List<RevisionData> data = em.createQuery(
			"FROM RevisionData WHERE rev = :rev", RevisionData.class)
			.setParameter("rev", rev)
			.getResultList();
		Revision revision = data.isEmpty() ? null : new Revision(em, data.get(0));
		if (revision == null) {
			revision = new Revision(em, rev, (rev == 0));
		}
		return revision;
	}

	public final org.codemancer.db.Revision getCurrentRevision() {
		List<RevisionData> data = em.createQuery(
			"FROM RevisionData WHERE committed = TRUE ORDER BY rev DESC LIMIT 1", RevisionData.class)
			.getResultList();
		return getRevision(data.isEmpty() ? 0 : data.get(0).rev);
	}

	public final org.codemancer.db.Revision getNextRevision() {
		List<RevisionData> data = em.createQuery(
			"FROM RevisionData WHERE committed = TRUE ORDER BY rev DESC LIMIT 1", RevisionData.class)
			.getResultList();
		return getRevision(data.isEmpty() ? 1 : 1 + data.get(0).rev);
	}

	public final org.codemancer.db.Lines getLines() {
		if (lines == null) {
			lines = new Lines(this, em);
		}
		return lines;
	}

	public final org.codemancer.db.References getReferences() {
		if (references == null) {
			references = new References(this, em);
		}
		return references;
	}

	public final org.codemancer.db.Comments getComments() {
		if (comments == null) {
			comments = new Comments(this, em);
		}
		return comments;
	}

	public final org.codemancer.db.BasicBlocks getBasicBlocks() {
		if (basicBlocks == null) {
			basicBlocks = new BasicBlocks(this, em);
		}
		return basicBlocks;
	}

	public final org.codemancer.db.ExtendedBasicBlocks getExtendedBasicBlocks() {
		if (extendedBasicBlocks == null) {
			extendedBasicBlocks = new ExtendedBasicBlocks(this, em);
		}
		return extendedBasicBlocks;
	}

	public final org.codemancer.db.Subroutines getSubroutines() {
		if (subroutines == null) {
			subroutines = new Subroutines(this, em);
		}
		return subroutines;
	}

	public final org.codemancer.db.SsaExpressions getSsaExpressions() {
		if (ssaExpressions == null) {
			ssaExpressions = new SsaExpressions(this, em);
		}
		return ssaExpressions;
	}

	public final org.codemancer.db.SsaMappings getSsaMappings() {
		if (ssaMappings == null) {
			ssaMappings = new SsaMappings(this, em);
		}
		return ssaMappings;
	}
}
