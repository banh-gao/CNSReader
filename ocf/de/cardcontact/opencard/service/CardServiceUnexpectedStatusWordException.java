/*
 *  ---------
 * |.##> <##.|  CardContact Software & System Consulting
 * |#       #|  32429 Minden, Germany (www.cardcontact.de)
 * |#       #|  Copyright (c) 1999-2005. All rights reserved
 * |'##> <##'|  See file COPYING for details on licensing
 *  --------- 
 *
 */

package de.cardcontact.opencard.service;

import opencard.core.service.CardServiceException;
import opencard.core.util.HexString;

/**
 * 
 * @author Andreas Schwier (info@cardcontact.de)
 */
public class CardServiceUnexpectedStatusWordException extends CardServiceException {
    protected int sw = 0;

    /**
     * Create exception and store last SW1/SW2
     *
     * @param msg
     * @param sw
     */
    public CardServiceUnexpectedStatusWordException(String msg, int sw) {
        super(msg);
        this.sw = sw;
    }
    

    
    /**
     * Get failed SW1/SW2
     * @return SW1/SW2
     */
    public int getSW() {
        return sw;
    }
    

    
    public String getMessage() {
        String msg = super.getMessage() + " failed with SW1/SW2 = " + HexString.hexifyShort(sw);
        String swmsg = StatusWordTable.StringForSW(sw);
        
        if (swmsg != null) {
            return msg + " \"" + swmsg + "\""; 
        } else {
            return msg;
        }
    }
}
