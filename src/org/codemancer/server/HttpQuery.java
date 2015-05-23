// This file is part of Codemancer.
// Copyright 2015 Graham Shaw.
// All rights reserved.

package org.codemancer.server;

import java.util.HashMap;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import com.sun.net.httpserver.HttpExchange;

/** A class for parsing the parameters of an HTTP GET or POST request. */
public class HttpQuery {
	/** The key-value pairs contained within this query,
	 * indexed by key. */
	private HashMap<String, String> parameters = new HashMap<String, String>();

	/** Parse HTTP GET or POST request.
	 * @param t the HttpExchange object corresponding to the request
	 */
	public HttpQuery(HttpExchange t) throws IOException {
		// Extract the query string from the HTTP request.
		String method = t.getRequestMethod();
		String query;
		if (method.equalsIgnoreCase("GET")) {
			// For GET requests, extract from URI.
			URI uri = t.getRequestURI();
			query = uri.getQuery();
		} else if (method.equalsIgnoreCase("POST")) {
			// For POST requests, extract from body.
			InputStreamReader isr = new InputStreamReader(
				t.getRequestBody(), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			query = br.readLine();
		} else {
			throw new IllegalArgumentException(
				"HTTP query parsing not supported for method '" +
				method + "'");
		}

		// Split the query string into parameters.
		// These can be separated by ampersands or semicolons.
		for (String parameterString: query.split("[&;]")) {
			// A parameter may be either a key-value pair,
			// or just a key. Splitting on the first equals
			// sign if there is one should yield a 2- or
			// 1-element array respectively in these two
			// cases.
			String parameter[] = parameterString.split("=", 2);
			if (parameter.length == 2) {
				parameters.put(parameter[0], URLDecoder.decode(parameter[1], "UTF-8"));
			} else {
				parameters.put(parameter[0], "");
			}
		}
	}

	/** Determine whether the query contains a given key.
	 * @param key the name of the key to check
	 * @return true if present in the query, otherwise false
	 */
	public final boolean containsKey(String key) {
		return parameters.containsKey(key);
	}

	/** Get the value corresponding to a given key.
	 * @param key the key for which the value is required
	 * @return the corresponding value, or null if the key was not found
	 */
	public final String get(String key) {
		return parameters.get(key);
	}
}
