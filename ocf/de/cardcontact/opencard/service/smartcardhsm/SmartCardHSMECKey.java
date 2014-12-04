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

package de.cardcontact.opencard.service.smartcardhsm;



/**
 * Reference to a EC private key on the SmartCardHSM
 * 
 * @author lew
 * 
 * @see opencard.opt.security.PrivateKeyRef
 *
 */
public class SmartCardHSMECKey extends SmartCardHSMKey {


	/**
	 * 
	 */
	private static final long serialVersionUID = -3278183569421768636L;


	/**
	 * 
	 * @param keyID The ID which refers to the private key on the card.
	 * @param name
	 * @param keySize
	 */
	public SmartCardHSMECKey(byte keyID, String name, short keySize) {
		super(keyID, name, keySize);
	}
	
	@Override
	public String getAlgorithm() {
		return "EC";
	}
}
