// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.elf;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

/** A class to represent a symbol from an ELF symbol table. */
public class ElfSymbol {
	/** A constant to indicate that a symbol is local to the object file. */
	public static final byte STB_LOCAL = 0;

	/** A constant to indicate that a symbol is global. */
	public static final byte STB_GLOBAL = 1;

	/** A constant to indicate that a symbol is weak. */
	public static final byte STB_WEAK = 2;

	/** A constant to indicate that a symbol has no type. */
	public static final byte STT_NOTYPE = 0;

	/** A constant to indicate that a symbol refers to a data object. */
	public static final byte STT_OBJECT = 1;

	/** A constant to indicate that a symbol refers to a function
	 * entry point. */
	public static final byte STT_FUNC = 2;

	/** A constant to indicate that a symbol refers to a section. */
	public static final byte STT_SECTION = 3;

	/** A constant to indicate that a symbol refers to the object file
	 * as a whole. */
	public static final byte STT_FILE = 4;

	/** The name of this symbol. */
	private final String st_name;

	/** The symbol type. */
	private final byte st_type;

	/** The symbol binding. */
	private final byte st_bind;

	/** The section to which this symbol refers. */
	private final short st_shndx;

	/** The value of this symbol. */
	private final long st_value;

	/** The size of the value of this symbol. */
	private final long st_size;

	/** Construct new symbol.
	 * On entry the ByteBuffer must be positioned at the start of the
	 * relevant symbol. On exit its position is unspecified.
	 * @param buffer a ByteBuffer giving access to the underlying ELF file
	 * @param elf the ELF file to which the symbol belongs
	 * @param sect the section to which the symbol belongs
	 * @param strtab the string table section used by this symbol
	 */
	public ElfSymbol(ByteBuffer buffer, ElfFile elf, ElfSection sect,
		ElfStringTableSection strtab) throws IOException {

		byte fileClass = elf.getElfFileClass();

		byte st_info;
		byte st_other;
		int namendx = buffer.getInt();
		if (fileClass == ElfFile.ELFCLASS64) {
			st_info = buffer.get();
			st_other = buffer.get();
			st_shndx = buffer.getShort();
			st_value = buffer.getLong();
			st_size = buffer.getLong();
		} else {
			st_value = buffer.getInt() & 0xFFFFFFFFL;
			st_size = buffer.getInt() & 0xFFFFFFFFL;
			st_info = buffer.get();
			st_other = buffer.get();
			st_shndx = buffer.getShort();
		}

		st_name = strtab.getString(namendx);
		st_type = (byte)(st_info & 0xf);
		st_bind = (byte)(st_info >> 4);
	}

	/** Get the name of this symbol.
	 * @return the name
	 */
	public final String getName() {
		return st_name;
	}

	/** Get the value of this symbol.
	 * @return the value
	 */
	public final long getValue() {
		return st_value;
	}

	/** Get the size of this symbol.
	 * @return the size, or 0 if not applicable
	 */
	public final long getSize() {
		return st_size;
	}

	/** Get the type of this ELF symbol.
	 * @return the symbol type
	 */
	public final byte getElfType() {
		return st_type;
	}

	/** Get the binding of this ELF symbol.
	 * @return the symbol binding
	 */
	public final byte getElfBinding() {
		return st_bind;
	}

	/** Get the type of this ELF symbol as a string.
	 * @return the type, as a string
	 */
	public final String getElfTypeString() {
		switch (st_type) {
		case STT_NOTYPE:
			return "NOTYPE";
		case STT_OBJECT:
			return "OBJECT";
		case STT_FUNC:
			return "FUNC";
		case STT_SECTION:
			return "SECTION";
		case STT_FILE:
			return "FILE";
		default:
			return "UNKNOWN";
		}
	}

	/** Get the binding of this ELF symbol as a string.
	 * @return the binding, as a string
	 */
	public final String getElfBindingString() {
		switch (st_bind) {
		case STB_LOCAL:
			return "LOCAL";
		case STB_GLOBAL:
			return "GLOBAL";
		case STB_WEAK:
			return "WEAK";
		default:
			return "UNKNOWN";
		}
	}

	public void dump(PrintWriter out) throws IOException {
		out.printf("%016x%6d %7s %7s%4d %s\n", st_value, st_size,
			getElfTypeString(), getElfBindingString(),
			st_shndx, st_name);
	}
}
