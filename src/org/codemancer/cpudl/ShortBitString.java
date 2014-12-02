// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

/** A class to represent a bitstring of up to 64 bits. */
public class ShortBitString extends BitString {
	/** The content of this bitstring, as a little-endian
	 * long integer. */
	private final long content;

	/** The length of this bitstring. */
	private final long size;

	/** Construct empty bitstring. */
	public ShortBitString() {
		this.content = 0;
		this.size = 0;
	}

	/** Construct bitstring from long integer.
	 * The behaviour of this function is undefined if length < 0
	 * or length > 64.
	 * @param content the required content, as a long integer
	 * @param length the required length, in bits
	 * @param bigEndian true if the supplied content is big-endian,
	 *  otherwise false
	 */
	public ShortBitString(long content, long length, boolean bigEndian) {
		if (length > 64) {
			throw new IllegalArgumentException("length of ShortBitString must not exceed 64 bits");
		}
		if (bigEndian) {
			content <<= 64 - length;
			content = ((content & 0x5555555555555555L) << 1) |
				((content & 0xAAAAAAAAAAAAAAAAL) >>> 1);
			content = ((content & 0x3333333333333333L) << 2) |
				((content & 0xCCCCCCCCCCCCCCCCL) >>> 2);
			content = ((content & 0x0F0F0F0F0F0F0F0FL) << 4) |
				((content & 0xF0F0F0F0F0F0F0F0L) >>> 4);
			content = ((content & 0x00FF00FF00FF00FFL) << 8) |
				((content & 0xFF00FF00FF00FF00L) >>> 8);
			content = ((content & 0x0000FFFF0000FFFFL) << 16) |
				((content & 0xFFFF0000FFFF0000L) >>> 16);
			content = ((content & 0x00000000FFFFFFFFL) << 32) |
				((content & 0xFFFFFFFF00000000L) >>> 32);
		}
		this.content = content;
		this.size = length;
	}

	public final int getBit(long index) {
		return (int)((content >>> index) & 1);
	}

	public final long getBits(long index, long length, boolean bigEndian) {
		if (index < 0) {
			throw new IllegalArgumentException("index into bitstring must be non-negative");
		}
		if (index > this.size) {
			index = this.size;
		}
		if (index + length > this.size) {
			length = this.size - index;
		}
		long mask = (length < 64) ? ((1L << length) - 1) : -1;
		long bits = (content >>> index) & mask;

		if (bigEndian) {
			bits <<= 64 - length;
			bits = ((bits & 0x5555555555555555L) << 1) |
				((bits & 0xAAAAAAAAAAAAAAAAL) >>> 1);
			bits = ((bits & 0x3333333333333333L) << 2) |
				((bits & 0xCCCCCCCCCCCCCCCCL) >>> 2);
			bits = ((bits & 0x0F0F0F0F0F0F0F0FL) << 4) |
				((bits & 0xF0F0F0F0F0F0F0F0L) >>> 4);
			bits = ((bits & 0x00FF00FF00FF00FFL) << 8) |
				((bits & 0xFF00FF00FF00FF00L) >>> 8);
			bits = ((bits & 0x0000FFFF0000FFFFL) << 16) |
				((bits & 0xFFFF0000FFFF0000L) >>> 16);
			bits = ((bits & 0x00000000FFFFFFFFL) << 32) |
				((bits & 0xFFFFFFFF00000000L) >>> 32);
		}
		return bits;
	}

	public final long length() {
		return size;
	}
}
