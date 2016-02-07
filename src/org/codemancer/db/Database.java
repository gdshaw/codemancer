// This file is part of Codemancer.
// Copyright 2014-2015 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import javax.persistence.EntityTransaction;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.NoResultException;

/** A class to represent a Codemancer database. */
public class Database {
	/** The entity manager for this database. */
	private final EntityManager em;

	/** The current revision number for this database. */
	private final Revision revision;

	/** Open database.
	 * @param url the url of the database to be opened.
	 */
	public Database(String url) {
		Properties props = new Properties();
		props.setProperty("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.ClientDriver");
		props.setProperty("javax.persistence.jdbc.url", url);
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.codemancer", props);
		em = emf.createEntityManager();

		Revision tempRevision = null;
		try {
			tempRevision = em.createQuery("FROM Revision WHERE id = 0", Revision.class).getSingleResult();
		} catch (NoResultException ex) {
			tempRevision = new Revision();
			em.getTransaction().begin();
			em.persist(tempRevision);
			em.getTransaction().commit();
		}
		revision = tempRevision;
	}

	/** Get entity manager.
	 * @return the entity manager
	 */
	public final EntityManager getEntityManager() {
		return em;
	}

	/** Get transaction object.
	 * @return a transaction object
	 */
	public final EntityTransaction getTransaction() {
		return em.getTransaction();
	}

	/** Get current revision number.
	 * @return the object representing the current revision number
	 */
	public final Revision getRevision() {
		return revision;
	}

	/** Get basic block containing a given address.
	 * @param addr an address within the requested basic block
	 * @return the basic block, or null if none
	 */
	public final BasicBlock getBasicBlock(long addr) {
		List<BasicBlock> blocks = em.createQuery(
			"FROM BasicBlock WHERE minAddr <= :addr AND maxAddr >= :addr", BasicBlock.class)
			.setParameter("addr", addr)
			.getResultList();
		if (blocks.size() == 0) {
			return null;
		} else if (blocks.size() == 1) {
			return blocks.get(0);
		} else {
			throw new IllegalStateException("multiple basic blocks found ending at the same start address");
		}
	}

	/** Get basic block ending immediately prior to a given address.
	 * @param addr the address immediately following the requested basic block
	 * @return the basic block, or null if none
	 */
	public final BasicBlock getPreviousBasicBlock(long addr) {
		List<BasicBlock> blocks = em.createQuery(
			"FROM BasicBlock WHERE maxAddr = :maxAddr", BasicBlock.class)
			.setParameter("maxAddr", addr - 1)
			.getResultList();
		if (blocks.size() == 0) {
			return null;
		} else if (blocks.size() == 1) {
			return blocks.get(0);
		} else {
			throw new IllegalStateException("multiple basic blocks found ending at the same end address");
		}
	}

	/** Get SSA expression with a given name in a given subroutine.
	 * @param subroutine the subroutine containing the required expression
	 * @param name the name of the required expression
	 * @return the SSA expression
	 */
	public final SsaExpression getSsaExpression(Subroutine subroutine, String name) {
		return em.createQuery(
			"FROM SsaExpression WHERE subroutine = :subroutine AND name = :name", SsaExpression.class)
			.setParameter("subroutine", subroutine)
			.setParameter("name", name)
			.getSingleResult();
	}

	/** Get SSA mappings for a given address.
	 * @param addr the address for which mappings are required
	 * @return a list of mappings
	 */
	public final List<SsaMapping> getSsaMappings(long addr) {
		return em.createQuery(
			"FROM SsaMapping WHERE addr = :addr", SsaMapping.class)
			.setParameter("addr", addr)
			.getResultList();
	}

	/** Get comments for a given address.
	 * @param addr the address for which comments are required
	 * @return a list of comments
	 */
	public final List<Comment> getComments(long addr) {
		return em.createQuery(
			"FROM Comment WHERE addr = :addr", Comment.class)
			.setParameter("addr", addr)
			.getResultList();
	}

	/** Get unprocessed references.
	 * @param requiredLevel the required level of processing to be omitted from the result
	 * @return a list of unprocessed references
	 */
	public final List<Reference> getUnprocessedReferences(int requiredLevel) {
		return em.createQuery(
			"FROM Reference WHERE processedLevel < :requiredLevel", Reference.class)
			.setParameter("requiredLevel", requiredLevel)
			.getResultList();
	}

	/** Get unprocessed lines of disassembled code.
	 * @param requiredLevel the required level of processing to be omitted from the result
	 * @return a list of unprocessed lines
	 */
	public final List<Line> getUnprocessedLines(int requiredLevel) {
		return em.createQuery(
			"FROM Line WHERE processedLevel < :requiredLevel", Line.class)
			.setParameter("requiredLevel", requiredLevel)
			.getResultList();
	}

