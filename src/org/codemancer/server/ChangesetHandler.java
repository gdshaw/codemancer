// This file is part of Codemancer.
// Copyright 2015 Graham Shaw.
// All rights reserved.

package org.codemancer.server;

import java.math.BigInteger;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import org.codemancer.db.Database;
import org.codemancer.db.Revision;

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

			// Open database then wait for specified revision
			// if it is not already available.
			Database db = server.open(dbName);
			Revision revision = db.getRevision();
			revision.await(minRev);

			Headers h = t.getResponseHeaders();
			h.set("Content-Type", "application/json");
			t.sendResponseHeaders(200, 0);
			OutputStream os = t.getResponseBody();
			os.write("({\"rev\":".getBytes());
			os.write(new Long(db.getRevision().get()).toString().getBytes());
			os.write("})".getBytes());
			os.close();
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
