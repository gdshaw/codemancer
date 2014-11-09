// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.aof;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

/** A class to represent a chunk within an AOF file. */
public class AofChunk {
	/** Construct new chunk.
	 * If the chunk type is recognised then the object returned will be
	 * of an appropriate subclass, otherwise it will be of this class.
	 *
	 * On entry the supplied ByteBuffer should be positioned at the start
	 * of the 16-byte entry in the chunk file header that corresponds to
	 * the relevant chunk. On exit it will be positioned at the end of
	 * that 16-byte entry. A reference to the buffer is kept, but other
	 * members of this class do not alter or depend on its position.
	 *
	 * @param buffer a ByteBuffer giving access to the underlying AOF file
	 * @param aof the AOF file to which the chunk belongs
	 * @return the newly constructed chunk
	 */
	public static AofChunk makeChunk(ByteBuffer buffer, AofFile aof) throws IOException {
		// Extract the chunk ID, then restore the buffer position to
		// the start of the 16-byte entry in the chunk file header.
		int position = buffer.position();
		byte[] chunkIdBytes = new byte[8];
		buffer.get(chunkIdBytes, 0, 8);
		String chunkId = new String(chunkIdBytes, 0, 8, "ISO-8859-1");
		buffer.position(position);

		// Construct a chunk of the appropriate type according to the chunk ID.
		if (chunkId.equals("OBJ_HEAD")) {
			return new AofHeaderChunk(buffer, aof);
		} else if (chunkId.equals("OBJ_IDFN")) {
			return new AofIdentificationChunk(buffer, aof);
		} else {
			return new AofChunk(buffer, aof);
		}
	}

	/** A ByteBuffer giving access to the underlying AOF file. */
	protected final ByteBuffer buffer;

	/** An object representing the decoded AOF file as a whole. */
	protected final AofFile aof;

	/** The type of this chunk. */
	protected final String chunkId;

	/** The file byte offset corresponding to the start of this chunk. */
	protected final int fileOffset;

	/** The size of this chunk, in bytes. */
	protected final int size;

	/** Construct new chunk.
	 * On entry the ByteBuffer must be positioned at the start of the
	 * relevant chunk header. A defensive copy is made immediately,
	 * and from that point onward the AofChunk instance neither
	 * modifies nor depends on the original ByteBuffer.
	 * @param buffer a ByteBuffer giving access to the underlying AOF file
	 * @param aof the AOF file to which the chunk belongs
	 */
	public AofChunk(ByteBuffer parentBuffer, AofFile aof) throws IOException {
		this.buffer = parentBuffer.duplicate();
		this.buffer.order(parentBuffer.order());
		this.aof = aof;

		byte[] chunkIdBytes = new byte[8];
		buffer.get(chunkIdBytes, 0, 8);
		chunkId = new String(chunkIdBytes, 0, 8, "ISO-8859-1");
		fileOffset = buffer.getInt();
		size = buffer.getInt();
	}

	/** Get the AOF file to which this chunk belongs.
	 * @return the AOF file.
	 */
	public final AofFile getAofFile() {
		return aof;
	}

	/** Get the chunk ID for this chunk.
	 * @return the chunk ID
	 */
	public final String getChunkId() {
		return chunkId;
	}

	/** Get the file offset for this chunk.
	 * @return the file offset, in bytes
	 */
	public final int getFileOffset() {
		return fileOffset;
	}

	/** Get the size of this chunk.
	 * @return the size, in bytes
	 */
	public final int getSize() {
		return size;
	}

	/** Dump the chunk header to a stream in human-readable form.
	 * @param out the stream to be written to
	 */
	public void dump(PrintWriter out) throws IOException {
		out.printf("chunkID: %s\n", chunkId);
		out.printf("fileOffset: %08x\n", fileOffset);
		out.printf("size: %08x\n", size);
	}
}
