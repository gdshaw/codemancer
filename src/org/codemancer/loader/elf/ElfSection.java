// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.elf;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

/** A class to represent a section within an ELF file. */
public class ElfSection {
	/** A constant to indicate that a section header is not used. */
	public static final int SHT_NULL = 0;

	/** A constant to indicate that a section contains part of a
	 * program. */
	public static final int SHT_PROGBITS = 1;

	/** A constant to indicate that a section contains a symbol table. */
	public static final int SHT_SYMTAB = 2;

	/** A constant to indicate that a section contains a string table. */
	public static final int SHT_STRTAB = 3;

	/** A constant to indicate that a section contains rela-type
	 * relocations. */
	public static final int SHT_RELA = 4;

	/** A constant to indicate that a section contains a symbol
	 * hash table. */
	public static final int SHT_HASH = 5;

	/** A constant to indicate that a section contains dynamic linking
	 * tables. */
	public static final int SHT_DYNAMIC = 6;

	/** A constant to indicate that a section contains auxiliary
	 * information. */
	public static final int SHT_NOTE = 7;

	/** A constant to indicate that a section contains uninitialised
	 * space. */
	public static final int SHT_NOBITS = 8;

	/** A constant to indicate that a section contains rel-style
	 * relocations. */
	public static final int SHT_REL = 9;

	/** A constant corresponding to a reserved section type. */
	public static final int SHT_SHLIB = 10;

	/** A constant to indicate that a section contains a dynamic symbol
	 * table. */
	public static final int SHT_DYNSYM = 11;

	/** A flag to indicate that a section is writeable. */
	public static final int SHF_WRITE = 0x1;

	/** A flag to indicate that a section has a presence in memory. */
	public static final int SHF_ALLOC = 0x2;

	/** A flag to indicate that a section contains executable machine
	 * instructions. */
	public static final int SHF_EXECINSTR = 0x4;

	/** A mask to identify flags with processor-specific semantics. */
	public static final int SHF_MASKPROC = 0xf0000000;

	/** Construct new section.
	 * If the section type is recognised then the object returned will be
	 * of an appropriate subclass, otherwise it will be of this class.
	 * @param buffer a ByteBuffer giving access to the underlying ELF file
	 * @param elf the ELF file to which the section belongs
	 * @return the newly constructed section
	 */
	public static ElfSection makeSection(ByteBuffer buffer, ElfFile elf)
		throws IOException {

		// Parse the sh_type field, which is needed to determine what
		// type of section to construct, but then rewind the ByteBuffer
		// to its original position in order to let the constructor see
		// the whole section header.
		int position = buffer.position();
		int sh_name = buffer.getInt();
		int sh_type = buffer.getInt();
		buffer.position(position);

		switch (sh_type) {
		case SHT_SYMTAB:
		case SHT_DYNSYM:
			return new ElfSymbolTableSection(buffer, elf);
		default:
			return new ElfSection(buffer, elf);
		}
	}

	/** A ByteBuffer giving access to the underlying ELF file. */
	private final ByteBuffer buffer;

	/** An object representing the decoded ELF file as a whole. */
	private final ElfFile elf;

	/** The name of this section, as a section header string table
	 * index. */
	private int sh_name;

	/** The type of this section. */
	private int sh_type;

	/** The flags associated with this section. */
	private long sh_flags;

	/** The virtual byte address corresponding to the start of this
	 * section. */
	private long sh_addr;

	/** The file byte offset corresponding to the start of this section. */
	private long sh_offset;

	/** The size of this section, in bytes. */
	private long sh_size;

	/** The section index of the string or symbol table associated with
	 * this section.
	 * For SHT_DYNAMIC, SHT_SYMTAB or SHT_DYNSYM this is the supporting
	 * symbol table. For SHT_HASH, SHT_REL or SHT_RELA this is the symbol
	 * table to which this section applies.
	 */
	private int sh_link;

	/** Extra information about this section.
	 * For a SHT_REL or SHT_RELA this is the index of the section to
	 * which the relocations apply. For a SHT_SYMTAB or SHT_DYNSYM this
	 * is the index of the first non-local symbol.
	 */
	private int sh_info;

	/** The required alignment of this section, in bytes. */
	private long sh_addralign;

	/** The entry size, in bytes, or zero if this section does not have
	 * fixed-size entries.
	 */
	private long sh_entsize;

