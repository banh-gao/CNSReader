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
package de.cardcontact.opencard.terminal.remoteterminal;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import de.cardcontact.opencard.service.remoteclient.RemoteProtocolUnit;

import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.CommunicationErrorException;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.SlotChannel;
import opencard.core.terminal.TerminalTimeoutException;
import opencard.core.util.Tracer;

public class RemoteTerminal extends CardTerminal {

	private final static Tracer ctracer = new Tracer(RemoteTerminal.class);

	private CardID cardID = null;
	
	private LinkedBlockingQueue<RemoteProtocolUnit> comQueue = new LinkedBlockingQueue<RemoteProtocolUnit>(1);
	private LinkedBlockingQueue<RemoteProtocolUnit> resQueue = new LinkedBlockingQueue<RemoteProtocolUnit>(1);



	protected RemoteTerminal(String name, String type, String address) throws CardTerminalException {
		super(name, type, address);		
		ctracer.debug("RemoteTerminal", "TERMINAL: starting " + name);
		addSlots(1);
	}



	/**
	 * Transmit a command object and wait for a response object
	 * 
	 * @param cmdObject the command object (i.e. CommandAPDU, Reset, Notify)
	 * @return the response object (i.e. ResponseAPDU, CardID, Notify)
	 * @throws CardTerminalException
	 */
	protected RemoteProtocolUnit transmit(RemoteProtocolUnit cmdObject) throws CardTerminalException {
		RemoteProtocolUnit resObject;
		
		try {
			comQueue.put(cmdObject);

			ctracer.debug("transmit", "Waiting for response");
	
			resObject = resQueue.poll(60, TimeUnit.SECONDS);
			if (resObject == null) {
				throw new TerminalTimeoutException("The waiting time of 60 seconds for the response has expired.", 60);
			}
			if (resObject.isClosing()) {
				throw new CommunicationErrorException(resObject.getMessage());
			}
		} catch (InterruptedException e) {
			throw new CardTerminalException(e.getMessage());
		}
		return resObject;
	}



	/**
	 * Poll for a command object. Used by remote connection.
	 * @return the command object (i.e. CommandAPDU, RemoteControl)
	 * @throws CardTerminalException
	 */
	public RemoteProtocolUnit poll(int timeout) throws CardTerminalException {
		RemoteProtocolUnit comObject;

		try {				
			ctracer.debug("poll", "TERMINAL: wait for available com apdu");
			ctracer.debug("poll", "TERMINAL: Queue size" + comQueue.size());
			comObject = comQueue.poll(timeout, TimeUnit.SECONDS);	
			ctracer.debug("poll", "TERMINAL: received com apdu");
			if (comObject == null) {
				throw new CommunicationErrorException("The waiting time of " + timeout + " seconds for the command apdu has expired.");
			}
		} catch (InterruptedException e) {
			throw new CardTerminalException(e.getMessage());
		}

		return comObject;
	}



	/**
	 * Put response object into queue. Used by remote connection.
	 * 
	 * @return the response object (i.e. ResponseAPDU, CardID, RemoteControl)
	 * @throws CardTerminalException
	 */
	public void put(RemoteProtocolUnit resObject) throws CardTerminalException {
		ctracer.debug("put", "Put response into queue");
		try {
			resQueue.put(resObject);
		} catch (InterruptedException e) {
			throw new CardTerminalException(e.getMessage());
		}
	}



	@Override
	protected void internalCloseSlotChannel(SlotChannel sc)
			throws CardTerminalException {

		ctracer.debug("internalCloseSlotChannel", "TERMINAL: Closing terminal " + this.name);

		if (!comQueue.isEmpty()) {
			ctracer.debug("internalCloseSlotChannel", "TERMINAL: clearing com queue...");
			comQueue.clear();
		}
		try {
			comQueue.put(new RemoteProtocolUnit(RemoteProtocolUnit.Action.CLOSE));
		} catch (InterruptedException e) {
			throw new CardTerminalException(e.getMessage());
		}
	}



	@Override
	public CardID getCardID(int slotID) throws CardTerminalException {
		if (this.cardID != null) {
			return this.cardID;
		}
		return new CardID(this, 0, new byte[] {0x3b, (byte)0x80, 0x00, 0x00});
	}



	public void setCardID(CardID cardID) throws CardTerminalException {
		this.cardID = new CardID(this, 0, cardID.getATR());
	}



	@Override
	public boolean isCardPresent(int slotID) throws CardTerminalException {
		return true;
	}



	@Override
	public void open() throws CardTerminalException {
		ctracer.debug("open", "open");
	}



	@Override
	public void close() throws CardTerminalException {
		ctracer.debug("close", "close");
	}



	@Override
	protected CardID internalReset(int slot, int ms) throws CardTerminalException {

		RemoteProtocolUnit rpu = transmit(new RemoteProtocolUnit(RemoteProtocolUnit.Action.RESET));

		if (!rpu.isRESET()) {
			throw new CardTerminalException("Received unexpected message");
		}

		setCardID((CardID)rpu.getPayload());
		return this.cardID;
	}



	@Override
	protected ResponseAPDU internalSendAPDU(int slot, CommandAPDU capdu, int ms)
			throws CardTerminalException {

		RemoteProtocolUnit rpu = transmit(new RemoteProtocolUnit(capdu));

		if (!rpu.isAPDU()) {
			throw new CardTerminalException("Received unexpected message");
		}
		
		return (ResponseAPDU)rpu.getPayload();
	}



	public String sendNotification(int id, String message) throws CardTerminalException {

		RemoteProtocolUnit rpu = transmit(new RemoteProtocolUnit(RemoteProtocolUnit.Action.NOTIFY, id, message));

		if (!rpu.isNOTIFY()) {
			throw new CardTerminalException("Received unexpected message");
		}
		
		return (String)rpu.getPayload();
	}
}
