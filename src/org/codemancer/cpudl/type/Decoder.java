// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.List;
import java.util.ArrayList;

import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.expr.Expression;

/** A class for decoding a bit pattern to yield an instruction fragment. */
public class Decoder {
	/** A constant for indexing the branch corresponding to a bit value of either one or zero. */
	private static final int EITHER = 2;

	/** The number of branches in the decoder trie (zeros, ones and either). */
	private static final int NUM_BRANCHES = 3;

	/** A structure for assessing the merits of using a given bit number for decoding. */
	private static class BitInfo {
		/** The chunk number to which this structure refers. */
		private final int chunk;

		/** The chunk-relative bit number to which this structure refers. */
		private final int cindex;

		/** The fragment-relative bit number to which this structure refers. */
		private final int findex;

		/** The number of fragments that would be inserted into each branch of the decoder trie. */
		private final int[] counts;

		/** The minimum number of fragments that a trie node for this bit number would eliminate. */
		private int eliminates = 0;

		/** Construct bit_info structure.
		 * @param chunk the chunk number to which this structure refers
		 * @param cindex the chunk-relative bit number to which this structure refers
		 * @param findex the fragment-relative bit number to which this structure refers
		 * @param count the initial value to use for the fragment counts
		 */
		BitInfo(int chunk, int cindex, int findex, int count) {
			this.chunk = chunk;
			this.cindex = cindex;
			this.findex = findex;
			this.counts = new int[] {count, count, count};
		}

		/** Accumulate fragment into counts.
		 * @param frag the fragment to accumulate
		 */
		public final void accumulate(Type type) {
			int value = type.getFixedBit(chunk, cindex);
			counts[(value < 0) ? EITHER : value] += 1;
		}

		/** Complete the accumulation of fragments.
		 * This function should be called once following the final call to accumulate,
		 * but before any calls to better_than.
		 */
		public final void complete() {
			eliminates = Math.min(counts[0], counts[1]);
		}

		/** Test whether this bit is a better choice than another bit.
		 * @param other the other bit, against which this should be compared
		 * @return true if this bit is a better choice, otherwise false
		 */
		boolean betterThan(BitInfo other) {
			if ((counts[EITHER] < other.counts[EITHER]) && (eliminates > 0)) return true;
			if ((counts[EITHER] == other.counts[EITHER]) && (eliminates > other.eliminates)) return true;
			return false;
		}
	}

	/** The chunk number to be inspected by this decoder. */
	private final int chunk;

	/** The chunk-relative bit index to be inspected by this decoder. */
	private final int cindex;

	/** The fragment-relative bit index to be inspected by this decoder. */
	private final int findex;

	/** Branches to follow depending on the value of the inspected bit.
	 * Branches 0 and 1 lead to instruction fragments for which the
	 * inspected bit must have a value of 0 or 1 respectively. Branch 2
	 * leads to instruction fragments for which the inspected bit is
	 * capable of having a value of 0 or 1.
	 */
	Decoder[] branches = new Decoder[NUM_BRANCHES];

	/** A list of types that cannot be distinguished further using the trie. */
	ArrayList<Type> types = new ArrayList<Type>();

	/** Construct a decoder for a given list of types.
	 * @param types the list of instruction fragments
	 * @param chunkCount the number of chunks that the decoder will handle
	 */
	Decoder(List<Type> types, int chunkCount) {
		if (types.size() == 0) {
			throw new IllegalArgumentException("cannot decode an empty list of types");
		}

		// Choose the bit that most effectively partitions the fragment list.
		// The choice made here will affect the efficiency of the decoder, but
		// should not detract from its correctness. Current policy is to first
		// minimise backtracking, then maximise the number of fragments
		// eliminated at this level of the trie.
		int k = 0;
		BitInfo bestBit = new BitInfo(0, 0, 0, Integer.MAX_VALUE);
		for (int i = 0; i != chunkCount; ++i) {
			// Determine the width of the shortest fixed-width region for the
			// current chunk of the fragment list. This acts as a limit on the
			// bits that can be examined at the current level of the trie.
			// (It does not limit what can be examined at lower levels.)
			long minFixedWidth = Integer.MAX_VALUE;
			for (Type type: types) {
				long fixedWidth = type.getFixedWidth(i);
				if (fixedWidth < minFixedWidth) {
					minFixedWidth = fixedWidth;
				}
			}

			// Iterate over bits that are within the fixed-width region
			// of all fragments.
			for (int j = 0; j != minFixedWidth; ++j) {
				BitInfo currentBit = new BitInfo(i, j, k++, 0);
				for (Type type: types) {
					currentBit.accumulate(type);
				}
				currentBit.complete();
				if (currentBit.betterThan(bestBit)) {
					bestBit = currentBit;
				}
			}
		}

		// Record the location of the bit that has been chosen to partition
		// the fragment list.
		chunk = bestBit.chunk;
		cindex = bestBit.cindex;
		findex = bestBit.findex;

		if (bestBit.eliminates == 0) {
			// If inspecting that bit would not eliminate any fragments then make this a leaf node.
			for (Type type: types) {
				this.types.add(type);
			}
		} else {
			// Otherwise, partition the fragment list into three sub-lists:
			// one for each possible value of the bit, and one for if it could
			// take either value.
			ArrayList<ArrayList<Type>> listBranches = new ArrayList<ArrayList<Type>>();
			for (int i = 0; i != NUM_BRANCHES; ++i) {
				listBranches.add(new ArrayList<Type>());
			}

			for (Type type: types) {
				int value = type.getFixedBit(chunk, cindex);
				listBranches.get((value < 0) ? EITHER : value).add(type);
			}

			// Create decoders for any of the lists that are non-empty.
			for (int i = 0; i != listBranches.size(); ++i) {
				if (!listBranches.get(i).isEmpty()) {
					branches[i] = new Decoder(listBranches.get(i), chunkCount);
				}
			}
		}
	}

	/** Attempt to decode a collection of bit sequences.
	 * This function has the same semantics as Type.decode.
	 * @param readers sources of bits, one for each chunk
	 * @return an expression corresponding to the bit sequences, or null if they did not match
	 */
	Expression decode(List<BitReader> readers) {
		// Record the positions of the bit readers to allow them to be reset.
		long positions[] = new long[readers.size()];
		for (int i = 0; i != readers.size(); ++i) {
			positions[i] = readers.get(i).tell();
		}

		// If this is a leaf node then try matching each of the listed candidates.
		if (!types.isEmpty()) {
			for (Type type: types) {
				Expression expr = type.decode(readers);
				if (expr != null) {
					return expr;
				}
				for (int i = 0; i != readers.size(); ++i) {
					readers.get(i).seek(positions[i]);
				}
			}
		}

		// For interior nodes, first isolate the bit to be extracted and follow
		// the relevant branch.
		int bit = readers.get(chunk).peek(cindex);
		if (branches[bit] != null) {
			Expression expr = branches[bit].decode(readers);
			if (expr != null) {
				return expr;
			}
			for (int i = 0; i != readers.size(); ++i) {
				readers.get(i).seek(positions[i]);
			}
		}

		// If nothing was found on the branch above then backtrack and try the
		// either-value branch instead.
		if (branches[EITHER] != null) {
			Expression expr = branches[EITHER].decode(readers);
			if (expr != null) {
				return expr;
			}
			for (int i = 0; i != readers.size(); ++i) {
				readers.get(i).seek(positions[i]);
			}
		}

		// If nothing found on either of the branches above then return null
		// to indicate failure.
		return null;
	}
}
