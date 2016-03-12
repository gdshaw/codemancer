// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db.jpa;

import javax.persistence.Entity;

/** A class to represent a disassembled line of assembly language. */
@Entity
public class Line extends Fact implements org.codemancer.db.Line {
	/** The first address occupied this line. */
	private long minAddr;

	/** The last address occupied this line. */
	private long maxAddr;

	/** The disassembled instruction. */
	private final String instruction;

	/** Construct empty line.
	 * A default constructor is required by the JPA.
	 */
	protected Line() {
		super();
		this.minAddr = 0;
		this.maxAddr = 0;
		this.instruction = null;
	}

	/** Construct disassembled line of assembly language.
	 * @param minRev the lowest revision number for which this line is present
	 * @param maxRev the highest revision number for which this line is present
	 * @param minAddr the first address occupied by this line
	 * @param maxAddr the last address occupied by this line
	 * @param instruction the disassembled instruction
	 */
	protected Line(long minRev, long maxRev, long minAddr, long maxAddr, String instruction) {
		super(minRev, maxRev);
		this.minAddr = minAddr;
		this.maxAddr = maxAddr;
		this.instruction = instruction;
	}

	/** Get first addr.
	 * @return the first address occupied by this line
	 */
	public final long getMinAddr() {
		return minAddr;
	}

	/** Get last addr.
	 * @return the last address occupied by this line
	 */
	public final long getMaxAddr() {
		return maxAddr;
	}

	/** Get instruction.
	 * @return the disassembled instruction
	 */
	public final String getInstruction() {
		return instruction;
	}

	/** Convert to JSON.
	 * @return this line, as JSON.
	 */
	public final String asJSON() {
		StringBuffer buffer = new StringBuffer(100);
		buffer.append("[0x");
		buffer.append(Long.toHexString(minAddr));
		buffer.append(",0x");
		buffer.append(Long.toHexString(maxAddr));
		buffer.append(",\"");
		if (instruction != null) {
			buffer.append(instruction);
		}
		buffer.append("\"]");
		return buffer.toString();
	}
}
