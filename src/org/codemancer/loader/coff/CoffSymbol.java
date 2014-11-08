// This file is part of Codemancer.
// Copyright 2014 Graham Shaw.
// Distribution and modification are permitted within the terms of the
// GNU General Public License (version 3 or any later version).

package org.codemancer.loader.coff;

import java.io.PrintWriter;
import java.io.IOException;
import java.nio.ByteBuffer;

/** A class to represent a symbol from an COFF symbol table. */
public class CoffSymbol {
	/** A constant to indicate that a symbol has no section number because it is external. */
	public static final int N_UNDEF = 0;

	/** A constant to indicate that a symbol has no section number because it is not an address. */
	public static final int N_ABS = -1;

	/** A constant to indicate that a symbol has no section number because it is a debugging symbol. */
	public static final int N_DEBUG = -2;

	/** A constant to indicate that a symbol has no specified type. */
	public static final short T_NULL = 0;

	/** A constant to indicate that a symbol has void type. */
	public static final short T_VOID = 1;

	/** A constant to indicate that a symbol refers to a character. */
	public static final short T_CHAR = 2;

	/** A constant to indicate that a symbol refers to a short integer. */
	public static final short T_SHORT = 3;

	/** A constant to indicate that a symbol refers to an integer. */
	public static final short T_INT = 4;

	/** A constant to indicate that a symbol refers to a long integer. */
	public static final short T_LONG = 5;

	/** A constant to indicate that a symbol refers to a single precision floating point number. */
	public static final short T_FLOAT = 6;

	/** A constant to indicate that a symbol refers to a double precision floating point number. */
	public static final short T_DOUBLE = 7;

	/** A constant to indicate that a symbol refers to a stucture. */
	public static final short T_STRUCT = 8;

	/** A constant to indicate that a symbol refers to a union. */
	public static final short T_UNION = 9;

	/** A constant to indicate that a symbol refers to an enumeration. */
	public static final short T_ENUM = 10;

	/** A constant to indicate that a symbol refers to an enumeration constant. */
	public static final short T_MOE = 11;

	/** A constant to indicate that a symbol refers to an unsigned character. */
	public static final short T_UCHAR = 12;

	/** A constant to indicate that a symbol refers to an unsigned short integer. */
	public static final short T_USHORT = 13;

	/** A constant to indicate that a symbol refers to an unsigned integer. */
	public static final short T_UINT = 14;

	/** A constant to indicate that a symbol refers to an unsigned long integer. */
	public static final short T_ULONG = 15;

	/** A constant to indicate that a symbol refers to a long double precision floating point number. */
	public static final short T_LNGDBL = 16;

	/** A constant to indicate that a symbol does not refer to a derived type. */
	public static final int DT_NON = 0;

	/** A constant to indicate that a symbol refers to a pointer. */
	public static final int DT_PTR = 1;

	/** A constant to indicate that a symbol refers to a function. */
	public static final int DT_FCN = 2;

	/** A constant to indicate that a symbol refers to an array. */
	public static final int DT_ARY = 3;

	/** A constant to indicate that a symbol has no specified storage class. */
	public static final byte C_NULL = 0;

	/** A constant to indicate that a symbol refers to an automatic variable. */
	public static final byte C_AUTO = 1;

	/** A constant to indicate that a symbol refers to a public symbol. */
	public static final byte C_EXT = 2;

	/** A constant to indicate that a symbol refers to a private symbol. */
	public static final byte C_STAT = 3;

	/** A constant to indicate that a symbol refers to a register variable. */
	public static final byte C_REG = 4;

	/** A constant to indicate that a symbol refers to an external definition. */
	public static final byte C_EXTDEF = 5;

	/** A constant to indicate that a symbol refers to a label. */
	public static final byte C_LABEL = 6;

	/** A constant to indicate that a symbol refers to an undefined label. */
	public static final byte C_ULABEL = 7;

	/** A constant to indicate that a symbol refers to a member of a structure. */
	public static final byte C_MOS = 8;

	/** A constant to indicate that a symbol refers to a function argument. */
	public static final byte C_ARG = 9;

