// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader;

import java.util.Map;
import java.util.NavigableMap;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.codemancer.loader.ObjectFile;
import org.codemancer.loader.Segment;

/** A class for reading the content of an object file. */
public class ObjectFileReader {
	/** The object file to be read from. */
	private ObjectFile obj;

	/** The address map for the object file. */
	private NavigableMap<Long, Segment> map;

	/** The current segment. */
	private Segment segment = null;

	/** The content of the current segment. */
	private ByteBuffer content = null;

	/** The current address. */
	private long curAddr = 0;

	/** The lowest address that is part of the current segment. */
	private long minAddr = 0;

	/** The highest address that is part of the current segment. */
	private long maxAddr = 0;

	/** Construct object file reader.
	 * @param obj the object file to be read from
	 */
	public ObjectFileReader(ObjectFile obj) throws IOException {
		this.obj = obj;
		this.map = obj.getAddressMap();
	}

	/** Read byte.
	 * The current address is incremented after the byte has been read.
	 * @return the byte read from the current address
	 */
	public final byte get() {
		if (segment == null) {
			Map.Entry<Long, Segment> entry = map.floorEntry(curAddr);
			if (entry != null) {
				segment = entry.getValue();
				content = segment.getContent();
				minAddr = segment.getAddress();
				maxAddr = minAddr + segment.getSize() - 1;
				if ((curAddr < minAddr) || (curAddr > maxAddr)) {
					segment = null;
				}
			}
			if (segment == null) {
				throw new IndexOutOfBoundsException(String.format(
					"address %08X not mapped to a segment", curAddr));
			}
		}

		byte b = content.get((int)(curAddr - minAddr));
		if (curAddr == maxAddr) {
			segment = null;
		}
		curAddr += 1;
		return b;
	}

	/** Seek to a given address.
	 * @param addr the address to which to seek
	 */
	public final void seek(long addr) {
		curAddr = addr;
		segment = null;
	}

	/** Get the current address.
	 * @return the current address
	 */
	public final long tell() {
		return curAddr;
	}

	/** Test whether a given address maps to a segment.
	 * @param addr the address to be tested
	 * @return true if the address is mapped, otherwise false
	 */
	public final boolean isMapped(long addr) {
		Map.Entry<Long, Segment> entry = map.floorEntry(addr);
		if (entry == null) {
			return false;
		}
		Segment segment = entry.getValue();
		long minAddr = segment.getAddress();
		long maxAddr = minAddr + segment.getSize() - 1;
		return ((addr >= minAddr) && (addr <= maxAddr));
	}
}
