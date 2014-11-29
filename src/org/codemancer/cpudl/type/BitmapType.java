// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.BitString;
import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.ShortBitString;
import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.FeatureSet;
import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Constant;
import org.codemancer.cpudl.expr.Sequence;
import org.codemancer.cpudl.expr.Fragment;

/** A class to represent an instruction fragment that is interpreted as a bitmap
 * controlling the membership of an argument list.
 */
public class BitmapType extends Type {
	/** A class for recording information about members of this bitmap type. */
	protected static class BitInfo {
		/** The position of this bit within the bitmap. */
		public long index;

		/** A bit mask corresponding to the position. */
		public long mask;

		/** The name associated with this bit. */
		public String name;

		/** The effect of this bit when set. */
		public Expression effect;

		/** Construct bit information.
		 * @param index the position of this bit within the bitmap
	 	 * @param name the name associated with this bit
		 * @param effect the effect of this bit when set
		 */
		public BitInfo(long index, String name, Expression effect) {
			this.index = index;
			this.mask = 1L << index;
			this.name = name;
			this.effect = effect;
		}
	}

	/** The members of this bitmap, indexed by position. */
	private final List<BitInfo> members = new ArrayList<BitInfo>();

	/** The effects associated with this bitmap. */
	private Map<String, Expression> effects = new HashMap<String, Expression>();

	/** The parameter name to use for recording the bit index, or null if not recorded. */
	private String indexName;

	/** The parameter name to use for recording the bit count, or null if not recorded. */
	private String countName;

	/** The parameter name to use for recording the total bit count, or null if not recorded. */
	private String totalName;

	/** The parameter name to use for recording the effect of a bit, or null if not recorded. */
	private String effectName;

	/** The style to be used for this integer type. */
	private final Style style;

	/** Construct bitmap type.
	 * @param ctx the context of this type
	 * @param element this type as an XML element
	 */
	public BitmapType(Context ctx, Element element) throws CpudlParseException {
		indexName = element.getAttribute("index");
		if (indexName.length() == 0) {
			indexName = null;
		}
		countName = element.getAttribute("count");
		if (countName.length() == 0) {
			countName = null;
		}
		totalName = element.getAttribute("total");
		if (totalName.length() == 0) {
			totalName = null;
		}
		effectName = element.getAttribute("effect");
		if (effectName.length() == 0) {
			effectName = null;
		}

		Node child = element.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element childElement = (Element)child;
				String tagName = childElement.getTagName();
				if (tagName.equals("bit")) {
					parseMember(ctx, childElement);
				} else if (tagName.equals("effect")) {
					parseEffect(ctx, childElement);
				}
			}
			child = child.getNextSibling();
		}
		if (!effects.containsKey("foreach")) {
			throw new CpudlParseException(element, "missing definition of 'foreach' effect");
		}

		this.style = ctx.getStylesheet().getStyle(element.getAttribute("class"));
	}

	/** Parse member.
	 * @param ctx the context of the bitmap
	 * @param element the required type as an XML element
	 */
	private final void parseMember(Context ctx, Element element) throws CpudlParseException {
		String name = element.getAttribute("name");
		if (name.equals("")) {
			throw new CpudlParseException(element, "name of bitmap member not specified");
		}
		Expression effect = Sequence.make(ctx, element);
		members.add(new BitInfo(members.size(), name, effect));
	}

	/** Parse effect.
	 * @param ctx the context of this fragment
	 * @param element the effect element to be parsed
	 */
	private final void parseEffect(Context ctx, Element element) throws CpudlParseException {
		String name = element.getAttribute("name");
		if (name.equals("")) {
			throw new CpudlParseException(element, "effect name not specified");
		}
		if (effects.containsKey(name)) {
			throw new CpudlParseException(element, "duplicate definition of '" + name + "' effect");
		}
		effects.put(name, Sequence.make(ctx, element));
	}

	public final int getChunkCount() {
		return 1;
	}

	public final long getFixedWidth(int chunk) {
		if (chunk != 0) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return members.size();
	}

	public final int getFixedBit(int chunk, long index) {
		if (chunk != 0) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		if ((index < 0) || (index >= members.size())) {
			throw new IllegalArgumentException("invalid bit index");
		}
		return -1;
	}

	public final boolean isVariableWidth(int chunk) {
		if (chunk != 0) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		return false;
	}

	public final BitString encode(int chunk, Expression expr) {
		if (chunk != 0) {
			throw new IllegalArgumentException("invalid chunk number");
		}
		if (!(expr instanceof Constant)) {
			throw new IllegalArgumentException("not a bitmap constant");
		}
		Constant constExpr = (Constant)expr;
		long value = constExpr.getValue();
		return new ShortBitString(value, members.size());
	}

	public final Expression decode(List<BitReader> readers, FeatureSet features) {
		if (readers.size() != 1) {
			throw new IllegalArgumentException("incorrect number of chunks");
		}
		BitString bits = readers.get(0).read(members.size());
		long bitmap = bits.getBits(0, members.size());
		Fragment frag = new Fragment(this);
		frag.put("_bitmap", new Constant(null, bitmap));

		long total = 0;
		for (BitInfo member: members) {
			if ((bitmap & member.mask) != 0) {
				total += 1;
			}
		}

		Expression effect = null;
		long index = 0;
		long count = 0;
		for (BitInfo member: members) {
			if ((bitmap & member.mask) != 0) {
				Map<String, Expression> args = new HashMap<String, Expression>();
				if (indexName != null) {
					args.put(indexName, new Constant(null, index));
				}
				if (countName != null) {
					args.put(countName, new Constant(null, count));
				}
				if (totalName != null) {
					args.put(totalName, new Constant(null, total));
				}
				if (effectName != null) {
					args.put(effectName, member.effect);
				}

				Expression resolvedEffect = effects.get("foreach").resolveReferences(null, args);
				if (effect == null) {
					effect = resolvedEffect;
				} else {
					effect = new Sequence(null, effect, resolvedEffect);
				}
				count += 1;
			}
			index += 1;
		}
		frag.setEffect(effect);
		if (totalName != null) {
			frag.put(totalName, new Constant(null, total));
		}
		return frag;
	}

	public int getPieceCount() {
		return 1;
	}

	public String unparse(int piece, Expression expr) {
		if (piece != 0) {
			throw new IllegalArgumentException("invalid piece number");
		}
		if (!(expr instanceof Fragment)) {
			throw new IllegalArgumentException("not a bitmap fragment");
		}
		Fragment frag = (Fragment)expr;
		Expression bitmapExpr = frag.get("_bitmap");
		if (!(bitmapExpr instanceof Constant)) {
			throw new IllegalArgumentException("_bitmap member not a constant");
		}
		Constant bitmapConst = (Constant)bitmapExpr;
		long bitmap = bitmapConst.getValue();

		StringBuilder out = new StringBuilder();
		for (BitInfo member: members) {
			if ((bitmap & 1) != 0) {
				if (out.length() != 0) {
					out.append(style.get("separator", ","));
				}
				out.append(member.name);
			}
			bitmap = bitmap >> 1;
		}
		return out.toString();
	}
}
