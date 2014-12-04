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
import java.io.OutputStream;

import opencard.core.terminal.CardTerminalException;

/** <tt>CardFileInputStream</tt> together with the accompanying 
  * <tt>CardFileOutputStream</tt> provides access to a <tt>CardFile</tt> via
  * the familiar Java input/output stream mechanism.<p>
  *
  * @author  Peter Trommler (trp@zurich.ibm.com), Dirk Husemann (hud@zurich.ibm.com)
  * @version $Id: CardFileOutputStream.java,v 1.2 1999/11/03 12:37:17 damke Exp $
  *
  * @see opencard.opt.iso.fs.CardFile
  * @see opencard.opt.iso.fs.CardFileInputStream
  * @see java.io.InputStream
  */
public class CardFileOutputStream extends OutputStream
// implements OpenCardConstants 
{
  /** References to the associated smart card and <tt>CardFile</tt>. */
  private FileAccessCardService fileSystem = null;
  private CardFile cf = null;

  private int filePointer = 0;
  private boolean open;

  /** Instantiate an <tt>OuputStream</tt> for the specified
   * <tt>CardFile</tt> object.
   *
   * @param     file
   *            The file to instantiate the <tt>OutputStream</tt> for.
   * @exception java.io.IOException 
   *            Thrown if the file is not found.
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public CardFileOutputStream(CardFile file) 
       throws IOException, CardIOException, CardTerminalException
  {
    this.cf = file;
    this.fileSystem = file.getFileAccessService();

    if (!cf.isTransparent())
      throw new CardIOException("not transparent: " + file.getPath());

    this.filePointer = 0;
    open = true;
  }

  /** Closes the output stream. This method must be called to release any
   * resources associated with the stream.
   *
   * @exception java.io.IOException 
   *            If an I/O error has occurred.
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void close() throws IOException, CardTerminalException {
    if (open)
      open = false;
    fileSystem = null;
    cf = null;
  }

  /** Flushes this <tt>CardFileOutputStream</tt> and forces any buffered output
   * bytes to be written out.
   *
   * @exception opencard.core.terminal.CardTerminalException 
   *            Thrown when the smart card has been removed.
   * @exception java.io.IOException 
   *            if an I/O error occurs.
   */
  public void flush() throws IOException, CardTerminalException {
    // ... we do not buffer
    return;
  }

  /** Write a byte of data.
   *
   * @param     b 
   *            The byte to be written
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occured.
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void write(int b) throws IOException, CardTerminalException {
    byte[] data = new byte[1];

    data[0] = (byte)b;
    fileSystem.write(cf.getPath(), filePointer, data);
    filePointer++;

    return;
  }

  /** Write an array of bytes.
   *
   * @param     b 
   *            The data to be written
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occured.
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void write(byte[] b) throws IOException, CardTerminalException {
    fileSystem.write(cf.getPath(), filePointer, b);
    filePointer+= b.length;

    return;
  }

  /**
    * Write a slice of a byte array.
    *
    * @param     b 
    *            The data to be written
    * @param     offset 
    *            The start offset in the data (not in the file!)
    * @param     length 
    *            The number of bytes that are written
    * @exception java.io.IOException 
    *            Thrown if an I/O error has occured.
    * @exception opencard.core.terminal.CardTerminalException
    *            Thrown when the smart card has been removed.
    */
  public void write(byte[] b, int offset, int length) 
    throws IOException, CardTerminalException {
      byte[] data = new byte[length];

      System.arraycopy(b, offset, data, 0, length);
      this.write(data);

      return;
  }

  /** Make sure that the output stream is closed on garbage collection.
    *
    * @exception java.io.IOException 
    *            Thrown when an I/O error occurs.
    */
  protected void finalize() throws IOException {
    close();
  }


} // class CardFileOutputStream
