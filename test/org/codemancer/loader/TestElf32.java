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
import org.codemancer.loader.elf.ElfSymbolTableSection;
import org.codemancer.loader.elf.ElfRelocationSection;
import org.codemancer.loader.elf.ElfSegment;
import org.codemancer.loader.elf.ElfSymbol;
import org.codemancer.loader.elf.ElfRelocation;

// Test file compiled using gcc 4.6.3 for i686-linux-gnu.
// Test values obtained using readelf and/or objdump.

public class TestElf32 {
	private ElfFile elf;
	private ElfSection text;
	private ElfSymbolTableSection symtab;
	private ElfRelocationSection pltreltab;
	private ElfSegment loadseg0;
	private ElfSegment dynseg;

	public TestElf32() throws IOException {
		String pathname = "testdata" + File.separatorChar +
			"loader" + File.separatorChar + "hello-i686";
		RandomAccessFile file = new RandomAccessFile(pathname, "r");
		ByteBuffer image = file.getChannel().map(
			FileChannel.MapMode.READ_ONLY, 0, file.length());
		elf = new ElfFile(image);
		text = elf.getElfSections().get(13);
		symtab = (ElfSymbolTableSection)elf.getElfSections().get(28);
		pltreltab = (ElfRelocationSection)elf.getElfSections().get(10);
		loadseg0 = elf.getElfSegments().get(2);
		dynseg = elf.getElfSegments().get(4);
	}

	@Test
	public void testElfFile() {
		assertEquals(ElfFile.ELFCLASS32, elf.getElfFileClass());
		assertEquals(ElfFile.ET_EXEC, elf.getElfFileType());
		assertEquals(3, elf.getElfArchitecture());
		assertEquals(0x08048320, elf.getEntryPoint());
		assertEquals(0, elf.getElfFlags());
		assertEquals(30, elf.getElfSections().size());
	}

	@Test
	public void testElfTextSection() throws IOException {
		assertEquals(".text", text.getName());
		assertEquals(ElfSection.SHT_PROGBITS, text.getElfSectionType());
		assertEquals(6, text.getElfSectionFlags());
		assertEquals(0x08048320, text.getAddress());
		assertEquals(0x017c, text.getSize());
		assertEquals(16, text.getAlignment());
	}

	@Test
	public void testElfSymTabSection() throws IOException {
		assertEquals(".symtab", symtab.getName());
		assertEquals(ElfSection.SHT_SYMTAB, symtab.getElfSectionType());
		assertEquals(0, symtab.getElfSectionFlags());
		assertEquals(0, symtab.getAddress());
		assertEquals(0x0410, symtab.getSize());
		assertEquals(4, symtab.getAlignment());
	}

	@Test
	public void testMainSymbol() throws IOException {
		ElfSymbol sym = symtab.getElfSymbols().get(62);
		assertEquals("main", sym.getName());
		assertEquals(0x080483d4, sym.getValue());
		assertEquals(28, sym.getSize());
		assertEquals(ElfSymbol.STT_FUNC, sym.getElfType());
		assertEquals(ElfSymbol.STB_GLOBAL, sym.getElfBinding());
	}

	@Test
	public void testPutsRelocation() throws IOException {
		ElfRelocation rel = pltreltab.getElfRelocations().get(0);
		assertEquals(0x0804a000, rel.getAddress());
		assertEquals(7, rel.getElfRelocationType());
		assertEquals("puts", rel.getElfSymbol().getName());
		assertEquals(0, rel.getAddend());
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

	@Test
	public void testTextContent() {
		ByteBuffer content = text.getContent();
		assertEquals(0x895EED31, content.getInt() & 0xFFFFFFFF);
		assertEquals(0xF0E483E1, content.getInt() & 0xFFFFFFFF);
		assertEquals(0x017C, content.limit());
	}
}
