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
import de.cardcontact.opencard.service.gemxcos.GemXCosCardService;
import de.cardcontact.opencard.service.isocard.IsoConstants;

/**
 * @author  Frank Thater
 */

public class GemXCosCardServiceFactory extends CardServiceFactory {

	private final static byte[] GemXCosHIC = { 0x3B, 0x2B, 0x00, 0x00, 0x64, 0x05, 0x3E, 0x02, 0x0F, 0x31, (byte)0x80, 0x0E, (byte)0x90, 0x00 };
	private final static byte[] GemXCosHPC = { 0x3B, 0x2B, 0x00, 0x00, 0x64, 0x05, 0x3E, 0x02, (byte)0xF0, 0x31, (byte)0x80, 0x0E, (byte)0x90, 0x00 };

    private final Vector service_classes = new Vector();

    /* A tracer for debugging output. */
    private static Tracer ctracer = new Tracer(GemXCosCardServiceFactory.class);

    /**
     * Creates new GemXCosCardServiceFactory 
     */
	public GemXCosCardServiceFactory() {
		super();
	    service_classes.addElement(GemXCosCardService.class);
	}

	
	
	/* (non-Javadoc)
	 * @see opencard.core.service.CardServiceFactory#getCardType(opencard.core.terminal.CardID, opencard.core.service.CardServiceScheduler)
	 */
	protected CardType getCardType(CardID cid, CardServiceScheduler scheduler) throws CardTerminalException {

		byte[] atr = cid.getATR();
		
		if (match(atr, GemXCosHIC) || match(atr, GemXCosHPC)) {
			return new CardType(IsoConstants.CARDTYPE_GEMXCOS);
		} else {
			return CardType.UNSUPPORTED;
		}
	}

	
	
	/* (non-Javadoc)
	 * @see opencard.core.service.CardServiceFactory#getClasses(opencard.core.service.CardType)
	 */
	protected Enumeration getClasses(CardType type) {
		ctracer.info("[getClasses]", "card type is " + type.getType());
	    return service_classes.elements();
	}
	
	
	
	private static boolean match(byte[] atr, byte[] ref) {
		int i = 0;
		
		if (atr.length != ref.length)
			return false;
		
		for (; i < ref.length && atr[i] == ref[i]; i++);
		
		return i == ref.length;
	}
}
