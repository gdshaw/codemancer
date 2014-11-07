// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.elf;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

/** A class to represent a segment within an ELF file. */
public class ElfSegment {
	/** A constant to indicate that a program header table entry is
	 * not used. */
	public static final int PT_NULL = 0;

	/** A constant to indicate that a segment is loadable. */
	public static final int PT_LOAD = 1;

	/** A constant to indicate that a segment contains dynamic
	 * linking information. */
	public static final int PT_DYNAMIC = 2;

	/** A constant to indicate that a segment contains an
	 * interpreter name. */
	public static final int PT_INTERP = 3;

	/** A constant to indicate that a segment contains auxiliary
	 * information. */
	public static final int PT_NOTE = 4;

	/** A constant to identify a type of segment that has unspecified
	 * semantics. */
	public static final int PT_SHLIB = 5;

	/** A constant to indicate that a segment contains the program
	 * header table. */
	public static final int PT_PHDR = 6;

	/** A ByteBuffer giving access to the underlying ELF file. */
	private final ByteBuffer buffer;

	/** An object representing the decoded ELF file as a whole. */
	private final ElfFile elf;

	/** The type of this segment. */
	private int p_type;

	/** The file byte offset corresponding to the start of this segment. */
	private long p_offset;

	/** The virtual byte address corresponding to the start of this
	 * segment. */
	private long p_vaddr;

	/** The physical byte address (if applicable) corresponding to the
	 * start of this segment. */
	private long p_paddr;

	/** The size of this segment within the file image, in bytes. */
	private long p_filesz;

	/** The size of this segment within the memory image, in bytes. */
	private long p_memsz;

	/** The flags associated with this segment. */
	private int p_flags;

	/** The required alignment of this segment, in bytes. */
	private long p_align;

	/** Construct object to represent ELF segment.
	 * On entry the ByteBuffer must be positioned at the start of the
	 * relevant program header. On exit the position is unspecified.
	 * @param buffer a ByteBuffer giving access to the underlying ELF file
	 * @param elf the ELF file to which the segment belongs
	 */
	public ElfSegment(ByteBuffer buffer, ElfFile elf) throws IOException {
		this.elf = elf;
		this.buffer = buffer;
		byte fileClass = elf.getElfFileClass();

		p_type = buffer.getInt();
		if (fileClass == ElfFile.ELFCLASS64) {
			p_flags = buffer.getInt();
			p_offset = buffer.getLong();
			p_vaddr = buffer.getLong();
			p_paddr = buffer.getLong();
			p_filesz = buffer.getLong();
			p_memsz = buffer.getLong();
			p_align = buffer.getLong();
		} else {
			p_offset = buffer.getInt() & 0xFFFFFFFFL;
			p_vaddr = buffer.getInt() & 0xFFFFFFFFL;
			p_paddr = buffer.getInt() & 0xFFFFFFFFL;
			p_filesz = buffer.getInt() & 0xFFFFFFFFL;
			p_memsz = buffer.getInt() & 0xFFFFFFFFL;
			p_flags = buffer.getInt();
			p_align = buffer.getInt() & 0xFFFFFFFFL;
		}
	}

	/** Get ELF segment type.
	 * @return the segment type
	 */
	public final int getElfSegmentType() {
		return p_type;
	}

	/** Get virtual address.
	 * @return the virtual byte address of the start of this segment.
	 */
	public final long getVirtualAddress() {
		return p_vaddr;
	}

	/** Get physical address, if applicable.
	 * @return the physical byte address of the start of this segment.
	 */
	public final long getPhysicalAddress() {
		return p_vaddr;
	}

	/** Get size in object file.
	 * @return the size, in bytes
	 */
	public final long getFileSize() {
		return p_filesz;
	}

	/** Get size in memory.
	 * @return the size, in bytes
	 */
	public final long getMemorySize() {
		return p_memsz;
	}

	/** Get ELF segment flags.
	 * @return the flags word
	 */
	public final int getElfSegmentFlags() {
		return p_flags;
	}

	/** Get alignment.
	 * @return the alignment, in bytes.
	 */
	public final long getAlignment() {
		return p_align;
	}

	/** Dump the segment header to a stream in human-readable form.
	 * @param out the stream to be written to
	 */
	public void dump(PrintWriter out) throws IOException {
		out.printf("Type: 0x%08x\n", p_type);
		out.printf("File offset: 0x%08x\n", p_offset);
		out.printf("Virtual address: 0x%08x\n", p_vaddr);
		out.printf("Physical address: 0x%08x\n", p_paddr);
		out.printf("File size: 0x%08x\n", p_filesz);
		out.printf("Member size: 0x%08x\n", p_memsz);
		out.printf("Flags: 0x%08x\n", p_flags);
		out.printf("Alignment: %d\n", p_align);
	}
}
