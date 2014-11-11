// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

/** A class to represent a generic CPUDL data type. */
public abstract class Type {
	/** Get the number of chunks of machine code matched by this type.
	 * If this function is not overridden, the number of chunks defaults to 0.
	 * @return the number of chunks
	 */
	public int getChunkCount() {
		return 0;
	}

	/** Get the number of pieces of assembly language matched by this type.
	 * If this function is not overridden, the number of pieces defaults to 0.
	 * @return the number of pieces
	 */
	public int getPieceCount() {
		return 0;
	}
}
