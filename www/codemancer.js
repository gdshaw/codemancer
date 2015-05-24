// This file is part of Codemancer.
// Copyright 2015 Graham Shaw.
// All rights reserved.

var rev = 0;
var minAddr = 0x0000;
var maxAddr = 0x3FFF;
var codeTableInfo = [];
var xhr = null;
var reloadCodeTable = 1;

// Construct hash containing name-value pairs from URI query string.
var Query = function() {
	// Extract query string from URI, removing the leading '?'.
	var queryString = window.location.search.substring(1);

	// Split into name-value pairs.
	var pairs = queryString.split("&");

	// Insert pairs into hash.
	for (var i = 0; i < pairs.length; i++) {
		var pair = pairs[i].split("=");
		this[pair[0]] = pair[1];
	}
}
var query = new Query();
var db = query['db'];

/** Convert integer to upper case hex.
 * @param value the integer to be converted
 * @param digits the number of digits required (up to 16)
 * @return the value as a hex string (no prefix)
 */
function intToHex(value, digits) {
	var string = "0000000000000000"+value.toString(16);
	return string.slice(-digits).toUpperCase();
}

/** Process lines from changeset.
 * Each line is represented by an array with four components: a minimum
 * address, a maximum address, a type, and the disassembled instruction.
 * The lines must not overlap, and must be listed in ascending address order.
 * @param lines an array of lines.
 */
function processLines(lines) {
	var codeTable = document.getElementById("code");
	if (codeTable == null) {
		document.getElementById("content").innerHTML = "<table id='code'></table>";
		codeTable = document.getElementById("code");
	}

	var i = 0;

	if (reloadCodeTable != 0) {
		codeTable.innerHTML = "";
		codeTableInfo = [];
		reloadCodeTable = 0;
	}

	// For each supplied line:
	for (var j = 0; j != lines.length; j++) {
		var line = lines[j];

		// Construct a DOM element for the new row.
		var td1 = document.createElement('td');
		td1.appendChild(document.createTextNode(intToHex(line[0], 4)));
		var td2 = document.createElement('td');
		td2.appendChild(document.createTextNode(line[2]));
		var td3 = document.createElement('td');
		var tr = document.createElement('tr');
		tr.setAttribute("onclick", "handleLineClick(this);");
		tr.appendChild(td1);
		tr.appendChild(td2);
		tr.appendChild(td3);

		// Construct new entry for insertion into codeTableInfo.
		var rowInfo = [line[0], tr];

		// Skip over any existing rows located prior to the new row.
		while ((i < codeTableInfo.length) && (codeTableInfo[i][0] < line[0])) {
			++i;
		}

		// Insert the new row, replacing an existing one if appropriate.
		if (i < codeTableInfo.length) {
			if (codeTableInfo[i][0] == line[0]) {
				codeTable.replaceChild(tr, codeTableInfo[i][1]);
				codeTableInfo[i] = rowInfo;
			} else {
				codeTable.insertBefore(tr, codeTableInfo[i][1]);
				codeTableInfo.splice(i, 0, rowInfo);
			}
		} else {
			codeTable.insertBefore(tr, null);
			codeTableInfo.push(rowInfo);
		}
		++i;

		// Remove any rows which overlap with the new line.
		while ((i < codeTableInfo.length) && (codeTableInfo[i][0] <= line[1])) {
			codeTable.removeChild(codeTableInfo[i][1]);
			codeTableInfo.splice(i, 1);
		}
	}
}

/** Process a changeset from the server.
 * @param changeset the changeset to process
 */
function processChangeset(changeset) {
	if (changeset.lines != null) {
		processLines(changeset.lines);
	}
	rev = changeset.rev;
}

/** Repeatedly request and process changesets from the server. */
function requestChangeset() {
	var minRev = rev + 1;
	if (reloadCodeTable != 0) {
		minRev = 0;
	}

	xhr = new XMLHttpRequest();
	xhr.open("GET", "/changeset.json?db=" + db + "&minrev=" + minRev + "&minaddr=" + minAddr.toString(16) + "&maxaddr=" + maxAddr.toString(16), true);
	xhr.onreadystatechange = function() {
		if (xhr.readyState == 4) {
			if (xhr.status >= 200 && xhr.status < 300) {
				// If the request was successful then process the response, then immediately request again.
				retryCounter = 0;
				var changeset = eval(xhr.responseText);
				processChangeset(changeset);
				requestChangeset();
			} else {
				// If the request failed, display the response as an error.
				var contentDiv = document.getElementById("content");
				contentDiv.innerHTML = xhr.responseText;
			}
		}
	}
	xhr.send(null);
}
