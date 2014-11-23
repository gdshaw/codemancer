// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.Map;
import java.util.HashMap;

import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.type.IntegerType;

/** A class for collecting terms when simplifying an addition or subtraction. */
public class Accumulator {
	/** The result type for this accumulator. */
	Type type;

	/** The accumulated non-constant terms, indexed by expression. */
	Map<Expression, Long> terms = new HashMap<Expression, Long>();

	/** The accumulated constant term. */
	long constantTerm = 0;

	/** The result mask. */
	long resultMask = -1L;

	/** Construct accumulator of given type.
	 * @param type the result type for this accumulator
	 */
	public Accumulator(Type type) {
		this.type = type;
		if (type instanceof IntegerType) {
			int size = ((IntegerType)type).getSize();
			if (size < 64) {
				resultMask = (1L << size) - 1;
			}
		}
	}

	/** Add a given term to this accumulator
	 * @param term the term to be added, or null for a constant term
	 * @param multiplier a multiplier to be applied to the term
	 */
	public final void accumulate(Expression term, long multiplier) {
		if (term == null) {
			constantTerm += multiplier;
		} else {
			term = term.simplify();
			if (term instanceof Constant) {
				constantTerm += multiplier * ((Constant)term).getValue();
			} else {
				Long prevMultiplier = terms.get(term);
				if (prevMultiplier == null) {
					prevMultiplier = new Long(0);
				}
				terms.put(term, prevMultiplier + multiplier);
			}
		}
	}

	/** Get the simplified content of this accumulator.
	 * @return the simplified content
	 */
	public final Expression simplify() {
		constantTerm &= resultMask;

		Expression result = null;
		for (Map.Entry<Expression, Long> entry: terms.entrySet()) {
			Expression term = entry.getKey();
			long multiplier = entry.getValue();
			if (multiplier > 1) {
				term = new Multiplication(type, new Constant(type, multiplier), term);
			}
			if (multiplier > 0) {
				if (result == null) {
					result = term;
				} else {
					result = new Addition(type, result, term);
				}
			}
		}
		if (result == null) {
			result = new Constant(type, constantTerm);
			constantTerm = 0;
		} else if (constantTerm > 0) {
			result = new Addition(type, result, new Constant(type, constantTerm));
			constantTerm = 0;
		}
		for (Map.Entry<Expression, Long> entry: terms.entrySet()) {
			Expression term = entry.getKey();
			long multiplier = entry.getValue();
			if (multiplier < -1) {
				term = new Multiplication(type, new Constant(type, -multiplier), term);
			}
			if (multiplier < 0) {
				result = new Subtraction(type, result, term);
			}
		}
		if (constantTerm < 0) {
			result = new Subtraction(type, result, new Constant(type, -constantTerm));
			constantTerm = 0;
		}
		return result;
	}
}
