// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.ShortBitString;
import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.BitStringReader;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.type.FragmentType.MemberInfo;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Fragment;

/** A class for matching the machine code representation of a sequence of
 * instruction fragments.
 * A bit string matches a pattern if and only if it can be expressed
 * as the concatenation of a sequence of substrings, each of which matches
 * the corresponding element of the pattern in length and content.
 */
public class Pattern {
	private static class ChunkInfo {
		/** The name of the fragment member to which this chunk belongs. */
		public final String name;

		/** The type to which this chunk belongs. */
		public final Type type;

		/** The chunk number within the fragment type to which this chunk refers. */
		public final int index;

		/** The index of the assembly buffer allocated to this fragment. */
		public final int buffer;

		/** True if this is the final chunk of the fragment, otherwise false. */
		public final boolean isFinal;

		/** Construct chunk information.
		 * @param name the name of this chunk
		 * @param type the type of this chunk
		 * @param index the chunk number
		 * @param buffer the assembly buffer index
		 * @param isFinal true if this is the final chunk, otherwise false
		 */
		ChunkInfo(String name, Type type, int index, int buffer, boolean isFinal) {
			this.name = name;
			this.type = type;
			this.index = index;
			this.buffer = buffer;
			this.isFinal = isFinal;
		}
	}

	/** The chunks that are matched by this pattern, in the order they occur. */
	private final ArrayList<ChunkInfo> chunks = new ArrayList<ChunkInfo>();

	/** The fragments that are matched by this pattern, indexed by final
	 * fixed bit number.
	 * Indexing is on the final fixed bit number (rather than the first)
	 * in order to facilitate searching using std::map::lower_bound.
	 */
	private final TreeMap<Long, ChunkInfo> chunkIndex = new TreeMap<Long, ChunkInfo>();

	/** The fixed width of this pattern in bits. */
	private long fixedWidth;

	/** True if the width of this pattern is variable, otherwise false. */
	boolean variableWidth;

	/** Construct pattern from XML.
	 * @param el this pattern as an XML element
	 */
	public Pattern(Element element, SortedMap<String, MemberInfo> members) throws CpudlParseException {
		Node child = element.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element childElement = (Element)child;
				String tagName = childElement.getTagName();
				if (tagName.equals("const")) {
					Type type = new ConstantType(childElement);
					MemberInfo member = new MemberInfo(type, 0);
					add(childElement, null, member);
				} else if (tagName.equals("ref")) {
					String name = childElement.getAttribute("name");
					if (name == null) {
						throw new CpudlParseException(childElement, "missing name attribute in ref element");
					}
					MemberInfo member = members.get(name);
					if (member == null) {
						throw new CpudlParseException(childElement, "undefined fragment member '" + name + "'");
					}
					add(childElement, name, member);
				}
			}
			child = child.getNextSibling();
		}
	}

	private void add(Element element, String name, MemberInfo member) throws CpudlParseException {
		if (member.chunk >= member.type.getChunkCount()) {
			throw new CpudlParseException(element, "too many pattern references to fragment member");
		}
		boolean isFinal = (member.chunk + 1 == member.type.getChunkCount());
		ChunkInfo chunkInfo = new ChunkInfo(name, member.type, member.chunk, member.buffer, isFinal);
		chunks.add(chunkInfo);
		if (!variableWidth) {
			long fragFixedWidth = member.type.getFixedWidth(member.chunk);
			if (fragFixedWidth > 0) {
				fixedWidth += fragFixedWidth;
				chunkIndex.put(fixedWidth - 1, chunkInfo);
			}
			if (member.type.isVariableWidth(member.chunk)) {
				variableWidth = true;
			}
		}
		member.chunk += 1;
	}

	/** Return the width of the initial fixed-width region of this pattern.
	 * @return the width, in bits
	 */
	public long getFixedWidth() {
		return fixedWidth;
	}

	/** Get possible values for the bit at a given index of this pattern.
	 * It is an error if index >= fixed_width().
	 * @param index the bit index
	 * @return the value the bit must have, or -1 if either is possible
	 */
	public int getFixedBit(long index) {
		Map.Entry<Long, ChunkInfo> chunkEntry = chunkIndex.ceilingEntry(index);
		if ((index < 0) || (chunkEntry == null)) {
			throw new IllegalArgumentException("invalid bit index");
		}
		ChunkInfo chunkInfo = chunkEntry.getValue();
		long offsetIntoChunk = index - (chunkEntry.getKey() + 1 - chunkInfo.type.getFixedWidth(chunkInfo.index));
		return chunkInfo.type.getFixedBit(chunkInfo.index, offsetIntoChunk);
	}

	/** Determine whether this pattern has a variable-width region.
	 * @return true if the pattern has a variable-width region, otherwise false
	 */
	boolean isVariableWidth() {
		return variableWidth;
	}

	/** Attempt to decode a bit sequence to match this pattern.
	 * The number of bits passed in may be greater than the number needed.
	 * If decoding is successful then the bit reader is left positioned at the end of
	 * the decoded chunk. If decoding fails then its position is undefined.
	 * @param reader a source of bits
	 * @param buffers an array of assembly buffers
	 * @param frag a fragment for recording the result
	 * @return true if the sequence matched, otherwise false
	 */
	public boolean decode(BitReader reader, ArrayList<ArrayList<BitReader>> buffers, Fragment frag) {
		for (ChunkInfo chunkInfo: chunks) {
			if (chunkInfo.isFinal) {
				buffers.get(chunkInfo.buffer).add(reader);
				Expression expr = chunkInfo.type.decode(buffers.get(chunkInfo.buffer));
				if (expr == null) return false;
				if (chunkInfo.name != null) {
					frag.put(chunkInfo.name, expr);
				}
			} else {
				long count = chunkInfo.type.getFixedWidth(chunkInfo.index);
				buffers.get(chunkInfo.buffer).add(new BitStringReader(reader.read(count)));
			}
			if (chunkInfo.buffer == 0) {
				buffers.get(0).clear();
			}
		}
		return true;
	}
}
