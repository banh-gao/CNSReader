/*
 * Copyright © 1997 - 1999 IBM Corporation.
 * 
 * Redistribution and use in source (source code) and binary (object code)
 * forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 1. Redistributed source code must retain the above copyright notice, this
 * list of conditions and the disclaimer below.
 * 2. Redistributed object code must reproduce the above copyright notice,
 * this list of conditions and the disclaimer below in the documentation
 * and/or other materials provided with the distribution.
 * 3. The name of IBM may not be used to endorse or promote products derived
 * from this software or in any other form without specific prior written
 * permission from IBM.
 * 4. Redistribution of any modified code must be labeled "Code derived from
 * the original OpenCard Framework".
 * 
 * THIS SOFTWARE IS PROVIDED BY IBM "AS IS" FREE OF CHARGE. IBM SHALL NOT BE
 * LIABLE FOR INFRINGEMENTS OF THIRD PARTIES RIGHTS BASED ON THIS SOFTWARE.  ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IBM DOES NOT WARRANT THAT THE FUNCTIONS CONTAINED IN THIS
 * SOFTWARE WILL MEET THE USER'S REQUIREMENTS OR THAT THE OPERATION OF IT WILL
 * BE UNINTERRUPTED OR ERROR-FREE.  IN NO EVENT, UNLESS REQUIRED BY APPLICABLE
 * LAW, SHALL IBM BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  ALSO, IBM IS UNDER NO OBLIGATION
 * TO MAINTAIN, CORRECT, UPDATE, CHANGE, MODIFY, OR OTHERWISE SUPPORT THIS
 * SOFTWARE.
 */

package opencard.opt.iso.fs;


import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceInabilityException;
import opencard.core.terminal.CardTerminalException;


/**
 * Interface defining creational methods for file system based smartcards.
 * Creational methods are methods that support file creation, deletion, and
 * invalidation. These methods are extensions to the file access methods
 * specified in ISO 7816-4. A card service has to implement these methods
 * in order to support the corresponding methods in class <tt>CardFile</tt>.
 * <br>
 * For the <tt>CardFilePath</tt> arguments in the methods defined here, the
 * restrictions described in <tt>FileAccessCardService</tt> apply, too.
 *
 * @author Dirk Husemann (hud@zurich.ibm.com)
 * @author Reto Hermann (rhe@zurich.ibm.com)
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @version $Id: FileSystemCardService.java,v 1.1.1.1 1999/10/05 15:08:47 damke Exp $
 *
 * @see CardFile
 * @see FileAccessCardService
 */
public interface FileSystemCardService extends FileAccessCardService
{
  /**
   * Creates a file on the smartcard.
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
   * @param data     the parameters specifying the file to create.
   *                 This argument is card-specific. Refer to the documentation
   *                 of the card-specific service for details.
   *
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
  public void create(CardFilePath parent, byte[] data)
       throws CardServiceException, CardTerminalException;


  /**
   * Deletes a file on the smartcard.
   * Deleting a file completely removes it from the smartcard. The associated
   * resources on the card, that is the allocated memory, will be freed. It
   * is not possible to restore the file. A new file with the same id as the
   * deleted file may be created in the same directory (DF).
   *
   * @param file        the path to the file to delete
   *
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public void delete(CardFilePath file)
       throws CardServiceException, CardTerminalException;


  /**
   * Invalidates a file on the smartcard.
   * Invalidating a file makes it inaccessible, but leaves it on the card.
   * The associated resources of the file are not freed. It is not possible
   * to create a new file with the same id in the same directory (DF).
   * It may be possible to reverse the invalidation by invoking
   * <tt>rehabilitate</tt>.
   * <br>
   * Since a card service may implement this interface to provide create
   * and delete access only, a <tt>CardServiceInabilityException</tt> may
   * be thrown if invalidation is not supported.
   *
   * <p>
   * This method should <i>not</i> be implemented by setting all access
   * conditions of the file to NEVER. Files with access condition NEVER
   * are often used for internal purposes, for example to hold keys or
   * application specific executable code. Invalidating a file should make
   * the card OS ignore the file's contents.
   *
   * @param file  the path to the file to invalidate
   *
   * @exception CardServiceInabilityException
   *            if the service does not support this operation
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public void invalidate(CardFilePath file)
       throws CardServiceInabilityException,
              CardServiceException,
              CardTerminalException;

  
  /**
   * Rehabilitates a file on the smartcard.
   * This is the inverse operation to the invalidation of a smartcard file.
   * It restores the file to the state it had before invalidation. Since a
   * card service may implement this interface to provide create and
   * delete access only, a <tt>CardServiceInabilityException</tt> may be
   * thrown if rehabilitation is not supported.
   *
   * @param file  the path to the file to rehabilitate
   *
   * @exception CardServiceInabilityException
   *            if the service does not support this operation
   * @exception CardServiceException
   *            if the service encountered an error
   * @exception CardTerminalException
   *            if the terminal encountered an error
   */
  public void rehabilitate(CardFilePath file)
       throws CardServiceInabilityException,
              CardServiceException,
              CardTerminalException;
  

} // interface FileSystemCardService
