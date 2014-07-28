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
import java.io.UnsupportedEncodingException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Handle SubjectDirectoryAttribute constructs from X.509
 * 
 * The class implements the following ASN.1 syntax
 * 
 * Attribute ::= SEQUENCE {
 *        type            AttributeType,
 *        values          SET OF AttributeValue
 * }
 * 
 * AttributeType ::= OBJECT IDENTIFIER
 * AttributeValue ::= ANY
 * 
 * @author Andreas Schwier (info@cardcontact.de)
 */

public class DirectoryAttribute extends Sequence {
	private ConstructedTLV contents = null;


	/**
	 * Create DirectoryAttribute object with given object identifier
	 * 
	 * @param newoid
	 * 		Object identifier for attribute
	 */
	public DirectoryAttribute(ObjectIdentifier newoid) {
		super();
		add(newoid);
		contents = new ConstructedTLV(new Tag(0x11, (byte)0x00, true));
		add(contents);
	}


	/**
	 * Create DirectoryAttribute from TLV structure
	 * 
	 * @param tlv
	 * 		TLV object
	 * @throws TLVEncodingException
	 * 		Data structure can not be converted
	 */
	public DirectoryAttribute(TLV tlv) throws TLVEncodingException {
		super(tlv);

		/* Check if sequence */
		
		if ((tag.getNumber() != 0x10) || !(tag.isConstructed())) {
			throw new TLVEncodingException("Directory attribute does not start with SEQUENCE");
		}

		/* Decode object identifier */
		
		TLV oid = findTag(new Tag(Tag.OBJECT_IDENTIFIER), null);
		
		if (oid == null) {
			throw new TLVEncodingException("Object identifier not found");
		}
		
		TLV res = findTag(new Tag(0x11, (byte)0x00, true), null);
		
		if ((res == null) || !(res instanceof ConstructedTLV)) {
			throw new TLVEncodingException("Contents set not found");
		}
		
		contents = (ConstructedTLV)res;
	}

	
	/**
	 * Add TLV element to value SET of attribute
	 * 
	 * @param element
	 * 		TLV object to be added
	 */
	public void addElement(TLV element) {
		contents.add(element);
	}


	/**
	 * Return TLV element with given tag from SET of attributes
	 * 
	 * @param tag
	 * @return
	 * 		TLV object or null if object was not found
	 */
	public TLV getElement(int tag) {
		return contents.findTag(new Tag((byte)tag, (byte)0x00, false), null);
	}
	
	
	/**
	 * Find string with given tag and decode using given encoding format
	 * 
	 * @param tag
	 * 		Tag to look for
	 * @param encoding
	 * 		Encoding format (e.g. "UTF-8" or "8859_1")
	 * @return
	 * 		String or null if object was not found
	 * @throws UnsupportedEncodingException
	 * 		Character string can not be encoded
	 */
	public String getString(int tag, String encoding) throws UnsupportedEncodingException {

		String rc = null;
		TLV tlv = getElement(tag);

		if ((tlv != null) && (tlv instanceof PrimitiveTLV)) {
			rc = new String(((PrimitiveTLV)tlv).getValue(), encoding);
		}

		return rc;
	}


	/**
	 * Add numeric string to value SET of attribute
	 * 
	 * @param numericString
	 * @throws UnsupportedEncodingException
	 */
	public void addNumericString(String numericString)
		throws UnsupportedEncodingException {
		addElement(
			new PrimitiveTLV(
				new Tag(0x12, (byte) 0x00, false),
				numericString.getBytes("8859_1")));
	}


	/**
	 * Return numeric string from value set of attribute
	 *  
	 * @return
	 * 		String or null if object was not found
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public String getNumericString()
		throws UnsupportedEncodingException {

		return getString(0x12, "8859_1");
	}


	/**
	 * Add UTF-8 encoded string to value SET of attribute
	 * 
	 * @return
	 * 		String or null if object was not found
     *
	 * @throws UnsupportedEncodingException
	 */
	public void addUTF8String(String utf8String)
		throws UnsupportedEncodingException {
		addElement(
			new PrimitiveTLV(
				new Tag(0x0C, (byte) 0x00, false),
				utf8String.getBytes("UTF-8")));
	}


	/**
	 * Return UTF-8 string from value set of attribute
	 *
	 * @return
	 * 		String or null if object was not found
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public String getUTF8String()
		throws UnsupportedEncodingException {

		return getString(0x0C, "UTF-8");
	}


	/**
	 * Add Plain string encoded string to value SET of attribute
	 * 
	 * @param printableString
	 * 		Plain String
	 * @throws UnsupportedEncodingException
	 */
	public void addPrintableString(String printableString)
		throws UnsupportedEncodingException {
		addElement(
			new PrimitiveTLV(
				new Tag(0x13, (byte) 0x00, false),
				printableString.getBytes("8859_1")));
	}


	/**
	 * Return printable string from value set of attribute
	 * 
	 * @return
	 * 		String or null if object was not found
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public String getPrintableString()
		throws UnsupportedEncodingException {

		return getString(0x13, "8859_1");
	}


	/**
	 * Add Latin-1 encoded string to value SET of attribute
	 * 
	 * @param latin1String
	 * 		Unicode string
	 * @throws UnsupportedEncodingException
	 */
	public void addLatin1String(String latin1String)
		throws UnsupportedEncodingException {
		addElement(
			new PrimitiveTLV(
				new Tag(0x04, (byte) 0x00, false),
				latin1String.getBytes("8859_1")));
	}


	/**
	 * Return Latin-1 string from value set of attribute
	 * 
	 * @return
	 * 		String or null if object was not found
	 * 
	 * @throws UnsupportedEncodingException
	 */
	public String getLatin1String()
		throws UnsupportedEncodingException {

		return getString(0x04, "8859_1");
	}


	/**
	 * Add date encoded as generalized time to value SET of attribute
	 * 
	 * @param date
	 * 		Date to add
	 * @throws UnsupportedEncodingException
	 */
	public void addGeneralizedTime(Date date)
		throws UnsupportedEncodingException {

		SimpleDateFormat form = new SimpleDateFormat("yyyyMMdd'120000Z'");
		String output = form.format(date);
		addElement(
			new PrimitiveTLV(
				new Tag(0x18, (byte) 0x00, false),
				output.getBytes("8859_1")));
	}


	/**
	 * Return date from value set of attribute
	 * 
	 * @return date
	 * @throws UnsupportedEncodingException
	 */
	public Date getGeneralizedTime()
		throws UnsupportedEncodingException {

		SimpleDateFormat form = new SimpleDateFormat("yyyyMMddHHmmss");
		String val = getString(0x18, "8859_1");
		Date date = null;
		
		if (val != null) {
			ParsePosition pp = new ParsePosition(0);
			date = form.parse(val, pp);
			if (date == null) {
				throw new UnsupportedEncodingException("Date " + val + " parse error at position " + pp.getErrorIndex());
			}
		}
		
		return date;
	}


	/**
	 * Return object identifier
	 * 
	 * @return
	 * 		Object identifier
	 */
	public ObjectIdentifier getOID() {

		TLV oid = findTag(new Tag(0x06, (byte)0x00, false), null);
		if ((oid != null) && (oid instanceof ObjectIdentifier)) {
			return (ObjectIdentifier)oid;
		} else {
			return null;
		}
	}

	
	/**
	 * Test if directory attribute has given object identifier
	 * 
	 * @param testoid
	 * 		Object identifier to test for.
	 * @return
	 * 		True if the directory attribute has given object identifier
	 */	
	public boolean isOID(ObjectIdentifier testoid) {
		return getOID().equals(testoid);
	}
}

