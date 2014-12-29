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
import javax.persistence.EntityManager;

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
public class SsaState implements State {
	/** Registers which should not be tracked.
	 * These are hardcoded as a temporary measure until the
	 * CPU descriptions provide a means to classify registers
	 * by role.
	 */
	private static final Set<String> noTrack = new HashSet<String>(
		Arrays.asList("PC", "PC+", "LR", "SP", "R13"));

	/** The database in which mappings are to be recorded. */
	private Database db;

	/** The entity manager for the database. */
	private EntityManager em;

	/** The subroutine that will allocate any machine-generated SSA expression names. */
	private Subroutine subroutine;

	/** The set of current live register mappings. */
	private HashMap<String, SsaMapping> liveMappings;

	/** The set of current live temporary values. */
	private HashMap<String, Expression> liveTemporaries;

	/** The address of the current instruction. */
	private long curAddr;

	/** The address of the next instruction. */
	private long nextAddr;

	/** The address following the instruction which most recently invalidated all registers. */
	private long invalidationAddr;

	/** Finalise the mapping of a given register, if live.
	 * @param regName the name of the register to be finalised
	 */
	private void finaliseRegister(String regName) {
		SsaMapping oldMapping = liveMappings.get(regName);
		if (oldMapping != null) {
			SsaMapping newMapping = new SsaMapping(0, -1, oldMapping.getMinAddr(), nextAddr,
				oldMapping.getName(), oldMapping.getValue());
			em.persist(newMapping);
			liveMappings.remove(regName);
		}
	}

	/** Finalise the mapping of all registers. */
	private void finaliseAllRegisters() {
		for (Map.Entry<String, SsaMapping> entry: liveMappings.entrySet()) {
			String name = entry.getKey();
			SsaMapping oldMapping = liveMappings.get(name);
			SsaMapping newMapping = new SsaMapping(0, -1, oldMapping.getMinAddr(), nextAddr,
				oldMapping.getName(), oldMapping.getValue());
			em.persist(newMapping);
		}
		liveMappings.clear();
	}

	/** Construct empty SSA state representation.
	 * @param db the database in which mappings are to be recorded
	 * @param subroutine the subroutine used to allocate any SSA names
	 * @param addr the initial invalidation address
	 */
	public SsaState(Database db, Subroutine subroutine, long addr) {
		this.db = db;
		this.em = db.getEntityManager();
		this.subroutine = subroutine;
		this.invalidationAddr = addr;
		this.liveMappings = new HashMap<String, SsaMapping>();
		this.liveTemporaries = new HashMap<String, Expression>();
	}

	/** Copy SSA state representation.
	 * @param subroutine the subroutine used to allocate any SSA names
	 * @param state the state to copy
	 */
	public SsaState(Subroutine subroutine, SsaState state) {
		this.db = state.db;
		this.subroutine = state.subroutine;
		this.liveMappings = (HashMap<String, SsaMapping>)state.liveMappings.clone();
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
		finaliseAllRegisters();
		invalidationAddr = nextAddr;
	}

	public final Expression get(Register register) {
		// Register names are used here as a temporary measure until
		// the CPU description language includes information about
		// register roles.
		String regName = register.getName();
		if (noTrack.contains(regName)) return null;
		SsaMapping mapping = liveMappings.get(regName);
		if (mapping == null) {
			String ssaName = subroutine.allocateSsaName();
			SsaExpression ssaValue = new SsaExpression(0, -1, ssaName);
			em.persist(ssaValue);
			mapping = new SsaMapping(0, -1, invalidationAddr, -1, regName, ssaValue);
			liveMappings.put(regName, mapping);
		}
		return new NamedValue(register.getType(), mapping.getValue().getName());
	}

	public final void put(Register register, Expression value) {
		String regName = register.getName();
		if (noTrack.contains(regName)) return;
		finaliseRegister(regName);
		String ssaName = subroutine.allocateSsaName();
		SsaExpression ssaValue = new SsaExpression(0, -1, ssaName);
		em.persist(ssaValue);
		SsaMapping mapping = new SsaMapping(0, -1, nextAddr, -1, regName, ssaValue);
		liveMappings.put(regName, mapping);
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
