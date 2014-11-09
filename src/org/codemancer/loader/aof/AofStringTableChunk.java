// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.aof;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

/** A class to represent a string table chunk within an AOF file. */
public class AofStringTableChunk extends AofChunk {
	/** Construct new string table chunk.
	 * @param buffer a ByteBuffer giving access to the underlying AOF file
	 * @param aof the AOF file to which the chunk belongs
	 */
	public AofStringTableChunk(ByteBuffer buffer, AofFile aof) throws IOException {
		super(buffer, aof);
	}

	/** Get the string at a given byte index.
	 * @param index the byte index
	 * @return the corresponding string
	 */
	public String get(int index) {
		buffer.position(getFileOffset() + index);
		StringBuffer stringBuffer = new StringBuffer();
		byte ch = buffer.get();
		while (ch != 0) {
			stringBuffer.append((char)ch);
			ch = buffer.get();
		}
		return stringBuffer.toString();
	}
}
