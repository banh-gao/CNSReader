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

package de.cardcontact.opencard.security;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;

import de.cardcontact.opencard.service.isocard.IsoConstants;

import opencard.core.service.CardServiceInvalidCredentialException;
import opencard.core.service.CardServiceInvalidParameterException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.HexString;
import opencard.opt.util.TLV;
import opencard.opt.util.Tag;


/**
 * <p>Class that implements secure messaging according to ISO7816-4 and specifically the profile from
 * CWA 14890 (eSign-K), eGK and Extended Access Control 2.0.</p>
 * 
 * <p>The class has support for MAC protection of command and response APDU as well as encryption
 * of command and response APDUs.</p>
 * 
 * <p>It supports send sequence counter for encryption and for MAC which can be incremented individually or in 
 * a synchronized way.</p>
 * 
 * <p>The class implements the SecureChannel interface and is as such a suitable APDU wrapper for
 * the IsoCardService and TransparentCardService class.</p>
 * 
 * @author Andreas Schwier (info@cardcontact.de)
 */
public class IsoSecureChannel implements SecureChannel {

	public enum SSCPolicyEnum { DEFAULT, SYNC, SYNC_AND_ENCRYPT };
	
	protected String provider;
	protected String macalgorithm;
	protected String cipheralgorithm;
	protected Key kenc;
	protected Key kmac;
	protected byte[] encssc;
	protected byte[] macssc;
	protected byte[] iv;
	protected byte[] crt;
	protected int maclen;
	protected int blocklen;
	protected SSCPolicyEnum sscpolicy = SSCPolicyEnum.DEFAULT;
	
	private static byte[] one = { (byte) 0x01 };

	private final static boolean singleByteT97 = false;	// Encode Le in tag 97 in one or two bytes
	
	
	/**
	 * Create uninitialised secure channel object
	 * 
	 * The crypto provider is preset with "BC"
	 */
	public IsoSecureChannel() {
		this("BC");
	}
	
	
	
	/**
	 * Create uninitialised secure channel object
	 * 
	 * @param provider Cryptographic service provider for JCE
	 * 
	 */
	public IsoSecureChannel(String provider) {
		this.provider = provider;
		this.kenc = null;
		this.kmac = null;
		this.encssc = null;
		this.macssc = null;
		this.iv = new byte[8];
		this.maclen = 8;
		this.blocklen = 8;
		this.crt = null;
	}

	
	
	/**
	 * Increments a send sequence counte
	 * 
	 * @param ssc the send sequence counter
	 * @return the ssc incremented by one
	 */
	protected static byte[] incrementSSC(byte[] ssc) {
		BigInteger bi = new BigInteger(1, ssc);
		BigInteger b1 = new BigInteger(one);
		
		bi = bi.add(b1);
		
		byte[] r = new byte[ssc.length];
		byte[] ri = bi.toByteArray();
		
		int riofs, rofs, length;
		
		if (ri.length > r.length) {
			riofs = ri.length - r.length;
			rofs = 0;
			length = r.length;
		} else {
			riofs = 0;
			rofs = r.length - ri.length;
			length = ri.length;
		}
		
		System.arraycopy(ri, riofs, r, rofs, length);
		return r;
	}
	
	
	
	/**
	 * Increment send sequence counter for MAC by one
	 *
	 */
	protected void incrementMACSSC() {
		this.macssc = incrementSSC(this.macssc);
	}

	

	/**
	 * Increment send sequence counter for encryption by one
	 *
	 */
	protected void incrementENCSSC() {
		this.encssc = incrementSSC(this.encssc);
	}



	/**
	 * Determine the initialisation vector for encryption
	 * 
	 * @param cipher the cipher to use
	 * @return the initialisation vector
	 * @throws GeneralSecurityException
	 */
	protected byte[] getIV(Cipher cipher) throws GeneralSecurityException {
		byte[] iiv = this.iv;
		if (this.sscpolicy == SSCPolicyEnum.SYNC_AND_ENCRYPT) {		// Encrypt send sequence counter
			IvParameterSpec dps = new IvParameterSpec(iiv);
			cipher.init(Cipher.ENCRYPT_MODE, this.kenc, dps); 
			
			iiv = cipher.doFinal(this.macssc);
		} else if (this.sscpolicy == SSCPolicyEnum.SYNC) {			// Use a single SSC
			iiv = this.macssc;
		} else {
			if (this.encssc != null) {
				iiv = this.encssc;
			}
		}
		return iiv;
	}



