// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

/** An interface to represent references to or between addresses within an executable.
 * References may be internal or external. Examples of internal references would include:
 * - jumps and subroutine calls within the executable code; and
 * - pointers identified within the associated data.
 * Examples of external references would include:
 * - the entry point, if the object file defines one;
 * - global symbols, if the object file has a symbol table; and
 * - any reset or interrupt vectors defined by the processor architecture.
 * If isDataRef is true then isCodeRef should normally be false, and vice versa,
 * however this should not be assumed as there may be conflicting evidence.
 */
public interface Reference extends Fact {
	/** Get the address from which this reference originates.
	 * @return the source address, or -1 if not applicable
	 */
	long getSrcAddr();

	/** Get the address to which this reference refers.
	 * @return the destination address
	 */
	long getDstAddr();

	/** Test whether this is an internal reference.
	 * @return true if this is an internal reference, otherwise false
	 */
	boolean isInternal();

	/** Test whether this is a data reference.
	 * @return true if there is evidence that this is a reference to data, otherwise false
	 */
	boolean isDataRef();

	/** Test whether this is a code reference.
	 * @return true if there is evidence that this is a reference to code, otherwise false
	 */
	boolean isCodeRef();

	/** Test whether this is a subroutine reference.
	 * @return true if there is evidence that this is a reference to a subroutine, otherwise false
	 */
	boolean isSubRef();
}
