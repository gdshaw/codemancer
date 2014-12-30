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
	/** The address of the instruction to which this mapping applies. */
	private long addr;

	/** True if this mapping applies on entry to the instruction, false if on exit. */
	private boolean inbound;

	/** The name of the register containing the value mapped. */
	private String name;

	/** The SSA expression to which the register is mapped. */
	@ManyToOne(fetch=FetchType.EAGER)
	private SsaExpression value;

	/** Construct empty SSA mapping.
	 * A default constructor is required by the JPA.
	 */
	protected SsaMapping() {
		super();
		this.addr = 0;
		this.inbound = false;
		this.name = null;
		this.value = null;
	}

	/** Construct SSA mapping.
	 * If this mapping applies to all instructions in a basic block then maxAddr should be
	 * set to the address of the first memory location after the end of the basic block.
	 * @param minRev the lowest revision number for which this mapping applies
	 * @param maxRev the highest revision number for which this mapping applies
	 * @param addr the address to which this mapping applies
	 * @param inbound true if this mapping applies on entry, false if on exit
	 * @param name the name of the register containing the variable mapped
	 * @param value the SSA expression to which the register is mapped
	 */
	public SsaMapping(long minRev, long maxRev, long addr, boolean inbound, String name, SsaExpression value) {
		super(minRev, maxRev);
		this.addr = addr;
		this.inbound = inbound;
		this.name = name;
		this.value = value;
	}

	/** Get the address to which this mapping applies.
	 * @return the address
	 */
	public long getAddr() {
		return addr;
	}

	/** Check whether this mapping applies on entry or exit.
	 * @return true on entry, false on exit
	 */
	public boolean isInbound() {
		return inbound;
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
