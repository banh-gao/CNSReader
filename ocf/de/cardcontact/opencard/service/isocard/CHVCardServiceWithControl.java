/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2007 CardContact Software & System Consulting
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
import opencard.core.terminal.CHVControl;
import opencard.core.terminal.CardTerminalException;
import opencard.opt.security.CHVCardService;
import opencard.opt.security.SecurityDomain;

/**
 * Extend CHVCardService to allow an application to pass an
 * CHVControl object
 * 
 * @author Andreas Schwier (www.cardcontact.de)
 */
public interface CHVCardServiceWithControl extends CHVCardService {
	
	public enum PasswordStatus {
		VERIFIED,
		NOTVERIFIED,
		BLOCKED,
		LASTTRY,
		NOTINITIALIZED,
		RETRYCOUNTERLOW
	};
	
    /**
     * Checks a password for card holder verification.
     * Note that repeated verification of a wrong password will typically
     * block that password on the smartcard.
     *
     * @param domain      The security domain in which to verify the password.
     *                    <tt>null</tt> can be passed to refer to the root
     *                    domain on the smartcard.
     *                    <br>
     *                    For file system based smartcards, the security
     *                    domain is specified as a <tt>CardFilePath</tt>.
     *                    The root domain then corresponds to the master file.
     * @param number      The number of the password to verify. This parameter
     *                    is used to distinguish between different passwords
     *                    in the same security domain.
     * @param cc          Control parameter defined by the application
     * @param password    The password data that has to be verified.
     *                    If the data is supplied, it has to be padded to the
     *                    length returned by <tt>getPasswordLength</tt> for
     *                    that password.
     *                    <br>
     *                    <tt>null</tt> may be passed to indicate that this
     *                    service should use a protected PIN path facility,
     *                    if available. Alternatively, this service may query
     *                    the password by some other, implementation-dependend
     *                    means. In any case, the service implementation will
     *                    require knowledge about the encoding of the password
     *                    data on the smartcard.
     *
     * @exception CardServiceException
     *            if this service encountered an error.
     *            In this context, it is not considered an error if the password
     *            to be verified is wrong. However, if the password is blocked
     *            on the smartcard, an exception will be thrown.
     * @exception CardTerminalException
     *            if the underlying card terminal encountered an error
     *            when communicating with the smartcard
     */
    public boolean verifyPassword(SecurityDomain domain, int number, CHVControl cc, 
                                  byte[] password)
         throws CardServiceException, CardTerminalException
    ;

    /**
     * Get the smartcard's password status.
     * 
     * @param domain      The security domain in which to verify the password.
     *                    <tt>null</tt> can be passed to refer to the root
     *                    domain on the smartcard.
     *                    <br>
     *                    For file system based smartcards, the security
     *                    domain is specified as a <tt>CardFilePath</tt>.
     *                    The root domain then corresponds to the master file.
     * @param number      The number of the password to verify. This parameter
     *                    is used to distinguish between different passwords
     *                    in the same security domain.
     * @return The password status
     * 
     * @throws CardServiceException
     *         if this service encountered an error.
     * @throws CardTerminalException
     *         if the underlying card terminal encountered an error
     *         when communicating with the smartcard
     */
    public PasswordStatus getPasswordStatus(SecurityDomain domain, int number) throws CardServiceException, CardTerminalException;
}
