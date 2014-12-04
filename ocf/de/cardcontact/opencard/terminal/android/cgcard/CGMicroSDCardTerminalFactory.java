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

import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalFactory;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.terminal.TerminalInitException;
import opencard.core.util.Tracer;

/**
 * Class implementing a card terminal factory for Certgate micro SD card
 * 
 * @author Frank Thater
 */
public class CGMicroSDCardTerminalFactory implements CardTerminalFactory {
	
	private final static Tracer ctracer = new Tracer(CGMicroSDCardTerminalFactory.class);

	
	public CGMicroSDCardTerminalFactory() {
		super();
	}

	
	
	@Override
	public void createCardTerminals(CardTerminalRegistry ctr, String[] terminalInfo) throws CardTerminalException, TerminalInitException {
		try {
			ctr.add(new CGMicroSDCardTerminal("cgCard", "MicroSD card", ""));	
		}
		catch (Exception e) {
			ctracer.debug("CGCardCardTerminalFactory", "createCardTerminals() failed: " + e.getLocalizedMessage());
			throw new TerminalInitException("CGCardCardTerminal could not be added to card terminal registry! " + e.getMessage());
		}
	}

	
	
	@Override
	public void open() throws CardTerminalException {
	}

	
	
	@Override
	public void close() throws CardTerminalException {
	}
	
}
