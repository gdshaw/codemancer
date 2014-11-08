// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.elf;

import java.io.IOException;
import java.nio.ByteBuffer;

/** A class to represent a section within an ELF file containing a string table. */
public class ElfStringTableSection extends ElfSection {
	/** Construct new string table section.
	 * On entry the ByteBuffer must be positioned at the start of the
	 * relevant section header. On exit the position is unspecified.
	 * @param buffer a ByteBuffer giving access to the underlying ELF file
	 * @param elf the ELF file to which the section belongs
	 */
	public ElfStringTableSection(ByteBuffer buffer, ElfFile elf) throws IOException {
		super(buffer, elf);
	}

	/** Get the string starting at a given offset into this table.
	 * @param offset the offset into this string table
	 * @return the string
	 */
	public final String getString(long offset) throws IOException {
		StringBuilder sb = new StringBuilder();
		int absOffset = (int)(getOffset() + offset);
		buffer.position(absOffset);
		char c;
		while ((c = (char)buffer.get()) != 0) {
			sb.append(c);
		}
		return sb.toString();
	}
}
