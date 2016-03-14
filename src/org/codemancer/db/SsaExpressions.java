// This file is part of Codemancer.
// Copyright 2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db;

import java.util.List;

/** An interface to represent the collection of SSA expressions in a Codemancer database. */
public interface SsaExpressions {
	/** Make new SSA expression.
	 * @param subroutine the subroutine to which the SSA expression belongs
	 * @param name the name of the SSA expression
	 * @return the newly-created SSA expression
	 */
	SsaExpression make(org.codemancer.db.Subroutine subroutine, String name);

	/** Get SSA expression with a given name in a given subroutine.
	 * @param subroutine the subroutine containing the required expression
	 * @param name the name of the required expression
	 * @return the SSA expression
	 */
	SsaExpression get(Subroutine subroutine, String name);
}
