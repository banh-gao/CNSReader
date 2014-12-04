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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import opencard.core.event.CTListener;
import opencard.core.event.CardTerminalEvent;
import opencard.core.event.EventGenerator;
import opencard.core.service.CardRequest;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.opt.security.CHVCardService;

import de.cardcontact.opencard.service.remoteclient.RemoteClient;
import de.cardcontact.opencard.utils.StreamingAPDUTracer;



/**
 * Daemon accepting requests from the local browser to initiate card update sessions with remote server
 * 
 * @author asc
 *
 */
public class CardUpdaterDaemon implements Runnable, CTListener {

	final static int SERVER_PORT = 27001;
	final static int closingDelay = 60;

	private CardUpdater cu;
	private CardTerminal ct;
	private ServerSocket server;
	private byte[] passedImage;
	private byte[] failedImage;
	private URLVerifier urlVerifier;
	private SmartCard card = null;
	private Timer timer = new Timer();
	private SmartCardCloser scc = null;



	public CardUpdaterDaemon(CardUpdater cu, CardTerminal ct) throws IOException {
		this.cu = cu;
		this.ct = ct;
		server = new ServerSocket(SERVER_PORT, 0, InetAddress.getByName(null));		// Bind to localhost
		loadImages();
		urlVerifier = new URLVerifier();
	}



	class SmartCardCloser extends TimerTask {
		CardUpdaterDaemon daemon;

		public SmartCardCloser(CardUpdaterDaemon daemon) {
			super();
			this.daemon = daemon;
		}
		
		
		
		@Override
		public void run() {
			if (this.daemon.card != null) {
				
				try	{
					this.daemon.card.close();
					this.daemon.log(1,"Smartcard closed");
				}
				catch(Exception e) {
					// Ignore
				}
				this.daemon.card = null;
			}
		}
	}
	
	
	
	public void log(int level, String msg) {
		this.cu.log(level, msg);
	}



	private byte[] loadImage(String name) throws IOException {
		InputStream is = CardUpdaterDaemon.class.getResourceAsStream(name);
		byte[] buffer = new byte[1024];
		int ofs = 0, len = buffer.length;
		int r;
		while((r = is.read(buffer, ofs, len)) > 0) {
			ofs += r;
			len -= r;
		}
		byte[] rb = new byte[ofs];
		System.arraycopy(buffer, 0, rb, 0, ofs);
		return rb;
	}



	void loadImages() throws IOException {
		passedImage = loadImage("passed.png");
		failedImage = loadImage("failed.png");
	}



	void serveResponse(Socket con, boolean passed) throws IOException {
		byte[] image = passed ? passedImage : failedImage;

		OutputStream os = con.getOutputStream();
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));

		out.write("HTTP/1.1 200 OK\r\n");
		out.write("Content-Length: " + image.length + "\r\n");
		out.write("\r\n");
		out.flush();

		os.write(image);
		os.close();
	}



	private void handleRequest(CardTerminal ct) {
		Socket con = null;
		boolean passed = false;

		try	{
			cu.log(1, "Daemon waiting on port " + SERVER_PORT + "...");
			con = server.accept();
			if (this.scc != null){
				this.scc.cancel();
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String methodAndUrl = in.readLine();
			if (methodAndUrl == null)		// Skip empty connects
				return;

			cu.log(2, methodAndUrl);

			String s;
			while (((s = in.readLine()) != null) && !s.equals("")) {
				cu.log(2, s);
			}

			int sofs = methodAndUrl.indexOf('?');
			if (methodAndUrl.startsWith("GET /") && (sofs > 0)) {
				int eofs = methodAndUrl.lastIndexOf(' ');
				String query = methodAndUrl.substring(sofs + 1, eofs);
				String[] params = query.split("&");
				String url = null;
				String sessionId = null;
				boolean pinRequired = false;
				int chvNumber = -1;

				for (String p : params) {
					String[] keyvalue = p.split("=");
					if (keyvalue[0].equals("url")) {
						url = keyvalue[1];
					} else if (keyvalue[0].equals("sessionId")) {
						sessionId = keyvalue[1];
					} else if (keyvalue[0].equals("pinrequired")) {
						pinRequired = true;
						chvNumber = Integer.parseInt(keyvalue[1]);
					}
				}

				if (url == null) {
					cu.log(1, "No URL defined in redirect");
					return;
				}

				boolean valid = verifyURL(url);
				if (!valid) {
					return;
				}

				try	{
					if (this.card == null) {
						CardRequest cr = new CardRequest(CardRequest.ANYCARD, ct, RemoteClient.class);
						cr.setTimeout(0);
						this.card = SmartCard.waitForCard(cr);
					}

					if (this.card == null) {
						cu.log(1, "No card in reader");
					} else {
						SmartCard sc = this.card;

						if (cu.getVerbosityLevel() > 1) {
							sc.setAPDUTracer(new StreamingAPDUTracer(System.out));
						}

						if (pinRequired) {
							CHVCardService chv = (CHVCardService) sc.getCardService(CHVCardService.class, true);

							boolean verified = chv.verifyPassword(null, chvNumber, null);
							cu.log(1, "PIN verified: " + verified);
						}

						cu.log(1, "Connecting to " + url);

						RemoteClient rc = (RemoteClient)sc.getCardService(RemoteClient.class, true);
						rc.update(url, sessionId, cu);
						this.scc = new SmartCardCloser(this);
						this.timer.schedule(this.scc, 1000 * closingDelay);
						passed = true;
					}
				}
				catch(Exception e) {
					cu.log(1, e.getMessage());
					e.printStackTrace();
					if (this.card!= null) {
						this.card.close();
						this.card = null;
					}
				}
			}
			serveResponse(con, passed);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			if (con != null) {
				try	{
					con.close();
				}
				catch(IOException e) {
					// Ignore
				}
			}
		}
	}



	private boolean verifyURL(String url) {
		if (urlVerifier == null) {
			return true;
		}
		cu.log(2, "Verify URL " + url);
		return urlVerifier.verifyURL(url);
	}



	@Override
	public void run() {
		EventGenerator.getGenerator().addCTListener(this);
		while(true) {
			handleRequest(ct);
		}
	}



	@Override
	public void cardInserted(CardTerminalEvent ctEvent)
			throws CardTerminalException {
		// Ignore
	}



	@Override
	public void cardRemoved(CardTerminalEvent ctEvent)
			throws CardTerminalException {
 
		if (this.card != null) {
			CardTerminal ct = this.card.getCardID().getCardTerminal();
			if ((ct != null) && (ctEvent.getCardTerminal().equals(ct))) {
				this.card = null;
				this.scc.cancel();
				this.log(1, "Card removed");
			}
		}
	}
}
