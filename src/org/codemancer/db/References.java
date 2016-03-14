// This file is part of Codemancer.
// Copyright 2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import java.util.List;

/** An interface to represent the collection of references in a Codemancer database. */
public interface References {
	/** Make new reference.
	 * @param srcAddr the address from which this reference originates, or -1 if not applicable
	 * @param dstAddr the address to which this reference refers
	 * @param dataRef true if there is evidence that this is a reference to data, otherwise false
	 * @param codeRef true if there is evidence that this is a reference to code, otherwise false
	 * @param subRef true if there is evidence that this is a reference to a subroutine, otherwise false
	 * @return the newly-created reference
	 */
	Reference make(long srcAddr, long dstAddr, boolean internal, boolean dataRef, boolean codeRef, boolean subRef);

	/** Get all references to a given address range.
	 * @param minAddr the lowest address to include
	 * @param maxAddr the highest address to include
	 * @return a list of references
	 */
	List<Reference> getByDstAddr(long minAddr, long maxAddr);

	/** Get unprocessed references.
	 * @param requiredLevel the required level of processing to be omitted from the result
	 * @return a list of unprocessed references
	 */
	List<Reference> getUnprocessed(int requiredLevel);

	/** Get next address that is the destination of a reference.
	 * @param addr the address at which to begin the search
	 * @return the destination address, or null if none found
	 */
	Long findNextDestination(long addr);
}
