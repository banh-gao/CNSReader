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

package de.cardcontact.opencard.terminal.jcwdpsim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import com.sun.javacard.apduio.Apdu;
import com.sun.javacard.apduio.CadClientInterface;
import com.sun.javacard.apduio.CadDevice;
import com.sun.javacard.apduio.CadTransportException;
import com.sun.javacard.apduio.TLP224Exception;

import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.Pollable;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.HexString;
import opencard.core.util.Tracer;

/**
 * Class implementing a SUN JCWDE simulation card terminal using T=1 protocol
 * 
 * This class is based on the Gemplus ApduIO reference implementation. 
 * There were some necessary modifications to adapt that terminal to the new JC 2.2.2 reference implementation.
 * 
 * @author Frank Thater (info@cardcontact.de)
 */
public class JCWDPSimCardTerminal extends CardTerminal implements Pollable {

	/**
	 * Simple OCF tracer
	 */
	private final static Tracer ctracer = new Tracer(JCWDPSimCardTerminal.class);

	/**
	 * Client connection to the terminal server VM
	 */
	private CadClientInterface cad;

	/**
	 * Socket for communication
	 */
	private Socket socket = null;

	/**
	 * Remote address (Hostname, Port) of the simulation
	 */
	private SocketAddress socketAddr;

	/**
	 * CardID of the simulated card
	 */
	private CardID cid = null;

	/**
	 * Indicators for established connection and opened socket
	 */
	private boolean opened = false;
	private boolean connected = false;

	
	/**
	 * Constructor for JCWDPSimCardTerminal
	 * 
	 * @param name
	 *            Friendly name of the terminal
	 * @param type
	 *            Type of the card terminal
	 * @param address
	 *            Identifier for the driver to locate the terminal
	 * @param host
	 *            Host of the remote terminal simulation
	 * @param port
	 *            Port number of the remote terminal simulation
	 * @param timeout
	 * 
	 * @throws CardTerminalException
	 */
	public JCWDPSimCardTerminal(String name, String type, String address,
			String host, int port) throws CardTerminalException {

		super(name, type, address);

		ctracer.info("<init>", "name|type|address|host|port = " + name + "|"
				+ type + "|" + address + "|" + host + "|" + port);

		socketAddr = new InetSocketAddress(host, port);

		addSlots(1);
	}

	
	
	/**
	 * Open the terminal - just adds the terminal to the OCF polling registry.
	 * 
	 * @exception CardTerminalException if there are problems adding the terminal to the registry
	 */
	public synchronized void open() {
		ctracer.info("open", "open terminal: " + getName());
		ctracer.debug("open", "add terminal to polling-list");

		// add this terminal to polling-list
		CardTerminalRegistry.getRegistry().addPollable((Pollable) this);
		opened = true;
	}

	
	
	/**
	 * Disable the terminal
	 * 
	 * Removes the terminal from the polling registry, powers down the client 
	 * interface and closes the socket 
	 * 
	 * @exception CardTerminalException
	 *                Thrown in case of errors during close process
	 */
	public synchronized void close() throws CardTerminalException {
		ctracer.info("close", "close terminal: " + getName());
		ctracer.debug("close", "remove terminal from polling-list");
		
		// remove this terminal from the polling-list
		CardTerminalRegistry.getRegistry().removePollable((Pollable) this);

		// power down the card, if inserted
		cid = null;
		
		try {
			if (cad != null) // if inserted
			{
				cad.powerDown();
				socket.close();
				opened = false;
				ctracer.debug("close", "close cad");
			}
			
		} catch (IOException ioex) {
			ctracer.debug("close", "IOException in close:\nMessage: "
					+ ioex.getMessage());
		} catch (TLP224Exception tex) {
			throw new CardTerminalException(
					"TLP224Exception in close:\nMessage: " + tex.getMessage());
		} catch (CadTransportException e) {
			throw new CardTerminalException(
					"CadTransportException in close:\nMessage: "
							+ e.getMessage());
		}
	}

	
	
