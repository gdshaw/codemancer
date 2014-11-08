// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.coff;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

/** A class to represent a COFF relocation directive for the Z80. */
public class CoffRelocationZ80 extends CoffRelocation {
	/** The location to which the relocation is applicable. */
	private int r_vaddr;

	/** An offset to be added to the symbol. */
	private int r_offset;

	/** The type of this relocation. */
	private int r_type;

        /** The symbol to which this relocation refers. */
        private CoffSymbol symbol;

	/** Construct new Z80 relocation directive.
	 * On entry the ByteBuffer must be positioned at the start of the
	 * required directive. On exit it will be positioned immediately after
	 * the end of that directive.
	 * @param buffer a ByteBuffer giving access to the underlying COFF file
	 * @param sect the section to which the relocation directive belongs
	 */
	public CoffRelocationZ80(ByteBuffer buffer, CoffSection sect)
		throws IOException {

		// Read the relocation directive.
		r_vaddr = buffer.getInt();
		int symndx = buffer.getInt();
		r_offset = buffer.getInt();
		r_type = buffer.getShort();

		// Get the relevant symbol.
		symbol = sect.getCoffFile().getCoffSymbol(symndx);
	}

	/** Get the address to which this relocation is applicable.
	 * @return the offset
	 */
	public long getOffset() {
		return r_vaddr;
	}

	/** Get the type of this relocation.
	 * @return the type
	 */
	public int getCoffRelocationType() {
		return r_type;
	}

	/** Get the COFF symbol to which this relocation refers.
	 * @return the symbol
	 */
	public CoffSymbol getCoffSymbol() {
		return symbol;
	}

	/** Get the addend.
	 * @return the addend
	 */
	public int getAddend() {
		return r_offset;
	}

	public void dump(PrintWriter out) throws IOException {
		out.printf("%04x %04x %02x %s\n", r_vaddr, r_offset,
			r_type, symbol.getName());
	}
}
