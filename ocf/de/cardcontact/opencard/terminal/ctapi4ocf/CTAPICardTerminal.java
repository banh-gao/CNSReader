/*
 *  ---------
 * |.**> <**.|  CardContact
 * |*       *|  Software & System Consulting
 * |*       *|  Minden, Germany
 * |�**> <**�|  Copyright (c) 2000. All rights reserved
 *  --------- 
 *
 * See file LICENSE for details on licensing
 *
 * Abstract :       Implementation of a CTAPI Card Terminal for Opencard Framework
 *
 * Author :         Frank Thater (FTH)
 *
 * Last modified:   08/04/2000
 *
 *****************************************************************************/

package de.cardcontact.opencard.terminal.ctapi4ocf;

import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.Pollable;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.HexString;
import opencard.core.util.Tracer;
import opencard.opt.terminal.TerminalCommand;
import de.cardcontact.jni2ctapi.cardterminal_api;
import de.cardcontact.opencard.terminal.jcopsim.JCOPSimCardTerminal;

/**
 * Implements a CT-API card terminal for OCF.
 * 
 */
public class CTAPICardTerminal extends CardTerminal implements Pollable,
		TerminalCommand {

	private final static Tracer ctracer = new Tracer(CTAPICardTerminal.class);
	
	private final static byte NOCARD = 0x00;
	private final static byte CARDIN = 0x01;
	private final static byte CARDDISCONNECTED = 0x03;
	private final static byte CARDCONNECTED = 0x05;

	public final static byte[] requestICC = { (byte) 0x20, (byte) 0x12,
			(byte) 0x01, (byte) 0x01, (byte) 0x00 };
	public final static byte[] getStatus = { (byte) 0x20, (byte) 0x13,
			(byte) 0x00, (byte) 0x80, (byte) 0x00 };

	protected boolean termopened;
	protected byte[] cardStatus;
	protected CardID[] cardIdTable;
	protected cardterminal_api CT;
	protected char ctn, pn;

	/** Determines if polling is used for this terminal */
	private boolean polling;

	/**
	 * Create CTAPICardTerminal object
	 * 
	 * @param name
	 * @param type
	 * @param device
	 * @param libname
	 * @throws CardTerminalException
	 */
	protected CTAPICardTerminal(String name, String type, String device,
			String libname) throws CardTerminalException {

		super(name, type, device);

		polling = !type.endsWith("-NOPOLL"); // Disable polling if type is "*-NOPOLL"

		termopened = false;
		CT = new cardterminal_api(libname);
		try {
			ctn = (char) Integer.decode(address).intValue();
		} catch (NumberFormatException nfe) {
			throw (new CardTerminalException(
					"CTAPICardTerminal: Invalid port address."));
		}
		pn = ctn;
	}

	/**
	 * Open card terminal connection
	 * 
	 * Called from OCF during startup
	 * 
	 */
	public void open() throws CardTerminalException {
		int rc, len;
		byte[] newStatus;

		if (termopened == true)
			throw (new CardTerminalException(
					"CTAPICardTerminal: Already opened."));

		synchronized (this) {
			rc = CT.CT_Init(ctn, pn);
		}

		if (rc < 0)
			throw (new CardTerminalException(
					"CTAPICardTerminal: CT_Init failed with rc=" + rc));

		termopened = true;

		// Get status to determine number of slots
		newStatus = getStatus();

		len = newStatus.length;
		addSlots(len);
		cardStatus = new byte[len];
		cardIdTable = new CardID[len];

		if (polling) {
			CardTerminalRegistry.getRegistry().addPollable((Pollable) this);
		}
	}

	/**
	 * Close used resources
	 * 
	 */
	public void close() throws CardTerminalException {

		if (termopened == false)
			throw (new CardTerminalException(
					"CTAPICardTerminal: Terminal not opened."));

		if (polling) {
			CardTerminalRegistry.getRegistry().removePollable((Pollable) this);
		}
		
		synchronized (this) {

			if (CT.CT_Close(ctn) == 0)
				termopened = false;
		}
		if (termopened == true) {
			CardTerminalRegistry.getRegistry().addPollable((Pollable) this);
			throw (new CardTerminalException(
					"CTAPICardTerminal: CT_close failed."));
		}
	}

	/**
	 * Return true is slot contains a card
	 *
	 * @param slot Slot number starting at 0
	 */
	public boolean isCardPresent(int slot) throws CardTerminalException {
		if (termopened == false)
			throw (new CardTerminalException(
					"CTAPICardTerminal: isCardPresent(), Terminal not opened."));

		if (!polling) {
			poll();
		}
		return cardIdTable[slot] != null;
	}

	/**
	 * Return ATR for card in slot
	 * 
	 */
	public CardID getCardID(int slot) throws CardTerminalException {
		if (termopened == false)
			throw (new CardTerminalException(
					"CTAPICardTerminal: getCardID(), Terminal not opened."));

		return cardIdTable[slot];
	}

	/**
	 * Reset card in slot and return ATR
	 */
	protected CardID internalReset(int slot, int ms)
			throws CardTerminalException {
		byte[] response;
		byte[] buf = new byte[258];
		int res;
		byte[] com = { (byte) 0x20, (byte) 0x11, (byte) (slot + 1),
				(byte) 0x01, (byte) 0x00 };
		char buflen;
		CardID cid;

		if (termopened == false)
			throw (new CardTerminalException(
					"CTAPICardTerminal: internalReset(), Terminal not opened."));

		cardIdTable[slot] = null;

		buflen = (char) buf.length;
		synchronized (this) {
			res = CT.CT_Data(ctn, (byte) 1, (byte) 2, (char) com.length, com,
					buflen, buf);
		}

		ctracer.debug("internalReset", "CT_Data rc=" + res + " returns " + HexString.hexify(buf));
		
		if (res < 0)
			throw (new CardTerminalException(
					"CTAPICardTerminal: internalReset(), ERROR=" + res));

		if ((res < 2) || ((buf[res - 2] & 0xFF) != 0x90))
			throw (new CardTerminalException(
					"CTAPICardTerminal: internalReset(), No card inserted."));

		response = new byte[res - 2];
		System.arraycopy(buf, 0, response, 0, res - 2);

		cid = new CardID(this, slot, response);

		cardIdTable[slot] = cid;

		return cid;
	}

	/**
	 * Send APDU to card in slot
	 * 
	 */
	protected ResponseAPDU internalSendAPDU(int slot, CommandAPDU capdu, int ms)
			throws CardTerminalException {
		byte[] response;
		char resplen;
		byte fu;
		byte[] com;
		byte[] resp = new byte[16386];
		int res;

		if (termopened == false)
			throw (new CardTerminalException(
					"CTAPICardTerminal: internalSendAPDU(), Terminal not opened."));

		com = capdu.getBytes();

		resplen = (char) resp.length;

		fu = 0;
		if (slot > 0) {
			fu = (byte) (1 + slot);
		}
		synchronized (this) {
			res = CT.CT_Data(ctn, fu, (byte) 2, (char) com.length, com,
					resplen, resp);
		}

		if (res <= 0) {
			throw (new CardTerminalException(
					"CTAPICardTerminal: internalSendAPDU(), Error=" + res));
		}

		response = new byte[res];
		System.arraycopy(resp, 0, response, 0, res);
		return new ResponseAPDU(response);
	}

	/**
	 * Poll for status change
	 * 
	 * This is called from OCF every second
	 * 
	 */
	public void poll() throws CardTerminalException {

		int i;
		boolean updateStatus = false;
		byte[] newStatus;

		newStatus = getStatus();

		for (i = 0; i < newStatus.length; i++) {
//			ctracer.debug("poll", "Status " + newStatus[i] + " on slot " + i);
			if (newStatus[i] != cardStatus[i]) { // Status change
//				ctracer.debug("poll","Status change " + newStatus[i] + " on slot " + i);
				if (newStatus[i] == NOCARD) { // Card removed
					cardIdTable[i] = null;
					cardStatus[i] = NOCARD;
					cardRemoved(i);
				} else { // Something else happend
					try {
						internalReset(i, 0);
						cardInserted(i);
					} catch (CardTerminalException e) {
						// System.out.println(e);
						// Do nothing
					}
					updateStatus = true;
				}
			}
		}
		if (updateStatus) { // Update status of all slots
			cardStatus = getStatus();
		}
	}

	/**
	 * Send a control command to the terminal
	 * 
	 */
	public byte[] sendTerminalCommand(byte[] com) throws CardTerminalException {
		byte[] response;
		byte[] resp;
		char buflen;
		int res;

		if (termopened == false)
			throw (new CardTerminalException(
					"CTAPICardTerminal: sendTerminalCommand(), Terminal not opened."));

		resp = new byte[258];
		buflen = (char) resp.length;

		synchronized (this) {
			res = CT.CT_Data(ctn, (byte) 1, (byte) 2, (char) com.length, com,
					buflen, resp);
		}

		if (res < 2)
			throw (new CardTerminalException(
					"CTAPICardTerminal: internalSendAPDU(), ERROR!"));

		response = new byte[res];
		System.arraycopy(resp, 0, response, 0, res);

		return response;
	}

	/**
	 * Issue STATUS command to query status of card reader slots
	 * 
	 * @return Byte array of slot status as returned by STATUS command
	 * 
	 * @throws CardTerminalException
	 */
	public byte[] getStatus() throws CardTerminalException {
		byte[] buf = new byte[258];
		char lenbuf = (char) buf.length;
		int i, len;

		synchronized (this) {
			len = CT.CT_Data(ctn, (byte) 1, (byte) 2, (char) getStatus.length,
					getStatus, lenbuf, buf);
		}

		if (len <= 0) {
			throw (new CardTerminalException(
					"CTAPICardTerminal: GetStatus() failed"));
		}
		
		i = 0;
		if (buf[0] == (byte) 0x80) {
			len = buf[1];
			i += 2;
		} else {
			len -= 2;
		}

		byte[] response = new byte[len];
		System.arraycopy(buf, i, response, 0, len);
		return response;
	}
}
