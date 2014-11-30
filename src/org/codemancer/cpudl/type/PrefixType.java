// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.type;

import java.util.List;
import org.w3c.dom.Element;

import org.codemancer.cpudl.BitReader;
import org.codemancer.cpudl.FeatureSet;
import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.expr.Expression;
import org.codemancer.cpudl.expr.Prefix;

/** A class to represent an instruction prefix. */
public class PrefixType extends ConstantType {
	/** The name of the feature enabled by this prefix. */
	private final String featureName;

	/** Construct prefix type from XML.
	 * @param ctx the context of this type
	 * @param element this type as an XML element
	 */
	public PrefixType(Context ctx, Element element) throws CpudlParseException {
		super(ctx, element);
		featureName = Context.parseStringAttribute("name", element);
	}

	/** Get feature name.
	 * @return the name of the feature enabled by this prefix.
	 */
	public String getFeatureName() {
		return featureName;
	}

	public final Expression decode(List<BitReader> readers, FeatureSet features) {
		Expression expr = super.decode(readers, features);
		if (expr != null) {
			expr = new Prefix(this);
		}
		return expr;
	}
}
