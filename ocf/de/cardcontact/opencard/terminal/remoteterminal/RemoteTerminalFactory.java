package de.cardcontact.opencard.terminal.remoteterminal;

import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalFactory;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.terminal.TerminalInitException;

public class RemoteTerminalFactory implements CardTerminalFactory {

	@Override
	public void createCardTerminals(CardTerminalRegistry ctr,
			String[] terminalInfo) throws CardTerminalException,
			TerminalInitException {

		String terminalType = "RemoteTerminal";
		String name = terminalInfo[0];
		String address = "";
		
		ctr.add(new RemoteTerminal(name, terminalType, address));

		// ctr.add
	}

	@Override
	public void open() throws CardTerminalException {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() throws CardTerminalException {
		// TODO Auto-generated method stub

	}

}
