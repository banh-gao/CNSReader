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

package de.cardcontact.opencard.service.globalplatform;

import opencard.core.service.CardChannel;
import opencard.core.service.CardService;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.InvalidCardChannelException;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.SlotChannel;
import opencard.core.util.APDUTracer;
import opencard.core.util.Tracer;
import opencard.opt.applet.AppletID;
import opencard.opt.security.CredentialBag;
import opencard.opt.security.SecureService;
import opencard.opt.security.SecurityDomain;
import de.cardcontact.opencard.security.GPSCP02SecureChannel;
import de.cardcontact.opencard.security.IsoCredentialStore;
import de.cardcontact.opencard.security.SecureChannel;
import de.cardcontact.opencard.security.SecureChannelCredential;
import de.cardcontact.opencard.service.isocard.IsoConstants;
import de.cardcontact.opencard.utils.CapFile;
import de.cardcontact.tlv.TLV;
import de.cardcontact.tlv.TLVEncodingException;

/**
 * Class implementing a Global Platform Security Domain card service
 * 
 * @author Andreas Schwier (info@cardcontact.de)
 * @author Frank Thater (info@cardcontact.de)
 */
public class SecurityDomainCardService extends CardService implements SecureService {

    private final static Tracer ctracer = new Tracer(SecurityDomainCardService.class);
	
    public static final AppletID ISD_AID = new AppletID("A000000003000000");
    private AppletID aid = ISD_AID;
    private CredentialBag credentialBag = null;
	private byte level = GPSCP02SecureChannel.NONE;
	
    
    /**
     * Create the IsoCardState object in the card channel if it
     * does not yet exist.
     * 
     * Overwrites #opencard.core.service.CardService#initialize
     */
    protected void initialize(CardServiceScheduler scheduler,
                              SmartCard smartcard,
                              boolean blocking)
                       throws CardServiceException {
    
        super.initialize(scheduler, smartcard, blocking);

        ctracer.debug("initialize", "called");
    }
    
    
    
