// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.analysis;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.codemancer.loader.ObjectFileReader;
import org.codemancer.cpudl.Architecture;
import org.codemancer.cpudl.FeatureSet;
import org.codemancer.cpudl.State;
import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.ShortBitString;
import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.BitStringReader;
import org.codemancer.cpudl.type.Type;
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
	/** A reader for the object file that is being disassembled. */
	private ObjectFileReader objReader;

	/** The architecture to be used when disassembling. */
	private Architecture arch;

	/** The set of input expressions for the given instruction. */
	private HashMap<String, SsaExpression> inputs;

	/** The set of output expressions for the given instruction. */
	private HashMap<String, SsaExpression> outputs;

	/** The current set of live temporary values. */
	private HashMap<String, Expression> liveTemporaries;

	/** The address of the instruction to be commented. */
	private long instrAddr;

	/** The accumulated comment (which may span multiple lines). */
	private StringBuilder commentBuilder = new StringBuilder();

	/** Construct machine state representation for commenting an instruction
	 * @param objReader a reader for the object file that is being disassembled
	 * @param db the database from which mappings are to be fetched
	 * @param arch the architecture to be used when disassembling
	 * @param instrAddr the address of the instruction to be commented
	 */
	public SsaStatePlayer(ObjectFileReader objReader, Database db, Architecture arch, long instrAddr) {
		this.objReader = objReader;
		this.arch = arch;
		this.instrAddr = instrAddr;
		this.inputs = new HashMap<String, SsaExpression>();
		this.outputs = new HashMap<String, SsaExpression>();
		this.liveTemporaries = new HashMap<String, Expression>();

		for (SsaMapping mapping: db.getSsaMappings().get(instrAddr)) {
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
		} else if (regName.equals("PC")) {
			return new Constant(null, instrAddr);
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

	/** Read from known absolute memory location.
	 * @param type the type of the value to be read
	 * @param memAddr the address, as a constant expression
	 * @return the value read from memory
	 */
	private final Expression get(Type type, Constant memAddr) {
		// Cannot fetch unless type is known.
		if (type == null) return null;

		// Cannot fetch unless memory content available for that address.
		if (!objReader.isMapped(memAddr.getValue())) return null;

		// Fetch bits from memory.
		long tell = objReader.tell();
		objReader.seek(memAddr.getValue());
		long width = (type.getFixedWidth(0) + 7) / 8;
		BitString bits = new ShortBitString(0, 0, false);
		for (long i = 0; i != width; ++i) {
			byte newByte = objReader.get();
			BitString newBits = new ShortBitString(newByte, 8, arch.isBigEndian());
			bits = bits.concat(newBits);
		}
		objReader.seek(tell);

		// Transform bits into expression using type.
		BitStringReader reader = new BitStringReader(bits);
		List<BitReader> readers = new ArrayList<BitReader>();
		readers.add(reader);
		return type.decode(readers, new FeatureSet(arch));
	}

	public final Expression get(Memory memory) {
		Expression memAddr = memory.getAddress();
		if (memAddr instanceof Constant) {
			Expression value = get(memory.getType(), (Constant)memAddr);
			if (value != null) return value;
		}
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
