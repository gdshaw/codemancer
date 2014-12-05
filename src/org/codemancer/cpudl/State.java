// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Register;
import org.codemancer.cpudl.expr.Memory;
import org.codemancer.cpudl.expr.Temporary;

/** An interface to represent the state of the machine.
 * The purpose of this interface is to allow instructions to access and
 * modify the machine state without regard to how and to what extent this
 * state is recorded. It does not therefore provide any means to access
 * prior state (however implementations are at liberty to provide such
 * functionality if required).
 */
public interface State {
	/** Get the value of a given register.
	 * @param register the register to be inspected
	 * @return the value of that register
	 */
	Expression get(Register register);

	/** Put a value into a register.
	 * @param register the register to be altered
	 * @param value the value to be placed in that register
	 */
	void put(Register register, Expression value);

	/** Get the value of a given memory location.
	 * @param memory the memory location to be inspected
	 * @return the value of that location
	 */
	Expression get(Memory memory);

	/** Put a value into a memory location.
	 * @param memory the memory location to be altered
	 * @param value the value to be placed in that location
	 */
	void put(Memory memory, Expression value);

	/** Get the value of a temporary.
	 * @param temp the temporary value to be inspected
	 * @return the value of that temporary
	 */
	Expression get(Temporary temp);

	/** Put a value into a temporary.
	 * @param temporary the temporary to be altered
	 * @param value the value to be placed in that temporary
	 */
	void put(Temporary temp, Expression value);
}
