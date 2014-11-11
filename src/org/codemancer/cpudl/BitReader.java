// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

/** An interface for reading from a sequence of bits. */
public interface BitReader {
	/** Read a number of bits from this source as a bitstring.
	 * If there are insufficient bits remaining then this function will
	 * read all of the remaining bits, without padding.
	 * If count < 0 then the behaviour of this function is undefined.
	 * The position is advanced by the number of bits read.
	 * @param count the number of bits to read
	 * @return the bits as a bitstring
	 */
	public BitString read(long count);
}
