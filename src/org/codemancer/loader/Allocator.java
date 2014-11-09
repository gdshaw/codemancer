// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader;

/** A class for allocating memory addresses. */
public class Allocator {
	/** The next address to allocate. */
	private long nextAddr;

	/** The page size which allocations should be a multiple of. */
	private long pageSize;

	/** Construct allocator.
	 * @param baseAddr the first address to allocate.
	 * @param pageSize the page size which allocations should be a multiple of
	 */
	public Allocator(long baseAddr, long pageSize) {
		this.nextAddr = baseAddr;
		this.pageSize = pageSize;
	}

	/** Allocate a given number of bytes.
	 * @param size the number of bytes to allocate
	 * @return the allocated address
	 */
	public final long allocate(long size) {
		long addr = nextAddr;
		nextAddr += size + pageSize - 1;
		nextAddr -= nextAddr % pageSize;
		return addr;
	}
}
