/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2011 CardContact Software & System Consulting
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

package de.cardcontact.opencard.terminal.smartcardio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.activation.CommandObject;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;

import opencard.core.terminal.CHVEncoder;
import opencard.core.util.HexString;

/**
 * @class This class implements PC/SC features
 */
public class PCSCIOControl {

	private static final byte FEATURE_VERIFY_PIN_DIRECT = 0x06;
	private static final byte FEATURE_MODIFY_PIN_DIRECT = 0x07;
	private static final byte FEATURE_EXECUTE_PACE = 0x20;
	
	
	private Card card;
	
	
	
	/** Timeout in seconds */
	private byte timeOut = 0x30;
	
	
	
	/** Timeout in seconds after first key stroke */
	private byte timeOut2 = 0x30;
	
	
	
	/** 
	 * The formatting options <br>
	 * USB CCID PIN FORMAT xxx
	 */
	private byte formatString = (byte) 0x89;
	
	
	
	/**
	 * PIN block string <br>
	 * 	bits 7-4 bit size of PIN length in APDU<br>
	 * 	bits 3-0 PIN block size in bytes after justification and formatting<br>
	 */
	private byte pinBlockString = 0x47;
	
	
	
	/**
	 * bits 7-5 RFU,<br>
	 * bit 4 set if system units are bytes
	 * clear if system units are bits,<br>
	 * bits 3-0 PIN length position in system units
	 */
	private byte pinLengthFormat = 0x04;
	
	
	
	/** Max PIN size */
	private byte maxPINSize = (byte) 0x06;
	
	
	
	/** Min PIN size */
	private byte minPINSize = (byte) 0x04;
	
	
	
	/** 
	 * Indicates if a confirmation is requested before acceptance of a new PIN<br>
	 * 	Bit 0 = 0: No confirmation requested<br>
	 * 	Bit 0 = 1: Confirmation requested<br>
	 *	Bit 1 = 0: No current PIN entry requested<br>
	 *	Bit 1 = 1: Current PIN entry requested  <br>
	 *	Bit 2-7 RFU
	 */  
	private byte confirmPIN = 0x03;
	
	
	
	/** 
	 * Conditions under which PIN entry should be considered complete<br>
	 *	0x01 = max size reached<br>
	 *	0x02 = validation key pressed<br>
	 *	0x04 = timeout occurred
	 */
	private byte entryValidationCondition = 0x02;
	
	
	
	/** 
	 * Number of messages to display for PIN verification<br>
	 *	0x00 = no message <br>
	 * 	0x01 = Message which index is indicated in MsgIndex1 (confirmPIN = 0x00)<br>
	 * 	0x02 = Messages which index are indicated in MsgIndex1 and MsgIndex2 (confirmPIN = 0x01 or 0x02)<br>
	 * 	0x03 = Messages which index are indicated in MsgIndex1, MsgIndex2 and MsgIndex3 (confirmPIN = 0x03<br>
	 *  0xFF = Default CCID message 
	 */
	private byte numberMessage = 0x01;
	
	
	
	/** First byte of the two byte language code for messages */
	private byte langId = 0x13;
	
	
	
	/** Last byte of the two byte language code for messages */
	private byte langId2 = 0x08;
	
	
	
	/** Index of first prompting message */
	private byte msgIndex = 0x00;
	
	
	
	/** Index of second prompting message */
	private byte msgIndex2 = 0x01;
	
	
	
	/** Index of third prompting message */
	private byte msgIndex3 = 0x02;
	
	
	
	/** 
	 * T=1 I-block prologue field to use (fill with 00).<br>
	 * First of three bytes
	 */
	private byte teoPrologue = 0x00;

	
	
	/** 
	 * T=1 I-block prologue field to use (fill with 00).<br>
	 * Second of three bytes
	 */
	private byte teoPrologue2 = 0x00;
	
	
	
	/** 
	 * T=1 I-block prologue field to use (fill with 00).<br>
	 * Third of three bytes
	 */	
	private byte teoPrologue3 = 0x00;
	
	
	
	/** Insertion position offset in bytes for the current PIN */
	private byte insertionOffsetOld = 0x00;
	
	
	
