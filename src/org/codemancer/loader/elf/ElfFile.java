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

import org.codemancer.loader.ObjectFile;
import org.codemancer.loader.Symbol;
import org.codemancer.loader.InvalidFileFormat;

/** A class to represent the content of an ELF file.
 * Both 32- and 64-bit ELF files are supported, using either
 * little- or big-endian encoding.
 *
 * Note that this class is responsible for configuring the byte order of
 * the ByteBuffer through which the ELF file is accessed. All other classes
 * must leave the byte order unchanged.
 */
public class ElfFile implements ObjectFile {
	/** A constant equal to the size of the ident field, in bytes. */
	private static final int EI_NIDENT = 16;

	/** A constant to indicate use of 32-bit ELF format. */
	public static final byte ELFCLASS32 = 1;

	/** A constant to indicate use of 64-bit ELF format. */
	public static final byte ELFCLASS64 = 2;

	/** A constant to indicate use of LSB-first encoding. */
	public static final byte ELFDATA2LSB = 1;

	/** A constant to indicate use of MSB-first encoding. */
	public static final byte ELFDATA2MSB = 2;

	/** A constant to indicate that a file has no specified type. */
	public static final byte ET_NONE = 0;

	/** A constant to indicate that a file is a relocatable file. */
	public static final byte ET_REL = 1;

	/** A constant to indicate that a file is an executable file. */
	public static final byte ET_EXEC = 2;

	/** A constant to indicate that a file is a shared object file. */
	public static final byte ET_DYN = 3;

	/** A constant to indicate that a file is a core file. */
	public static final byte ET_CORE = 4;

	/** A ByteBuffer giving access to the underlying ELF file. */
	private final ByteBuffer buffer;

	/** The ELF file class.
	 * This should be ELFCLASS32 or ELFCLASS64.
	 */
	private byte ei_class;

	/** The ELF data encoding method.
	 * This should be ELFDATA2LSB or ELFDATA2MSB.
	 */
	private byte ei_data;

	/** The ELF header version.
	 * This should be equal to 1.
	 */
	private byte ei_version;

	/** The object file type.
	 * Possible values include ET_REL, ET_EXEC, ET_DYN and ET_CORE.
	 */
	private short e_type;

	/** The machine architecture. */
	private short e_machine;

	/** The object file version. */
	private int e_version;

	/** The entry point, or zero if there is no entry point. */
	private long e_entry;

	/** The offset in bytes from the start of the file
	 * to the start of the program header table. */
	private long e_phoff;

	/** The offset in bytes from the start of the file
	 * to the start of the section header table. */
	private long e_shoff;

	/** The flags word. */
	private int e_flags;

	/** The size of the ELF header in bytes. */
	private short e_ehsize;

	/** The size of a program header table entry in bytes. */
	private short e_phentsize;

	/** The number of entries in the program header table. */
	private short e_phnum;

	/** The size of a section header table entry in bytes. */
	private short e_shentsize;

	/** The number of entries in the section header table. */
	private short e_shnum;

	/** The index of the section containing the string table
	 * used for section names. */
	private short e_shstrndx;

	/** The offset in bytes from the start of the file to the
	 * start of the strings section containing the section names. */
	private long shstroff;

	/** A table of ELF sections that have been parsed.
	 * The size of this table is equal to e_shnum.
	 * Entries for unparsed sections are set to null.
	 */
	private ArrayList<ElfSection> elfSections;

	/** A table of ELF segments that have been parsed.
	 * The size of this table is equal to e_phnum.
	 * Entries for unparsed segments are set to null.
	 */
	private ArrayList<ElfSegment> elfSegments;

	/** A list of symbols defined by this ELF file. */
	private ArrayList<Symbol> symbols = new ArrayList<Symbol>();

	/** Parse one of the sections from this ELF file.
	 * @param shndx the section index
	 * @return the section
	 */
	protected final ElfSection getElfSection(int shndx) throws IOException {
		// Validate the section index.
		if ((shndx < 0) || (shndx >= e_shnum) || (e_shoff == 0)) {
			throw new IllegalArgumentException("section index out of range");
		}

		// Check whether the section has already been parsed.
		ElfSection section = elfSections.get(shndx);

		// If not then parse it from the ByteBuffer.
		if (section == null) {
			int offset = (int)(e_shoff + shndx * e_shentsize);
			buffer.position(offset);
			section = ElfSection.makeSection(buffer, this);
			elfSections.set(shndx, section);
		}

		return section;
	}

	/** Parse one of the segments from this ELF file.
	 * @param phndx the segment index
	 * @return the segment
	 */
	protected final ElfSegment getElfSegment(int phndx) throws IOException {
		// Validate the segment index.
		if ((phndx < 0) || (phndx >= e_phnum) || (e_phoff == 0)) {
			throw new IllegalArgumentException("segment index out of range");
		}

		// Check whether the segment has already been parsed.
		ElfSegment segment = elfSegments.get(phndx);

		// If not then parse it from the ByteBuffer.
		if (segment == null) {
			int offset = (int)(e_phoff + phndx * e_phentsize);
			buffer.position(offset);
			segment = new ElfSegment(buffer, this);
			elfSegments.set(phndx, segment);
		}

		return segment;
	}

