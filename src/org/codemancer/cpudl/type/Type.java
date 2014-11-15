// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.expr.Expression;

/** A class to represent a generic CPUDL data type. */
public abstract class Type {
	/** Make type from XML node.
	 * @param node the node to be interpreted as a type
	 * @return the corresponding type, or null if node does not contain a type
	 */
	public static Type makeType(Node node) throws CpudlParseException {
		if (!(node instanceof Element)) {
			return null;
		}
		Element element = (Element)node;
		String tagName = element.getTagName();
		if (tagName.equals("const")) {
			return new ConstantType(element);
		} else if (tagName.equals("literal")) {
			return new LiteralType(element);
		} else if (tagName.equals("integer")) {
			return new IntegerType(element);
		} else if (tagName.equals("fragment")) {
			return new FragmentType(element);
		} else if (tagName.equals("choice")) {
			return makeChoice(element);
		} else {
			return null;
		}
	}

	/** Make choice of types from children of XML element.
	 * If only one type is specified then it may be returned directly
	 * (as opposed to being wrapped within a Choice object).
	 * @param element the parent of the elements to be interpreted as types
	 * @return the type or choice of types
	 */
	public static Type makeChoice(Element element) throws CpudlParseException {
		List<Type> types = new ArrayList<Type>();
		Node child = element.getFirstChild();
		while (child != null) {
			Type type = makeType(child);
			if (type != null) {
				types.add(type);
			}
			child = child.getNextSibling();
		}
		if (types.size() == 0) {
			throw new CpudlParseException(element, "type expected");
		} else if (types.size() == 1) {
			return types.get(0);
		} else {
			return new Choice(types);
		}
	}

	/** Get the number of chunks of machine code matched by this type.
	 * If this function is not overridden, the number of chunks defaults to 0.
	 * @return the number of chunks
	 */
	public int getChunkCount() {
		return 0;
	}

	/** Return the width of the initial fixed-width region for a given chunk.
	 * It is an error if chunk < 0 or chunk >= getChunkCount().
	 * @param chunk the chunk number
	 * @return the width, in bits
	 */
	public long getFixedWidth(int chunk) {
		throw new IllegalArgumentException("invalid chunk number");
	}

	/** Get possible values for the bit at a given index of a given chunk.
	 * It is an error if chunk < 0, chunk >= getChunkCount() or index >= getFixedWidth(chunk).
	 * @param chunk the chunk number
	 * @param index the bit index
	 * @return the value the bit must have, or -1 if either is possible
	 */
	public int getFixedBit(int chunk, long index) {
		throw new IllegalArgumentException("invalid chunk number");
	}

	/** Determine whether a given chunk has a variable-width region
	 * It is an error if chunk < 0 or chunk >= getChunkCount().
	 * @param chunk the chunk number
	 * @return true if the chunk has a variable-width region, otherwise false
	 */
	public boolean isVariableWidth(int chunk) {
		throw new IllegalArgumentException("invalid chunk number");
	}

	/** Attempt to decode a collection of bit sequences as an instance of this type.
	 * If decoding is successful then the bit readers are left positioned at the end
	 * of the respective chunks. If decoding fails then their positions are unspecified.
	 * @param readers sources of bits, one for each chunk
	 * @return an expression corresponding to the bit sequences, or null if they did not match
	 */
	public Expression decode(List<BitReader> readers) {
		return null;
	}

	/** Get the number of pieces of assembly language matched by this type.
	 * If this function is not overridden, the number of pieces defaults to 0.
	 * @return the number of pieces
	 */
	public int getPieceCount() {
		return 0;
	}

	/** Construct a piece of assembly language to match this fragment.
	 * It is an error if piece < 0 or piece >= getPieceCount().
	 * @param piece the piece number
	 * @param expr the expression to be unparsed
	 * @return the corresponding string
	 */
	public String unparse(int piece, Expression expr) {
		throw new IllegalArgumentException("invalid piece number");
	}
}
