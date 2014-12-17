// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.List;
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

/** An expression class to represent a reference to a memory location. */
public class Memory extends Expression {
	/** The address of the memory location. */
	private final Expression address;

	/** Construct reference to memory location
	 * @param type the required type of this reference
	 * @param name the address of the memory location
	 */
	public Memory(Type type, Expression address) {
		super(type);
		this.address = address;
	}

	/** Get address.
	 * @return the address of the memory location
	 */
	public Expression getAddress() {
		return address;
	}

	public String unparse(Style style) {
		return "[" + address.unparse(style) + "]";
	}

	public Expression resolveReferences(Fragment frag, Map<String, Expression> args) {
		return new Memory(getType(), address.resolveReferences(frag, args));
	}

	public Expression resolveRegisters(Map<String, Expression> registers) {
		return new Memory(getType(), address.resolveRegisters(registers));
	}

	public void assign(State state, Expression value) {
		state.put(this, value);
	}

	public Expression evaluate(State state) {
		Expression value = state.get(this);
		if (value != null) return value;
		return this;
	}

	public void listAssignments(List<Assignment> uncond, List<Assignment> cond, boolean isCond) {
		address.listAssignments(uncond, cond, isCond);
	}

	/** Make memory reference from XML element.
	 * @param ctx the context of this expression
	 * @param element the reference as XML
	 * @return the reference as an object
	 */
	public static Memory make(Context ctx, Element element) throws CpudlParseException {
		Type type = null;
		String sizeStr = element.getAttribute("size");
		if (sizeStr.length() != 0) {
			try {
				int size = Integer.parseInt(sizeStr);
				int encoding = IntegerType.UNSIGNED;
				boolean bigEndian = ctx.getArchitecture().isBigEndian();
				Style style = null;
				type = new IntegerType(size, encoding, bigEndian, style);
			} catch (NumberFormatException ex) {
				throw new CpudlParseException(element,
					"invalid size attribute in <memory> element");
			}
		}

		Expression address = Sequence.make(ctx, element);
		return new Memory(type, address);
	}
}
