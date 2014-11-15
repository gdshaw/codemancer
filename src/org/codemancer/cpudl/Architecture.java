// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.type.Choice;

/** A class to represent an instruction set architecture. */
public class Architecture {
	/** A type which describes any instruction of this architecture. */
	private Type start = null;

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
}
