// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

/** A class to represent a bitstring of any length. */
public class LongBitString extends BitString {
	/** The content of this bitstring, as an array of little-endian
	 * long integers.
	 * Its length must be such that 64 * content.length >= size.
	 */
	private final long[] content;

	/** The length, in bits. */
	private final long size;

	/** Construct bitstring from array of long integers.
	 * The behaviour of this function is undefined if length < 0,
	 * or if the array is too short for the length.
	 * @param content the required content, as an array of
	 *  little-endian long integers
	 * @param length the required length, in bits
	 */
	public LongBitString(long[] content, long length) {
		this.content = content.clone();
		this.size = length;
	}

	public final int getBit(long index) {
		int wordIndex = (int)(index >> 6);
		int bitIndex = (int)(index & 63);
		return (int)((content[wordIndex] >> bitIndex) & 1);
	}

	public final long getBits(long index, long length) {
		// Decompose the given index to determine which word
		// and which bit within that word contains the first
		// bit of the result.
		int wordIndex = (int)(index >> 6);
		int bitIndex = (int)(index & 63);

		// Fetch and combine up to two words from the array,
		// shifting them into position as required. Do not fetch
		// any words unnecessarily, as that could cause an
		// array indexing exception to be thrown.
		long word = 0;
		if (length > 0) {
			word |= content[wordIndex] >>> bitIndex;
			if (bitIndex + length > 64) {
				word |= content[wordIndex + 1] << (64 - bitIndex);
			}
		}

		// Mask the fetched word or words to return the required
		// number of bits.
		long mask = (length < 64) ? ((1L << length) - 1) : -1;
		return word & mask;
	}

	public final long length() {
		return size;
	}
}
