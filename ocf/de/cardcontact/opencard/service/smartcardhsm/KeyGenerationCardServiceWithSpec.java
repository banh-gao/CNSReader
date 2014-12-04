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




import de.cardcontact.tlv.TLVEncodingException;

import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;

import opencard.opt.signature.KeyGenerationCardService;

public interface KeyGenerationCardServiceWithSpec extends
KeyGenerationCardService {



	/**
	 * Initiate the generation of a fresh key pair for the selected key object.
	 * 
	 * Generating a new key pair requires a successful verification of the User PIN.
	 * @param newPrivateKey the ID for the key to be generated
	 * @param signingKey  the ID for signing authenticated request
	 * @param spec the KeySpec containing the domain parameter
	 */
	public byte[] generateKeyPair(byte newKeyId, byte signingId, SmartCardHSMPrivateKeySpec spec) throws CardTerminalException, CardServiceException, TLVEncodingException;
}
