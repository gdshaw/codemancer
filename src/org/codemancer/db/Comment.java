// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

/** An interface to represent a comment. */
public interface Comment extends Fact {
	/** Get address.
	 * @return the address of the instruction to which this comment applies
	 */
	long getAddr();

	/** Check whether this is an autogenerated comment.
	 * @return true if autogenerated, otherwise false
	 */
	boolean getAuto();

	/** Get content.
	 * @return the content of this comment
	 */
	String getContent();
}
