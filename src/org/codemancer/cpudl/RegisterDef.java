// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import org.w3c.dom.Element;

import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.type.IntegerType;

/** A class to represent a CPUDL register definition. */
public class RegisterDef {
	/** The name of the register. */
	public final String name;

	/** The size of the register, in bits. */
	int size;

	/** The type of this register. */
	Type type;

	/** Construct register definition.
	 * @param name the name of the register
	 * @param size the size of the register, in bits
	 */
	private RegisterDef(String name, int size) {
		this.name = name;
		this.size = size;
		this.type = new IntegerType(size, 0, new Style());
	}

	/** Get name.
	 * @return the name of the register
	 */
	public String getName() {
		return name;
	}

	/** Get size.
	 * @return the size of the register, in bits
	 */
	public int getSize() {
		return size;
	}

	/** Get type.
	 * @return the type of this register
	 */
	public Type getType() {
		return type;
	}

	/** Make register definition from XML element.
	 * @param element the register reference as XML
	 * @return the register reference as an object
	 */
	public static RegisterDef make(Element element) throws CpudlParseException {
		String name = element.getAttribute("name");
		if (name.isEmpty()) {
			throw new CpudlParseException(element, "register name not specified");
		}
		String sizeString = element.getAttribute("size");
		if (sizeString.isEmpty()) {
			throw new CpudlParseException(element, "missing size attribute in register definition");
		}
		try {
			int size = Integer.decode(sizeString);
			return new RegisterDef(name, size);
		} catch (NumberFormatException ex) {
			throw new CpudlParseException(element, "invalid size attribute in register definition");
		}
	}
}
