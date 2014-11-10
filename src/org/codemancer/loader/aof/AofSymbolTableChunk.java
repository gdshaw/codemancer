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

import org.codemancer.loader.Symbol;

/** A class to represent a symbol table chunk within an AOF file. */
public class AofSymbolTableChunk extends AofChunk {
	/** An array containing the symbols defined by this chunk,
	 * as AofSymbols, indexed by SID.
	 */
	private final ArrayList<AofSymbol> aofSymbols;

	/** An array containing the symbols defined by this chunk,
	 * as generic Symbols. */
	private final ArrayList<Symbol> symbols;

	/** Construct new symbol table chunk.
	 * @param buffer a ByteBuffer giving access to the underlying AOF file
	 * @param aof the AOF file to which the chunk belongs
	 */
	public AofSymbolTableChunk(ByteBuffer buffer, AofFile aof) throws IOException {
		super(buffer, aof);

		buffer.position(getFileOffset());
		int numSymbols = aof.getHeaderChunk().getNumSymbols();
		aofSymbols = new ArrayList<AofSymbol>(numSymbols);
		symbols = new ArrayList<Symbol>(numSymbols);
		for (int i = 0; i != numSymbols; ++i) {
			AofSymbol symbol = new AofSymbol(buffer, aof, this);
			aofSymbols.add(symbol);
			symbols.add(symbol);
		}
	}

	/** Get a list of generic symbols in this file.
	 * @return a list of symbols
	 */
	public List<Symbol> getSymbols() {
		return Collections.unmodifiableList(symbols);
	}

	/** Get a list of AOF symbols in this file.
	 * @return a list of symbols
	 */
	public List<AofSymbol> getAofSymbols() {
		return Collections.unmodifiableList(aofSymbols);
	}
}
