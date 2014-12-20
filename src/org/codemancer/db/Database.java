// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import java.util.List;
import java.util.Properties;
import javax.persistence.EntityTransaction;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/** A class to represent a Codemancer database. */
public class Database {
	/** The entity manager for this database. */
	private final EntityManager em;

	/** Open database.
	 * @param url the url of the database to be opened.
	 */
	public Database(String url) {
		Properties props = new Properties();
		props.setProperty("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.ClientDriver");
		props.setProperty("javax.persistence.jdbc.url", url + ";create=true");
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.codemancer", props);
		em = emf.createEntityManager();
	}

	/** Get entity manager.
	 * @return the entity manager
	 */
	public final EntityManager getEntityManager() {
		return em;
	}

	/** Get transaction object.
	 * @return a transaction object
	 */
	public final EntityTransaction getTransaction() {
		return em.getTransaction();
	}

	/** Get unprocessed references.
	 * @return a list of unprocessed references
	 */
	public final List<Reference> getUnprocessedReferences() {
		return em.createQuery(
			"FROM Reference WHERE processed = false", Reference.class)
			.getResultList();
	}

	/** Get lines of disassembled code.
	 * @return a list of lines
	 */
	public final List<Line> getLines() {
		return em.createQuery(
			"FROM Line ORDER BY minAddr", Line.class)
			.getResultList();
	}
}
