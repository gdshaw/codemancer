// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.List;

import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.expr.Expression;

/** A class to represent a generic CPUDL data type. */
public abstract class Type {
	/** Get the number of chunks of machine code matched by this type.
	 * If this function is not overridden, the number of chunks defaults to 0.
	 * @return the number of chunks
	 */
	public int getChunkCount() {
		return 0;
	}

	/** Attempt to decode a collection of bit sequences as an instance of this type.
	 * If decoding is successful then the bit readers are left positioned at the end
	 * of the respective chunks. If decoding fails then their positions are unspecified.
	 * @param readers sources of bits, one for each chunk
	 * @return an expression corresponding to the bit sequences, or null if they did not match
	 */
	public Expression decode(List<BitReader> readers) {
		return null;
	}

	/** Get the number of pieces of assembly language matched by this type.
	 * If this function is not overridden, the number of pieces defaults to 0.
	 * @return the number of pieces
	 */
	public int getPieceCount() {
		return 0;
	}

	/** Construct a piece of assembly language to match this fragment.
	 * It is an error if piece < 0 or piece >= getPieceCount().
	 * @param piece the piece number
	 * @param expr the expression to be unparsed
	 * @return the corresponding string
	 */
	public String unparse(int piece, Expression expr) {
		throw new IllegalArgumentException("invalid piece number");
	}
}
