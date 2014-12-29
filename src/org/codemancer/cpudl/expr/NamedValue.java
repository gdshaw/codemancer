// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent a named value.
 * Named values are used to describe the effect of an instruction in the context
 * in which it occurs. They should not appear in instruction descriptions.
 */
public class NamedValue extends Expression {
	/** The name of this value. */
	private final String name;

	/** Construct named value.
	 * @param type the type of this value
	 * @param name the name of this value
	 */
	public NamedValue(Type type, String name) {
		super(type);
		this.name = name;
	}

	/** Get name.
	 * @return the name of this value
	 */
	public String getName() {
		return name;
	}

	public String unparse(Style style) {
		return name;
	}
}
