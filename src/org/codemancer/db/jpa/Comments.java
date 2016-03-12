// This file is part of Codemancer.
// Copyright 2014-2016 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.db.jpa;

import java.util.List;
import java.util.ArrayList;
import javax.persistence.EntityManager;

/** A class to represent the collection of comments in a Codemancer database. */
class Comments implements org.codemancer.db.Comments {
	/** The entity manager for the database. */
	private final EntityManager em;

	/** Construct collection of comments.
	 * @param em the entity manager for the database
	 */
	protected Comments(EntityManager em) {
		this.em = em;
	}

	public final Comment make(long minRev, long maxRev, long addr, boolean auto, String content) {
		Comment comment = new Comment(minRev, maxRev, addr, auto, content);
		em.persist(comment);
		return comment;
	}

	public final List<org.codemancer.db.Comment> get(long addr) {
		List<Comment> comments = em.createQuery(
			"FROM Comment WHERE addr = :addr", Comment.class)
			.setParameter("addr", addr)
			.getResultList();
		return new ArrayList<org.codemancer.db.Comment>(comments);
	}
}
