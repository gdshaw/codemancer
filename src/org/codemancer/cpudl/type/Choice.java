// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.List;
import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.ShortBitString;
import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.FeatureSet;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Prefix;

/** A class to represent a choice from a collection of possible types. */
public class Choice extends Type {
	/** A class for associating a type with a priority and a set of features. */
	public static class TypeInfo {
		/** The type. */
		public final Type type;

		/** The priority associated with the type. */
		public final int priority;

		/** The features which must be enabled for this type to be available. */
		public final FeatureSet requiredFeatures;

		/** The features which must not be enabled for this type to be available. */
		public final FeatureSet forbiddenFeatures;

		/** Construct type information structure.
		 * @param type the type
		 * @param priority the priority
		 * @param features the required feature set
		 */
		TypeInfo(Type type, int priority, FeatureSet requiredFeatures, FeatureSet forbiddenFeatures) {
			this.type = type;
			this.priority = priority;
			this.requiredFeatures = new FeatureSet(requiredFeatures);
			this.forbiddenFeatures = new FeatureSet(forbiddenFeatures);
		}

		/** Test whether this can be simplified to a plain type without loss of information.
		 * @return true if can be simplified, otherwise false
		 */
		public final boolean isPlainType() {
			return (priority == 0) && requiredFeatures.isEmpty() && forbiddenFeatures.isEmpty();
		}
	}

	/** A structure for recording aggregate information about the patterns
	 * matched by the allowed types. */
	protected static class PatternInfo {
		/** The width of the initial fixed-width region for this chunk. */
		public long fixedWidth;

		/** Bits which could be zero. */
		public BitString couldBeZero;

		/** Bits which could be one. */
		public BitString couldBeOne;

		/** True if the pattern could be variable length.
		 * This can only be true for the pattern corresponding to the final chunk.
		 */
		public boolean variableWidth;

		/** Initialise pattern information structure.
		 * @param fixedWidth the width of the initial fixed-width region
		 * @param variableWidth true if the chunk could be variable width
		 */
		PatternInfo(long fixedWidth, boolean variableWidth) {
			this.fixedWidth = fixedWidth;
			this.couldBeZero = new ShortBitString(0, fixedWidth, false);
			this.couldBeOne = new ShortBitString(0, fixedWidth, false);
			this.variableWidth = variableWidth;
		}
	}

	/** The number of chunks for this collection of types. */
	private int chunkCount = -1;

	/** The number of pieces for this collection of types. */
	private int pieceCount = -1;

	/** The types from which the choice can be made. */
	private final ArrayList<Type> types = new ArrayList<Type>();

	/** Aggregate information about the patterns matched by the allowed types. */
	private final ArrayList<PatternInfo> patterns = new ArrayList<PatternInfo>();

	/** A decoder for this collection of types. */
	Decoder decoder;

	/** Construct choice from list of type information structures.
	 * @param types the possible types
	 */
	public Choice(List<TypeInfo> types) {
		for (TypeInfo info: types) {
			add(info);
		}
		decoder = new Decoder(types, patterns.size());
	}

	/** Add a type to this choice.
	 * @param info the type to be added
	 */
	private void add(TypeInfo info) {
		if (chunkCount == -1) {
			chunkCount = info.type.getChunkCount();
			patterns.ensureCapacity(chunkCount);
			for (int i = 0; i != chunkCount; ++i) {
				patterns.add(new PatternInfo(info.type.getFixedWidth(i), info.type.isVariableWidth(i)));
			}
		} else {
			if (info.type.getChunkCount() != chunkCount) {
				throw new IllegalArgumentException("chunk count mismatch");
			}
		}

		if (!(info.type instanceof PrefixType)) {
			if (pieceCount == -1) {
				pieceCount = info.type.getPieceCount();
			} else {
				if (info.type.getPieceCount() != pieceCount) {
					throw new IllegalArgumentException("piece count mismatch");
				}
			}
		}
		types.add(info.type);

		for (int i = 0; i != chunkCount; ++i) {
			long fixedWidth = info.type.getFixedWidth(i);
			if (fixedWidth != patterns.get(i).fixedWidth) {
				if (i + 1 != chunkCount) {
					throw new IllegalArgumentException("fixed width mismatch");
				} else {
					if (fixedWidth < patterns.get(i).fixedWidth) {
						patterns.get(i).fixedWidth = fixedWidth;
					}
					patterns.get(i).variableWidth = true;
				}
			}
			for (long j = 0; j != fixedWidth; ++j) {
				int fixedBit = info.type.getFixedBit(i, j);
				if (fixedBit != 1) {
					patterns.get(i).couldBeZero = patterns.get(i).couldBeZero.setBit(j, 1);
				}
				if (fixedBit != 0) {
					patterns.get(i).couldBeOne = patterns.get(i).couldBeOne.setBit(j, 1);
				}
			}
			patterns.get(i).variableWidth |= info.type.isVariableWidth(i);
		}
	}