	/** Insertion position offset in bytes for the new PIN */
	private byte insertionOffsetNew = 0x8;
	
	
	/**
	 * Set the formatString depending on the PIN encoding.
	 * @param encoding one of "Format 2 Block", "Packed BCD", "String encoding"
	 * @throws CardException 
	 */
	public void setPinEncoding(String encoding) throws CardException {
		/** PIN position starts 1 byte after Lc*/		
		byte pinPos = 0x8;
		byte bcd = 0x1;
		byte ascii = 0x2;
		
		byte format = (byte) 0x80;
		// See USB SmartCard CCID for bm encoding
		if (encoding.equals(CHVEncoder.F2B_ENCODING)) {
			format = (byte) (format | pinPos | bcd);
		} else if (encoding.equals(CHVEncoder.BCD_ENCODING)) {
			format = (byte) (format | bcd);
		} else if (encoding.equals(CHVEncoder.STRING_ENCODING)) {
			format = (byte) (format | ascii);
		} else {
			throw new CardException("Encoding: " + encoding + " is not supported.");
		}
		setFormatString(format);
	}
	
	
	/** Set timeout in seconds */
	public void setTimeOut(byte timeOut) {
		this.timeOut = timeOut;
	}

	
	
	/**Set timeout in seconds after first key stroke */
	public void setTimeOut2(byte timeOut2) {
		this.timeOut2 = timeOut2;
	}

	
	
	/** 
	 * Set formatting options<br>
	 * USB CCID PIN FORMAT xxx
	 */
	public void setFormatString(byte formatString) {
		this.formatString = formatString;
	}

	
	
	/**
	 * Set Pin block string <br>
	 * 	bits 7-4 bit size of PIN length in APDU<br>
	 * 	bits 3-0 PIN block size in bytes after justification and formatting
	 */
	public void setPinBlockString(byte pinBlockString) {
		this.pinBlockString = pinBlockString;
	}

	
	
	/**
	 * Set Pin length format<br>
	 * 	bits 7-5 RFU,<br>
	 * 	bit 4 set if system units are bytes
	 * 	clear if system units are bits,<br>
	 * 	bits 3-0 PIN length position in system units
	 */
	public void setPinLengthFormat(byte pinLengthFormat) {
		this.pinLengthFormat = pinLengthFormat;
	}

	
	/** Set max PIN size */
	public void setMaxPINSize(byte maxPINSize) {
		this.maxPINSize = maxPINSize;
	}

	
	
	/** Set min PIN size */
	public void setMinPINSize(byte minPINSize) {
		this.minPINSize = minPINSize;
	}

	
	
	/** 
	 * Set confirmation options for acceptance of a new PIN<br>
	 * 	Bit 0 = 0: No confirmation requested<br>
	 * 	Bit 0 = 1: Confirmation requested<br>
	 *	Bit 1 = 0: No current PIN entry requested<br>
	 *	Bit 1 = 1: Current PIN entry requested  <br>
	 *	Bit 2-7 RFU
	 */  
	public void setConfirmPIN(byte confirmPIN) {
		this.confirmPIN = confirmPIN;
	}
	
	
	
	/** 
	 * Set conditions under which PIN entry should be considered complete<br>
	 *	0x01 = max size reached<br>
	 *	0x02 = validation key pressed<br>
	 *	0x04 = timeout occurred
	 */
	public void setEntryValidationCondition(byte entryValidationCondition) {
		this.entryValidationCondition = entryValidationCondition;
	}

	
	
	/** 
	 * Set number of messages to display for PIN verification/modification<br>
	 * 	0x00 = no message <br>
	 * 	0x01 = Message which index is indicated in MsgIndex1 (confirmPIN = 0x00)<br>
	 * 	0x02 = Messages which index are indicated in MsgIndex1 and MsgIndex2 (confirmPIN = 0x01 or 0x02)<br>
	 * 	0x03 = Messages which index are indicated in MsgIndex1, MsgIndex2 and MsgIndex3 (confirmPIN = 0x03<br>
	 *  0xFF = Default CCID message
	 */  
	public void setNumberMessage(byte numberMessage) {
		this.numberMessage = numberMessage;
	}

	
	
	/** Set first byte of the two byte language code for messages */
	public void setLangId(byte langId) {
		this.langId = langId;
	}

	
	
	/** Last byte of the two byte language code for messages */
	public void setLangId2(byte langId2) {
		this.langId2 = langId2;
	}

	
	
	/** Set index of first prompting message */
	public void setMsgIndex(byte msgIndex) {
		this.msgIndex = msgIndex;
	}

	
	
	/** Set index of second prompting message */
	public void setMsgIndex2(byte msgIndex2) {
		this.msgIndex2 = msgIndex2;
	}

	
	
	/** Set index of third prompting message */
	public void setMsgIndex3(byte msgIndex3) {
		this.msgIndex3 = msgIndex3;
	}

	
	
	/** 
	 * Set teoPrologue<br>
	 * 	T=1 I-block prologue field to use (fill with 00).<br>
	 * 	First of three bytes
	 */
	public void setTeoPrologue(byte teoPrologue) {
		this.teoPrologue = teoPrologue;
	}
	
	
	
