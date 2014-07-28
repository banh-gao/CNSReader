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
import de.cardcontact.opencard.service.globalplatform.SecurityDomainCardService;

public class GlobalPlatformCardServiceFactory extends CardServiceFactory {

    /* A tracer for debugging output. */
    private static Tracer ctracer = new Tracer(GlobalPlatformCardServiceFactory.class);

    private final Vector service_classes = new Vector();

    
    public GlobalPlatformCardServiceFactory() {
        super();
        service_classes.addElement(SecurityDomainCardService.class);
    }
    
    
    
    protected CardType getCardType(CardID cid, CardServiceScheduler scheduler) throws CardTerminalException {
        ctracer.info("[getCardType]", "card type is " + cid);
        return new CardType(0);
    }

    
    
    protected Enumeration getClasses(CardType type) {
        ctracer.info("[getClasses]", "card type is " + type.getType());
        return service_classes.elements();
    }

}
