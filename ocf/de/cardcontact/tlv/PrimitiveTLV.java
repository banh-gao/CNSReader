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
import java.util.Arrays;
import java.util.Date;
import java.util.SimpleTimeZone;



/**
 * Base class for all primitive TLV objects
 *  
 * @author Andreas Schwier (info@cardcontact.de)
 */

public class PrimitiveTLV extends TLV {
	protected byte[] value = null;


	
	/**
	 * Create a primitive TLV object with tag and value
	 * 
	 * @param newtag
	 * 		Tag as object
	 * @param newvalue
	 * 		Byte array containing value
	 */
	public PrimitiveTLV(Tag newtag, byte[] newvalue) {
		tag = newtag;
		value = newvalue;
	}



	/**
	 * Create a primitive TLV object with tag given as integer value
	 * 
	 * @param newTagValue
	 * 		Tag as integer value
	 * @param newvalue
	 * 		Byte array containing value
	 * @throws TLVEncodingException 
	 */
	public PrimitiveTLV(int newTagValue, byte[] newvalue) throws TLVEncodingException {
		tag = new Tag(newTagValue);
		value = newvalue;
	}



	/**
	 * Create a primitive TLV object from binary data in
	 * buffer at given offset
	 * 
	 * @param buffer
	 * 		Buffer containing TLV object
	 * @param offset
	 * 		Offset in buffer
	 * @throws TLVEncodingException 
	 */
	public PrimitiveTLV(byte[] buffer, int offset) throws TLVEncodingException {
		int length;
		
		tag = new Tag(buffer, offset);
		offset += tag.getSize();
		
		length = lengthFromByteArray(buffer, offset, alternateLengthFormat);
		offset += getLengthFieldSizeHelper(length, alternateLengthFormat);

		value = new byte[length];
		System.arraycopy(buffer, offset, value, 0, length);
	}



	/**
	 * Create a primitive TLV object or structure from binary
	 *  
	 * @param buffer
	 * 		Binary data containing TLV structure
	 * @throws TLVEncodingException 
	 */
	public PrimitiveTLV(byte[] buffer) throws TLVEncodingException {
		this(buffer, 0);
	}



	public PrimitiveTLV(ParseBuffer pb) throws TLVEncodingException {
		int length;
		
		tag = new Tag(pb);
		
		if (alternateLengthFormat) 
			length = pb.getDGILength();
		else
			length = pb.getDERLength();

        if (length > pb.remaining()) {
            throw new TLVEncodingException("Length field (" + length + ") exceeds value field (" + pb.remaining() + ").");
        }
		value = new byte[length];		
		pb.get(value, 0, length);
	}



	/**
	 * Store value in binary buffer
	 * 
	 * @param buffer
	 * 		Byte array that received the binary data
	 * @param offset
	 * 		Offset in byte array
	 * @return
	 * 		New offset behind the stored object
	 */
	protected int valueToByteArray(byte[] buffer, int offset) {
		if (value != null) {
			System.arraycopy(value, 0, buffer, offset, value.length);
			offset += value.length;
		}
		return offset;
	}


	
	/**
	 * Store primitive object to binary buffer
	 * 
	 * @param buffer
	 * 		Byte array that received the binary data
	 * @param offset
	 * 		Offset in byte array
	 * @return
	 * 		New offset behind the stored object
	 */
	protected int toByteArray(byte[] buffer, int offset) {
		int length = value == null ? 0 : value.length;

		offset = tag.toByteArray(buffer, offset);
		offset = lengthToByteArray(length, buffer, offset, alternateLengthFormat);
		if (value != null) {
			System.arraycopy(value, 0, buffer, offset, value.length);
			offset += value.length;
		}
		return offset;
	}


	
	/**
	 * Return length of value field
	 *  
	 * @return
	 * 		Length in bytes
	 */
	public int getLength() {
		return (value == null) ? 0 : value.length;
	}
	 


	/**
	 * Return the value
	 * @return
	 * 		Byte array containing the value
	 */
	public byte[] getValue() {
		return value;
	}
	
	

