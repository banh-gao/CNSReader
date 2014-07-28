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

package de.cardcontact.opencard.service.gemxcos;

import java.util.Enumeration;

import opencard.core.service.CardChannel;
import opencard.core.service.CardService;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceInabilityException;
import opencard.core.service.CardServiceInvalidParameterException;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.InvalidCardChannelException;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CHVControl;
import opencard.core.terminal.CHVEncoder;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalIOControl;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.HexString;
import opencard.core.util.Tracer;
import opencard.opt.iso.fs.CardFileAppID;
import opencard.opt.iso.fs.CardFileFileID;
import opencard.opt.iso.fs.CardFileInfo;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.iso.fs.CardFilePathComponent;
import opencard.opt.iso.fs.CardIOException;
import opencard.opt.iso.fs.FileAccessCardService;
import opencard.opt.iso.fs.FileSystemCardService;
import opencard.opt.security.CHVCardService;
import opencard.opt.security.CredentialBag;
import opencard.opt.security.SecurityDomain;
import opencard.opt.service.CardServiceObjectNotAvailableException;
import de.cardcontact.opencard.service.CardServiceUnexpectedStatusWordException;
import de.cardcontact.opencard.service.isocard.IsoCardService;
import de.cardcontact.opencard.service.isocard.IsoCardState;
import de.cardcontact.opencard.service.isocard.IsoConstants;

/**
 * File access card service for Gemplus XCos cards.
 *
 * @author Frank Thater
 */

public class GemXCosCardService extends CardService implements FileAccessCardService, FileSystemCardService, CHVCardService {
    private final static CardFileFileID root_file = new CardFileFileID((short)0x3F00);
    private final static CardFilePath root_path = new CardFilePath(":3F00");
    private final static Tracer ctracer = new Tracer(IsoCardService.class);

    
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