	/** Get unprocessed basic blocks.
	 * @param requiredLevel the required level of processing to be omitted from the result
	 * @return a list of unprocessed basic blocks
	 */
	public final List<BasicBlock> getUnprocessedBasicBlocks(int requiredLevel) {
		return em.createQuery(
			"FROM BasicBlock WHERE processedLevel < :requiredLevel", BasicBlock.class)
			.setParameter("requiredLevel", requiredLevel)
			.getResultList();
	}

	/** Get unprocessed extended basic blocks.
	 * @param requiredLevel the required level of processing to be omitted from the result
	 * @return a list of unprocessed extended basic blocks
	 */
	public final List<ExtendedBasicBlock> getUnprocessedExtendedBasicBlocks(int requiredLevel) {
		return em.createQuery(
			"FROM ExtendedBasicBlock WHERE processedLevel < :requiredLevel", ExtendedBasicBlock.class)
			.setParameter("requiredLevel", requiredLevel)
			.getResultList();
	}

	/** Get basic blocks in a given extended basic block.
	 * @param ebb the extended basic block
	 * @return a list of basic blocks
	 */
	public final List<BasicBlock> getBasicBlocksIn(ExtendedBasicBlock ebb) {
		return em.createQuery(
			"FROM BasicBlock WHERE ebb = :ebb", BasicBlock.class)
			.setParameter("ebb", ebb)
			.getResultList();
	}

	/** Get extended basic blocks in a given subroutine.
	 * @param sub the subroutine
	 * @return a list of extended basic blocks
	 */
	public final List<ExtendedBasicBlock> getExtendedBasicBlocksIn(Subroutine sub) {
		return em.createQuery(
			"FROM ExtendedBasicBlock WHERE subroutine = :sub", ExtendedBasicBlock.class)
			.setParameter("sub", sub)
			.getResultList();
	}

	/** Get all references to a given address range.
	 * @param minAddr the lowest address to include
	 * @param maxAddr the highest address to include
	 * @return a list of references
	 */
	public final List<Reference> getReferences(long minAddr, long maxAddr) {
		return em.createQuery(
			"FROM Reference WHERE (dstAddr >= :minAddr) AND (dstAddr <= :maxAddr) ORDER BY minAddr", Reference.class)
			.setParameter("minAddr", minAddr)
			.setParameter("maxAddr", maxAddr)
			.getResultList();
	}

	/** Get all lines of disassembled code in given address range.
	 * @param minRev the earliest revision for which results are required
	 * @param maxRev the latest revision for which results are required
	 * @param minAddr the lowest address to include
	 * @param maxAddr the highest address to include
	 * @return a list of lines
	 */
	public final List<Line> getLines(long minRev, long maxRev, long minAddr, long maxAddr) {
		return em.createQuery(
			"FROM Line where (minRev >= :minRev) AND (minRev <= :maxRev) AND (maxRev = -1) AND (minAddr >= :minAddr) AND (maxAddr <= :maxAddr) ORDER BY minAddr", Line.class)
			.setParameter("minRev", minRev)
			.setParameter("maxRev", maxRev)
			.setParameter("minAddr", minAddr)
			.setParameter("maxAddr", maxAddr)
			.getResultList();
	}

	/** Get all basic blocks.
	 * @return a list of basic blocks
	 */
	public final List<BasicBlock> getBasicBlocks() {
		return em.createQuery(
			"FROM BasicBlock ORDER BY minAddr", BasicBlock.class)
			.getResultList();
	}

	/** Get all extended basic blocks.
	 * @return a list of extended basic blocks
	 */
	public final List<ExtendedBasicBlock> getExtendedBasicBlocks() {
		return em.createQuery(
			"FROM ExtendedBasicBlock ORDER BY entryAddr", ExtendedBasicBlock.class)
			.getResultList();
	}

	/** Get all subroutines.
	 * @return a list of subroutines
	 */
	public final List<Subroutine> getSubroutines() {
		return em.createQuery(
			"FROM Subroutine ORDER BY entryAddr", Subroutine.class)
			.getResultList();
	}

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
	public final Map<Long, Subroutine> getSubroutines(long minRev, long maxRev) {
		List<Subroutine> subroutines = em.createQuery(
			"FROM Subroutine where ((minRev >= :minRev) AND (minRev <= :maxRev)) OR ((maxRev >= :minRev) AND (maxRev <= :maxRev)) ORDER BY minRev", Subroutine.class)
			.setParameter("minRev", minRev)
			.setParameter("maxRev", maxRev)
			.getResultList();

		Map<Long, Subroutine> filteredSubroutines = new HashMap<Long, Subroutine>();
		for (Subroutine subroutine: subroutines) {
			if ((subroutine.getMaxRev() >= maxRev) || (subroutine.getMaxRev() == -1)) {
				filteredSubroutines.put(subroutine.getEntryAddr(), subroutine);
			} else {
				filteredSubroutines.put(subroutine.getEntryAddr(), null);
			}
		}

		return filteredSubroutines;
	}
}
