// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityTransaction;
import javax.persistence.EntityManager;

/** An interface to represent a Codemancer database. */
public interface Database {
	/** Get entity manager.
	 * @return the entity manager
	 */
	EntityManager getEntityManager();

	/** Get transaction object.
	 * @return a transaction object
	 */
	EntityTransaction getTransaction();

	/** Get current revision number.
	 * @return the object representing the current revision number
	 */
	Revision getRevision();

	/** Get basic block containing a given address.
	 * @param addr an address within the requested basic block
	 * @return the basic block, or null if none
	 */
	BasicBlock getBasicBlock(long addr);

	/** Get basic block ending immediately prior to a given address.
	 * @param addr the address immediately following the requested basic block
	 * @return the basic block, or null if none
	 */
	BasicBlock getPreviousBasicBlock(long addr);

	/** Get SSA expression with a given name in a given subroutine.
	 * @param subroutine the subroutine containing the required expression
	 * @param name the name of the required expression
	 * @return the SSA expression
	 */
	SsaExpression getSsaExpression(Subroutine subroutine, String name);

	/** Get SSA mappings for a given address.
	 * @param addr the address for which mappings are required
	 * @return a list of mappings
	 */
	List<SsaMapping> getSsaMappings(long addr);

	/** Get comments for a given address.
	 * @param addr the address for which comments are required
	 * @return a list of comments
	 */
	List<Comment> getComments(long addr);

	/** Get unprocessed references.
	 * @param requiredLevel the required level of processing to be omitted from the result
	 * @return a list of unprocessed references
	 */
	List<Reference> getUnprocessedReferences(int requiredLevel);

	/** Get unprocessed lines of disassembled code.
	 * @param requiredLevel the required level of processing to be omitted from the result
	 * @return a list of unprocessed lines
	 */
	List<Line> getUnprocessedLines(int requiredLevel);

	/** Get unprocessed basic blocks.
	 * @param requiredLevel the required level of processing to be omitted from the result
	 * @return a list of unprocessed basic blocks
	 */
	List<BasicBlock> getUnprocessedBasicBlocks(int requiredLevel);

	/** Get unprocessed extended basic blocks.
	 * @param requiredLevel the required level of processing to be omitted from the result
	 * @return a list of unprocessed extended basic blocks
	 */
	List<ExtendedBasicBlock> getUnprocessedExtendedBasicBlocks(int requiredLevel);

	/** Get basic blocks in a given extended basic block.
	 * @param ebb the extended basic block
	 * @return a list of basic blocks
	 */
	List<BasicBlock> getBasicBlocksIn(ExtendedBasicBlock ebb);

	/** Get basic blocks in a given subroutine.
	 * @param sub the subroutine
	 * @return a list of basic blocks
	 */
	List<BasicBlock> getBasicBlocksIn(Subroutine sub);

	/** Get extended basic blocks in a given subroutine.
	 * @param sub the subroutine
	 * @return a list of extended basic blocks
	 */
	List<ExtendedBasicBlock> getExtendedBasicBlocksIn(Subroutine sub);

	/** Get all references to a given address range.
	 * @param minAddr the lowest address to include
	 * @param maxAddr the highest address to include
	 * @return a list of references
	 */
	List<Reference> getReferences(long minAddr, long maxAddr);

	/** Get all lines of disassembled code in given address range.
	 * @param minRev the earliest revision for which results are required
	 * @param maxRev the latest revision for which results are required
	 * @param minAddr the lowest address to include
	 * @param maxAddr the highest address to include
	 * @return a list of lines
	 */
	List<Line> getLines(long minRev, long maxRev, long minAddr, long maxAddr);

	/** Get all lines of disassembled code that lie within a given set of address ranges.
	 * @param minRev the earliest revision for which results are required
	 * @param maxRev the latest revision for which results are required
	 * @param ranges the set of address ranges
	 * @return a list of lines
	 */
	List<Line> getLines(long minRev, long maxRev, AddressRangeSet ranges);

	/** Get all basic blocks.
	 * @return a list of basic blocks
	 */
	List<BasicBlock> getBasicBlocks();

	/** Get all extended basic blocks.
	 * @return a list of extended basic blocks
	 */
	List<ExtendedBasicBlock> getExtendedBasicBlocks();

	/** Get subroutine with given entry address.
	 * @param entryAddr the required entry address
	 * @param rev the revision number for which a result is required
	 * @return the subroutine with that entry address, or null if not found
	 */
	Subroutine getSubroutineAt(long entryAddr, long rev);

	/** Get all subroutines.
	 * @return a list of subroutines
	 */
	List<Subroutine> getSubroutines();

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
	Map<Long, Subroutine> getSubroutines(long minRev, long maxRev);
}
