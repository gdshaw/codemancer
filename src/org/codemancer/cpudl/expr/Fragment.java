// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.Map;
import java.util.HashMap;

import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.CpudlReferenceException;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent an instruction fragment. */
public class Fragment extends Expression {
	/** The members of this fragment, indexed by name. */
	private final Map<String, Expression> members = new HashMap<String, Expression>();

	/** The effect of this fragment. */
	private Expression effect = null;

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

	/** Get effect.
	 * @return the unresolved effect of this fragment
	 */
	public final Expression getEffect() {
		return effect;
	}

	/** Set effect.
	 * @param effect the required effect of this fragment
	 */
	public final void setEffect(Expression effect) {
		this.effect = effect;
	}

	public Expression resolveReferences(Fragment frag, Map<String, Expression> args)
		throws CpudlReferenceException {

		Fragment resolvedFragment = new Fragment(getType());
		for (Map.Entry<String, Expression> entry: members.entrySet()) {
			Expression resolvedValue = entry.getValue().resolveReferences(this, args);
			resolvedFragment.put(entry.getKey(), resolvedValue);
		}
		if (effect != null) {
			Expression resolvedEffect = effect.resolveReferences(this, args);
			resolvedFragment.setEffect(resolvedEffect);
		}
		return resolvedFragment;
	}

	public Expression resolveRegisters(Map<String, Expression> registers) {
		Fragment resolvedFragment = new Fragment(getType());
		for (Map.Entry<String, Expression> entry: members.entrySet()) {
			Expression resolvedValue = entry.getValue().resolveRegisters(registers);
			resolvedFragment.put(entry.getKey(), resolvedValue);
		}
		if (effect != null) {
			Expression resolvedEffect = effect.resolveRegisters(registers);
			resolvedFragment.setEffect(resolvedEffect);
		}
		return resolvedFragment;
	}

	public Expression simplify() {
		Fragment simplifiedFragment = new Fragment(getType());
		for (Map.Entry<String, Expression> entry: members.entrySet()) {
			Expression simplifiedValue = entry.getValue().simplify();
			simplifiedFragment.put(entry.getKey(), simplifiedValue);
		}
		if (effect != null) {
			Expression simplifiedEffect = effect.simplify();
			simplifiedFragment.setEffect(simplifiedEffect);
		}
		return simplifiedFragment;
	}

	public String unparse(Style style) {
		StringBuffer result = new StringBuffer();
		result.append("{");
		for (Map.Entry<String, Expression> entry: members.entrySet()) {
			result.append(entry.getKey());
			result.append(":");
			result.append(entry.getValue().unparse(style));
		}
		result.append("}");
		return result.toString();
	}
}
