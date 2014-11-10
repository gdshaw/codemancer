// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.test;

import java.io.RandomAccessFile;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import org.codemancer.loader.coff.CoffFile;
import org.codemancer.loader.coff.CoffSection;
import org.codemancer.loader.coff.CoffSymbol;
import org.codemancer.loader.coff.CoffRelocation;

class DumpCoff {
	public static final void main(String args[]) throws Exception {
		PrintWriter out = new PrintWriter(System.err, true);
		RandomAccessFile file = new RandomAccessFile(args[0], "r");
		ByteBuffer image = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
		CoffFile coff = new CoffFile(image);
		coff.dump(out);
		List<CoffSection> sections = coff.getCoffSections();
		for (short i = 0; i != sections.size(); ++i) {
			CoffSection section = sections.get(i);
			out.println();
			out.printf("Section: %d\n", i);
			section.dump(out);
			out.println();
			List<CoffRelocation> relocations = section.getCoffRelocations();
			for (int j = 0; j != relocations.size(); ++j) {
				relocations.get(j).dump(out);
			}
			if (relocations.size() == 0) {
				out.println("(no relocations)");
			}
                }
		out.println();
		List<CoffSymbol> symbols = coff.getCoffSymbols();
		for (short i = 0; i != symbols.size(); ++i) {
			CoffSymbol sym = symbols.get(i);
			if (sym != null) {
				sym.dump(out);
			}
                }
	}
}
