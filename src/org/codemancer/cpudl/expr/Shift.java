// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.expr.Addition;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent a shift to the left or right.
 * The left-hand side is the value to be shifted, the right-hand side is
 * the distance in bits through which to shift (to the left if positive,
 * to the right if negative).
 */
public class Shift extends BinaryExpression {
	/** A constant to represent a logical shift. */
	public static final int LOGICAL = 0;

	/** A constant to represent an arithmetic shift. */
	public static final int ARITHMETIC = 1;

	/** A constant to represent a rotation. */
	public static final int ROTATION = 2;

	/** A constant to represent a rotation through carry. */
	public static final int EXTENDED = 3;

	/** The width of the shift operation, in bits. */
	private int width;

	/** The shift method.
	 * This must be one of LOGICAL, ARITHMETIC, ROTATION or EXTENDED.
	 */
	private int method;

	/** Construct shift operation from two arguments.
	 * @param type the required type of the result
	 * @param width the width of the shift operation, in bits
	 * @param method the shift method
	 * @param lhs the value to be shifted
	 * @param rhs the distance through which to shift
	 */
	public Shift(Type type, int width, int method, Expression lhs, Expression rhs) {
		super(type, lhs, rhs);
		if (width < 0) {
			throw new IllegalArgumentException("width of shift operation must be non-negative");
		}

		this.width = width;
		this.method = method;
	}

	public Expression partialClone(Expression lhs, Expression rhs) {
		return new Shift(getType(), width, method, lhs, rhs);
	}

	public String getSymbol() {
		return "<<";
	}

	/** Make shift operation from XML element.
	 * If the element contains two or more subexpressions then the result is
	 * a shift expression.
	 * If there is only one subexpression then the result is that subexpression.
	 * If there are no subexpressions then the result is zero.
	 * @param ctx the context of this expression
	 * @param the expression as an XML element
	 * @return the expression as an object
	 */
	public static Expression make(Context ctx, Element element) throws CpudlParseException {
		int width = Integer.decode(element.getAttribute("width"));

		String methodString = element.getAttribute("method");
		int method;
		if (methodString.length() == 0) {
			method = LOGICAL;
		} else if (methodString.equals("logical")) {
			method = LOGICAL;
		} else if (methodString.equals("arithmetic")) {
			method = ARITHMETIC;
		} else if (methodString.equals("rotation")) {
			method = ROTATION;
		} else if (methodString.equals("extended")) {
			method = EXTENDED;
		} else {
			throw new CpudlParseException(element, "invalid shift method in <shift>");
		}

		Expression result = null;
		Node child = element.getFirstChild();
		while (child != null) {
			Expression operand = ctx.makeExpression(child);
			if (operand != null) {
				if (result == null) {
					result = operand;
				} else {
					result = new Shift(null, width, method, result, operand);
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
