// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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

public class Disassemble {
	/** Disassemble raw binary file from the command line.
	 * Usage: java org.codemancer.Disassemble <architecture> <pathname>
	 * The disassembled code is written to stdout.
	 */
	public static final void main(String args[]) throws Exception {
		String architectureName = args[0];
		String pathname = args[1];

		Architecture arch = Architecture.makeArchitecture(architectureName);
		Type start = arch.getStart();
		FeatureSet features = new FeatureSet(arch);

		RandomAccessFile file = new RandomAccessFile(pathname, "r");
		ByteBuffer image = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
		int addr = 0;
		while (addr < image.limit()) {
			BitString code = new ShortBitString();
			int j = addr + 8;
			if (j > image.limit()) j = image.limit();
			for (int i = addr; i < j; ++i) {
				code = code.concat(new ShortBitString(image.get(i), 8, arch.isBigEndian()));
			}
			BitReader codeReader = new BitStringReader(code);
			List<BitReader> codeReaders = new ArrayList<BitReader>();
			codeReaders.add(codeReader);

			Expression expr = start.decode(codeReaders, features);
			if (codeReader.tell() % 8 != 0) {
				System.out.printf("Error: instruction not a whole number of bytes\n");
				expr = null;
			}
			if (expr != null) {
				Map<String, Expression> registers = new HashMap<String, Expression>();
				registers.put("PC", new Constant(null, addr));
				expr = expr.resolveReferences(null, null);
				expr = expr.resolveRegisters(registers).simplify();
				System.out.printf("%04X\t%s\t%s\n", addr, start.unparse(0, expr), start.unparse(1, expr));
				addr += codeReader.tell() / 8;
			} else {
				System.out.printf("%04X\t.byte\t%02X\n", addr, image.get(addr));
				addr += 1;
			}
		}
	}
}
