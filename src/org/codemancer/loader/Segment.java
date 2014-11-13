// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

/** An interface for inspecting a segment from an object file. */
public interface Segment {
	/** Get the name of this segment.
	 * @return the name
	 */
	public String getName() throws IOException;

	/** Get the start address of this segment.
	 * @return the address, or 0 if unknown or not applicable
	 */
	public long getAddress();

	/** Get the size of this segment.
	 * @return the size in bytes, or 0 if unknown or not applicable
	 */
	public long getSize();

	/** Determine whether this section is mapped into the address space.
	 * Some sections exist only as part of the object file and do not have
	 * a presence in memory. This method returns false if and only if that
	 * is the case.
	 * The question of whether a section is mapped is first and foremost
	 * a matter for the file format specification, if there is one.
	 * Otherwise, there is a presumption against mapping unless this is
	 * technically necessary for the program to run or is known to be
	 * custom and practice.
	 * Note that this may result in some information being mapped by some
	 * object file formats and not by others. Also, it is immaterial whether
	 * or how the section is initialised (so this method should return true
	 * for BSS sections).
	 * @return true if this section is mapped, otherwise false
	 */
	public boolean isMapped();

	/** Get the content of this section as a ByteBuffer.
	 * @return the content
	 */
	public ByteBuffer getContent();

	/** Dump the metadata for this symbol to a stream in human-readable form.
	 * @param out the stream to be written to
	 */
	public void dump(PrintWriter out) throws IOException;
}
