// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.codemancer.loader.elf.ElfFile;
import org.codemancer.loader.elf.ElfSection;
import org.codemancer.loader.elf.ElfSegment;

// Test file compiled using gcc 4.6.3 for i686-linux-gnu.
// Test values obtained using readelf and/or objdump.

public class TestElf32 {
	private ElfFile elf;
	private ElfSection text;
	private ElfSection symtab;
	private ElfSegment loadseg0;
	private ElfSegment dynseg;

	public TestElf32() throws IOException {
		String pathname = "testdata" + File.separatorChar +
			"loader" + File.separatorChar + "hello-i686";
		RandomAccessFile file = new RandomAccessFile(pathname, "r");
		ByteBuffer image = file.getChannel().map(
			FileChannel.MapMode.READ_ONLY, 0, file.length());
		elf = new ElfFile(image);
		text = elf.getElfSection(13);
		symtab = elf.getElfSection(28);
		loadseg0 = elf.getElfSegment(2);
		dynseg = elf.getElfSegment(4);
	}

	@Test
	public void testElfFile() {
		assertEquals(ElfFile.ELFCLASS32, elf.getElfFileClass());
		assertEquals(ElfFile.ET_EXEC, elf.getElfFileType());
		assertEquals(3, elf.getElfArchitecture());
		assertEquals(0x08048320, elf.getEntryPoint());
		assertEquals(0, elf.getElfFlags());
		assertEquals(30, elf.getElfSectionCount());
	}

	@Test
	public void testElfTextSection() {
		assertEquals(ElfSection.SHT_PROGBITS, text.getElfSectionType());
		assertEquals(6, text.getElfSectionFlags());
		assertEquals(0x08048320, text.getAddress());
		assertEquals(0x017c, text.getSize());
		assertEquals(16, text.getAlignment());
	}

	@Test
	public void testElfSymTabSection() {
		assertEquals(ElfSection.SHT_SYMTAB, symtab.getElfSectionType());
		assertEquals(0, symtab.getElfSectionFlags());
		assertEquals(0, symtab.getAddress());
		assertEquals(0x0410, symtab.getSize());
		assertEquals(4, symtab.getAlignment());
	}

	@Test
	public void testElfLoadSegment0() {
		assertEquals(ElfSegment.PT_LOAD, loadseg0.getElfSegmentType());
		assertEquals(0x08048000, loadseg0.getVirtualAddress());
		assertEquals(0x08048000, loadseg0.getPhysicalAddress());
		assertEquals(0x005c8, loadseg0.getFileSize());
		assertEquals(0x005c8, loadseg0.getMemorySize());
		assertEquals(5, loadseg0.getElfSegmentFlags());
		assertEquals(0x1000, loadseg0.getAlignment());
	}

	@Test
	public void testElfDynamicSegment() {
		assertEquals(ElfSegment.PT_DYNAMIC, dynseg.getElfSegmentType());
		assertEquals(0x08049f28, dynseg.getVirtualAddress());
		assertEquals(0x08049f28, dynseg.getPhysicalAddress());
		assertEquals(0x000c8, dynseg.getFileSize());
		assertEquals(0x000c8, dynseg.getMemorySize());
		assertEquals(6, dynseg.getElfSegmentFlags());
		assertEquals(4, dynseg.getAlignment());
	}
}