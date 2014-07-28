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

import java.io.IOException;

import opencard.core.terminal.CardTerminalException;

/** Superclass for both <tt>CardRandomAccess</tt> classes.
  *
  * @author  Peter Trommler (trp@zurich.ibm.com)
  * @version $Id: CardRandomAccess.java,v 1.2 1999/11/03 12:37:18 damke Exp $
  *
  * @see opencard.opt.iso.fs.CardRandomRecordAccess
  * @see opencard.opt.iso.fs.CardRandomByteAccess
  */
public abstract class CardRandomAccess {
  //  protected CardFileType fileType;
  protected CardFile file;
  protected FileAccessCardService fileSystem;
  protected int filePointer;
  protected boolean writeAccess;
  protected boolean open;

  /** Instantiate a <tt>CardRandomAccess</tt> object.
   *
   * @param     scFile
   *            The <tt>CardFile</tt> object that represents the file
   *            on the smart card.
   * @param     accessMode
   *            Specifies whether the card is accessed for read "r" or
   *            read and write "rw".
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public CardRandomAccess(CardFile scFile, String accessMode) 
    throws CardTerminalException, IOException {
      file = scFile;
      fileSystem = scFile.getFileAccessService();
      open = true;
      filePointer = 0;
  }

  /** Close the file.
   *
   * @exception IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void close() throws IOException, CardTerminalException {
    if (open)
      open = false;
  }

  /** (Re-)open the file.
   *
   * @exception IOException 
   *            Thrown if an I/O error has occured.
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void open() throws IOException, CardTerminalException {
    open = true;
    filePointer = 0;
  }

  /** Free up all resources at garbage collection time.
    *
    * @exception java.io.IOException
    *            An I/O error occurred.
    */
  protected void finalize() throws IOException {
    close();
  }

} // CardRandomAccess
