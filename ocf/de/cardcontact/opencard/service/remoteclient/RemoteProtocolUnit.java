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

import opencard.core.terminal.CardID;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;

/**
 * Basic exchange unit for remote card terminal
 * 
 * @author asc
 *
 */
public class RemoteProtocolUnit {

	public enum Action { APDU, RESET, NOTIFY, CLOSE };

	private Action action;
	private int id;
	private String message;
	private Object payload;


	public RemoteProtocolUnit(Action action) {
		this.action = action;
	}



	public RemoteProtocolUnit(RemoteProtocolUnit.Action action, int id, String message) {
		this.id = id;
		this.message = message;
		this.action = action;
	}



	public RemoteProtocolUnit(CommandAPDU com) {
		this.action = RemoteProtocolUnit.Action.APDU;
		this.payload = com;
	}



	public RemoteProtocolUnit(ResponseAPDU res) {
		this.action = RemoteProtocolUnit.Action.APDU;
		this.payload = res;
	}



	public RemoteProtocolUnit(CardID cardID) {
		this.action = RemoteProtocolUnit.Action.RESET;
		this.payload = cardID;
	}



	public Object getPayload() {
		return this.payload;
	}



	public Action getAction() {
		return this.action;
	}



	public int getId() {
		return this.id;
	}
	
	
	
	public String getMessage() {
		return this.message;
	}



	public boolean isAPDU() {
		return this.action == Action.APDU;
	}
	
	
	
	public boolean isRESET() {
		return this.action == Action.RESET;
	}
	
	
	
	public boolean isNOTIFY() {
		return this.action == Action.NOTIFY;
	}
	
	
	
	public boolean isClosing() {
		return this.action == Action.CLOSE;
	}
}
