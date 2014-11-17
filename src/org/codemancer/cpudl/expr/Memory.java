// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.Map;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.CpudlReferenceException;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent a reference to a memory location. */
public class Memory extends Expression {
	/** The address of the memory location. */
	public final Expression address;

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

	public String unparse() {
		return "[" + address.unparse() + "]";
	}

	public Expression resolve(Fragment frag, Map<String, Expression> args, boolean part)
		throws CpudlReferenceException {

		return new Memory(getType(), address.resolve(frag, args, part));
	}

	/** Make memory reference from XML element.
	 * @param element the reference as XML
	 * @return the reference as an object
	 */
	public static Memory make(Element element) throws CpudlParseException {
		Expression address = null;
		Node child = element.getFirstChild();
		while (child != null) {
			Expression operand = Expression.make(child);
			if (operand != null) {
				if (address == null) {
					address = operand;
				} else {
					throw new CpudlParseException(element, "single address argument expected to <memory> element");
				}
			}
			child = child.getNextSibling();
		}
		if (address == null) {
			throw new CpudlParseException(element, "address attribute missing in <memory> element");
		}
		return new Memory(null, address);
	}
}
