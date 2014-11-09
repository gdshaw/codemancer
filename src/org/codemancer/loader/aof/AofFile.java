// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.aof;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import org.codemancer.loader.InvalidFileFormat;

/** A class to represent the content of an AOF (ARM object format) file. */
public class AofFile {
	/** The offset of the first chunk header. */
	private static final int chunkOffset = 12;

	/** The size of each chunk header. */
	private static final int chunkSize = 16;

	/** A ByteBuffer giving access to the underlying AOF file. */
	private final ByteBuffer buffer;

	/** The maximum number of chunks allowed in this AOF file. */
	private final int maxChunks;

	/** A directory of chunks in this AOF file, indexed by ID. */
	private final Map<String, List<Integer>> chunkDirectory =
		new HashMap<String, List<Integer>>();

	/** A table of chunks in this AOF file that have been parsed,
	 * indexed by position. */
	private final ArrayList<AofChunk> chunks;

	/** The header chunk. */
	private AofHeaderChunk headerChunk = null;

	/** The string table chunk. */
	private AofStringTableChunk stringTableChunk = null;

	/** The symbol table chunk. */
	private AofSymbolTableChunk symbolTableChunk = null;

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

		// Construct chunk directory.
		for (int i = 0; i != maxChunks; ++i) {
			buffer.position(chunkOffset + i * chunkSize);
			AofChunk chunk = new AofChunk(buffer, this);
			if (chunk.getFileOffset() != 0) {
				String chunkId = chunk.getChunkId();
				List<Integer> chunkList = chunkDirectory.get(chunkId);
				if (chunkList == null) {
					chunkList = new ArrayList<Integer>();
					chunkDirectory.put(chunkId, chunkList);
				}
				chunkList.add(i);
			}
		}

		// Initialise chunk table.
		chunks = new ArrayList<AofChunk>(
			Collections.nCopies(maxChunks, (AofChunk)null));
	}

	/** Get the maximum number of chunks allowed in this AOF file.
	 * @return the maximum number of chunks
	 */
	public final int getMaxChunks() {
		return maxChunks;
	}

	/** Get chunk by index.
	 * @param index the chunk index
	 * @return the corresponding chunk
	 */
	public final AofChunk getChunk(int index) throws IOException {
		// Validate the chunk index.
		if ((index < 0) || (index >= maxChunks)) {
			throw new IllegalArgumentException(
				"chunk index out of range");
		}

		// Attempt to fetch the chunk from the table.
		AofChunk chunk = chunks.get(index);

		// If the chunk was not in the table then parse it.
		if (chunk == null) {
			buffer.position(chunkOffset + index * chunkSize);
			chunk = AofChunk.makeChunk(buffer, this);
			chunks.set(index, chunk);
		}

		// Don't return unused chunks.
		if (chunk.getFileOffset() == 0) {
			chunk = null;
		}

		return chunk;
	}

	/** Find the unique chunk with a given chunk ID.
	 * @param chunkId the required chunk ID
	 * @param required true if this chunk is required to be present,
	 *  otherwise false
	 * @return the chunk, or null if not found and not required
	 * @throws InvalidFileFormat if the chunk is missing but required,
	 *  or if there is more than one chunk with the given ID
	 */
	public final AofChunk getUniqueChunk(String chunkId, boolean required)
		throws IOException {

		List<Integer> chunkList = chunkDirectory.get(chunkId);
		if ((chunkList == null) || (chunkList.size() == 0)) {
			throw new InvalidFileFormat("missing " + chunkId + " chunk");
		}
		if (chunkList.size() > 1) {
			throw new InvalidFileFormat("multiple " + chunkId + " chunks");
		}
		return getChunk(chunkList.get(0));
	}

	/** Get the header chunk.
	 * @return the header chunk, or null if not present
	 */
	public final AofHeaderChunk getHeaderChunk() throws IOException {
		if (headerChunk == null) {
			headerChunk = (AofHeaderChunk)getUniqueChunk("OBJ_HEAD", false);
		}
		return headerChunk;
	}

	/** Get the string table chunk.
	 * @return the string table chunk, or null if not present
	 */
	public final AofStringTableChunk getStringTableChunk() throws IOException {
		if (stringTableChunk == null) {
			stringTableChunk = (AofStringTableChunk)getUniqueChunk("OBJ_STRT", false);
		}
		return stringTableChunk;
	}

	/** Get the symbol table chunk.
	 * @return the symbol table chunk, or null if not present
	 */
	public final AofSymbolTableChunk getSymbolTableChunk() throws IOException {
		if (symbolTableChunk == null) {
			symbolTableChunk = (AofSymbolTableChunk)getUniqueChunk("OBJ_SYMT", false);
		}
		return symbolTableChunk;
	}

	public void dump(PrintWriter out) throws IOException {
		out.printf("maxChunks: %d\n", maxChunks);
	}
}
