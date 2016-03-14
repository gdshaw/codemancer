// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.analysis;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import org.codemancer.cpudl.State;
import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Constant;
import org.codemancer.cpudl.expr.Register;
import org.codemancer.cpudl.expr.Memory;
import org.codemancer.cpudl.expr.Temporary;
import org.codemancer.cpudl.expr.NamedValue;
import org.codemancer.db.Subroutine;
import org.codemancer.db.SsaExpression;
import org.codemancer.db.SsaMapping;
import org.codemancer.db.Database;

/** A class to represent the state of the machine in terms of live SSA expressions. */
public class SsaStateRecorder implements State {
	/** Registers which should not be tracked.
	 * These are hardcoded as a temporary measure until the
	 * CPU descriptions provide a means to classify registers
	 * by role.
	 */
	private static final Set<String> noTrack = new HashSet<String>(
		Arrays.asList("PC", "PC+", "LR", "SP", "R13"));

	/** The database in which mappings are to be recorded. */
	private Database db;

	/** The subroutine that will allocate any machine-generated SSA expression names. */
	private Subroutine subroutine;

	/** The set of current live expressions. */
	private HashMap<String, SsaExpression> liveExpressions;

	/** The set of current live temporary values. */
	private HashMap<String, Expression> liveTemporaries;

	/** The address of the current instruction. */
	private long curAddr;

	/** The address of the next instruction. */
	private long nextAddr;

	/** Construct empty SSA state representation.
	 * @param db the database in which mappings are to be recorded
	 * @param subroutine the subroutine used to allocate any SSA names
	 */
	public SsaStateRecorder(Database db, Subroutine subroutine) {
		this.db = db;
		this.subroutine = subroutine;
		this.liveExpressions = new HashMap<String, SsaExpression>();
		this.liveTemporaries = new HashMap<String, Expression>();
	}

	/** Copy SSA state representation.
	 * @param state the state to copy
	 */
	public SsaStateRecorder(SsaStateRecorder state) {
		this.db = state.db;
		this.subroutine = state.subroutine;
		this.liveExpressions = (HashMap<String, SsaExpression>)state.liveExpressions.clone();
		this.liveTemporaries = new HashMap<String, Expression>();
	}

	/** Set address of current and next instruction.
	 * @param curAddr the address of the current instruction
	 * @param nextAddr the address of the next instruction
	 */
	public void setAddr(long curAddr, long nextAddr) {
		this.curAddr = curAddr;
		this.nextAddr = nextAddr;
	}

	/** Invalidate all registers with effect from the current instruction. */
	public void invalidate() {
		liveExpressions.clear();
	}

	public final Expression get(Register register) {
		// Register names are used here as a temporary measure until
		// the CPU description language includes information about
		// register roles.
		String regName = register.getName();
		if (noTrack.contains(regName)) return null;
		SsaExpression expr = liveExpressions.get(regName);
		if (expr == null) {
			String ssaName = subroutine.allocateSsaName();
			expr = db.getSsaExpressions().make(subroutine, ssaName);
			liveExpressions.put(regName, expr);
		}
		SsaMapping mapping = db.getSsaMappings().make(curAddr, true, regName, expr);
		return new NamedValue(register.getType(), expr.getName());
	}

	public final void put(Register register, Expression value) {
		String regName = register.getName();
		if (noTrack.contains(regName)) return;

		SsaExpression expr;
		if (value instanceof NamedValue) {
			NamedValue namedValue = (NamedValue)value;
			expr = db.getSsaExpressions().get(subroutine, namedValue.getName());
		} else {
			String ssaName = subroutine.allocateSsaName();
			expr = db.getSsaExpressions().make(subroutine, ssaName);
		}
		liveExpressions.put(regName, expr);
		SsaMapping mapping = db.getSsaMappings().make(curAddr, false, regName, expr);
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
}
