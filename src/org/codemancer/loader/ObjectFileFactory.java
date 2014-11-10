// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader;

import java.lang.IllegalArgumentException;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;

/** A factory class for recognising the type of object files using signatures. */
public class ObjectFileFactory {
	private static class Signature {
		/** The constructor to be invoked if this signature is matched. */
		public final Constructor constructor;

		/** The introductory byte sequence matched by this signature. */
		public final int[] intro;

		/** Construct signature.
	 	 * @param className the name of the object file class
		 * @param intro the introductory byte sequence
		 */
		public Signature(String className, int[] intro) throws IllegalArgumentException {
			try {
				Class cl = Class.forName(className);
				this.constructor = cl.getConstructor(new Class[]{ByteBuffer.class});
				this.intro = intro;
			} catch (ClassNotFoundException ex) {
				throw new IllegalArgumentException("class '" + className + "' not found", ex);
			} catch (NoSuchMethodException ex) {
				throw new IllegalArgumentException("constructor for '" + className + "' not found", ex);
			}
		}

		/** Test whether a given byte sequence matches this signature.
		 * @param intro the byte sequence to be tested
		 * @return true if it matches, otherwise false
		 */
		public boolean test(byte[] intro) {
			for (int i = 0; i != this.intro.length; ++i) {
				if ((intro[i] & 0xff) != this.intro[i]) return false;
			}
			return true;
		}

		/** Invoke the constructor for this signature.
		 * @param buffer a ByteBuffer to provide access to the object file
		 * @return the resulting object file, or null if it could not be interpreted as one
		 */
		public ObjectFile invoke(ByteBuffer buffer) {
			try {
				return (ObjectFile)constructor.newInstance(buffer);
			} catch (Exception ex) {
				return null;
			}
		}
	}

	/** The signatures to be tested when a file of unknown type is loaded. */
	private static final Signature[] signatures = {
		new Signature("org.codemancer.loader.elf.ElfFile", new int[]{0x7f, 0x45, 0x4c, 0x46}),
		new Signature("org.codemancer.loader.aof.AofFile", new int[]{0xc5, 0xc6, 0xcb, 0xc3}),
		new Signature("org.codemancer.loader.aof.AofFile", new int[]{0xc3, 0xcb, 0xc6, 0xc5}),
		// This is the signature for a Z80 COFF file, which is the only type supported currently.
		new Signature("org.codemancer.loader.coff.CoffFile", new int[]{0x5a, 0x80})
	};

	/** Make an ObjectFile from a ByteBuffer.
	 * @param buffer the ByteBuffer
	 * @return the ObjectFile, or null if the ByteBuffer could not be interpreted as one
	 */
	public static ObjectFile make(ByteBuffer buffer) {
		byte[] intro = new byte[4];
		buffer.position(0);
		buffer.get(intro);
		buffer.position(0);
		for (Signature signature: signatures) {
			if (signature.test(intro)) {
				ObjectFile objFile = signature.invoke(buffer);
				if (objFile != null) return objFile;
			}
		}
		return null;
	}
}
