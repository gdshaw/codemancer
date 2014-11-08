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
}
