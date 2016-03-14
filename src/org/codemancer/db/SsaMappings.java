// This file is part of Codemancer.
// Copyright 2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import java.util.List;

/** An interface to represent the collection of SSA mappings in a Codemancer database. */
public interface SsaMappings {
	/** Make new SSA mapping.
	 * If this mapping applies to all instructions in a basic block then maxAddr should be
	 * set to the address of the first memory location after the end of the basic block.
	 * @param addr the address to which this mapping applies
	 * @param inbound true if this mapping applies on entry, false if on exit
	 * @param name the name of the register containing the variable mapped
	 * @param value the SSA expression to which the register is mapped
	 * @return the newly-created SSA mapping
	 */
	SsaMapping make(long addr, boolean inbound, String name, org.codemancer.db.SsaExpression value);

	/** Get SSA mappings for a given address.
	 * @param addr the address for which mappings are required
	 * @return a list of mappings
	 */
	List<SsaMapping> get(long addr);
}
