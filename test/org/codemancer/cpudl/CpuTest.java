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
import java.util.Map;
import java.util.HashMap;

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
import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.FeatureSet;
import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Constant;
import org.codemancer.cpudl.expr.Register;
import org.codemancer.cpudl.expr.Memory;
import org.codemancer.cpudl.expr.Fragment;
import org.codemancer.cpudl.expr.Prefix;

@RunWith(Parameterized.class)
public class CpuTest {
	Architecture arch;
	String cpuName;
	Type start;
	BitString code;
	String line;
	String[] fields;
	String[] preconds;
	String[] postconds;
	long pc;

	private static int parseHexValue(char c) {
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

	private static Expression parseLvalue(Context ctx, String s) {
		if (s.length() == 0) {
			throw new IllegalArgumentException("l-value in pre/post-condition is empty string");
		}
		if (s.charAt(0) == '[') {
			if (s.charAt(s.length() - 1) != ']') {
				throw new IllegalArgumentException("unmatched brackets in pre/post-condition");
			}
			long addr = Long.parseLong(s.substring(1, s.length() - 1), 16);
			return new Memory(null, new Constant(null, addr));
		} else {
			return Register.make(ctx.getArchitecture(), s);
		}
	}

	private static Expression parseRvalue(String s) {
		if (s.length() == 0) {
			throw new IllegalArgumentException("r-value in pre/post-condition is empty string");
		}
		return new Constant(null, Long.parseLong(s, 16));
	}

	public CpuTest(Architecture arch, String cpuName, String line, String setup, Long pc, Integer width) throws Exception {
		this.arch = arch;
		this.cpuName = cpuName;
		this.start = arch.getStart();

		this.postconds = new String[0];
		this.line = line;
		int f = line.indexOf("\t?");
		if (f != -1) {
			this.postconds = line.substring(f + 2, line.length()).split(",");
			line = line.substring(0, f);
		}
		this.fields = line.split("\t");

		this.code = new ShortBitString();
		String codeField = this.fields[0];
		int mask = (arch.isBigEndian()) ? 0 : ((width >> 2) - 1);
		for (int i = 0; i < codeField.length(); ++i) {
			int v = parseHexValue(codeField.charAt(i ^ mask));
			code = code.concat(new ShortBitString(v, 4, arch.isBigEndian()));
		}
		code = code.concat(new ShortBitString(0, 64, false));

		this.preconds = (setup.isEmpty()) ? new String[0] : setup.split(",");
		this.pc = pc;
	}

	@Test
	public void disassemble() throws Exception {
		Style style = arch.getStylesheet().getStyle(null);
		BitReader reader = new BitStringReader(code);
		List<BitReader> readers = new ArrayList<BitReader>();
		readers.add(reader);
		FeatureSet features = arch.getFeatureSet(cpuName);
		assertTrue(features != null);

		Expression expr;
		try {
			expr = start.decode(readers, features);
			if (expr != null) {
				expr = expr.resolveReferences(null, null);
			}
		} catch (Exception ex) {
			System.err.printf("!%s (%s)\n", line, ex.getMessage());
			throw ex;
		}

		boolean shouldDecode = (fields.length > 1);
		boolean didDecode = (expr != null) && !(expr instanceof Prefix);
		if (shouldDecode) {
			if (!didDecode) {
				System.err.printf("-%s\n", line);
			}
		} else {
			if (didDecode) {
				try {
					System.err.printf("+%s", line);
					for (int i = 0; i != start.getPieceCount(); ++i) {
						System.err.printf("\t%s", start.unparse(i, expr));
					}
				} catch (Exception ex) {
					System.err.printf("(exception caught)");
				}
				System.err.println();
			}
		}
		assertTrue(didDecode == shouldDecode);
		if (!shouldDecode) return;

		long nextPc = pc + (reader.tell() / 8);
		Map<String, Expression> registers = new HashMap<String, Expression>();
		registers.put("PC", new Constant(null, pc));
		registers.put("PC+", new Constant(null, nextPc));
		expr = expr.resolveRegisters(registers).simplify();

		for (int i = 0; i != start.getPieceCount(); ++i) {
			String asm = start.unparse(i, expr);
			if (i + 1 < fields.length) {
				assertEquals(fields[i + 1], asm);
			} else {
				assertEquals(new String(), asm);
			}
		}
	}

	@Parameters
	public static Collection<Object[]> getParameters() throws Exception {
		Collection<Object[]> params = new ArrayList<Object[]>();
		String setup = new String();
		Long pc = new Long(0);
		Integer width = new Integer(8);
		File[] testFiles = new File("testdata/cpus").listFiles();
		for (int i = 0; i != testFiles.length; ++i) {
			File testFile = testFiles[i];
			String fileName = testFile.getName();
			if (fileName.endsWith(".test")) {
				String cpuName = fileName.substring(0, fileName.length() - 5);
				Architecture arch = Architecture.makeArchitecture(cpuName);
				String testDataPath = "testdata/cpus/" + cpuName + ".test";
				BufferedReader reader = new BufferedReader(new FileReader(testDataPath));
				String line = null;
				while ((line = reader.readLine()) != null) {
					if (line.length() == 0) continue;
					switch (line.charAt(0)) {
					case ';':
						continue;
					case '!':
						setup = line.substring(1, line.length());
						continue;
					case '+':
						width = Integer.parseInt(line.substring(1, line.length()));
						if ((width & 7) != 0) {
							throw new IllegalArgumentException("instruction width not a multiple of 8");
						}
						continue;
					case '@':
						pc = Long.parseLong(line.substring(1, line.length()), 16);
						continue;
					default:
						params.add(new Object[] {arch, cpuName, line, setup, pc, width});
					}
				}
			}
		}
		return params;
	}
}
