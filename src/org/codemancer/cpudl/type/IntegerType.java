// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.List;
import org.w3c.dom.Element;

import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Constant;

/** A class to represent a CPUDL integer type. */
public class IntegerType extends Type {
	/** A constant to indicate that this integer type is unsigned. */
	public static final int UNSIGNED = 0;

	/** A constant to indicate that this integer type uses twos-complement encoding. */
	public static final int TWOS_COMPLEMENT = 1;

	/** The size of this integer type, in bits. */
	private final int size;

	/** The encoding method used to represent this integer type. */
	private final int encoding;

	/** Construct integer type from attributes.
	 * @param size the required size, in bits
	 * @param sign the required encoding method
	 */
	public IntegerType(int size, int encoding) {
		this.size = size;
		this.encoding = encoding;
	}

	/** Construct integer type from XML.
	 * @param element this type as an XML element
	 */
	public IntegerType(Element element) {
		// Parse size attribute.
		String sizeString = element.getAttribute("size");
		if (sizeString.length() == 0) {
			throw new IllegalArgumentException("missing integer size attribute");
		}
		try {
			this.size = Integer.decode(element.getAttribute("size"));
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("invalid integer size attribute");
		}
		if ((this.size < 0) || (this.size > 64)) {
			throw new IllegalArgumentException("integer size attribute out of range");
		}

		// Parse encoding attribute.
		String encodingString = element.getAttribute("encoding");
		if (encodingString.length() == 0) {
			this.encoding = UNSIGNED;
		} else if (encodingString.equals("u")) {
			this.encoding = UNSIGNED;
		} else if (encodingString.equals("2c")) {
			this.encoding = TWOS_COMPLEMENT;
		} else {
			throw new IllegalArgumentException("invalid integer encoding attribute");
		}
	}

	public final int getChunkCount() {
		return 1;
	}

	public final Expression decode(List<BitReader> readers) {
		if (readers.size() != 1) {
			throw new IllegalArgumentException("incorrect number of chunks");
		}
		BitString bits = readers.get(0).read(size);
		long value = bits.getBits(0, size);
		switch (encoding) {
		case UNSIGNED:
			break;
		case TWOS_COMPLEMENT:
			if ((size < 64) && (((value >> (size - 1)) & 1) != 0)) {
				value -= (1L << size);
			}
		}
		return new Constant(this, value);
	}

	public final int getPieceCount() {
		return 1;
	}

	public String unparse(int piece, Expression expr) {
		if (piece != 0) {
			throw new IllegalArgumentException("invalid piece number");
		}
		return expr.unparse();
	}
}
