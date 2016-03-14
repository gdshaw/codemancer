// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.test;

import java.util.List;
import java.util.ArrayList;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.codemancer.cpudl.Architecture;
import org.codemancer.cpudl.expr.Register;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.loader.ObjectFile;
import org.codemancer.loader.ObjectFileFactory;
import org.codemancer.loader.Symbol;
import org.codemancer.db.Database;
import org.codemancer.db.Reference;
import org.codemancer.db.Line;
import org.codemancer.db.BasicBlock;
import org.codemancer.db.ExtendedBasicBlock;
import org.codemancer.db.Subroutine;
import org.codemancer.analysis.IterativeDisassembler;
import org.codemancer.analysis.BasicBlockDetector;
import org.codemancer.analysis.ExtendedBasicBlockDetector;
import org.codemancer.analysis.SubroutineDetector;
import org.codemancer.analysis.SsaMapper;
import org.codemancer.analysis.CommentGenerator;

class Analyse {
	public static final void main(String args[]) throws Exception {
		String projName = args[0];
		String architectureName = args[1];
		String imagePathname = args[2];

		// Parse description for required architecture.
		Architecture arch = Architecture.makeArchitecture(architectureName);

		// Open connection to database.
		String dbUrl = "jdbc:derby:" + projName + ";create=true";
		Database db = new org.codemancer.db.jpa.Database(dbUrl);

		// Open object file.
		RandomAccessFile file = new RandomAccessFile(imagePathname, "r");
		ByteBuffer image = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
		ObjectFile obj = ObjectFileFactory.make(image);
		if (obj == null) {
			System.err.println("Failed to load object file.");
			System.exit(1);
		}

		// Create external reference for each symbol in object file.
		for (Symbol symbol: obj.getSymbols()) {
			if (symbol.isCode()) {
				Reference ref = db.getReferences().make(-1, symbol.getValue(), false, false, true, true);
			}
		}
		db.getNextRevision().commit();

		// Run iterative disassembler.
		System.err.printf("Starting iterative disassembler.\n");
		IterativeDisassembler disasm = new IterativeDisassembler(obj, db, arch);
		Register pc = Register.make(arch, "PC");
		List<Expression> links = new ArrayList<Expression>();
		while (!disasm.process(pc, links));
		db.getNextRevision().commit();
		System.err.printf("Iterative disassembly complete.\n");
		System.err.printf("%d instructions disassembled.\n" ,db.getLines().count(db.getCurrentRevision().get()));

		// Run basic block detector.
		System.err.printf("Starting basic block detector.\n");
		BasicBlockDetector bbDetector = new BasicBlockDetector(obj, db, arch);
		while (!bbDetector.detectNext(pc, links));
		db.getNextRevision().commit();
		System.err.printf("Basic block detection complete.\n");
		System.err.printf("%d blocks detected.\n", db.getBasicBlocks().count(db.getCurrentRevision().get()));

		// Run extended basic block detector.
		System.err.printf("Starting extended basic block detector.\n");
		ExtendedBasicBlockDetector ebbDetector = new ExtendedBasicBlockDetector(obj, db, arch);
		while (!ebbDetector.detectNext(pc, links));
		db.getNextRevision().commit();
		System.err.printf("Extended basic block detection complete.\n");
		System.err.printf("%d extended basic blocks detected.\n", db.getExtendedBasicBlocks().count(db.getCurrentRevision().get()));

		// Run subroutine detector.
		System.err.printf("Starting subroutine detector.\n");
		SubroutineDetector subDetector = new SubroutineDetector(obj, db, arch);
		while (!subDetector.detectNext(pc, links));
		db.getNextRevision().commit();
		System.err.printf("Subroutine detection complete.\n");
		System.err.printf("%d subroutines detected.\n", db.getSubroutines().count(db.getCurrentRevision().get()));

		// Run register tracer.
		System.err.printf("Starting register tracing.\n");
		SsaMapper mapper = new SsaMapper(obj, db, arch);
		while (!mapper.mapNext(pc, links));
		db.getNextRevision().commit();
		System.err.printf("Register tracing complete.\n");

		// Run comment generator.
		System.err.printf("Starting comment generation.\n");
		CommentGenerator commenter = new CommentGenerator(obj, db, arch);
		while (!commenter.commentNext(pc, links));
		db.getNextRevision().commit();
		System.err.printf("Comment generation complete.\n");
	}
}
