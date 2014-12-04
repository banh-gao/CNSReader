/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2008 CardContact Software & System Consulting
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

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

import opencard.core.terminal.CHVControl;
import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ExtendedVerifiedAPDUInterface;
import opencard.core.terminal.Pollable;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.SlotChannel;
import opencard.core.util.Tracer;
import opencard.opt.terminal.TerminalCommand;

/**
 * Implements a wrapper card terminal for access to smart card with the javax.smartcardio interface.
 */
public class SmartCardIOTerminal extends CardTerminal implements TerminalCommand, Pollable, ExtendedVerifiedAPDUInterface {

	private final static Tracer ctracer = new Tracer(SmartCardIOTerminal.class);

	private boolean polling;
	private javax.smartcardio.CardTerminal ct;
	private javax.smartcardio.Card card = null;

	/** The state of this card terminal. */
	private boolean closed;

	/** Is a card inserted currently? */
	private boolean cardInserted;



	public SmartCardIOTerminal(String name, String type, String address, javax.smartcardio.CardTerminal ct) throws CardTerminalException {

		super(name, type, address);

		polling = !type.endsWith("-NOPOLL");	// Disable polling if type is "*-NOPOLL"

		this.ct = ct;
		this.card = null;
		addSlots(1);
	}



	@Override
	public void open() throws CardTerminalException {

		if (polling) {
			CardTerminalRegistry.getRegistry().addPollable((Pollable)this);
		}
		closed = false;
		cardInserted = isCardPresent(0);
	}



	@Override
	public void close() throws CardTerminalException {

		disconnect(true);
		if (polling) {
			CardTerminalRegistry.getRegistry().removePollable((Pollable)this);
		}
		closed = true;
	}



	@Override
	public CardID getCardID(int slotID) throws CardTerminalException {

		if (!isCardPresent(slotID)) {
			ctracer.debug("getCardID", "no card in reader");
			return null;
		}
		connect();

		CardID cardid = new CardID(this, slotID, this.card.getATR().getBytes());
		ctracer.debug("getCardID", "CardID: " + cardid);

		return cardid;
	}



	@Override
	protected CardID internalReset(int slot, int ms) throws CardTerminalException {

		disconnect(true);
		return getCardID(slot);
	}



	@Override
	protected CardID internalReset(int slot, boolean warm) throws CardTerminalException {

		disconnect(true);
		return getCardID(slot);
	}



	@Override
	protected ResponseAPDU internalSendAPDU(int slot, CommandAPDU capdu, int ms) throws CardTerminalException {

		connect();
		javax.smartcardio.CommandAPDU xcapdu = new javax.smartcardio.CommandAPDU(capdu.getBytes());
		CardChannel ch = this.card.getBasicChannel();
		javax.smartcardio.ResponseAPDU xrapdu;

		try	{
			xrapdu = ch.transmit(xcapdu);
		}
		catch(CardException ce) {
			ctracer.error("internalSendAPDU", ce);
			this.card = null;
			throw new CardTerminalException("CardException in transmit(): " + ce.getMessage());
		}

		return new ResponseAPDU(xrapdu.getBytes());
	}



	@Override
	public boolean isCardPresent(int slotID) throws CardTerminalException {
		boolean cardPresent;

		try	{
			cardPresent = ct.isCardPresent();
		}
		catch(CardException ce) {
			ctracer.error("isCardPresent", ce);
			throw new CardTerminalException("CardException in isCardPresent(): " + ce.getMessage());
		}

		if (!cardPresent) {
			card = null;
		}
		return cardPresent;
	}



	/**
	 * Send control command to terminal.
	 * 
	 * The first four byte encode the PC/SC Control Code.
	 * 
	 * @param cmd the command data
	 * @return the response data
	 */
	@Override
	public byte[] sendTerminalCommand(byte[] cmd) throws CardTerminalException {
		int c = 0;
		int i = 0;

		for (; (i < cmd.length) && (i < 4); i++) {
			c <<= 8;
			c |= cmd[i] & 0xFF;
		}

		byte[] cmddata = new byte[cmd.length - i];
		System.arraycopy(cmd, i, cmddata, 0, cmd.length - i);

		byte[] resdata = null;

		try	{
			resdata = this.card.transmitControlCommand(c, cmddata);
		}
		catch(CardException ce) {
			ctracer.error("sendTerminalCommand", ce);
			throw new CardTerminalException("CardException in sendTerminalCommand(): " + ce.getMessage());
		}
		return resdata;
	}



