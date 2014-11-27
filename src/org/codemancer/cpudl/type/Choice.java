// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.ShortBitString;
import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.expr.Expression;

/** A class to represent a choice from a collection of possible types. */
public class Choice extends Type {
	/** A class for associating a type with a priority. */
	public static class TypeInfo {
		/** The type. */
		public final Type type;

		/** The priority associated with the type. */
		public final int priority;

		/** Construct type information structure.
		 * @param type the type
		 * @param priority the priority
		 */
		TypeInfo(Type type, int priority) {
			this.type = type;
			this.priority = priority;
		}
	}

	/** A structure for recording aggregate information about the patterns
	 * matched by the allowed types. */
	protected static class PatternInfo {
		/** The width of the initial fixed-width region for this chunk. */
		public long fixedWidth;

		/** Bits which could be zero. */
		public BitString couldBeZero;

		/** Bits which could be one. */
		public BitString couldBeOne;

		/** True if the pattern could be variable length.
		 * This can only be true for the pattern corresponding to the final chunk.
		 */
		public boolean variableWidth;

		/** Initialise pattern information structure.
		 * @param fixedWidth the width of the initial fixed-width region
		 * @param variableWidth true if the chunk could be variable width
		 */
		PatternInfo(long fixedWidth, boolean variableWidth) {
			this.fixedWidth = fixedWidth;
			this.couldBeZero = new ShortBitString(0, fixedWidth);
			this.couldBeOne = new ShortBitString(0, fixedWidth);
			this.variableWidth = variableWidth;
		}
	}

	/** The number of chunks for this collection of types. */
	private int chunkCount = 0;

	/** The number of pieces for this collection of types. */
	private int pieceCount = 0;

	/** The types from which the choice can be made. */
	private final ArrayList<Type> types = new ArrayList<Type>();

	/** Aggregate information about the patterns matched by the allowed types. */
	private final ArrayList<PatternInfo> patterns = new ArrayList<PatternInfo>();

	/** A decoder for this collection of types. */
	Decoder decoder;

	/** Construct choice from list of type information structures.
	 * @param types the possible types
	 */
	public Choice(List<TypeInfo> types) {
		for (TypeInfo info: types) {
			add(info);
		}
		decoder = new Decoder(types, patterns.size());
	}

	/** Add a type to this choice.
	 * @param info the type to be added
	 */
	private void add(TypeInfo info) {
		if (types.isEmpty()) {
			chunkCount = info.type.getChunkCount();
			pieceCount = info.type.getPieceCount();
			patterns.ensureCapacity(chunkCount);
			for (int i = 0; i != chunkCount; ++i) {
				patterns.add(new PatternInfo(info.type.getFixedWidth(i), info.type.isVariableWidth(i)));
			}
		} else {
			if (info.type.getChunkCount() != chunkCount) {
				throw new IllegalArgumentException("chunk count mismatch");
			}
			if (info.type.getPieceCount() != pieceCount) {
				throw new IllegalArgumentException("piece count mismatch");
			}
		}
		types.add(info.type);

		for (int i = 0; i != chunkCount; ++i) {
			long fixedWidth = info.type.getFixedWidth(i);
			if (fixedWidth != patterns.get(i).fixedWidth) {
				if (i + 1 != chunkCount) {
					throw new IllegalArgumentException("fixed width mismatch");
				} else {
					if (fixedWidth < patterns.get(i).fixedWidth) {
						patterns.get(i).fixedWidth = fixedWidth;
					}
					patterns.get(i).variableWidth = true;
				}
			}
			for (long j = 0; j != fixedWidth; ++j) {
				int fixedBit = info.type.getFixedBit(i, j);
				if (fixedBit != 1) {
					patterns.get(i).couldBeZero = patterns.get(i).couldBeZero.setBit(j, 1);
				}
				if (fixedBit != 0) {
					patterns.get(i).couldBeOne = patterns.get(i).couldBeOne.setBit(j, 1);
				}
			}
			patterns.get(i).variableWidth |= info.type.isVariableWidth(i);
		}
	}

	public final int getChunkCount() {
		return patterns.size();
	}

	public final long getFixedWidth(int chunk) {
		if (chunk >= patterns.size()) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return patterns.get(chunk).fixedWidth;
	}

	public final int getFixedBit(int chunk, long index) {
		if (chunk >= patterns.size()) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		boolean couldBeZero = (patterns.get(chunk).couldBeZero.getBit(index) != 0);
		boolean couldBeOne = (patterns.get(chunk).couldBeOne.getBit(index) != 0);
		if (couldBeZero && !couldBeOne) {
			return 0;
		} else if (couldBeOne && !couldBeZero) {
			return 1;
		} else {
			return -1;
		}
	}

	public final boolean isVariableWidth(int chunk) {
		if (chunk >= patterns.size()) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return patterns.get(chunk).variableWidth;
	}

	public Expression decode(List<BitReader> readers) {
		return decoder.decode(readers);
	}

	public int getPieceCount() {
		return pieceCount;
	}

	public String unparse(int piece, Expression expr) {
		return expr.getType().unparse(piece, expr);
	}

	/** Add children of element to list of choices.
	 * @param ctx the context of the element
	 * @param element the parent of the child elements to be added
	 * @param priority the priority
	 * @param types the list of types to be added to
	 */
	private static void addElement(Context ctx, Element element, int priority, List<TypeInfo> types)
		throws CpudlParseException {

		Node child = element.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element childElement = (Element)child;
				String tagName = childElement.getTagName();
				if (tagName.equals("priority")) {
					String childPriorityStr = childElement.getAttribute("level");
					if (childPriorityStr.isEmpty()) {
						throw new CpudlParseException(childElement,
							"missing level attribute in <priority> element");
					}
					int childPriority = Integer.parseInt(childPriorityStr);
					addElement(ctx, childElement, priority + childPriority, types);
				} else {
					Type type = ctx.makeType(child);
					if (type != null) {
						types.add(new TypeInfo(type, priority));
					}
				}
			}
			child = child.getNextSibling();
		}
	}

	/** Make choice of types from children of XML element.
	 * If only one type is specified then it may be returned directly
	 * (as opposed to being wrapped within a Choice object).
	 * @param ctx the context of this element
	 * @param node the parent of the elements to be interpreted as types
	 * @return the type or choice of types
	 */
	public static Type make(Context ctx, Element element) throws CpudlParseException {
		List<TypeInfo> types = new ArrayList<TypeInfo>();
		addElement(ctx, element, 0, types);
		if (types.size() == 0) {
			throw new CpudlParseException(element, "type expected");
		} else if (types.size() == 1) {
			return types.get(0).type;
		} else {
			return new Choice(types);
		}
	}
}
