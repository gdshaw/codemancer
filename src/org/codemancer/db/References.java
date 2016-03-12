// This file is part of Codemancer.
// Copyright 2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import java.util.List;

/** An interface to represent the collection of references in a Codemancer database. */
public interface References {
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
}
