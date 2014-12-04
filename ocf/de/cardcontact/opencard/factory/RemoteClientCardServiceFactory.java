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

package de.cardcontact.opencard.factory;

import java.util.Enumeration;
import java.util.Vector;

import de.cardcontact.opencard.service.isocard.IsoConstants;
import de.cardcontact.opencard.service.remoteclient.RemoteClient;
import de.cardcontact.opencard.service.smartcardhsm.SmartCardHSMCardService;

import opencard.core.service.CardService;
import opencard.core.service.CardServiceFactory;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.CardType;
import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminalException;
import opencard.core.util.Tracer;


/**
 * Factory creating RemoteClient card services
 * 
 * @author lew
 *
 */
public class RemoteClientCardServiceFactory extends CardServiceFactory {
	
	
	
	private static Tracer ctracer = new Tracer(RemoteClientCardServiceFactory.class);
	
	
	
	@Override
	protected CardType getCardType(CardID cid, CardServiceScheduler scheduler)
			throws CardTerminalException {
		Vector<Class<? extends CardService>> serviceClasses = new Vector<Class<? extends CardService>>();
		serviceClasses.addElement(RemoteClient.class);
		
		CardType type = new CardType(IsoConstants.CARDTYPE_SC_HSM);
		type.setInfo(serviceClasses);
		return type;
	}

	
	
	@Override
	protected Enumeration getClasses(CardType type) {
		ctracer.info("[getClasses]", "card type is " + type.getType());
		Vector<Class<? extends CardService>> serviceClasses = (Vector) type.getInfo();
		return serviceClasses.elements();
	}
}
