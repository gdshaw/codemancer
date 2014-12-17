// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.analysis;

import java.util.List;
import java.util.ArrayList;

import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Assignment;
import org.codemancer.cpudl.expr.Fragment;

/** A class for classifying instructions according to how they affect the flow of control. */
public class InstructionClassifier {
	/** True if the instruction is capable of altering the program counter, otherwise false. */
	private boolean altersPC = false;

	/** True if the instruction unconditionally alters the program counter, otherwise false. */
	private boolean unconditionallyAltersPC = false;

	/** True if the instruction saves the program counter (anywhere, not necessarily on the stack), otherwise false. */
	private boolean savesPC = false;

	/** True if the instruction restores the program counter by setting it equal to one
	 * of a given set of expressions (the list having been supplied to the constructor),
	 * otherwise false. Note that restoresPC implies altersPC.
	 **/
	private boolean restoresPC = false;

	/** A list of the expressions which this instruction is capable of writing to the program counter. */
	private final List<Expression> destinationAddresses = new ArrayList<Expression>();

	/** Process a list of assignments.
	 * @param assignments the list of assignments to be processed
	 * @param conditional true if the assignments are conditional, otherwise false
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 */
	private final void process(List<Assignment> assignments, boolean conditional, Expression pc, List<Expression> links) {
		for (Assignment assignment: assignments) {
			if (assignment.getLhs().equals(pc)) {
				altersPC = true;
				if (!conditional) {
					unconditionallyAltersPC = true;
				}
				if (links != null) {
					for (Expression link: links) {
						if (assignment.getRhs().equals(link)) {
							restoresPC = true;
						}
					}
				}
				destinationAddresses.add(assignment.getRhs());
			}
			if (assignment.getRhs().equals(pc)) {
				savesPC = true;
			}
		}
	}

	/** Create a classifier for a given instruction.
	 * @param effect the resolved effect of the instruction
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 */
	public InstructionClassifier(Expression effect, Expression pc, List<Expression> links) {
		if (effect instanceof Fragment) {
			effect = ((Fragment)effect).getEffect();
		}
		List<Assignment> uncond = new ArrayList<Assignment>();
		List<Assignment> cond = new ArrayList<Assignment>();
		effect.listAssignments(uncond, cond, false);
		process(uncond, false, pc, links);
		process(cond, true, pc, links);
	}

	/** Test whether this instruction call fall through to the next one.
	 * If an instruction is a subroutine call then it is presumed
	 * to fall through; otherwise, if it unconditionally alters the
	 * program counter then it is presumed not to fall through; otherwise,
	 * it is presumed to fall through.
	 * The result is unspecified in the following special cases:
	 * - where a branch is at first sight conditional, but the condition is
	 *   always satisfied; and
	 * - where the destination address is or could be the following
	 *   instruction.
	 * @return true if this instruction can fall through, otherwise false
	 */
	public final boolean canFallThrough() {
		return savesPC || !unconditionallyAltersPC;
	}

	/** Test whether the instruction is a branch.
	 * A branch is defined to be an instruction which is capable of changing
	 * the program counter (other than by the normal process of fall through
	 * to the next instruction), but which has not been identified as a
	 * subroutine call or return instruction.
	 * @return true if a branch, otherwise false
	 */
	public final boolean isBranch() {
		return altersPC && (!savesPC) && (!restoresPC);
	}

	/** Test whether the instruction is a subroutine call.
	 * A subroutine call is defined to be an instruction which is capable of
	 * both making a copy of the program counter (anywhere, not necessarily
	 * on the stack) and of changing the program counter.
	 * @return true if a subroutine call, otherwise false
	 */
	public final boolean isCall() {
		return altersPC && savesPC;
	}

	/** Test whether the instruction is a subroutine return.
	 * A subroutine return is defined to be an instruction which is capable of
	 * copying one of a given list of expressions to the program counter
	 * (the list having been supplied to the constructor).
	 * @return true if a subroutine return, otherwise false
	 */
	public final boolean isReturn() {
		return restoresPC;
	}

	/** Get a list of destination addresses.
	 * The result is a list of addresses which this instruction is capable
	 * of writing to the program counter (excluding addresses attributable
	 * to the normal process of fall through to the next instruction).
	 * This list will be non-empty if the instruction is a branch, call or
	 * return instruction, otherwise it will be empty.
	 * @return the list of possible destination addresses
	 */
	public final List<Expression> getDestinationAddresses() {
		return destinationAddresses;
	}
}
