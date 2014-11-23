// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent a subtraction operation. */
public class Subtraction extends BinaryExpression {
	/** Construct subtraction operation from two arguments.
	 * @param type the required type of the result
	 * @param lhs the left-hand argument
	 * @param rhs the right-hand argument
	 */
	public Subtraction(Type type, Expression lhs, Expression rhs) {
		super(type, lhs, rhs);
	}

	public Expression partialClone(Expression lhs, Expression rhs) {
		return new Subtraction(getType(), lhs, rhs);
	}

	public String getSymbol() {
		return "-";
	}

	public Expression simplify() {
		Accumulator acc = new Accumulator(getLhs().getType());
		accumulate(acc, 1);
		return acc.simplify();
	}

	public Expression solve(Reference solveFor, Expression placeholder) {
		Expression solveLhs = getLhs().solve(solveFor, new Addition(getType(), getRhs(), placeholder));
		if (solveLhs != null) {
			return solveLhs.simplify();
		}
		Expression solveRhs = getRhs().solve(solveFor, new Subtraction(getType(), getLhs(), placeholder));
		if (solveRhs != null) {
			return solveRhs.simplify();
		}
		return null;
	}

	public void accumulate(Accumulator acc, long multiplier) {
		getLhs().accumulate(acc, multiplier);
		getRhs().accumulate(acc, -multiplier);
	}

	/** Make subtraction expression from XML element.
	 * If the element contains two or more subexpressions then the result is
	 * a subtraction expression.
	 * If there is only one subexpression then the result is that subexpression.
	 * If there are no subexpressions then the result is zero.
	 * @param element the subtraction as an XML element
	 * @return the subtraction as an object
	 */
	public static Expression make(Element element) throws CpudlParseException {
		Expression result = null;
		Node child = element.getFirstChild();
		while (child != null) {
			Expression operand = Expression.make(child);
			if (operand != null) {
				if (result == null) {
					result = operand;
				} else {
					result = new Subtraction(result.getType(), result, operand);
				}
			}
			child = child.getNextSibling();
		}
		if (result == null) {
			result = new Constant(null, 0);
		}
		return result;
	}
}
