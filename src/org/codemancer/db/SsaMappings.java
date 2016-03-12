// This file is part of Codemancer.
// Copyright 2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import java.util.List;

/** An interface to represent the collection of SSA mappings in a Codemancer database. */
public interface SsaMappings {
	/** Get SSA mappings for a given address.
	 * @param addr the address for which mappings are required
	 * @return a list of mappings
	 */
	List<SsaMapping> get(long addr);
}
