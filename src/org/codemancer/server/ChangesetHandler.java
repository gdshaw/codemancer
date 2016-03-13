// This file is part of Codemancer.
// Copyright 2015-2016 Graham Shaw.
// All rights reserved.

package org.codemancer.server;

import java.util.List;
import java.util.Map;
import java.math.BigInteger;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import org.codemancer.db.AddressRangeSet;
import org.codemancer.db.Database;
import org.codemancer.db.Revision;
import org.codemancer.db.Line;
import org.codemancer.db.BasicBlock;
import org.codemancer.db.Subroutine;

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
			long minAreaRev = new BigInteger(query.get("arearev"), 10).longValue();
			long minCodeRev = new BigInteger(query.get("coderev"), 10).longValue();
			long minRev = Math.min(minAreaRev, minCodeRev);

			// Open database then wait for specified revision if it is not already available.
			Database db = server.open(dbName);
			db.getRevision(minRev).await();
			long curRev = db.getCurrentRevision().get();

			// Construct required address range, either from sub or from minaddr/maxaddr
			// parameters.
			AddressRangeSet allRanges = new AddressRangeSet();
			AddressRangeSet finalRanges = new AddressRangeSet();
			String subArg = query.get("sub");
			if (subArg != null) {
				// Get the subroutine object, both as it was at minCodeRev,
				// and as it is now at curRev.
				long entryAddr = new BigInteger(subArg, 16).longValue();
				Subroutine subBefore = db.getSubroutines().getStarting(entryAddr, minCodeRev);
				Subroutine subNow = db.getSubroutines().getStarting(entryAddr, curRev);

				// The subroutine need not have existing before, but if it
				// does not exist now then that is an error.
				if (subNow == null) {
					throw new Exception("subroutine not found");
				}

				// Add the basic blocks for both subroutines to the range set.
				if (subBefore != null) {
					for (BasicBlock block: db.getBasicBlocks().getMembersOf(subBefore)) {
						allRanges.add(block.getMinAddr(), block.getMaxAddr());
					}
				}
				for (BasicBlock block: db.getBasicBlocks().getMembersOf(subNow)) {
					allRanges.add(block.getMinAddr(), block.getMaxAddr());
					finalRanges.add(block.getMinAddr(), block.getMaxAddr());
				}
			} else {
				long minAddr = new BigInteger(query.get("minaddr"), 16).longValue();
				long maxAddr = new BigInteger(query.get("maxaddr"), 16).longValue();
				allRanges.add(minAddr, maxAddr);
				finalRanges.add(minAddr, maxAddr);
			}

			StringBuilder response = new StringBuilder();
			response.append("({\"rev\":");
			response.append(new Long(curRev).toString());

			response.append(",\"areas\":[");
			response.append("[\"sub\", \"subroutines\", [");

			Map<Long, Subroutine> subroutines = db.getSubroutines().getChanged(minAreaRev, curRev);
			boolean firstSubroutine = true;
			for (Map.Entry<Long, Subroutine> entry: subroutines.entrySet()) {
				if (firstSubroutine) {
					firstSubroutine = false;
				} else {
					response.append(",");
				}
				Long entryAddr = entry.getKey();
				Subroutine subroutine = entry.getValue();
				String json = (subroutine != null) ?
					String.format("[0x%08x, \"sub%08x\"]", entryAddr, entryAddr) :
					String.format("[0x%08x]", entryAddr);
				response.append(json);
			}
			response.append("]]]");

			response.append(",\"lines\":[");
			List<Line> lines = db.getLines().getChanges(minCodeRev, curRev, allRanges);
			boolean firstLine = true;
			for (Line line: lines) {
				if (firstLine) {
					firstLine = false;
				} else {
					response.append(",");
				}
				if (finalRanges.contains(line.getMinAddr())) {
					response.append(line.asJSON());
				} else {
					String json = String.format("[0x%08x]", line.getMinAddr());
					response.append(json);
				}
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
