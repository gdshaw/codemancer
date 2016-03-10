// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db.jpa;

import javax.persistence.Entity;

/** A class to represent references to or between addresses within an executable.
 * References may be internal or external. Examples of internal references would
 * include:
 * - jumps and subroutine calls within the executable code; and
 * - pointers identified within the associated data.
 * Examples of external references would include:
 * - the entry point, if the object file defines one;
 * - global symbols, if the object file has a symbol table; and
 * - any reset or interrupt vectors defined by the processor architecture.
 * If isDataRef is true then isCodeRef should normally be false, and vice versa,
 * however this should not be assumed as there may be conflicting evidence.
 */
@Entity
public class Reference extends Fact implements org.codemancer.db.Reference {
	/** The address from which this reference originates, or -1 if not applicable. */
	private long srcAddr;

	/** The address to which this reference refers. */
	private long dstAddr;

	/** True if this is an internal reference, otherwise false. */
	private boolean internal;

	/** True if there is evidence that this is a reference to data, otherwise false. */
	private boolean dataRef;

	/** True if there is evidence that this is a reference to code, otherwise false. */
	private boolean codeRef;

	/** True if there is evidence that this is a reference to a subroutine, otherwise false. */
	private boolean subRef;

	/** Construct empty reference.
	 * A default constructor is required by the JPA.
	 */
	protected Reference() {
		super();
		this.srcAddr = 0;
		this.dstAddr = 0;
		this.internal = false;
		this.dataRef = false;
		this.codeRef = false;
		this.subRef = false;
	}

	/** Construct reference.
	 * @param minRev the lowest database revision to which this reference is applicable
	 * @param maxRev the highest database revision to which this reference is applicable,
	 *  or -1 for all higher revisions
	 * @param srcAddr the address from which this reference originates, or -1 if not applicable
	 * @param dstAddr the address to which this reference refers
	 * @param dataRef true if there is evidence that this is a reference to data, otherwise false
	 * @param codeRef true if there is evidence that this is a reference to code, otherwise false
	 * @param subRef true if there is evidence that this is a reference to a subroutine, otherwise false
	 */
	public Reference(long minRev, long maxRev, long srcAddr, long dstAddr, boolean internal,
		boolean dataRef, boolean codeRef, boolean subRef) {
		super(minRev, maxRev);
		this.srcAddr = srcAddr;
		this.dstAddr = dstAddr;
		this.internal = internal;
		this.dataRef = dataRef;
		this.codeRef = codeRef;
		this.subRef = subRef;
	}

	/** Get the address from which this reference originates.
	 * @return the source address, or -1 if not applicable
	 */
	public long getSrcAddr() {
		return srcAddr;
	}

	/** Get the address to which this reference refers.
	 * @return the destination address
	 */
	public long getDstAddr() {
		return dstAddr;
	}

	/** Test whether this is an internal reference.
	 * @return true if this is an internal reference, otherwise false
	 */
	public boolean isInternal() {
		return internal;
	}

	/** Test whether this is a data reference.
	 * @return true if there is evidence that this is a reference to data, otherwise false
	 */
	public boolean isDataRef() {
		return dataRef;
	}

	/** Test whether this is a code reference.
	 * @return true if there is evidence that this is a reference to code, otherwise false
	 */
	public boolean isCodeRef() {
		return codeRef;
	}

	/** Test whether this is a subroutine reference.
	 * @return true if there is evidence that this is a reference to a subroutine, otherwise false
	 */
	public boolean isSubRef() {
		return subRef;
	}
}