	/** A constant to indicate that a symbol refers to a structure tag. */
	public static final byte C_STRTAG = 10;

	/** A constant to indicate that a symbol refers to a member of a union. */
	public static final byte C_MOU = 11;

	/** A constant to indicate that a symbol refers to a union tag. */
	public static final byte C_UNTAG = 12;

	/** A constant to indicate that a symbol refers to a type definition. */
	public static final byte C_TPDEF = 13;

	/** A constant to indicate that a symbol refers to an undefined static. */
	public static final byte C_USTATIC = 14;

	/** A constant to indicate that a symbol refers to an enumeration tag. */
	public static final byte C_ENTAG = 15;

	/** A constant to indicate that a symbol refers to a member of an enumeration. */
	public static final byte C_MOE = 16;

	/** A constant to indicate that a symbol refers to a register parameter. */
	public static final byte C_REGPARM = 17;

	/** A constant to indicate that a symbol refers to a bit field. */
	public static final byte C_FIELD = 18;

	/** A constant to indicate that a symbol refers to an auto argument. */
	public static final byte C_AUTOARG = 19;

	/** A constant to indicate the end of the symbol table. */
	public static final byte C_LASTENT = 20;

	/** A constant to mark the beginning or end of a block. */
	public static final byte C_BLOCK = 100;

	/** A constant to mark the beginning or end of a function. */
	public static final byte C_FCN = 101;

	/** A constant to mark the end of a structure. */
	public static final byte C_EOS = 102;

	/** A constant to indicate that a symbol is a file name. */
	public static final byte C_FILE = 103;

	/** A constant to indicate that a symbol is a line number. */
	public static final byte C_LINE = 104;

	/** A constant to indicate that a symbol is a duplicate tag. */
	public static final byte C_ALIAS = 105;

	/** A constant to indicate that a symbol is an external symbol
	 * in a dmert public library. */
	public static final byte C_HIDDEN = 106;

	/** A constant to mark the physical end of a function. */
	public static final byte C_EFCN = (byte)255;

	/** The size of a symbol table entry, in bytes. */
	public static final int SYMESZ = 18;

	/** The name of this symbol. */
	private String e_name;

	/** The value of this symbol. */
	private final long e_value;

	/** The section to which this symbol refers. */
	private final short e_scnum;

	/** The type of this symbol. */
	private final short e_type;

	/** The storage class for this symbol. */
	private final byte e_sclass;

	/** The number of auxiliary entries. */
	private final byte e_numaux;

	/** Construct new symbol.
	 * On entry the ByteBuffer must be positioned at the start of the
	 * required symbol. On exit it will be positioned immediately after
	 * the end of that symbol.
	 * @param buffer a ByteBuffer giving access to the underlying COFF file
	 * @param coff the COFF file to which the symbol belongs
	 */
	public CoffSymbol(ByteBuffer buffer, CoffFile coff) throws IOException {
		// Read the first four bytes of the symbol name.
		byte[] bytes = new byte[8];
		buffer.get(bytes, 0, 4);
		if ((bytes[0] | bytes[1] | bytes[2] | bytes[3]) == 0) {
			// If the first four bytes are zero
			// then the second four are an offset.
			int e_offset = buffer.getInt();
			e_name = coff.getString(e_offset);
			e_name = "foo";
		} else {
			// If any of the first four bytes are non-zero
			// then the first eight bytes are a string.
			buffer.get(bytes, 4, 4);
			int length = 0;
			while ((length < 8) && (bytes[length] != 0)) {
				length += 1;
			}
			e_name = new String(bytes, 0, length, "ISO-8859-1");
		}

		// Read the remainder of the symbol table entry.
		e_value = buffer.getInt();
		e_scnum = buffer.getShort();
		e_type = buffer.getShort();
		e_sclass = buffer.get();
		e_numaux = buffer.get();

		// Skip any auxiliary entries.
		buffer.position(buffer.position() + e_numaux * SYMESZ);
	}

	/** Get the name of this symbol.
	 * @return the symbol name
	 */
	public String getName() {
		return e_name;
	}

