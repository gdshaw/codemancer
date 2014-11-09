// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.aof;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.codemancer.loader.InvalidFileFormat;

/** A class to represent a relocation directive. */
public class AofRelocation {
	/** The location of the object to be relocated,
	 * as a byte offset from the start of the relevant area. */
	private long offset;

	/** The size of the object to be relocated, in bytes. */
	private int size;

	/** The symbol to which this relocation refers, or null if not applicable. */
	private AofSymbol symbol;

	/** True if this is a PC-relative relocation, false if it is an additive relocation. */
	boolean pcRelative;

	/** True if this is an additive symbol relocation, otherwise false. */
	boolean symbolic;

	/** Construct new relocation directive.
	 * @param buffer a ByteBuffer giving access to the underlying ELF file
	 * @param area the area to which the relocation directive belongs
	 */
	public AofRelocation(ByteBuffer buffer, AofArea area) throws IOException {
		AofFile aof = area.getAofFile();

		// Record offset.
		offset = buffer.getInt();

		// Read second word and determine type of relocation.
		int word1 = buffer.getInt();
		if ((word1 & 0xfff00000) == 0) {
			// Type 1 relocation.
			// Determine size.
			size = 1 << ((word1 >> 16) & 3);
			if (size > 4) {
				throw new InvalidFileFormat("invalid field type in type 1 relocation");
			}

			// Determine relocation method.
			pcRelative = ((word1 >> 18) & 1) != 0;
			if (pcRelative) {
				symbolic = false;
			} else {
				symbolic = ((word1 >> 19) & 1) != 0;
			}

			// Identify symbol, if applicable.
			if (symbolic) {
				int sid = word1 & 0xffff;
				symbol = aof.getSymbolTableChunk().getAofSymbol(sid);
			} else {
				symbol = null;
			}
		} else if ((word1 & 0x80000000) == 0x80000000) {
			// Type 2 relocation.
			// Determine size.
			size = 1 << ((word1 >> 24) & 3);
			if (size == 8) {
				size = ((word1 >> 29) & 3);
			}

			// Determine relocation method.
			pcRelative = ((word1 >> 26) & 1) != 0;
			symbolic = ((word1 >> 27) & 1) != 0;

			// Identify symbol, if applicable.
			if (symbolic) {
				int sid = word1 & 0xffffff;
				symbol = aof.getSymbolTableChunk().getAofSymbol(sid);
			} else {
				symbol = null;
			}
		} else {
			throw new InvalidFileFormat("relocation not valid as type 1 or type 2");
		}
	}

	/** Get the offset to which this relocation is applicable.
	 * @return the offset
	 */
	public long getOffset() {
		return offset;
	}

	/** Get the size of the object relocated.
	 * @return the size, in bytes
	 */
	public long getSize() {
		return size;
	}

	/** Get the symbol to which this relocation refers.
	 * @return the symbol
	 */
	public AofSymbol getSymbol() {
		return symbol;
	}

	private String getMethodString() {
		if (pcRelative) {
			return "PC-relative";
		} else {
			if (symbolic) {
				return "Add-symbol";
			} else {
				return "Add-internal";
			}
		}
	}

	public void dump(PrintWriter out) throws IOException {
		String symbolStr = (symbol != null) ? symbol.getName() : "";
		String methodStr = getMethodString();
		out.printf("%08x %d %12s %s\n", offset, size, methodStr, symbolStr);
	}
}
