// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import java.io.FileReader;
import java.util.HashMap;
import java.util.ArrayList;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.dom.DOMResult;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.type.IntegerType;
import org.codemancer.cpudl.type.Choice;
import org.codemancer.cpudl.expr.Register;

/** A class to represent an instruction set architecture. */
public class Architecture {
	/** True if the default byte order is big-endian, false if little-endian. */
	private final boolean bigEndian;

	/** A type which describes any instruction of this architecture. */
	private Type start = null;

	/** The fragment types defined by this architecture, indexed by name. */
	private final HashMap<String, Type> types = new HashMap<String, Type>();

	/** The registers defined by this architecture, indexed by name. */
	private final HashMap<String, Register> registers = new HashMap<String, Register>();

	/** The features defined by this architecture, indexed by name. */
	private final HashMap<String, Integer> featuresByName = new HashMap<String, Integer>();

	/** The features defined by this architecture, index by ID. */
	private final ArrayList<String> featuresById = new ArrayList<String>();

	/** The stylesheet for this architecture. */
	private Stylesheet stylesheet = new Stylesheet();

	/** Construct architecture from XML.
	 * @param element the required content as an XML element
	 */
	public Architecture(Element element) throws CpudlParseException {
		String endian = Context.parseStringAttribute("endian", element);
		if (endian.equals("big")) {
			bigEndian = true;
		} else if (endian.equals("little")) {
			bigEndian = false;
		} else {
			throw new CpudlParseException(element, "invalid endian attribute in <cpudl> element");
		}

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
					start = Choice.make(ctx, childElement);
				} else if (tagName.equals("define")) {
					String typeName = Context.parseStringAttribute("name", childElement);
					Type type = Choice.make(ctx, childElement);
					types.put(typeName, type);
				} else if (tagName.equals("register")) {
					parseRegisterDefinition(childElement);
				} else if (tagName.equals("style")) {
					stylesheet.merge(childElement);
				}
			}
			child = child.getNextSibling();
		}
		if (start == null) {
			throw new CpudlParseException(element, "missing <start> element");
		}
	}

	private void parseRegisterDefinition(Element element) throws CpudlParseException {
		String name = Context.parseStringAttribute("name", element);
		if (registers.get(name) != null) {
			throw new CpudlParseException(element, "multiple definitions for register '" + name + "'");
		}
		int size = Context.parseIntegerAttribute("size", element);

		String className = element.getAttribute("class");
		Type type = new IntegerType(size, IntegerType.UNSIGNED, isBigEndian(), stylesheet.getStyle(className));
		Register register = new Register(type, name);
		registers.put(name, register);
	}

	/** Test whether the default bit order is big-endian.
	 * @return true if big-endian, false if little-endian
	 */
	public final boolean isBigEndian() {
		return bigEndian;
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

	/** Get register.
	 * @param registerName the required register name
	 * @return the corresponding register, or null if not found
	 */
	public final Register getRegister(String registerName) {
		return registers.get(registerName);
	}

	/** Get feature ID given name.
	 * @param featureName the feature name
	 * @return the corresponding feature ID
	 */
	public final int getFeatureId(String featureName) {
		Integer id = featuresByName.get(featureName);
		if (id == null) {
			featuresByName.put(featureName, featuresById.size());
			featuresById.add(featureName);
			id = featuresByName.get(featureName);
		}
		return id;
	}

	/** Get feature name given ID.
	 * @param featureId the feature ID
	 * @return the corresponding feature name
	 */
	public final String getFeatureName(int featureId) {
		return featuresById.get(featureId);
	}

	/** Get stylesheet.
	 * @return the stylesheet for this architecture
	 */
	public final Stylesheet getStylesheet() {
		return stylesheet;
	}

	/** Make architecture from CPUDL file.
	 * @param architectureName the name of the required architecture
	 * @return the architecture
	 */
	public static Architecture makeArchitecture(String architectureName) throws Exception {

		// Create InputSource to read from CPUDL file.
		String architectureSystemId = "cpus/" + architectureName + ".cpu";
		InputSource in = new InputSource(new FileReader(architectureSystemId));
		in.setSystemId(architectureSystemId);

		// Create SAXSource with attached LocationFilter, to annotate
		// elements with source filename/row/column.
		XMLReader reader = XMLReaderFactory.createXMLReader();
		LocationFilter filter = new LocationFilter(reader);
		SAXSource sax = new SAXSource(filter, in);

		// Transform resulting SAXSource into a DOM tree.
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		DOMResult result = new DOMResult();
		transformer.transform(sax, result);

		// Build architecture from DOM tree.
		Document document = (Document)result.getNode();
		Element root = document.getDocumentElement();
		return new Architecture(root);
	}
}
