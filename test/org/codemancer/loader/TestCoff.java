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
		CoffSection sect = coff.getCoffSection(0);
		assertEquals(".text", sect.getName());
		assertEquals(0x0000010b, sect.getSize());
	}

	@Test
	public void testDataSection() throws IOException {
		CoffSection sect = coff.getCoffSection(1);
		assertEquals(".data", sect.getName());
		assertEquals(0x0000000f, sect.getSize());
	}
}
