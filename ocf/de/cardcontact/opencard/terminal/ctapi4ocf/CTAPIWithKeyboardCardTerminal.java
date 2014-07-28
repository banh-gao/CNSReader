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

package de.cardcontact.opencard.terminal.ctapi4ocf;

import opencard.core.terminal.CHVControl;
import opencard.core.terminal.CHVEncoder;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.SlotChannel;
import opencard.core.terminal.VerifiedAPDUInterface;
import opencard.opt.util.TLV;
import opencard.opt.util.Tag;

public class CTAPIWithKeyboardCardTerminal extends CTAPICardTerminal implements VerifiedAPDUInterface {

    protected CTAPIWithKeyboardCardTerminal(String name, String type, String device, String libname) throws CardTerminalException {
        super(name, type, device, libname);
    }

    public ResponseAPDU sendVerifiedCommandAPDU(SlotChannel chann, CommandAPDU capdu, CHVControl vc) throws CardTerminalException {
        CommandAPDU command = new CommandAPDU(128);
        
        command.append((byte)0x20);   // CLA
        command.append((byte)0x18);   // INS
        
        int slot = chann.getSlotNumber() + 1;
        
        command.append((byte)slot); // P1 - Functional Unit
        command.append((byte)0x00); // P2 - User authentication by PINPad
        
        command.append((byte)0x00); // Lc - need to fill that later

        byte[] tmp = new byte[capdu.getLength() + 2];
        
        if (vc.passwordEncoding().equals(CHVEncoder.STRING_ENCODING)) {
            tmp[0] |= 0x01;
        } else if (vc.passwordEncoding().equals(CHVEncoder.F2B_ENCODING)) {
            tmp[0] |= 0x02;
        } // Default is 00, which is BCD encoding
        
/*
        CardTerminalIOControl io = vc.ioControl();
        if (io != null) {
            int ics = io.maxInputChars();
            tmp[0] |= (ics << 4) & 0xF0;
        }
*/

        tmp[1] = (byte)(vc.passwordOffset() + 6);   // Offset in OCF is 0 based offset in data field
                                                    // Offset in MKT is 1 based offset in APDU

        System.arraycopy(capdu.getBuffer(), 0, tmp, 2, capdu.getLength());
        
        TLV ctpdo = new TLV(new Tag(18, (byte)1, false), tmp);
        command.append(ctpdo.toBinary());
        
        String prompt = vc.prompt();
        if (prompt != null) {
            TLV dspdo = new TLV(new Tag(16, (byte)1, false), prompt.getBytes());
            command.append(dspdo.toBinary());
        }
        
        command.setByte(4, command.getLength() - 5);
        
        byte[] buf = new byte[2];
        char buflen = (char)buf.length;
        int res;
        
        synchronized (this) {
            res = CT.CT_Data(ctn, (byte) 1, (byte) 2, (char)command.getLength(), command.getBuffer(), buflen, buf);
        }

        if (res < 0) {
            throw (new CardTerminalException("CTAPICardTerminal: PERFORM VERIFICATION failed, ERROR=" + res));
        }
        
        return new ResponseAPDU(buf);
    }
}
