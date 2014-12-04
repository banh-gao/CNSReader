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
 * Class implementing EMV encoded TLV objects
 * 
 * @author Andreas Schwier (www.cardcontact.de)
 */
public class GPTLV_EMV extends GPTLV_Generic {

	public GPTLV_EMV(int tag, byte[] data) throws TagSizeException, TLVDataSizeException {
		super(tag, data);
		
		if (getLengthFieldSizeHelper() == GPTLV_Generic.INVALID_SIZE) {
			throw new TLVDataSizeException("Illegal data size! EMV supports only up to 4 byte length fields !");
		}		
	}


    
    @Override
	public int getLengthFieldSizeHelper() {
        int size = 1;
        
        if (data.length >= 0x80)
            size++;
        if (data.length >= 0x100)
            size++;
        if (data.length >= 0x10000)
            size++;
        if (data.length >= 0x1000000) {
            return GPTLV_Generic.INVALID_SIZE;
        }
        return size;
	}

    
    
    @Override
	public byte[] encodeLength() {
        int length = data.length;
        int size = getLengthFieldSizeHelper();
        int i = 0;
        byte[] encodedLength = new byte[size];
        int offset = 0;

        if (size > 1) {
            encodedLength[offset++] = (byte) (0x80 | (size - 1));
            i = (size - 2) * 8;
        }

        for (; i >= 0; i -= 8) {
            encodedLength[offset++] = (byte) (length >> i);
        }

        return encodedLength;
	}
	


    @Override
    public byte[] encodeTag() {
        byte[] t = new byte[getTagFieldSizeHelper()];
        int akku = tag;
        for (int i = t.length - 1; i >= 0; i--) {
            t[i] = (byte)(akku & 0xFF);
            akku >>= 8;
        }
        return t;
    }

    
    
    @Override
    public int getTagFieldSizeHelper() {
        if (tag >= 0x01000000)
            return 4;
        else if (tag >= 0x010000)
            return 3;
        else if (tag >= 0x0100)
            return 2;
        return 1;
    }
}
