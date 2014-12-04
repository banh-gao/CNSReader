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

import opencard.core.terminal.CardID;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.security.Credential;
import opencard.opt.security.CredentialStore;
import opencard.opt.security.SecurityDomain;

/**
 * Class implementing a credential store for secure channel credentials
 *
 * Secure channel credentials are stored with the file path and access mode as index
 * 
 * @author Andreas Schwier (info@cardcontact.de)
 */

public class IsoCredentialStore extends CredentialStore {

    public final static int SELECT = 1;
    public final static int READ = 2;
    public final static int UPDATE = 4;
    public final static int APPEND = 8;
    public final static int CREATE = 16;
    public final static int DELETE = 32;
    public final static int ACTIVATE = 64;
    public final static int DEACTIVATE = 128;
    public final static int SIZE_ACCESS_MATRIX = 8;
    

    /**
     * Generic store that supports any card
     */
    public boolean supports(CardID cardID) {
        return true;
    }
    


    /**
     * Set secure channel credential for a security domain
     * 
     * @param sd            Security domain (usually a CardFilePath object)
     * @param scc           Secure channel credential for this domain
     */
    public void setSecureChannelCredential(SecurityDomain sd, SecureChannelCredential scc) {
        storeCredential(sd, scc);
    }
    

    
    /**
     * Return the credentials defined for a specific security domain
     *
     * @param sd            Security domain (usually a CardFilePath object)
     * @return              Secure channel credential for this domain
     */
    public SecureChannelCredential getSecureChannelCredential(SecurityDomain sd) {
        Credential c = fetchCredential(sd);
        
        if (c instanceof SecureChannelCredential) {
            SecureChannelCredential scc = (SecureChannelCredential)c;
            return scc;
        }
        return null;
    }
    
    

    /**
     * Get a secure channel credential for a specified security domain and access mode
     * 
     * @param sd            Security domain (usually a CardFilePath object)
     * @param accessMode    Access mode, one of SELECT, READ, UPDATE, APPEND
     * @return              Secure channel credential or null if none defined
     */
    public SecureChannelCredential getSecureChannelCredential(SecurityDomain sd, int accessMode) {
        CardFilePath path = new CardFilePath((CardFilePath)sd);
        
        do  {
            Credential c = fetchCredential(path.toString() + "§" + accessMode);
            
            if (c == null) {
                c = fetchCredential(path.toString());
            }
            
            if ((c != null) && (c instanceof SecureChannelCredential)) {
                SecureChannelCredential scc = (SecureChannelCredential)c;
                return scc;
            }
        } while(path.chompTail());
        
        CardFilePath rootPath = new CardFilePath(":3F00");
        
        if (!path.equals(rootPath)) {
        	// Check if there is a credential defined for the MF
            Credential c = fetchCredential(rootPath.toString() + "§" + accessMode);
            
            if (c == null) {
                c = fetchCredential(rootPath.toString());
            }
            
            if ((c != null) && (c instanceof SecureChannelCredential)) {
                SecureChannelCredential scc = (SecureChannelCredential)c;
                return scc;
            }
        }
        
        return null;
    }
    
    

    /**
     * Set a secure channel credential for a specified security domain and access mode
     * 
     * @param sd            Security domain (usually a CardFilePath object)
     * @param accessMode    Access mode, one of SELECT, READ, UPDATE, APPEND
     * @param scc           Secure channel credential
     */
    public void setSecureChannelCredential(SecurityDomain sd, int accessMode, SecureChannelCredential scc) {
        for (int i = 0; i < SIZE_ACCESS_MATRIX; i++ ) {
            if ((accessMode & (1 << i)) > 0) {
                storeCredential(sd.toString() + "§" + (1 << i), scc);
            }
        }
    }
}
