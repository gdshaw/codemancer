// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.List;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.TreeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Fragment;

/** A class to represent a type of instruction fragment. */
public class FragmentType extends Type {
	protected static class MemberInfo {
		/** The type of this member. */
		public final Type type;

		/** The assembly buffer index for this member. */
		public final int buffer;

		/** The number of chunks that have been referenced from this member. */
		public int chunk;

		/** The number of pieces that have been referenced from this member. */
		public int piece;

		/** Construct member information.
		 * @param type the type of this member
		 * @param buffer the assembly buffer index for this subfragment
		 */
		MemberInfo(Type type, int buffer) {
			this.type = type;
			this.buffer = buffer;
			this.chunk = 0;
			this.piece = 0;
		}
	}

	/** The members of this fragment, indexed by name. */
	private final SortedMap<String, MemberInfo> members = new TreeMap<String, MemberInfo>();

	/** The patterns that are matched by this fragment, indexed by chunk number. */
	private final List<Pattern> patterns = new ArrayList<Pattern>();

	/** The phrases that are matched by this fragment, indexed by piece number. */
	private final List<Phrase> phrases = new ArrayList<Phrase>();

	/** The number of bitstring assembly buffers needed to decode this compound fragment. */
	private int bufferCount = 1;

	/** Construct fragment type from XML.
	 * @param element this fragment type as an XML element
	 */
	public FragmentType(Element element) throws CpudlParseException {
		Node child = element.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element childElement = (Element)child;
				String tagName = childElement.getTagName();
				if (tagName.equals("var")) {
					parseMember(childElement);
				} else if (tagName.equals("pattern")) {
					patterns.add(new Pattern(childElement, members));
				} else if (tagName.equals("phrase")) {
					phrases.add(new Phrase(childElement, members));
				}
			}
			child = child.getNextSibling();
		}
	}

	/** Parse member.
	 * @param element the member as an XML element
	 */
	private final void parseMember(Element element) throws CpudlParseException {
		String name = element.getAttribute("name");
		if (name.equals("")) {
			throw new CpudlParseException(element, "member name not specified");
		}
		Type type = Type.makeType(element);
		if (type == null) {
			throw new CpudlParseException(element, "member type not found");
		}
		members.put(name, new MemberInfo(type, bufferCount));
		bufferCount += 1;
	}

	public final int getChunkCount() {
		return patterns.size();
	}

	public final long getFixedWidth(int chunk) {
		if (chunk >= patterns.size()) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return patterns.get(chunk).getFixedWidth();
	}

	public final int getFixedBit(int chunk, long index) {
		if (chunk >= patterns.size()) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return patterns.get(chunk).getFixedBit(index);
	}

	public final boolean isVariableWidth(int chunk) {
		if (chunk >= patterns.size()) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return patterns.get(chunk).isVariableWidth();
	}

	public Expression decode(List<BitReader> readers) {
		ArrayList<ArrayList<BitReader>> buffers = new ArrayList<ArrayList<BitReader>>(bufferCount);
		for (int i = 0; i != bufferCount; ++i) {
			buffers.add(new ArrayList<BitReader>());
		}

		Fragment frag = new Fragment(this);
		for (int i = 0; i != patterns.size(); ++i) {
			if (!patterns.get(i).decode(readers.get(i), buffers, frag)) {
				return null;
			}
		}
		return frag;
	}

	public int getPieceCount() {
		return phrases.size();
	}

	public String unparse(int piece, Expression expr) {
		if ((piece < 0) || (piece >= phrases.size())) {
			throw new IllegalArgumentException("invalid piece number");
		}
		return phrases.get(piece).unparse(expr);
	}
}
