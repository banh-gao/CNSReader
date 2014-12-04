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

package de.cardcontact.opencard.terminal.jcopsim;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.HexString;
import opencard.core.util.Tracer;

/**
 * Class implementing a JCOP simulation card terminal
 * 
 * @author Frank Thater (info@cardcontact.de)
 */
public class JCOPSimCardTerminal extends CardTerminal {
	
	private final static Tracer ctracer = new Tracer(JCOPSimCardTerminal.class);

	public static final int DEFAULT_SOCKET_TIMEOUT = 5000;

	// Set the buffer size to 65536 (max. extended length) + 100 bytes reserved for the protocol overhead
	private static final int JCOP_RECV_BUFFER_SIZE = 65636;

	/**
	 * Message types accepted by the simulation
	 */
	static final byte MTY_WAIT_FOR_CARD = 0;
	static final byte MTY_APDU_DATA = 1;
	static final byte MTY_STATUS = 2;
	static final byte MTY_ERROR_MESSAGE = 3;
	static final byte MTY_TERMINAL_INFO = 4;
	static final byte MTY_INIT_INFO = 5;
	static final byte MTY_ECHO = 6;
	static final byte MTY_DEBUG = 7;

	/**
	 * Node number of terminal
	 */
	static final byte NODE_TERMINAL = 33;

	/**
	 * Node number of card
	 */
	static final byte NODE_CARD = 0;

	/**
	 * Status of the reader simulation
	 */
	static final int NOT_CONNECTED = 1;
	static final int SLOT_EMPTY = 2;
	static final int CARD_PRESENT = 4;
	static final int ERROR = 8;
	static final int PROTOCOL_T0 = 0;
	static final int PROTOCOL_T1 = 1;
	static final int PROTOCOL_TCL = 5;


	/**
	 * Socket for communication
	 */
	private Socket socket = null;

	/**
	 * Remote address (Hostname, Port) of the simulation
	 */
	private SocketAddress socketAddr;

	/**
	 * Timeout value for socket
	 */
	private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

	/**
	 * Stream for incoming data
	 */
	private BufferedInputStream inStream = null;

	/**
	 * Stream for outgoing data
	 */
	private BufferedOutputStream outStream = null;

	/**
	 * CardID of the simulated card
	 */
	private CardID cid = null;

	/**
	 * Indicator for established connection
	 */
	private boolean connected = false;

	/**
	 * Data buffer
	 */
	private byte[] jcopBuffer;


	/**
	 * Constructor for JCOPSimCardTerminal
	 * 
	 * @param name
	 * 			Friendly name of the terminal
	 * @param type
	 * 			Type of the card terminal
	 * @param address
	 * 			Identifier for the driver to locate the terminal
	 * @param host
	 * 			Host of the remote terminal simulation
	 * @param port
	 * 			Port number of the remote terminal simulation
	 * @param timeout 
	 * 
	 * @throws CardTerminalException
	 */
	public JCOPSimCardTerminal(String name, String type, String address, String host, int port, int timeout) throws CardTerminalException {

		super(name, type, address);

		socketAddr = new InetSocketAddress(host, port);
		jcopBuffer = new byte[JCOP_RECV_BUFFER_SIZE];

		this.socketTimeout = timeout;

		addSlots(1);
	}



	/* (non-Javadoc)
	 * @see opencard.core.terminal.CardTerminal#getCardID(int)
	 */
	public CardID getCardID(int slotID) throws CardTerminalException {
		
		if (!connected) {
			open();
		}
		
		if (!connected) {
			throw new CardTerminalException("JCOPSimCardTerminal: getCardID(), Terminal not opened.");
		}
		
		return cid;
	}
	
	
	
	/* (non-Javadoc)
	 * @see opencard.core.terminal.CardTerminal#isCardPresent(int)
	 */
	public boolean isCardPresent(int slotID) throws CardTerminalException {
		if (!connected) {
			open();
		}
		return connected;
	}
	
	
	
	/* (non-Javadoc)
	 * @see opencard.core.terminal.CardTerminal#open()
	 */
	public void open() throws CardTerminalException {
				
		if (connected) {
			return;
		}
		
		try {
			// Try to open the specified socket
			socket = new Socket();
			socket.connect( socketAddr, 100 );
			
			// Set timeout for socket
			socket.setSoTimeout(this.socketTimeout);
						
			// get streams for communication
			outStream = new BufferedOutputStream(socket.getOutputStream());
			inStream = new BufferedInputStream(socket.getInputStream());
			
			connected = true;
		} catch(ConnectException ste) {
			// Ignore, server may run sometimes later
		} catch(SocketTimeoutException ste) {
			// Ignore, server may run sometimes later
		} catch (Exception e) {
			throw new CardTerminalException("JCOPSimCardTerminal: Card terminal could not be opened! Reason: " + e.getLocalizedMessage());
		}

		try {
			
			if (connected) {
				// request JCOP ATR
				byte[] data = {0x00, 0x00, 0x00, 0x00};
				sendJcop(MTY_WAIT_FOR_CARD, NODE_TERMINAL, data);
				
				int read = 0;
				read = readJcop(MTY_WAIT_FOR_CARD, jcopBuffer);
				
				byte[] scr = new byte[read];
				System.arraycopy(jcopBuffer, 0, scr, 0, read);
				
				cid = new CardID(scr);
				
				cardInserted(0);
			}
		} catch (SocketException se) {
			close();
		} catch (Exception e) {
			close();
			throw new CardTerminalException("JCOPSimCardTerminal: Error in socket communication! Reason: " + e.getLocalizedMessage());
		}	
	}
	
	
	
