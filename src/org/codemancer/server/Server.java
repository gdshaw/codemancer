// This file is part of Codemancer.
// Copyright 2015 Graham Shaw.
// All rights reserved.

package org.codemancer.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

/** The main class for running a Codemancer server. */
public class Server {
	/** Start accepting HTTP requests.
	 * @param port the TCP port number on which to accept requests
	 */
	public final void start(int port) throws IOException {
		// Create and run HTTP server.
		HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
		FileHandler fileHandler = new FileHandler("www");
		httpServer.createContext("/", fileHandler);
		httpServer.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
		httpServer.start();
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
