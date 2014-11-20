// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.type.ConstantType;
import org.codemancer.cpudl.type.LiteralType;
import org.codemancer.cpudl.type.IntegerType;
import org.codemancer.cpudl.type.FragmentType;
import org.codemancer.cpudl.type.Choice;

/** The static context within which a CPUDL element should be interpreted. */
public class Context {
	/** The architecture. */
	private final Architecture arch;

	/** Construct CPUDL context.
	 * @param arch the architecture
	 */
	public Context(Architecture arch) {
		this.arch = arch;
	}

	/** Get the architecture.
	 * @return the architecture
	 */
	public final Architecture getArchitecture() {
		return arch;
	}

	/** Get the stylesheet.
	 * @return the stylesheet
	 */
	public final Stylesheet getStylesheet() {
		return arch.getStylesheet();
	}

	/** Make type from XML node.
	 * @param node the node to be interpreted as a type
	 * @return the corresponding type, or null if node does not contain a type
	 */
	public Type makeType(Node node) throws CpudlParseException {
		if (!(node instanceof Element)) {
			return null;
		}
		Element element = (Element)node;
		String tagName = element.getTagName();
		if (tagName.equals("ref")) {
			String typeName = element.getAttribute("name");
			if (typeName == null) {
				throw new CpudlParseException(element, "missing name attribute in <ref> element");
			}
			Type type = arch.getType(typeName);
			if (type == null) {
				throw new CpudlParseException(element, "type name '" + typeName + "' not found");
			}
			return type;
		} else if (tagName.equals("const")) {
			return new ConstantType(this, element);
		} else if (tagName.equals("literal")) {
			return new LiteralType(this, element);
		} else if (tagName.equals("integer")) {
			return new IntegerType(this, element);
		} else if (tagName.equals("fragment")) {
			return new FragmentType(this, element);
		} else if (tagName.equals("choice")) {
			return makeChoice(element);
		} else {
			return null;
		}
	}

	/** Make choice of types from children of XML element.
	 * If only one type is specified then it may be returned directly
	 * (as opposed to being wrapped within a Choice object).
	 * @param node the parent of the elements to be interpreted as types
	 * @return the type or choice of types
	 */
	public Type makeChoice(Element element) throws CpudlParseException {
		ArrayList<Type> types = new ArrayList<Type>();
		Node child = element.getFirstChild();
		while (child != null) {
			Type type = makeType(child);
			if (type != null) {
				types.add(type);
			}
			child = child.getNextSibling();
		}
		if (types.size() == 0) {
			throw new CpudlParseException(element, "type expected");
		} else if (types.size() == 1) {
			return types.get(0);
		} else {
			return new Choice(types);
		}
	}
}
