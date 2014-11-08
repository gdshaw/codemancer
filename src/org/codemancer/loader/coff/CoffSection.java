// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.coff;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/** A class to represent a section within an COFF file. */
public class CoffSection {
	/** A flag to indicate that a section contains only executable code. */
	public static final int STYP_TEXT = 0x0020;

	/** A flag to indicate that a section contains only initialised data. */
	public static final int STYP_DATA = 0x0040;

	/** A flag to indicate that a section defines a region of uninitialised data. */
	public static final int STYP_BSS = 0x0080;

	/** The size of a symbol table entry, in bytes. */
	public static final int SCNHSZ = 40;

	/** A ByteBuffer giving access to the underlying COFF file. */
	protected final ByteBuffer buffer;

	/** An object representing the decoded COFF file as a whole. */
	protected final CoffFile coff;

	/** The name of this section. */
	private final String s_name;

	/** The physical address of this section. */
	private final int s_paddr;

	/** The virtual address of this section. */
	private final int s_vaddr;

	/** The size of this section. */
	private final int s_size;

	/** The file offset of the raw data for this section. */
	private final int s_scnptr;

	/** The file offset of the relocation data for this section. */
	private final int s_relptr;

	/** The file offset of the line number information for this section. */
	private final int s_lnnoptr;

	/** The number of relocation entries for this section. */
	private final short s_nreloc;

	/** The number of line number entries for this section. */
	private final short s_nlnno;

	/** The flags for this section. */
	private final int s_flags;

	/** A list of relocation directives for this COFF section. */
	private ArrayList<CoffRelocation> coffRelocations;

	/** Construct new section.
	 * @param buffer a ByteBuffer giving access to the underlying COFF file
	 * @param elf the COFF file to which the section belongs
	 * @return the newly constructed section
	 */
	public CoffSection(ByteBuffer buffer, CoffFile coff)
		throws IOException {

		this.buffer = buffer;
		this.coff = coff;

		// Parse the section name.
		byte[] bytes = new byte[8];
		buffer.get(bytes, 0, 8);
		int length = 0;
		while ((length < 8) && (bytes[length] != 0)) {
			length += 1;
		}
		s_name = new String(bytes, 0, length, "ISO-8859-1");

		// Parse the remainder of the section header.
		s_paddr = buffer.getInt();
		s_vaddr = buffer.getInt();
		s_size = buffer.getInt();
		s_scnptr = buffer.getInt();
		s_relptr = buffer.getInt();
		s_lnnoptr = buffer.getInt();
		s_nreloc = buffer.getShort();
		s_nlnno = buffer.getShort();
		s_flags = buffer.getInt();

		// Parse relocations.
		coffRelocations = new ArrayList<CoffRelocation>(s_nreloc);
		buffer.position(s_relptr);
		for (int i = 0; i < s_nreloc; ++i) {
			CoffRelocation rel = new CoffRelocationZ80(buffer, this);
			coffRelocations.add(rel);
		}
	}

	/** Get the COFF file to which this section belongs.
	 * @return the COFF file
	 */
	public final CoffFile getCoffFile() {
		return coff;
	}

	/** Get the name of this COFF section.
	 * @return the name
	 */
	public final String getName() throws IOException {
		return s_name;
	}

	/** Get the size of this COFF section.
	 * @return the size
	 */
	public final long getSize() {
		return s_size;
	}

	/** Get the number of relocations.
	 * @return the number of relocations
	 */
	public int getCoffRelocationCount() {
		return coffRelocations.size();
	}

	/** Get relocation at given index.
	 * @param index the index of the required relocation
	 * @return the relocation, or null if not found
	 */
	public CoffRelocation getCoffRelocation(int index) {
		return coffRelocations.get(index);
	}

	/** Dump the section header to a stream in human-readable form.
	 * @param out the stream to be written to
	 */
	public void dump(PrintWriter out) throws IOException {
		out.printf("Name: %s\n", s_name);
		out.printf("Physical address: 0x%08x\n", s_paddr);
		out.printf("Virtual address: 0x%08x\n", s_vaddr);
		out.printf("Size: 0x%08x\n", s_size);
		out.printf("Raw data offset: 0x%08x\n", s_scnptr);
		out.printf("Relocation data offset: 0x%08x\n", s_relptr);
		out.printf("Line number offset: 0x%08x\n", s_lnnoptr);
		out.printf("Number of relocations: 0x%04x\n", s_nreloc);
		out.printf("Number of line entries: 0x%04x\n", s_nlnno);
		out.printf("Flags: 0x%08x\n", s_flags);
	}
}
