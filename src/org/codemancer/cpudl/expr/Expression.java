// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.CpudlParseException;
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

	/** Make expression from XML node.
	 * @param node the expression as XML
	 * @return a corresponding expression, or null if node does not contain an expression
	 */
	public static Expression make(Node node) throws CpudlParseException {
		if (!(node instanceof Element)) {
			return null;
		}
		Element el = (Element)node;
		String tagName = el.getTagName();
		if (tagName.equals("const")) {
			return Constant.make(el);
		} else {
			return null;
		}
	}
}
