// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;

import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.ShortBitString;
import org.codemancer.cpudl.Architecture;
import org.codemancer.cpudl.EphemeralState;
import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Constant;
import org.codemancer.cpudl.expr.Register;
import org.codemancer.cpudl.expr.Fragment;

@RunWith(Parameterized.class)
public class CpuTest {
	Architecture arch;
	Type start;
	BitString code;
	String[] fields;
	String[] preconds;
	String[] postconds;

	private static int hexValue(char c) {
		if ((c >= '0') && (c <= '9')) {
			return c - '0';
		} else if ((c >= 'A') && (c <= 'F')) {
			return c - 'A' + 10;
		} else if ((c >= 'a') && (c <= 'f')) {
			return c - 'a' + 10;
		} else {
			throw new IllegalArgumentException("hex digit expected");
		}
	}

	public CpuTest(Architecture arch, String line, String setup) throws Exception {
		this.arch = arch;
		this.start = arch.getStart();

		this.postconds = new String[0];
		int f = line.indexOf("\t?");
		if (f != -1) {
			this.postconds = line.substring(f + 2, line.length()).split(",");
			line = line.substring(0, f);
		}
		this.fields = line.split("\t");

		this.code = new ShortBitString();
		String codeField = this.fields[0];
		for (int i = 0; i + 1 < codeField.length(); i += 2) {
			int v = (hexValue(codeField.charAt(i)) << 4) |
				hexValue(codeField.charAt(i + 1));
			code = code.concat(new ShortBitString(v, 8));
		}

		this.preconds = setup.split(",");
	}

	@Test
	public void disassemble() throws Exception {
		BitReader reader = new BitStringReader(code);
		List<BitReader> readers = new ArrayList<BitReader>();
		readers.add(reader);

		Expression expr = start.decode(readers);
		assertTrue(expr != null);
		Expression effect = expr.resolve(null, null, false);

		EphemeralState state = new EphemeralState();
		for (int i = 0; i != preconds.length; ++i) {
			String precond = preconds[i];
			int f = precond.indexOf("=");
			if (f == -1) {
				throw new IllegalArgumentException("missing = in precondition");
			}
			String name = precond.substring(0, f);
			long value = Long.parseLong(precond.substring(f + 1, precond.length()));
			state.put(Register.make(name), new Constant(null, value));
		}
		if (effect != null) effect.evaluate(state);

		for (int i = 0; i != postconds.length; ++i) {
			String postcond = postconds[i];
			int f = postcond.indexOf("=");
			if (f == -1) {
				throw new IllegalArgumentException("missing = in postcondition");
			}
			String name = postcond.substring(0, f);
			String expected = postcond.substring(f + 1, postcond.length());
			String found = state.get(Register.make(name)).unparse();
			assertEquals(found, expected);
		}

		for (int i = 0; i != start.getPieceCount(); ++i) {
			String asm = start.unparse(i, expr);
			if (i + 1 < fields.length) {
				assertEquals(fields[i + 1].replace(" ", ""), asm);
			} else {
				assertEquals(new String(), asm);
			}
		}
	}

	@Parameters
	public static Collection<Object[]> getParameters() throws Exception {
		Collection<Object[]> params = new ArrayList<Object[]>();
		String setup = new String();
		File[] testFiles = new File("testdata/cpus").listFiles();
		for (int i = 0; i != testFiles.length; ++i) {
			File testFile = testFiles[i];
			String fileName = testFile.getName();
			if (fileName.endsWith(".test")) {
				String architectureName = fileName.substring(0, fileName.length() - 5);
				Architecture arch = Architecture.makeArchitecture(architectureName);
				String testDataPath = "testdata/cpus/" + architectureName + ".test";
				BufferedReader reader = new BufferedReader(new FileReader(testDataPath));
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.length() == 0) continue;
					if (line.charAt(0) == ';') continue;
					if (line.charAt(0) == '!') {
						setup = line.substring(1, line.length());
						continue;
					}
					params.add(new Object[] {arch, line, setup});
				}
			}
		}
		return params;
	}
}