	/**
	 * Encode the body into a TLV encoded secure messaging body for even INS bytes
	 * 
	 * @param body the unwrapped body
	 * @param isEncrypted true if the body is encrypted
	 * @param isProtected true if the body is going to be MAC protected
	 * @return the encoded body
	 */
	protected byte[] encodeBodyEvenINS(byte[] body, boolean isEncrypted, boolean isProtected) {
		TLV t = null;
		if (isEncrypted) {					// We encipher the body
			if (this.encssc != null) {		// Send Sequence Counter
				incrementENCSSC();
			}

			int paddedLength = (body.length & ~(this.blocklen - 1)) + this.blocklen;
			byte[] cryptoInput = new byte[paddedLength];
			System.arraycopy(body, 0, cryptoInput, 0, body.length);
			cryptoInput[body.length] = (byte)0x80;
			
			try {
				Cipher cipher = Cipher.getInstance(this.cipheralgorithm, this.provider);

				byte[] iiv = getIV(cipher);
				IvParameterSpec dps = new IvParameterSpec(iiv);
				cipher.init(Cipher.ENCRYPT_MODE, this.kenc, dps); 
				
				byte[] cipherText = cipher.doFinal(cryptoInput);
				byte[] tlvbody = new byte[cipherText.length + 1];
				tlvbody[0] = (byte)0x01;
				System.arraycopy(cipherText, 0, tlvbody, 1, cipherText.length);
				t = new TLV(new Tag((isProtected ? 7 : 6), (byte)2, false), tlvbody);
			}
			catch(Exception e) {
				throw new CardServiceInvalidParameterException(e.getMessage());
			}
			// Special handling for Micardo
			// Increment SSC if we send 
			if (!isProtected && (this.macssc != null)) {
				incrementMACSSC();
			}
		} else {    // We send the body in plain
			// Create DO81 with plain
			t = new TLV(new Tag((isProtected ? 1 : 0), (byte)2, false), body);
		}
		return t.toBinary();
	}

	
	
	/**
	 * Encode the body into a TLV encoded secure messaging body
	 * 
	 * @param body the unwrapped body
	 * @param isEncrypted true if the body is encrypted
	 * @param isProtected true if the body is going to be MAC protected
	 * @return the encoded body
	 */
	protected byte[] encodeBodyOddINS(byte[] body, boolean isEncrypted, boolean isProtected) {
		TLV t = null;
		if (isEncrypted) {					// We encipher the body
			if (this.encssc != null) {		// Send Sequence Counter
				incrementENCSSC();
			}

			int paddedLength = (body.length & ~(this.blocklen - 1)) + this.blocklen;
			byte[] cryptoInput = new byte[paddedLength];
			System.arraycopy(body, 0, cryptoInput, 0, body.length);
			cryptoInput[body.length] = (byte)0x80;
			
			try {
				Cipher cipher = Cipher.getInstance(this.cipheralgorithm, this.provider);

				byte[] iiv = getIV(cipher);
				IvParameterSpec dps = new IvParameterSpec(iiv);
				cipher.init(Cipher.ENCRYPT_MODE, this.kenc, dps); 
				
				byte[] cipherText = cipher.doFinal(cryptoInput);
				t = new TLV(new Tag((isProtected ? 5 : 4), (byte)2, false), cipherText);
			}
			catch(Exception e) {
				throw new CardServiceInvalidParameterException(e.getMessage());
			}
			// Special handling for Micardo
			// Increment SSC if we send 
			if (!isProtected && (this.macssc != null)) {
				incrementMACSSC();
			}
		} else {    // We send the body in plain
			// Create DO81 with plain
			t = new TLV(new Tag((isProtected ? 3 : 2), (byte)2, true), body);
		}
		return t.toBinary();
	}

	
	
