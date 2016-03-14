// This file is part of Codemancer.
// Copyright 2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import java.util.List;

/** An interface to represent the collection of basic blocks in a Codemancer database. */
public interface BasicBlocks {
	/** Make new basic block.
	 * @param minAddr the first address that is part of this basic block
	 * @param maxAddr the last address that is part of this basic block
	 * @param fallThrough true if execution call fall through to the next basic block,
	 *  otherwise false
	 * @return the newly-created basic block
	 */
	BasicBlock make(long minAddr, long maxAddr, boolean fallThrough);

	/** Get basic block containing a given address.
	 * @param addr an address within the requested basic block
	 * @return the basic block, or null if none
	 */
	BasicBlock getContaining(long addr);

	/** Get basic block ending immediately prior to a given address.
	 * @param addr the address immediately following the requested basic block
	 * @return the basic block, or null if none
	 */
	BasicBlock getPrevious(long addr);

	/** Get all basic blocks.
	 * @return a list of basic blocks
	 */
	List<BasicBlock> get();

	/** Get basic blocks in a given extended basic block.
	 * @param ebb the extended basic block
	 * @return a list of basic blocks
	 */
	List<BasicBlock> getMembersOf(ExtendedBasicBlock ebb);

	/** Get basic blocks in a given subroutine.
	 * @param sub the subroutine
	 * @return a list of basic blocks
	 */
	List<BasicBlock> getMembersOf(Subroutine sub);

	/** Get unprocessed basic blocks.
	 * @param requiredLevel the required level of processing to be omitted from the result
	 * @return a list of unprocessed basic blocks
	 */
	List<BasicBlock> getUnprocessed(int requiredLevel);

	/** Get the number of basic blocks in the database.
	 * @param rev the revision for which results are required
	 * @return the number of basic blocks
	 */
	long count(long rev);
}