    /**
     * Select applet using SELECT command and application identifier passed in id
     * 
     * If the argument id is set to null, then the issuer security domain is selected
     * 
     * @param id Applet Id (AID) or null
     * @param next True to select next matching AID
     * @return Response for SELECT APDU
     * 
     * @throws CardTerminalException
     */
    public ResponseAPDU select(AppletID id, boolean next) throws CardTerminalException {
        CardChannel channel;
        CommandAPDU com = new CommandAPDU(64);
        ResponseAPDU res = new ResponseAPDU(258);

        try {
            allocateCardChannel();
            channel = getCardChannel();
            
            com.setLength(0);
            com.append(IsoConstants.CLA_ISO);
            com.append(IsoConstants.INS_SELECT_FILE);
            com.append(IsoConstants.SC_AID);
            com.append((byte)(next ? IsoConstants.RC_NEXT : 0));
            
            if (id != null) {
                byte[] aid = id.getBytes();
                com.append((byte)aid.length);
                com.append(aid);
                this.aid = id;
            } else {
            	this.aid = ISD_AID;
            }
            
            com.append((byte)0x00);
            
            res = channel.sendCommandAPDU(com);

        } finally {
            releaseCardChannel();
        }

        return res;
    }


    
    /**
     * Issue INSTALL command with INSTALL FOR INSTALL and SELECTABLE option to security domain
     * 
     * @param loadFileAID AID for load file in card
     * @param execModAID AID for module contained in load file
     * @param appInsAID AID for application instance
     * @param privileges Privileges for application
     * @param installParam Install parameter for application
     * @param installToken Install tokens
     * @return ResponseAPDU from card
     */
    public ResponseAPDU installForInstallAndSelectable(
                                       byte[] loadFileAID, 
                                       byte[] execModAID, 
                                       byte[] appInsAID, 
                                       byte[] privileges, 
                                       byte[] installParam,
                                       byte[] installToken) throws CardTerminalException {
                
        CardChannel channel;
        CommandAPDU com = new CommandAPDU(262);
        ResponseAPDU res = new ResponseAPDU(258);

        try {
            allocateCardChannel();
            channel = getCardChannel();

            com.setLength(0);
            com.append(IsoConstants.CLA_EMV);
            com.append(IsoConstants.INS_INSTALL);
            com.append((byte)0x0C);
            com.append((byte)0x00);
            
            com.append((byte)0x00);
            
            com.append((byte)loadFileAID.length);
            com.append(loadFileAID);

            com.append((byte)execModAID.length);
            com.append(execModAID);

            com.append((byte)appInsAID.length);
            com.append(appInsAID);

            com.append((byte)privileges.length);
            com.append(privileges);

            com.append((byte)installParam.length);
            com.append(installParam);

            if (installToken != null) {
                com.append((byte)installToken.length);
                com.append(installToken);
            } else {
                com.append((byte)0x00);
            }

            com.setByte(4, com.getLength() - 5);
            com.append((byte)0x00);
                
            res = sendCommandAPDU(channel, com);
        } finally {
            releaseCardChannel();
        }

        return res;
    }
            

    
    /**
     * Issue INSTALL command with INSTALL FOR LOAD option to security domain
     * 
     * @param loadFileAID
     * @param secDomAID
     * @param loadDBHash
     * @param loadParam
     * @param loadToken
     * @return ResponseAPDU from card
     * @throws CardTerminalException
     */
    public ResponseAPDU installForLoad(byte[] loadFileAID, 
                                       byte[] secDomAID, 
                                       byte[] loadDBHash, 
                                       byte[] loadParam, 
                                       byte[] loadToken) throws CardTerminalException {
        
        CardChannel channel;
        CommandAPDU com = new CommandAPDU(262);
        ResponseAPDU res = new ResponseAPDU(258);

        
        try {
            allocateCardChannel();
            channel = getCardChannel();

            com.setLength(0);
            com.append(IsoConstants.CLA_EMV);
            com.append(IsoConstants.INS_INSTALL);
            com.append((byte)0x02);
            com.append((byte)0x00);
            
            com.append((byte)0x00);
            
            com.append((byte)loadFileAID.length);
            com.append(loadFileAID);
            
            if (secDomAID != null) {
                com.append((byte)secDomAID.length);
                com.append(secDomAID);
            } else {
                com.append((byte)0x00);
            }

            if (loadDBHash != null) {
                com.append((byte)loadDBHash.length);
                com.append(loadDBHash);
            } else {
                com.append((byte)0x00);
            }
            
            if (loadParam != null) {
                com.append((byte)loadParam.length);
                com.append(loadParam);
            } else {
                com.append((byte)0x00);
            }
            
            if (loadToken != null) {
                com.append((byte)loadToken.length);
                com.append(loadToken);
            } else {
                com.append((byte)0x00);
            }

            com.setByte(4, com.getLength() - 5);
            com.append((byte)0x00);
                
            res = sendCommandAPDU(channel, com);
        } finally {
            releaseCardChannel();
        }

        return res;
    }
            

    
    /**
     * Load load file into card using a sequence of LOAD apdus
     * 
     * @param capFile Load file with cap components
     * 
     * @return Response from last LOAD commands
     * 
     * @throws CardTerminalException
     */
    public ResponseAPDU load(CapFile capFile) throws CardTerminalException {
        CardChannel channel;
        CommandAPDU com = new CommandAPDU(262);
        ResponseAPDU res = new ResponseAPDU(258);

        byte[] loadFile = capFile.getLoadFile(CapFile.CAPSEQUENCE);
        int length = loadFile.length;
        int offset = 0;
        int block;
        int count = 0;
        
        try {
            allocateCardChannel();
            channel = getCardChannel();

            while(length > 0) {
                block = length;
                if (block > 230) {
                    block = 230;
                }
                length -= block;
                
                com.setLength(0);
                com.append(IsoConstants.CLA_EMV);
                com.append(IsoConstants.INS_LOAD);
                com.append((byte)(length > 0 ? 0x00 : 0x80));   // Last block ?
                com.append((byte)count);
                com.append((byte)block);
                
                System.arraycopy(loadFile, offset, com.getBuffer(), 5, block);
                com.setLength(block + 5);
                
                com.append((byte)0x00);
                
                res = sendCommandAPDU(channel, com);
                
                if (res.sw() != IsoConstants.RC_OK) {
                    break;
                }
                count++;
                offset += block;
            }
        } finally {
            releaseCardChannel();
        }

        return res;
    }
    
    
    
    /**
     * Issue DELETE command to remove package with given AID from card
     * 
     * @param aid
     * @return ResponseAPDU from card
     * @throws CardTerminalException
     */
    public ResponseAPDU deleteAID(byte[] aid) throws CardTerminalException {
        
        CardChannel channel;
        CommandAPDU com = new CommandAPDU(262);
        ResponseAPDU res = new ResponseAPDU(258);
        
        try {
            allocateCardChannel();
            channel = getCardChannel();

            com.setLength(0);
            com.append(IsoConstants.CLA_EMV);
            com.append(IsoConstants.INS_DELETE_FILE);
            com.append((byte)0x00);
            com.append((byte)0x00);

            com.append((byte)(aid.length + 2));

            com.append((byte)0x4F);
            com.append((byte)aid.length);
            com.append(aid);
            
            com.append((byte)0x00);
                
            res = sendCommandAPDU(channel, com);
        } finally {
            releaseCardChannel();
        }

        return res;
    }



