/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2012 CardContact Software & System Consulting
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

public class GPSCP02SecureChannelCredential implements SecureChannelCredential {

	private int usageQualifier;
	private GPSCP02SecureChannel sc;

	/**
     * Create a secure channel credential object
     * 
     * @param usageQualifier    Usage qualifier for use of secure channel. Must be a bitmap combination
     *                          of SecureChannel.CPRO, SecureChannel.CENC, SecureChannel.RPRO.
     * @param sc                Secure channel object
	 * @return 
     */
    public GPSCP02SecureChannelCredential(int usageQualifier, GPSCP02SecureChannel sc) {
        this.usageQualifier = usageQualifier;
        this.sc = sc;
    }
    
    
	public SecureChannel getSecureChannel() {
		return (SecureChannel) this.sc;
	}

	public int getUsageQualifier() {
		return this.usageQualifier;
	}

}