	/** 
	 * Set teoPrologue<br>
	 * 	T=1 I-block prologue field to use (fill with 00).<br>
	 * 	Second of three bytes
	 */
	public void setTeoPrologue2(byte teoPrologue2) {
		this.teoPrologue2 = teoPrologue2;
	}

	
	
	/** 
	 * Set teoPrologue<br>
	 *	 T=1 I-block prologue field to use (fill with 00).<br>
	 *	 Third of three bytes
	 */
	public void setTeoPrologue3(byte teoPrologue3) {
		this.teoPrologue3 = teoPrologue3;
	}

	
	
	/** Set insertion position offset in bytes for the current PIN */
	public void setInsertionOffsetOld(byte insertionOffsetOld) {
		this.insertionOffsetOld = insertionOffsetOld;
	}

	
	
	/** Set insertion position offset in bytes for the new PIN */
	public void setInsertionOffsetNew(byte insertionOffsetNew) {
		this.insertionOffsetNew = insertionOffsetNew;
	}	

	/**
	 * @constructor
	 * @param card the card object
	 */
	public PCSCIOControl(Card card) {
		this.card = card;
	}
	
	
	
	/**
	 * True if card terminal supports FEATURE_VERIFY_PIN_DIRECT
	 */
	public boolean hasVerifyPinDirect() {
		return getFeatureControlCode(FEATURE_VERIFY_PIN_DIRECT) != -1;
	}

	
	
	/**
	 * True if card terminal supports FEATURE_MODIFY_PIN_DIRECT
	 */
	public boolean hasModifyPinDirect() {
		return getFeatureControlCode(FEATURE_MODIFY_PIN_DIRECT) != -1;
	}	
	
	
	
	/**
	 * True if card terminal supports FEATURE_EXECUTE_PACE
	 */
	public boolean hasExecutePace() {
		return getFeatureControlCode(FEATURE_EXECUTE_PACE) != -1;
	}
	
	
	
	/**
	 * Verifying PIN direct with a class 3 card terminal
	 *  
	 * @param xcapdu			The CommandAPDU
	 * @return byte array
	 * @throws CardException
	 */
	public byte[] verifyPINDirect(CommandAPDU xcapdu) throws CardException {
		byte[] commandData = new byte[] {
				this.timeOut, 
				this.timeOut2, 
				this.formatString,
				this.pinBlockString, 
				this.pinLengthFormat,
				this.maxPINSize,
				this.minPINSize, 
				this.entryValidationCondition, 
				this.numberMessage,
				this.langId, 
				this.langId2, 
				this.msgIndex, 
				this.teoPrologue,
				this.teoPrologue2, 
				this.teoPrologue3, 
		};

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	
		byte[] commandBlock = null;
		
		int pcc = getFeatureControlCode(FEATURE_VERIFY_PIN_DIRECT);
		
		try {
			
						
			bos.write(commandData);
			bos.write((byte)xcapdu.getBytes().length); //bos.write((byte)capdu.getLength());
			bos.write(0x00);
			bos.write(0x00);
			bos.write(0x00);
			bos.write(xcapdu.getBytes());
			
		} catch (IOException e) {
			throw new CardException("Error creating terminal command: " + e.getMessage());
		}
		
		commandBlock = bos.toByteArray();
		byte[] rsp = this.card.transmitControlCommand(pcc, commandBlock);
		return rsp;
	}
	
	
	
	/**
	 * Modifying PIN direct with a class 3 card terminal
	 * 
	 * @param xcapdu			The CommandAPDU
	 * @return byte array
	 * @throws CardException
	 */
	public byte[] modifyPINDirect(CommandAPDU xcapdu) throws CardException {
		byte[] commandData = new byte[] {
			this.timeOut, 
			this.timeOut2, 
			this.formatString, 
			this.pinBlockString, 
			this.pinLengthFormat, 
			this.insertionOffsetOld, 
			this.insertionOffsetNew,  
			this.maxPINSize, 
			this.minPINSize, 
			this.confirmPIN, 
			this.entryValidationCondition, 
			0x03, //bNumberMessage
			this.langId, 
			this.langId2, 
			this.msgIndex, 
			this.msgIndex2, 
			this.msgIndex3, 
			this.teoPrologue, 
			this.teoPrologue2,
			this.teoPrologue3,
		};
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] commandBlock = null;
		int pcc = getFeatureControlCode(FEATURE_MODIFY_PIN_DIRECT);
		
