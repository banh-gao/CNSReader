/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2012 CardContact Software & System Consulting
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

package de.cardcontact.opencard.service.smartcardhsm;

import java.io.IOException;

import de.cardcontact.tlv.ConstructedTLV;
import de.cardcontact.tlv.PrimitiveTLV;
import de.cardcontact.tlv.TLVEncodingException;



/**
 * This class contains the data for EC key pair generation. 
 * 
 * @author lew
 *
 */
public class SmartCardHSMRSAPrivateKeySpec extends SmartCardHSMPrivateKeySpec {
	
	
	
	private int exponent;
	
	
	
	private int modulusSize;
	
	
	
	/**
	 * SmartCardHSMRSAPrivateKeySpec constructor
	 *
	 * @param car The Certificate Authority Reference
	 * @param chr The Certificate Holder Reference
	 * @param algorithm The key algorithm
	 * @param params The domain parameter
	 */
	public SmartCardHSMRSAPrivateKeySpec(String car, String chr, int exponent, int size) {
		super(car, chr);
		this.exponent = exponent;
		this.modulusSize = size;
	}
	
	
	
	/**
	 * The command data for RSA key pair generation.
	 * @return The TLV encoded c-data
	 */	
	public byte[] getCData() throws IOException, TLVEncodingException {
		
		ConstructedTLV gakpcdata = new ConstructedTLV(0x30);
		
		//CPI
		byte[] cpi = {getCpi()};							
		gakpcdata.add(new PrimitiveTLV(0x5F29, cpi));
		
		//CAR
		if (hasCar()) {
			gakpcdata.add(new PrimitiveTLV(0x42, getCar()));
		}
		
		//Public Key
		ConstructedTLV puk = new ConstructedTLV(0x7F49);
		
		//Public Key Algorithm
		puk.add(new PrimitiveTLV(0x06, getAlgorithm()));
		
		
		//Public exponent
		byte[] exp = intToByteArray(exponent);
		puk.add(new PrimitiveTLV(0x82, exp));												
		
		//Key size
		puk.add(new PrimitiveTLV(0x02, intToByteArray(modulusSize)));
		
		//CHR
		gakpcdata.add(puk);
		gakpcdata.add(new PrimitiveTLV(0x5f20, getCertificateHolderReference()));
		
		//Outer Certificate Authority Reference for authentication signature if P2 != '00'
		if (hasOuterCar()) {								
			gakpcdata.add(new PrimitiveTLV(0x45, getOuterCar()));
		}	
		
		return gakpcdata.getValue();
	}
	
	
	
	/**
	 * @return The size of the modulus
	 */
	public int getModulusSize() {
		return modulusSize;
	}

	/**
	 * @param modulusSize
	 */
	public void setModulusSize(int modulusSize) {
		this.modulusSize = modulusSize;
	}
	
	
	/** 
	 * @param num
	 * @return A new byte[] containing the number
	 */
	private static byte[] intToByteArray(int num) {
		double countBits = (Math.log(num) / Math.log(2));
		int length = (int) Math.round((countBits / 8) + 0.5);
		
		byte[] b = new byte[length];
		int j = length;
		for (int i = 0; i < length; i++) {
			int shift = 8 * --j;
			b[i] = (byte)(num >> shift);
		}
		
		return b;
	}
}
