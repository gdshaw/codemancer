// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import org.w3c.dom.Element;

import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.CpudlReferenceException;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent a reference to a fragment member. */
public class Reference extends Expression {
	/** The name of this reference. */
	private final String name;

	/** Construct reference.
	 * @param type the required type of this reference
	 * @param name the name of the fragment member
	 */
	public Reference(Type type, String name) {
		super(type);
		this.name = name;
	}

	/** Get name.
	 * @return the name of the fragment member
	 */
	public String getName() {
		return name;
	}

	public String unparse() {
		StringBuilder sb = new StringBuilder();
		sb.append("%");
		sb.append(name);
		return sb.toString();
	}

	public Expression resolve(Fragment frag, boolean part) throws CpudlReferenceException {
		// Attempt to resolve the name as a fragment member.
		Expression result = frag.get(name);

		// If the name was resolved then resolve the associated value.
		if (result != null) {
			result = result.resolve(frag, part);
		}

		// If resolution failed then either throw an exception or return
		// this unresolved reference, as appropriate.
		if (result == null) {
			if (part) {
				result = this;
			} else {
				throw new CpudlReferenceException(name);
			}
		}

		return result;
	}

	/** Make reference from XML element.
	 * @param element the reference as XML
	 * @return the reference as an object
	 */
	public static Reference make(Element element) throws CpudlParseException {
		String name = element.getAttribute("name");
		if (name == null) {
			throw new CpudlParseException(element, "missing name attribute in <ref> element");
		}
		return new Reference(null, name);
	}
}
