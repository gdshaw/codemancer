// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.aof;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.codemancer.loader.Allocator;
import org.codemancer.loader.InvalidFileFormat;

/** A class to represent an AOF area header. */
public class AofArea {
	/** An attribute to indicate that the area has an absolute base address. */
	public static final int ATTR_ABSOLUTE = 0x00000100;

	/** An attribute to indicate that the area contains code, as opposed to data. */
	public static final int ATTR_CODE = 0x00000200;

	/** An attribute to indicate that the area is a common block definition. */
	public static final int ATTR_COMMON_DEF = 0x00000400;

	/** An attribute to indicate that the area is a common block reference.
	 * Data for this area is not included in the area chunk. */
	public static final int ATTR_COMMON_REF = 0x00000800;

	/** An attribute to indicate that the area that is initialised with zeros.
	 * Data for this area is not included in the area chunk. */
	public static final int ATTR_BSS = 0x00001000;

	/** An attribute to indicate that the area is read only. */
	public static final int ATTR_RO = 0x00002000;

	/** An attribute to indicate that the area contains symbolic debugging tables. */
	public static final int ATTR_DEBUG = 0x00008000;

	/** A ByteBuffer giving access to the underlying AOF file. */
	private final ByteBuffer buffer;

	/** The header chunk to which this area header belongs. */
	AofHeaderChunk aofHeaderChunk;

	/** The name of this area. */
	private final String name;

	/** The attributes for this area. */
	private final int attributes;

	/** The size of this area. */
	private final int size;

	/** The number of relocations for this area. */
	private final int numRelocs;

	/** The base address for this area. */
	private final int baseAddress;

	/** The file offset for this area. */
	private final int fileOffset;

	/** The relocations for this area. */
	private ArrayList<AofRelocation> aofRelocations;

	/** Construct new area.
	 * @param buffer a ByteBuffer giving access to the underlying AOF file
	 * @param aofHeaderChunk the header chunk within which the area header is located
	 * @param fileAlloc an allocator for offsets into the AOF file
	 * @param memAlloc an allocator for addresses in memory
	 */
	public AofArea(ByteBuffer buffer, AofHeaderChunk aofHeaderChunk,
		Allocator fileAlloc, Allocator memAlloc) throws IOException {

		this.buffer = buffer;
		this.aofHeaderChunk = aofHeaderChunk;
		AofFile aof = aofHeaderChunk.getAofFile();

		// Parse area header.
		this.name = aof.getStringTableChunk().get(buffer.getInt());
		this.attributes = buffer.getInt();
		this.size = buffer.getInt();
		this.numRelocs = buffer.getInt();
		int fileBaseAddress = buffer.getInt();

		// Allocate address.
		if ((this.attributes & ATTR_ABSOLUTE) == 0) {
			if (fileBaseAddress != 0) {
				throw new InvalidFileFormat("non-absolute area has non-zero base address");
			}
			this.baseAddress = (int)memAlloc.allocate(this.size);
		} else {
			this.baseAddress = fileBaseAddress;
		}

		// Validate size.
		if ((size & 3) != 0) {
			throw new InvalidFileFormat("area size is not a multiple of 4 bytes");
		}

		// Determine location of area content in AOF file.
		this.fileOffset = (int)fileAlloc.allocate(0);
		if (hasContent()) {
			fileAlloc.allocate(this.size);
		}
		fileAlloc.allocate(this.numRelocs * 4);
	}

	/** Get the AOF file to which this area belongs. */
	public final AofFile getAofFile() {
		return aofHeaderChunk.getAofFile();
	}

	/** Get the AOF header chunk to which this area belongs. */
	public final AofHeaderChunk getAofHeaderChunk() {
		return aofHeaderChunk;
	}

	/** Get the name of this area.
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/** Get the size of this area.
	 * @return the size, in bytes
	 */
	public final long getSize() {
		return size;
	}

	/** Get the base address for this area.
	 * @return the base address, or zero if not applicable
	 */
	public final int getBaseAddress() {
		return baseAddress;
	}

	/** Get the attributes for this area.
	 * @return the attributes word
	 */
	public final int getAofAttributes() {
		return attributes;
	}

	/** Get a list of relocations in this AOF area.
	 * @return a list of relocations
	 */
	public List<AofRelocation> getAofRelocations() throws IOException {
		if (aofRelocations == null) {
			buffer.position(hasContent() ? (fileOffset + size) : fileOffset);
			aofRelocations = new ArrayList<AofRelocation>(numRelocs);
			for (int i = 0; i != numRelocs; ++i) {
				AofRelocation rel = new AofRelocation(buffer, this);
				aofRelocations.add(rel);
			}
		}
		return Collections.unmodifiableList(aofRelocations);
	}

	/** Test whether the initial content for this area is provided by the area chunk.
	 * @return true if content provided, otherwise false.
	 */
	public final boolean hasContent() {
		return (attributes & (ATTR_BSS | ATTR_COMMON_REF)) == 0;
	}

	public void dump(PrintWriter out) throws IOException {
		out.printf("%08x %08x %08x %08x %08x %s\n", fileOffset, baseAddress, size, numRelocs, attributes, name);
	}
}
