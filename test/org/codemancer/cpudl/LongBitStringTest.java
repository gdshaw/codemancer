package org.codemancer.cpudl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.LongBitString;

public class LongBitStringTest {
	private final long[] content = {0x243F6A8885A308D3L, 0x13198A2E03707344L,
		0xA4093822299F31D0L, 0x082EFA98EC4E6C89L};
	private final long[] altContent = {0x243F6A8885A308D3L, 0x13198A2E03707344L,
		0xA4093822299F31D0L, 0x082EFA98EC4E6C8AL};

	@Test
	public void testGetBit() {
		assertEquals(1, new LongBitString(content, 256).getBit(0));
		assertEquals(1, new LongBitString(content, 256).getBit(1));
		assertEquals(0, new LongBitString(content, 256).getBit(2));
		assertEquals(0, new LongBitString(content, 256).getBit(3));
		assertEquals(1, new LongBitString(content, 256).getBit(4));
		assertEquals(0, new LongBitString(content, 256).getBit(5));
		assertEquals(1, new LongBitString(content, 256).getBit(6));
		assertEquals(1, new LongBitString(content, 256).getBit(7));
		assertEquals(0, new LongBitString(content, 256).getBit(248));
		assertEquals(0, new LongBitString(content, 256).getBit(249));
		assertEquals(0, new LongBitString(content, 256).getBit(250));
		assertEquals(1, new LongBitString(content, 256).getBit(251));
		assertEquals(0, new LongBitString(content, 256).getBit(252));
		assertEquals(0, new LongBitString(content, 256).getBit(253));
		assertEquals(0, new LongBitString(content, 256).getBit(254));
		assertEquals(0, new LongBitString(content, 256).getBit(255));
	}

	@Test
	public void testGetBits() {
		assertEquals(0, new LongBitString(content, 256).getBits(0, 0));
		assertEquals(1, new LongBitString(content, 256).getBits(0, 1));
		assertEquals(0xD3, new LongBitString(content, 256).getBits(0, 8));
		assertEquals(0x308D3, new LongBitString(content, 256).getBits(0, 20));
		assertEquals(0x243F6A8885A308D3L, new LongBitString(content, 256).getBits(0, 64));
		assertEquals(0, new LongBitString(content, 256).getBits(20, 0));
		assertEquals(0, new LongBitString(content, 256).getBits(20, 1));
		assertEquals(0x5A, new LongBitString(content, 256).getBits(20, 8));
		assertEquals(0x8885A, new LongBitString(content, 256).getBits(20, 20));
		assertEquals(0x07344243F6A8885AL, new LongBitString(content, 256).getBits(20, 64));
	}

	@Test
	public void testLength() {
		assertEquals(0, new LongBitString(content, 0).length());
		assertEquals(20, new LongBitString(content, 20).length());
		assertEquals(256, new LongBitString(content, 256).length());
	}

	@Test
	public void testSubstring() {
		assertEquals(new ShortBitString(), new LongBitString(content, 256).substring(0, 0));
		assertEquals(new ShortBitString(), new LongBitString(content, 256).substring(256, 256));
		assertEquals(new ShortBitString(1, 1), new LongBitString(content, 256).substring(0, 1));
		assertEquals(new ShortBitString(0xD3, 8), new LongBitString(content, 256).substring(0, 8));
		assertEquals(0x243F6A8885A308D3L, new LongBitString(content, 256).substring(0, 64).getBits(0, 64));
		assertEquals(0x07344243F6A8885AL, new LongBitString(content, 256).substring(20, 84).getBits(0, 64));
		assertEquals(0x082EFA98EC4E6C89L, new LongBitString(content, 256).substring(192, 256).getBits(0, 64));
	}

	@Test
	public void testConcatBit() {
		BitString bits1 = new LongBitString(content, 60);
		bits1 = bits1.concat(0);
		bits1 = bits1.concat(1);
		bits1 = bits1.concat(0);
		bits1 = bits1.concat(0);
		bits1 = bits1.concat(0);
		bits1 = bits1.concat(0);
		bits1 = bits1.concat(1);
		bits1 = bits1.concat(0);
		assertEquals(bits1, new LongBitString(content, 68));
		BitString bits2 = new LongBitString(content, 124);
		bits2 = bits2.concat(1);
		bits2 = bits2.concat(0);
		bits2 = bits2.concat(0);
		bits2 = bits2.concat(0);
		bits2 = bits2.concat(0);
		bits2 = bits2.concat(0);
		bits2 = bits2.concat(0);
		bits2 = bits2.concat(0);
		bits2 = bits2.concat(1);
		bits2 = bits2.concat(0);
		bits2 = bits2.concat(1);
		bits2 = bits2.concat(1);
		assertEquals(bits2, new LongBitString(content, 136));
	}

	@Test
	public void testConcat() {
		BitString bits = new LongBitString(content, 256);
		for (int i = 0; i <= 256; ++i) {
			int zLength = i;
			for (int j = 0; j <= i; ++j) {
				int xLength = j;
				int yLength = i - j;
				BitString xBits = bits.substring(0, xLength);
				BitString yBits = bits.substring(xLength, xLength + yLength);
				BitString zBits = xBits.concat(yBits);
				assertEquals(zBits.length(), zLength);
				assertEquals(zBits, bits.substring(0, zLength));
			}
		}
	}

	@Test
	public void testEquals() {
		assertEquals(new LongBitString(content, 0), new LongBitString(content, 0));
		assertEquals(new LongBitString(content, 1), new LongBitString(content, 1));
		assertEquals(new LongBitString(content, 64), new LongBitString(content, 64));
		assertEquals(new LongBitString(content, 256), new LongBitString(content, 256));
		assertFalse(new LongBitString(content, 256).equals(new LongBitString(content, 255)));
		assertFalse(new LongBitString(content, 256).equals(new LongBitString(altContent, 256)));
	}

	@Test
	public void testHashCode() {
		assertEquals(new LongBitString(content, 0).hashCode(), new LongBitString(content, 0).hashCode());
		assertEquals(new LongBitString(content, 1).hashCode(), new LongBitString(content, 1).hashCode());
		assertEquals(new LongBitString(content, 64).hashCode(), new LongBitString(content, 64).hashCode());
		assertEquals(new LongBitString(content, 256).hashCode(), new LongBitString(content, 256).hashCode());
	}

	@Test
	public void testImmutable() {
		long[] mutableContent = content.clone();
		LongBitString mutatedBitString = new LongBitString(mutableContent, 256);
		mutableContent[0] += 1;
		assertEquals(new LongBitString(content, 256), mutatedBitString);
	}
}
