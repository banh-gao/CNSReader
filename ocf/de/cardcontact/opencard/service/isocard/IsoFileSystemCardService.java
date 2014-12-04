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

import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;
import opencard.opt.iso.fs.CardFileInfo;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.iso.fs.CardFilePathComponent;
import opencard.opt.iso.fs.FileAccessCardService;
import opencard.opt.iso.fs.FileSystemCardService;

/**
 * This interface extents the original interface in the signature for the
 * create() and delete() method to allow better compatibility with ISO 7816-9
 * smart cards.
 * 
 * @author Andreas Schwier (info@cardcontact.de)
 */
public interface IsoFileSystemCardService extends FileSystemCardService {

    /**
     * Creates a file on the smartcard.
     * 
     * This method is an extension to the method originally defined by OCF. It allows the
     * caller to specify P1 and P2 in the command APDU as defined by ISO 7816-9.
     * <br> 
     * Creating files is a card-specific operation. While the ISO file types
     * are specified, the access conditions that can be defined are not. When
     * creating a file, the access conditions to the new file have to be given.
     * The result is that no card-independent arguments to a <tt>create</tt>
     * method can be specified.
     * <br>
     * This method defines only a card-neutral <i>signature</i> by expecting
     * a byte array as a parameter. The data to be stored in that byte array
     * is card-specific. It is suggested, but not required, that a file header,
     * as it is returned by <tt>CardFileInfo.getHeader</tt>, is accepted as
     * that parameter block. A file header typically holds all information
     * needed for creating a file, in a card-specific encoding. This information
     * includes the file ID, structure, size, and the access conditions.
     * <p>
     * This method is intended to be used in a scenario where new applications
     * have to be downloaded on a smartcard. Typically, a server will be
     * contacted to retrieve the information about the directories and files
     * that have to be created. This server can be supplied with the card's ATR,
     * which is encapsulated by class <tt>CardID</tt>. The server will then be
     * able to send parameter blocks that are appropriate arguments for this
     * method and the respective card.
     *
     * @param parent   the path to the directory in which to create a new file
     * @param fileDescriptorByte
     *                 File descriptor byte according to ISO 7816-4
     * @param shortFileIdentifier
     *                 Short file identifer coded on bit b8 - b4
     * @param data     the parameters specifying the file to create.
     *                 This argument is card-specific. Refer to the documentation
     *                 of the card-specific service for details.
     *
     * @see opencard.opt.iso.fs.FileSystemCardService
     * @see FileAccessCardService#getFileInfo
     * @see CardFileInfo#getHeader
     * @see opencard.core.terminal.CardID
     * @see opencard.core.service.SmartCard#getCardID
     *
     * @exception CardServiceException
     *            if the service encountered an error
     * @exception CardTerminalException
     *            if the terminal encountered an error
     */
    public void create(CardFilePath parent, byte fileDescriptorByte, byte shortFileIdentifier, byte[] data)
         throws CardServiceException, CardTerminalException;


    /**
     * Deletes a file on the smartcard.
     * 
     * This method is an extension to the original method defined by OCF. It allows to
     * delete an object from within a selected DF. The implementation therefore allows to
     * differentiate between a "delete child" and a "delete self" operation which may have
     * different access conditions.
     * 
     * Deleting a file completely removes it from the smartcard. The associated
     * resources on the card, that is the allocated memory, will be freed. It
     * is not possible to restore the file. A new file with the same id as the
     * deleted file may be created in the same directory (DF).
     *
     * @param file        the path to the file to delete
     * @param child       File identifier of child object (either EF, DF or application)
     * @param childIsDF   True, if the child is a dedicated file 
     *
     * @exception CardServiceException
     *            if the service encountered an error
     * @exception CardTerminalException
     *            if the terminal encountered an error
     */
    public void delete(CardFilePath file, CardFilePathComponent child, boolean childIsDF)
         throws CardServiceException, CardTerminalException;
}
