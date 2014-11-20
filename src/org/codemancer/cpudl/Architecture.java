// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import java.io.FileReader;
import java.util.HashMap;
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
import org.codemancer.cpudl.type.Choice;

/** A class to represent an instruction set architecture. */
public class Architecture {
	/** A type which describes any instruction of this architecture. */
	private Type start = null;

	/** The types defined by this architecture, indexed by name. */
	private final HashMap<String, Type> types = new HashMap<String, Type>();

	/** The stylesheet for this architecture. */
	private Stylesheet stylesheet = new Stylesheet();

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
