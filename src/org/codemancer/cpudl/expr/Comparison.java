// This file is part of Codemancer.
// Copyright 2015 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.type.Type;

/** An abstract expression class to represent a comparison. */
public abstract class Comparison extends BinaryExpression {
	/** Construct comparison operation from two arguments.
	 * @param lhs the left-hand argument
	 * @param rhs the right-hand argument
	 */
	public Comparison(Expression lhs, Expression rhs) {
		super(null, lhs, rhs);
	}

	/** Perform comparison.
	 * @param diff the right hand side minus the left hand side
	 * @return the result of the comparison
	 */
	public abstract boolean compare(long diff);

	public Expression simplify() {
		Expression diff = new Subtraction(getLhs().getType(), getLhs(), getRhs());
		diff = diff.simplify();
		if (diff instanceof Constant) {
			Constant constDiff = (Constant)diff;
			boolean passed = compare(constDiff.getValue());
			return new Constant(null, (passed) ? 1 : 0);
		}
		return this;
	}

	/** Make comparison from XML element.
	 * @param ctx the context of this expression
	 * @param element the equality as an XML element
	 * @return the equality as an object
	 */
	public static Expression make(Context ctx, Element element) throws CpudlParseException {
		List<Expression> operands = new ArrayList<Expression>();
		Node child = element.getFirstChild();
		while (child != null) {
			Expression operand = ctx.makeExpression(child);
			if (operand != null) {
				operands.add(operand);
			}
			child = child.getNextSibling();
		}
		if (operands.size() != 2) {
			throw new CpudlParseException(element, "two operands expected for equality");
		}

		String tagName = element.getTagName();
		if (tagName.equals("is-equal")) {
			return new IsEqual(operands.get(0), operands.get(1));
		} else if (tagName.equals("is-not-equal")) {
			return new IsNotEqual(operands.get(0), operands.get(1));
		} else if (tagName.equals("is-less")) {
			return new IsLess(operands.get(0), operands.get(1));
		} else if (tagName.equals("is-greater-equal")) {
			return new IsGreaterEqual(operands.get(0), operands.get(1));
		} else if (tagName.equals("is-less-equal")) {
			return new IsLessEqual(operands.get(0), operands.get(1));
		} else if (tagName.equals("is-greater")) {
			return new IsGreater(operands.get(0), operands.get(1));
		} else {
			throw new CpudlParseException(element, "invalid tag name <" + tagName + "> for comparison element");
		}
	}
}