        try {
            allocateCardChannel();

            IsoCardState cardState = (IsoCardState)getCardChannel().getState();
            
            if (cardState == null) {
                cardState = new IsoCardState();
                cardState.setPath(root_path);
                cardState.setSelectCommandResponseQualifier(IsoConstants.SO_RETURNFCI);
                cardState.setLeInSelectFlag(false);
                getCardChannel().setState(cardState);
            }
        } finally {
            releaseCardChannel();
        }
    }


    
    /**
     * Determine if file exists
     * 
     * @see opencard.opt.iso.fs.FileAccessCardService#exists(opencard.opt.iso.fs.CardFilePath)
     */
    public boolean exists(CardFilePath file)
        throws CardServiceException, CardTerminalException {

        try {
            getFileInfo(file);
        }
        catch(CardServiceObjectNotAvailableException e) {
            return false;
        }
        return true;
    }

    

    /**
     * Obtain file information as returned in the SELECT command
     * 
     * @see opencard.opt.iso.fs.FileAccessCardService#getFileInfo(opencard.opt.iso.fs.CardFilePath)
     */
    public CardFileInfo getFileInfo(CardFilePath file)
        throws CardServiceException, CardTerminalException {

        CardFileInfo fci;
        
        try {
            allocateCardChannel();
            fci = selectFile(getCardChannel(), file);
        } finally {
            releaseCardChannel();
        }

        return fci;
    }

    

    /**
     * Return the root path (:3F00) of this card service
     * 
     * @see opencard.opt.iso.fs.FileAccessCardService#getRoot()
     */
    public CardFilePath getRoot() {
        return root_path;
    }


    
    /**
     * Read binary data from transparent file
     *
     * @see opencard.opt.iso.fs.FileAccessCardService#read(opencard.opt.iso.fs.CardFilePath, int, int)
     */
    public byte[] read(CardFilePath file, int offset, int length)
        throws CardServiceException, CardTerminalException {
        
        CardChannel channel;
        CommandAPDU com = new CommandAPDU(5);
        ResponseAPDU res = new ResponseAPDU(258);
        int remaining;
        byte le;
        byte[] response = null;
        
        if ((offset < 0) || (offset > 0x7FFF) || ((length != READ_SEVERAL) && (length < 0))) {
            throw new CardServiceInvalidParameterException
              ("read: offset = " + offset + ", length = " + length);
        }
        
        try {
            allocateCardChannel();
            channel = getCardChannel();
            
            if (selectFile(channel, file) == null)
                throw new CardIOException("File not found");
    
            if (length == READ_SEVERAL) {
                remaining = 0x8000;
            } else {
                remaining = length;
            }
            
            while (remaining > 0) {
                com.setLength(0);
                com.append(IsoConstants.CLA_ISO);
                com.append(IsoConstants.INS_READ_BINARY);
                com.append((byte)(offset >> 8));
                com.append((byte)offset);
                if (length == READ_SEVERAL) {
                    le = (byte)220;
                } else {
                    le = (byte)(remaining > 220 ? 220 : remaining);
                }
                com.append(le);
                
                res = channel.sendCommandAPDU(com);

                // Resend if card specifies a Le value 
                if (res.sw1() == IsoConstants.RC_INVLE) {
                    com.setLength(4);
                    com.append(res.sw2());
                    res = channel.sendCommandAPDU(com);
                }
                
                /* This handles a special case, when we read with READ_SEVERAL
                 * and read exactly to the end of file (e.g. file size is multiple
                 * of 256. In that case the offset moves exactly one byte
                 * after the end of file.
                 * As a side effect this return 0 bytes if the offset was
                 * behind the file in the first read.
                 */
                if ((res.sw() == IsoConstants.RC_INVP1P2) && (length == READ_SEVERAL))
                    break;
                    
                if ((res.sw() == IsoConstants.RC_OK) || (res.sw() == IsoConstants.RC_EOF)) {
                    int len = res.getLength() - 2;      /* Ignore SW1/SW2 */
                    
                    if (response == null) {
                        response = new byte[len];
                        System.arraycopy(res.getBuffer(), 0, response, 0, len);
                    } else {
                        byte[] buff = new byte[response.length + len];
                        System.arraycopy(response, 0, buff, 0, response.length);
                        System.arraycopy(res.getBuffer(), 0, buff, response.length, len);
                        response = buff;
                    }
                    offset += len;
                    remaining -= len;
                    if ((res.sw() == IsoConstants.RC_EOF) || ((le == 0) && (len < 256)))
                        break;
                } else {
                    throw new CardServiceUnexpectedStatusWordException("READ_BINARY", res.sw());
                }
            }
        } finally {
            releaseCardChannel();
        }

        return response;
    }

    

    /* (non-Javadoc)
     * @see opencard.opt.iso.fs.FileAccessCardService#readRecord(opencard.opt.iso.fs.CardFilePath, int)
     */
    public byte[] readRecord(CardFilePath file, int recordNumber)
        throws CardServiceException, CardTerminalException {
        
        CardChannel channel;
        CommandAPDU com = new CommandAPDU(5);
        ResponseAPDU res = new ResponseAPDU(258);
        byte[] response = null;
                
        try {
            allocateCardChannel();
            channel = getCardChannel();
            
            if (selectFile(channel, file) == null)
                throw new CardIOException("File not found");
        
            com.setLength(0);
            com.append(IsoConstants.CLA_ISO);
            com.append(IsoConstants.INS_READ_RECORD);
            com.append((byte)recordNumber);
            com.append((byte)0x04);
            com.append((byte)0x00);
                
            res = channel.sendCommandAPDU(com);

            if (res.sw() != IsoConstants.RC_OK) {
                throw new CardServiceUnexpectedStatusWordException("READ_RECORD", res.sw());
            }
            
            response = new byte[res.data().length];
            System.arraycopy(res.data(), 0, response, 0, res.data().length);
            
        } finally {
            releaseCardChannel();
        }

        return response;
    }

    
    
    /* (non-Javadoc)
     * @see opencard.opt.iso.fs.FileAccessCardService#readRecords(opencard.opt.iso.fs.CardFilePath, int)
     */
    public byte[][] readRecords(CardFilePath file, int number)
        throws CardServiceException, CardTerminalException {
        // TODO Auto-generated method stub
        return null;
    }

    
    
    /**
     * Write binary data to transparent file
     * 
     * @see opencard.opt.iso.fs.FileAccessCardService#write(opencard.opt.iso.fs.CardFilePath, int, byte[], int, int)
     */
    public void write(
        CardFilePath file,
        int foffset,
        byte[] source,
        int soffset,
        int length)
        throws CardServiceException, CardTerminalException {

        CardChannel channel;
        CommandAPDU com = new CommandAPDU(261);
        ResponseAPDU res = new ResponseAPDU(2);
        int lc;

        if ((foffset < 0) || (foffset > 0x7FFF) || (length < 0)) {
            throw new CardServiceInvalidParameterException
              ("write: offset = " + foffset + ", length = " + length);
        }

        try {
            allocateCardChannel();
            channel = getCardChannel();
            
            if (selectFile(channel, file) == null)
                throw new CardIOException("File not found");

            while (length > 0) {
                lc = length > 220 ? 220 : length;

                com.setLength(0);
                com.append(IsoConstants.CLA_ISO);
                com.append(IsoConstants.INS_UPDATE_BINARY);
                com.append((byte)(foffset >> 8));
                com.append((byte)foffset);
                com.append((byte)lc);
                System.arraycopy(source, soffset, com.getBuffer(), 5, lc);
                com.setLength(5 + lc);
                
                res = channel.sendCommandAPDU(com);
                
                if (res.sw() != IsoConstants.RC_OK) {
                    throw new CardServiceUnexpectedStatusWordException("UPDATE_BINARY" ,res.sw());
                }
                
                foffset += lc;
                soffset += lc;
                length -= lc;
            }
        } finally {
            releaseCardChannel();
        }
    }

    
    
    /**
     * Write binary data to transparent file
     *
     * @see opencard.opt.iso.fs.FileAccessCardService#write(opencard.opt.iso.fs.CardFilePath, int, byte[])
     */
    public void write(CardFilePath file, int offset, byte[] data)
        throws CardServiceException, CardTerminalException {

        write(file, offset, data, 0, data.length);
    }

    
    
    /**
     * Update record in linear file
     * 
     * @see opencard.opt.iso.fs.FileAccessCardService#writeRecord(opencard.opt.iso.fs.CardFilePath, int, byte[])
     */
    public void writeRecord(CardFilePath file, int recordNumber, byte[] data)
        throws CardServiceException, CardTerminalException {

        CardChannel channel;
        CommandAPDU com = new CommandAPDU(261);
        ResponseAPDU res = new ResponseAPDU(2);

        if ((recordNumber < 0) || (recordNumber > 254)) {
            throw new CardServiceInvalidParameterException
              ("writeRecord: recordNumber = " + recordNumber);
        }

        if (data.length > 255) {
            throw new CardServiceInvalidParameterException
              ("writeRecord: length of data = " + data.length);
        }

        try {
            allocateCardChannel();
            channel = getCardChannel();
            
            if (selectFile(channel, file) == null)
                throw new CardIOException("File not found");

            com.setLength(0);
            com.append(IsoConstants.CLA_ISO);
            com.append(IsoConstants.INS_UPDATE_RECORD);
            
            com.append((byte)(recordNumber + 1));
            com.append((byte)0x04);

            com.append((byte)data.length);
            System.arraycopy(data, 0, com.getBuffer(), 5, data.length);
            com.setLength(5 + data.length);
                
            res = channel.sendCommandAPDU(com);
                
            if (res.sw() != IsoConstants.RC_OK) {
                throw new CardServiceUnexpectedStatusWordException("UPDATE_RECORD" ,res.sw());
            }
        } finally {
            releaseCardChannel();
        }
    }

    
    
    /**
     * Append record to linear file
     * 
     * @see opencard.opt.iso.fs.FileAccessCardService#appendRecord(opencard.opt.iso.fs.CardFilePath, byte[])
     */
    public void appendRecord(CardFilePath file, byte[] data)
        throws CardServiceException, CardTerminalException {

        CardChannel channel;
        CommandAPDU com = new CommandAPDU(261);
        ResponseAPDU res = new ResponseAPDU(2);

        if (data.length > 255) {
            throw new CardServiceInvalidParameterException
              ("appendRecord: length of data = " + data.length);
        }

        try {
            allocateCardChannel();
            channel = getCardChannel();
            
            if (selectFile(channel, file) == null)
                throw new CardIOException("File not found");

            com.setLength(0);
            com.append(IsoConstants.CLA_ISO);
            com.append(IsoConstants.INS_APPEND_RECORD);
            
            com.append((byte)0x00);
            com.append((byte)0x00);

            com.append((byte)data.length);
            System.arraycopy(data, 0, com.getBuffer(), 5, data.length);
            com.setLength(5 + data.length);
                
            res = channel.sendCommandAPDU(com);
                
            if (res.sw() != IsoConstants.RC_OK) {
                throw new CardServiceUnexpectedStatusWordException("APPEND_RECORD", res.sw());
            }
        } finally {
            releaseCardChannel();
        }
    }

    
    
    /* (non-Javadoc)
     * @see opencard.opt.security.SecureService#provideCredentials(opencard.opt.security.SecurityDomain, opencard.opt.security.CredentialBag)
     */
    public void provideCredentials(SecurityDomain domain, CredentialBag creds) throws CardServiceException {
        // TODO Auto-generated method stub
        
    }


    /**
     * Select a single path component
     * 
     * @param channel
     *          Card channel to use for SELECT command
     * @param comp
     *          Path component. null is parent file is to be selected
     * @param isDF
     *          true if the path component is known to be a DF
     * @return
     *          Response APDU from SELECT command
     * 
     * @throws InvalidCardChannelException
     * @throws CardTerminalException
     */
    protected ResponseAPDU doSelect(CardChannel channel, CardFilePathComponent comp, boolean isDF, byte p1, byte p2, boolean sendLe) throws InvalidCardChannelException, CardTerminalException {
        ResponseAPDU res;
        CommandAPDU com = new CommandAPDU(30);

        com.append(IsoConstants.CLA_ISO);
        com.append(IsoConstants.INS_SELECT_FILE);

        if (p1 != -1) {
            com.append(p1);
        } else {
            if (comp == null) {
                com.append(IsoConstants.SC_PARENT);
            } else if (comp instanceof CardFileAppID) {
                com.append(IsoConstants.SC_AID);
//          } else if (!sendLe) {
//              com.append((byte)0x00);
            } else if (comp.equals(root_file)) {
                com.append(IsoConstants.SC_MF);
            } else {
                com.append(isDF ? IsoConstants.SC_DF : IsoConstants.SC_EF);
            }
        }
        
        com.append(p2);

        if (comp != null) {
            if (comp instanceof CardFileFileID) {
                com.append((byte)0x02);
                com.append(((CardFileFileID)comp).toByteArray());
            } else if (comp instanceof CardFileAppID) {
                byte aid[] = ((CardFileAppID)comp).toByteArray();
                com.append((byte)aid.length);
                com.append(aid);
            }
        }

        if (sendLe) {
            com.append((byte)0x00);
        }
        
        res = channel.sendCommandAPDU(com);
        
        if (res.sw1() == IsoConstants.RC_OKMOREDATA) {
            com.setLength(0);
            com.append(IsoConstants.CLA_ISO);
            com.append(IsoConstants.INS_GET_RESPONSE);
            com.append((byte)0x00);
            com.append((byte)0x00);
            com.append(res.sw2());
            res = channel.sendCommandAPDU(com);
        }
        
        return res;
    }


    
	/**
	 * Select directory or file according to path. This function
	 * observes the currently selected EF or DF as stored in the
	 * CardState object of the CardChannel.
	 * 
	 * @param channel
	 * 			Card channel used to communicate with the card
	 * @param path
	 * 			Path to file to be selected
	 * @return
	 * 			File control information returned in SELECT command or
	 * 			null if file was not selected
	 * 
	 * @throws InvalidCardChannelException
	 * @throws CardTerminalException
	 * @throws CardServiceObjectNotAvailableException 
	 * @throws CardServiceUnexpectedStatusWordException
	 */		
	public synchronized CardFileInfo selectFile(CardChannel channel, CardFilePath path) throws InvalidCardChannelException, CardTerminalException, CardServiceObjectNotAvailableException, CardServiceUnexpectedStatusWordException {
        CardFileInfo fci;
		boolean selectFromRoot = false;
		ResponseAPDU res = new ResponseAPDU( new byte[] { (byte)0x90, (byte)0x00 });
		CardFilePathComponent comp;
		int count;

		/* Determine card state for current channel */
				
		IsoCardState cardState = (IsoCardState)channel.getState();
		
		/* Determine cached file control information, if any */
		fci = cardState.getFCI();
		
		/* We need to select a file if the path differs from the current*/
		/* path or the file control information are not	cached			*/	
			
		if (!path.equals(cardState.getPath()) || (fci == null)) {
			/* If the currently selected object is a file or if the		*/
			/* non cached fci is requested for a DF then strip of the	*/
			/* last component of the path								*/ 
			CardFilePath currentDir = new CardFilePath(cardState.getPath());
			
			if (cardState.elementaryFileSelected() || (fci == null)) {
				if (!currentDir.chompTail()) {
					selectFromRoot = true;
				}
			}
			
			/* If the new object is a subordinate of the currently 		*/
			/* selected directory, then we just need to select			*/
			/* whatever is remaining									*/
			CardFilePath toselect = new CardFilePath(path);

			if (path.startsWith(currentDir)) {
				toselect.chompPrefix(currentDir);
			} else {
				int common;
				
				common = toselect.commonPrefixLength(currentDir);
				
				if ((common >= 1) && (currentDir.numberOfComponents() - common) == 1) {
					/* Select parent */
					res = doSelect(channel, null, false, (byte)-1, cardState.getSelectCommandResponseQualifier(), cardState.isLeInSelectEnabled());
					
					if ((res.sw() == IsoConstants.RC_OK) || (res.sw() == IsoConstants.RC_INVFILE)) {
						currentDir.chompTail();
						toselect.chompPrefix(currentDir);
					} else {
						selectFromRoot = true;
					}
				} else {
					selectFromRoot = true;
				}
			}
			
			if (selectFromRoot)
				currentDir = null;

			ctracer.info("selectFile", "Going to select " + toselect.toString());				

			Enumeration components = toselect.components();
			
			count = toselect.numberOfComponents();
			boolean assumeDF = true;
			
			while (components.hasMoreElements()) {
				comp = (CardFilePathComponent)components.nextElement();

				/* Micardo does not support a SELECT without knowing */
				/* the file type. As we don't know either, we try	 */
				/* selecting an EF first. If that fails, we retry	 */
				/* selecting a DF								     */
				
				/* The last component is usually an EF */
				if ((comp instanceof CardFileFileID) && (count == 1)) {
					assumeDF = false;
				}

				res = doSelect(channel, comp, assumeDF, (byte)-1, cardState.getSelectCommandResponseQualifier(), cardState.isLeInSelectEnabled());
				if ((res.sw1() == IsoConstants.RC_INVLEN) && selectFromRoot) {
					ctracer.info("selectFile", "Invalid length when selecting MF - Trying without data");
					res = doSelect(channel, null, assumeDF, IsoConstants.SC_MF, cardState.getSelectCommandResponseQualifier(), cardState.isLeInSelectEnabled());
				}
				    
				if ((res.sw() == IsoConstants.RC_INCP1P2) ||
				    (res.sw() == IsoConstants.RC_INVP1P2) ||
				    (res.sw() == IsoConstants.RC_INVPARA)) {
					ctracer.info("selectFile", "Invalid P1/P2 - Trying FCI instead of FCP");
				    cardState.setSelectCommandResponseQualifier(IsoConstants.SO_RETURNFCI);
					res = doSelect(channel, comp, assumeDF, (byte)-1, cardState.getSelectCommandResponseQualifier(), cardState.isLeInSelectEnabled());
					
					if (res.sw() == IsoConstants.RC_WRONGLENGTH) {
						ctracer.info("selectFile", "Wrong length - Trying without Le");
					    cardState.setLeInSelectFlag(false);
						res = doSelect(channel, comp, assumeDF, (byte)-1, cardState.getSelectCommandResponseQualifier(), cardState.isLeInSelectEnabled());
					}
				}
				    
				if ((res.sw() == IsoConstants.RC_FILENOTFOUND) && !assumeDF) {
					ctracer.info("selectFile", "EF not found - Trying DF");
					assumeDF = true;
					res = doSelect(channel, comp, assumeDF, (byte)-1, cardState.getSelectCommandResponseQualifier(), cardState.isLeInSelectEnabled());
				}
				
				if ((res.sw() == IsoConstants.RC_OK) || 
				    (res.sw() == IsoConstants.RC_INVFILE) ||
				    (res.sw1() == IsoConstants.RC_OKMOREDATA)) {
					if (currentDir == null) {
						/* We are starting at the root */
						currentDir = new CardFilePath(comp.toString());
						assumeDF = true;
					} else {
						currentDir.append(comp);
					}

					ctracer.info("selectFile", "FCI = " + HexString.hexify(res.data()));

					if (res.getLength() > 2) {
					    fci = new GemXCosFileControlInformation(res.data());
					} else {
					    fci = new GemXCosFileControlInformation();
					}
				} else {
					ctracer.error("selectFile", "SW1SW2 = " + HexString.hexifyShort(res.sw()));
					assumeDF = true;
					break;
				}
				
				count--;
			}

			if (currentDir != null) {			
				cardState.setPath(currentDir);
				cardState.setFCI(fci, !assumeDF);
			}

			/* If we only succeded to select part of the path 	*/
			/* then we can not return the fci					*/
			 			
			if (res.sw() == IsoConstants.RC_FILENOTFOUND) {
				throw new CardServiceObjectNotAvailableException("File not found");
			}
			if ((res.sw() != IsoConstants.RC_OK) && 
			    (res.sw() != IsoConstants.RC_INVFILE) &&
			    (res.sw1() != IsoConstants.RC_OKMOREDATA)) {
				throw new CardServiceUnexpectedStatusWordException("SELECT", res.sw());
			}
		}
		
		return fci;
	}

	
	
    /* (non-Javadoc)
     * @see opencard.opt.security.CHVCardService#getPasswordLength(opencard.opt.security.SecurityDomain, int)
     */
    public int getPasswordLength(SecurityDomain domain, int number) throws CardServiceException, CardTerminalException {
        // TODO Auto-generated method stub
        return 0;
    }


    
	/* (non-Javadoc)
	 * @see opencard.opt.security.CHVCardService#verifyPassword(opencard.opt.security.SecurityDomain, int, byte[])
	 */
	public boolean verifyPassword(SecurityDomain domain, int number, byte[] password) throws CardServiceException, CardTerminalException {
		
	    boolean result = false;
		CardChannel channel;
		CommandAPDU com = new CommandAPDU(40);
		ResponseAPDU res = new ResponseAPDU(2);

		try	{
			allocateCardChannel();

			channel = getCardChannel();
			
			if (domain != null) {
			    CardFilePath path = (CardFilePath)domain;

			    if (selectFile(channel, path) == null)
					throw new CardIOException("File not found");
			}
			
			com.setLength(0);
			com.append(IsoConstants.CLA_ISO);
			com.append(IsoConstants.INS_VERIFY);
			
			com.append((byte)0); // P1
			com.append((byte)0); // P2

			if (password != null) {
				
			    com.append((byte)password.length);
			    System.arraycopy(password, 0, com.getBuffer(), 5, password.length);
			    com.setLength(5 + password.length);
			
			    res = channel.sendCommandAPDU(com);
			    
			} else {
			    com.append((byte)8);
			    com.append(new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF });
			    
			    CardTerminalIOControl ioctl = 
			        new CardTerminalIOControl(8, 30, CardTerminalIOControl.IS_NUMBERS, "" );
			    CHVControl cc =
			        new CHVControl( null, number, CHVEncoder.STRING_ENCODING, 0, ioctl);
			    res = channel.sendVerifiedAPDU(com, cc, null);
			}
			
			if (res.sw() == IsoConstants.RC_OK) {
			    result = true;
			} else if ((res.sw() & 0xFFF0) == IsoConstants.RC_WARNING0LEFT) {
			    result = false;
			} else {
				throw new CardServiceUnexpectedStatusWordException("VERIFY" ,res.sw());
			}
		} finally {
			releaseCardChannel();
		}
		
		return result;
	}
    

    
    /* (non-Javadoc)
     * @see opencard.opt.security.CHVCardService#closeApplication(opencard.opt.security.SecurityDomain)
     */
    public void closeApplication(SecurityDomain domain) throws CardServiceException, CardTerminalException {
        // TODO Auto-generated method stub
        
    }



    public void create(CardFilePath parent, byte[] data) throws CardServiceException, CardTerminalException {
        // TODO Auto-generated method stub
        
    }



    public void delete(CardFilePath file) throws CardServiceException, CardTerminalException {
        // TODO Auto-generated method stub
        
    }



    public void invalidate(CardFilePath file) throws CardServiceInabilityException, CardServiceException, CardTerminalException {
        // TODO Auto-generated method stub
        
    }



    public void rehabilitate(CardFilePath file) throws CardServiceInabilityException, CardServiceException, CardTerminalException {
        // TODO Auto-generated method stub
        
    }
}