    /**
	 * Exchange APDU with card, optionally transforming the APDU with a secure channel
	 * 
	 * @param channel
	 * @param com
	 * @return the response APDU
	 * @throws InvalidCardChannelException
	 * @throws CardTerminalException
	 */
	protected ResponseAPDU sendCommandAPDU(CardChannel channel, CommandAPDU com) throws InvalidCardChannelException, CardTerminalException {
		
		ResponseAPDU res;

		SecureChannelCredential secureChannelCredential = null;
		
		if (this.credentialBag != null) {
			IsoCredentialStore credentialStore = (IsoCredentialStore) this.credentialBag.getCredentialStore(null, IsoCredentialStore.class);
			
			if (credentialStore != null) {
				secureChannelCredential = credentialStore.getSecureChannelCredential(this.aid); 
			}
		}
		
		// Transport command and response APDU using the secure channel
		if (secureChannelCredential != null) {
			SlotChannel slc = channel.getSlotChannel();
			APDUTracer tracer = slc.getAPDUTracer();
			if ((tracer != null) && (com.getLength() > 5)) {
				tracer.traceCommandAPDU(slc, com);
			}
			SecureChannel sc = secureChannelCredential.getSecureChannel();
			com = sc.wrap(com, level);
			res = channel.sendCommandAPDU(com);
			res = sc.unwrap(res, level);
			if ((tracer != null) && (res.getLength() > 2)) {
				tracer.traceResponseAPDU(slc, res);
			}
		} else {
			res = channel.sendCommandAPDU(com);
		}

		return res;
	}
	
	
	
	/**
	 * Perform INITIALIZE UPDATE APDU
	 * @param keyVersionNumber
	 * @param keyIndex
	 * @return
	 * @throws CardTerminalException 
	 * @throws InvalidCardChannelException 
	 * @throws CardServiceException 
	 */
	public ResponseAPDU initializeUpdate(byte keyVersionNumber, byte keyIndex, byte[] hostChallenge) throws InvalidCardChannelException, CardTerminalException, CardServiceException {
		
		CardChannel channel;
		CommandAPDU com = new CommandAPDU(50);
        ResponseAPDU res = new ResponseAPDU(258);
	        
        try {

        	allocateCardChannel();
            channel = getCardChannel();
                      
            // Send get data command to figure out the options for the secure channel            
            com.append((byte) 0x80);    		
    		com.append((byte) 0xCA);
    		com.append((byte) 0x00);
    		com.append((byte) 0x66);
    		com.append((byte) 0x00);

            res = channel.sendCommandAPDU(com);

            TLV cardData = TLV.factory(res.data());             
            TLV cardRecoginitionData = (TLV) cardData.getChildAt(0);            
            TLV secureChannelInfo = (TLV) cardRecoginitionData.getChildAt(3);
            
            byte[] v = secureChannelInfo.getValue();            
            byte scp = v[v.length - 2];
            byte i = v[v.length - 1];
                        
            // Check if the current secure channel implementation supports the options
            if (!GPSCP02SecureChannel.scpOptionsSupported(scp, i)) {
            	throw new CardServiceException("SCP " + scp + " with option " + i + " is not suppported");
            }
            
            com = new CommandAPDU(50);
            // Send INIT-UPDATE APDU
            com.append((byte) 0x80);    		
    		com.append((byte) 0x50);
    		com.append(keyVersionNumber);
    		com.append((byte) 0x00);
    		com.append((byte) 0x08);
    		com.append(hostChallenge);
    		com.append((byte) 0x00);		
                
            res = channel.sendCommandAPDU(com);
            
        } catch (TLVEncodingException e) {
        	throw new CardServiceException("Invalid encoding of card recognition data : " + e.getLocalizedMessage());
		} finally {
            releaseCardChannel();
        }

        return res;
	}
	
	
	
	/**
	 * Perform EXTERNAL AUTHENTICATE APDU
	 * @param level
	 * @param data Data block containing host cryptogram and MAC (must be 16 bytes)
	 * @throws CardTerminalException 
	 * @throws InvalidCardChannelException 
	 * @throws CardServiceException 
	 * 
	 */
	public ResponseAPDU externalAuthenticate(byte level, byte[] data) throws InvalidCardChannelException, CardTerminalException, CardServiceException {
		
		CardChannel channel;
		CommandAPDU com = new CommandAPDU(50);
        ResponseAPDU res = new ResponseAPDU(258);
	        
        if (level != GPSCP02SecureChannel.NONE &&
        	level != GPSCP02SecureChannel.C_MAC &&
        	level != GPSCP02SecureChannel.C_MAC_AND_C_ENC) {
        	throw new CardServiceException("Security level not supported");
        }
        
        if (data.length != 16) {
            throw new CardServiceException("Wrong length of input data. Must be 16 bytes.");
        } 
        
        try {

        	allocateCardChannel();
            channel = getCardChannel();
            
            com.append((byte) 0x84);    		
    		com.append((byte) 0x82);
    		com.append(level);
    		com.append((byte) 0x00);
    		com.append((byte) 16);
    		com.append(data);    		
                
            res = channel.sendCommandAPDU(com);
            
        } finally {
            releaseCardChannel();
        }
        
        if (res.sw() == IsoConstants.RC_OK) {
        	this.level = level;
        }
        
        return res;
	}
	
	
	
	@Override
	public void provideCredentials(SecurityDomain domain, CredentialBag creds) throws CardServiceException {
		this.credentialBag  = creds;					
	}
}
