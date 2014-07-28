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
import java.io.InputStream;

import opencard.core.terminal.CardTerminalException;

/** <tt>CardFileInputStream</tt> together with the accompanying 
 * <tt>CardFileOutputStream</tt> provides access to a <tt>CardFile</tt> via
 * the familiar Java input/output stream mechanism.<p>
 * <b>This class only works on transparent files.</b>
 *
 * @author  Peter Trommler (trp@zurich.ibm.com), Dirk Husemann (hud@zurich.ibm.com)
 * @version $Id: CardFileInputStream.java,v 1.2 1999/11/03 12:37:17 damke Exp $
 *
 * @see opencard.opt.iso.fs.CardFile
 * @see opencard.opt.iso.fs.CardFileOutputStream
 * @see java.io.InputStream
 */
public class CardFileInputStream extends InputStream
{
  /** Miscellaneous private fields.
   */
  private CardFile cf;
  private FileAccessCardService fileSystem;
  private int filePointer;
  private boolean open;

  /** Instantiate an input stream using the specified <tt>CardFile</tt> object.
    *
    * @param     file 
    *            The <tt>CardFile</tt> object to base the input stream on.
    * @exception java.io.IOException
    *            Thrown when the file open fails.
    * @exception CardIOException 
    *            The file is not found or it's type is not transparent.
    * @exception opencard.core.terminal.CardTerminalException
    *            Thrown when the smart card has been removed.
    */
  public CardFileInputStream(CardFile file) 
    throws IOException, CardTerminalException, CardIOException {
      this.cf = file;
      this.fileSystem = file.getFileAccessService();

      if (!cf.isTransparent())
	throw new CardIOException("not transparent: " + file.getPath());

      this.filePointer = 0;
      open = true;
  }
  
  /** Return the number of byte available for reading.
    *
    * @return    The number of bytes available for reading without blocking.
    * @exception java.io.IOException
    *            Thrown if we encounter an IO error.
    * @exception opencard.core.terminal.CardTerminalException
    *            Thrown when the smart card has been removed.
    */
  public int available() throws IOException, CardTerminalException {
    return 0;			// this is actually true for now
				// we do not use buffering yet.
  }

  /** Close the input stream and release any resources associated with 
    * the stream.
    *
    * @exception java.io.IOException 
    *            Thrown if an I/O error has occurred.
    * @exception opencard.core.terminal.CardTerminalException
    *            Thrown when the smart card has been removed.
    */
  public void close() throws IOException, CardTerminalException {
    if (open)
      open = false;
    fileSystem = null;
    cf = null;
  }

  /** Read a byte of data.
    *
    * @return    The byte read, or <tt>-1</tt> if we reached the end of 
    *            the input stream.
    * @exception java.io.IOException 
    *            Thrown if we encounter IO trouble.
    * @exception opencard.core.terminal.CardTerminalException
    *            Thrown when the smart card has been removed.
    */
  public int read() throws IOException, CardTerminalException {
    byte[] data = fileSystem.read(cf.getPath(), filePointer, 1);
    filePointer++;

    return (data == null) ? -1 : (data[0] & 0xff);
  }

  /** Read data into a byte array.
    *
    * @param     b 
    *            The buffer to use.
    * @return    The actual number of bytes read, or <tt>-1</tt> if we reached
    *            the end of the input stream.
    * @exception java.io.IOException 
    *            Thrown if an IO error occured.
    * @exception opencard.core.terminal.CardTerminalException
    *            Thrown when the smart card has been removed.
    */
  public int read(byte[] b) throws IOException, CardTerminalException {
    return this.read(b, 0, b.length);
  }
  
  /** Reads data into a slice of a byte array.
    *
    * @param     b 
    *            The buffer into which the data is read
    * @param     offset 
    *            The start offset of the data
    * @param     length
    *            The maximum number of bytes to be read
    * @return    The actual number of bytes read, or <tt>-1</tt> if we reached
    *            end of the rainbow, err, input stream.
    * @exception java.io.IOException 
    *            Thrown if an I/O error has occured.
    * @exception opencard.core.terminal.CardTerminalException
    *            Thrown when the smart card has been removed.
    */
  public int read(byte[] b, int offset, int length) 
    throws IOException, CardTerminalException {
      byte[] data = fileSystem.read(cf.getPath(), filePointer, length);

      if (data == null)
	return -1;

      System.arraycopy(data, 0, b, offset, data.length);
      filePointer += data.length;

      return data.length;
  }

  /** Skips <tt>n</tt> bytes of input.
    *
    * @param     n   
    *            The number of bytes to be skipped
    * @return    The actual number of bytes skipped.
    * @exception java.io.IOException
    *            Thrown when <tt>skip()</tt>ing was not possible.
    * @exception opencard.core.terminal.CardTerminalException
    *            Thrown when the smart card has been removed.
    */
  public long skip(long n) throws IOException, CardTerminalException {
    long len = (long)cf.getLength();
    long oldFilePointer = (long)filePointer;
    long newFilePointer = oldFilePointer + n;

    if (newFilePointer > len)
      newFilePointer = len;

    filePointer = (int)newFilePointer;

    return newFilePointer - oldFilePointer;
  }

  /** Close the stream at garbage collection time.
    * 
    * @exception java.io.IOException
    *            Thrown when we encountered an IO error.
    */
  protected void finalize() throws IOException {
    close();
  }


} // class CardFileInputStream
