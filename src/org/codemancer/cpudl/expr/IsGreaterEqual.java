// This file is part of Codemancer.
// Copyright 2015 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

/** An expression class for testing whether one operand is greater than or equal to another. */
public class IsGreaterEqual extends Comparison {
	/** Construct is-greater-equal comparison from two arguments.
	 * @param lhs the left-hand argument
	 * @param rhs the right-hand argument
	 */
	public IsGreaterEqual(Expression lhs, Expression rhs) {
		super(lhs, rhs);
	}

	public Expression partialClone(Expression lhs, Expression rhs) {
		return new IsGreaterEqual(lhs, rhs);
	}

	public String getSymbol() {
		return ">=";
	}

	public boolean compare(long diff) {
		return (diff >= 0);
	}
}
