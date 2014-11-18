// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.Map;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.State;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.CpudlReferenceException;
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

	/** Recursively resolve references within this expression.
	 * @param frag the instruction fragment that is currently in scope, or null if none
	 * @param args the arguments that are currently in scope, or null if none
	 * @param part true if it is acceptable for references to be left unresolved
	 *  where necessary, otherwise false
	 * @return the resolved expression
	 */
	public Expression resolve(Fragment frag, Map<String, Expression> args, boolean part)
		throws CpudlReferenceException {

		return this;
	}

	/** Recursively evaluate this expression.
	 * @param state the machine state on which to act
	 * @return the evaluated expression
	 */
	public Expression evaluate(State state) {
		return this;
	}

	/** Assign a value to this expression.
	 * This will fail unless the expression is an l-value.
	 * @param state the machine state on which to act
	 * @param value the value to be assigned to this expression
	 */
	public void assign(State state, Expression value) {
		throw new IllegalArgumentException("not an l-value");
	}

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
		if (tagName.equals("ref")) {
			return Reference.make(el);
		} else if (tagName.equals("const")) {
			return Constant.make(el);
		} else if (tagName.equals("register")) {
			return Register.make(el);
		} else if (tagName.equals("memory")) {
			return Memory.make(el);
		} else if (tagName.equals("add")) {
			return Addition.make(el);
		} else if (tagName.equals("sub")) {
			return Subtraction.make(el);
		} else if (tagName.equals("mul")) {
			return Multiplication.make(el);
		} else if (tagName.equals("and")) {
			return BitwiseAnd.make(el);
		} else if (tagName.equals("or")) {
			return BitwiseOr.make(el);
		} else if (tagName.equals("xor")) {
			return BitwiseXor.make(el);
		} else if (tagName.equals("assign")) {
			return Assignment.make(el);
		} else if (tagName.equals("sequence")) {
			return Sequence.make(el);
		} else {
			return null;
		}
	}
}
