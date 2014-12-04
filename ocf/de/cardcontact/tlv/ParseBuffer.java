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

/**
 * Object used to parse binary buffers
 * 
 * @author Andreas Schwier (info@cardcontact.de)
 */
public class ParseBuffer {
	private byte[] buffer = null;
	private int cursor;
	private int mark;
	private int limit;
	
	/**
	 * Create ParseBuffer using bytes at given range in byte array
	 * 
	 * @param newBuffer
	 * 		Binary array
	 * @param newCursor
	 * 		Offset in buffer to start at
	 * @param newLength
	 * 		Length of region in buffer to parse
	 */
	public ParseBuffer(byte[] newBuffer, int newCursor, int newLength) {
		buffer = newBuffer;
		cursor = newCursor;
		mark = newCursor;
		limit = newCursor + newLength;
	}

	
	
	/**
	 * Create ParseBuffer to parse entire byte array
	 * 
	 * @param newBuffer
	 * 		Binary array
	 */
	public ParseBuffer(byte[] newBuffer) {
		this(newBuffer, 0, newBuffer.length);	
	}

	

	/**
	 * Mark current parser position
	 *
	 */
	public void mark() {
		mark = cursor;
	}

	
	
	/**
	 * Reset current parser position to previously set mark
	 *
	 */
	public void reset() {
		cursor = mark;
	}



	/**
	 * Return number of remaining bytes in parse buffer
	 * 
	 * @return
	 * 		Number of bytes
	 */
	public int remaining() {
		return limit - cursor;
	}
	
	
	
	/**
	 * Returns the current upper limit for the parse buffer
	 * 
	 * @return the upper limit
	 */
	public int getLimit() {
		return limit;
	}
	
	
	
	/**
	 * Sets the upper limit for the parse buffer
	 * 
	 * @param limit
	 */
	public void setLimit(int limit) {
		if (limit > buffer.length)
			throw new IllegalArgumentException("Can't set new limit if it exceeds buffer");
		
		this.limit = limit;
	}
	
	
	
	/**
	 * Set new length of parse region starting at current offset
	 * 
	 * Throws IllegalArgumentException if the length exceed the
	 * underlying byte array
	 * 
	 * @param newLength
	 * 		New length starting at current offset
	 */
	public void setLength(int newLength) {
		if (newLength >= 0) {
			if (cursor + newLength > buffer.length)
				throw new IllegalArgumentException("Can't set new length if it exceeds buffer");
			limit = cursor + newLength;
		}
	}
	
	
	
	/**
	 * Return true if bytes are available to parse
	 * @return
	 * 		true if more bytes are available
	 */
	public boolean hasRemaining() {
		return cursor < limit;
	}
	
	
	
	/**
	 * Get next byte from parse buffer. Advance current position
	 * by one.
	 * 
	 * @return
	 * 		Byte at current parse position
	 * 
	 * @throws TLVEncodingException
	 * 		End of region reached
	 */
	public byte get() throws TLVEncodingException {
		if (cursor >= limit)
			throw new TLVEncodingException("End of buffer");

		return buffer[cursor++];
	}
	


	/**
	 * Bulk get from parse buffer
	 * 
	 * Get specified number of bytes from buffer
	 * 
	 * @param dst
	 * 		Receiving buffer
	 * @param offset
	 * 		Offset in receiving buffer
	 * @param length
	 * 		Number of bytes to get
	 * @return
	 * 		This ParseBuffer object
	 * 
	 * @throws TLVEncodingException
	 * 		Given length exceeds parse region
	 */	
	public ParseBuffer get(byte[] dst, int offset, int length) throws TLVEncodingException {
		if (length > limit - cursor)
			throw new TLVEncodingException("Invalid length field");

		System.arraycopy(buffer, cursor, dst, offset, length);
		cursor += length;
		
		return this;
	}

	

	/**
	 * Return next two bytes as unsigned integer
	 * 
	 * @return value of next two byte in MSB/LSB encoding
	 * 
	 * @throws TLVEncodingException
	 */
	public int getUnsignedWord() throws TLVEncodingException {
		int value;

		if (cursor + 1 >= limit)
			throw new TLVEncodingException("End of buffer");

		value  = (buffer[cursor++] & 0xFF) << 8;
		value +=  buffer[cursor++] & 0xFF;
		return value;
	}



	/**
	 * Get DGI coded length
	 * 
	 * Values between 0 and 254 are encoded in one byte
	 * Values between 255 and 65535 are encoded in three bytes with the
	 * first byte set to 'FF'
	 * 
	 * @return
	 * 		Length
	 * @throws TLVEncodingException
	 * 		End of region reached during decoding
	 */	
	public int getDGILength() throws TLVEncodingException {
		int length;
		int i = 1;

		if (cursor >= limit)
			throw new TLVEncodingException("End of buffer");

		if (buffer[cursor] == (byte)0xFF) {
			i = 2;
			cursor++;
		}

		if (cursor + i > limit)
			throw new TLVEncodingException("Invalid DGI length field");

		length = 0;
		for (; i > 0; i--) {
			length = (length << 8) | (buffer[cursor++] & 0xFF);
		}

		return length;
	}

	

	/**
	 * Get DER coded length
	 * 
	 * Values between 0 and 127 are encoded in one byte
	 * Values between 128 and 255 are encoded in two bytes
	 * Values between 256 and 65535 are encoded in three bytes
	 * Values between 65536 and 2^24 - 1 are encoded in four bytes
	 * Values between 2^24 and 2^32 - 1 are encoded in five byte
	 * 
	 * For value >= 128 the first byte encoded the number of trailing
	 * bytes plus '80'
	 *  
	 * @return
	 * 		Length or -1 if variable length BER encoding
	 * @throws TLVEncodingException
	 * 		End of region reached during decoding
	 */	
	public int getDERLength() throws TLVEncodingException {
		int length;
		int i = 1;

		if (cursor >= limit)
			throw new TLVEncodingException("End of buffer");

		if ((buffer[cursor] & 0x80) == 0x80) {
			i = buffer[cursor] & 0x07;
			cursor++;
			if (i == 0) {
				return -1;		// Variable length BER encoding
			}
		}

		if (i > 4)
			throw new TLVEncodingException("More than 4 bytes in length field");

		if (cursor + i > limit)
			throw new TLVEncodingException("Invalid DER length field");

		length = 0;
		for (; i > 0; i--) {
			length = (length << 8) | (buffer[cursor++] & 0xFF);
		}

		return length;
	}



	/**
	 * Get variable length encoded tag as used in DER/BER encoding
	 * @return tag value
	 * 
	 * @throws TLVEncodingException
	 */
	public int getTag() throws TLVEncodingException {
		int value;

		if (cursor >= limit)
			throw new TLVEncodingException("End of buffer");

		value = buffer[cursor++] & 0xFF;
		if ((value & 0x1F) == 0x1F) {
			int i = 4;
			do  {
				if (cursor >= limit)
					throw new TLVEncodingException("End of buffer");
				value <<= 8;
				value += buffer[cursor++] & 0xFF;
				i--;
			} while (((value & 0x80) == 0x80) && (i > 0));
		}
		return value;
	}
}
