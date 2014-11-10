// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.aof;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/** A class to represent a symbol table chunk within an AOF file. */
public class AofSymbolTableChunk extends AofChunk {
	/** An array containing the symbols defined by this chunk, indexed by SID.
	 * This array is populated on demand by accessing it via the getAofSymbols function.
	 */
	private ArrayList<AofSymbol> aofSymbols = null;

	/** Construct new symbol table chunk.
	 * @param buffer a ByteBuffer giving access to the underlying AOF file
	 * @param aof the AOF file to which the chunk belongs
	 */
	public AofSymbolTableChunk(ByteBuffer buffer, AofFile aof) throws IOException {
		super(buffer, aof);
	}

	/** Get a list of symbols in this AOF file.
	 * @return a list of symbols
	 */
	public List<AofSymbol> getAofSymbols() throws IOException {
		if (aofSymbols == null) {
			buffer.position(getFileOffset());
			int numSymbols = aof.getHeaderChunk().getNumSymbols();
			aofSymbols = new ArrayList<AofSymbol>(numSymbols);
			for (int i = 0; i != numSymbols; ++i) {
				AofSymbol symbol = new AofSymbol(buffer, aof, this);
				aofSymbols.add(symbol);
			}
		}
		return Collections.unmodifiableList(aofSymbols);
	}
}
