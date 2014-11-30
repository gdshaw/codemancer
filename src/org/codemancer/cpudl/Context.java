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
import org.codemancer.cpudl.type.Whitespace;
import org.codemancer.cpudl.type.IntegerType;
import org.codemancer.cpudl.type.BitmapType;
import org.codemancer.cpudl.type.FragmentType;
import org.codemancer.cpudl.type.PrefixType;
import org.codemancer.cpudl.type.Choice;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Reference;
import org.codemancer.cpudl.expr.Constant;
import org.codemancer.cpudl.expr.Register;
import org.codemancer.cpudl.expr.Memory;
import org.codemancer.cpudl.expr.Extension;
import org.codemancer.cpudl.expr.Addition;
import org.codemancer.cpudl.expr.Subtraction;
import org.codemancer.cpudl.expr.Multiplication;
import org.codemancer.cpudl.expr.BitwiseAnd;
import org.codemancer.cpudl.expr.BitwiseOr;
import org.codemancer.cpudl.expr.BitwiseXor;
import org.codemancer.cpudl.expr.Shift;
import org.codemancer.cpudl.expr.Equality;
import org.codemancer.cpudl.expr.Assignment;
import org.codemancer.cpudl.expr.Sequence;

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
			String typeName = parseStringAttribute("name", element);
			Type type = arch.getType(typeName);
			if (type == null) {
				throw new CpudlParseException(element, "type name '" + typeName + "' not found");
			}
			return type;
		} else if (tagName.equals("const")) {
			return new ConstantType(this, element);
		} else if (tagName.equals("literal")) {
			return new LiteralType(this, element);
		} else if (tagName.equals("ws")) {
			return new Whitespace(this, element);
		} else if (tagName.equals("integer")) {
			return new IntegerType(this, element);
		} else if (tagName.equals("bitmap")) {
			return new BitmapType(this, element);
		} else if (tagName.equals("fragment")) {
			return new FragmentType(this, element);
		} else if (tagName.equals("prefix")) {
			return new PrefixType(this, element);
		} else if (tagName.equals("choice")) {
			return Choice.make(this, element);
		} else {
			return null;
		}
	}

	/** Make expression from XML node.
	 * @param node the expression as XML
	 * @return a corresponding expression, or null if the node is not an expression
	 */
	public Expression makeExpression(Node node) throws CpudlParseException {
		if (!(node instanceof Element)) {
			return null;
		}
		Element el = (Element)node;
		String tagName = el.getTagName();
		if (tagName.equals("ref")) {
			return Reference.make(this, el);
		} else if (tagName.equals("const")) {
			return Constant.make(this, el);
		} else if (tagName.equals("register")) {
			return Register.make(this, el);
		} else if (tagName.equals("memory")) {
			return Memory.make(this, el);
		} else if (tagName.equals("extend")) {
			return Extension.make(this, el);
		} else if (tagName.equals("add")) {
			return Addition.make(this, el);
		} else if (tagName.equals("sub")) {
			return Subtraction.make(this, el);
		} else if (tagName.equals("mul")) {
			return Multiplication.make(this, el);
		} else if (tagName.equals("and")) {
			return BitwiseAnd.make(this, el);
		} else if (tagName.equals("or")) {
			return BitwiseOr.make(this, el);
		} else if (tagName.equals("xor")) {
			return BitwiseXor.make(this, el);
		} else if (tagName.equals("shift")) {
			return Shift.make(this, el);
		} else if (tagName.equals("equals")) {
			return Equality.make(this, el);
		} else if (tagName.equals("assign")) {
			return Assignment.make(this, el);
		} else if (tagName.equals("sequence")) {
			return Sequence.make(this, el);
		} else {
			return null;
		}
	}

	public static String parseStringAttribute(String attrName, Element element)
		throws CpudlParseException {

		String attr = element.getAttribute(attrName);
		if (attr.length() == 0) {
			throw new CpudlParseException(element,
				"missing " + attrName + " attribute in <" + element.getTagName() + "> element");
		}
		return attr;
	}

	public static int parseIntegerAttribute(String attrName, Element element)
		throws CpudlParseException {

		String attrStr = element.getAttribute(attrName);
		if (attrStr.length() == 0) {
			throw new CpudlParseException(element,
				"missing " + attrName + " attribute in <" + element.getTagName() + "> element");
		}
		try {
			return Integer.parseInt(attrStr);
		} catch (NumberFormatException ex) {
			throw new CpudlParseException(element,
				"invalid " + attrName + " attribute in <" + element.getTagName() + "> element");
		}
	}
}
