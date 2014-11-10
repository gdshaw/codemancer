// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.test;

import java.io.RandomAccessFile;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.codemancer.loader.aof.AofFile;
import org.codemancer.loader.aof.AofChunk;
import org.codemancer.loader.aof.AofHeaderChunk;
import org.codemancer.loader.aof.AofSymbolTableChunk;
import org.codemancer.loader.aof.AofArea;
import org.codemancer.loader.aof.AofSymbol;
import org.codemancer.loader.aof.AofRelocation;

class DumpAof {
	public static final void main(String args[]) throws Exception {
		PrintWriter out = new PrintWriter(System.err, true);
		RandomAccessFile file = new RandomAccessFile(args[0], "r");
		ByteBuffer image = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
		AofFile aof = new AofFile(image);
		AofHeaderChunk header = aof.getHeaderChunk();
		aof.dump(out);
		for (AofChunk chunk: aof.getChunks()) {
			out.println();
			chunk.dump(out);
		}
		for (AofArea area: aof.getHeaderChunk().getAofAreas()) {
			out.println();
			area.dump(out);
			out.println();
			for (AofRelocation rel: area.getAofRelocations()) {
				rel.dump(out);
			}
			if (area.getAofRelocations().size() == 0) {
				out.println("(no relocations)");
			}
		}
		out.println();
		for (AofSymbol symbol: aof.getSymbolTableChunk().getAofSymbols()) {
			symbol.dump(out);
		}
	}
}
