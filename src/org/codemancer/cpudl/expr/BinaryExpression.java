// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.List;
import java.util.Map;

import org.codemancer.cpudl.State;
import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.CpudlReferenceException;
import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.type.FragmentType;

/** An abstract expression class to represent an operation with two arguments. */
public abstract class BinaryExpression extends Expression {
	/** The left-hand argument. */
	private final Expression lhs;

	/** The right-hand argument. */
	private final Expression rhs;

	/** Construct a binary expression of given type.
	 * @param type the required type
	 * @param lhs the left-hand argument
	 * @param rhs the right-hand argument
	 */
	public BinaryExpression(Type type, Expression lhs, Expression rhs) {
		super(type);
		this.lhs = lhs;
		this.rhs = rhs;
	}

	/** Return a binary expression identical to this one except for its arguments.
	 * @param lhs the required left-hand argument
	 * @param rhs the required right-hand argument
	 * @return the partially cloned binary expression
	 */
	public abstract Expression partialClone(Expression lhs, Expression rhs);

	/** Get the symbol representing this operation.
	 * @return the symbol
	 */
	public abstract String getSymbol();

	/** Get the left-hand argument.
	 * @return the left-hand argument
	 */
	public final Expression getLhs() {
		return lhs;
	}

	/** Get the right-hand argument.
	 * @return the right-hand argument
	 */
	public final Expression getRhs() {
		return rhs;
	}

	public Expression resolveReferences(Fragment frag, Map<String, Expression> args) {
		Expression resolvedLhs = lhs.resolveReferences(frag, args);
		Expression resolvedRhs = rhs.resolveReferences(frag, args);
		return partialClone(resolvedLhs, resolvedRhs);
	}

	public Expression resolveRegisters(Map<String, Expression> registers) {
		Expression resolvedLhs = lhs.resolveRegisters(registers);
		Expression resolvedRhs = rhs.resolveRegisters(registers);
		return partialClone(resolvedLhs, resolvedRhs);
	}

	public Expression evaluate(State state) {
		Expression evaluatedLhs = lhs.evaluate(state);
		Expression evaluatedRhs = rhs.evaluate(state);
		return partialClone(evaluatedLhs, evaluatedRhs);
	}

	public void listAssignments(List<Assignment> uncond, List<Assignment> cond, boolean isCond) {
		lhs.listAssignments(uncond, cond, isCond);
		rhs.listAssignments(uncond, cond, isCond);
	}

	public String unparse(Style style) {
		StringBuffer result = new StringBuffer();
		result.append(lhs.unparse(style));
		result.append(getSymbol());
		result.append(rhs.unparse(style));
		return result.toString();
	}
}
