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
	/** The properties of this stylesheet, indexed by class then name. */
	private HashMap<String, HashMap<String, String>> properties =
		new HashMap<String, HashMap<String, String>>();

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
					parseProperty(null, childElement);
				} else if (tagName.equals("select")) {
					parseSelect(childElement);
				}
			}
			child = child.getNextSibling();
		}
	}

	/** Parse property element.
	 * @param element the property, as an XML element
	 */
	private void parseProperty(String className, Element element) {
		String propName = element.getAttribute("name");
		String value = element.getAttribute("value");
		HashMap<String, String> classProperties = properties.get(className);
		if (classProperties == null) {
			classProperties = new HashMap<String, String>();
			properties.put(className, classProperties);
		}
		classProperties.put(propName, value);
	}

	/** Parse select element.
	 * @param element the class selection, as an XML element
	 */
	private void parseSelect(Element element) {
		String className = element.getAttribute("class");
		Node child = element.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element childElement = (Element)child;
				String tagName = childElement.getTagName();
				if (tagName.equals("property")) {
					parseProperty(className, childElement);
				}
			}
			child = child.getNextSibling();
		}
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
		String result = defaultValue;
		HashMap<String, String> globalProperties = properties.get(null);
		if (globalProperties != null) {
			String value = globalProperties.get(propName);
			if (value != null) result = value;
		}
		HashMap<String, String> classProperties = properties.get(className);
		if (classProperties != null) {
			String value = classProperties.get(propName);
			if (value != null) result = value;
		}
		return result;
	}
}