	/**
	 * Calculates the MAC
	 * 
	 * @param cla the class byte
	 * @param ins the instruction byte
	 * @param p1 the parameter p1
	 * @param p2 the parameter p2
	 * @param do81or87 the data body object
	 * @param doLe the Le object
	 * @return the cryptogram
	 */
	protected byte[] calculateMAC(byte cla, byte ins, byte p1, byte p2, byte[] do81or87, byte[] doLe) {

		// Determine length of MAC input buffer
		int macblen = this.blocklen;	// CLA|INS|P1|P2|80|..|00 
		boolean addPadding = false;		// No padding if we only encrypt the header
		
		if (this.macssc != null) {		// Send Sequence Counter
			macblen += this.macssc.length;
		}

		// Add CRT with odd tag to MAC
		if ((this.crt != null) && ((this.crt[0] & 1) == 1)) {
			macblen += this.crt.length;
			addPadding = true;
		}
		
		if (do81or87 != null) {     // Body
			macblen += do81or87.length;
			addPadding = true;
		}
		
		if (doLe != null) {      // Le object
			macblen += doLe.length;
			addPadding = true;
		}

		if (addPadding) {
			macblen = (macblen & ~(this.blocklen - 1)) + this.blocklen;
		}
		
		byte[] macb = new byte[macblen];
		int offset = 0;

		if (this.macssc != null) {
			System.arraycopy(this.macssc, 0, macb, offset, this.macssc.length);
			offset += this.macssc.length;
		}

		macb[offset++] = (byte)(cla | 0x0C);
		macb[offset++] = ins;
		macb[offset++] = p1;
		macb[offset++] = p2;
		macb[offset++] = (byte)0x80;
		for (int i = this.blocklen - 5; i > 0; i--) {
			macb[offset++] = (byte)0x00;
		}

		if ((this.crt != null) && ((this.crt[0] & 1) == 1)) {
			System.arraycopy(this.crt, 0, macb, offset, this.crt.length);
			offset += this.crt.length;
		}
		
		if (do81or87 != null) {
			System.arraycopy(do81or87, 0, macb, offset, do81or87.length);
			offset += do81or87.length;
		}
		
		if (doLe != null) {
			System.arraycopy(doLe, 0, macb, offset, doLe.length);
			offset += doLe.length;
		}
		
		if (addPadding) {
			macb[offset++] = (byte)0x80;
			while(offset < macblen) {
				macb[offset++] = 0x00;
			}
		}
		
		byte[] doMac;
		
		try {
			Mac eng = Mac.getInstance(this.macalgorithm, this.provider);
			eng.init(this.kmac);
			byte[] mac = eng.doFinal(macb);
			
			doMac = new byte[2 + this.maclen];
			doMac[0] = (byte)0x8E;
			doMac[1] = (byte)this.maclen;
			System.arraycopy(mac, 0, doMac, 2, this.maclen);
		}
		catch(Exception e) {
			throw new CardServiceInvalidParameterException(e.getMessage());
		}
		return doMac;
	}
	
	
	
