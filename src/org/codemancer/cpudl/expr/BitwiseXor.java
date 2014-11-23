// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent a bitwise XOR operation. */
public class BitwiseXor extends BinaryExpression {
	/** Construct bitwise XOR operation from two arguments.
	 * @param type the required type of the result
	 * @param lhs the left-hand argument
	 * @param rhs the right-hand argument
	 */
	public BitwiseXor(Type type, Expression lhs, Expression rhs) {
		super(type, lhs, rhs);
	}

	public Expression partialClone(Expression lhs, Expression rhs) {
		return new BitwiseXor(getType(), lhs, rhs);
	}

	public String getSymbol() {
		return "^";
	}

	/** Make bitwise XOR operation from XML element.
	 * If the element contains two or more subexpressions then the result is
	 * a bitwise XOR expression.
	 * If there is only one subexpression then the result is that subexpression.
	 * If there are no subexpressions then the result is zero.
	 * @param ctx the context of this expression
	 * @param the expression as an XML element
	 * @return the expression as an object
	 */
	public static Expression make(Context ctx, Element element) throws CpudlParseException {
		Expression result = null;
		Node child = element.getFirstChild();
		while (child != null) {
			Expression operand = ctx.makeExpression(child);
			if (operand != null) {
				if (result == null) {
					result = operand;
				} else {
					result = new BitwiseXor(result.getType(), result, operand);
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
