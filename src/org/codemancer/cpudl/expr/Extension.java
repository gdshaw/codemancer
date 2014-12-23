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
import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.type.IntegerType;
import org.codemancer.cpudl.type.FragmentType;

/** An expression for extending the width of another expression. */
public class Extension extends Expression {
	/** The argument to be extended. */
	private final Expression arg;

	/** Construct an extension expression.
	 * @param type the required type
	 * @param arg the argument to be extended
	 */
	public Extension(IntegerType type, Expression arg) {
		super(type);
		this.arg = arg;
	}

	/** Get the argument.
	 * @return the argument
	 */
	public final Expression getArg() {
		return arg;
	}

	public Expression resolveReferences(Fragment frag, Map<String, Expression> args) {
		return new Extension((IntegerType)getType(), arg.resolveReferences(frag, args));
	}

	public Expression resolveRegisters(Map<String, Expression> registers) {
		return new Extension((IntegerType)getType(), arg.resolveRegisters(registers));
	}

	public Expression evaluate(State state) {
		Expression evaluatedArg = arg.evaluate(state);
		return new Extension((IntegerType)getType(), evaluatedArg);
	}

	public Expression simplify() {
		Expression simpleArg = arg.simplify();
		if (simpleArg instanceof Constant) {
			return new Constant(getType(), ((Constant)simpleArg).getValue());
		}
		return this;
	}

	public String unparse(Style style) {
		StringBuffer result = new StringBuffer();
		result.append("extend(");
		result.append(arg.unparse(style));
		result.append(",");
		result.append(((IntegerType)getType()).getSize());
		result.append(")");
		return result.toString();
	}

	/** Make extension expression from XML element.
	 * @param ctx the context of this expression
	 * @param the expression as an XML element
	 * @return the expression as an object
	 */
	public static Expression make(Context ctx, Element element) throws CpudlParseException {
		int size = Context.parseIntegerAttribute("size", element);
		int encoding = Context.parseEncodingAttribute(element);
		boolean bigEndian = ctx.getArchitecture().isBigEndian();
		Style style = null;

		Expression arg = Sequence.make(ctx, element);
		if (arg.getType() instanceof IntegerType) {
			IntegerType argType = (IntegerType)arg.getType();
			encoding = argType.getEncoding();
			bigEndian = argType.isBigEndian();
			style = argType.getStyle();
		}

		return new Extension(new IntegerType(size, encoding, bigEndian, style), arg);
	}
}
