// This file is part of Codemancer.
// Copyright 2016 Graham Shaw.
// All rights reserved.

package org.codemancer.db;

import java.util.TreeMap;
import java.util.Map;

/** A class to represent a set of address ranges.
 * Adjacent or overlapping address ranges are merged automatically.
 */
public class AddressRangeSet {
	/** A mapping from the start address of each range to its length. */
	private final TreeMap<Long, Long> ranges = new TreeMap<Long, Long>();

	/** Determine whether an address is a member of the set.
	 * @param addr the address to be tested
	 * @return true if a member of the set, otherwise false
	 */
	public final boolean contains(long addr) {
		Map.Entry<Long, Long> range = ranges.floorEntry(addr);
		return (range != null) && (addr <= range.getKey() + range.getValue() - 1);
	}

	/** Add address range to set.
	 * @param minAddr the lowest address in the range to be added
	 * @param maxAddr the highest address in the range to be added
	 */
	public final void add(long minAddr, long maxAddr) {
		// If there is an existing range starting prior to minAddr,
		// and ending ajacent to or overlapping minAddr, then merge
		// it into this range. (There should be at most one of these.)
		Map.Entry<Long, Long> prevRange = ranges.lowerEntry(minAddr);
		if ((prevRange != null) && (prevRange.getKey() + prevRange.getValue() >= minAddr)) {
			minAddr = prevRange.getKey();
			if (prevRange.getKey() + prevRange.getValue() >= maxAddr) {
				maxAddr = prevRange.getKey() + prevRange.getValue() - 1;
			}
			ranges.remove(prevRange.getKey());
		}

		// While there is an existing range starting at or beyond
		// minAddr, but no more than 1 byte beyond maxAddr, merge
		// that into this range. (There could be several of these.)
		Map.Entry<Long, Long> nextRange = ranges.ceilingEntry(minAddr);
		while ((nextRange != null) && (nextRange.getKey() <= maxAddr + 1)) {
			if (nextRange.getKey()  + nextRange.getValue() >= maxAddr) {
				maxAddr = nextRange.getKey() + nextRange.getValue() - 1;
			}
			ranges.remove(nextRange.getKey());
			nextRange = ranges.ceilingEntry(minAddr);
		}

		// Add the (possibly expanded) range to the map.
		ranges.put(minAddr, maxAddr - minAddr + 1);
	}

	/** Represent set as JPQL query.
	 * @param addrName the placeholder used to represent an address
	 * @return a query corresponding to the address range set
	 */
	public final String asJPQL(String addrName) {
		StringBuilder result = new StringBuilder();

		long count = 0;
		for (Map.Entry<Long, Long> range: ranges.entrySet()) {
			long minAddr = range.getKey();
			long maxAddr = minAddr + range.getValue();

			if (count > 0) {
				result.append("OR");
			}

			result.append("((");
			result.append(addrName);
			result.append(">=");
			result.append(minAddr);
			result.append(")AND(");
			result.append(addrName);
			result.append("<=");
			result.append(maxAddr);
			result.append("))");
			count += 1;
		}

		if (count == 0) {
			result.append("TRUE");
			count += 1;
		}

		if (count > 1) {
			result.insert(0, '(');
			result.append(')');
		}

		return result.toString();
	}
}
