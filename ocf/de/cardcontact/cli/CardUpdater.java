/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2010 CardContact Software & System Consulting
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

package de.cardcontact.cli;

import java.util.Enumeration;

import de.cardcontact.opencard.factory.RemoteClientCardServiceFactory;
import de.cardcontact.opencard.factory.SmartCardHSMCardServiceFactory;
import de.cardcontact.opencard.service.remoteclient.RemoteClient;
import de.cardcontact.opencard.service.remoteclient.RemoteNotificationListener;
import de.cardcontact.opencard.terminal.smartcardio.SmartCardIOFactory;
import de.cardcontact.opencard.utils.StreamingAPDUTracer;
import opencard.core.OpenCardException;
import opencard.core.service.CardRequest;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceFactory;
import opencard.core.service.CardServiceRegistry;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalFactory;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.opt.security.CHVCardService;



/**
 * Connect to URL and update card
 * 
 */
public class CardUpdater implements RemoteNotificationListener {

	private String readerName = null;
	private String url = null;
	private String session = null;
	private int verbose = 2;
	private int lastMessageId = 0;
	private boolean reset = true;
	private boolean listReaders = false;
	private boolean password = false;



	public CardUpdater() {
	}



	public void log(int level, String msg) {
		if (verbose >= level) {
			System.out.println(msg);
		}
	}



	public int getVerbosityLevel() {
		return this.verbose;
	}



	@Override
	public void remoteNotify(int id, String message) {
		this.lastMessageId = id;
		log(1, message);
	}



	/**
	 * Configure and start OCF
	 * 
	 * @throws OpenCardException
	 * @throws ClassNotFoundException
	 */
	public void setupOCF() throws OpenCardException, ClassNotFoundException {
		SmartCard.startup();
		CardTerminalRegistry ctr = CardTerminalRegistry.getRegistry();
		CardTerminalFactory ctf = new SmartCardIOFactory();
		String param[] = { "*", "PCSC" };
		ctf.createCardTerminals(ctr, param);

		CardServiceRegistry csr = CardServiceRegistry.getRegistry();
		CardServiceFactory csf = new RemoteClientCardServiceFactory();
		csr.add(csf);
		csf = new SmartCardHSMCardServiceFactory();
		csr.add(csf);
	}



	/**
	 * Display help
	 */
	public void help() {
		System.out.println("Usage: java -jar ocf-cc.jar [-r <readername>] [-s <id>] [-n] [-l] [-v] [<url>]\n");
		System.out.println("-n\t\tNo card reset at end of session");
		System.out.println("-s <id>\t\tSession id");
		System.out.println("-n\t\tNo reset at end of session");
		System.out.println("-l\t\tList reader names");
		System.out.println("-v\t\tVerbose");
		System.out.println("-q\t\tQuiet");
		System.out.println("-p\t\tPassword verification");
		System.out.println("An URL on the command line deactivates the daemon mode and connects directly with that URL");
	}



	/**
	 * Decode command line arguments
	 * 
	 * @param args Arguments passed on the command line
	 * @return true if arguments valid
	 */
	public boolean decodeArgs(String[] args) {
		int i = 0;

		while (i < args.length) {
			if (args[i].equals("-r")) {
				readerName = args[++i];
			} else if (args[i].equals("-s")) {
				session = args[++i];
			} else if (args[i].equals("-v")) {
				verbose++;
			} else if (args[i].equals("-q")) {
				verbose--;
			} else if (args[i].equals("-n")) {
				reset = false;
			} else if (args[i].equals("-l")) {
				listReaders = true;
			} else if (args[i].equals("-p")) {
				password = true;
			} else if (args[i].charAt(0) == '-') {
				this.log(1, "Unknown option " + args[i]);
				return false;
			} else {
				url = args[i];
			}
			i++;
		}

		return true;
	}



	/**
	 * Perform update
	 * 
	 * @param args command line arguments
	 */
	public void run(String[] args) {
		if (!decodeArgs(args)) {
			help();
			System.exit(1);
		}
		try	{
			setupOCF();
			this.log(1, SmartCard.getVersion());

			if (listReaders) {
				CardTerminalRegistry ctr = CardTerminalRegistry.getRegistry();
				Enumeration ctlist = ctr.getCardTerminals();

				if (listReaders) {
					System.out.println("Available card terminals:");
					while(ctlist.hasMoreElements()) {
						CardTerminal ct = (CardTerminal)ctlist.nextElement();
						System.out.println(" " + ct.getName());
					}
					System.out.println("");
				}
			} else {
				CardTerminal ct = null;
				if (readerName != null) {
					CardTerminalRegistry ctr = CardTerminalRegistry.getRegistry();
					ct = ctr.cardTerminalForName(readerName);

					if (ct == null) {
						this.log(1, "Card reader " + readerName + " not found");
						System.exit(1);
					}
					this.log(1, "Using reader " + readerName);
				}

				if (password) {
					if (!verifyPin(ct, 0)) {
						this.log(1, "PIN not verified");
						System.exit(1);
					}
				}

				if (url == null) {
					CardUpdaterDaemon updaterDaemon = new CardUpdaterDaemon(this, ct);
					TrayView view = new TrayView();
					Thread t = new Thread(updaterDaemon);
					t.setDaemon(true);
					t.start();
				} else {
					this.log(1, "Connecting to " + url);

					CardRequest cr = new CardRequest(CardRequest.ANYCARD, ct, RemoteClient.class);
					cr.setTimeout(0);
					SmartCard sc = SmartCard.waitForCard(cr);
					if (sc == null) {
						this.log(1, "No card in reader");
					} else {
						RemoteClient rc = (RemoteClient)sc.getCardService(RemoteClient.class, true);
						sc.setAPDUTracer(new StreamingAPDUTracer(System.out));

						rc.update(url, session, this);

						this.log(1, "Update complete");

						if (reset) {
							sc.reset(false);
						}
						sc.close();
					}
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			this.lastMessageId = -1;
		}
		finally {
			try	{
				if (url != null) {
					SmartCard.shutdown();
				}
			}
			catch(Exception e) {
				// Ignore
			}
		}
	}



	/**
	 * Perform PIN verification
	 * 
	 * @param chvNumber the CHV number used for PIN verification
	 * @return true if verification was successful
	 * @throws CardTerminalException
	 * @throws CardServiceException
	 * @throws ClassNotFoundException
	 */
	boolean verifyPin(CardTerminal ct, int chvNumber) throws CardTerminalException, CardServiceException, ClassNotFoundException {
		CardRequest creq = new CardRequest(CardRequest.ANYCARD, ct, CHVCardService.class);
		creq.setTimeout(0);

		SmartCard card = SmartCard.waitForCard(creq);
		if (card == null) {
			this.log(1, "No card in reader");
			return false;
		}
		CHVCardService chv = (CHVCardService) card.getCardService(CHVCardService.class, true);

		boolean verified = false;
		verified = chv.verifyPassword(null, chvNumber, null);

		this.log(1, "PIN verified: " + verified);
		return verified;
	}



	public static void main(String[] args) {
		CardUpdater cu = new CardUpdater();
		cu.run(args);
	}
}
