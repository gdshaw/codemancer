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

public class TestElf64 {
	private ElfFile elf;

	public TestElf64() throws IOException {
		String pathname = "testdata" + File.separatorChar +
			"loader" + File.separatorChar + "hello_amd64";
		RandomAccessFile file = new RandomAccessFile(pathname, "r");
		ByteBuffer image = file.getChannel().map(
			FileChannel.MapMode.READ_ONLY, 0, file.length());
		elf = new ElfFile(image);
	}

	@Test
	public void testElfFileClass() {
		assertEquals(elf.getElfFileClass(), ElfFile.ELFCLASS64);
	}

	@Test
	public void testElfFileType() {
		assertEquals(elf.getElfFileType(), ElfFile.ET_EXEC);
	}

	@Test
	public void testArchitecture() {
		assertEquals(elf.getElfArchitecture(), 62);
	}

	@Test
	public void testEntryPoint() {
		assertEquals(elf.getEntryPoint(), 0x400410);
	}

	@Test
	public void testElfFlags() {
		assertEquals(elf.getElfFlags(), 0);
	}
}
