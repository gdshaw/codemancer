// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import javax.persistence.EntityTransaction;

/** An interface to represent a Codemancer database. */
public interface Database {
	/** Get transaction object.
	 * @return a transaction object
	 */
	EntityTransaction getTransaction();

	/** Get current revision number.
	 * @return the object representing the current revision number
	 */
	Revision getRevision();

	/** Get collection of lines for this database.
	 * @return the collection of lines
	 */
	Lines getLines();

	/** Get collection of comments for this database.
	 * @return the collection of comments
	 */
	Comments getComments();

	/** Get collection of references for this database.
	 * @return the collection of references
	 */
	References getReferences();

	/** Get collection of basic blocks for this database.
	 * @return the collection of basic blocks
	 */
	BasicBlocks getBasicBlocks();

	/** Get collection of extended basic blocks for this database.
	 * @return the collection of extended basic blocks
	 */
	ExtendedBasicBlocks getExtendedBasicBlocks();

	/** Get collection of subroutines for this database.
	 * @return the collection of subroutines
	 */
	Subroutines getSubroutines();

	/** Get collection of SSA expressions for this database.
	 * @return the collection of SSA expressions
	 */
	SsaExpressions getSsaExpressions();

	/** Get collection of SSA mappings for this database.
	 * @return the collection of SSA mappings
	 */
	SsaMappings getSsaMappings();
}