	/**
	 * Wrap command APDU into a secure messaging command APDU using algorithm defined in eSign-K (CWA 14890)
	 * 
	 * @param apduToWrap        Command APDU to be wrapped by secure messaging
	 * @param usageQualifier    Bitmap of SecureChannel.CPRO and SecureChannel.CENC to indicate 
	 *                          if MAC protection and or encryption is required.
	 * @return                  Wrapped APDU
	 *                            
	 * @throws CardServiceInvalidParameterException
	 *                          Thrown if crypto service provider does not support algorithm
	 */
	public CommandAPDU wrap(CommandAPDU apduToWrap, int usageQualifier) {
		
		boolean pro = (usageQualifier & SecureChannel.CPRO) > 0;
		boolean enc = (usageQualifier & SecureChannel.CENC) > 0;

		// Determine values of lc, body and le
		int le = -1;
		byte[] body = null;
		byte cla = (byte)(apduToWrap.getByte(0) | 0x08);    // Indicate secure messaging
		byte ins = (byte)apduToWrap.getByte(1);
		byte p1 = (byte)apduToWrap.getByte(2);
		byte p2 = (byte)apduToWrap.getByte(3);

		boolean oddINS = (ins & 1) == 1;

		boolean extended = false;
		int l = apduToWrap.getLength() - 4;		// Length without header
		if (l > 0) {		// Not case 1
			int i = 4;
			int n = apduToWrap.getByte(i++);
			l--;
			
			if ((n == 0) && (l > 0)) { // Extended APDU ?
				extended = true;
				if (l < 2) {
					throw new CardServiceInvalidParameterException("Invalid Le in extended APDU");
				}
				n = (apduToWrap.getByte(i) << 8) + apduToWrap.getByte(i + 1);
				i += 2;
				l -= 2;
			}
			
			if (l > 0) {	// Case 3s / Case 3e / Case 4s / Case 4e
				if (l < n) {
					throw new CardServiceInvalidParameterException("Invalid Lc in APDU");
				}
				body = new byte[n];
				System.arraycopy(apduToWrap.getBuffer(), i, body, 0, n);
				i += n;
				l -= n;
				
				if (l > 0) {	// Case 4s / Case 4e
					le = apduToWrap.getByte(i++);
					l--;
					if (extended) {
						if (l < 1) {
							throw new CardServiceInvalidParameterException("Invalid Le in extended APDU");
						}
						le = (le << 8) + apduToWrap.getByte(i++);
						l--;
					}
				}
			} else {		// Case 2s / Case 2e
				le = n;
			}
			
		
			if (l > 0) {
				throw new CardServiceInvalidParameterException("Unexpected bytes in APDU");
			}
		}

		if (pro && (this.macssc != null)) {     // Send Sequence Counter
			incrementMACSSC();
		}
		
		byte[] do81or87 = null;
		
		if ((body != null) || (crt != null)) {           // We have a body
			if (oddINS) {
				do81or87 = encodeBodyOddINS(body, enc, pro);
			} else {
				do81or87 = encodeBodyEvenINS(body, enc, pro);
			}
		}
		
		byte[] doLe = null;     // Le data object
		byte[] doMac = null;     // MAC data object

		if (le >= 0) {      // Le object
			doLe = new byte[(extended && !singleByteT97) ? 4 : 3];
			doLe[0] = (pro ? (byte)0x97 : (byte)0x96);
			if ((extended && !singleByteT97)) {
				doLe[1] = (byte)0x02;
				doLe[2] = (byte)(le >> 8);
				doLe[3] = (byte)(le & 0xFF);
			} else {
				doLe[1] = (byte)0x01;
				doLe[2] = (byte)(le & 0xFF);
			}
		}

		
		if (pro) {    // We do MAC protection
			cla |= 0x04;    // Indicate "Header is protected
			doMac = calculateMAC(cla, ins, p1, p2, do81or87, doLe);
		}
		
		// Create protected APDU
		// Header, New Lc, 81 / 87 do, LeDo, Mac, New Le
		
		int newlc = 0;
		
		if (this.crt != null) {
			newlc += this.crt.length;
		}
		
		if (do81or87 != null)
			newlc += do81or87.length;
		
		if (doLe != null)
			newlc += doLe.length;
		
		if (doMac != null)
			newlc += doMac.length;

		if (newlc > 255) {
			extended = true;
		}
		
		CommandAPDU apdu = new CommandAPDU(10 + newlc);
		
		apdu.append(cla);
		apdu.append(ins);
		apdu.append(p1);
		apdu.append(p2);
		
		if (newlc > 0) {
			if (extended) {
				apdu.append((byte)0);
				apdu.append((byte)(newlc >> 8));
				apdu.append((byte)(newlc & 0xFF));
			} else {
				apdu.append((byte)newlc);
			}
		}
		
		if (this.crt != null) {
			apdu.append(this.crt);
		}
		
		if (do81or87 != null) {
			apdu.append(do81or87);
		}
		
		if (doLe != null) {
			apdu.append(doLe);
		}
		
		if (doMac != null) {
			apdu.append(doMac);
		}

		// Append new Le
		if ((le != -1) || ((usageQualifier & (SecureChannel.RENC|SecureChannel.RPRO)) > 0)) {
			apdu.append((byte)0x00);
			if (extended) {
				apdu.append((byte)0x00);
			}
		}
		
		return apdu;
	}

	
	
