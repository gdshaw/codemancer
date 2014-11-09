// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader;

import java.io.PrintWriter;
import java.io.IOException;

/** An interface for inspecting a symbol from an object file. */
public interface Symbol {
	/** Get the name of this symbol.
	 * @return the name
	 */
	public String getName();

	/** Get the value associated with this symbol.
	 * @return the value, or 0 if unknown
	 */
	public long getValue();

	/** Get the size associated with this symbol.
	 * @return the size in bytes, or 0 if unknown or not applicable
	 */
	public long getSize();

	/** Dump the metadata for this symbol to a stream in human-readable form.
	 * @param out the stream to be written to
	 */
	public void dump(PrintWriter out) throws IOException;
}
