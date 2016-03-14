// This file is part of Codemancer.
// Copyright 2015-2016 Graham Shaw.
// All rights reserved.

package org.codemancer.db.jpa;

import javax.persistence.EntityManager;

/** A class to represent a Codemancer database revision. */
public class Revision implements org.codemancer.db.Revision {
	/** The entity manager for the database to which this revision refers. */
	private EntityManager em;

	/** The persistent revision data for this revision. */
	private RevisionData data;

	/** Construct revision.
	 * @param em the entity manager for the database to which this revision refers
	 * @param data the persistent revision data
	 */
	protected Revision(EntityManager em, RevisionData data) {
		this.em = em;
		this.data = data;
	}

	/** Construct revision.
	 * @param em the entity manager for the database to which this revision refers
	 * @param rev the revision number
	 * @param committed true if this revision has been committed, otherwise false
	 */
	protected Revision(EntityManager em, long rev, boolean committed) {
		this.em = em;
		this.data = new RevisionData();
		this.data.rev = rev;
		this.data.committed = committed;
		em.persist(this.data);
	}

	/** Get the revision number for this revision.
	 * @return the revision number
	 */
	public final synchronized long get() {
		return data.rev;
	}

	public final synchronized boolean isCommitted() {
		return data.committed;
	}

	public final synchronized void await() {
		while (!data.committed) {
			try {
				wait();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	public final synchronized void commit() {
		data.committed = true;
		em.getTransaction().commit();
		em.getTransaction().begin();
		notifyAll();
	}
}
