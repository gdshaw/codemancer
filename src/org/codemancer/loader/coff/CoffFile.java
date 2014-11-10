// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.coff;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.codemancer.loader.ObjectFile;
import org.codemancer.loader.Symbol;
import org.codemancer.loader.InvalidFileFormat;

/** A class to represent the content of a COFF file. */
public class CoffFile implements ObjectFile {
	/** A flag to indicate that relocation information has been stripped from the file. */
	public static final short F_RELFLG = 0x0001;

	/** A flag to indicate that the file is executable. */
	public static final short F_EXEC = 0x0002;

	/** A flag to indicate that line numbers have been stripped from the file. */
	public static final short F_LNNO = 0x0004;

	/** A flag to indicate that local symbols have been stripped from the file. */
	public static final short F_LSYMS = 0x0008;

	/** A magic number to indicate that this is a Z80 COFF file. */
	public static final short Z80MAGIC = (short)0x805a;

	/** A ByteBuffer giving access to the underlying COFF file. */
	private final ByteBuffer buffer;

	/** The magic number.
	 * This depends on the target architecture.
	 */
	private short f_magic;

	/** The number of section headers. */
	private short f_nscns;

	/** The timestamp. */
	private int f_timdat;

	/** The offset to the start of the symbol table. */
	private int f_symptr;

	/** The number of symbols in the symbol table. */
	private int f_nsyms;

	/** The size of the optional header, in bytes. */
	private short f_opthdr;

	/** The flags word. */
	private short f_flags;

	/** A table of sections in this COFF file. */
	private final ArrayList<CoffSection> coffSections;

	/** A table of COFF symbols defined within this COFF file.
	 * This table includes null values for auxiliary symbol table
	 * entries, in order to facilitate indexing.
	 */
	private ArrayList<CoffSymbol> coffSymbols;

	/** A list of generic symbols defined within this COFF file.
	 * This list does not include null entries.
	 */
	private ArrayList<Symbol> symbols;

	/** The string table. */
	private final byte[] strings;

	/** Construct object to represent COFF file.
	 * On entry the ByteBuffer must be positioned at the start of the file.
	 * Any byte order is permissible. On exit the position is unspecified,
	 * but the byte order will have been set to match the ELF file.
	 * @param buffer the content of the COFF file as a ByteBuffer
	 */
	public CoffFile(ByteBuffer buffer) throws IOException {
		this.buffer = buffer;
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		// Read COFF file header.
		f_magic = buffer.getShort();
		f_nscns = buffer.getShort();
		f_timdat = buffer.getInt();
		f_symptr = buffer.getInt();
		f_nsyms = buffer.getInt();
		f_opthdr = buffer.getShort();
		f_flags = buffer.getShort();
		int sectptr = buffer.position();

		// Parse string table.
		buffer.position(f_symptr + f_nsyms * CoffSymbol.SYMESZ);
		int strsz = buffer.getInt();
		if (strsz < 4) {
			throw new InvalidFileFormat("invalid string table size");
		}
		strings = new byte[strsz];
		buffer.get(strings, 4, strsz - 4);

		// Parse symbol table.
		coffSymbols = new ArrayList<CoffSymbol>(f_nsyms);
		symbols = new ArrayList<Symbol>(f_nsyms);
		buffer.position(f_symptr);
		for (int i = 0; i < f_nsyms; ++i) {
			CoffSymbol symbol = new CoffSymbol(buffer, this);
			coffSymbols.add(symbol);
			symbols.add(symbol);
			i += symbol.getAuxiliaryEntryCount();
			for (int j = 0; j != symbol.getAuxiliaryEntryCount(); ++j) {
				coffSymbols.add(null);
			}
		}

		// Parse section headers.
		coffSections = new ArrayList<CoffSection>(f_nscns);
		for (int i = 0; i != f_nscns; ++i) {
			buffer.position(sectptr + i * CoffSection.SCNHSZ);
			CoffSection section = new CoffSection(buffer, this);
			coffSections.add(section);
		}
	}

	public final List<Symbol> getSymbols() {
		return Collections.unmodifiableList(symbols);
	}

	/** Get the COFF file magic number.
	 * @return the magic number
	 */
	public final short getCoffMagic() {
		return f_magic;
	}

	/** Get the COFF file timestamp.
	 * @return the timestamp, as a numer of seconds since the UNIX epoch
	 */
	public final int getCoffTimestamp() {
		return f_timdat;
	}

	/** Get the COFF file flags.
	 * @return the flags word
	 */
	public final short getFlags() {
		return f_flags;
	}

	/** Get a list of sections in this COFF file.
	 * @return a list of sections
	 */
	public final List<CoffSection> getCoffSections() {
		return Collections.unmodifiableList(coffSections);
	}

	/** Get a list of symbols in this COFF file.
	 * @return a list of symbols
	 */
	public final List<CoffSymbol> getCoffSymbols() {
		return Collections.unmodifiableList(coffSymbols);
	}

	/** Get string from string table
	 * @param offet the required offset into the string table
	 * @return the corresponding string
	 */
	public final String getString(int offset) throws IOException {
		int i = offset;
		while (strings[i] != 0) {
			i += 1;
		}
		int length = i - offset;
		return new String(strings, offset, length, "ISO-8859-1");
	}

	/** Dump the COFF header to a stream in human-readable form.
	 * @param out the stream to which output should be sent
	 */
	public final void dump(PrintWriter out) {
		out.printf("Magic number: 0x%04X\n", f_magic);
		out.printf("Number of headers: %d\n", f_nscns);
		out.printf("Timestamp: %s\n", new Date(f_timdat * 1000L).toString());
		out.printf("Symbol table offset: %08X\n", f_symptr);
		out.printf("Number of symbols: %d\n", f_nsyms);
		out.printf("Optional header size: %d\n", f_opthdr);
		out.printf("Flags: 0x%04x\n", f_flags);
	}
}
