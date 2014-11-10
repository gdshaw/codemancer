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

import org.codemancer.loader.elf.ElfFile;
import org.codemancer.loader.elf.ElfSection;
import org.codemancer.loader.elf.ElfSegment;

class DumpElf {
	public static final void main(String args[]) throws Exception {
		PrintWriter out = new PrintWriter(System.err, true);
		RandomAccessFile file = new RandomAccessFile(args[0], "r");
		ByteBuffer image = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
		ElfFile elf = new ElfFile(image);
		elf.dump(out);
		List<ElfSection> sections = elf.getElfSections();
		for (short i = 0; i != sections.size(); ++i) {
			out.println();
			out.printf("Section: %d\n", i);
			sections.get(i).dump(out);
		}
		List<ElfSegment> segments = elf.getElfSegments();
		for (short i = 0; i != segments.size(); ++i) {
			out.println();
			out.printf("Segment: %d\n", i);
			segments.get(i).dump(out);
		}
	}
}
