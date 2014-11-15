// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import java.util.HashMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.type.Choice;

/** A class to represent an instruction set architecture. */
public class Architecture {
	/** A type which describes any instruction of this architecture. */
	private Type start = null;

	/** The types defined by this architecture, indexed by name. */
	private final HashMap<String, Type> types = new HashMap<String, Type>();

	/** Construct architecture from XML.
	 * @param element the required content as an XML element
	 */
	public Architecture(Element element) throws CpudlParseException {
		Context ctx = new Context(this);
		Node child = element.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element childElement = (Element)child;
				String tagName = childElement.getTagName();
				if (tagName.equals("start")) {
					if (start != null) {
						throw new CpudlParseException(childElement, "multiple <start> elements");
					}
					start = ctx.makeChoice(childElement);
				} else if (tagName.equals("define")) {
					String typeName = childElement.getAttribute("name");
					if (typeName == null) {
						throw new CpudlParseException(childElement, "missing name attribute in <define> element");
					}
					Type type = ctx.makeChoice(childElement);
					types.put(typeName, type);
				}
			}
			child = child.getNextSibling();
		}
		if (start == null) {
			throw new CpudlParseException(element, "missing <start> element");
		}
	}

	/** Get the start type for this architecture. */
	public final Type getStart() {
		return start;
	}

	/** Get named type.
	 * @param typeName the required type name
	 * @return the corresponding type, or null if not found
	 */
	public final Type getType(String typeName) {
		return types.get(typeName);
	}
}
