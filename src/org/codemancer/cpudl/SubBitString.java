// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// All rights reserved.

package org.codemancer.cpudl;

/** A class to represent a substring of a bitstring. */
public class SubBitString extends BitString {
	/** The bitstring of which this is a substring. */
	private final BitString parent;

	/** The offset in bits from the start of the parent bitstring
	 * to the start of this substring. */
	private final long offset;

	/** The length of this substring. */
	private final long size;

	/** Construct substring from bitstring.
	 * @param parent the bitstring of which this is a substring
	 * @param offset the offset to the start of this substring, in bits
	 * @param length the required length, in bits
	 */
	public SubBitString(BitString parent, long offset, long length) {
		if ((offset < 0) || (offset + length > parent.length())) {
			throw new IllegalArgumentException(
				"substring does not fully overlap parent bitstring");
		}
		this.parent = parent;
		this.offset = offset;
		this.size = length;
	}

	public final int getBit(long index) {
		if ((index < 0) || (index >= size)) {
			throw new IllegalArgumentException("bitstring index out of range");
		}
		return parent.getBit(offset + index);
	}

	public final long getBits(long index, long length) {
		if ((index < 0) || (index + length > size)) {
			throw new IllegalArgumentException("bitstring index out of range");
		}
		return parent.getBits(offset + index, length);
	}

	public final long length() {
		return size;
	}
}
