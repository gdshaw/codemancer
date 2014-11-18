// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import org.w3c.dom.Element;

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

	public String unparse() {
		return new Long(value).toString();
	}

	public void accumulate(Accumulator acc, long multiplier) {
		acc.accumulate(null, multiplier * value);
	}

	/** Make constant from XML element.
	 * @param el the constant as XML
	 * @return a corresponding expression
	 */
	public static Constant make(Element el) {
		return new Constant(null, Long.decode(el.getTextContent()));
	}
}
