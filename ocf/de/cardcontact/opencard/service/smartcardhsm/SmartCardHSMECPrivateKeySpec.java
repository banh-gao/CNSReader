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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.spec.ECField;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.EllipticCurve;

import de.cardcontact.tlv.ConstructedTLV;
import de.cardcontact.tlv.PrimitiveTLV;
import de.cardcontact.tlv.TLVEncodingException;



/**
 * This class contains the data for EC key pair generation. 
 * 
 * @author lew
 *
 */
public class SmartCardHSMECPrivateKeySpec extends SmartCardHSMPrivateKeySpec {



	private ECParameterSpec domainParameter;



	private EllipticCurve curve;



	/**
	 * The prime modulus
	 */
	private ECFieldFp field;



	private int keySize;



	/**
	 * SmartCardHSMECCPrivateKeySpec constructor
	 *
	 * @param car The Certificate Authority Reference
	 * @param chr The Certificate Holder Reference
	 * @param algorithm The key algorithm
	 * @param params The domain parameter
	 */
	public SmartCardHSMECPrivateKeySpec(String car, String chr, ECParameterSpec params) {
		super(car, chr);
		this.domainParameter = params;	

		this.curve = domainParameter.getCurve();
		this.field = (ECFieldFp)curve.getField();
		this.keySize = field.getFieldSize();
	}



	/**
	 * @return The domain parameter
	 */
	public ECParameterSpec getECParameterSpec() {
		return this.domainParameter;
	}


	/**
	 * @return The key size
	 */
	public int getKeySize() {
		return keySize;
	}


	/**
	 * @return The encoded Base Point G
	 * @throws IOException
	 */
	public byte[] getBasePointG() throws IOException {
		ByteArrayOutputStream basePointG = new ByteArrayOutputStream();
		basePointG.write(0x04);
		basePointG.write(unsignedBigIntegerToByteArray(domainParameter.getGenerator().getAffineX(), keySize));
		basePointG.write(unsignedBigIntegerToByteArray(domainParameter.getGenerator().getAffineY(), keySize));
		return basePointG.toByteArray();		
	}



	/**
	 * The command data for EC key pair generation.
	 * @return the tlv encoded c-data
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

		//Prime modulus p
		ECField field = domainParameter.getCurve().getField();
		int keySize = field.getFieldSize();				
		byte[] v = unsignedBigIntegerToByteArray(((ECFieldFp)field).getP(), keySize);
		puk.add(new PrimitiveTLV(0x81, v));	

		//First coefficient a
		v = unsignedBigIntegerToByteArray(domainParameter.getCurve().getA(), keySize);
		puk.add(new PrimitiveTLV(0x82, v));		

		//Second coefficient b	
		v = unsignedBigIntegerToByteArray(domainParameter.getCurve().getB(), keySize);
		puk.add(new PrimitiveTLV(0x83, v));		

		//Base point G 	
		ByteArrayOutputStream basePointG = new ByteArrayOutputStream();
		basePointG.write(0x04);
		basePointG.write(unsignedBigIntegerToByteArray(domainParameter.getGenerator().getAffineX(), keySize));
		basePointG.write(unsignedBigIntegerToByteArray(domainParameter.getGenerator().getAffineY(), keySize));
		puk.add(new PrimitiveTLV(0x84, basePointG.toByteArray()));		

		//Order of the base point
		v = unsignedBigIntegerToByteArray(domainParameter.getOrder(), keySize);
		puk.add(new PrimitiveTLV(0x85, v));	

		//Cofactor f				
		byte [] cofactor = {(byte) domainParameter.getCofactor()};
		puk.add(new PrimitiveTLV(0x87, cofactor));	

		gakpcdata.add(puk);

		//CHR
		gakpcdata.add(new PrimitiveTLV(0x5F20, getCertificateHolderReference()));	

		//Outer Certificate Authority Reference for authentication signature if P2 != '00'
		if (hasOuterCar()) {							
			gakpcdata.add(new PrimitiveTLV(0x45, getOuterCar()));	
		}	

		return gakpcdata.getValue();		
	}
}
