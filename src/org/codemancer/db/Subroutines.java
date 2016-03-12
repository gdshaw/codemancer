// This file is part of Codemancer.
// Copyright 2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import java.util.List;
import java.util.Map;

/** An interface to represent the collection of subroutines in a Codemancer database. */
public interface Subroutines {
	/** Get subroutine with given entry address.
	 * @param entryAddr the required entry address
	 * @param rev the revision number for which a result is required
	 * @return the subroutine with that entry address, or null if not found
	 */
	Subroutine getStarting(long entryAddr, long rev);

	/** Get all subroutines.
	 * @return a list of subroutines
	 */
	List<Subroutine> get();

	/** Get changed subroutines.
	 * Subroutines are listed at most once for each entry address, and then
	 * only if a change has occurred within the given range of revisions.
	 * It is the state as of revision maxRev that is reported. Subroutines
	 * that have been deleted with no replacement are represented by a
	 * mapping to null.
	 * @param minRev the earliest revision for which results are required
	 * @param maxRev the latest revision for which results are required
	 * @return the subroutines that have changed, indexed by entry address
	 */
	Map<Long, Subroutine> getChanged(long minRev, long maxRev);

	/** Get the number of subroutines in the database.
	 * @param rev the revision for which results are required
	 * @return the number of subroutines
	 */
	long count(long rev);
}
