// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

/** An abstract class to represent a sequence of bits.
 * Instances of this class are required to be immutable.
 */
public abstract class BitString {
	/** Get a bit from this bitstring.
	 * The behaviour of this function is undefined if
	 * index < 0 or index >= this.length().
	 * @param index the index of the bit to be inspected
	 * @return the bit at the given index
	 */
	public abstract int getBit(long index);

	/** Get a number of bits from this bitstring.
	 * The behaviour of this function is undefined if
	 * index < 0 or index + length > this.length().
	 * @param index the index of the first bit to be inspected
	 * @param length the number of bits to be inspected
	 * @return the bits, as a little-endian long integer
	 */
	public abstract long getBits(long index, long length);

	/** Get the length of this bitstring.
	 * @return the length, in bits
	 */
	public abstract long length();

	/** Extract a substring of this bitstring.
	 * It is an error if beginIndex < 0, beginIndex > endIndex, or
	 * endIndex > this.length().
	 * @param beginIndex the index at which the substring is to begin (inclusive)
	 * @param endIndex the index at which the substring is to end (exclusive)
	 */
	public BitString substring(long beginIndex, long endIndex) {
		return new SubBitString(this, beginIndex, endIndex - beginIndex);
	}

	public boolean equals(Object that) {
		if (this == that) return true;
		if (!(that instanceof BitString)) return false;
		BitString bits = (BitString)that;

		long length = this.length();
		if (bits.length() != length) return false;

		long offset = 0;
		while (offset + 64 <= length) {
			if (bits.getBits(offset, 64) != getBits(offset, 64)) return false;
			offset += 64;
		}
		if (bits.getBits(offset, length - offset) != getBits(offset, length - offset)) return false;
		return true;
	}

	public int hashCode() {
		int hash = 0;
		long offset = 0;
		long length = this.length();
		while (offset + 32 <= length) {
			hash += getBits(offset, 32);
			offset += 32;
		}
		hash += getBits(offset, length - offset);
		return hash;
	}
}
