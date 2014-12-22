// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import java.util.List;
import java.util.Properties;
import javax.persistence.EntityTransaction;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/** A class to represent a Codemancer database. */
public class Database {
	/** The entity manager for this database. */
	private final EntityManager em;

	/** Open database.
	 * @param url the url of the database to be opened.
	 */
	public Database(String url) {
		Properties props = new Properties();
		props.setProperty("javax.persistence.jdbc.driver", "org.apache.derby.jdbc.ClientDriver");
		props.setProperty("javax.persistence.jdbc.url", url + ";create=true");
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.codemancer", props);
		em = emf.createEntityManager();
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

	/** Get basic block containing a given address
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

	/** Get basic block ending immediately prior to a given address
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
	 * @param minAddr the lowest address to include
	 * @param maxAddr the highest address to include
	 * @return a list of lines
	 */
	public final List<Line> getLines(long minAddr, long maxAddr) {
		return em.createQuery(
			"FROM Line where (minAddr >= :minAddr) AND (maxAddr <= :maxAddr) ORDER BY minAddr", Line.class)
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
}
