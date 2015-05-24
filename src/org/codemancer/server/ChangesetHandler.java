// This file is part of Codemancer.
// Copyright 2015 Graham Shaw.
// All rights reserved.

package org.codemancer.server;

import java.util.List;
import java.math.BigInteger;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import org.codemancer.db.Database;
import org.codemancer.db.Revision;
import org.codemancer.db.Line;

/** A class for supplying changesets to the client. */
public class ChangesetHandler implements HttpHandler {
	/** The Codemancer server. */
	private final Server server;

	/** Construct changeset request handler.
	 * @param server the Codemancer server
	 */
	public ChangesetHandler(Server server) {
		this.server = server;
	}

	public final void handle(HttpExchange t) throws IOException {
		try {
			// Parse query string.
			HttpQuery query = new HttpQuery(t);
			String dbName = query.get("db");
			long minRev = new BigInteger(query.get("minrev"), 10).longValue();
			long minAddr = new BigInteger(query.get("minaddr"), 16).longValue();
			long maxAddr = new BigInteger(query.get("maxaddr"), 16).longValue();

			// Open database then wait for specified revision
			// if it is not already available.
			Database db = server.open(dbName);
			Revision revision = db.getRevision();
			revision.await(minRev);

			StringBuilder response = new StringBuilder();
			response.append("({\"rev\":");
			response.append(new Long(revision.get()).toString());
			response.append(",\"lines\":[");

			List<Line> lines = db.getLines(minRev, revision.get(), minAddr, maxAddr);
			boolean firstLine = true;
			for (Line line: lines) {
				if (firstLine) {
					firstLine = false;
				} else {
					response.append(",");
				}
				response.append(line.asJSON());
			}
			response.append("]})");

			Headers h = t.getResponseHeaders();
			h.set("Content-Type", "application/json");
			t.sendResponseHeaders(200, response.length());
			PrintStream ps = new PrintStream(t.getResponseBody());
			ps.print(response.toString());
			ps.close();
		}
		catch (Exception ex) {
			// Request failed: reject with 500 error.
			Headers h = t.getResponseHeaders();
			h.set("Content-Type", "text/html");
			t.sendResponseHeaders(500, 0);
			PrintStream ps = new PrintStream(t.getResponseBody());
			ps.print("<h1>500 (Internal server error)</h1>\n");
			ps.print("<pre>");
			ex.printStackTrace(ps);
			ps.print("</pre>");
			ps.close();
		}
	}
}