	@Override
	public void poll() throws CardTerminalException {

		if (!closed) {
			try {
				boolean newStatus = isCardPresent(0);
				if (cardInserted != newStatus) {
					ctracer.debug("poll", "status change");
					cardInserted = !cardInserted;
					// ... notify listeners
					if (cardInserted) {
						cardInserted(0);
					} else {
						cardRemoved(0);
					}
				}
			}
			catch (CardTerminalException cte) {
				ctracer.debug("poll", cte);

				// make sure the CardTerminalException is 
				// propagated to listeners waiting for a card
				cardInserted(0);
			}
		}
	}



	/**
	 * Connect to card, first with T=1 then with any protocol
	 * 
	 */
	private void connect() throws CardTerminalException {

		if (this.card != null) {
			return;
		}
		try	{
			this.card = ct.connect("T=1");
		}
		catch(CardException ce) {
			ctracer.debug("second connect due to", ce);
			try	{
				this.card = ct.connect("*");
			}
			catch(CardException nce) {
				ctracer.error("final connect failed", nce);
				throw new CardTerminalException("Error connecting to card: " + nce.getMessage());
			}
		}
		ctracer.debug("connect", this.card.getProtocol());
	}



	/**
	 * Disconnect from card
	 * 
	 * @param reset reset card if set to true
	 * @throws CardTerminalException
	 */
	private void disconnect(boolean reset) throws CardTerminalException {

		if (this.card != null) {
			try	{
				// Inverse logic bug: false means reset, true means leave card
				this.card.disconnect(!reset);
				// Disconnect and immediate reconnect often fails. Wait to give PCSC subsystem a chance to handle disconnect
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
				}
			}
			catch(CardException ce) {
				ctracer.error("disconnect", ce);
				throw new CardTerminalException("Error disconnecting from card: " + ce.getMessage());
			}
			finally {
				this.card = null;
			}
		}
	}



	/**
	 * Send a modify PIN command to the card. A class 3 card terminal is requested for PIN modification.
	 * 
	 * @param capdu	the command APDU
	 * @return The response APDU or null if no class 3 card terminal was found.
	 * @throws CardTerminalException
	 */
	public ResponseAPDU sendModifyPINCommandAPDU(CommandAPDU capdu) throws CardTerminalException {

		PCSCIOControl pcscio = new PCSCIOControl(this.card);
		javax.smartcardio.CommandAPDU xcapdu = new javax.smartcardio.CommandAPDU(capdu.getBytes());

		if (pcscio.hasModifyPinDirect()) {
			ctracer.debug("sendVerifiedCommandAPDU", "Class 3 card terminal found.");	

			try {
				byte[] rsp = pcscio.modifyPINDirect(xcapdu);
				return new ResponseAPDU(rsp);
			} catch (CardException e) {
				throw new CardTerminalException("Error modifying PIN with class 3 reader: " + e.getMessage());
			}
		}
		else{
			return null;
		}
	}



	/**
	 * Send a verified command APDU to the card. The verification will be performed with a class 3 card reader.
	 * The PIN has to be entered on the PIN pad.
	 * 
	 * @param chann		the SlotChannel
	 * @param capdu		the CommandAPDU
	 * @param vc		the CHVControl
	 */
	@Override
	public ResponseAPDU sendVerifiedCommandAPDU(SlotChannel chann, CommandAPDU capdu, CHVControl vc) throws CardTerminalException {
		byte[] rsp;

		ctracer.debug("sendVerifiedCommandAPDU", "PIN entry on card terminal");

		PCSCIOControl pcscio = new PCSCIOControl(this.card);

		javax.smartcardio.CommandAPDU xcapdu = new javax.smartcardio.CommandAPDU(capdu.getBytes());

		try {
			pcscio.setPinEncoding(vc.passwordEncoding());
			byte pinSize = (byte)vc.ioControl().maxInputChars();
			pcscio.setMaxPINSize(pinSize);
			pcscio.setMinPINSize((byte) 1);

			byte timeOut = (byte)vc.ioControl().timeout();
			pcscio.setTimeOut(timeOut);
			pcscio.setTimeOut2(timeOut);
			pcscio.setPinBlockString(pinSize);

			rsp = pcscio.verifyPINDirect(xcapdu);

		} catch (CardException e) {
			throw new CardTerminalException("Error verifying PIN with class 3 reader: " + e.getMessage());
		} 

		return new ResponseAPDU(rsp);
	}



	@Override
	public boolean hasSendVerifiedCommandAPDU() {
		PCSCIOControl pcscio = new PCSCIOControl(this.card);
		return pcscio.hasVerifyPinDirect();
	}
}
