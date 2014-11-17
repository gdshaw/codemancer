// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.Map;
import java.util.HashMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.CpudlReferenceException;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent a reference to a fragment member. */
public class Reference extends Expression {
	/** The name of this reference. */
	private final String name;

	/** The arguments to be used when resolving this reference, or null if none. */
	private final Map<String, Expression> args;

	/** Construct reference.
	 * @param type the required type of this reference
	 * @param name the name of the fragment member
	 * @param args the arguments to be used when resolving this reference, or null if none
	 */
	public Reference(Type type, String name, Map<String, Expression> args) {
		super(type);
		this.name = name;
		this.args = args;
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

	public Expression resolve(Fragment frag, Map<String, Expression> args, boolean part)
		throws CpudlReferenceException {

		// Resolve the supplied argument list, so far as is possible, in the current context.
		Map<String, Expression> resolvedArgs = new HashMap<String, Expression>();
		if (this.args != null) {
			for (Map.Entry<String, Expression> entry: this.args.entrySet()) {
				resolvedArgs.put(entry.getKey(), entry.getValue().resolve(frag, args, true));
			}
		}

                // Attempt to resolve the name as an argument.
		Expression result = null;
		if (args != null) {
			result = args.get(name);
		}

		// If unresolved, attempt to resolve the name as a fragment member.
		if (result == null) {
			result = frag.get(name);
		}

		// If the name was resolved then resolve the associated value.
		if (result != null) {
			result = result.resolve(frag, resolvedArgs, part);
		}

		// If resolution failed then either throw an exception or return
		// this unresolved reference, as appropriate.
		if (result == null) {
			if (part) {
				result = new Reference(getType(), name, resolvedArgs);
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

		Map<String, Expression> args = new HashMap<String, Expression>();
		Node child = element.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element childElement = (Element)child;
				String tagName = childElement.getTagName();
				if (tagName.equals("bind")) {
					String argName = childElement.getAttribute("name");
					if (argName.length() == 0) {
						throw new CpudlParseException(element, "argument name not specified");
					}

					Expression argValue = null;
					String argSrc = childElement.getAttribute("src");
					if (argSrc.length() != 0) {
						argValue = new Reference(null, argSrc, null);
					}
					if (argValue == null) {
						argValue = Sequence.make(childElement);
					}
					args.put(argName, argValue);
				}
			}
			child = child.getNextSibling();
		}

		return new Reference(null, name, args);
	}
}
