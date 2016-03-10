// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

/** A class for describing an SSA expression. */
public interface SsaExpression extends Fact {
	/** Get the subroutine to which this SSA expression belongs.
	 * @return the subroutine
	 */
	Subroutine getSubroutine();

	/** Get the name of this SSA expression.
	 * @return the name
	 */
	String getName();
}
