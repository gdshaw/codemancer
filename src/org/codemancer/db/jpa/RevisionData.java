// This file is part of Codemancer.
// Copyright 2016 Graham Shaw.
// All rights reserved.

package org.codemancer.db.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

/** A class to hold the persistent content of a Codemancer database revision. */
@Entity
public class RevisionData {
	/** The revision number. */
	@Id
	public long rev = 0;

	/** True if this revision has been committed, otherwise false. */
	public boolean committed = true;
}
