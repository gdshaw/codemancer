// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Constant;
import org.codemancer.cpudl.expr.Register;
import org.codemancer.cpudl.expr.Memory;
import org.codemancer.cpudl.expr.Temporary;

/** A class to represent the state of the machine at a single point in the program.
 * Information about prior state is discarded once it has been superseded.
 */
public class EphemeralState implements State {
	/** The current state of the registers. */
	private TreeMap<String, Expression> registers = new TreeMap<String, Expression>();

	/** The current state of memory. */
	private TreeMap<Long, Expression> locations = new TreeMap<Long, Expression>();

	/** The current state of temporary values. */
	private TreeMap<Long, Expression> temporaries = new TreeMap<Long, Expression>();

	public final Expression get(Register register) {
		return registers.get(register.getName());
	}

	public final void put(Register register, Expression value) {
		registers.put(register.getName(), value);
	}

	public final Expression get(Memory memory) {
		Expression address = memory.getAddress().evaluate(this);
		if (!(address instanceof Constant)) {
			return null;
		}
		Constant constAddress = (Constant)address;
		return locations.get(constAddress.getValue());
	}

	public final void put(Memory memory, Expression value) {
		Expression address = memory.getAddress().evaluate(this);
		if (!(address instanceof Constant)) {
			return;
		}
		Constant constAddress = (Constant)address;
		locations.put(constAddress.getValue(), value);
	}

	public final Expression get(Temporary temp) {
		return temporaries.get(temp.getId());
	}

	public final void put(Temporary temp, Expression value) {
		temporaries.put(temp.getId(), value);
	}

	/** Dump the machine state to a given stream in human-readable form.
	 * @param out the stream to which output should be sent
	 */
	public final void dump(PrintWriter out) {
		Style style = new Style();
		for (Map.Entry<String, Expression> entry: registers.entrySet()) {
			out.printf("%s: %s\n", entry.getKey(), entry.getValue().unparse(style));
		}
		for (Map.Entry<Long, Expression> entry: locations.entrySet()) {
			out.printf("%08X: %s\n", entry.getKey(), entry.getValue().unparse(style));
		}
	}
}
