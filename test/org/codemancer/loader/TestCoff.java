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

import org.codemancer.loader.coff.CoffFile;
import org.codemancer.loader.coff.CoffSection;
import org.codemancer.loader.coff.CoffSymbol;
import org.codemancer.loader.coff.CoffRelocationZ80;

// Test file compiled using GNU as 2.22 for z80-unknown-coff.
// Test values obtained using objdump.

public class TestCoff {
	private CoffFile coff;

	public TestCoff() throws IOException {
		String pathname = "testdata" + File.separatorChar +
			"loader" + File.separatorChar + "hello-cpm-z80.o";
		RandomAccessFile file = new RandomAccessFile(pathname, "r");
		ByteBuffer image = file.getChannel().map(
			FileChannel.MapMode.READ_ONLY, 0, file.length());
		coff = new CoffFile(image);
	}

	@Test
	public void testCoffFile() {
		assertEquals(0x805a, coff.getCoffMagic() & 0xFFFF);
	}

	@Test
	public void testTextSection() throws IOException {
		CoffSection sect = coff.getCoffSections().get(0);
		assertEquals(".text", sect.getName());
		assertEquals(0x0000010b, sect.getSize());
	}

	@Test
	public void testDataSection() throws IOException {
		CoffSection sect = coff.getCoffSections().get(1);
		assertEquals(".data", sect.getName());
		assertEquals(0x0000000f, sect.getSize());
	}

	@Test
	public void testStartSymbol() throws IOException {
		// Index into symbol table now counts auxiliary entries.
		CoffSymbol sym = coff.getCoffSymbols().get(9);
		assertEquals("_start", sym.getName());
		assertEquals(0x100, sym.getValue());
	}

	@Test
	public void testMessageRelocation() throws IOException {
		CoffSection sect = coff.getCoffSections().get(0);
		CoffRelocationZ80 rel = (CoffRelocationZ80)sect.getCoffRelocations().get(0);
		assertEquals(0x101, rel.getAddress());
		assertEquals(".data", rel.getCoffSymbol().getName());
		assertEquals(1, rel.getCoffRelocationType());
	}

	@Test
	public void testTextContent() {
		CoffSection sect = coff.getCoffSections().get(0);
		ByteBuffer content = sect.getContent();
		content.position(0x100);
		assertEquals(0x0E000011, content.getInt() & 0xFFFFFFFF);
		assertEquals(0x0005CD09, content.getInt() & 0xFFFFFFFF);
		assertEquals(0x010B, content.limit());
	}
}
