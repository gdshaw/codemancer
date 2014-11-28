// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.Map;
import java.util.HashMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.CpudlReferenceException;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent a reference to a fragment member. */
public class Reference extends Expression {
	/** The name of this reference. */
	private final String name;

	/** The name split into components. */
	private final String[] components;

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
		this.components = name.split("[.]");
		this.args = args;
	}

	/** Get name.
	 * @return the name of the fragment member
	 */
	public String getName() {
		return name;
	}

	public String unparse(Style style) {
		StringBuilder sb = new StringBuilder();
		sb.append("%");
		sb.append(name);
		if ((args != null) && (!args.isEmpty())) {
			sb.append("(");
			for (Map.Entry<String, Expression> entry: this.args.entrySet()) {
				sb.append(entry.getKey());
				sb.append("=");
				sb.append(entry.getValue().unparse(style));
			}
			sb.append(")");
		}
		return sb.toString();
	}

	public Expression resolveReferences(Fragment frag, Map<String, Expression> args) {
		// Resolve the supplied argument list, so far as is possible, in the current context.
		Map<String, Expression> resolvedArgs = new HashMap<String, Expression>();
		if (this.args != null) {
			for (Map.Entry<String, Expression> entry: this.args.entrySet()) {
				resolvedArgs.put(entry.getKey(), entry.getValue().resolveReferences(frag, args));
			}
		}

		// Attempt to resolve the first component of the name as an argument.
		Expression result = null;
		int i = 0;
		if (args != null) {
			result = args.get(components[i]);
			if (result != null) {
				i += 1;
				if (result instanceof Fragment) {
					frag = (Fragment)result;
				} else {
					frag = null;
				}
			}
		}

		// Attempt to resolve the remaining components as fragment members.
		// (This will include the first component in the case where it did not resolve
		// as an argument.)
		while ((i != components.length) && (frag != null)) {
			result = frag.get(components[i]);
			if (result != null) {
				i += 1;
				if (result instanceof Fragment) {
					frag = (Fragment)result;
				} else {
					frag = null;
				}
			} else {
				frag = null;
			}
		}

		// If the name was resolved then resolve the associated value.
		if (result != null) {
			result = result.resolveReferences(frag, resolvedArgs);
		}

		// If resolution failed then either throw an exception or return a
		// partially resolved reference, as appropriate.
		if (result == null) {
			result = new Reference(getType(), name, resolvedArgs);
		}
		return result;
	}

	public Expression solve(Reference solveFor, Expression placeholder) {
		if (name.equals(solveFor.getName())) {
			return placeholder;
		}
		return null;
	}

	/** Make reference from XML element.
	 * @param ctx the context of this expression
	 * @param element the reference as XML
	 * @return the reference as an object
	 */
	public static Reference make(Context ctx, Element element) throws CpudlParseException {
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
						argValue = Sequence.make(ctx, childElement);
					}
					args.put(argName, argValue);
				}
			}
			child = child.getNextSibling();
		}

		return new Reference(null, name, args);
	}
}
