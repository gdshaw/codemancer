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
	 * @param content the required content, as a little-endian
	 *  long integer
	 * @param length the required length, in bits
	 */
	public ShortBitString(long content, long length) {
		this.content = content;
		this.size = length;
	}

	public final int getBit(long index) {
		return (int)((content >> index) & 1);
	}

	public final long getBits(long index, long length) {
		long mask = (length < 64) ? ((1L << length) - 1) : -1;
		return (content >> index) & mask;
	}

	public final long length() {
		return size;
	}
}
