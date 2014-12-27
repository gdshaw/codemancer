// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.FetchType;

/** A class for mapping a register to a given SSA expression. */
@Entity
public class SsaMapping extends Fact {
	/** The address of the first instruction for which this mapping applies on entry. */
	private long minAddr;

	/** The address of the first instruction for which this mapping does not apply on exit.
	 * If it applies to all instructions in a basic block then this is the address
	 * of the first memory location after the end of the basic block.
	 */
	private long maxAddr;

	/** The name of the register containing the variable mapped. */
	private String name;

	/** The SSA expression to which the variable is mapped. */
	@ManyToOne(fetch=FetchType.EAGER)
	private SsaExpression value;

	/** Construct empty SSA mapping.
	 * A default constructor is required by the JPA.
	 */
	protected SsaMapping() {
		super();
		this.minAddr = -1;
		this.maxAddr = -1;
		this.name = null;
		this.value = null;
	}

	/** Construct SSA mapping.
	 * If this mapping applies to all instructions in a basic block then maxAddr should be
	 * set to the address of the first memory location after the end of the basic block.
	 * @param minRev the lowest revision number for which this mapping applies
	 * @param maxRev the highest revision number for which this mapping applies
	 * @param minAddr the address of the first instruction for which this mapping applies on entry
	 * @param maxAddr the address of the first instruction for which this mapping does not apply on exit
	 * @param name the name of the register containing the variable mapped
	 * @param value the SSA expression to which the variable is mapped
	 */
	public SsaMapping(long minRev, long maxRev, long minAddr, long maxAddr,
		String name, SsaExpression value) {
		super(minRev, maxRev);
		this.minAddr = minAddr;
		this.maxAddr = maxAddr;
		this.name = name;
		this.value = value;
	}

	/** Get the address of the first instruction for which this mapping applies on entry.
	 * @return the address of the first instruction
	 */
	public long getMinAddr() {
		return minAddr;
	}

	/** Get the address of the first instruction for which this mapping does not apply on exit.
	 * @return the address of the last instruction
	 */
	public long getMaxAddr() {
		return maxAddr;
	}

	/** Get the name of the register or address space containing the variable mapped.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/** Get the SSA expression to which the variable is mapped.
	 * @return the expression
	 */
	public SsaExpression getValue() {
		return value;
	}
}