	/* (non-Javadoc)
	 * @see opencard.core.terminal.CardTerminal#close()
	 */
	public void close() throws CardTerminalException {
		
		if (connected) {
			try {
				connected = false;
							
				outStream.close();
				inStream.close();
					
				outStream = null;
				inStream = null;
					
				socket.close();						
					
			} catch (Exception e) {
				throw new CardTerminalException("JCOPSimCardTerminal: Error in socket communication! Reason: " + e.getLocalizedMessage());
			}			
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see opencard.core.terminal.CardTerminal#internalReset(int, int)
	 */
	protected CardID internalReset(int slot, int ms) throws CardTerminalException {
		
		close();
		open();
					
		return cid;
	}
	
	
	
	/* (non-Javadoc)
	 * @see opencard.core.terminal.CardTerminal#internalSendAPDU(int, opencard.core.terminal.CommandAPDU, int)
	 */
	protected ResponseAPDU internalSendAPDU(int slot, CommandAPDU capdu, int ms) throws CardTerminalException {

		if (!connected) {
			open();
		}
		
		if (!connected) {
			throw new CardTerminalException("JCOPSimCardTerminal: Error sending APDU! No connection");
		}
		
		ResponseAPDU r = null;
		
		try {
				
			byte[] apdu = capdu.getBytes();
		
			sendJcop(MTY_APDU_DATA, NODE_CARD, apdu);
				
			int read = 0;
			read = readJcop(MTY_APDU_DATA, jcopBuffer);
							
			byte[] rsp = new byte[read];
			System.arraycopy(jcopBuffer, 0, rsp, 0, read);
				
			r = new ResponseAPDU(rsp);
		}
		catch (Exception e) {				
			ctracer.debug("internalSendAPDU()", "Error sending APDU: " + e.getMessage());
			throw new CardTerminalException("JCOPSimCardTerminal: Error sending APDU! Reason: " + e.getLocalizedMessage());
		}

		return r;
	}



	/**
	 * Send a command message to the remote terminal simulation
	 * 
	 * @param mty
	 * 			Message type
	 * @param destNode
	 * 			Destination node
	 * @param cmd
	 * 			Command data
	 * 
	 * @throws CardTerminalException
	 */
	private void sendJcop(byte mty, byte destNode, byte[] cmd) throws CardTerminalException {
		
		int length = cmd == null ? 0 : cmd.length;
		
		byte[] scr = new byte[4 + length];
		
		scr[0] = mty;
		scr[1] = destNode;
		scr[2] = (byte)(length / 256);		
		scr[3] = (byte) length;
		
		if (cmd != null) {
			System.arraycopy(cmd, 0, scr, 4, length);
		}
		
		try {
			ctracer.debug("sendJcop()", "SEND: " + HexString.dump(scr, 0, scr.length));
			outStream.write(scr);
			outStream.flush();
		} catch (IOException e) {
			connected = false;
		}		
	}
	
	
	
	/**
	 * Read a command message from the remote terminal simulation and extract the "raw" command data 
	 * 
	 * @param mty
	 * 			Expected message type
	 * @param buf
	 * 			Destination buffer for command data
	 * @return
	 * 			Number of bytes read
	 * 
	 * @throws Exception
	 */
	private int readJcop(byte mty, byte[] buf) throws Exception {
			
		int sizeRsp = -1;
		int totalBytesRead = 0;
		int read = 0;
		
		byte[] tmp = new byte[buf.length];
		
		read = inStream.read(tmp, 0, 4);
		
		if (read != 4) {
			connected = false;
			ctracer.debug("readJcop()", "JCOP header not received! recv = " + HexString.dump(tmp, 0, read) + " (" + read +")");
			throw new CardTerminalException("JCOP header not received!");
		}

		if (tmp[0] != mty) { // Incorrect message type? 
			connected = false;
			ctracer.debug("readJcop()", "Mismatch of message types");
			throw new CardTerminalException("Mismatch of message types");
		}
		
		sizeRsp = (tmp[2] & 0xff) << 8 | (tmp[3] & 0xff);
		
		while (totalBytesRead < sizeRsp) {			
			read = inStream.read(tmp, 0, tmp.length);			
			System.arraycopy(tmp, 0, buf, totalBytesRead, read);			
			totalBytesRead += read;
		}
		
		if (inStream.available() > 0) {
			ctracer.debug("readJcop()", "Warning: not all bytes were read! left = " + inStream.available());			
		}
		
		return totalBytesRead;
	}
}
