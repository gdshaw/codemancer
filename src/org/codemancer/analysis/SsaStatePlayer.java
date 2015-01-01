// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.analysis;

import java.util.Map;
import java.util.HashMap;

import org.codemancer.cpudl.State;
import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Constant;
import org.codemancer.cpudl.expr.Register;
import org.codemancer.cpudl.expr.Memory;
import org.codemancer.cpudl.expr.Temporary;
import org.codemancer.cpudl.expr.NamedValue;
import org.codemancer.db.SsaExpression;
import org.codemancer.db.SsaMapping;
import org.codemancer.db.Database;

/** A class to represent the state of the machine for the purpose of commenting an instruction. */
public class SsaStatePlayer implements State {
	/** The set of input expressions for the given instruction. */
	private HashMap<String, SsaExpression> inputs;

	/** The set of output expressions for the given instruction. */
	private HashMap<String, SsaExpression> outputs;

	/** The current set of live temporary values. */
	private HashMap<String, Expression> liveTemporaries;

	/** The address of the instruction to be commented. */
	private long addr;

	/** The accumulated comment (which may span multiple lines). */
	private StringBuilder commentBuilder = new StringBuilder();

	/** Construct machine state representation for commenting an instruction
	 * @param db the database from which mappings are to be fetched
	 * @param addr the address of the instruction to be commented
	 */
	public SsaStatePlayer(Database db, long addr) {
		this.addr = addr;
		this.inputs = new HashMap<String, SsaExpression>();
		this.outputs = new HashMap<String, SsaExpression>();
		this.liveTemporaries = new HashMap<String, Expression>();

		for (SsaMapping mapping: db.getSsaMappings(addr)) {
			if (mapping.isInbound()) {
				inputs.put(mapping.getName(), mapping.getValue());
			} else {
				outputs.put(mapping.getName(), mapping.getValue());
			}
		}
	}

	public final Expression get(Register register) {
		String regName = register.getName();
		SsaExpression expr = inputs.get(regName);
		if (expr != null) {
			return new NamedValue(register.getType(), expr.getName());
		} else {
			return register;
		}
	}

	public final void put(Register register, Expression value) {
		String regName = register.getName();
		SsaExpression expr = outputs.get(regName);
		if (expr != null) {
			if ((value instanceof NamedValue) && ((NamedValue)value).getName().equals(expr.getName())) {
				// Avoid comments of the form v0 := v0. Instead, just list the register.
				commentBuilder.append(String.format("%s := %s\n", regName, value.unparse(new Style())));
			} else {
				commentBuilder.append(String.format("%s (%s) := %s\n", regName, expr.getName(), value.unparse(new Style())));
			}
		}
	}

	public final Expression get(Memory memory) {
		return null;
	}

	public final void put(Memory memory, Expression value) {}

	public final Expression get(Temporary temp) {
		return liveTemporaries.get(temp.getName());
	}

	public final void put(Temporary temp, Expression value) {
		liveTemporaries.put(temp.getName(), value);
	}

	public final String getComment() {
		return commentBuilder.toString();
	}
}
