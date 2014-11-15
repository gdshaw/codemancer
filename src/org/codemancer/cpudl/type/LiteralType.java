// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.expr.Expression;

import org.w3c.dom.Element;

/** A class to represent an instruction fragment that has a fixed
 * representation in assembly language. */
public class LiteralType extends Type {
	/** The piece of assembly language that is matched by this fragment. */
	private String content;

	/** Construct literal type from XML.
	 * @param ctx the context of this type
	 * @param element this type as an XML element
	 */
	public LiteralType(Context ctx, Element element) {
		content = element.getTextContent();
	}

	public final int getPieceCount() {
		return 1;
	}

	public String unparse(int piece, Expression expr) {
		if (piece != 0) {
			throw new IllegalArgumentException("invalid piece number");
		}
		return content;
	}
}
