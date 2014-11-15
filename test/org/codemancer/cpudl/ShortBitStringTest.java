// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import org.codemancer.cpudl.ShortBitString;

public class ShortBitStringTest {
	private final long content = 0x243F6A8885A308D3L;

	@Test
	public void testGetBit() {
		assertEquals(1, new ShortBitString(content, 64).getBit(0));
		assertEquals(1, new ShortBitString(content, 64).getBit(1));
		assertEquals(0, new ShortBitString(content, 64).getBit(2));
		assertEquals(0, new ShortBitString(content, 64).getBit(3));
		assertEquals(1, new ShortBitString(content, 64).getBit(4));
		assertEquals(0, new ShortBitString(content, 64).getBit(5));
		assertEquals(1, new ShortBitString(content, 64).getBit(6));
		assertEquals(1, new ShortBitString(content, 64).getBit(7));
		assertEquals(0, new ShortBitString(content, 64).getBit(56));
		assertEquals(0, new ShortBitString(content, 64).getBit(57));
		assertEquals(1, new ShortBitString(content, 64).getBit(58));
		assertEquals(0, new ShortBitString(content, 64).getBit(59));
		assertEquals(0, new ShortBitString(content, 64).getBit(60));
		assertEquals(1, new ShortBitString(content, 64).getBit(61));
		assertEquals(0, new ShortBitString(content, 64).getBit(62));
		assertEquals(0, new ShortBitString(content, 64).getBit(63));
	}

	@Test
	public void testGetBits() {
		assertEquals(0, new ShortBitString(content, 64).getBits(0, 0));
		assertEquals(1, new ShortBitString(content, 64).getBits(0, 1));
		assertEquals(0xD3, new ShortBitString(content, 64).getBits(0, 8));
		assertEquals(0x308D3, new ShortBitString(content, 64).getBits(0, 20));
		assertEquals(content, new ShortBitString(content, 64).getBits(0, 64));
		assertEquals(0, new ShortBitString(content, 64).getBits(20, 0));
		assertEquals(0, new ShortBitString(content, 64).getBits(20, 1));
		assertEquals(0x5A, new ShortBitString(content, 64).getBits(20, 8));
		assertEquals(0x8885A, new ShortBitString(content, 64).getBits(20, 20));
	}

	@Test
	public void testLength() {
		assertEquals(0, new ShortBitString(0, 0).length());
		assertEquals(20, new ShortBitString(0, 20).length());
		assertEquals(64, new ShortBitString(content, 64).length());
	}

	@Test
	public void testSubstring() {
		assertEquals(new ShortBitString(), new ShortBitString(content, 64).substring(0, 0));
		assertEquals(new ShortBitString(), new ShortBitString(content, 64).substring(64, 64));
		assertEquals(new ShortBitString(1, 1), new ShortBitString(content, 64).substring(0, 1));
		assertEquals(new ShortBitString(0xD3, 8), new ShortBitString(content, 64).substring(0, 8));
		assertEquals(0x243F6A8885A308D3L, new ShortBitString(content, 64).substring(0, 64).getBits(0, 64));
		assertEquals(0x8885AL, new ShortBitString(content, 64).substring(20, 40).getBits(0, 20));
		assertEquals(0x243F6AL, new ShortBitString(content, 64).substring(40, 64).getBits(0, 24));
	}

	@Test
	public void testConcatBit() {
		BitString bits = new ShortBitString(content, 56);
		bits = bits.concat(0);
		bits = bits.concat(0);
		bits = bits.concat(1);
		bits = bits.concat(0);
		bits = bits.concat(0);
		bits = bits.concat(1);
		bits = bits.concat(0);
		bits = bits.concat(0);
		assertEquals(bits, new ShortBitString(content, 64));
	}

	@Test
	public void testConcat() {
		for (int i = 0; i <= 64; ++i) {
			int zLength = i;
			long zMask = (zLength < 64) ? ((1L << zLength) - 1) : -1;
			long zContent = content & zMask;
			for (int j = 0; j <= i; ++j) {
				int xLength = j;
				int yLength = i - j;
				long xMask = (xLength < 64) ? ((1L << xLength) - 1) : -1;
				long yMask = (yLength < 64) ? ((1L << yLength) - 1) : -1;
				long xContent = content & xMask;
				long yContent = (content >> xLength) & yMask;
				BitString xBits = new ShortBitString(xContent, xLength);
				BitString yBits = new ShortBitString(yContent, yLength);
				BitString zBits = xBits.concat(yBits);
				assertEquals(zBits.length(), zLength);
				assertEquals(zBits.getBits(0, zLength), zContent);
			}
		}
	}

	@Test
	public void testEquals() {
		assertEquals(new ShortBitString(), new ShortBitString());
		assertEquals(new ShortBitString(1, 1), new ShortBitString(1, 1));
		assertEquals(new ShortBitString(content, 64), new ShortBitString(content, 64));
		assertFalse(new ShortBitString(content, 64).equals(new ShortBitString(content, 63)));
		assertFalse(new ShortBitString(content, 64).equals(new ShortBitString(content + 1, 64)));
	}

	@Test
	public void testHashCode() {
		assertEquals(new ShortBitString().hashCode(), new ShortBitString().hashCode());
		assertEquals(new ShortBitString(1, 1).hashCode(), new ShortBitString(1, 1).hashCode());
		assertEquals(new ShortBitString(content, 64).hashCode(), new ShortBitString(content, 64).hashCode());
	}
}
