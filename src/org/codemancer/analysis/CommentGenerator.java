// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.analysis;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.ArrayDeque;
import java.io.IOException;
import javax.persistence.EntityManager;

import org.codemancer.loader.ObjectFile;
import org.codemancer.loader.ObjectFileReader;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Register;
import org.codemancer.cpudl.expr.Constant;
import org.codemancer.cpudl.type.Type;
import org.codemancer.cpudl.Architecture;
import org.codemancer.cpudl.FeatureSet;
import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.ShortBitString;
import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.BitStringReader;
import org.codemancer.db.Fact;
import org.codemancer.db.BasicBlock;
import org.codemancer.db.ExtendedBasicBlock;
import org.codemancer.db.Subroutine;
import org.codemancer.db.Comment;
import org.codemancer.db.Database;

/** A class for generating comments. */
public class CommentGenerator {
	/** The object file to be disassembled. */
	private ObjectFile obj;

	/** A reader for the object file. */
	private ObjectFileReader reader;

	/** A database corresponding to the object file. */
	private Database db;

	/** The entity manager for the database. */
	EntityManager em;

	/** The architecture to be used when disassembling. */
	private Architecture arch;

	/** The feature set to be used when disassembling. */
	private FeatureSet features;

	/** A queue of basis blocks waiting to be processed. */
	private Queue<BasicBlock> pendingBlocks = new ArrayDeque<BasicBlock>();

	/** Construct comment generator.
	 * @param obj the object file to be commented
	 * @param db the database corresponding to the object file
	 * @param arch the architecture
	 */
	public CommentGenerator(ObjectFile obj, Database db, Architecture arch) throws IOException {
		this.obj = obj;
		this.reader = new ObjectFileReader(obj);
		this.db = db;
		this.em = db.getEntityManager();
		this.arch = arch;
		this.features = new FeatureSet(arch);
	}

	/** Generate comments for a given basic block
	 * @param block the basic block to be commented
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 */
	private void comment(BasicBlock block, Register pc, List<Expression> links) {
		EntityManager em = db.getEntityManager();
		Type start = arch.getStart();

		// Initialise buffer.
		BitString buffer = new ShortBitString();
		reader.seek(block.getMinAddr());

		// Disassemble each instruction in the basic block.
		long addr = block.getMinAddr();
		while (addr <= block.getMaxAddr()) {
			// Fill/refill buffer.
			while ((buffer.length() < 64) && (reader.tell() <= block.getMaxAddr())) {
				byte newByte = reader.get();
				BitString newBits = new ShortBitString(newByte, 8, arch.isBigEndian());
				buffer = buffer.concat(newBits);
			}

			// Decode the next instruction.
			BitReader codeReader = new BitStringReader(buffer);
			List<BitReader> codeReaders = new ArrayList<BitReader>();
			codeReaders.add(codeReader);
			Expression instr = start.decode(codeReaders, features);
			if (instr == null) {
				break;
			}

			// Resolve references within instruction.
			instr = instr.resolveReferences(null, null);

			// Calculate the length of the instruction.
			long bitCount = codeReader.tell();
			if ((bitCount & 7) != 0) {
				throw new IllegalArgumentException("instruction not a whole number of bytes");
			}
			long byteCount = bitCount >> 3;

			// Evaluate the effect of this instruction.
			SsaStatePlayer state = new SsaStatePlayer(db, addr);
			instr.evaluate(state);
			String commentString = state.getComment();
			Comment comment = new Comment(0, -1, addr, true, commentString);
			em.persist(comment);

			// Advance the address to the next instruction.
			addr += byteCount;

			// Remove any bits which have been disassembled.
			buffer = buffer.substring(bitCount, buffer.length());
		}
	}

	/** Generate comments for the next unprocessed basic block.
	 * @param pc the program counter
	 * @param links a list of possible expressions for a subroutine return address
	 * @return true if all pending blocks have been processed, otherwise false
	 */
	public boolean commentNext(Register pc, List<Expression> links) {
		EntityManager em = db.getEntityManager();

		// If the pending blocks queue is empty then attempt to refill it.
		if (pendingBlocks.isEmpty()) {
			// Attempt to refill the blocks queue.
			pendingBlocks.addAll(db.getUnprocessedBasicBlocks(Fact.DONE_COMMENT_GENERATOR));

			// If the queue is still empty then stop because there is nothing to do.
			if (pendingBlocks.isEmpty()) return true;
		}

		// If the pending blocks queue is now non-empty then process one block.
		if (!pendingBlocks.isEmpty()) {
			BasicBlock block = pendingBlocks.remove();
			comment(block, pc, links);
			block.setProcessed(Fact.DONE_COMMENT_GENERATOR);
		}
		return false;
	}
}
