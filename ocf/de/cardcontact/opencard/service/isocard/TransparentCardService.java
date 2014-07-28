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

package de.cardcontact.opencard.service.isocard;

import opencard.core.service.CardChannel;
import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.SlotChannel;
import opencard.core.util.APDUTracer;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.security.CredentialBag;
import opencard.opt.security.SecureService;
import opencard.opt.security.SecurityDomain;
import opencard.opt.util.PassThruCardService;
import de.cardcontact.opencard.security.IsoCredentialStore;
import de.cardcontact.opencard.security.SecureChannel;
import de.cardcontact.opencard.security.SecureChannelCredential;

/**
 * Transparent card service with secure messaging support
 * 
 * @author Andreas Schwier
 */
public class TransparentCardService extends PassThruCardService implements SecureService {
    private CredentialBag credentialBag;
    private SecurityDomain securityDomain;

    public TransparentCardService() {
        super();
        credentialBag = null;
    }
    
  
    
    /**
     * Provide collection of credentials for secure messaging transformation
     *  
     * @see opencard.opt.security.SecureService#provideCredentials(opencard.opt.security.SecurityDomain, opencard.opt.security.CredentialBag)
     */
    @Override
    public void provideCredentials(SecurityDomain domain, CredentialBag creds) throws CardServiceException {
    	if (domain == null) {
    		this.securityDomain = new CardFilePath(":3F00");
    	} else {
    		this.securityDomain = domain;
    	}
        this.credentialBag = creds;
    }
    


    /**
     * Send command APDU and receive response APDU, possibly wrapped by secure channel
     * 
     * The implementation will try to fetch a secure messaging credential from the bag allocated to the MF (3F00)
     * 
     * @param command Command APDU
     * @param usageQualifier Secure messaging transformation selector, a combination of SecureChannel.CPRO, .CENC, .RPRO and .RENC.
     * @return Response APDU
     * @throws CardTerminalException
     */
    public ResponseAPDU sendCommandAPDU(CommandAPDU command, int usageQualifier)
        throws CardTerminalException {

        SecureChannelCredential secureChannelCredential = null;
        ResponseAPDU response; 
            
        if (credentialBag != null) {
            IsoCredentialStore ics = (IsoCredentialStore)credentialBag.getCredentialStore(null, IsoCredentialStore.class);
        
            if (ics != null) {
                secureChannelCredential = ics.getSecureChannelCredential(this.securityDomain);
            }
        }
        
        try {
            allocateCardChannel();
            CardChannel channel = getCardChannel();

            if (secureChannelCredential != null) {
            	SlotChannel slc = channel.getSlotChannel();
            	APDUTracer tracer = slc.getAPDUTracer();
            	if ((tracer != null) && (command.getLength() > 5)) {
            		tracer.traceCommandAPDU(slc, command);
            	}

                SecureChannel secureChannel = secureChannelCredential.getSecureChannel();
                command = secureChannel.wrap(command, usageQualifier);
                response = channel.sendCommandAPDU(command);
                response = secureChannel.unwrap(response, usageQualifier);
            	if ((tracer != null) && (response.getLength() > 2)) {
            		tracer.traceResponseAPDU(slc, response);
            	}
            } else {
                response = channel.sendCommandAPDU(command);
            }
        } finally {
            releaseCardChannel();
        }

        return response;
    }        
}
