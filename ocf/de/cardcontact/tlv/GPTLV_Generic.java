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

import de.cardcontact.tlv.Tag;

/**
 * Abstract base class for EMV, DGI and L16 encoded TLV objects
 * 
 * @author Andreas Schwier (www.cardcontact.de)
 */
public abstract class GPTLV_Generic {

	public final static int INVALID_SIZE = -1;
	
	/**
	 * Tag of the TLV object
	 */
	protected int tag;

	/**
	 * Data block of the TLV object
	 */
	protected byte[] data;

    
    
	public GPTLV_Generic(int tag, byte[] data) {
		this.tag = tag;
		this.data = data;
	}
	
    
    
	/**
	 * Return the encoded length field of the TLV object
	 * @return Encoded length field
	 */
	public byte[] getL() {
		return encodeLength();
	}

    
    
	/**
	 * Return the encoded length and value field of the TLV object
	 * @return Encoded length and value field
	 */
	public byte[] getLV() {
		byte[] encodedLength = encodeLength();

		byte[] tmp = new byte[encodedLength.length + data.length];

		System.arraycopy(encodedLength, 0, tmp, 0, encodedLength.length);
		System.arraycopy(data, 0, tmp, encodedLength.length, data.length);

		return tmp;
	}

    
    
	/**
	 * Return the tag of the TLV object
	 * @return Tag
	 */
	public int getTag() {
		return tag;
	}

    
    
	/**
	 * Return the encoded TLV structure of the object
	 * @return TLV structure
	 */
	public byte[] getTLV() {

		byte[] encodedTag = encodeTag();
		byte[] encodedLength = encodeLength();

		byte[] tmp = new byte[encodedTag.length + encodedLength.length + data.length];

		System.arraycopy(encodedTag, 0, tmp, 0, encodedTag.length);
		System.arraycopy(encodedLength, 0, tmp, encodedTag.length,
				encodedLength.length);
		System.arraycopy(data, 0, tmp,
				encodedTag.length + encodedLength.length, data.length);

		return tmp;
	}

    
    
	/**
	 * Return the encoded tag and length field of the TLV object
	 * @return Encoded tag and length field
	 */
	public byte[] getTV() {
		byte[] encodedTag = encodeTag();

		byte[] tmp = new byte[encodedTag.length + data.length];

		System.arraycopy(encodedTag, 0, tmp, 0, encodedTag.length);
		System.arraycopy(data, 0, tmp, encodedTag.length, data.length);

		return tmp;
	}

    
    
	/**
	 * Return the value field of the TLV object
	 * @return Value field
	 */
	public byte[] getValue() {
		return data;
	}


    
    /**
     * Helper function to determine bytes required to store the tag
     * 
     * @return Number of bytes required to store tag
     */
    public abstract int getTagFieldSizeHelper();

    
    
    /**
     * Encode tag field in byte array
     * @return
     */
    public abstract byte[] encodeTag();

    
    
	/**
	 * Helper function for getSize() and getLengthFieldSize()
	 * @return Size of length field in bytes
	 */
	public abstract int getLengthFieldSizeHelper();

    
    
	/**
	 * Encode length field in byte array
	 * @return Encoded length field
	 */
	public abstract byte[] encodeLength();
}
