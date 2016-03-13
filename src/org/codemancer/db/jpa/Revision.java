// This file is part of Codemancer.
// Copyright 2015-2016 Graham Shaw.
// All rights reserved.

package org.codemancer.db.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

/** A class to represent a Codemancer database revision. */
@Entity
public class Revision implements org.codemancer.db.Revision {
	/** The revision number. */
	@Id
	private long rev;

	/** True if this revision has been committed, otherwise false. */
	private boolean committed;

	/** Construct default revision.
	 * A default constructor is required by the JPA.
	 */
	protected Revision() {
		this.rev = 0;
		this.committed = true;
	}

	/** Construct revision.
	 * @param rev the revision number
	 * @param committed true if this revision has been committed, otherwise false
	 */
	protected Revision(long rev, boolean committed) {
		this.rev = rev;
		this.committed = committed;
	}

	/** Get the revision number for this revision.
	 * @return the revision number
	 */
	public final synchronized long get() {
		return rev;
	}

	public final synchronized boolean isCommitted() {
		return committed;
	}

	public final synchronized void await() {
		while (!committed) {
			try {
				wait();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}

	public final synchronized void commit() {
		committed = true;
		notifyAll();
	}
}
