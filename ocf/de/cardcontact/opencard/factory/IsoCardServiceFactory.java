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

import opencard.core.service.CardServiceFactory;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.CardType;
import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminalException;
import opencard.core.util.Tracer;
import de.cardcontact.opencard.service.acos.ACOSCardService;
import de.cardcontact.opencard.service.isocard.IsoCardService;
import de.cardcontact.opencard.service.isocard.IsoConstants;
import de.cardcontact.opencard.service.isocard.TransparentCardService;
import de.cardcontact.opencard.service.signature.ISSSSignatureService;



/**
 * Factory used to create an card service for ISO 7816-4 / -8 / -9 compliant cards
 * 
 * @author  Andreas Schwier
 */
public class IsoCardServiceFactory extends CardServiceFactory  {
	private final static byte[] Micardo20 = 			{ (byte)0x00,(byte)0x00,(byte)0x68,(byte)0xD2,(byte)0x76,(byte)0x00,(byte)0x00,(byte)0x28,(byte)0xFF,(byte)0x05,(byte)0x1E };
	private final static byte[] Micardo21 = 			{ (byte)0x00,(byte)0x00,(byte)0x68,(byte)0xD2,(byte)0x76,(byte)0x00,(byte)0x00,(byte)0x28,(byte)0xFF,(byte)0x05,(byte)0x24 };
	private final static byte[] Micardo23 = 			{ (byte)0x00,(byte)0x00,(byte)0x68,(byte)0xD2,(byte)0x76,(byte)0x00,(byte)0x00,(byte)0x28,(byte)0xFF,(byte)0x05,(byte)0x23 };
	private final static byte[] Starcos30 = 			{ (byte)0x80,(byte)0x67,(byte)0x04,(byte)0x12,(byte)0xB0,(byte)0x02,(byte)0x01,(byte)0x82,(byte)0x01 };
	private final static byte[] Starcos30_P5CC036 = 	{ (byte)0x80,(byte)0x67,(byte)0x04,(byte)0x14,(byte)0xB0,(byte)0x01,(byte)0x01,(byte)0x82,(byte)0x01 };
	private final static byte[] JCOP41CL1 =				{ (byte)0x41,(byte)0x20,(byte)0x00,(byte)0x11,(byte)0x33,(byte)0xB0,(byte)0x4A,(byte)0x43,(byte)0x4F,(byte)0x50,(byte)0x34,(byte)0x31,(byte)0x56,(byte)0x32 };
	private final static byte[] JCOP41CL2 =				{ (byte)0x41,(byte)0x28,(byte)0x00,(byte)0x11,(byte)0x33,(byte)0xB0,(byte)0x4A,(byte)0x43,(byte)0x4F,(byte)0x50,(byte)0x34,(byte)0x31,(byte)0x56,(byte)0x32 };
	private final static byte[] JCOP41 =				{ (byte)0x4A,(byte)0x43,(byte)0x4F,(byte)0x50,(byte)0x34,(byte)0x31,(byte)0x56,(byte)0x32,(byte)0x32 };
	private final static byte[] TCOSICAOPHICL =			{ (byte)0x41,(byte)0x20,(byte)0x00,(byte)0x41,(byte)0x22,(byte)0xE1,(byte)0x02,(byte)0x00,(byte)0x64,(byte)0x04,(byte)0x00,(byte)0x03,(byte)0x00,(byte)0x31 };
	private final static byte[] TCOSICAOIFXCL =         { (byte)0x42,(byte)0x00,(byte)0x01,(byte)0x33,(byte)0xE1 };
	private final static byte[] EC = 					{ (byte)0x65,(byte)0x63 };
	private final static byte[] ACOS =                  { (byte)0x3B,(byte)0xBF,(byte)0x18,(byte)0x00,(byte)0x81,(byte)0x31,(byte)0xFE,(byte)0x45 };

	/* A tracer for debugging output. */
	private static Tracer ctracer = new Tracer(IsoCardServiceFactory.class);



	/** Creates new IsoCardServiceFactory */
	public IsoCardServiceFactory() 
	{
		super();
	}



	protected CardType getCardType(CardID cid, CardServiceScheduler scheduler)
	throws CardTerminalException
	{
		byte[] hb;
		int i;

		Vector serviceClasses = new Vector();
		serviceClasses.addElement(TransparentCardService.class);

		hb = cid.getHistoricals();

		i = 0;
		if (hb != null) {
			if ((hb[0] != 0x00) && (hb[0] != (byte)0x80)) { 
				ctracer.info("[IsoCardServiceFactory.getCardType]", "Historical bytes do not indicate an ISO card");
				if (partialMatch(hb, JCOP41CL1)) {
					i = IsoConstants.CARDTYPE_JCOP41;
					serviceClasses.addElement(IsoCardService.class);
				} else if (partialMatch(hb, JCOP41CL2)) {
					i = IsoConstants.CARDTYPE_JCOP41;
					serviceClasses.addElement(IsoCardService.class);
				} else if (partialMatch(hb, JCOP41)) {
					i = IsoConstants.CARDTYPE_JCOP41;
					serviceClasses.addElement(IsoCardService.class);
				} else if (partialMatch(hb, TCOSICAOPHICL)) {
					i = IsoConstants.CARDTYPE_TCOSICAO30;
					serviceClasses.addElement(IsoCardService.class);
				} else if (partialMatch(hb, TCOSICAOIFXCL)) {
					i = IsoConstants.CARDTYPE_TCOSICAO30;
					serviceClasses.addElement(IsoCardService.class);
				} else if (partialMatch(hb, EC)) {
					i = IsoConstants.CARDTYPE_EC;
					serviceClasses.addElement(IsoCardService.class);
				} else if (partialMatch(cid.getATR(), ACOS)) {
					i = IsoConstants.CARDTYPE_ACOS;
					serviceClasses.addElement(ACOSCardService.class);
				} else {
					serviceClasses.addElement(IsoCardService.class);
				}
			} else {
				serviceClasses.addElement(IsoCardService.class);
				if (partialMatch(hb, Micardo20)) {
					i = IsoConstants.CARDTYPE_MICARDO20;
				} else if (partialMatch(hb, Micardo21)) {
					i = IsoConstants.CARDTYPE_MICARDO21;
				} else if (partialMatch(hb, Micardo23)) {
					i = IsoConstants.CARDTYPE_MICARDO23;
					serviceClasses.addElement(ISSSSignatureService.class);
				} else if (partialMatch(hb, Starcos30)) {
					i = IsoConstants.CARDTYPE_STARCOS30;
				} else if (partialMatch(hb, Starcos30_P5CC036)) {
					i = IsoConstants.CARDTYPE_STARCOS30;
				}
			}
		} else {
			serviceClasses.addElement(IsoCardService.class);
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