	/** Construct object to represent ELF file.
	 * On entry the ByteBuffer must be positioned at the start of the file.
	 * Any byte order is permissible. On exit the position is unspecified,
	 * but the byte order will have been set to match the ELF file.
	 * @param buffer the content of the ELF file as a ByteBuffer
	 */
	public ElfFile(ByteBuffer buffer) throws IOException {
		this.buffer = buffer;

		// Fetch the initial bytes of the header.
		byte[] e_ident = new byte[EI_NIDENT];
		buffer.get(e_ident, 0, EI_NIDENT);

		// Check that magic number is as expected.
		if ((e_ident[0] != 127) || (e_ident[1] != 'E') ||
			(e_ident[2] != 'L') || (e_ident[3] != 'F')) {
			throw new InvalidFileFormat("incorrect ELF magic number");
		}

		// Extract and validate the file class.
		ei_class = e_ident[4];
		if ((ei_class != ELFCLASS32) && (ei_class != ELFCLASS64)) {
			throw new InvalidFileFormat("invalid ELF file class");
		}

		// Extract and validate the data encoding, configuring the
		// ByteBuffer object to use the appropriate byte order.
		ei_data = e_ident[5];
		if (ei_data == ELFDATA2LSB) {
			buffer.order(ByteOrder.LITTLE_ENDIAN);
		} else if (ei_data == ELFDATA2MSB) {
			buffer.order(ByteOrder.BIG_ENDIAN);
		} else {
			throw new InvalidFileFormat("invalid ELF file encoding");
		}

		// Extract and validate the file version.
		ei_version = e_ident[6];
		if (ei_version != 1) {
			throw new InvalidFileFormat("invalid ELF file version");
		}

		// Fetch the remaining fields from the header.
		// (Note that this must be done after the byte order has
		// been configured.)
		e_type = buffer.getShort();
		e_machine = buffer.getShort();
		e_version = buffer.getInt();
		if (ei_class == ELFCLASS64) {
			e_entry = buffer.getLong();
			e_phoff = buffer.getLong();
			e_shoff = buffer.getLong();
		} else {
			e_entry = buffer.getInt() & 0xFFFFFFFFL;
			e_phoff = buffer.getInt() & 0xFFFFFFFFL;
			e_shoff = buffer.getInt() & 0xFFFFFFFFL;
		}
		e_flags = buffer.getInt();
		e_ehsize = buffer.getShort();
		e_phentsize = buffer.getShort();
		e_phnum = buffer.getShort();
		e_shentsize = buffer.getShort();
		e_shnum = buffer.getShort();
		e_shstrndx = buffer.getShort();

		// Initialise section and segment tables.
		elfSections = new ArrayList<ElfSection>(
			Collections.nCopies(e_shnum, (ElfSection)null));
		elfSegments = new ArrayList<ElfSegment>(
			Collections.nCopies(e_phnum, (ElfSegment)null));

		// Record the offset to the section name table.
		shstroff = getElfSection(e_shstrndx).getOffset();

		// Populate the section and segment tables.
		for (int i = 0; i != e_shnum; ++i) {
			ElfSection section = getElfSection(i);
			if (section instanceof ElfSymbolTableSection) {
				ElfSymbolTableSection symtab =
					(ElfSymbolTableSection)section;
				symbols.addAll(symtab.getElfSymbols());
			}
		}
		for (int i = 0; i != e_phnum; ++i) {
			getElfSegment(i);
		}
	}

	public final List<Symbol> getSymbols() {
		return Collections.unmodifiableList(symbols);
	}

	/** Get the ELF file class.
	 * @return the file class
	 */
	public final byte getElfFileClass() {
		return ei_class;
	}

	/** Get the ELF file type.
	 * @return the file type
	 */
	public final short getElfFileType() {
		return e_type;
	}

	/** Get the ELF machine architecture.
	 * @return the machine architecture
	 */
	public final short getElfArchitecture() {
		return e_machine;
	}

	/** Get the entry point address.
	 * @return the entry point, or 0 if none
	 */
	public final long getEntryPoint() {
		return e_entry;
	}

	/** Get ELF file flags.
	 * @return the flags word
	 */
	public final int getElfFlags() {
		return e_flags;
	}

	/** Get a list of the sections in this ELF file.
	 * @return a list of sections
	 */
	public final List<ElfSection> getElfSections() {
		return Collections.unmodifiableList(elfSections);
	}

	/** Get a list of the segments in this ELF file.
	 * @return a list of segments
	 */
	public final List<ElfSegment> getElfSegments() {
		return Collections.unmodifiableList(elfSegments);
	}

	/** Get the name of a section, given the offset into the relevant string table.
	 * @param offset the offset into the string table
	 * @return the section name
	 */
	public final String getElfSectionName(long offset) throws IOException {
		StringBuilder sb = new StringBuilder();
		int absOffset = (int)(shstroff + offset);
		buffer.position(absOffset);
		char c;
		while ((c = (char)buffer.get()) != 0) {
			sb.append(c);
		}
		return sb.toString();
	}

	/** Dump the ELF header to a stream in human-readable form.
	 * @param out the stream to which output should be sent
	 */
	public final void dump(PrintWriter out) {
		out.printf("File class: %d\n", ei_class);
		out.printf("Data encoding: %d\n", ei_data);
		out.printf("Header version: %d\n", ei_version);
		out.printf("Object file type: %d\n", e_type);
		out.printf("Architecture: %d\n", e_machine);
		out.printf("Object file version: %d\n", e_version);
		out.printf("Entry point: 0x%08x\n", e_entry);
		out.printf("Program header offset: 0x%08x\n", e_phoff);
		out.printf("Section header offset: 0x%08x\n", e_shoff);
		out.printf("Flags: 0x%08x\n", e_flags);
		out.printf("ELF header size: 0x%04x\n", e_ehsize);
		out.printf("Program header size: 0x%04x\n", e_phentsize);
		out.printf("Number of program headers: %d\n", e_phnum);
		out.printf("Section header size: 0x%04x\n", e_shentsize);
		out.printf("Number of section headers: %d\n", e_shnum);
		out.printf("String section index: %d\n", e_shstrndx);
	}
}