	/**
	 * Check whether a smart card is present in a particular slot.
	 * 
	 * @param slotID slot to check for a card.
	 * 
	 * @return true if connected to a simulator
	 * 
	 * @exception IndexOutOfBoundsException when the slotID is different from 0
	 */
	public synchronized boolean isCardPresent(int slotID) throws CardTerminalException {
		
		ctracer.info("isCardPresent", "isCardPresent(" + slotID + " on " + getName() + "...");
		
		if (slotID == 0) {
			return connected;
		} else {
			throw new IndexOutOfBoundsException("Wrong slotID: only 0 is allowed.");
		}
	}

	
	
	/**
	 * Return the ATR of the card inserted in the specified slot.
	 * 
	 * @param slotID slot id.
	 * 
	 * @return The CardID containing the ATR.
	 * 
	 * @exception CardTerminalException in case of communication problems.
	 * @exception IndexOutOfBoundsException when the slotID is different from 0
	 */
	public synchronized CardID getCardID(int slotID)
			throws CardTerminalException {
		
		ctracer.info("getCardID", "getCardID(" + slotID + " on " + getName() + "...");
		
		if (isCardPresent(slotID)) {
			
			try {
				// powerUp the card
				byte[] atr = cad.powerUp();
				ctracer.debug("getCardID", "powered Up");
			
				// get the ATR
				cid = new CardID(this, slotID, atr);
			
			} catch (TLP224Exception tex) {
				throw new CardTerminalException(
						"TLP224Exception in getCardID: \n" + "Message: " + tex.getMessage());
			} catch (IOException ioex) {
				throw new CardTerminalException("IOException in getCardID: "
						+ "Message: " + ioex.getMessage());
			} catch (CadTransportException e) {
				throw new CardTerminalException(
						"CadTransportException in getCardID: " + "Message: " + e.getMessage());
			}
		}
		
		return cid;
	}

	
	
	/**
	 * Updates the card inserted/removed state.
	 */
	public synchronized void poll() {
		
		ctracer.info("poll", "polling " + getName() + "...");
		
		if (opened == true && connected == false) {
			
			try {
				// debug information
				ctracer.debug("poll", "simulator socket connection");

				// Try to open the specified socket
				socket = new Socket();
				socket.connect(socketAddr, 100);

				InputStream in = socket.getInputStream();
				OutputStream out = socket.getOutputStream();

				// Create the client interface using T=1 protocol
				cad = CadDevice.getCadClientInstance(CadDevice.PROTOCOL_T1, in, out);
				ctracer.debug("poll", "CadClient with protocol T=1 created");

				connected = true;

				// connection successful: notify all listeners
				cardInserted(0);

			} catch (IOException e) {
				// debug information
				ctracer.debug("poll", "socket connection failed");
				socket = null;
				connected = false;
			}
			
		} else {
			
			// already connected
			try {
				// verify connection using powerUp
				// works only because cad.powerUp doesn't do a reset	
				cid = new CardID(cad.powerUp());
			
			} catch (TLP224Exception tex) {
				ctracer.debug("poll", "TLP224Exception " + tex.getMessage());
				connected = false;
				cardRemoved(0);
			} catch (IOException e) {
				ctracer.debug("poll", "socket connection failed");
				socket = null;
				connected = false;
				cardRemoved(0);
			} catch (CadTransportException e) {
				ctracer.debug("poll", "CadTransportException " + e.getMessage());
				connected = false;
				cardRemoved(0);
			}
		}
	}

	
	