	/** Construct object to represent ELF section.
	 * On entry the ByteBuffer must be positioned at the start of the
	 * relevant section header. A defensive copy is made immediately,
	 * and from that point onward the ElfSection instance neither
	 * modifies nor depends on the original ByteBuffer.
	 * @param parentBuffer a ByteBuffer giving access to the underlying
	 *  ELF file
	 * @param elf the ELF file to which the section belongs
	 */
	public ElfSection(ByteBuffer parentBuffer, ElfFile elf)
		throws IOException {

		this.elf = elf;
		this.buffer = parentBuffer.duplicate();
		this.buffer.order(parentBuffer.order());
		byte fileClass = elf.getElfFileClass();

		sh_name = buffer.getInt();
		sh_type = buffer.getInt();
		if (fileClass == ElfFile.ELFCLASS64) {
			sh_flags = buffer.getLong();
			sh_addr = buffer.getLong();
			sh_offset = buffer.getLong();
			sh_size = buffer.getLong();
		} else {
			sh_flags = buffer.getInt() & 0xFFFFFFFFL;
			sh_addr = buffer.getInt() & 0xFFFFFFFFL;
			sh_offset = buffer.getInt() & 0xFFFFFFFFL;
			sh_size = buffer.getInt() & 0xFFFFFFFFL;
		}
		sh_link = buffer.getInt();
		sh_info = buffer.getInt();
		if (fileClass == ElfFile.ELFCLASS64) {
			sh_addralign = buffer.getLong();
			sh_entsize = buffer.getLong();
		} else {
			sh_addralign = buffer.getInt() & 0xFFFFFFFFL;
			sh_entsize = buffer.getInt() & 0xFFFFFFFFL;
		}
	}

	/** Get ELF file.
	 * @return the ELF file to which this section belongs
	 */
	public ElfFile getElfFile() {
		return elf;
	}

	/** Get ELF section name.
	 * @return the section name
	 */
	public final String getName() throws IOException {
		return elf.getElfSectionName(sh_name);
	}

	/** Get ELF section type.
	 * @return the section type
	 */
	public final int getElfSectionType() {
		return sh_type;
	}

	/** Get ELF section flags.
	 * @return the flags word
	 */
	public final long getElfSectionFlags() {
		return sh_flags;
	}

	/** Get file offset of this section.
	 * @return the file offset
	 */
	public final long getOffset() {
		return sh_offset;
	}

	/** Get address of this section, if applicable.
	 * @return the byte address, or 0 if not applicable
	 */
	public final long getAddress() {
		return sh_addr;
	}

	/** Get size of this section.
	 * @return the size, in bytes
	 */
	public final long getSize() {
		return sh_size;
	}

	/** Get alignment of this section.
	 * @return the alignment, in bytes
	 */
	public final long getAlignment() {
		return sh_addralign;
	}

	/** Get entry size of this section.
	 * @return the entry size, in bytes
	 */
	public final long getEntrySize() {
		return sh_entsize;
	}

	/** Get linked section.
	 * @return the linked section, or null if none
	 */
	public <T> T getLinkedSection() throws IOException {
		ElfSection sect = elf.getElfSection(sh_link);
		return (T)sect;
	}

	/** Get a string from a linked string table.
	 * This function must only be used when the linked section
	 * contains a string table.
	 * @param index the index into the string table
	 * @return the string
	 */
	public final String getLinkedString(long index) throws IOException {
		StringBuilder sb = new StringBuilder();
		ElfSection sect = elf.getElfSection(sh_link);
		int absOffset = (int)(sect.getOffset() + index);
		buffer.position(absOffset);
		char c;
		while ((c = (char)buffer.get()) != 0) {
			sb.append(c);
		}
		return sb.toString();
	}

	/** Dump the section header to a stream in human-readable form.
	 * @param out the stream to be written to
	 */
	public void dump(PrintWriter out) throws IOException {
		out.printf("Name: %s\n", getName());
		out.printf("Type: 0x%08x\n", sh_type);
		out.printf("Flags: 0x%08x\n", sh_flags);
		out.printf("Virtual address: 0x%08x\n", sh_addr);
		out.printf("File offset: 0x%08x\n", sh_offset);
		out.printf("Size: 0x%08x\n", sh_size);
		out.printf("Linked section: %d\n", sh_link);
		out.printf("Information: 0x%04x\n", sh_info);
		out.printf("Alignment: %d\n", sh_addralign);
		out.printf("Entry size: 0x%04x\n", sh_entsize);
	}
}