	/**
	 * Unwrap response APDU received with secure messaging
	 * 
	 * @param apduToUnwrap      Response APDU to process
	 * @param usageQualifier    Bitmap of SecureChannel.RPRO and SecureChannel.RENC to indicate 
	 *                          if MAC protection and or encryption is requested.
	 * @return                  Unwrapped APDU
	 * 
	 * @throws CardServiceInvalidParameterException
	 *                          Thrown if crypto service provider does not support algorithm or
	 *                          secure messaging response is invalid
	 * @throws CardServiceInvalidCredentialException
	 *                          Thrown is MAC verification or decryption of message failed 
	 */
	public ResponseAPDU unwrap(ResponseAPDU apduToUnwrap, int usageQualifier) {
		boolean pro = (usageQualifier & SecureChannel.RPRO) > 0;
		byte[] do81or87 = null;
		byte[] doSW = null;
		byte[] doMac = null;

		byte[] buffer = apduToUnwrap.getBuffer();
		int length = apduToUnwrap.getLength();
		int[] toffset = new int[1];
		toffset[0] = 0;
		
		while (length > 2) {
			byte[] tb;
			try {
				TLV t = new TLV(buffer, toffset);
				tb = t.toBinary();
			}
			catch(Exception e) {
				throw new CardServiceInvalidParameterException("Invalid encoding of TLV object in secure messaging response detected");
			}
			
			switch(tb[0]) {
			case (byte)0x80:		// Plain data
			case (byte)0x81:		// Plain data with MAC
			case (byte)0x84:		// Enciphered TLV data
			case (byte)0x85:		// Enciphered TLV data with MAC
			case (byte)0x86:		// Enciphered non-TLV data
			case (byte)0x87:		// Enciphered non-TLV data with MAC
			case (byte)0xB2:		// Plain TLV data
			case (byte)0xB3:		// Plain TLV data with MAC
				if (do81or87 != null) {
					throw new CardServiceInvalidParameterException("Data object " + HexString.hexify(tb[0]) + " detected in secure messaging response that contains data object 80,81,84,85,86,86,B2 or B3 as well");
				}
				do81or87 = tb;
				break;
			case (byte)0x99:
				if (doSW != null) {
					throw new CardServiceInvalidParameterException("Duplicate data object 99 (SW) found in secure messaging response");
				}
				doSW = tb;
				break;
			case (byte)0x8E:
				if (doMac != null) {
					throw new CardServiceInvalidParameterException("Duplicate data object 8E (MAC) found in secure messaging response");
				}
				doMac = tb;
				break;
			default:
				throw new CardServiceInvalidParameterException("Unknown data object found in secure messaging response");
			}
			length -= tb.length;
		}
		
		if (length != 2) {
			throw new CardServiceInvalidParameterException("Length of secure messaging response message invalid");
		}
		
		if (pro) {    // We do MAC protection
			if (doMac == null) {
				if ((apduToUnwrap.sw() == IsoConstants.RC_SMOBJMISSING) || 
					(apduToUnwrap.sw() == IsoConstants.RC_INCSMDATAOBJECT) ||
					(apduToUnwrap.sw() == IsoConstants.RC_SMNOTSUPPORTED)) {
					return apduToUnwrap;
				}
				throw new CardServiceInvalidParameterException("MAC data object missing from secure messaging response");
			}
			
			if (doMac.length != this.maclen + 2) {
				throw new CardServiceInvalidParameterException("MAC data object has wrong length");
			}
			
			int macblen = 0; 
			
			if (this.macssc != null) {     // Send Sequence Counter
				incrementMACSSC();
				macblen += this.macssc.length;
			}

			if (do81or87 != null) {     // Body
				macblen += do81or87.length;
			}
			
			if (doSW != null) {
				macblen += doSW.length;
			}
			
			macblen = (macblen & ~(this.blocklen - 1)) + this.blocklen;
				
			byte[] macb = new byte[macblen];
			int offset = 0;

			if (this.macssc != null) {
				System.arraycopy(this.macssc, 0, macb, offset, this.macssc.length);
				offset += this.macssc.length;
			}

			if (do81or87 != null) {
				System.arraycopy(do81or87, 0, macb, offset, do81or87.length);
				offset += do81or87.length;
			}
			
			if (doSW != null) {
				System.arraycopy(doSW, 0, macb, offset, doSW.length);
				offset += doSW.length;
			}
			
			macb[offset++] = (byte)0x80;
			while(offset < macblen) {
				macb[offset++] = 0x00;
			}
			
			byte[] mac;
			try {
				Mac eng = Mac.getInstance(this.macalgorithm, this.provider);
				eng.init(this.kmac);
				mac = eng.doFinal(macb);
			}
			catch(Exception e) {
				throw new CardServiceInvalidParameterException(e.getMessage());
			}
			
			if (doMac[1] != this.maclen) {
				throw new CardServiceInvalidParameterException("MAC data object has wrong length");
			}
			for (int i = 0; i < this.maclen; i++) {
				if (doMac[i + 2] != mac[i]) {
					throw new CardServiceInvalidCredentialException("MAC verification failed");
				}
			}
		}
		
		byte[] body = null;
		
		if (do81or87 != null) {
			int tag = do81or87[0] & 0xFE;
			if ((usageQualifier & SecureChannel.RENC) > 0) {    // We do response encryption
				if ((tag != 0x84) && (tag != 0x86)) {
					throw new CardServiceInvalidParameterException("Expected enciphered response");
				}
				TLV t = new TLV(do81or87);
				byte[] tv = t.valueAsByteArray();
				

				int ofs = 0;
				if (tag == 0x86) {
					ofs = 1;
					if (tv[0] != 0x01) {
						throw new CardServiceInvalidParameterException("Invalid padding indicator in enciphered block");
					}
				}
				
				if (this.encssc != null) {     // Send Sequence Counter
					incrementENCSSC();
				}

				byte[] cryptoInput = new byte[tv.length - ofs];
				System.arraycopy(tv, ofs, cryptoInput, 0, cryptoInput.length);
				
				try {
					Cipher cipher = Cipher.getInstance(this.cipheralgorithm, this.provider);

					byte[] iiv = getIV(cipher);
					IvParameterSpec dps = new IvParameterSpec(iiv);
					cipher.init(Cipher.DECRYPT_MODE, this.kenc, dps); 
					
					byte[] plainText = cipher.doFinal(cryptoInput);

					int i = plainText.length - 1;
					for (; (i > 0) && (plainText[i] == 0x00); i--);
					if (plainText[i] != (byte)0x80) {
						throw new CardServiceInvalidCredentialException("Invalid padding in enciphered block");
					}
					body = new byte[i];
					System.arraycopy(plainText, 0, body, 0, i);
				}
				catch(Exception e) {
					throw new CardServiceInvalidParameterException(e.getMessage());
				}
				// Special handling for Micardo
				if (!pro && (this.macssc != null)) {
					incrementMACSSC();
				}
			} else {
				if ((tag != 0x80) && (tag != 0xB2)) {
					throw new CardServiceInvalidParameterException("Expected plain response");
				}
				TLV t = new TLV(do81or87);
				body = t.toBinaryContent();
			}
		}
		
		ResponseAPDU apdu = new ResponseAPDU((body == null ? 0 : body.length) + 2);
		if (body != null) {
			apdu.append(body);
		}
		if (doSW != null) {
			apdu.append(doSW[2]);
			apdu.append(doSW[3]);
		} else {
			apdu.append(buffer[apduToUnwrap.getLength() - 2]);
			apdu.append(buffer[apduToUnwrap.getLength() - 1]);
		}
		
		return apdu;
	}
	
	
	