	/**
	 * Re-power up the card and retreives the ATR.
	 * 
	 * @param slotID the slot number of the slot used.
	 * @param ms A timeout in milliseconds. (ignored)
	 * 
	 * @return The CardID containing the ATR.
	 * 
	 * @exception CardTerminalException if there is a problem during reset.
	 */
	protected synchronized CardID internalReset(int slotID, int ms) throws CardTerminalException {
		
		ctracer.info("internalReset", "internalReset(" + slotID + ") on " + getName());
		
		if (isCardPresent(slotID) == false) {
			// to force the power up
			cid = null;
			return this.getCardID(slotID);
		} else {
			return cid;
		}
	}

	
	
	/**
	 * Exchange APDU commands
	 * 
	 * @param slotID The slot number of the slot used.
	 * @param capdu The <tt>CommandAPDU</tt> to send.
	 * @param ms A timeout in milliseconds. (ignored)
	 * 
	 * @return the response to this APDU
	 * 
	 * @exception CardTerminalException if there is an error in apdu exchange
	 */
	protected synchronized ResponseAPDU internalSendAPDU(int slotID,
			CommandAPDU capdu, int ms) throws CardTerminalException {
		ctracer.info("internalReset", "internalReset(" + slotID + ") on "
				+ getName());

		if (this.isCardPresent(slotID)) {

			// map CommandAPDU into ApduIO Apdu object
			Apdu apdu = new Apdu();

			ctracer.debug("internalSendAPDU", "capdu: "	+ HexString.hexify(capdu.getBytes()));

			byte[] cmd = apdu.command; // direct access to the byte array - ugly, but the getter seems not to work properly
			
			// build the header
			cmd[Apdu.CLA] = (byte) capdu.getByte(0);
			cmd[Apdu.INS] = (byte) capdu.getByte(1);
			cmd[Apdu.P1] = (byte) capdu.getByte(2);
			cmd[Apdu.P2] = (byte) capdu.getByte(3);

			int Lc = 0;
			int Le = 0;

			if (capdu.getLength() == 4) { // Case 1 command
				// no Lc, no Le
			} else if (capdu.getLength() == 5) { // Case 2 command
				// Le, no Lc
				Le = capdu.getByte(4);
			} else {
				Lc = capdu.getByte(4);
				if (capdu.getLength() == 5 + Lc) { // Case 3 command
					// Lc, no Le
				} else {
					// Lc, Le
					Le = capdu.getByte(capdu.getLength() - 1); // Case 4 command
				}
			}
			
			apdu.Le = Le;
			
			ctracer.debug("internalSendAPDU", "cmd : " + HexString.hexify(cmd));

			// data buffer
			if (Lc > 0) {
				byte[] dataIn = new byte[Lc];
				System.arraycopy(capdu.getBuffer(), 5, dataIn, 0, dataIn.length);
				apdu.setDataIn(dataIn);
				
				ctracer.debug("internalSendAPDU", "data: " + HexString.hexify(dataIn));
			}

			// Perform the apdu exchange
			try {
				this.cad.exchangeApdu(apdu);
			} catch (IOException ioex) {
				throw new CardTerminalException(
						"IOException in internalSendAPDU:\nMessage: " + ioex.getMessage());
			} catch (TLP224Exception tex) {
				throw new CardTerminalException(
						"TLP224Exception in internalSendAPDU:\nMessage: " + tex.getMessage());
			} catch (CadTransportException e) {
				throw new CardTerminalException(
						"CadTransportException in internalSendAPDU:\nMessage: "	+ e.getMessage());
			}

			// ResponseAPDU creation and setting
			byte[] resp = new byte[apdu.getLe() + 2];
			System.arraycopy(apdu.getDataOut(), 0, resp, 0, apdu.getLe());
			System.arraycopy(apdu.getSw1Sw2(), 0, resp, apdu.getLe(), 2);

			// debug information
			ctracer.debug("internalSendAPDU", "response: " + HexString.hexify(resp));
			ctracer.debug("internalSendAPDU", "got response: " + resp);

			return new ResponseAPDU(resp);
		
		} else {
			throw new CardTerminalException("no card inserted");
		}
	}
}
