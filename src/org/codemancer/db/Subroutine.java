// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

/** An interface to represent a subroutine. */
public interface Subroutine extends Fact {
	/** Get entry point.
	 * @return the entry point for this subroutine
	 */
	long getEntryAddr();

	/** Allocate SSA expression name.
	 * @return the name
	 */
	String allocateSsaName();
}