	public final int getChunkCount() {
		return patterns.size();
	}

	public final long getFixedWidth(int chunk) {
		if (chunk >= patterns.size()) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return patterns.get(chunk).fixedWidth;
	}

	public final int getFixedBit(int chunk, long index) {
		if (chunk >= patterns.size()) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		boolean couldBeZero = (patterns.get(chunk).couldBeZero.getBit(index) != 0);
		boolean couldBeOne = (patterns.get(chunk).couldBeOne.getBit(index) != 0);
		if (couldBeZero && !couldBeOne) {
			return 0;
		} else if (couldBeOne && !couldBeZero) {
			return 1;
		} else {
			return -1;
		}
	}

	public final boolean isVariableWidth(int chunk) {
		if (chunk >= patterns.size()) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return patterns.get(chunk).variableWidth;
	}

	public Expression decode(List<BitReader> readers, FeatureSet features) {
		Expression expr = decoder.decode(readers, features);
		if (expr instanceof Prefix) {
			Prefix prefix = (Prefix)expr;
			FeatureSet childFeatures = new FeatureSet(features);
			childFeatures.add(prefix.getFeatureName());
			expr = decoder.decode(readers, childFeatures);
		}
		return expr;
	}

	public int getPieceCount() {
		return pieceCount;
	}

	public String unparse(int piece, Expression expr) {
		return expr.getType().unparse(piece, expr);
	}

	/** Add children of element to list of choices.
	 * @param ctx the context of the element
	 * @param element the parent of the child elements to be added
	 * @param priority the priority
	 * @param requiredFeatures the required feature set
	 * @param forbiddenFeatures the forbidden feature set
	 * @param types the list of types to be added to
	 */
	private static void addElement(Context ctx, Element element, int priority, FeatureSet requiredFeatures, FeatureSet forbiddenFeatures,
		List<TypeInfo> types) throws CpudlParseException {

		Node child = element.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element childElement = (Element)child;
				String tagName = childElement.getTagName();
				if (tagName.equals("priority")) {
					int childPriority = Context.parseIntegerAttribute("level", childElement);
					addElement(ctx, childElement, priority + childPriority, requiredFeatures, forbiddenFeatures, types);
				} else if (tagName.equals("require")) {
					String featureName = Context.parseStringAttribute("name", childElement);
					if (featureName.charAt(0) == '!') {
						featureName = featureName.substring(1);
						FeatureSet childForbiddenFeatures = new FeatureSet(forbiddenFeatures);
						childForbiddenFeatures.add(featureName);
						addElement(ctx, childElement, priority, requiredFeatures, childForbiddenFeatures, types);
					} else {
						FeatureSet childRequiredFeatures = new FeatureSet(requiredFeatures);
						childRequiredFeatures.add(featureName);
						addElement(ctx, childElement, priority, childRequiredFeatures, forbiddenFeatures, types);
					}
				} else {
					Type type = ctx.makeType(child);
					if (type != null) {
						types.add(new TypeInfo(type, priority, requiredFeatures, forbiddenFeatures));
					}
				}
			}
			child = child.getNextSibling();
		}
	}

	/** Make choice of types from children of XML element.
	 * If only one type is specified then it may be returned directly
	 * (as opposed to being wrapped within a Choice object).
	 * @param ctx the context of this element
	 * @param node the parent of the elements to be interpreted as types
	 * @return the type or choice of types
	 */
	public static Type make(Context ctx, Element element) throws CpudlParseException {
		List<TypeInfo> types = new ArrayList<TypeInfo>();
		addElement(ctx, element, 0, new FeatureSet(ctx.getArchitecture()), new FeatureSet(ctx.getArchitecture()), types);
		if (types.size() == 0) {
			throw new CpudlParseException(element, "type expected");
		} else if ((types.size() == 1) && types.get(0).isPlainType()) {
			return types.get(0).type;
		} else {
			return new Choice(types);
		}
	}
}