		try {
			bos.write(commandData);
			bos.write((byte)xcapdu.getBytes().length);
			bos.write(0x00);
			bos.write(0x00);
			bos.write(0x00);
			bos.write(xcapdu.getBytes());
		} catch (IOException e) {
			throw new CardException("Error creating terminal command: " + e.getMessage());
		}
		
		commandBlock = bos.toByteArray();
		byte[] rsp = this.card.transmitControlCommand(pcc, commandBlock);
		return rsp;
	}
	
	
	
	/**
	 * Determine the IOCTL for the requested feature
	 * @param feature One of PCSCIOControl.FEATURE_*
	 * @return The feature control code or -1 if feature was not found
	 */
	public int getFeatureControlCode(int feature) {
		byte[] features;
	
		int ioctl = (0x31 << 16 | (3400) << 2);
		
		byte[] empty = {};
		
		try {
			features = this.card.transmitControlCommand(ioctl, empty);
		}
		catch(CardException e) {
			return -1;
		}
		
		int i = 0;
		
		while(i < features.length) {
			if (features[i] == feature) {
				int c = 0;
				i += 2;				
				for (int l = 0; (i < features.length) && (l < 4); i++, l++) {
					c <<= 8;
					c |= features[i] & 0xFF;
				}
				
				return c;
				
			} else {
				i += 6; // skip six bytes
			}
		}
		
		return -1;
	}	
	
	
	
	/**
	 * Get the readers PACE capabilities<br>
	 * 	0x40: Terminal supports PACE<br>
	 *	0x20: Terminal supports EPA:eID<br>
	 *	0x60: Supporting PACE and EPA:eID
	 * @return The PACE capabilities or -1
	 * @throws CardException
	 */
	public long getReadersPACECapabilities() throws CardException {
		int PACEControlCode = getFeatureControlCode(FEATURE_EXECUTE_PACE);
		
		//SCardControl
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(0x01); //Index of the PACE function
		bos.write(0x00); //Two byte length field
		bos.write(0x00);
		byte[] commandBlock = bos.toByteArray();
			
		byte[] rpc = this.card.transmitControlCommand(PACEControlCode, commandBlock);
		
		long ergebniscode = de.cardcontact.opencard.utils.Util.extractLongFromByteArray(rpc, 0, 4);
		if(ergebniscode == 0) {
			long outputData = de.cardcontact.opencard.utils.Util.extractLongFromByteArray(rpc, 6, 1);
			return outputData;

		}
		else{
			return -1;
		}	
	}
	
	
	
	/**
	 * 
	 * @param pinid				The PinID must be one of these: <li>0x01 MRZ,</li>
	 * 															<li>0x02 CAN,</li>
	 * 															<li>0x03 PIN,</li>
	 * 															<li>0x04 PUK</li>
	 * @param chat				Certificate Holder Authorization Template
	 * @param pin				PIN
	 * @param certdesc			Certificate Description
	 * @return byte[] 			Output Data
	 * @throws CardException
	 */
	public byte[] establishPACEChannel(int pinid, byte[] chat, byte[] pin, byte[] certdesc) throws CardException {
		//SCardControl
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(0x02); //Index of the PACE function
		bos.write(0x00); //Length template
		bos.write(0x00); //Length template
		
		//EstablishPACEChannelInputData
		bos.write(pinid); 
		if(chat != null) {
			bos.write(chat.length);
			bos.write(chat, 0, chat.length);
		} else {
			bos.write(0);
		}
		if(pin != null) {
			bos.write(pin.length);
			bos.write(pin, 0, pin.length);
		} else {
			bos.write(0);
		}
		if(certdesc != null) {
			bos.write((byte) (certdesc.length & 0xFF));
			bos.write((byte) (certdesc.length >> 8));
			bos.write(certdesc, 0, certdesc.length);
		} else {
			bos.write(0);
			bos.write(0);
		}
		
		int inputLength = bos.size() - 3;
		
		byte[] inputData = bos.toByteArray();
		//Writing the length of the EstablishPACEChannelInputData into the template
		inputData[1] = (byte) (inputLength & 0xFF);
		inputData[2] = (byte) (inputLength >> 8);
		
		
		int PACEControlCode = getFeatureControlCode(FEATURE_EXECUTE_PACE);
		System.out.println(HexString.dump(inputData));
		byte[] rpc = this.card.transmitControlCommand(PACEControlCode, inputData);
		
		long ergebniscode =  de.cardcontact.opencard.utils.Util.extractLongFromByteArray(rpc, 0, 4);
		System.out.println(Long.toHexString(ergebniscode));
		
		return rpc;
	}
}

