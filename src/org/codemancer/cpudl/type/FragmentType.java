// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Reference;
import org.codemancer.cpudl.expr.Constant;
import org.codemancer.cpudl.expr.Sequence;
import org.codemancer.cpudl.expr.Fragment;
import org.codemancer.cpudl.expr.Equality;

/** A class to represent a type of instruction fragment. */
public class FragmentType extends Type {
	protected static class MemberInfo {
		/** The type of this member. */
		public final Type type;

		/** The assembly buffer index for this member. */
		public final int buffer;

		/** The number of chunks that have been referenced from this member. */
		public int chunk;

		/** The number of pieces that have been referenced from this member. */
		public int piece;

		/** An expression for deducing the value of this member, or null if none available. */
		public Expression solution;

		/** Construct member information.
		 * @param type the type of this member
		 * @param buffer the assembly buffer index for this subfragment
		 */
		MemberInfo(Type type, int buffer) {
			this.type = type;
			this.buffer = buffer;
			this.chunk = 0;
			this.piece = 0;
			this.solution = null;
		}
	}

	/** The members of this fragment, indexed by name. */
	private final SortedMap<String, MemberInfo> members = new TreeMap<String, MemberInfo>();

	/** The patterns that are matched by this fragment, indexed by chunk number. */
	private final List<Pattern> patterns = new ArrayList<Pattern>();

	/** The phrases that are matched by this fragment, indexed by piece number. */
	private final List<Phrase> phrases = new ArrayList<Phrase>();

	/** The effect of this fragment. */
	private Expression effect = null;

	/** A list of constraints which instances of this type must satisfy. */
	private final List<Expression> constraints = new ArrayList<Expression>();

	/** The number of bitstring assembly buffers needed to decode this compound fragment. */
	private int bufferCount = 1;

	/** Construct fragment type from XML.
	 * @param ctx the context of this type
	 * @param element this fragment type as an XML element
	 */
	public FragmentType(Context ctx, Element element) throws CpudlParseException {
		Node child = element.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element childElement = (Element)child;
				String tagName = childElement.getTagName();
				if (tagName.equals("var")) {
					parseMember(ctx, childElement);
				} else if (tagName.equals("pattern")) {
					patterns.add(new Pattern(ctx, childElement, members));
				} else if (tagName.equals("phrase")) {
					phrases.add(new Phrase(ctx, childElement, members));
				} else if (tagName.equals("effect")) {
					parseEffect(ctx, childElement);
				} else if (tagName.equals("where")) {
					parseConstraints(ctx, childElement);
				}
			}
			child = child.getNextSibling();
		}

		// The following method selects the first constraint capable of providing
		// a solution for a missing fragment member without checking that any other
		// members referenced by the solution are available. It is therefore
		// unlikely to handle chains of dependencies correctly.
		for (Expression constraint: constraints) {
			if (constraint instanceof Equality) {
				Equality equality = (Equality)constraint;
				for (Map.Entry<String, MemberInfo> entry: members.entrySet()) {
					String name = entry.getKey();
					MemberInfo member = entry.getValue();
					Reference solveFor = new Reference(null, name, null);
					if (member.solution == null) {
						Expression solution = equality.getLhs().solve(solveFor, equality.getRhs());
						if (solution != null) member.solution = solution;
					}
					if (member.solution == null) {
						Expression solution = equality.getRhs().solve(solveFor, equality.getLhs());
						if (solution != null) member.solution = solution;
					}
				}
			}
		}
	}

	/** Parse member.
	 * @param ctx the context of this type
	 * @param element the member as an XML element
	 */
	private final void parseMember(Context ctx, Element element) throws CpudlParseException {
		String name = element.getAttribute("name");
		if (name.equals("")) {
			throw new CpudlParseException(element, "member name not specified");
		}
		Type type = ctx.makeChoice(element);
		members.put(name, new MemberInfo(type, bufferCount));
		bufferCount += 1;
	}

	/** Parse effect.
	 * @param ctx the context of this fragment
	 * @param element the effect element to be parsed
	 */
	private final void parseEffect(Context ctx, Element element) throws CpudlParseException {
		if (effect != null) {
			throw new CpudlParseException(element, "multiple <effect> elements in <fragment>");
		}
		effect = Sequence.make(element);
	}

	/** Parse constraints.
	 * @param ctx the context of this fragment
	 * @param element the constraint element to be parsed
	 */
	private final void parseConstraints(Context ctx, Element element) throws CpudlParseException {
		Node child = element.getFirstChild();
		while (child != null) {
			Expression constraint = Expression.make(child);
			if (constraint != null) {
				constraints.add(constraint);
			}
			child = child.getNextSibling();
		}
	}

	/** Deduce the values of members which have not yet been determined.
	 * @param frag the fragment on which to act
	 */
	public final void deduce(Fragment frag) {
		for (Map.Entry<String, MemberInfo> entry: members.entrySet()) {
			String name = entry.getKey();
			MemberInfo member = entry.getValue();
			if (frag.get(name) == null) {
				Expression value = member.solution.resolveReferences(frag, null);
				frag.put(name, value);
			}
		}
	}

	/** Check that constraints have been satisfied.
	 * @param frag the proposed fragment to be checked
	 * @return true if all constraints are satisfied, otherwise false
	 */
	boolean check(Fragment frag) {
		for (Expression constraint: constraints) {
			Expression result = constraint.resolveReferences(frag, null).simplify();
			if (!(result instanceof Constant)) return false;
			Constant constResult = (Constant)result;
			if (constResult.getValue() == 0) return false;
		}
		return true;
	}

	public final int getChunkCount() {
		return patterns.size();
	}

	public final long getFixedWidth(int chunk) {
		if (chunk >= patterns.size()) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return patterns.get(chunk).getFixedWidth();
	}

	public final int getFixedBit(int chunk, long index) {
		if (chunk >= patterns.size()) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return patterns.get(chunk).getFixedBit(index);
	}

	public final boolean isVariableWidth(int chunk) {
		if (chunk >= patterns.size()) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return patterns.get(chunk).isVariableWidth();
	}

	public Expression decode(List<BitReader> readers) {
		ArrayList<ArrayList<BitReader>> buffers = new ArrayList<ArrayList<BitReader>>(bufferCount);
		for (int i = 0; i != bufferCount; ++i) {
			buffers.add(new ArrayList<BitReader>());
		}

		Fragment frag = new Fragment(this);
		for (int i = 0; i != patterns.size(); ++i) {
			if (!patterns.get(i).decode(readers.get(i), buffers, frag)) {
				return null;
			}
		}
		frag.setEffect(effect);
		deduce(frag);
		if (!check(frag)) return null;
		return frag;
	}

	public int getPieceCount() {
		return phrases.size();
	}

	public String unparse(int piece, Expression expr) {
		if ((piece < 0) || (piece >= phrases.size())) {
			throw new IllegalArgumentException("invalid piece number");
		}
		return phrases.get(piece).unparse(expr);
	}

	public Expression getEffect() {
		return effect;
	}
}
