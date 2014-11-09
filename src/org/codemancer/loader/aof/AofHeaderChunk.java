// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// All rights reserved.

package org.codemancer.loader.aof;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.codemancer.loader.InvalidFileFormat;

/** A class to represent a header chunk within an AOF file. */
public class AofHeaderChunk extends AofChunk {
	/** The object file type, which should be equal to 0xc5e2d080. */
	private final int objectFileType;

	/** The AOF file format version, multiplied by 100. */
	private final int versionId;

	/** The number of areas. */
	private final int numAreas;

	/** The number of symbols. */
	private final int numSymbols;

	/** The area number containing the entry point, or zero if none. */
	private final int entryAreaIndex;

	/** The offset to the entry point, if there is one. */
	private final int entryAreaOffset;

	/** Construct new AOF header chunk.
	 * On entry the supplied ByteBuffer should be positioned at the start
	 * of the 16-byte entry in the chunk file header that corresponds to
	 * the relevant chunk. On exit it will be positioned at the end of
	 * that 16-byte entry.  A reference to the buffer is kept, but other
	 * members of this class do not alter or depend on its position.
	 *
	 * @param buffer a ByteBuffer giving access to the underlying AOF file
	 * @param aof the AOF file to which the chunk belongs
	 */
	public AofHeaderChunk(ByteBuffer buffer, AofFile aof) throws IOException {
		super(buffer, aof);
		buffer.position(fileOffset);

		// Read and validate object file type.
		objectFileType = buffer.getInt();
		if (objectFileType != 0xC5E2D080) {
			throw new InvalidFileFormat(
				"invalid object file type for AOF file");
		}

		// Read remainder of fixed part.
		versionId = buffer.getInt();
		numAreas = buffer.getInt();
		numSymbols = buffer.getInt();
		entryAreaIndex = buffer.getInt();
		entryAreaOffset = buffer.getInt();
	}

	/** Get the AOF version ID.
	 * @return the version, multiplied by 100
	 */
	public final int getVersionId() {
		return versionId;
	}

	/** Get the number of areas in this AOF file.
	 * @return the number of areas
	 */
	public final int getAreaCount() {
		return numAreas;
	}

	/** Get the number of symbols in this AOF file.
	 * @return the number of symbols
	 */
	public final int getSymbolCount() {
		return numSymbols;
	}

	/** Dump the chunk header to a stream in human-readable form.
	 * @param out the stream to be written to
	 */
	public void dump(PrintWriter out) throws IOException {
		super.dump(out);
		out.printf("objectFileType: %08x\n", objectFileType);
		out.printf("versionId: %d\n", versionId);
		out.printf("numAreas: %d\n", numAreas);
		out.printf("numSymbols: %d\n", numSymbols);
	}
}
