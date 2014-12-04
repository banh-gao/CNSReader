/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2006 CardContact Software & System Consulting
 * |'##> <##'|  Andreas Schwier, 32429 Minden, Germany (www.cardcontact.de)
 *  --------- 
 *
 *  This file is part of OpenSCDP.
 *
 *  OpenSCDP is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *
 *  OpenSCDP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSCDP; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.cardcontact.tlv;

import java.util.StringTokenizer;


/**
 * Class to implement TLV encoded Object Identifier according to ASN.1
 * 
 * @author Andreas Schwier (info@cardcontact.de)
 */
public class ObjectIdentifier extends PrimitiveTLV {


	/**
	 * Create object identifier
	 * @param oid
	 * 		Integer array with identifier
	 */	
	public ObjectIdentifier(int[] oid) {
		super(new Tag(Tag.OBJECT_IDENTIFIER, Tag.UNIVERSAL, false), null);
		fromIntArray(oid);
	}



	/**
	 * Create object identifier from binary presentation
	 * 
	 * @param pb
	 * 		Buffer with binary presentation
	 */
	public ObjectIdentifier(ParseBuffer pb) throws TLVEncodingException {
		super(pb);
	}
	
	

	/**
	 * Create object identifier from binary presentation
	 * 
	 * @param pb
	 * 		Buffer with binary presentation
	 */
	public ObjectIdentifier(byte[] value) {
		super(new Tag(Tag.OBJECT_IDENTIFIER, Tag.UNIVERSAL, false), value);
	}
	
	

	/**
	 * Create object identifier from string
	 * 
	 * @param oid
	 * 		Dottet or blank separated object identifier
	 */
	public ObjectIdentifier(String oid) {
		super(new Tag(Tag.OBJECT_IDENTIFIER, Tag.UNIVERSAL, false), null);
		fromString(oid);
	}



	/**
	 * Create object identifier from base and extension
	 * 
	 * @param baseoid
	 * 		Base object identifier
	 * @param extoid
	 * 		Extension added to base object identifier
	 */
	public ObjectIdentifier(int[] baseoid, int[] extoid) {
		super(new Tag(Tag.OBJECT_IDENTIFIER, Tag.UNIVERSAL, false), null);
		int[] oid = new int[baseoid.length + extoid.length];
		System.arraycopy(baseoid, 0, oid, 0, baseoid.length);
		System.arraycopy(extoid, 0, oid, baseoid.length, extoid.length);
		fromIntArray(oid);
	}



	/**
	 * Helper to create value field from array of object identifier elements
	 * 
	 * @param oid
	 * 		Array containing object identifier elements
	 */	
	protected void fromIntArray(int oid[]) {
		int i, j, size, val;
		
		if ((oid.length < 2) || (oid[0] < 0) || (oid[0] > 2) || (oid[1] < 0) || (oid[1] > 39))
			throw new IllegalArgumentException("Object identifier out of range");
			
		size = 1;
		
		for (i = 2; i < oid.length; i++) {
			val = oid[i];
			do	{
				size++;
				val >>= 7;
			} while (val > 0);
		}

		value = new byte[size];

		value[0] = (byte)(40 * oid[0] + oid[1]);
		
		j = 1;
		for (i = 2; i < oid.length; i++) {
			val = oid[i];
			size = -7;
			do	{
				size += 7;
				val >>= 7;
			} while (val > 0);
			
			val = oid[i];
			for (; size >= 0; size -= 7) {
				value[j++] = (byte)((val >> size) & 0x7F | 0x80);
			}
			value[j - 1] &= 0x7F;
		}
	}


	
	/**
	 * Helper to create byte array from string
	 * 
	 * @param oid
	 */
	protected void fromString(String oid) {
		try {
			StringTokenizer sp = new StringTokenizer(oid, " .");

			int[] elements = new int[sp.countTokens()];
			int i = 0;

			while (sp.hasMoreTokens()) {

				String temp = sp.nextToken();

				elements[i++] = Integer.parseInt(temp);
			}

			// Call the helper function to create the actual byte buffer
			fromIntArray(elements);
		}
		catch(NumberFormatException nfe) {
			throw new IllegalArgumentException("Object identifier string is invalid");
		}
	}
	
	
	
	/**
	 * Return object identifier
	 * 
	 * @return
	 * 		Object identifier as int[]
	 */
	public int[] getObjectIdentifier() {
		return convertBytesToOID(value);
	}


	
	/**
	 * Helper to convert binary data into list of object identifier components
	 * 
	 * @param value Binary data
	 * @return Array of object identifiers
	 */
	public static int[] convertBytesToOID(byte[] value) {
		int i, j, size;
		
		if (value.length == 0) {
			return new int[0];
		}
		
		size = 2;
		for (i = 1; i < value.length; i++) {
			if ((value[i] & 0x80) != 0x80)
				size++;
		}
		
		int objectIdentifier[] = new int[size];
		objectIdentifier[0] = value[0] / 40;
		objectIdentifier[1] = value[0] % 40;
		j = 2;		
		for (i = 1; i < value.length; i++) {
			objectIdentifier[j] = (objectIdentifier[j] << 7) | (value[i] & 0x7F);
			if ((value[i] & 0x80) != 0x80) {
				 j++;
			}
		}
		return objectIdentifier;
	}
	
	
	
	/**
	 * Convert list of object identifier into dotted string format
	 *  
	 * @param oid Array of object identifier
	 * 
	 * @return String in dotted format
	 */
	public static String getObjectIdentifierAsString(int[] oid) {
		StringBuffer buffer = new StringBuffer(80);

		buffer.append(oid[0]);
		
		for (int i = 1; i < oid.length; i++) {
			buffer.append("." + oid[i]);
		}
		return buffer.toString();
	}
	
	
	
	/**
	 * Convert object identifier to ASN.1 string syntax
	 * 
	 * @param indent
	 * 		Left indentation
	 * @return
	 * 		String containing the ASN.1 representation
	 */
	public String dump(int indent) {
		return dumpSingleLine(indent);
	}
	


	/**
	 * Return object identifier as ASN.1 string
	 * 
	 * @return
	 * 		String in ASN.1 notation
	 */	
	public String toString() {
		StringBuffer buffer = new StringBuffer(80);

		if (name != null) {
			buffer.append(name);
			buffer.append(' ');
		}

		buffer.append("OBJECT IDENTIFIER = {");
		
		ObjectIdentifierRegistry reg = ObjectIdentifierRegistry.getInstance();
		String name = reg.getNameFor(value);
		
		if (name != null) {
			buffer.append(" " + name);
		} else {
			int oid[] = getObjectIdentifier();
		
			for (int i = 0; i < oid.length; i++) {
				buffer.append(" " + oid[i]);
			}
		}
		buffer.append(" }");

		return buffer.toString();
	}
}

