// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import org.w3c.dom.Element;

/** An exception class to indicate an error while parsing a CPUDL file. */
@SuppressWarnings("serial")
public class CpudlParseException extends Exception {
	/** Construct message containing location information if available.
	 * @param element the XML element within which the error occurred
	 * @param message a message describing the error, without location information
	 * @return a message describing the error, with location information if available
	 */
	private static String buildMessage(Element element, String message) {
		String location = element.getAttribute("location");
		if (location != null) {
			message = message + " at " + location;
		}
		return message;
	}

	/** Construct a CPUDL parse exception.
	 * @param element the XML element within which the error occurred
	 * @param message a message describing the error, without location information
	 */
	public CpudlParseException(Element element, String message) {
		super(buildMessage(element, message));
	}
}
