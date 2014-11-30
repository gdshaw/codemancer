// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.type.PrefixType;

/** An expression class to represent an instruction prefix. */
public class Prefix extends Expression {
	/** Construct instruction prefix.
	 * @param type the type of this prefix
	 */
	public Prefix(PrefixType type) {
		super(type);
	}

	public String getFeatureName() {
		return ((PrefixType)getType()).getFeatureName();
	}

	public String unparse(Style style) {
		StringBuffer result = new StringBuffer();
		result.append("{prefix:");
		result.append(getFeatureName());
		result.append("}");
		return result.toString();
	}
}
