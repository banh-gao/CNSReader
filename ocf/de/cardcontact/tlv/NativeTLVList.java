/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2013 CardContact Software & System Consulting
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.cardcontact.tlv.ParseBuffer;
import de.cardcontact.tlv.TLVEncodingException;



/**
 * TLVList
 * 
 * @author lew
 */
public class NativeTLVList {

	private List<GPTLV_Generic> entries = new ArrayList<GPTLV_Generic>();


	/**
	 * Create a new TLVList
	 * 
	 * @param tlv
	 */
	public NativeTLVList(GPTLV_Generic tlv) {
		entries.add(tlv);
	}



	/**
	 * Create a new TLVList from a given
	 * EMV encoded byte array.
	 * 
	 * @param data The EMV encoded data
	 * @throws TLVEncodingException
	 * @throws TagSizeException
	 * @throws TLVDataSizeException
	 */
	public NativeTLVList(byte[] data) throws TLVEncodingException, TagSizeException, TLVDataSizeException {
		ParseBuffer pb = new ParseBuffer(data);

		while(pb.hasRemaining()) {
			int tag = pb.getTag();
			int length = pb.getDERLength();
			byte[] value = new byte[length];
			pb.get(value, 0, length);

			GPTLV_EMV emv = new GPTLV_EMV(tag, value);
			entries.add(emv);
		}
	}



	/**
	 * Get the element at the specified position
	 * @param i the position
	 * @return tlv entrie
	 */
	public GPTLV_Generic get(int i) {
		return entries.get(i);
	}



	/**
	 * The number of entries
	 * @return
	 */
	public int getLength() {
		return entries.size();
	}



	/**
	 * Get the TLV encoded byte array
	 * 	
	 * @return tlv byte array
	 */
	public byte[] getBytes() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (GPTLV_Generic tlv : entries) {
			try {
				bos.write(tlv.getTLV());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return bos.toByteArray();
	}
}
