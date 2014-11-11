// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import org.codemancer.cpudl.type.Type;

/** A class to represent a generic CPUDL expression.
 * CPUDL expressions are symbolic in nature, and may therefore refer
 * to quantities that will not become known until run time.
 */
public abstract class Expression {
	/** The type of this expression. */
	private Type type;

	/** Construct an expression of given type.
	 * @param type the required type
	 */
	public Expression(Type type) {
		this.type = type;
	}

	/** Get the type of this expression.
	 * @return the type
	 */
	public final Type getType() {
		return type;
	}

	/** Convert this expression to a string.
	 * @return this expression as a string
	 */
	public abstract String unparse();
}
