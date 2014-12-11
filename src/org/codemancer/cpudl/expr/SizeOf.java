// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.Map;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.State;
import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.CpudlReferenceException;
import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.type.IntegerType;

/** An expression class to represent the size of another expression. */
public class SizeOf extends Expression {
	/** The expression for which the size is required. */
	private final Expression expr;

	/** The divisor, in bits.
	 * This is the size in bits which corresponds to a result of one.
	 */
	private final int divisor;

	/** Construct size expression.
	 * @param type the required type of the result
	 * @param expr the expression for which the size is required
	 * @param divisor the divisor, in bits
	 */
	public SizeOf(Type type, Expression expr, int divisor) {
		super(type);
		this.expr = expr;
		this.divisor = divisor;
	}

	public String unparse(Style style) {
		return "sizeof(" + expr.unparse(style) + ")";
	}

	public Expression resolveReferences(Fragment frag, Map<String, Expression> args) {
		return new SizeOf(getType(), expr.resolveReferences(frag, args), divisor);
	}

	public Expression resolveRegisters(Map<String, Expression> registers) {
		return new SizeOf(getType(), expr.resolveRegisters(registers), divisor);
	}

	public Expression simplify() {
		Type type = expr.getType();
		if (type == null) return this;
		if (type.getChunkCount() != 1) return this;
		if (type.isVariableWidth(0)) return this;
		long width = type.getFixedWidth(0);
		if ((width % divisor) != 0) return this;
		return new Constant(getType(), width / divisor);
        }

	/** Make size expression from XML element.
	 * @param ctx the context of this expression
	 * @param element the reference as XML
	 * @return the reference as an object
	 */
	public static SizeOf make(Context ctx, Element element) throws CpudlParseException {
		int divisor = Integer.decode(element.getAttribute("divisor"));
		Expression expr = Sequence.make(ctx, element);
		return new SizeOf(null, expr, divisor);
	}
}
