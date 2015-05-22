// This file is part of Codemancer.
// Copyright 2015 Graham Shaw.
// All rights reserved.

package org.codemancer.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

/** The main class for running a Codemancer server. */
public class Server {
	/** Start accepting HTTP requests. */
	public final void start() throws IOException {
		// Create and run HTTP server.
		HttpServer httpServer = HttpServer.create(new InetSocketAddress(80), 0);
		FileHandler fileHandler = new FileHandler("www");
		httpServer.createContext("/", fileHandler);
		httpServer.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
		httpServer.start();
	}

	public static void main(String[] args) {
		try {
			Server server = new Server();
			server.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
