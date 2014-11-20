// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.expr.Expression;

import org.w3c.dom.Element;

/** An instruction fragment class for selectively permitting or requiring
 * whitespace in assembly language. */
public class Whitespace extends Type {
	/** The style to be used for this whitespace. */
	private final Style style;

	/** Construct whitespace from XML.
	 * @param ctx the context of this type
	 * @param element this type as an XML element
	 */
	public Whitespace(Context ctx, Element element) {
		this.style = ctx.getStylesheet().getStyle(element.getAttribute("class"));
	}

	public final int getPieceCount() {
		return 1;
	}

	public String unparse(int piece, Expression expr) {
		if (piece != 0) {
			throw new IllegalArgumentException("invalid piece number");
		}
		return style.get("whitespace", "");
	}
}
