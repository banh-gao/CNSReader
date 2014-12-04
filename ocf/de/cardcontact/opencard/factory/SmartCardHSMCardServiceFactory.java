/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2006 CardContact Software & System Consulting
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
import de.cardcontact.opencard.service.smartcardhsm.SmartCardHSMCardService;

import opencard.core.service.CardServiceFactory;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.CardType;
import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.Tracer;



/**
 * Factory creating SmartCard-HSM card services
 * 
 * @author lew
 *
 */
public class SmartCardHSMCardServiceFactory extends CardServiceFactory {
	private final static byte[] SMARTCARDHSM =      { (byte)0x80, (byte)0x31, (byte)0x81, (byte)0x54, (byte)0x48, (byte)0x53, (byte)0x4D, (byte)0x31, (byte)0x73, (byte)0x80, (byte)0x21, (byte)0x40, (byte)0x81, (byte)0x07 };
	private final static byte[] GENERIC_JCOP = { (byte)0x4A, (byte)0x43, (byte)0x4F, (byte)0x50, (byte)0x76, (byte)0x32, (byte)0x34, (byte)0x31 };
	
	private final static byte[] SELECT_SC_HSM = {0x00, (byte) 0xA4, 0x04, 0x04, 0x0B, (byte) 0xE8, 0x2B, 0x06, 0x01, 0x04, 0x01, (byte) 0x81, (byte) 0xC3, 0x1F, 0x02, 0x01, 0x00};

	/* A tracer for debugging output. */
	private static Tracer ctracer = new Tracer(IsoCardServiceFactory.class);


	@Override
	protected CardType getCardType(CardID cid, CardServiceScheduler scheduler) throws CardTerminalException {

		Vector<Class<SmartCardHSMCardService>> serviceClasses = new Vector<Class<SmartCardHSMCardService>>();

		byte[] hb = cid.getHistoricals();
		if (hb == null) {
			return CardType.UNSUPPORTED;
		}
		int i = 0;

		if (partialMatch(hb, SMARTCARDHSM)) {
			i = IsoConstants.CARDTYPE_SC_HSM;
			serviceClasses.addElement(SmartCardHSMCardService.class);
		} else {
			if (partialMatch(hb, GENERIC_JCOP)) { // Generic JCOP - SmartCard-HSM present?
				
				CommandAPDU c = new CommandAPDU(SELECT_SC_HSM);
				ResponseAPDU r = scheduler.getSlotChannel().sendAPDU(c);
				
				if (r.sw() == 0x9000) {
					i = IsoConstants.CARDTYPE_SC_HSM;
					serviceClasses.addElement(SmartCardHSMCardService.class);
				} else {				
					return CardType.UNSUPPORTED; // No SmartCard-HSM on card
				}
					
			} else {
				return CardType.UNSUPPORTED; // no JCOP
			}
		}

		CardType cardType = new CardType(i);
		cardType.setInfo(serviceClasses);
		return cardType;
	}



	protected Enumeration getClasses(CardType type) {
		ctracer.info("[getClasses]", "card type is " + type.getType());
		Vector serviceClasses = (Vector)type.getInfo();
		return serviceClasses.elements();
	}



	private static boolean partialMatch(byte[] hb, byte[] ref) {
		int i = 0;

		if (hb.length < ref.length)
			return false;

		for (; i < ref.length && hb[i] == ref[i]; i++);

		return i == ref.length;
	}
}
