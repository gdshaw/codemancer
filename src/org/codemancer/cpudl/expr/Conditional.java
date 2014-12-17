// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.Map;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent a conditional expression. */
public class Conditional extends Expression {
	/** The condition to be tested. */
	private final Expression condition;

	/** The result when the condition is satisfied. */
	private final Expression whenTrue;

	/** The result when the condition is not satisfied. */
	private final Expression whenFalse;

	/** Construct assignment operation from three operands.
	 * @param condition the condition to be tested
	 * @param whenTrue the result when the condition is satisfied
	 * @param whenFalse the result when the condition is not satisfied
	 */
	public Conditional(Expression condition, Expression whenTrue, Expression whenFalse) {
		super(whenTrue.getType());
		this.condition = condition;
		this.whenTrue = whenTrue;
		this.whenFalse = whenFalse;
	}

	public String unparse(Style style) {
		StringBuffer result = new StringBuffer();
		result.append(condition.unparse(style));
		result.append("?");
		result.append(whenTrue.unparse(style));
		result.append(":");
		result.append(whenFalse.unparse(style));
		return result.toString();
	}

	public Expression resolveReferences(Fragment frag, Map<String, Expression> args) {
		Expression resolvedCondition = condition.resolveReferences(frag, args);
		Expression resolvedWhenTrue = whenTrue.resolveReferences(frag, args);
		Expression resolvedWhenFalse = whenFalse.resolveReferences(frag, args);
		return new Conditional(resolvedCondition, resolvedWhenTrue, resolvedWhenFalse);
	}

	public Expression resolveRegisters(Map<String, Expression> registers) {
		Expression resolvedCondition = condition.resolveRegisters(registers);
		Expression resolvedWhenTrue = whenTrue.resolveRegisters(registers);
		Expression resolvedWhenFalse = whenFalse.resolveRegisters(registers);
		return new Conditional(resolvedCondition, resolvedWhenTrue, resolvedWhenFalse);
	}

	/** Make conditional expression from XML element.
	 * @param ctx the context of this expression
	 * @param the expression as an XML element
	 * @return the expression as an object
	 */
	public static Expression make(Context ctx, Element element) throws CpudlParseException {
		Expression condition = null;
		Expression whenTrue = null;
		Expression whenFalse = null;

		Node child = element.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element childElement = (Element)child;
				String tagName = childElement.getTagName();
				if (tagName.equals("test")) {
					if (condition != null) {
						throw new CpudlParseException(childElement, "multiple <test> elements in <if>");
					}
					condition = Sequence.make(ctx, childElement);
				} else if (tagName.equals("then")) {
					if (whenTrue != null) {
						throw new CpudlParseException(childElement, "multiple <then> elements in <if>");
					}
					whenTrue = Sequence.make(ctx, childElement);
				} else if (tagName.equals("else")) {
					if (whenFalse != null) {
						throw new CpudlParseException(childElement, "multiple <else> elements in <if>");
					}
					whenFalse = Sequence.make(ctx, childElement);
				} else {
					throw new CpudlParseException(childElement, "unexpected <" + tagName + "> element in <if>");
				}
			}
			child = child.getNextSibling();
		}
		if (condition == null) {
			throw new CpudlParseException(element, "<test> element required in <if>");
		}
		if (whenTrue == null) {
			whenTrue = new Constant(null, 0);
		}
		if (whenFalse == null) {
			whenFalse = new Constant(null, 0);
		}
		return new Conditional(condition, whenTrue, whenFalse);
	}
}
