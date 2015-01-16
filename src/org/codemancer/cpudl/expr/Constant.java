// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import org.w3c.dom.Element;

import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent a known integer constant. */
public class Constant extends Expression {
	/** The value of this integer constant. */
	public final long value;

	/** Construct integer constant from given value.
	 * @param type the required type of this constant
	 * @param value the required value
	 */
	public Constant(Type type, long value) {
		super(type);
		this.value = value;
	}

	/** Get value.
	 * @return the value of this constant
	 */
	public long getValue() {
		return value;
	}

	public String unparse(Style style) {
		int base = style.getInteger("base", 10);
		int width = style.getInteger("width", 0);
		String prefix = style.get("prefix", "");
		String suffix = style.get("suffix", "");
		if (width < 1) width = 1;
		if (width > 64) width = 64;

		boolean negative = (value < 0);
		String sign = (negative) ? "-" : "";

		char[] digits = new char[64];
		int i = digits.length;
		long v = (negative) ? -value : value;
		while ((v != 0) || (width != 0)) {
			int dv = (int)(v % base);
			char digit = (dv < 10) ? (char)('0' + dv) : (char)('A' + dv - 10);
			digits[--i] = digit;
			v = v / base;
			if (width != 0) --width;
		}
		return sign + prefix + new String(digits, i, digits.length - i) + suffix;
	}

	public void accumulate(Accumulator acc, long multiplier) {
		acc.accumulate(null, multiplier * value);
	}

	/** Make constant from XML element.
	 * @param ctx the context of this expression
	 * @param el the constant as XML
	 * @return a corresponding expression
	 */
	public static Constant make(Context ctx, Element el) {
		return new Constant(null, Long.decode(el.getTextContent()));
	}
}
