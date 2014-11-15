// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.codemancer.cpudl.ShortBitString;
import org.codemancer.cpudl.LongBitString;
import org.codemancer.cpudl.BitStringReader;

public class BitStringReaderTest {
	private final long content = 0x243F6A8885A308D3L;
        private final long[] longContent = {0x243F6A8885A308D3L, 0x13198A2E03707344L,
                0xA4093822299F31D0L, 0x082EFA98EC4E6C89L};

	@Test
	public void testReadSingleBits() {
		BitReader bits = new BitStringReader(new ShortBitString(content, 64));
		assertEquals(new ShortBitString(1, 1), bits.read(1));
		assertEquals(new ShortBitString(1, 1), bits.read(1));
		assertEquals(new ShortBitString(0, 1), bits.read(1));
		assertEquals(new ShortBitString(0, 1), bits.read(1));
		assertEquals(new ShortBitString(1, 1), bits.read(1));
		assertEquals(new ShortBitString(0, 1), bits.read(1));
		assertEquals(new ShortBitString(1, 1), bits.read(1));
		assertEquals(new ShortBitString(1, 1), bits.read(1));
	}

	@Test
	public void testReadQuadBits() {
		BitReader bits = new BitStringReader(new ShortBitString(content, 64));
		assertEquals(new ShortBitString(0x3, 4), bits.read(4));
		assertEquals(new ShortBitString(0xD, 4), bits.read(4));
		assertEquals(new ShortBitString(0x8, 4), bits.read(4));
		assertEquals(new ShortBitString(0x0, 4), bits.read(4));
		assertEquals(new ShortBitString(0x3, 4), bits.read(4));
		assertEquals(new ShortBitString(0xA, 4), bits.read(4));
		assertEquals(new ShortBitString(0x5, 4), bits.read(4));
		assertEquals(new ShortBitString(0x8, 4), bits.read(4));
	}

	@Test
	public void testReadMixedBits() {
		BitReader bits = new BitStringReader(new ShortBitString(content, 64));
		assertEquals(new ShortBitString(0xD3, 8), bits.read(8));
		assertEquals(new ShortBitString(0x08, 5), bits.read(5));
		assertEquals(new ShortBitString(0x18, 7), bits.read(7));
		assertEquals(new ShortBitString(0x1A, 5), bits.read(5));
		assertEquals(new ShortBitString(0x42, 7), bits.read(7));
	}

	@Test
	public void testReadBeyondEnd() {
		BitReader bits = new BitStringReader(new ShortBitString(content, 60));
		bits.read(48);
		assertEquals(new ShortBitString(0x43F, 12), bits.read(16));
	}

	@Test
	public void testReadLongMixedBits() {
		BitReader bits = new BitStringReader(new LongBitString(longContent, 256));
		bits.read(96);
		assertEquals(new ShortBitString(0x31D013198A2EL, 48), bits.read(48));
	}

	@Test
	public void testPeek() {
		BitReader bits = new BitStringReader(new LongBitString(longContent, 256));
		assertEquals(1, bits.peek(7));
		assertEquals(1, bits.peek(6));
		assertEquals(0, bits.peek(5));
		assertEquals(1, bits.peek(4));
		assertEquals(0, bits.peek(3));
		assertEquals(0, bits.peek(2));
		assertEquals(1, bits.peek(1));
		assertEquals(1, bits.peek(0));
		bits.read(1);
		assertEquals(0, bits.peek(7));
		assertEquals(1, bits.peek(6));
		assertEquals(1, bits.peek(5));
		assertEquals(0, bits.peek(4));
		assertEquals(1, bits.peek(3));
		assertEquals(0, bits.peek(2));
		assertEquals(0, bits.peek(1));
		assertEquals(1, bits.peek(0));
		bits.read(3);
		assertEquals(1, bits.peek(7));
		assertEquals(0, bits.peek(6));
		assertEquals(0, bits.peek(5));
		assertEquals(0, bits.peek(4));
		assertEquals(1, bits.peek(3));
		assertEquals(1, bits.peek(2));
		assertEquals(0, bits.peek(1));
		assertEquals(1, bits.peek(0));
		bits.read(56);
		assertEquals(0, bits.peek(7));
		assertEquals(1, bits.peek(6));
		assertEquals(0, bits.peek(5));
		assertEquals(0, bits.peek(4));
		assertEquals(0, bits.peek(3));
		assertEquals(0, bits.peek(2));
		assertEquals(1, bits.peek(1));
		assertEquals(0, bits.peek(0));
	}

	@Test
	public void testSeek() {
		BitReader bits = new BitStringReader(new LongBitString(longContent, 256));
		bits.seek(1);
		assertEquals(new ShortBitString(0x69, 8), bits.read(8));
		bits.seek(56);
		assertEquals(new ShortBitString(0x24, 8), bits.read(8));
		bits.seek(63);
		assertEquals(new ShortBitString(0x88, 8), bits.read(8));
		bits.seek(64);
		assertEquals(new ShortBitString(0x44, 8), bits.read(8));
		bits.seek(120);
		assertEquals(new ShortBitString(0x13, 8), bits.read(8));
		bits.seek(190);
		assertEquals(new ShortBitString(0x26, 8), bits.read(8));
	}

	@Test
	public void testTell() {
		for (int i = 1; i != 256; ++i) {
			BitReader bits = new BitStringReader(new LongBitString(longContent, 256));
			for (int j = 0; j < 256; j += i) {
				assertEquals(j, bits.tell());
				bits.read(i);
			}
		}
	}
}
