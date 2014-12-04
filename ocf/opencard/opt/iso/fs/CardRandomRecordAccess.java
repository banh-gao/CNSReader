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

import java.io.EOFException;
import java.io.IOException;

import opencard.core.terminal.CardTerminalException;

/**
 * <tt>CardRandomRecordAccess</tt> provides record-oriented, random access to
 * structured smart card files. This class supports only linear files of
 * either fixed or variable record size. There is no random access to cyclic
 * files. To access transparent files, use <tt>CardRandomByteAccess</tt>
 *
 * <tt>CardRandomRecordAccess</tt> is loosely modeled on
 * <tt>java.io.RandomAccessFile</tt>.<p>
 *
 * @author Peter Trommler (trp@zurich.ibm.com)
 * @author Reto Hermann (rhe@zurich.ibm.com)
 * @author Dirk Husemann (hud@zurich.ibm.com)
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @version $Id: CardRandomRecordAccess.java,v 1.1.1.1 1999/10/05 15:08:47 damke Exp $
 *
 * @see java.io.RandomAccessFile
 * @see opencard.opt.iso.fs.CardRandomByteAccess
 */
public class CardRandomRecordAccess extends CardRandomAccess {

  /**
   * Instantiates a <tt>CardRandomRecordAccess</tt>.
   *
   * @param     scFile
   *            The <tt>CardFile</tt> object that represents the file on the 
   *            smart card.
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   * @exception java.io.IOException 
   *            Thrown for all other I/O exceptions.
   */
  public CardRandomRecordAccess(CardFile scFile) 
    throws CardTerminalException, IOException {
      this(scFile, "rw");
  }

  /**
   * Instantiates a <tt>CardRandomRecordAccess</tt> object.
   *
   * @param     scFile
   *            The <tt>CardFile</tt> object that represents the file on the 
   *            smart card.
   * @param     accessMode
   *            Specifies whether the card is accessed for read "r" or
   *            read and write "rw".
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   * @exception java.io.IOException 
   *            Thrown for all other I/O exceptions.
   */
  public CardRandomRecordAccess(CardFile scFile, String accessMode ) 
    throws CardTerminalException, IOException {
      super(scFile, accessMode);

      if (file.isDirectory() || file.isTransparent())
	throw new CardIOException("not a structured file " + file.getPath());
      if (file.isCyclic())
        throw new CardIOException("cyclic file: " + file.getPath());
  }



  /** Returns the current location of the file pointer.
   *
   * @return    The current location of the file pointer.
   * @exception IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   * @exception java.io.IOException 
   *            Thrown for all other I/O exceptions.
   */
  public long getFilePointer() 
    throws IOException, CardTerminalException {
      // we could check whether the card is still present...
      return filePointer;
  }

  /**
   * Sets the file pointer to the specified absolute position.
   *
   * @param     pos 
   *            The absolute position 
   * @exception java.io.EOFException 
   *            Thrown if the seeked position is behind the end of the file.
   *            For linear variable files, this cannot be checked. An error
   *            will occur on the subsequent read or write operation.
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void seek(long pos) throws EOFException, CardTerminalException {
    if (!file.isVariable()) {
      // check the position
      int length  = file.getLength();
      int size    = file.getRecordSize();
      int records = length/size;

      if (pos >= records)
        throw new EOFException("seek past end of file");
    }

    filePointer = (int)pos;
  }
  
  /** Skips the number of records specified.
   *
   * @param     n 
   *            The number of records to skip
   * @exception java.io.EOFException 
   *            EOF reached before all records have been skipped.
   *            This cannot be checked for linear variable files.
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public int skip(int n)
    throws EOFException, CardTerminalException {
      seek(filePointer+n);
      return n;
  }



  /**
   * Reads a data record. This method will block if no input is
   * available.
   *
   * @return    The record read, or <tt>null</tt> if the
   *            end of the file is reached. 
   * @exception IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public CardRecord readRecord() throws IOException, CardTerminalException {
    CardRecord cr = null;
    byte[] data = fileSystem.readRecord(file.getPath(), filePointer);
    if (data != null) {
      cr = new CardRecord(data);
      filePointer++;
    }
    return cr;
  }
  
  /**
   * Reads data into an array of <tt>CardRecord</tt>s.
   * This method blocks until some input is available.
   *
   * @param     r
   *            The <tt>CardRecord</tt> array to store the records in.
   * @return    The actual number of records read, <tt>-1</tt> is returned if
   *            the end of the file is reached.
   * @exception IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public int read(CardRecord r[]) 
    throws IOException, CardTerminalException {
      return read(r, 0, r.length);
  }
  
  /**
   * Reads a sub array as a sequence of <tt>CardRecord</tt>s.
   *
   * @param     r 
   *            The data to be read
   * @param     off 
   *            The start offset in the data 
   * @param     len 
   *            The number of records to be read
   * @return    The actual number of records read, <tt>-1</tt> is returned if
   *            the end of the stream is reached.
   * @exception IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public int read(CardRecord r[], int off, int len) 
    throws IOException, CardTerminalException {
      int i;

      for (i = 0; len > 0; len--, i++, off++) {
	r[off] = readRecord();
	if (r[off] == null)
	  break;
      }

      if (i == 0)
	return -1;

      return i;
  }


  
  /** Writes a record. This method will block until the record is
   * actually written.
   * 
   * @param     r 
   *            The record to be written 
   * @exception IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void write(CardRecord r) throws IOException, CardTerminalException {
    fileSystem.writeRecord(file.getPath(), filePointer, r.bytes());
    filePointer++;
  }
  
  /** Writes an array of records. Will block until the records are
   * actually written.
   *
   * @param     r 
   *            The records to be written 
   * @exception IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void write(CardRecord r[]) throws IOException, CardTerminalException {
    write(r, 0, r.length);
  }

  /** Writes a slice of a <tt>CardRecord</tt> array.
   * 
   * @param     r 
   *            The data to be written 
   * @param     off 
   *            The start offset in the data 
   * @param     len 
   *            The number of records to be written 
   * @exception IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void write(CardRecord r[], int off, int len) 
    throws IOException, CardTerminalException {
      for ( ; len > 0; off++, len --)
	write(r[off]);
  }

} // class CardRandomRecordAccess
