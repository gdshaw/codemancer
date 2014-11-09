// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.aof;

import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

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

	/** Get a reference to the aofSymbols array, creating and populating it if necessary.
	 * @return a reference to the aofSymbols array
	 */
	private ArrayList<AofSymbol> getAofSymbols() throws IOException {
		if (aofSymbols == null) {
			buffer.position(getFileOffset());
			int numSymbols = aof.getHeaderChunk().getSymbolCount();
			aofSymbols = new ArrayList<AofSymbol>(numSymbols);
			for (int i = 0; i != numSymbols; ++i) {
				AofSymbol symbol = new AofSymbol(buffer, aof, this);
				aofSymbols.add(symbol);
			}
		}
		return aofSymbols;
	}

	/** Get the number of symbols.
	 * @return the number of symbols
	 */
	public int getAofSymbolCount() throws IOException {
		return getAofSymbols().size();
	}

	/** Get the symbol with a given SID.
	 * @param sid the required SID
	 * @return the symbol
	 */
	public AofSymbol getAofSymbol(int sid) throws IOException {
		return getAofSymbols().get(sid);
	}
}
