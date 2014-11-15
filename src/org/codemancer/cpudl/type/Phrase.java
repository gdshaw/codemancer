// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.ArrayList;
import java.util.SortedMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.type.FragmentType.MemberInfo;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Fragment;

/** A class for recognising all or part of an assembly language instruction. */
public class Phrase {
	private static class PieceInfo {
		/** The name of the member to which this piece refers. */
		public final String name;

		/** The type to which this piece refers. */
		public final Type type;

		/** The piece number within the fragment type to which this piece refers. */
		public final int index;

		/** Construct piece information.
		 * @param name the name of this piece
		 * @param type the type of this piece
		 * @param index the piece number
		 */
		PieceInfo(String name, Type type, int index) {
			this.name = name;
			this.type = type;
			this.index = index;
		}
	}

	/** The pieces that are matched by this phrase, in the order they occur. */
	private final ArrayList<PieceInfo> pieces = new ArrayList<PieceInfo>();

	/** Construct phrase from XML.
	 * @param element this phrase as an XML element
	 * @param members the members of the fragment type to which this phrase belongs
	 */
	public Phrase(Element element, SortedMap<String, MemberInfo> members) throws CpudlParseException {
		Node child = element.getFirstChild();
		while (child != null) {
			if (child instanceof Element) {
				Element childElement = (Element)child;
				String tagName = childElement.getTagName();
				if (tagName.equals("literal")) {
					Type type = new LiteralType(childElement);
					MemberInfo member = new MemberInfo(type, 0);
					add(childElement, null, member);
				} else if (tagName.equals("ref")) {


					String name = childElement.getAttribute("name");
					if (name == null) {
						throw new CpudlParseException(childElement, "missing name attribute in ref element");
					}
					MemberInfo member = members.get(name);
					if (member == null) {
						throw new CpudlParseException(childElement, "undefined fragment member '" + name + "'");
					}
					add(childElement, name, member);
				}
			}
			child = child.getNextSibling();
		}
	}

	private void add(Element element, String name, MemberInfo member) throws CpudlParseException {
		if (member.piece >= member.type.getPieceCount()) {
			throw new CpudlParseException(element, "too many phrase references to fragment member");
		}
		PieceInfo pieceInfo = new PieceInfo(name, member.type, member.piece);
		pieces.add(pieceInfo);
		member.piece += 1;
	}

	/** Construct a piece of assembly language to match this phrase.
	 * @param expr the expression to be expressed as assembly language
	 * @return the phrase as a string
	 */
	String unparse(Expression expr) {
		if (!(expr instanceof Fragment)) {
			throw new IllegalArgumentException("fragment expected");
		}
		Fragment fragExpr = (Fragment)expr;

		String source = new String();
		for (PieceInfo pieceInfo: pieces) {
			source = source + pieceInfo.type.unparse(
				pieceInfo.index, fragExpr.get(pieceInfo.name));
		}
		return source;
	}
}
