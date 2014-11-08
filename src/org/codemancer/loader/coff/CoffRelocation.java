// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.coff;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

/** A class to represent a COFF relocation directive.
 * This is an abstract class because the format depends on the
 * target architecture.
 */
public abstract class CoffRelocation {
	/** Get the address to which this relocation is applicable.
	 * @return the offset
	 */
	public abstract long getOffset();

	/** Get the COFF symbol to which this relocation refers.
	 * @return the symbol
	 */
	public abstract CoffSymbol getCoffSymbol();

	/** Dump this directive to a stream in human-readable form.
	 * @param out the stream to be written to
	 */
	public abstract void dump(PrintWriter out) throws IOException;
}
