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

import org.codemancer.loader.aof.AofFile;
import org.codemancer.loader.aof.AofChunk;
import org.codemancer.loader.aof.AofHeaderChunk;
import org.codemancer.loader.aof.AofIdentificationChunk;
import org.codemancer.loader.aof.AofSymbolTableChunk;
import org.codemancer.loader.aof.AofSymbol;
import org.codemancer.loader.aof.AofArea;
import org.codemancer.loader.aof.AofRelocation;

// Test file compiled using GCCSDK 3.4.6 release 3.
// Type, location and size of chunks obtained by inspection of hex dump.
// Area base addresses calculated assuming start address of 0x8000.
// Other information obtained using decaof 2.00 (part of GCCSDK above).

public class TestAof {
	private AofFile aof;

	public TestAof() throws IOException {
		String pathname = "testdata" + File.separatorChar +
			"loader" + File.separatorChar + "hello-riscos-arm.o";
		RandomAccessFile file = new RandomAccessFile(pathname, "r");
		ByteBuffer image = file.getChannel().map(
			FileChannel.MapMode.READ_ONLY, 0, file.length());
		aof = new AofFile(image);
	}

	@Test
	public void testAofFile() throws IOException {
		assertEquals(5, aof.getMaxChunks());
	}

	@Test
	public void testAofHeadChunk() throws IOException {
		AofHeaderChunk chunk = aof.getHeaderChunk();
		assertEquals("OBJ_HEAD", chunk.getChunkId());
		assertEquals(0x005c, chunk.getFileOffset());
		assertEquals(0x0054, chunk.getSize());
		assertEquals(310, chunk.getVersionId());
		assertEquals(3, chunk.getNumAreas());
		assertEquals(8, chunk.getNumSymbols());
	}

	@Test
	public void testAofIdfnChunk() throws IOException {
		AofIdentificationChunk chunk = aof.getIdentificationChunk();
		assertEquals("OBJ_IDFN", chunk.getChunkId());
		assertEquals(0x00b0, chunk.getFileOffset());
		assertEquals(0x0064, chunk.getSize());
		assertEquals("GCCSDK AS AOF/ELF Assembler v1.46 (Jul 22 2007) [GCCSDK 3.4.6 (RISC OS GCCSDK 3.4.6 Release 3)]\n",
			chunk.getIdentificationString());
	}

	@Test
	public void testAofStrtChunk() throws IOException {
		AofChunk chunk = aof.getStringTableChunk();
		assertEquals("OBJ_STRT", chunk.getChunkId());
		assertEquals(0x0114, chunk.getFileOffset());
		assertEquals(0x0058, chunk.getSize());
	}

	@Test
	public void testAofSymtChunk() throws IOException {
		AofChunk chunk = aof.getSymbolTableChunk();
		assertEquals("OBJ_SYMT", chunk.getChunkId());
		assertEquals(0x016c, chunk.getFileOffset());
		assertEquals(0x0080, chunk.getSize());
	}

	@Test
	public void testAofAreaChunk() throws IOException {
		AofChunk chunk = aof.getAreaChunk();
		assertEquals("OBJ_AREA", chunk.getChunkId());
		assertEquals(0x01ec, chunk.getFileOffset());
		assertEquals(0x006c, chunk.getSize());
	}

	@Test
	public void testMainSymbol() throws IOException {
		AofSymbol symbol = aof.getSymbolTableChunk().getAofSymbols().get(2);
		assertEquals("main", symbol.getName());
		assertEquals(0, symbol.getValue());
	}

	@Test
	public void testArea0() throws IOException {
		AofArea area = aof.getHeaderChunk().getAofAreas().get(0);
		assertEquals("C$$code2", area.getName());
		assertEquals(0x34, area.getSize());
		assertEquals(0x8000, area.getAddress());
	}

	@Test
	public void testArea1() throws IOException {
		AofArea area = aof.getHeaderChunk().getAofAreas().get(1);
		assertEquals("C$$rodata1", area.getName());
		assertEquals(0x10, area.getSize());
		assertEquals(0x8034, area.getAddress());
	}

	@Test
	public void testArea0Relocation0() throws IOException {
		AofArea area = aof.getHeaderChunk().getAofAreas().get(0);
		AofRelocation rel = area.getAofRelocations().get(0);
		assertEquals(0x30, rel.getAddress());
		assertEquals(4, rel.getSize());
		assertEquals("__main", rel.getSymbol().getName());
	}

	@Test
	public void testArea0Content() throws IOException {
		AofArea area = aof.getHeaderChunk().getAofAreas().get(0);
		ByteBuffer content = area.getContent();
		assertEquals(0xE1A0C00D, content.getInt() & 0xFFFFFFFF);
		assertEquals(0xE92DDA00, content.getInt() & 0xFFFFFFFF);
		assertEquals(52, content.limit());
	}
}
