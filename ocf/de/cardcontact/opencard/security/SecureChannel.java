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

package de.cardcontact.opencard.security;

import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;

/**
 * Interface implementing an APDU wrapping / unwrapping mechanism
 * 
 * @author Andreas Schwier (info@cardcontact.de)
 */
public interface SecureChannel {

    /**
     * Usage qualifier: Command APDU is MAC protected
     */
    public final static int CPRO = 1;   
    
    /**
     * Usage qualifier: Command APDU is encrypted
     */
    public final static int CENC = 2;
    
    /**
     * Usage qualifier: Response APDU is MAC protected
     */
    public final static int RPRO = 4;
    
    /**
     * Usage qualifier: Response APDU is encrypted
     */
    public final static int RENC = 8;
    
    /**
     * Combination of CPRO, CENC, RPRO, RENC
     */
    public final static int ALL = 0xFFFF;

    /**
     * Wrap a CommandAPDU
     * 
     * @param apduToWrap        Command APDU to be wrapped
     * @param usageQualifier    Qualifier to control the transformation process
     * @return                  Wrapped APDU
     */
    public CommandAPDU wrap(CommandAPDU apduToWrap, int usageQualifier);
    
    /**
     * Unwrap a CommandAPDU
     * 
     * @param apduToUnwrap      Response APDU to be unwrapped
     * @param usageQualifier    Qualifier to control the transformation process
     * @return                  Wrapped APDU
     */
    public ResponseAPDU unwrap(ResponseAPDU apduToUnwrap, int usageQualifier);
}
