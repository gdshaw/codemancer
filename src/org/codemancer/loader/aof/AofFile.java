// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.aof;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.codemancer.loader.InvalidFileFormat;

/** A class to represent the content of an AOF (ARM object format) file. */
public class AofFile {
	/** A ByteBuffer giving access to the underlying AOF file. */
	private final ByteBuffer buffer;

	/** The maximum number of chunks allowed in this AOF file. */
	private final int maxChunks;

	/** Construct object to represent AOF file.
	 * On entry the ByteBuffer must be positioned at the start of the file.
	 * Any byte order is permissible. On exit the position is unspecified,
	 * but the byte order will have been set to match the ELF file.
	 * @param buffer the content of the AOF file as a ByteBuffer
	 */
	public AofFile(ByteBuffer buffer) throws IOException {
		this.buffer = buffer;
		buffer.position(0);

		// Fetch the chunk file ID.
		byte[] id = new byte[4];
		buffer.get(id, 0, 4);

		// Validate the chunk file ID, setting the byte order accordingly.
		if ((id[0] == (byte)0xc5) && (id[1] == (byte)0xc6) &&
			(id[2] == (byte)0xcb) && (id[3] == (byte)0xc3)) {
			buffer.order(ByteOrder.LITTLE_ENDIAN);
		} else if ((id[0] == (byte)0xc3) && (id[1] == (byte)0xcb) &&
			(id[2] == (byte)0xc6) && (id[3] == (byte)0xc5)) {
			buffer.order(ByteOrder.BIG_ENDIAN);
		} else {
			throw new InvalidFileFormat("invalid ChunkFileId for AOF file");
		}

		// Fetch and validate the maximum number of chunks.
		maxChunks = buffer.getInt();
		if ((maxChunks < 0) || (maxChunks >= 0x10000)) {
			throw new InvalidFileFormat("unreasonable maxChunks field");
		}

		// Fetch and validate the actual number of chunks.
		int numChunks = buffer.getInt();
		if ((numChunks < 0) || (numChunks > maxChunks)) {
			throw new InvalidFileFormat("invalid numChunks field");
		}
	}

	/** Get the maximum number of chunks allowed in this AOF file.
	 * @return the maximum number of chunks
	 */
	public final int getMaxChunks() {
		return maxChunks;
	}

	public void dump(PrintWriter out) throws IOException {
		out.printf("maxChunks: %d\n", maxChunks);
	}
}
