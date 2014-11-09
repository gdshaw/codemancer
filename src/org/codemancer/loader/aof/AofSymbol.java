// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.aof;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

/** A class to represent a symbol from an AOF symbol table. */
public class AofSymbol {
	/** The name of this symbol. */
	private final String name;

	/** The attributes for this symbol. */
	private final int attributes;

	/** The value of this symbol. */
	private final int value;

	/** The area to which this symbol applies. */
	private final int area;

	/** Construct new symbol.
	 * @param buffer a ByteBuffer giving access to the underlying AOF file
	 * @param aof the AOF file to which the symbol belongs
	 * @param chunk the chunk to which the symbol belongs
	 */
	public AofSymbol(ByteBuffer buffer, AofFile aof, AofChunk chunk) throws IOException {
		name = aof.getStringTableChunk().get(buffer.getInt());
		attributes = buffer.getInt();
		value = buffer.getInt();
		area = buffer.getInt();
	}

	/** Get the name of this symbol.
	 * @return the name
	 */
        public String getName() {
		return name;
	}

	/** Get the value of this symbol.
	 * @return the value
	 */
        public long getValue() {
		return value;
	}

	public void dump(PrintWriter out) throws IOException {
		out.printf("%08x %08x %08x %s\n", value, attributes, area, name);
	}
}
