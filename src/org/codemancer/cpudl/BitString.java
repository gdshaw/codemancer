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

	/** Concatenate a given bit to the end of this bitstring.
	 * This bitstring, being immutable, is not altered by the concatenation.
	 * @param bit the bit to be concatenated
	 * @return the concatenated bitstring
	 */
	public BitString concat(int bit) {
		long oldLength = length();
		long newLength = oldLength + 1;
		if (newLength <= 64) {
			long oldContent = getBits(0, oldLength);
			long newContent = oldContent | ((bit & 1L) << oldLength);
			return new ShortBitString(newContent, newLength);
		} else {
			return concat(new ShortBitString(bit, 1));
		}
	}

	/** Concatenate a given bitstring to the end of this bitstring.
	 * This bitstring, being immutable, is not altered by the concatenation.
	 * @param bits the bitstring to be concatenated
	 * @return the concatenated bitstring
	 */
	public BitString concat(BitString bits) {
		long oldLength = length();
		long addLength = bits.length();
		long newLength = oldLength + addLength;
		if (addLength == 0) {
			// This test is required because it is assumed below that addLength != 0.
			return this;
		} else if (newLength <= 64) {
			long oldContent = getBits(0, oldLength);
			long newContent = oldContent | (bits.getBits(0, addLength) << oldLength);
			return new ShortBitString(newContent, newLength);
		} else {
			long newContent[] = new long[(int)((newLength + 63) / 64)];
			int index = 0;

			// Copy any complete words from the left hand side to the result.
			long oldOffset = 0;
			long oldRemaining = oldLength;
			while (oldRemaining >= 64) {
				newContent[index] = getBits(oldOffset, 64);
				index += 1;
				oldOffset += 64;
				oldRemaining -= 64;
			}

			// Copy any remaining bits from the left hand side, then append bits from the
			// right hand side to make this (as nearly as possible) into a complete word.
			long addRemaining = addLength;
			long addOffset = Math.min(addRemaining, (64 - oldRemaining));
			newContent[index] = getBits(oldOffset, oldRemaining) |
				(bits.getBits(0, addOffset) << oldRemaining);
			index += 1;
			addRemaining -= addOffset;

			// Append any remaining complete words from the right hand side.
			while (addRemaining >= 64) {
				newContent[index] = bits.getBits(addOffset, 64);
				index += 1;
				addOffset += 64;
				addRemaining -= 64;
			}

			// Append any remaining bits from the right hand side.
			if (addRemaining > 0) {
				newContent[index] = bits.getBits(addOffset, addRemaining);
			}

			return new LongBitString(newContent, newLength);
		}
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
