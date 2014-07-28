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

import java.util.Enumeration;
import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalFactory;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.terminal.Pollable;
import opencard.core.terminal.TerminalInitException;
import opencard.core.util.Tracer;

/**
 * Factory that creates a CardTerminal object for each card reader listed via the javax.smartcardio interface.
 * 
 */
public class SmartCardIOFactory implements CardTerminalFactory, Pollable {

	private final static Tracer ctracer = new Tracer(SmartCardIOFactory.class);

	private int numberOfRegisteredTerminals = 0;



	@Override
	public void close() throws CardTerminalException {
		// Empty
	}



	/**
	 * Creates an instance for each card listed.
	 */
	@Override
	public void createCardTerminals(CardTerminalRegistry ctr, String[] terminalInfo)
			throws CardTerminalException, TerminalInitException {

		String terminalType = "SmartCardIO";

		if (terminalInfo.length >= 2) {
			terminalType = terminalInfo[1];
		}
		if (terminalType.endsWith("-NOPOLL")) {
			try	{
				TerminalFactory factory = TerminalFactory.getDefault();
				List<javax.smartcardio.CardTerminal> terminals = factory.terminals().list();
				
				for (javax.smartcardio.CardTerminal ct : terminals) {
					ctr.add(new SmartCardIOTerminal(ct.getName(), terminalType, "", ct));
					numberOfRegisteredTerminals++;
				}
			}
			catch(CardException ce) {
				ctracer.error("createCardTerminals", ce);
			}
		} else {
			CardTerminalRegistry.getRegistry().addPollable(this);
		}
	}



	@Override
	public void open() throws CardTerminalException {
		// Empty
	}



	/**
	 * Check whether a new physical terminal was plugged in or removed.
	 * If so update the CardTerminalRegistry.
	 */
	@Override
	public void poll() throws CardTerminalException {
		CardTerminalRegistry ctr = CardTerminalRegistry.getRegistry();
		
		TerminalFactory factory = TerminalFactory.getDefault();
		CardTerminals ts = factory.terminals();
		List<javax.smartcardio.CardTerminal> terminals = null;
		try {
			terminals = ts.list();
		} catch (CardException e) {
			// Catch exception which is thrown when no terminal is available
			if(numberOfRegisteredTerminals > 0) {
				removeAllTerminals(ctr);
			}
			return;
		}
		int numberOfTerminals = terminals.size();
		
		if (numberOfTerminals < numberOfRegisteredTerminals) {
			removeTerminals(terminals, ctr);
		}
		if (numberOfTerminals > numberOfRegisteredTerminals) {
			addTerminals(terminals, ctr);
		}
	}
	
	
	
	/**
	 * Remove all terminals from the CardTerminalRegistry
	 * @param ctr OCF card terminal registry
	 * @throws CardTerminalException
	 */
	private void removeAllTerminals(CardTerminalRegistry ctr) throws CardTerminalException {
		Enumeration terminals = ctr.getCardTerminals();
		while(terminals.hasMoreElements()) {
			ctr.remove((CardTerminal)terminals.nextElement());
		}
		numberOfRegisteredTerminals = 0;
	}



	/**
	 * Remove Terminals which doesn't exists any more
	 * 
	 * @param terminals SmartCardIO terminals
	 * @param ctr OCF card terminal registry
	 * @throws CardTerminalException 
	 */
	private void removeTerminals(List<javax.smartcardio.CardTerminal> terminals, CardTerminalRegistry ctr) throws CardTerminalException {
		Enumeration registeredTerminals = ctr.getCardTerminals();
		
		while(registeredTerminals.hasMoreElements()) {
			CardTerminal rct = (CardTerminal)registeredTerminals.nextElement();
			if (rct instanceof SmartCardIOTerminal) {
				boolean isRemoved = true;			
				for (javax.smartcardio.CardTerminal ct : terminals) {
					if (ct.getName().equals(rct.getName())) {
						isRemoved = false;
						break;
					}
				}
				if (isRemoved) {
					ctr.remove(rct);
					numberOfRegisteredTerminals--;
				}
			}
		}
	}
	
	
	
	/**
	 * Add all new card terminal to the card terminal registry
	 * 
	 * @param terminals SmartCardIO terminals
	 * @param ctr OCF card terminal registry
	 * @throws CardTerminalException
	 */
	private void addTerminals(List<javax.smartcardio.CardTerminal> terminals, CardTerminalRegistry ctr) throws CardTerminalException {
		for (javax.smartcardio.CardTerminal ct : terminals) {
			Enumeration registeredTerminals = ctr.getCardTerminals();
			
			boolean isNew = true;
			while(registeredTerminals.hasMoreElements()) {
				CardTerminal rct = (CardTerminal)registeredTerminals.nextElement();
				if (ct.getName().equals(rct.getName())) {
					isNew = false;
					break;
				}
			}
			if (isNew) {
				ctr.add(new SmartCardIOTerminal(ct.getName(), "PCSC", "", ct));
				numberOfRegisteredTerminals++;
			}
		}
	}	
}
