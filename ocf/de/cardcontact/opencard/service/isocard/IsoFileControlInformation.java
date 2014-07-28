/*
 *  ---------
 * |.**> <**.|  CardContact Software & System Consulting
 * |*       *|  32429 Minden, Germany (www.cardcontact.de)
 * |*       *|  Copyright (c) 1999-2004. All rights reserved
 * |'**> <**'|  See file COPYING for details on licensing
 *  --------- 
 *
 * $Log: IsoFileControlInformation.java,v $
 * Revision 1.1  2005/09/19 19:22:30  asc
 * Added support for ISO file systems
 *
 * Revision 1.4  2005/09/19 10:20:31  asc
 * Added whitelist for contactless cards
 *
 * Revision 1.3  2005/02/11 13:25:19  asc
 * Added support for Starcos signature operations
 *
 * Revision 1.2  2004/04/05 06:35:25  asc
 * Version 0.1 and first release to ChipBE
 *
 *
 */

package de.cardcontact.opencard.service.isocard;

import opencard.opt.iso.fs.CardFileInfo;
import opencard.opt.util.TLV;
import opencard.opt.util.Tag;

/**
 * Parser to TLV-encoded file control information returned in SELECT APDU
 * 
 * @author Andreas Schwier
 */
public class IsoFileControlInformation implements CardFileInfo {
	private static final Tag tagFCPUSED = new Tag(0x00, (byte)2, false);
    private static final Tag tagFCPSTRUCTURAL = new Tag(0x01, (byte)2, false);
	private static final Tag tagFCPFILETYPE = new Tag(0x02, (byte)2, false);
	private static final Tag tagFCPFID = new Tag(0x03, (byte)2, false);
	private static final Tag tagFCPPROPRIETARY = new Tag(0x05, (byte)2, false);

	byte[] fci = null;
	int filelength = -1;
	short fileid = -1;
	int filetype = 8;
	int recordsize = -1;
			
	/**
	 * 
	 */
	public IsoFileControlInformation() {
	}

	/**
	 * Create file control information from TLV coded byte array
	 * 
	 * @param newfci File control information obtained from the card
	 */
	public IsoFileControlInformation(byte[] newfci) {
		fci = newfci;
		TLV fcp, cursor;
		
        try {
            fcp = new TLV(fci);
            cursor = null;
            while ((cursor = fcp.findTag(null, cursor)) != null) {
                if (cursor.tag().isConstructed()) {
                    // Ignore constructed elements in FCP
                } else if (cursor.tag().equals(tagFCPUSED)) {
                    filelength = cursor.valueAsNumber();
                } else if (cursor.tag().equals(tagFCPFILETYPE)) {
                    byte tb[] = cursor.valueAsByteArray();
					
                    filetype = tb[0] & 0x07;
                    if (tb.length >= 3) {
                        recordsize = tb[2];
                    }
                    if (tb.length >= 4) {
                        recordsize = (recordsize << 8) + tb[3];
                    }
                } else if (cursor.tag().equals(tagFCPSTRUCTURAL)) {
                    if (filelength == -1)
                        filelength = cursor.valueAsNumber();
                } else if (cursor.tag().equals(tagFCPFID)) {
					    fileid = (short)cursor.valueAsNumber();
                }
            }
		}
        catch(Exception e) {
            // Silently ignore problems decoding TLV structure
        }
	}
	
	/* (non-Javadoc)
	 * @see opencard.opt.iso.fs.CardFileInfo#getFileID()
	 */
	public short getFileID() {
		return fileid;
	}

	/* (non-Javadoc)
	 * @see opencard.opt.iso.fs.CardFileInfo#isDirectory()
	 */
	public boolean isDirectory() {
		return filetype == 0;
	}

	/* (non-Javadoc)
	 * @see opencard.opt.iso.fs.CardFileInfo#isTransparent()
	 */
	public boolean isTransparent() {
		return filetype == 1;
	}

	/* (non-Javadoc)
	 * @see opencard.opt.iso.fs.CardFileInfo#isCyclic()
	 */
	public boolean isCyclic() {
		return (filetype & 6) == 6;
	}

	/* (non-Javadoc)
	 * @see opencard.opt.iso.fs.CardFileInfo#isVariable()
	 */
	public boolean isVariable() {
		return (filetype & 6) == 4;
	}

	/* (non-Javadoc)
	 * @see opencard.opt.iso.fs.CardFileInfo#getLength()
	 */
	public int getLength() {
		return filelength;
	}

	/* (non-Javadoc)
	 * @see opencard.opt.iso.fs.CardFileInfo#getRecordSize()
	 */
	public int getRecordSize() {
		return recordsize;
	}

	/* (non-Javadoc)
	 * @see opencard.opt.iso.fs.CardFileInfo#getHeader()
	 */
	public byte[] getHeader() {
		return fci;
	}
}
