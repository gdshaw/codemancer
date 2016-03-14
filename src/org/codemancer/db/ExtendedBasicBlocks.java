// This file is part of Codemancer.
// Copyright 2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import java.util.List;

/** An interface to represent the collection of extended basic blocks in a Codemancer database. */
public interface ExtendedBasicBlocks {
	/** Make new extended basic block.
	 * @param entryAddr the entry point for this extended basic block
	 * @return the newly-created extended basic block
	 */
	ExtendedBasicBlock make(long entryAddr);

	/** Get all extended basic blocks.
	 * @return a list of extended basic blocks
	 */
	List<ExtendedBasicBlock> get();

	/** Get extended basic blocks in a given subroutine.
	 * @param sub the subroutine
	 * @return a list of extended basic blocks
	 */
	List<ExtendedBasicBlock> getMembersOf(Subroutine sub);

	/** Get unprocessed extended basic blocks.
	 * @param requiredLevel the required level of processing to be omitted from the result
	 * @return a list of unprocessed extended basic blocks
	 */
	List<ExtendedBasicBlock> getUnprocessed(int requiredLevel);

	/** Get the number of extended basic blocks in the database.
	 * @param rev the revision for which results are required
	 * @return the number of extended basic blocks
	 */
	long count(long rev);
}
