// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

/** An abstract class for reading bits from a bitstring. */
public class BitStringReader implements BitReader {
	/** The underlying bitstring. */
	private final BitString bits;

	/** The current position within the underlying bitstring. */
	long position = 0;

	/** Construct a BitStringReader from a BitString.
	 * @param bits a bitstring containing the data to be read
	 */
	public BitStringReader(BitString bits) {
		this.bits = bits;
	}

	public final BitString read(long count) {
		if (count > bits.length() - position) {
			count = bits.length() - position;
		}
		BitString result = bits.substring(position, position + count);
		position += count;
		return result;
	}
}
