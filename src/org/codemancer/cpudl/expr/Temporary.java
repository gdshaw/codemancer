// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.cpudl.expr;

import java.util.Map;
import java.util.HashMap;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.codemancer.cpudl.Context;
import org.codemancer.cpudl.State;
import org.codemancer.cpudl.Style;
import org.codemancer.cpudl.CpudlParseException;
import org.codemancer.cpudl.type.Type;

/** An expression class to represent a reference to a temporary value.
 * Temporary values are local to the context in which they are defined.
 * Within a given context, the name of each temporary must be unique.
 * Temporaries in different contexts are allowed to have the same name
 * but different types and values.
 */
public class Temporary extends Expression {
	/** The lowest unique ID which has not yet been assigned. */
	private static long nextId = 0;

	/** A unique ID for this temporary value. */
	private final long id;

	/** The name of this temporary value. */
	private final String name;

	/** Construct reference to temporary value
	 * @param type the required type of this temporary value
	 * @param name the name of this temporary value
	 */
	public Temporary(Type type, String name) {
		super(type);
		this.id = nextId++;
		this.name = name;
	}

	/** Get unique ID.
	 * @return the unique ID for this temporary value
	 */
	public final long getId() {
		return id;
	}

	/** Get name.
	 * @return the name of this temporary value
	 */
	public final String getName() {
		return name;
	}

	public String unparse(Style style) {
		return name;
	}

	public void assign(State state, Expression value) {
		state.put(this, value);
	}

	public Expression evaluate(State state) {
		Expression value = state.get(this);
		if (value != null) return value;
		return this;
	}

	/** Make temporary reference from XML element.
	 * @param ctx the context of this expression
	 * @param element the temporary value reference as XML
	 * @return the temporary value reference as an object
	 */
	public static Temporary make(Context ctx, Element element) throws CpudlParseException {
		String name = element.getAttribute("name");
		if (name == null) {
			throw new CpudlParseException(element, "temporary name not specified");
		}

		Expression value = null;
		Node child = element.getFirstChild();
		while (child != null) {
			Expression operand = ctx.makeExpression(child);
			if (operand != null) {
				if (value == null) {
					value = operand;
				} else {
					value = new Sequence(operand.getType(), value, operand);
				}
			}
			child = child.getNextSibling();
		}

		if (value != null) {
			// This is a defining instance of the temporary.
			// Create a temporary object with a new unique ID
			// and register it with the current context.
			if (ctx.getTemporary(name) != null) {
				throw new CpudlParseException(element, "redefinition of temporary '" + name + "'");
			}
			Temporary temporary = new Temporary(value.getType(), name);
			ctx.registerTemporary(temporary);
			return temporary;
		} else {
			// This is a reference to a previously-defined temporary.
			Temporary temporary = ctx.getTemporary(name);
			if (temporary == null) {
				throw new CpudlParseException(element, "undefined temporary '" + name + "'");
			}
			return temporary;
		}
	}
}