	/**
	 * Set key for encryption / decryption
	 * 
	 * <p>For DESede keys the default MAC algorithm is set to DESede/CBC/NoPadding.</p>
	 * <p>For AES keys the default MAC algorithm is set to AES/CBC/NoPadding.</p>
	 *
	 * <p>All ISO padding is performed by the code itself.</p>
	 * 
	 * @param key the key used for encipherment or decipherment
	 */
	public void setEncKey(Key key) {
		this.kenc = key;
		String keyalgo = key.getAlgorithm();
		
		this.cipheralgorithm = keyalgo + "/CBC/NoPadding";
	}


	
	/**
	 * Set key from MAC calculation / verification
	 * 
	 * <p>For DESede keys the default MAC algorithm is set to ISO9797ALG3Mac (Retail-MAC).</p>
	 * <p>For AES keys the default MAC algorithm is set to AES CMAC.</p>
	 * 
	 * <p>All ISO padding is performed by the code itself.</p>
	 * 
	 * @param key the key used for mac calculation
	 */
	public void setMacKey(Key key) {
		this.kmac = key;
		String keyalgo = key.getAlgorithm();
		
		if (keyalgo.equals("DESede")) {
			this.macalgorithm = "ISO9797ALG3Mac";
			this.blocklen = 8;
		} else if (keyalgo.equals("AES")) {
			this.macalgorithm = "AESCMAC";
			this.blocklen = 16;
		} else {
			throw new CardServiceInvalidParameterException("Unsupported key type " + keyalgo + ". Only DESede or AES allowed.");
		}
		this.iv = new byte[this.blocklen];
	}



