// This file is part of Codemancer.
// Copyright 2015 Graham Shaw.
// All rights reserved.

package org.codemancer.server;

import java.io.File;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** An HTTP handler class for serving files from the filesystem. */
public class FileHandler implements HttpHandler {
	/** The root of the filesystem subtree to be served. */
	private final File root;

	/** Construct handler for serving files from the filesystem.
	 * The supplied root path is canonicalised before use.
	 * @param rootPath the root of the filesystem subtree to be served
	 * @throws FileNotFoundException if the root path does not exist.
	 */
	public FileHandler(String rootPath) throws FileNotFoundException {
		try {
			this.root = new File(rootPath).getCanonicalFile();
			if (!this.root.exists()) {
				throw new IOException();
			}
		} catch (IOException ex) {
			throw new FileNotFoundException("Root for HTTP file handler not found");
		}
	}

	public final void handle(HttpExchange t) throws IOException {
		// Convert the supplied URI into a pathname.
		String path = t.getRequestURI().getPath();
		String basePath = t.getHttpContext().getPath();
		if (!basePath.endsWith("/")) {
			basePath = basePath + "/";
		}
		File file = null;
		if (path.startsWith(basePath)) {
			String relativePath = path.substring(basePath.length());
			if (relativePath.length() == 0) {
				file = root;
			} else {
				file = new File(root.getPath() + "/" + relativePath).getCanonicalFile();
			}
		}

		if ((file == null) || !file.getPath().startsWith(root.getPath())) {
			// Suspected path traversal attack:
			// reject with 403 error.
			Headers h = t.getResponseHeaders();
			h.set("Content-Type", "text/html");
			String response = "<h1>403 (Forbidden)</h1>\n";
			t.sendResponseHeaders(403, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		} else if (!file.isFile()) {
			// Object does not exist or is not a file:
			// reject with 404 error.
			Headers h = t.getResponseHeaders();
			h.set("Content-Type", "text/html");
			String response = "<h1>404 (Not Found)</h1>\n";
			t.sendResponseHeaders(404, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		} else {
			// Object exists and is a file:
			// accept with response code 200.
			t.sendResponseHeaders(200, 0);
			OutputStream os = t.getResponseBody();
			FileInputStream fs = new FileInputStream(file);

			// A buffer size of 16K is large enough to be
			// reasonably efficient, but not so large that
			// allocating it dynamically is burdensome.
			final int bufferSize = 0x4000;
			final byte[] buffer = new byte[bufferSize];
			int count = 0;
			while ((count = fs.read(buffer)) >= 0) {
				os.write(buffer, 0, count);
			}
			fs.close();
			os.close();
		}
	}
}
