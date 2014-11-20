// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

/** A class to represent the style to be used when formatting an expression. */
public class Style {
	/** The stylesheet which provides this style. */
	private final Stylesheet stylesheet;

	/** The class name for selecting this style. */
	private final String className;

	/** Construct empty style. */
	public Style() {
		this.stylesheet = null;
		this.className = null;
	}

	/** Construct style given stylesheet and class name
	 * @param stylesheet the stylesheet
	 * @param className the class name
	 */
	public Style(Stylesheet stylesheet, String className) {
		this.stylesheet = stylesheet;
		this.className = className;
	}

	/** Get the property with a given name.
	 * @param name the name of the required property
	 * @param defaultValue the default value for the property
	 * @return the value of the property, as a string
	 */
	public String get(String propName, String defaultValue) {
		String value = null;
		if (stylesheet != null) {
			value = stylesheet.get(className, propName, defaultValue);
		}
		return value;
	}

	/** Get the integer property with a given name.
	 * @param name the name of the required property
	 * @param defaultValue the default value for the property
	 * @return the value of the property, as an integer
	 */
	public int getInteger(String propName, int defaultValue) {
		int value = defaultValue;
		String string = null;
		if (stylesheet != null) {
			string = stylesheet.get(className, propName, null);
		}
		if (string != null) {
			value = Integer.parseInt(string);
		}
		return value;
	}
}
