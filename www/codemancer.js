// This file is part of Codemancer.
// Copyright 2015-2016 Graham Shaw.
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

/** Update the content of a tree view element.
 * @param root the DOM element at the root of the tree or subtree to be updated
 * @param updates an ordered list of changes to be applied to the tree or subtree
 * The root element should be a 'ul' element. It should not have any content,
 * other than that given to it by this function.
 * Each update has up to three components:
 * - a unique ID which uniquely identifies the node to be updated (required);
 * - a string used to label the node (optional); and
 * - a list of updates to be applied recursively to descendants of the node
 *   (optional).
 * If the label is missing or null then the node is deleted. Any descendants of
 * the deleted node are deleted implicitly, therefore they need not be listed
 * as updates in their own right.
 * If the list of updates is missing or null then the node becomes an internal
 * node (capable of being expanded or collapsed), otherwise it becomes an
 * external node (capable of being selected).
 * The unique ID is used as the 'id' attribute of the corresponding 'li' element
 * in the DOM, so it must be globally unique within the page (not just within the
 * tree view). The ID is also used to determine the order in which the children
 * of a node are displayed. Updates must be supplied in ascending order of ID.
 * Each label is displayed within a 'button' element.
 */
function updateTreeView(root, updates) {
	var currentLi = root.firstChild;

	for (var i = 0; i != updates.length; i++) {
		// Extract the update to be applied.
		var update = updates[i];
		var id = update[0];
		var label = update[1];
		var content = update[2];

		// Skip over any list items which precede the node to be updated.
		while (currentLi && (currentLi.getAttribute('id') < id)) {
			currentLi = currentLi.nextSibling;
		}

		if (label == null) {
			// Delete the list item with the given ID.
			if (currentLi && (currentLi.getAttribute('id') == id)) {
				// The current list item matches, so delete it.
				var nextLi = currentLi.nextSibling;
				root.removeChild(currentLi);
				currentLi = nextLi;
			}
		} else {
			// Ensure that a list item exists with the given ID and label.
			// It should also have a sub-list, if and only if it is an
			// internal node.
			var button = null;
			if (currentLi && (currentLi.getAttribute('id') == id)) {
				// There is already a list item with the required ID.
				// Ensure that it has the required label.
				button = currentLi.firstChild;
				if (button.textContent != label) {
					button.textContent = label;
				}
			} else {
				// The ID of the current list item is different,
				// or we have reached the end of the list.
				// Create a new button.
				button = document.createElement('button');
				button.textContent = label;

				// Create a new list item.
				var newLi = document.createElement('li');
				newLi.setAttribute('id', id);
				newLi.appendChild(button);
				root.insertBefore(newLi, currentLi);
				currentLi = newLi;
			}

			if (content) {
				// Ensure that there is a sublist (but if it is
				// a new one then don't add it yet).
				var ul = currentLi.firstChild.nextSibling;
				if (!ul) {
					ul = document.createElement('ul');
					ul.setAttribute('class', 'tree');
				}

				// Update the subtree with the required list of changes.
				updateTreeView(ul, content);

				// Ensure that the sublist is attached to the current
				// list item.
				if (currentLi.firstChild.nextSibling == null) {
					currentLi.appendChild(ul);
				}
			} else {
				// If the current list item has a sublist then remove it.
				if (currentLi.firstChild.nextSibling != null) {
					currentLi.removeChild(ul);
				}
			}
		}
	}
}

/** Process areas from changeset.
 * Each area is represented by an array with two components: a fixed ID
 * (which determines the ordering), and a name. For areas that have been
 * deleted, only the ID is listed.
 * @param areas an array of areas
 */
function processAreas(areas) {
	var areaTree = document.getElementById("areas");
	if (areaTree == null) {
		document.getElementById("nav").innerHTML = "<ul id='areas' class='tree root'></ul>";
		areaTree = document.getElementById("areas");
	}

	updateTreeView(document.getElementById("areas"), areas);
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
	if (changeset.areas != null) {
		processAreas(changeset.areas);
	}
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
		document.getElementById('loading').style.display = 'block';
	}

	xhr = new XMLHttpRequest();
	xhr.open("GET", "/changeset.json?db=" + db + "&minrev=" + minRev + "&minaddr=" + minAddr.toString(16) + "&maxaddr=" + maxAddr.toString(16), true);
	xhr.onreadystatechange = function() {
		if (xhr.readyState == 4) {
			document.getElementById('loading').style.display = 'none';
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
