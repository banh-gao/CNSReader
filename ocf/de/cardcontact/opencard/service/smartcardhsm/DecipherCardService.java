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

import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;

public interface DecipherCardService {



	/**
	 * The device decrypts a cryptogram and returns the plain text.
	 * 
	 * @param privateKey the private SmartCardHSMKey
	 * @param cryptogram 
	 * @return the plain text
	 * @throws CardServiceException
	 * @throws CardTerminalException
	 */
	public byte[] decipher(SmartCardHSMKey privateKey, byte[] cryptogram)
	throws  CardTerminalException, CardServiceException;



	/**
	 * The device calculates a shared secret point using an EC Diffie-Hellman
	 * operation. The public key of the sender must be provided as input to the command.
	 * The device returns the resulting point on the curve associated with the private key.
	 * 
	 * @param privateKey Key identifier of the SmartCardHSM private key
	 * @param pkComponents Concatenation of '04' || x || y point coordinates of ECC public Key
	 * @return Concatenation of '04' || x || y point coordinates on EC curve
	 * @throws CardServiceException
	 * @throws CardTerminalException
	 */
	public byte[] performECCDH(SmartCardHSMKey privateKey, byte[] pkComponents)
	throws  CardTerminalException, CardServiceException;
}
