// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.SortedMap;
import java.util.TreeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.expr.Expression;

/** A class to represent a type of instruction fragment. */
public class FragmentType extends Type {
	/** The members of this fragment, indexed by name. */
	private final SortedMap<String, Type> members = new TreeMap<String, Type>();

	/** Construct fragment type from XML.
	 * @param el this fragment type as an XML element
	 */
	public FragmentType(Element element) throws CpudlParseException {
		Node child = element.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element childElement = (Element)child;
				String tagName = childElement.getTagName();
				if (tagName.equals("var")) {
					parseMember(childElement);
				}
			}
			child = child.getNextSibling();
		}
	}

	/** Parse member.
	 * @param element the member as an XML element
	 */
	private final void parseMember(Element element) throws CpudlParseException {
		String name = element.getAttribute("name");
		if (name.equals("")) {
			throw new CpudlParseException(element, "member name not specified");
		}
		Type type = Type.makeType(element);
		if (type == null) {
			throw new CpudlParseException(element, "member type not found");
		}
		members.put(name, type);
	}
}
