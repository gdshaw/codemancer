// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.elf;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.codemancer.loader.Symbol;
import org.codemancer.loader.Relocation;
import org.codemancer.loader.InvalidFileFormat;

/** A class to represent a relocation directive (with or without an explicit addend). */
public class ElfRelocation implements Relocation {
	/** The location to which the relocation is applicable,
	 * as an offset from the start of the relevant section. */
	private long r_offset;

	/** The type of this relocation. */
	private int r_type;

	/** The symbol to which this relocation refers. */
	private ElfSymbol symbol;

	/** The addend for this relocation.
	 * This is set to zero if the relocation does not have an explicit addend.
	 */
	private long r_addend;

	/** Construct new relocation directive.
	 * @param buffer a ByteBuffer giving access to the underlying ELF file
	 * @param sect the section to which the relocation directive belongs
	 * @param hasAddend true if the relocation has an explicit addend,
	 *  otherwise false
	 */
	public ElfRelocation(ByteBuffer buffer, ElfRelocationSection sect,
		boolean hasAddend) throws IOException {

		ElfFile elf = sect.getElfFile();
		ElfSymbolTableSection symtab =
			(ElfSymbolTableSection)sect.getLinkedSection();
		List<ElfSymbol> symbols = symtab.getElfSymbols();

		byte elfClass = elf.getElfFileClass();

		int symndx;
		if (elfClass == ElfFile.ELFCLASS64) {
			r_offset = buffer.getLong();
			long info = buffer.getLong();
			symndx = (int)(info >> 32);
			r_type = (int)info;
		} else {
			r_offset = buffer.getInt() & 0xFFFFFFFFL;
			int info = buffer.getInt();
			symndx = (info >> 8) & 0xFFFFFF;
			r_type = info & 0xFF;
		}

		try {
			symbol = symbols.get(symndx);
		} catch (IndexOutOfBoundsException ex) {
			String message = String.format(
				"Symbol index out of bounds in relocation (%d > %d)",
				symndx, symbols.size());
			throw new InvalidFileFormat(message);
		}

		if (hasAddend) {
			if (elfClass == ElfFile.ELFCLASS64) {
				r_addend = buffer.getLong();
			} else {
				r_addend = buffer.getInt() & 0xFFFFFFFFL;
			}
		} else {
			r_addend = 0;
		}
	}

	public long getAddress() {
		return r_offset;
	}

	public long getSize() {
		return 0;
	}

	public Symbol getSymbol() {
		return symbol;
	}

	public long getAddend() {
		return r_addend;
	}

	/** Get the type of this relocation.
	 * @return the type
	 */
	public int getElfRelocationType() {
		return r_type;
	}

	/** Get the ELF symbol to which this relocation refers.
	 * @return the symbol
	 */
	public ElfSymbol getElfSymbol() {
		return symbol;
	}

	public void dump(PrintWriter out) throws IOException {
		out.printf("%016x %02x %016x %s\n", r_offset, r_type,
			r_addend, symbol.getName());
	}
}
