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

// Test file compiled using GCCSDK 3.4.6 release 3.
// Type, location and size of chunks obtained by inspection of hex dump.

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
		AofChunk chunk = aof.getUniqueChunk("OBJ_HEAD", true);
		assertEquals("OBJ_HEAD", chunk.getChunkId());
		assertEquals(0x005c, chunk.getFileOffset());
		assertEquals(0x0054, chunk.getSize());
	}

	@Test
	public void testAofIdfnChunk() throws IOException {
		AofChunk chunk = aof.getUniqueChunk("OBJ_IDFN", true);
		assertEquals("OBJ_IDFN", chunk.getChunkId());
		assertEquals(0x00b0, chunk.getFileOffset());
		assertEquals(0x0064, chunk.getSize());
	}

	@Test
	public void testAofStrtChunk() throws IOException {
		AofChunk chunk = aof.getUniqueChunk("OBJ_STRT", true);
		assertEquals("OBJ_STRT", chunk.getChunkId());
		assertEquals(0x0114, chunk.getFileOffset());
		assertEquals(0x0058, chunk.getSize());
	}

	@Test
	public void testAofSymtChunk() throws IOException {
		AofChunk chunk = aof.getUniqueChunk("OBJ_SYMT", true);
		assertEquals("OBJ_SYMT", chunk.getChunkId());
		assertEquals(0x016c, chunk.getFileOffset());
		assertEquals(0x0080, chunk.getSize());
	}

	@Test
	public void testAofAreaChunk() throws IOException {
		AofChunk chunk = aof.getUniqueChunk("OBJ_AREA", true);
		assertEquals("OBJ_AREA", chunk.getChunkId());
		assertEquals(0x01ec, chunk.getFileOffset());
		assertEquals(0x006c, chunk.getSize());
	}
}
