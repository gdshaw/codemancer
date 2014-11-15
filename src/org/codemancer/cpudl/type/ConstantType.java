// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.List;
import org.w3c.dom.Element;

import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.ShortBitString;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Constant;

/** A class to represent a fixed binary pattern. */
public class ConstantType extends Type {
	/** The binary pattern matched by this type. */
	private final BitString content;

	/** Construct constant type from XML.
	 * @param element this type as an XML element
	 */
	public ConstantType(Element element) throws CpudlParseException {
		BitString content = new ShortBitString();
		String contentString = element.getTextContent();
		for (int i = 0; i != contentString.length(); ++i) {
			char ch = contentString.charAt(i);
			if ((ch == '0') || (ch == '1')) {
				content = content.concat(ch - '0');
			} else if (!Character.isWhitespace(ch)) {
				throw new CpudlParseException(element, "bit sequence expected");
			}
		}
		this.content = content;
	}

	public final int getChunkCount() {
		return 1;
	}

	public final long getFixedWidth(int chunk) {
		if (chunk != 0) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return content.length();
	}

	public final int getFixedBit(int chunk, long index) {
		if (chunk != 0) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		if ((index < 0) || (index >= content.length())) {
			throw new IllegalArgumentException("invalid bit index");
		}
		return content.getBit(index);
	}

	public final boolean isVariableWidth(int chunk) {
		if (chunk != 0) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return false;
	}

	public final BitString encode(int chunk, Expression expr) {
		if (chunk != 0) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return content;
	}

	public final Expression decode(List<BitReader> readers) {
		if (readers.size() != 1) {
			throw new IllegalArgumentException("incorrect number of chunks");
		}
		BitString bits = readers.get(0).read(content.length());
		if (!bits.equals(content)) return null;
		// It would be possible to return a value more relevant to the content,
		// however there does not appear to be any need to do so currently.
		return new Constant(this, 0);
	}
}
