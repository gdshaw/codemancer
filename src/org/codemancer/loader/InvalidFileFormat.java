// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader;

import java.io.IOException;

/** An exception class for indicating that an object file cannot be read
 * due to it having malformed content.
 */
@SuppressWarnings("serial")
public class InvalidFileFormat extends IOException {
	/** Construct invalid file format exception.
         * @param message a message describing the cause of the error
	 */
	public InvalidFileFormat(String message) {
		super(message);
	}
}