	/**
	 * Sets the JCE algorithm name used for mac operations.
	 * 
	 * <p>Setting this parameter overwrites the default setting from setMacKey()</p>
	 * 
	 * @param algo the JCE algorithm name
	 */
	public void setMacAlgorithm(String algo) {
		this.macalgorithm = algo;
	}

	
	
	/**
	 * Sets the JCE algorithm name used for mac operations.
	 * 
	 * <p>Setting this parameter overwrites the default setting from setMacKey()</p>
	 * 
	 * @param algo the JCE algorithm name
	 */
	public void setCipherAlgorithm(String algo) {
		this.cipheralgorithm = algo;
	}

	
	
	/**
	 * Set initialisation vector for CBC
	 * 
	 * @param iv
	 */
	public void setIV(byte[] iv) {
		this.iv = iv;
	}
	
	
	
	/**
	 * Set length of mac as number of rightmost bytes
	 * 
	 * @param maclen
	 */
	public void setMacLength(int maclen) {
		this.maclen = maclen;
	}
	
	
	
	/**
	 * Initialise send sequence counter
	 * 
	 * @param ssc
	 * @deprecated Use setMACSendSequenceCounter instead.
	 */
	public void setSendSequenceCounter(byte[] ssc) {
		this.macssc = ssc;
	}
	
	
	
	/**
	 * Initialise send sequence counter
	 * 
	 * @param ssc
	 */
	public void setEncryptionSendSequenceCounter(byte[] ssc) {
		this.encssc = ssc;
	}
	
	
	
	/**
	 * Initialise send sequence counter
	 * 
	 * @param ssc
	 */
	public void setMACSendSequenceCounter(byte[] ssc) {
		this.macssc = ssc;
	}
	
	
	
	/**
	 * Return current value of send sequence counter
	 *
	 * @return Byte array containing send sequence counter or null if none defined
	 * @deprecated Use getMACSendSequenceCounter instead
	 */
	public byte[] getSendSequenceCounter() {
		return this.macssc;
	}

	
	
	/**
	 * Return current value of send sequence counter for encryption
	 *
	 * @return Byte array containing send sequence counter or null if none defined
	 */
	public byte[] getEncryptionSendSequenceCounter() {
		return this.encssc;
	}

	
	
	/**
	 * Return current value of send sequence counter for message authentication code
	 *
	 * @return Byte array containing send sequence counter or null if none defined
	 */
	public byte[] getMACSendSequenceCounter() {
		return this.macssc;
	}
	

	
	/**
	 * Set policy for handling send sequence counters.
	 * <p>Set to SSCPolicyEnum.DEFAULT to use SSC for encryption and SSC for MAC individually, if defined.</p>
	 * <p>Set to SSCPolicyEnum.SYNC to use SSC for MAC for encryption as well.</p>
	 * <p>Set to SSCPolicyEnum.SYNC_AND_ENCRYPT to use SSC for MAC for encrypted SSC for encryption.</p>
	 * 
	 * @param policy the policy to use
	 */
	public void setSendSequenceCounterPolicy(SSCPolicyEnum policy) {
		this.sscpolicy = policy;
	}
	

	
	/**
	 * Set the cryptographic reference template to be included in the command.
	 * 
	 * @param crt the crt to be included. Odd tags are included in the MAC.
	 */
	public void setCRT(byte[] crt) {
		this.crt = crt;
	}
}
