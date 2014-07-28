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

package de.cardcontact.opencard.service.remoteclient;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import de.cardcontact.tlv.TLVDataSizeException;
import de.cardcontact.tlv.TLVEncodingException;
import de.cardcontact.tlv.TagSizeException;

import opencard.core.OpenCardException;
import opencard.core.service.CardChannel;
import opencard.core.service.CardService;
import opencard.core.service.CardServiceException;
import opencard.core.service.InvalidCardChannelException;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;




/**
 * This client establish a connection to a remote administration server
 * and obtains command APDUs that will be transfered to the card.
 * The card's response will be send back to the server.
 * 
 * The APDUs are encoded according to RAMoverHTTP
 * 
 * @author lew
 */
public class RemoteClient extends CardService implements RemoteUpdateService {

	private HttpURLConnection connection;
	private String session = null;
	private String contentType = "application/org.openscdp-content-mgt-response;version=1.0";


	/**
	 * Connect to the remote administration server
	 * 
	 * @param serverURL The url of the remote administration server
	 * @return the command scripting template
	 * @throws IOException 
	 * @throws TLVEncodingException 
	 */
	private byte[] initialConnect(String serverURL) throws IOException, TLVEncodingException {
		URL url = new URL(serverURL);
		connection = (HttpURLConnection) url.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", contentType);

		// Use the given sessionid if available
		if (session != null) {
			connection.addRequestProperty("Cookie", session);
		}
		
		RemoteProtocolEncoder rpe = new RemoteProtocolEncoder();
		rpe.add(new RemoteProtocolUnit(getCard().getCardID()));
		DataOutputStream writer = new DataOutputStream(connection.getOutputStream());

		writer.write(rpe.encodeInitiationTemplate());
		writer.close();

		// The session returned from the server may differ from the given one
		String cookie = connection.getHeaderField("Set-Cookie");
		if (cookie != null) {
			session = cookie.split(";")[0];
		}

		InputStream reader = connection.getInputStream();
		DataInputStream in = new DataInputStream(reader);
		if (in.available() == 0) {
			throw new CardServiceException("No data received from server");
		}
		byte[] data = new byte[in.available()];
		in.readFully(data);
		
		return data;
	}



	/**
	 * Send the Response Scripting Template to the server
	 * and obtain the next Command Scripting Template from the server
	 * if available.
	 * 
	 * @param serverURL The url of the remote administration server
	 * @param rst The response APDU that will be send to the server
	 * @return The new command APDU or null if it's not exists.
	 * @throws IOException
	 * @throws TLVEncodingException
	 * @throws TagSizeException
	 * @throws TLVDataSizeException
	 */
	private byte[] processNext(String serverURL, byte[] rst) throws IOException, TLVEncodingException, TagSizeException, TLVDataSizeException {
		URL url = new URL(serverURL);	

		connection = (HttpURLConnection) url.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.addRequestProperty("Cookie", session);
		connection.setRequestProperty("Content-Type", contentType);

		DataOutputStream writer = new DataOutputStream(connection.getOutputStream());

		writer.write(rst);
		writer.close();

		InputStream reader = connection.getInputStream();
		DataInputStream in = new DataInputStream(reader);
		if (in.available() > 0) {
			byte[] data = new byte[in.available()];
			in.readFully(data);
			return data;
		}

		return null;
	}



	private RemoteProtocolUnit sendCommandAPDU(RemoteProtocolUnit rpu) throws InvalidCardChannelException, CardTerminalException{
		ResponseAPDU res;
		CardChannel channel;

		CommandAPDU capdu = (CommandAPDU)rpu.getPayload();
		try {
			allocateCardChannel();
			channel = getCardChannel();
			res = channel.sendCommandAPDU(capdu);
		} finally {
			releaseCardChannel();
		}

		return new RemoteProtocolUnit(res);
	}



	private RemoteProtocolUnit resetCard(RemoteProtocolUnit rpu) throws CardTerminalException {
		SmartCard sc = getCard();
		CardID cid = sc.reset(false);
		
		if (cid == null) {
			throw new CardTerminalException("Could not reset card");
		}
		return new RemoteProtocolUnit(cid);
	}



	private byte[] process(byte[] cst, RemoteNotificationListener notificationListener) throws TLVEncodingException {
		RemoteProtocolEncoder rpe = new RemoteProtocolEncoder();

		rpe.decodeCommandScriptingTemplate(cst);
		List<RemoteProtocolUnit> rpus = rpe.getRemoteProtocolUnits();

		rpe = new RemoteProtocolEncoder();

		try	{
			for (RemoteProtocolUnit rpu : rpus) {
				switch(rpu.getAction()) {
				case APDU:
					rpe.add(sendCommandAPDU(rpu));
					break;
				case RESET:
					rpe.add(resetCard(rpu));
					break;
				case NOTIFY:
					if (notificationListener != null) {
						notificationListener.remoteNotify(rpu.getId(), rpu.getMessage());
					}
					break;
				}
			}
		} catch(OpenCardException cte) {
			rpe.add(new RemoteProtocolUnit(RemoteProtocolUnit.Action.CLOSE, -1, cte.getMessage()));
		}

		return rpe.encodeResponseScriptingTemplate();
	}



	@Override
	public void update(String serverURL, String sessionId, RemoteNotificationListener notificationListener) throws CardServiceException {
		
		if (sessionId != null) {
			session = "JSESSIONID=" + sessionId;
		}
		try {
			byte[] cst = initialConnect(serverURL);
			int code = connection.getResponseCode();
			while( code == 200) {
				byte[] rst = process(cst, notificationListener);
				cst = processNext(serverURL, rst);
				code = connection.getResponseCode();
			}
		} catch (CardServiceException e) {
			throw e;
		} catch (FileNotFoundException e) {
			throw new CardServiceException("URL " + serverURL + " not found");
		} catch (IOException e) {
			throw new CardServiceException("IO error during connection to " + serverURL + "(" + e.getMessage() + ")");
		} catch (TLVEncodingException e) {
			throw new CardServiceException(e.getMessage());
		} catch (TagSizeException e) {
			throw new CardServiceException(e.getMessage());
		} catch (TLVDataSizeException e) {
			throw new CardServiceException(e.getMessage());
		}
	}
}
