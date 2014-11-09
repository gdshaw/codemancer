// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader;

import java.io.PrintWriter;
import java.io.IOException;

/** An interface for inspecting a relocation directive from an object file. */
public interface Relocation {
	/** Get the address to which this directive is applicable.
	 * @return the address
	 */
	public long getAddress();

	/** Get the size of the object that is relocated by this directive.
	 * @return the size in bytes, or 0 if unknown or not applicable
	 */
	public long getSize();

	/** Get the symbol to be used when performing the relocation.
	 * @return the symbol, or null if there is no symbol
	 */
	public Symbol getSymbol();

	/** Get the addend to be used when performing the relocation.
	 * @return the addend, or 0 if there is no addend
	 */
	public long getAddend();

	/** Dump a description of this relocation directive to a stream in human-readable form.
	 * @param out the stream to be written to
	 */
	public void dump(PrintWriter out) throws IOException;
}
