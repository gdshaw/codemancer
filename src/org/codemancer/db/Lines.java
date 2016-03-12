// This file is part of Codemancer.
// Copyright 2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import java.util.List;

import org.codemancer.db.AddressRangeSet;

/** An interface to represent the collection of lines in a Codemancer database. */
public interface Lines {
	/** Make new disassembled line of assembly language.
	 * @param minRev the lowest revision number for which this line is present
	 * @param maxRev the highest revision number for which this line is present
	 * @param minAddr the first address occupied by this line
	 * @param maxAddr the last address occupied by this line
	 * @param instruction the disassembled instruction
	 * @return the newly-created line
	 */
	Line make(long minRev, long maxRev, long minAddr, long maxAddr, String instruction);

	/** Get lines in a given basic block.
	 * @param bb the basic block
	 * @return a list of lines
	 */
	List<Line> getMembersOf(BasicBlock bb);

	/** Get all lines of disassembled code in given address range.
	 * @param minRev the earliest revision for which results are required
	 * @param maxRev the latest revision for which results are required
	 * @param minAddr the lowest address to include
	 * @param maxAddr the highest address to include
	 * @return a list of lines
	 */
	List<Line> getChanges(long minRev, long maxRev, long minAddr, long maxAddr);

	/** Get all lines of disassembled code that lie within a given set of address ranges.
	 * @param minRev the earliest revision for which results are required
	 * @param maxRev the latest revision for which results are required
	 * @param ranges the set of address ranges
	 * @return a list of lines
	 */
	List<Line> getChanges(long minRev, long maxRev, AddressRangeSet ranges);

	/** Get unprocessed lines of disassembled code.
	 * @param requiredLevel the required level of processing to be omitted from the result
	 * @return a list of unprocessed lines
	 */
	List<Line> getUnprocessed(int requiredLevel);

	/** Find address of first disassembled line, at or following a given address.
	 * @param addr the address at which to begin the search
	 * @return the address of the first disassembled line, or null if not found
	 */
	Long findFirstAddr(long addr);

	/** Get the number of disassembled lines in the database.
	 * @param rev the revision for which results are required
	 * @return the number of lines
	 */
	long count(long rev);
}
