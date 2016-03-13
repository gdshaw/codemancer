// This file is part of Codemancer.
// Copyright 2015-2016 Graham Shaw.
// All rights reserved.

package org.codemancer.db;

/** An interface to represent a Codemancer database revision.
 * Revisions are visible to the client when and only when they have been committed.
 */
public interface Revision {
	/** Get the revision number for this revision.
	 * @return the revision number
	 */
	long get();

	/** Wait for this revision to be committed.
	 * If this revision has not been committed then the calling thread
	 * will block until it becomes available. The lock on this object
	 * is released while waiting. If this revision has already been
	 * committed at some point in the past then this function returns
	 * immediately.
	 */
	void await();

	/** Commit this revision.
	 * Committing a revision causes it to become visible to the client.
	 * Any threads which were blocked in the await function will be
	 * unblocked if this function causes the requested revision to be
	 * reached.
	 */
	void commit();
}
