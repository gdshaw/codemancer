// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

/** An exception class to indicate that a CPUDL reference could not be resolved. */
@SuppressWarnings("serial")
public class CpudlReferenceException extends RuntimeException {
	/** Construct a CPUDL reference exception.
	 * @param name the name to which the reference refers
	 */
	public CpudlReferenceException(String name) {
		super("failed to resolve reference to '" + name + "'");
	}
}
