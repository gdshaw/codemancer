// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.ext.Attributes2Impl;

/** A class for adding line and column numbers to a stream of SAX events. */
public class LocationFilter extends XMLFilterImpl {
	/** Construct a new location filter.
	 * @param reader the SAX event stream to be processed
	 */
	public LocationFilter(XMLReader reader) {
		super(reader);
	}

	/** The SAX locator, if available.
	 * If the SAX parser provides a locator then it is required to call
	 * setDocumentLocator prior to reporting any other document events,
	 * causing it to be recorded here, otherwise it remains as null.
	 */
	private Locator locator = null;

	public void setDocumentLocator(Locator locator) {
		super.setDocumentLocator(locator);
		this.locator = locator;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// Allow for the possibility that a locator may not have been supplied.
		if (locator != null) {
			// Construct a human-readable string from the system ID, line number and column number.
			String location = locator.getSystemId() + ':' + locator.getLineNumber() + ':' + locator.getColumnNumber();

			// Create a copy of the existing attributes, in a form that can be modified.
			Attributes2Impl newAttributes = new Attributes2Impl(attributes);
			attributes = newAttributes;

			// Attach this string to the current element as an attribute named 'location'.
			newAttributes.addAttribute("", "", "location", "CDATA", location);
		}
		super.startElement(uri, localName, qName, attributes);
	}
}
