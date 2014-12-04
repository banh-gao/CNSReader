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

import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalFactory;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.terminal.TerminalInitException;

/**
 * Class implementing a JCOP simulation card terminal factory
 * 
 * @author Frank Thater (info@cardcontact.de)
 */
public class JCOPSimCardTerminalFactory implements CardTerminalFactory {


	/**
	 * Constructor
	 */
	public JCOPSimCardTerminalFactory() {
		super();
	}

	
	
	/* (non-Javadoc)
	 * @see opencard.core.terminal.CardTerminalFactory#createCardTerminals(opencard.core.terminal.CardTerminalRegistry, java.lang.String[])
	 */
	public void createCardTerminals(CardTerminalRegistry ctr, String[] terminalInfo) throws CardTerminalException, TerminalInitException {

		/*
		 * OpenCard.terminals =    de.cardcontact.opencard.terminal.jcopsim.JCOPSimCardTerminalFactory|Reader1|JCOPSIM|0|localhost|8050|5000  
		 */

		String host = "localhost"; // Default host

		int port = 8050; // Default port

		int timeout = JCOPSimCardTerminal.DEFAULT_SOCKET_TIMEOUT;

		if (terminalInfo.length < 3) {
			throw new TerminalInitException("JCOPSimCardTerminalFactory needs at least 3 parameters.");
		}

		if (!terminalInfo[TERMINAL_TYPE_ENTRY].equals("JCOPSIM")) {
			throw new TerminalInitException("Requested Terminal type not known.");
		}

		if (terminalInfo.length >= 4) {
			host = terminalInfo[3];
		}

		if (terminalInfo.length >= 5) {
			port = Integer.parseInt(terminalInfo[4]);
		}

		if (terminalInfo.length == 6) {
			timeout = Integer.parseInt(terminalInfo[5]);
		}

		try {
			ctr.add(new JCOPSimCardTerminal(terminalInfo[TERMINAL_NAME_ENTRY],terminalInfo[TERMINAL_TYPE_ENTRY],terminalInfo[TERMINAL_ADDRESS_ENTRY], host, port, timeout));	
		}
		catch (Exception e) {
			throw new TerminalInitException("JCOPSimCardTerminal could not be added to card terminal registry! " + e.getMessage());
		}

	}



	/* (non-Javadoc)
	 * @see opencard.core.terminal.CardTerminalFactory#open()
	 */
	public void open() throws CardTerminalException {
	}

	
	
	/* (non-Javadoc)
	 * @see opencard.core.terminal.CardTerminalFactory#close()
	 */
	public void close() throws CardTerminalException {
	}
}
