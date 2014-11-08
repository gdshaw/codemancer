package org.codemancer.loader.elf;

import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.codemancer.loader.InvalidFileFormat;

/** A class to represent a section within an ELF file containing relocations.
 * These can be with explicit addends (SHT_RELA) or without (SHT_REL).
 */
public class ElfRelocationSection extends ElfSection {
	/** The relocation entries in this section. */
	private final List<ElfRelocation> elfRelocations =
		new ArrayList<ElfRelocation>();

	/** Construct new relocation section.
	 * On entry the ByteBuffer must be positioned at the start of the
	 * relevant section header. A defensive copy is made immediately,
	 * and from that point onward the class instance neither modifies
	 * nor depends on the original ByteBuffer.
	 * @param parentBuffer a ByteBuffer giving access to the underlying
	 *  ELF file
	 * @param elf the ELF file to which the section belongs
	 * @param hasAddend true if the relocations have explicit addends,
	 *  otherwise false
	 */
	public ElfRelocationSection(ByteBuffer parentBuffer, ElfFile elf,
		boolean hasAddend) throws IOException {

		super(parentBuffer, elf);
		if (!(getLinkedSection() instanceof ElfSymbolTableSection)) {
			throw new InvalidFileFormat("linked section is not a symbol table");
		}

		long size = getSize();
		long entsize = getEntrySize();
		long offset = getOffset();
		for (long i = 0; i < size; i += entsize) {
			buffer.position((int)(offset + i));
			ElfRelocation rel = new ElfRelocation(buffer, this, hasAddend);
			elfRelocations.add(rel);
		}
	}

	/** Get the number of relocations.
	 * @return the number of relocations
	 */
	public int getElfRelocationCount() {
		return elfRelocations.size();
	}

        /** Get relocation at given index.
         * @param relndx the index of the required relocation
         * @return the relocation, or null if not found
         */
        public ElfRelocation getElfRelocation(int symndx) {
                return elfRelocations.get(symndx);
        }

	public void dump(PrintWriter out) throws IOException {
		super.dump(out);
		out.println();
		for (ElfRelocation elfRelocation: elfRelocations) {
			elfRelocation.dump(out);
		}
        }
}
