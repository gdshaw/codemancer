// This file is part of Codemancer.
// Copyright 2014-15 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.List;
import java.util.Map;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.State;
import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent a slice of another expression.
 * A slice is a selection of bits [index:index+size-1] of the supplied
 * operand, which are presented as bits [offset:offset+size-1]. Bits
 * [0:offset-1] of the result are padded with zeros.
 * It is unclear at present whether the result should be the type of
 * the operand, or a new integer type based on the size and offset.
 * The former has been implemented in the first instance, however
 * there is a significant chance that this will change in a future
 * version.
 */
public class Slice extends Expression {
	/** The operand to be sliced. */
	private Expression operand;

	/** The starting index of the slice, in bits. */
	private int index;

	/** The size of the slice, in bits. */
	private int size;

	/** The offset by which the result is shifted left, in bits. */
	private int offset;

	/** Construct slice.
	 * @param operand the operand to be sliced
	 * @param index the starting index of the slice, in bits
	 * @param size the slice, in bits
	 * @param offset the offset by which the result is shifted left, in bits
	 */
	public Slice(Expression operand, int index, int size, int offset) {
		super(operand.getType());
		this.operand = operand;
		this.index = index;
		this.size = size;
		this.offset = offset;
	}

	/** Get the operand that has been sliced.
	 * @param the operand that has been sliced
	 */
	public Expression getOperand() {
		return operand;
	}

	/** Get the starting index of this slice
	 * @return the index, in bits
	 */
	public int getIndex() {
		return index;
	}

	/** Get the size of this slice
	 * @return the size, in bits
	 */
	public int getSize() {
		return size;
	}

	/** Get the offset for this slice
	 * @return the offset, in bits
	 */
	public int getOffset() {
		return offset;
	}

	public Expression simplify() {
		Expression simpleOperand = operand.simplify();
		if (simpleOperand instanceof Constant) {
			long constOperand = ((Constant)simpleOperand).getValue();
			long mask = (size < 64) ? ((1L << size) - 1) : -1;
			return new Constant(getType(), ((constOperand >> index) & mask) << offset);
		}
		return new Slice(simpleOperand, index, size, offset);
	}

	public Expression resolveReferences(Fragment frag, Map<String, Expression> args) {
		Expression resolvedOperand = operand.resolveReferences(frag, args);
		return new Slice(resolvedOperand, index, size, offset);
	}

	public Expression resolveRegisters(Map<String, Expression> registers) {
		Expression resolvedOperand = operand.resolveRegisters(registers);
		return new Slice(resolvedOperand, index, size, offset);
	}

	public Expression evaluate(State state) {
		Expression evaluatedOperand = operand.evaluate(state);
		return new Slice(evaluatedOperand, index, size, offset);
	}

	public void listAssignments(List<Assignment> uncond, List<Assignment> cond, boolean isCond) {
		operand.listAssignments(uncond, cond, isCond);
	}

	public String unparse(Style style) {
		StringBuffer result = new StringBuffer();
		result.append(operand.unparse(style));
		result.append(String.format("[%d:%d:%d]", index, size, offset));
		return result.toString();
	}

	/** Make slice from XML element.
	 * @param ctx the context of this expression
	 * @param element the expression as an XML element
	 * @return the expression as an object
	 */
	public static Expression make(Context ctx, Element element) throws CpudlParseException {
		int index = Integer.decode(element.getAttribute("index"));
		int size = Integer.decode(element.getAttribute("size"));
		int offset = 0;
		String offsetString = element.getAttribute("offset");
		if (offsetString.length() != 0) {
			offset = Integer.decode(offsetString);
		}

		Expression operand = Sequence.make(ctx, element);
		return new Slice(operand, index, size, offset);
	}
}
