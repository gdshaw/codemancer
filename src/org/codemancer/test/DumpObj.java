// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.test;

import java.io.RandomAccessFile;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

import org.codemancer.loader.Symbol;
import org.codemancer.loader.Segment;
import org.codemancer.loader.ObjectFile;
import org.codemancer.loader.ObjectFileFactory;

class DumpObj {
	public static final void main(String args[]) throws Exception {
		PrintWriter out = new PrintWriter(System.out, true);
		RandomAccessFile file = new RandomAccessFile(args[0], "r");
		ByteBuffer image = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());

		ObjectFile objFile = ObjectFileFactory.make(image);
		if (objFile == null) {
			out.println("Failed to load object file.");
			return;
		}

		for (Map.Entry<Long, Segment> entry: objFile.getAddressMap().entrySet()) {
			out.printf("Mapped address: %08X\n", entry.getKey());
			Segment segment = entry.getValue();
			segment.dump(out);
			out.println();
		}
		for (Symbol symbol: objFile.getSymbols()) {
			symbol.dump(out);
		}
	}
}
