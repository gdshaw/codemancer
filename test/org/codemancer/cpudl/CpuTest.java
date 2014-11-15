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
import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.expr.Expression;

@RunWith(Parameterized.class)
public class CpuTest {
	Architecture arch;
	Type start;
	BitString code;
	String[] fields;

	public CpuTest(Architecture arch, String line) throws Exception {
		this.arch = arch;
		this.start = arch.getStart();
		this.fields = line.split("\t");
		this.code = new ShortBitString();

		String codeField = fields[0];
		for (int i = 0; i != codeField.length(); ++i) {
			char c = codeField.charAt(i);
			int v;
			if ((c >= '0') && (c <= '9')) {
				v = c - '0';
			} else if ((c >= 'A') && (c <= 'F')) {
				v = c - 'A' + 10;
			} else if ((c >= 'a') && (c <= 'f')) {
				v = c - 'a' + 10;
			} else {
				throw new Exception("non-hex digit in code field");
			}
			code = new ShortBitString(v, 4).concat(code);
		}
	}

	@Test
	public void disassemble() {
		BitReader reader = new BitStringReader(code);
		List<BitReader> readers = new ArrayList<BitReader>();
		readers.add(reader);

		Expression expr = start.decode(readers);
		assertTrue(expr != null);
		for (int i = 0; i != start.getPieceCount(); ++i) {
			String asm = start.unparse(i, expr);
			if (i + 1 < fields.length) {
				assertEquals(asm, fields[i + 1].replace(" ", ""));
			} else {
				assertEquals(asm, null);
			}
		}
	}

	@Parameters
	public static Collection<Object[]> getParameters() throws Exception {
		Collection<Object[]> params = new ArrayList<Object[]>();
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
					if (line.charAt(0) == '#') continue;
					params.add(new Object[] {arch, line});
				}
			}
		}
		return params;
	}
}
