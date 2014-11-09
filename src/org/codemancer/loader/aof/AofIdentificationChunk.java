// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.aof;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.codemancer.loader.InvalidFileFormat;

/** A class to represent a identification chunk within an AOF file. */
public class AofIdentificationChunk extends AofChunk {
	/** The identification string. */
	String identificationString;

	/** Construct new identification chunk.
	 * @param buffer a ByteBuffer giving access to the underlying AOF file
	 * @param aof the AOF file to which the chunk belongs
	 */
	public AofIdentificationChunk(ByteBuffer buffer, AofFile aof) throws IOException {
		super(buffer, aof);
		buffer.position(fileOffset);

		byte[] identificationBytes = new byte[size];
		buffer.get(identificationBytes, 0, size);
		int length = 0;
		while ((length < size) && (identificationBytes[length] != 0)) ++length;
		if (length == size) {
			throw new InvalidFileFormat("missing terminator in identification chunk");
		}
		identificationString = new String(identificationBytes, 0, length, "ISO-8859-1");
	}

	/** Get the identification string.
	 * The terminating NUL is stripped, but trailing linefeeds are not.
	 * @return the identification string
	 */
	public String getIdentificationString() {
		return identificationString;
	}

	/** Dump the chunk header to a stream in human-readable form.
	 * @param out the stream to be written to
	 */
	public void dump(PrintWriter out) throws IOException {
		String idStr = identificationString;
		while ((idStr.length() > 0) && (idStr.charAt(idStr.length() - 1) == '\n')) {
			idStr = idStr.substring(0, idStr.length() - 1);
		}

		super.dump(out);
		out.printf("identification: %s\n", idStr);
	}
}
