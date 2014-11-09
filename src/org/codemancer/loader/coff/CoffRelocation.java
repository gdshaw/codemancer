// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.coff;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.codemancer.loader.Relocation;

/** A class to represent a COFF relocation directive.
 * This is an abstract class because the format depends on the
 * target architecture.
 */
public abstract class CoffRelocation implements Relocation {
	/** Get the COFF symbol to which this relocation refers.
	 * @return the symbol
	 */
	public abstract CoffSymbol getCoffSymbol();
}
