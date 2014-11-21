// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent a test for equality. */
public class Equality extends BinaryExpression {
	/** Construct equality operation from two arguments.
	 * @param type the required type of the result
	 * @param lhs the left-hand argument
	 * @param rhs the right-hand argument
	 */
	public Equality(Type type, Expression lhs, Expression rhs) {
		super(type, lhs, rhs);
	}

	public Expression partialClone(Expression lhs, Expression rhs) {
		return new Equality(getType(), lhs, rhs);
	}

	public String getSymbol() {
		return "==";
	}

	public Expression simplify() {
		Expression diff = new Subtraction(null, getLhs(), getRhs());
		diff = diff.simplify();
		if (diff instanceof Constant) {
			Constant constDiff = (Constant)diff;
			if (constDiff.getValue() == 0) {
				return new Constant(null, 1);
			} else {
				return new Constant(null, 0);
			}
		}

		return this;
	}

	/** Make equality expression from XML element.
	 * @param element the equality as an XML element
	 * @return the equality as an object
	 */
	public static Expression make(Element element) throws CpudlParseException {
		List<Expression> operands = new ArrayList<Expression>();
		Node child = element.getFirstChild();
		while (child != null) {
			Expression operand = Expression.make(child);
			if (operand != null) {
				operands.add(operand);
			}
			child = child.getNextSibling();
		}
		if (operands.size() != 2) {
			throw new CpudlParseException(element, "two operands expected for equality");
		}
		return new Equality(null, operands.get(0), operands.get(1));
	}
}

