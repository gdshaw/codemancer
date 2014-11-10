// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.elf;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.codemancer.loader.InvalidFileFormat;

/** A class to represent a section within an ELF file containing a symbol table.
 * This can be either an ordinary symbol table (SHT_SYMTAB) or a dynamic symbol
 * table (SHT_DYNSYM).
 */
public class ElfSymbolTableSection extends ElfSection {
	/** A list of the symbols contained in this symbol table, with their original indexes. */
	private final List<ElfSymbol> elfSymbols = new ArrayList<ElfSymbol>();

	/** Construct new symbol table section.
	 * On entry the ByteBuffer must be positioned at the start of the
	 * relevant section header. A defensive copy is made immediately,
	 * and from that point onward the class instance neither modifies
	 * nor depends on the original ByteBuffer.
	 * @param parentBuffer a ByteBuffer giving access to the underlying
	 *  ELF file
	 * @param elf the ELF file to which the section belongs
	 */
	public ElfSymbolTableSection(ByteBuffer parentBuffer, ElfFile elf)
		throws IOException {

		super(parentBuffer, elf);
		if (!(getLinkedSection() instanceof ElfStringTableSection)) {
			throw new InvalidFileFormat(
				"symbol table linked section should be string table");
		}
		ElfStringTableSection strtab = (ElfStringTableSection)getLinkedSection();

		long offset = getOffset();
		long size = getSize();
		long entsize = getEntrySize();
		for (long i = 0; i + entsize <= size; i += entsize) {
			buffer.position((int)(offset + i));
			ElfSymbol elfSymbol = new ElfSymbol(buffer, elf, this, strtab);
			elfSymbols.add(elfSymbol);
		}
	}

	/** Get a list of symbols in this section.
	 * @return a list of symbols
	 */
	public List<ElfSymbol> getElfSymbols() {
		return Collections.unmodifiableList(elfSymbols);
	}

	public void dump(PrintWriter out) throws IOException {
		super.dump(out);
		out.println();
		for (ElfSymbol elfSymbol: elfSymbols) {
			elfSymbol.dump(out);
		}
	}
}
