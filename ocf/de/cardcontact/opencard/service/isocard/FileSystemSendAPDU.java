/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2010 CardContact Software & System Consulting
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

package de.cardcontact.opencard.service.isocard;

import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.opt.iso.fs.CardFilePath;



/**
 * Interface implemented by CardServices supporting direct APDUs to file system objects
 * 
 * @author asc
 */
public interface FileSystemSendAPDU {

	/**
	 * Send APDU making sure that the object referenced by path is selected
	 * 
	 * @param path the DF which should be the active DF for this APDU
	 * @param com the command APDU
	 * @param usageQualifier a combination of SecureChannel.CPRO / CENC / RPRO / RENC to control the transformation of the APDU
	 *                       for secure messaging. Use 0 for plain transmission.
	 * @return Response APDU the response from the card
	 * @throws CardServiceException
	 * @throws CardTerminalException
	 */
	public ResponseAPDU sendCommandAPDU(CardFilePath path, CommandAPDU com, int usageQualifier)	throws CardServiceException, CardTerminalException;
}
