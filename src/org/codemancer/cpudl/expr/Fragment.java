// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.Map;
import java.util.HashMap;

import org.codemancer.cpudl.type.Type;

/** An expression class to represent an instruction fragment. */
public class Fragment extends Expression {
	/** The members of this fragment, indexed by name. */
	private final Map<String, Expression> members = new HashMap<String, Expression>();

	/** Construct empty fragment.
	 * @param type the type of this fragment
	 */
	public Fragment(Type type) {
		super(type);
	}

	/** Get member.
	 * @param name the name of the required member.
	 * @return the value of the member, or null if not found
	 */
	public final Expression get(String name) {
		return members.get(name);
	}

	/** Put member.
	 * @param name the name of the member to be set.
	 * @param value the required value of that member
	 */
	public final void put(String name, Expression value) {
		members.put(name, value);
	}

	public String unparse() {
		StringBuffer result = new StringBuffer();
		result.append("{");
		for (Map.Entry<String, Expression> entry: members.entrySet()) {
			result.append(entry.getKey());
			result.append(":");
			result.append(entry.getValue().unparse());
		}
		result.append("}");
		return result.toString();
	}
}
