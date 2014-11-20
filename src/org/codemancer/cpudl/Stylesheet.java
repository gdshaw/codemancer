// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import java.util.HashMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/** A class to represent a CPUDL stylesheet. */
public class Stylesheet {
	/** The properties of this stylesheet. */
	private static HashMap<String, String> properties =
		new HashMap<String, String>();

	/** Construct empty stylesheet. */
	public Stylesheet() {}

	/** Merge XML element into stylesheet.
	 * @param element the XML element to be merged
	 */
	public void merge(Element element) {
		Node child = element.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element childElement = (Element)child;
				String tagName = childElement.getTagName();
				if (tagName.equals("property")) {
					parseProperty(childElement);
				}
			}
			child = child.getNextSibling();
		}
	}

	/** Parse property element.
	 * @param element the property, as an XML element
	 */
	private void parseProperty(Element element) {
		String name = element.getAttribute("name");
		String value = element.getAttribute("value");
		properties.put(name, value);
	}

	/** Get style for given class name.
	 * @param className the name of the required class
	 * @return the corresponding style
	 */
	public Style getStyle(String className) {
		return new Style(this, className);
	}

	/** Get property with given class name.
	 * @param className the name of the required class
	 * @param propName the name of the required property
	 * @param defaultValue the default value for the property
	 * @return the value of the property
	 */
	public String get(String className, String propName, String defaultValue) {
		String result = properties.get(propName);
		if (result == null) result = defaultValue;
		return result;
	}
}
