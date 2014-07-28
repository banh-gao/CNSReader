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
 * Support for compact integer storage format
 *  
 * @author Andreas Schwier (info@cardcontact.de)
 */
public class CompactInteger  {
	protected int value;
	protected int sizeof;
	
	public CompactInteger(int newValue) {
		if (newValue < 0) 
			throw new NumberFormatException("Negative compact integer");
			
		value = newValue;

		if (value < 0x80)
			sizeof = 1;
		else if (value < 0x4000)
			sizeof = 2;
		else if (value < 0x200000)
			sizeof = 3;
		else
			sizeof = 4;
	}


	public CompactInteger(byte[] bytes, int ofs) {
		byte t;
		
		value = 0;
		sizeof = 0;
		do	{
			t = bytes[ofs];
			value <<= 7;
			value |= t & 0x7F;
			ofs++;
			sizeof++;
		} while (((t & 0x80) == 0x80) && (sizeof < 4));
		
		if ((t & 0x80) == 0x80)
			throw new NumberFormatException("Compact integer too long");
	}


	public CompactInteger(byte[] bytes) {
		this(bytes, 0);
	}
	
	
	public int sizeOf() {
		return sizeof;
	}
	
	
	public int intValue() {
		return value;
	}

	
	public byte[] getBytes() {
		byte[] bytes = new byte[sizeof];
		int i = sizeof - 1;
		int v = value;
		
		bytes[i--] = (byte)(v & 0x7F);
		
		while (i >= 0) {
			v >>= 7;
			bytes[i--] = (byte)((v & 0x7F) | 0x80);
		}
		return bytes;
	}
}
