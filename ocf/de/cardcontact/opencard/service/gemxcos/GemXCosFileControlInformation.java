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

import opencard.opt.iso.fs.CardFileInfo;

public class GemXCosFileControlInformation implements CardFileInfo {

    byte[] fci = null;
    int filelength = -1;
    short fileid = -1;
    int filetype = 8;
    int recordsize = -1;
         
    
    
    /**
     * 
     */
    public GemXCosFileControlInformation() {
    }

    
    
    /**
     * Create file control information from TLV coded byte array
     * 
     * @param newfci File control information obtained from card as response to SELECT APDU
     */
    public GemXCosFileControlInformation(byte[] newfci) {
        fci = newfci;
        
        if (newfci.length >= 2) {
            filelength = ((newfci[0] & 0xFF) << 8) + (newfci[1] & 0xFF);
        }
        if (newfci.length >= 3) {
            filetype = newfci[2] & 0xFF;
        }
        if (newfci.length >= 5) {
            fileid = (short) ((((newfci[3] & 0xFF) << 8) + (newfci[4] & 0xFF)) & 0xFFFF);
        }
/*
        TLV fcp, cursor;
        
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
            } else if (cursor.tag().equals(tagFCPPROPRIETARY)) {
                if (filelength == -1)
                    filelength = cursor.valueAsNumber();
            } else if (cursor.tag().equals(tagFCPFID)) {
                    fileid = (short)cursor.valueAsNumber();
            }
        }
*/
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
        return filetype == 0x38;
    }

    /* (non-Javadoc)
     * @see opencard.opt.iso.fs.CardFileInfo#isTransparent()
     */
    public boolean isTransparent() {
        return (filetype & 1) == 1;
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
