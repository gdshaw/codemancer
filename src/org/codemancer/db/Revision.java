// This file is part of Codemancer.
// Copyright 2015 Graham Shaw.
// All rights reserved.

package org.codemancer.db;

import javax.persistence.Entity;
import javax.persistence.Id;

/** A class to represent the current revision number of a Codemancer database.
 * Revisions up to and including the current revision have been committed
 * and are therefore visible to the client. There may be entries in the
 * database numbered (revision+1), however these are uncommitted (so far
 * as the application is concerned), may be incomplete, and should not be
 * transmitted to the client.
 */
@Entity
public class Revision {
	/** The unique ID for this object. */
	@Id
	private long id = 0;

	/** The current revision number. */
	private long revision;

	/** Get the most recent committed revision number.
	 * Revisions up to and including this number are visible to the client.
	 * @return the revision number
	 */
	public final synchronized long get() {
		return this.revision;
	}

	/** Wait for a given revision to be committed.
	 * If the requested revision has not been committed then the
	 * calling thread will block until it becomes available. The lock
	 * on this object is released while waiting. If the requested
	 * revision has already been committed at some point in the past
	 * then this function returns immediately.
	 * @param revision the revision to wait for
	 */
	public final synchronized long await(long revision) {
		while (revision > this.revision) {
			try {
				wait();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		return revision;
	}

	/** Commit the revision currently in preparation.
	 * Committing a revision causes it to become visible to the client.
	 * Any threads which were blocked in the function awaitRevision
	 * will be unblocked if this function causes the requested revision
	 * to be reached.
	 */
	public final synchronized void commit() {
		this.revision = this.revision + 1;
		notifyAll();
	}
}
