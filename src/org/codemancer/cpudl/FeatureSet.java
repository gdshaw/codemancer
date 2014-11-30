// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

/** A class to represent a set of features.
 * The current implementation is limited to a maximum of 64 features
 * per architecture, however this could be extended if required.
 */
public class FeatureSet {
	/** The architecture to which the features refer. */
	private final Architecture arch;

	/** The features which are members, as a bitmap. */
	private long features = 0;

	/** Construct feature set.
	 * @param arch the architecture to which the features refer
	 */
	public FeatureSet(Architecture arch) {
		this.arch = arch;
	}

	/** Copy construct feature set.
	 * @param that the feature set to be copied
	 */
	public FeatureSet(FeatureSet that) {
		this.features = that.features;
		this.arch = that.arch;
	}

	/** Add a feature to this feature set.
	 * @param name the name of the feature to be added
	 */
	public final void add(String name) {
		int id = arch.getFeatureId(name);
		features |= (1L << id);
	}

	/** Test whether this set contains all members of another set.
	 * @param that the feature set to be compared
	 * @return true if it contains all members, otherwise false
	 */
	public final boolean containsAll(FeatureSet that) {
		return (that.features & ~features) == 0;
	}

	/** Test whether this set contains any members of another set.
	 * @param that the feature set to be compared
	 * @return true if it contains any members, otherwise false
	 */
	public final boolean containsAny(FeatureSet that) {
		return (that.features & features) != 0;
	}
}