	/** Get the value of this symbol.
	 * @return the symbol value
	 */
	public final long getValue() {
		return e_value;
	}

	/** Get the size of the object referred to by this symbol.
	 * @return the size, in bytes
	 */
	public final long getSize() {
		return 0;
	}

	/** Get the base type of this symbol.
	 * @return the base type
	 */
	public final int getCoffBaseType() {
		return e_type & 0xf;
	}

	/** Get the derived type of this symbol.
	 * @return the derived type
	 */
	public final int getCoffDerivedType() {
		return (e_type >> 4) & 0xf;
	}

	/** Get the base type of this COFF symbol as a string.
	 * @return the base type, as a string
	 */
	public final String getCoffBaseTypeString() {
		switch (getCoffBaseType()) {
		case T_NULL:
			return "NULL";
		case T_VOID:
			return "VOID";
		case T_CHAR:
			return "CHAR";
		case T_SHORT:
			return "SHORT";
		case T_INT:
			return "INT";
		case T_LONG:
			return "LONG";
		case T_FLOAT:
			return "FLOAT";
		case T_DOUBLE:
			return "DOUBLE";
		case T_STRUCT:
			return "STRUCT";
		case T_UNION:
			return "UNION";
		case T_ENUM:
			return "ENUM";
		case T_MOE:
			return "MOE";
		case T_UCHAR:
			return "UCHAR";
		case T_USHORT:
			return "USHORT";
		case T_UINT:
			return "UINT";
		case T_ULONG:
			return "ULONG";
		case T_LNGDBL:
			return "LNGDBL";
		default:
			return "UNKNOWN";
		}
	}

	/** Get the type of this COFF symbol as a string.
	 * @return the base type, as a string
	 */
	public final String getCoffTypeString() {
		String baseType = getCoffBaseTypeString();
		switch (getCoffDerivedType()) {
		case DT_NON:
			return baseType;
		case DT_PTR:
			return baseType + "*";
		case DT_FCN:
			return baseType + "(*)()";
		case DT_ARY:
			return baseType + "[]";
		default:
			return baseType + "?";
		}
	}

	/** Get the storage class of this COFF symbol as a string.
	 * @return the storage class, as a string
	 */
	public final String getCoffStorageClassString() {
		switch (e_sclass) {
		case C_NULL:
			return "NULL";
		case C_AUTO:
			return "AUTO";
		case C_EXT:
			return "EXT";
		case C_STAT:
			return "STAT";
		case C_REG:
			return "REG";
		case C_EXTDEF:
			return "EXTDEF";
		case C_LABEL:
			return "LABEL";
		case C_ULABEL:
			return "ULABEL";
		case C_MOS:
			return "MOS";
		case C_ARG:
			return "ARG";
		case C_STRTAG:
			return "STRTAG";
		case C_MOU:
			return "MOU";
		case C_UNTAG:
			return "UNTAG";
		case C_TPDEF:
			return "TPDEF";
		case C_USTATIC:
			return "USTATIC";
		case C_ENTAG:
			return "ENTAG";
		case C_MOE:
			return "MOE";
		case C_REGPARM:
			return "REGPARM";
		case C_FIELD:
			return "FIELD";
		case C_AUTOARG:
			return "AUTOARG";
		case C_LASTENT:
			return "LASTENT";
		case C_BLOCK:
			return "BLOCK";
		case C_FCN:
			return "FCN";
		case C_EOS:
			return "EOS";
		case C_FILE:
			return "FILE";
		case C_LINE:
			return "LINE";
		case C_ALIAS:
			return "ALIAS";
		case C_HIDDEN:
			return "HIDDEN";
		case C_EFCN:
			return "EFCN";
		default:
			return "UNKNOWN";
		}
	}

	/** Get the number of auxiliary entries.
	 * @return the number of auxiliary entries
	 */
	public final int getAuxiliaryEntryCount() {
		return e_numaux;
	}

	public void dump(PrintWriter out) throws IOException {
		out.printf("%016x%5d%7s %7s %s\n", e_value, e_scnum,
			getCoffStorageClassString(), getCoffTypeString(), e_name);
	}
}