	/**
	 * Return value as date
	 * 
	 * @return Date
	 * @throws UnsupportedEncodingException
	 */
	public Date getDate() throws UnsupportedEncodingException {
	    Date date = null;
	    boolean utc = false;
	    
	    String str = new String(value, "8859_1");
	    String format; 
	    
	    if (str.length() == 11) {
	    	format = "yyMMddHHmm";
	    } else if (str.length() == 13) {
	    	format = "yyMMddHHmmss";	    	
	    } else {
	    	format = "yyyyMMddHHmmss";
	    }	    
	    
	    if (str.length() > 14) {
	        if ((str.charAt(14) == '.') || (str.charAt(14) == ',')) {
	            format += ".SSS";
	        }	        
	    }
	    
	    if (str.endsWith("Z")) {
            utc = true;
        } else if ((str.charAt(str.length() - 5) == '-') || (str.charAt(str.length() - 5) == '+')) {
            format += "Z";
        }
	    
	    SimpleDateFormat formatter = new SimpleDateFormat(format);

	    if (utc) {
	        formatter.setTimeZone(new SimpleTimeZone(0,"Z"));
	    }
            
	    ParsePosition pp = new ParsePosition(0);
	    date = formatter.parse(str, pp);
	    if (date == null) {
	        throw new UnsupportedEncodingException("Date " + str + " parse error at position " + pp.getErrorIndex());
	    }
	    
	    return date;
	}
	
	
	
	/**
	 * Test for equality
	 * 
	 * @param testtlv
	 * 		Object to test for
	 * @return
	 * 		True if object identifiers are equal
	 */	
	public boolean equals(Object testtlv) {
		if (!(testtlv instanceof PrimitiveTLV))
			return false;
			
		return Arrays.equals(value, ((PrimitiveTLV)testtlv).value);
	}
	
	
	
	/**
	 * Return dump of primitive TLV object using a given left indentation
	 * @param indent
	 * 		Left indentation to be used
	 * @return
	 * 		String containing dump of primitive TLV object 
	 */
	public String dump(int indent) {
		StringBuffer buffer = new StringBuffer(80);
		
		for (int i = 0; i < indent; i++) {
			buffer.append(' ');
		}
		if (name != null) {
		    buffer.append(name);
		    buffer.append(' ');
		}
		buffer.append(tag.toString());
		buffer.append(" SIZE( "+ value.length + " )");
		buffer.append('\n');
		buffer.append(HexString.dump(value, 0, value.length, 16, indent + 2));
		return buffer.toString();
	}
	
	
	
	/**
	 * Return Tag of TLV object as string
	 * 
	 * @return
	 * 		String containing name of TLV object 
	 */	
	public String toString() {
		StringBuffer buffer = new StringBuffer(80);

		if (name != null) {
			buffer.append(name);
			buffer.append(' ');
		}

		buffer.append(tag.toString());
		if (tag.getClazz() == Tag.UNIVERSAL) {
			try	{
				switch(tag.getNumber()) {
				case Tag.UTF8String:
					buffer.append(" \"");
					buffer.append(new String(value, "UTF-8"));
					buffer.append('"');
					break;
				case Tag.PrintableString:
				case Tag.NumericString:
				case Tag.BMPString:
				case Tag.T61String:
				case Tag.GeneralString:
				case Tag.UniversalString:
				case Tag.UTCTime:
				case Tag.GeneralizedTime:
					buffer.append(" \"");
					buffer.append(new String(value, "8859_1"));
					buffer.append('"');
					break;
				default:
					buffer.append(' ');
				buffer.append(HexString.hexifyByteArray(value));
				}
			}
			catch(UnsupportedEncodingException e) {
				buffer.append(' ');
				buffer.append(HexString.hexifyByteArray(value));
			}
		} else {
			buffer.append(' ');
			buffer.append(HexString.hexifyByteArray(value));
		}

		return buffer.toString();
	}

	
	
    /**
     * Return number of childs, of object is constructed
     * 
     * @see de.cardcontact.tlv.TreeNode#getChildCount()
     */
    public int getChildCount() {
        return 0;
    }



    /**
     * Return true 
     * @see de.cardcontact.tlv.TreeNode#isLeaf()
     */
    public boolean isLeaf() {
        return true;
    }



    /**
     * Return parent - This we don't know
     * 
     * @see de.cardcontact.tlv.TreeNode#getParent()
     */
    public TreeNode getParent() {
        return null; // Not known
    }



    /**
     * Return child at index - No childs for PrimitiveTLV
     * 
     * @see de.cardcontact.tlv.TreeNode#getChildAt(int)
     */
    public TreeNode getChildAt(int index) {
        return null; // Not supported
    }



    /**
     * Return index of child - No childs for PrimitiveTLV
     * 
     * @see de.cardcontact.tlv.TreeNode#getIndex(de.cardcontact.tlv.TreeNode)
     */
    public int getIndex(TreeNode child) {
        return -1; // Not supported
    }
}
