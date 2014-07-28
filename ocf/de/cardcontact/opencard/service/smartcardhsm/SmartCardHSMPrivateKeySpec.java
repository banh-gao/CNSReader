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
import java.math.BigInteger;
import java.security.spec.KeySpec;

import de.cardcontact.tlv.TLVEncodingException;



/**
 * This class contains data for key pair generation. 
 * 
 * @author lew
 *
 * @see java.security.spec.KeySpec
 */
public abstract class SmartCardHSMPrivateKeySpec implements KeySpec{



	private String certificateHolderReference;



	private byte[] algorithm;



	private byte cpi = (byte) 0x00;



	private String car;



	private String outerCar;



	/**
	 * 
	 * @param car Certificate Authority Reference
	 * @param chr Certificate Holder Reference
	 * @param algorithm The key algorithm
	 */
	public SmartCardHSMPrivateKeySpec(String car, String chr) {
		setCar(car);
		this.certificateHolderReference = chr;
	}



	public byte[] getCertificateHolderReference() {
		return certificateHolderReference.getBytes();
	}



	public void setCertificateHolderReference(String certificateHolderReference) {
		this.certificateHolderReference = certificateHolderReference;
	}



	public byte[] getAlgorithm() {
		return this.algorithm;
	}



	public void setAlgorithm(byte[] algorithm) {
		this.algorithm = algorithm;
	}



	public byte getCpi() {
		return cpi;
	}



	public void setCpi(byte cpi) {
		this.cpi = cpi;
	}



	public void setCar(String car) {
		this.car = car;
	}



	public byte[] getCar() {
		return car.getBytes();
	}



	public boolean hasCar() {
		return this.car != null;
	}



	public void setOuterCar(String outerCar) {
		this.outerCar = outerCar;
	}



	public byte[] getOuterCar() {
		return outerCar.getBytes();
	}



	public boolean hasOuterCar() {
		return this.outerCar != null;
	}



	public byte[] getCData() throws IOException, TLVEncodingException{ 
		return null;
	}



	/**
	 * Convert unsigned big integer into byte array, stripping of a
	 * leading 00 byte
	 *
	 * This conversion is required, because the Java BigInteger is a signed
	 * value, whereas the byte arrays containing key components are unsigned by default
	 * 
	 * @param bi    BigInteger value to be converted
	 * @param size  Number of bits
	 * @return      Byte array containing unsigned integer value
	 */
	protected static byte[] unsignedBigIntegerToByteArray(BigInteger bi, int size) {
		byte[] s = bi.toByteArray();
		size = (size >> 3) + ((size & 0x7) == 0 ? 0 : 1);
		byte[] d = new byte[size];
		int od = size - s.length;
		int os = 0;
		if (od < 0) {  // Number is longer than expected
			if ((od < -1) || s[0] != 0) {   // If it is just a leading zero, then we cut it off
				throw new IllegalArgumentException("Size mismatch converting big integer to byte array");
			}
			os = -od;
			od = 0;
		}
		size = size - od;

		System.arraycopy(s, os, d, od, size);
		return d;
	}
}
