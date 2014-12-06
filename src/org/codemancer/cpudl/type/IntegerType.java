// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.List;
import org.w3c.dom.Element;

import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.FeatureSet;
import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.CpudlParseException;
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

	/** True for big-endian encoding, false for little-endian encoding. */
	private final boolean bigEndian;

	/** The style to be used for this integer type. */
	private final Style style;

	/** Construct integer type from attributes.
	 * @param size the required size, in bits
	 * @param sign the required encoding method
	 * @param bigEndian true for big-endian, false for little-endian
	 * @param style the required style
	 */
	public IntegerType(int size, int encoding, boolean bigEndian, Style style) {
		this.size = size;
		this.encoding = encoding;
		this.bigEndian = bigEndian;
		this.style = style;
	}

	/** Construct integer type from XML.
	 * @param ctx the context of this type
	 * @param element this type as an XML element
	 */
	public IntegerType(Context ctx, Element element) throws CpudlParseException {
		// Parse size attribute.
		String sizeString = element.getAttribute("size");
		if (sizeString.length() == 0) {
			throw new CpudlParseException(element, "missing integer size attribute");
		}
		try {
			this.size = Integer.decode(element.getAttribute("size"));
		} catch (NumberFormatException ex) {
			throw new CpudlParseException(element, "invalid integer size attribute");
		}
		if ((this.size < 0) || (this.size > 64)) {
			throw new CpudlParseException(element, "integer size attribute out of range");
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
			throw new CpudlParseException(element, "invalid integer encoding attribute");
		}

		this.bigEndian = ctx.getArchitecture().isBigEndian();
		this.style = ctx.getStylesheet().getStyle(element.getAttribute("class"));
	}

	public final int getSize() {
		return size;
	}

	public final int getEncoding() {
		return encoding;
	}

	public final boolean isBigEndian() {
		return bigEndian;
	}

	public final Style getStyle() {
		return style;
	}

	public final int getChunkCount() {
		return 1;
	}

	public final long getFixedWidth(int chunk) {
		if (chunk != 0) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return size;
	}

	public final int getFixedBit(int chunk, long index) {
		if (chunk != 0) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		if ((index < 0) || (index >= size)) {
			throw new IllegalArgumentException("invalid bit index");
		}
		return -1;
	}

	public final boolean isVariableWidth(int chunk) {
		if (chunk != 0) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return false;
	}

	public final Expression decode(List<BitReader> readers, FeatureSet features) {
		if (readers.size() != 1) {
			throw new IllegalArgumentException("incorrect number of chunks");
		}
		BitString bits = readers.get(0).read(size);
		if (bits.length() < size) {
			return null;
		}

		long value = bits.getBits(0, size, bigEndian);
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
		return expr.unparse(style);
	}
}
