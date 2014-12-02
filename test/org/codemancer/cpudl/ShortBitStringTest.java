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
	private final long rcontent = 0xCB10C5A11156FC24L;

	@Test
	public void testLength() {
		assertEquals(0, new ShortBitString(0, 0, false).length());
		assertEquals(20, new ShortBitString(0, 20, false).length());
		assertEquals(64, new ShortBitString(content, 64, false).length());
	}

	@Test
	public void testGetBit() {
		assertEquals(1, new ShortBitString(content, 64, false).getBit(0));
		assertEquals(1, new ShortBitString(content, 64, false).getBit(1));
		assertEquals(0, new ShortBitString(content, 64, false).getBit(2));
		assertEquals(0, new ShortBitString(content, 64, false).getBit(3));
		assertEquals(1, new ShortBitString(content, 64, false).getBit(4));
		assertEquals(0, new ShortBitString(content, 64, false).getBit(5));
		assertEquals(1, new ShortBitString(content, 64, false).getBit(6));
		assertEquals(1, new ShortBitString(content, 64, false).getBit(7));
		assertEquals(0, new ShortBitString(content, 64, false).getBit(56));
		assertEquals(0, new ShortBitString(content, 64, false).getBit(57));
		assertEquals(1, new ShortBitString(content, 64, false).getBit(58));
		assertEquals(0, new ShortBitString(content, 64, false).getBit(59));
		assertEquals(0, new ShortBitString(content, 64, false).getBit(60));
		assertEquals(1, new ShortBitString(content, 64, false).getBit(61));
		assertEquals(0, new ShortBitString(content, 64, false).getBit(62));
		assertEquals(0, new ShortBitString(content, 64, false).getBit(63));
	}

	@Test
	public void testGetBits() {
		assertEquals(0, new ShortBitString(content, 64, false).getBits(0, 0, false));
		assertEquals(1, new ShortBitString(content, 64, false).getBits(0, 1, false));
		assertEquals(0xD3, new ShortBitString(content, 64, false).getBits(0, 8, false));
		assertEquals(0x308D3, new ShortBitString(content, 64, false).getBits(0, 20, false));
		assertEquals(content, new ShortBitString(content, 64, false).getBits(0, 64, false));
		assertEquals(0, new ShortBitString(content, 64, false).getBits(20, 0, false));
		assertEquals(0, new ShortBitString(content, 64, false).getBits(20, 1, false));
		assertEquals(0x5A, new ShortBitString(content, 64, false).getBits(20, 8, false));
		assertEquals(0x8885A, new ShortBitString(content, 64, false).getBits(20, 20, false));

		assertEquals(0, new ShortBitString(content, 64, false).getBits(0, 0, true));
		assertEquals(1, new ShortBitString(content, 64, false).getBits(0, 1, true));
		assertEquals(0xCB, new ShortBitString(content, 64, false).getBits(0, 8, true));
		assertEquals(0xCB10C, new ShortBitString(content, 64, false).getBits(0, 20, true));
		assertEquals(rcontent, new ShortBitString(content, 64, false).getBits(0, 64, true));
		assertEquals(0, new ShortBitString(content, 64, false).getBits(20, 0, true));
		assertEquals(0, new ShortBitString(content, 64, false).getBits(20, 1, true));
		assertEquals(0x5A, new ShortBitString(content, 64, false).getBits(20, 8, true));
		assertEquals(0x5A111, new ShortBitString(content, 64, false).getBits(20, 20, true));
	}

	@Test
	public void testSetBit() {
		for (int i = 1; i <= 64; ++i) {
			int xLength = i;
			long xMask = (1L << xLength) - 1;
			long xContent = content & xMask;
			BitString xBits = new ShortBitString(xContent, xLength, false);
			for (int j = 0; j < i; ++j) {
				BitString yBits = xBits.setBit(j, 0);
				BitString zBits = xBits.setBit(j, 1);
				for (int k = 0; k < i; ++k) {
					if (k == j) {
						assertEquals(0, yBits.getBit(k));
						assertEquals(1, zBits.getBit(k));
					} else {
						assertEquals(xBits.getBit(k), yBits.getBit(k));
						assertEquals(xBits.getBit(k), zBits.getBit(k));
					}
				}
			}
		}
	}

	@Test
	public void testSubstring() {
		assertEquals(new ShortBitString(), new ShortBitString(content, 64, false).substring(0, 0));
		assertEquals(new ShortBitString(), new ShortBitString(content, 64, false).substring(64, 64));
		assertEquals(new ShortBitString(1, 1, false), new ShortBitString(content, 64, false).substring(0, 1));
		assertEquals(new ShortBitString(0xD3, 8, false), new ShortBitString(content, 64, false).substring(0, 8));
		assertEquals(0x243F6A8885A308D3L, new ShortBitString(content, 64, false).substring(0, 64).getBits(0, 64, false));
		assertEquals(0x8885AL, new ShortBitString(content, 64, false).substring(20, 40).getBits(0, 20, false));
		assertEquals(0x243F6AL, new ShortBitString(content, 64, false).substring(40, 64).getBits(0, 24, false));
	}

	@Test
	public void testConcatBit() {
		BitString bits = new ShortBitString(content, 56, false);
		bits = bits.concat(0);
		bits = bits.concat(0);
		bits = bits.concat(1);
		bits = bits.concat(0);
		bits = bits.concat(0);
		bits = bits.concat(1);
		bits = bits.concat(0);
		bits = bits.concat(0);
		assertEquals(bits, new ShortBitString(content, 64, false));
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
				BitString xBits = new ShortBitString(xContent, xLength, false);
				BitString yBits = new ShortBitString(yContent, yLength, false);
				BitString zBits = xBits.concat(yBits);
				assertEquals(zBits.length(), zLength);
				assertEquals(zBits.getBits(0, zLength, false), zContent);
			}
		}
	}

	@Test
	public void testEquals() {
		assertEquals(new ShortBitString(), new ShortBitString());
		assertEquals(new ShortBitString(1, 1, false), new ShortBitString(1, 1, false));
		assertEquals(new ShortBitString(content, 64, false), new ShortBitString(content, 64, false));
		assertFalse(new ShortBitString(content, 64, false).equals(new ShortBitString(content, 63, false)));
		assertFalse(new ShortBitString(content, 64, false).equals(new ShortBitString(content + 1, 64, false)));
	}

	@Test
	public void testHashCode() {
		assertEquals(new ShortBitString().hashCode(), new ShortBitString().hashCode());
		assertEquals(new ShortBitString(1, 1, false).hashCode(), new ShortBitString(1, 1, false).hashCode());
		assertEquals(new ShortBitString(content, 64, false).hashCode(), new ShortBitString(content, 64, false).hashCode());
	}
}
