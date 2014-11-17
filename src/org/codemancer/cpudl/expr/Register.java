// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.HashMap;
import org.w3c.dom.Element;

import org.codemancer.cpudl.State;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent a reference to a register. */
public class Register extends Expression {
	/** The name of the register. */
	public final String name;

	/** Construct reference to register
	 * @param type the required type of this reference
	 * @param name the name of the register
	 */
	private Register(Type type, String name) {
		super(type);
		this.name = name;
	}

	/** Get name.
	 * @return the name of the register
	 */
	public String getName() {
		return name;
	}

	public String unparse() {
		return name;
	}

	public void assign(State state, Expression value) {
		state.put(this, value);
	}

	public Expression evaluate(State state) {
		Expression value = state.get(this);
		if (value != null) return value;
		return this;
	}

	/** An index of unique register objects, indexed by name. */
	private static HashMap<String, Register> registers = new HashMap<String, Register>();

	/** Make register reference from register name.
	 * References to a given register obtained using this method
	 * are guaranteed to refer to the same object.
	 * @param name the register name
	 * @return the register reference
	 */
	public static Register make(String name) {
		Register register = registers.get(name);
		if (register == null) {
			register = new Register(null, name);
			registers.put(name, register);
		}
		return register;
	}

	/** Make register reference from XML element.
	 * @param element the register reference as XML
	 * @return the register reference as an object
	 */
	public static Register make(Element element) throws CpudlParseException {
		String name = element.getAttribute("name");
		if (name == null) {
			throw new CpudlParseException(element, "register name not specified");
		}
		return make(name);
	}
}
