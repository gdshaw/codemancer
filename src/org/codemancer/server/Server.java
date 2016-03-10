// This file is part of Codemancer.
// Copyright 2015 Graham Shaw.
// All rights reserved.

package org.codemancer.server;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

import org.codemancer.db.Database;

/** The main class for running a Codemancer server. */
public class Server {
	/** A cache of open databases, indexed by name. */
	private HashMap<String, Database> databases = new HashMap<String, Database>();

	/** Start accepting HTTP requests.
	 * @param port the TCP port number on which to accept requests
	 */
	public final void start(int port) throws IOException {
		// Initialise MIME types map for HTTP server.
		Map<String, String> mimeTypes = new HashMap<String, String>();
		mimeTypes.put("html", "text/html");
		mimeTypes.put("css", "text/css");
		mimeTypes.put("js", "application/javascript");
		mimeTypes.put("gif", "image/gif");

		// Create and run HTTP server.
		HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
		FileHandler fileHandler = new FileHandler("www", mimeTypes);
		ChangesetHandler changesetHandler = new ChangesetHandler(this);
		httpServer.createContext("/", fileHandler);
		httpServer.createContext("/changeset.json", changesetHandler);
		httpServer.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
		httpServer.start();
	}

	/** Obtain a Database object for an existing database.
	 * An existing Database object may be reused if one has been cached.
	 * @param dbName the name of the required database
	 * @return the database
	 */
	public final Database open(String dbName) throws Exception {
		Database db = databases.get(dbName);
		if (db == null) {
			String dbUrl = "jdbc:derby:" + dbName;
			db = new org.codemancer.db.jpa.Database(dbUrl);
			databases.put(dbName, db);
		}
		return db;
	}

	public static void main(String[] args) {
		try {
			// Construct and start server.
			// If no port specified then default to port 80.
			int port = 80;
			if (args.length > 0) {
				port = Integer.parseInt(args[0]);
			}
			Server server = new Server();
			server.start(port);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
