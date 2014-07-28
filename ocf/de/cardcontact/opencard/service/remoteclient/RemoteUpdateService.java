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

import opencard.core.service.CardServiceException;


public interface RemoteUpdateService {

	/**
	 * Update the card by obtaining command APDUs 
	 * from a remote administration server.
	 *  
	 * @param url the url of the remote administration server
	 * @param sessionId the session Id to be included as JSESSION cookie or null
	 * @param notificationListener the listener receiving notifications from the server or null
	 * @throws CardServiceException 
	 */
	public void update(String url, String sessionId, RemoteNotificationListener notificationListener) throws CardServiceException;
}
