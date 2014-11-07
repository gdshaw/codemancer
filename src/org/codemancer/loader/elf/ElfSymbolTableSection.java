// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.elf;

import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** A class to represent a section within an ELF file containing a symbol table.
 * This can be either an ordinary symbol table (SHT_SYMTAB) or a dynamic symbol
 * table (SHT_DYNSYM).
 */
public class ElfSymbolTableSection extends ElfSection {
	/** A list of the symbols contained in this symbol table, with their original indexes. */
	private final List<ElfSymbol> elfSymbols = new ArrayList<ElfSymbol>();

	/** Construct new symbol table section.
	 * On entry the ByteBuffer must be positioned at the start of the
	 * relevant section header. On exit the position is unspecified.
	 * @param buffer a ByteBuffer giving access to the underlying ELF file
	 * @param elf the ELF file to which the section belongs
	 */
	public ElfSymbolTableSection(ByteBuffer buffer, ElfFile elf) throws IOException {
		super(buffer, elf);

		long offset = getOffset();
		long size = getSize();
		long entsize = getEntrySize();
		for (long i = entsize; i + entsize < size; i += entsize) {
			buffer.position((int)(offset + i));
			ElfSymbol elfSymbol = new ElfSymbol(buffer, elf, this);
			elfSymbols.add(elfSymbol);
		}
	}

	/** Get the number of symbols.
	 * @return the number of symbols
	 */
	public int getElfSymbolCount() {
		return elfSymbols.size();
	}

	/** Get symbol at given index.
	 * @param symndx the index of the required symbol
	 * @return the symbol, or null if not found
	 */
	public ElfSymbol getElfSymbol(int symndx) {
		return elfSymbols.get(symndx);
	}

	public void dump(PrintWriter out) throws IOException {
		super.dump(out);
		out.println();
		for (ElfSymbol elfSymbol: elfSymbols) {
			elfSymbol.dump(out);
		}
	}
}
