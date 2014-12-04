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
package de.cardcontact.opencard.terminal.android.cgcard;


import com.certgate.android.SmartCardJni;
import com.certgate.android.SmartCardVersion;

import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.HexString;
import opencard.core.util.Tracer;

/**
 * Class implementing a card terminal for Certgate micro SD card
 * 
 * @author Frank Thater
 */
public class CGMicroSDCardTerminal extends CardTerminal {

	private final static Tracer ctracer = new Tracer(CGMicroSDCardTerminal.class);

	private final static byte[] DUMMY_ATR = { 0x3B, (byte) 0xF8, 0x13, 0x00,
			0x00, (byte) 0x81, 0x31, (byte) 0xFE, 0x45, 0x4A, 0x43, 0x4F, 0x50,
			0x76, 0x32, 0x34, 0x31, (byte) 0xB7 };

	private boolean connected = false;

	
	
	public CGMicroSDCardTerminal(String name, String type, String address)
			throws CardTerminalException {
		super(name, type, address);

		SmartCardVersion version = SmartCardJni.getLibraryVersion();

		ctracer.debug("CGCardCardTerminal", "Creating terminal using library version " + version.Release + " " + version.Text);

		addSlots(1);
	}

	
	
	@Override
	public CardID getCardID(int slotID) throws CardTerminalException {
		// TODO: Adjust ATR dummy value
		return new CardID(DUMMY_ATR);
	}

	
	
	@Override
	public boolean isCardPresent(int slotID) throws CardTerminalException {
		// Log.d("CGCardCardTerminal", "Terminal JNI enumerateCards()");
		// boolean cardPresent = SmartCardJni.enumerateCards().length > 0; //
		// Resets the card - so do not use it
		ctracer.debug("CGCardCardTerminal", "Terminal isCardPresent()");
		return true;
	}

	
	
	@Override
	public void open() throws CardTerminalException {
		ctracer.debug("CGCardCardTerminal", "open()");

		int rc = SmartCardJni.open();

		if (rc != 0) {
			ctracer.debug("CGCardCardTerminal", "Terminal open() failed " + rc);
			throw new CardTerminalException("Terminal open() failed " + rc);
		}

		connected = true;
	}

	
	
	@Override
	public void close() throws CardTerminalException {
		ctracer.debug("CGCardCardTerminal", "close()");

		int rc = SmartCardJni.close();

		if (rc != 0) {
			ctracer.debug("CGCardCardTerminal", "Terminal close() failed " + rc);
			connected = false;
			throw new CardTerminalException("Terminal close() failed " + rc);
		}

		connected = false;
	}

	
	
	@Override
	protected CardID internalReset(int slot, int ms) throws CardTerminalException {
		
		ctracer.debug("CGCardCardTerminal", "reset()");
		/* By now we only return a ATR dummy value as the real card ATR is not available using the JNI */
		return new CardID(DUMMY_ATR);
	}

	
	
	@Override
	protected ResponseAPDU internalSendAPDU(int slot, CommandAPDU capdu, int ms)
			throws CardTerminalException {
		if (!connected) {
			ctracer.debug("CGCardCardTerminal", "Terminal internalSendAPDU() - not connected");
		}

		ctracer.debug("CGCardCardTerminal", "sendAPDU()");
		ctracer.debug("CGCardCardTerminal",	"C-APDU: " + HexString.hexify(capdu.getBytes()));
		
		byte[] rsp = internalTransmit(capdu.getBytes());
		ctracer.debug("CGCardCardTerminal", "R-APDU: " + HexString.hexify(rsp));
		
		return new ResponseAPDU(rsp);
	}

	
	
	private byte[] internalTransmit(byte[] paramArrayOfByte) throws CardTerminalException {
		byte[] arrayOfByte = new byte[512];
		int i = SmartCardJni.apdu(paramArrayOfByte, arrayOfByte);
		if (i < 2) {
			ctracer.debug("CGCardCardTerminal", "Error transmitting APDU using JNI, rc = " + i);
			throw new IllegalStateException("internal error");
		}
		return shrinkByteArray(arrayOfByte, i);
	}

	
	
	private static byte[] shrinkByteArray(byte[] paramArrayOfByte, int paramInt) {
		byte[] arrayOfByte = new byte[paramInt];
		System.arraycopy(paramArrayOfByte, 0, arrayOfByte, 0, paramInt);
		return arrayOfByte;
	}
}
