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

/**
 * Class to encode a secure channel credential, which combines a secure channel object
 * with a usage qualifier.
 * 
 * Secure channel credentials can be added to an IsoCredentialStore, linking them with a 
 * a file object on the card and a file access mode.
 * 
 * @author Andreas Schwier (info@cardcontact.de)
 */
public class IsoSecureChannelCredential implements SecureChannelCredential {
    protected SecureChannel sc;
    protected int usageQualifier;
    
    
    /**
     * Create a secure channel credential object
     * 
     * @param usageQualifier    Usage qualifier for use of secure channel. Must be a bitmap combination
     *                          of SecureChannel.CPRO, SecureChannel.CENC, SecureChannel.RPRO and SecureChannel.RENC.
     * @param sc                Secure channel object
     */
    public IsoSecureChannelCredential(int usageQualifier, SecureChannel sc) {
        this.usageQualifier = usageQualifier;
        this.sc = sc;
    }
    
    

    /**
     * Getter for secure channel object
     * 
     * @return secure channel object
     */
    public SecureChannel getSecureChannel() {
        return this.sc;
    }

    
    
    /**
     * Getter for usageQualifier
     * 
     * @return usage qualifier
     */
    public int getUsageQualifier() {
        return this.usageQualifier;
    }
}
