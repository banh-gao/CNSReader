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

import java.math.BigInteger;

import de.cardcontact.tlv.ConstructedTLV;
import de.cardcontact.tlv.TLVEncodingException;
import opencard.opt.security.PrivateKeyRef;



/**
 * Reference to the private key on the SmartCardHSM
 * 
 * @author lew
 * 
 * @see opencard.opt.security.PrivateKeyRef
 *
 */
public class SmartCardHSMKey implements PrivateKeyRef {


	/**
	 * 
	 */
	private static final long serialVersionUID = -464439997111473313L;



	/**
	 * The ID which refers to the private key on the card.
	 */
	private byte keyID;



	private String label;



	private short keySize;



	private byte[] description;



	public SmartCardHSMKey(byte keyID, String label, short keySize) {
		this.keyID = keyID;
		this.label = label;
		this.keySize = keySize;
	}



	@Override
	public String getAlgorithm() {
		return "RSA";
	}



	public byte getKeyID() {
		return keyID;
	}



	public void setKeyID(byte keyID) {
		this.keyID = keyID;
	}



	public String getLabel() {
		return label;
	}



	public void setLabel(String label) {
		this.label = label;
	}



	public short getKeySize() {
		return keySize;
	}



	public void setKeySize(short keySize) {
		this.keySize = keySize;
	}



	@Override
	public byte[] getEncoded() {
		return description;
	}



	@Override
	public String getFormat() {
		return null;
	}



	/**
	 * Set key description and also set label and key size.
	 * @param description
	 */
	public void setDescription(byte[] description) {
		this.description = description;
		setLabelFromDescription();
		setKeySizeFromDescription();
	}



	private void setLabelFromDescription() {
		if (this.label == null || this.label.equals("")) {		
			ConstructedTLV descr = null;
			try {
				descr = new ConstructedTLV(this.description);
			} catch (TLVEncodingException e) {
				e.printStackTrace();
			}
			byte[] value = ((ConstructedTLV)descr.get(0)).get(0).getValue(); 
			String label = new String(value);
			setLabel(label);
		}			
	}



	private void setKeySizeFromDescription() {
		ConstructedTLV descr = null;
		byte[] value = null;
		
		try {
			descr = new ConstructedTLV(this.description);
			de.cardcontact.tlv.Tag tagA1  = new de.cardcontact.tlv.Tag(0xA1);
			value = ((ConstructedTLV) ((ConstructedTLV)descr.findTag(tagA1, null)).get(0)).get(1).getValue();
		} catch (TLVEncodingException e) {
			e.printStackTrace();
		}
				
		BigInteger keysize = byteArrayToUnsignedBigInteger(value);
		this.keySize = keysize.shortValue();
	}



	private BigInteger byteArrayToUnsignedBigInteger(byte[] data) {
		byte[] absoluteValue = new byte[data.length + 1];
		System.arraycopy(data, 0, absoluteValue, 1, data.length);
		return new BigInteger(absoluteValue);
	}



	@Override
	public String toString() {
		return "Label=" + label + ", KeyID=" + keyID + ", Size=" + keySize + " bits"; 
	}
}
