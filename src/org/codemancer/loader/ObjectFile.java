// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader;

import java.util.List;
import java.util.NavigableMap;
import java.io.PrintWriter;
import java.io.IOException;

/** An interface for inspecting an object file. */
public interface ObjectFile {
	/** Get a list of the symbols defined by this file. */
	public List<Symbol> getSymbols() throws IOException;

	/** Get the address map for this object file. */
	public NavigableMap<Long, Segment> getAddressMap() throws IOException;

	/** Dump the metadata for this file to a stream in human-readable form.
	 * @param out the stream to be written to
	 */
	public void dump(PrintWriter out) throws IOException;
}
