// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.test;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.BitStringReader;
import org.codemancer.cpudl.ShortBitString;
import org.codemancer.cpudl.Architecture;
import org.codemancer.cpudl.FeatureSet;
import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Constant;
import org.codemancer.cpudl.expr.Prefix;

public class ListInstructions {
	/** List instructions.
	 * Usage: java org.codemancer.test.ListInstructions <architecture> <width> <start> <count> <step>
	 * The instructions are written to stdout.
	 */
	public static final void main(String args[]) throws Exception {
		String architectureName = args[0];
		long width = Integer.parseInt(args[1]);
		long opcode = Long.parseLong(args[2], 16);
		long count = Long.parseLong(args[3], 16);
		long step = Long.parseLong(args[4], 16);

		Architecture arch = Architecture.makeArchitecture(architectureName);
		Type start = arch.getStart();
		FeatureSet features = new FeatureSet(arch);

		while (count > 0) {
			BitString bits = new ShortBitString(opcode, width, arch.isBigEndian());
			bits = bits.concat(new ShortBitString(0, 64, false));
			BitReader bitReader = new BitStringReader(bits);
			List<BitReader> bitReaders = new ArrayList<BitReader>();
			bitReaders.add(bitReader);

			Expression expr = start.decode(bitReaders, features);
			if (expr == null) {
				System.out.printf("%X\t(null)\n", opcode);
			} else if (expr instanceof Prefix) {
				System.out.printf("%X\t(prefix)\n", opcode);
			} else {
				Map<String, Expression> registers = new HashMap<String, Expression>();
				registers.put("PC", new Constant(null, 0));
				expr = expr.resolveReferences(null, null);
				expr = expr.resolveRegisters(registers).simplify();
				System.out.printf("%X\t%s\t%s\n", opcode, start.unparse(0, expr), start.unparse(1, expr));
			}
			opcode += step;
			count -= 1;
		}
	}
}
