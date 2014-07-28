/*
 *  ---------
 * |.**> <**.|  CardContact
 * |*       *|  Software & System Consulting
 * |*       *|  Minden, Germany
 * |´**> <**´|  Copyright (c) 2000. All rights reserved
 *  --------- 
 *
 * See file LICENSE for details on licensing
 *
 * Abstract :       Implementation of a CTAPI Interface for Java.
 *
 * Author :         Frank Thater (FTH)
 *
 * Last modified:   08/04/2000
 *
 *****************************************************************************/

package de.cardcontact.opencard.terminal.ctapi4ocf;

import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalFactory;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.terminal.TerminalInitException;

/**
 * Implements a card terminal factory that can instantiate CT-API card terminals.
 * 
 */
public class CTAPICardTerminalFactory implements CardTerminalFactory {

	public void open() throws CardTerminalException {
	}

	public void close() throws CardTerminalException {
	}

	public void createCardTerminals(CardTerminalRegistry ctr, String[] terminfo)
			throws CardTerminalException, TerminalInitException {
		if (terminfo.length != 4)
			throw new TerminalInitException(
					"CTAPICardTerminalFactory needs 4 parameters.");

		
		if (terminfo[TERMINAL_TYPE_ENTRY].startsWith("CTAPIKBD")) {
			ctr.add(new CTAPIWithKeyboardCardTerminal(
					terminfo[TERMINAL_NAME_ENTRY],
					terminfo[TERMINAL_TYPE_ENTRY],
					terminfo[TERMINAL_ADDRESS_ENTRY], terminfo[3]));
		} else if (terminfo[TERMINAL_TYPE_ENTRY].startsWith("CTAPI")) {
				ctr.add(new CTAPICardTerminal(terminfo[TERMINAL_NAME_ENTRY],
						terminfo[TERMINAL_TYPE_ENTRY],
						terminfo[TERMINAL_ADDRESS_ENTRY], terminfo[3]));
		} else {
			throw new TerminalInitException(
					"Requested Terminal type not known.");
		}
	}
}
