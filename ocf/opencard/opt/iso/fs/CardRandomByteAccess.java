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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import opencard.core.terminal.CardTerminalException;

/** <tt>CardRandomByteAccess</tt> provides a byte-oriented way of
  * accessing a card file (for <i>transparent</i> smart card files).<p>
  *
  * @author  Reto Hermann (rhe@zurich.ibm.com), Peter Trommler (trp@zurich.ibm.com), Dirk Husemann (hud@zurich.ibm.com)
  * @version $Id: CardRandomByteAccess.java,v 1.2 1999/11/03 12:37:18 damke Exp $
  *
  * @see opencard.opt.iso.fs.CardFile
  * @see java.io.RandomAccessFile
  * @see java.io.DataInput
  * @see java.io.DataOutput
  */
public class CardRandomByteAccess extends CardRandomAccess
 implements DataOutput, DataInput
{
  /** Instantiate a <tt>CardRandomByteAccess</tt> object.
   *
   * @param     scFile
   *            The <tt>CardFile</tt> object that represents the file on the 
   *            smart card.
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   * @exception java.io.IOException 
   *            Thrown for all other I/O exceptions.
   */
  public CardRandomByteAccess(CardFile scFile) 
    throws CardTerminalException, IOException {
      this(scFile, "rw");
  }

  /** Instantiate a <tt>CardRandomByteAccess</tt> object.
   *
   * @param     scFile
   *            The <tt>CardFile</tt> object that represents the file on the 
   *            smart card.
   * @param     accessMode
   *            Specifies whether the card is accessed for read "r" or read/write "rw".
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   * @exception java.io.IOException 
   *            Thrown for all other I/O exceptions.
   */
  public CardRandomByteAccess(CardFile scFile, String accessMode ) 
    throws CardTerminalException, IOException {
      super(scFile, accessMode);

      if (!file.isTransparent())
	throw new CardIOException("not transparent: " + file.getPath());
  }

  
  /** Reads a byte of data. This method will block if no input is
   * available.
   *
   * @return    The byte read, or <tt>-1</tt> if the end of the file is reached. 
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public int read() throws IOException, CardTerminalException {
    byte[] data = fileSystem.read(file.getPath(), filePointer, 1);
    filePointer++;

    return (data == null) ? -1 : data[0];
  }
  
  /** Reads data into an array of bytes. This method blocks until some
   * input is available.
   *
   * @param     b 
   *            A buffer to put the data into.
   * @return    The actual number of bytes read, <tt>-1</tt> is returned when
   *            the end of the file is reached.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public int read(byte b[]) 
    throws IOException, CardTerminalException {
      return this.read(b, 0, b.length);
  }
  
  /** Reads a sub array as a sequence of bytes.
   *
   * @param     b 
   *            the buffer to read the data into
   * @param     off 
   *            The start offset in the data buffer
   * @param     len 
   *            The number of bytes to read 
   * @return    The actual number of bytes read, <tt>-1</tt> is returned when
   *            the end of the file is reached.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   * #state     specified
   */
  public int read(byte b[], int off, int len) 
    throws IOException, CardTerminalException {
      byte[] data = fileSystem.read(file.getPath(), filePointer, len);
      
      if (data == null)
	return -1;

      System.arraycopy(data, 0, b, off, data.length);
      filePointer += data.length;
      
      return data.length;
  }
  
  /** Reads a boolean.
   *
   * @return    The boolean read.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public boolean readBoolean() throws IOException, CardTerminalException {
    // stolen from RandomAccessFile
    int ch = this.read();
    if (ch < 0)
      throw new EOFException();
    return (ch != 0);
  }
  
  /** Reads a byte.
   * 
   * @return    The byte read
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public byte readByte() throws IOException, CardTerminalException {
    int ch = this.read();
    if (ch < 0)
      throw new EOFException();
    return (byte)(ch);
  }
  
  /** Reads a 16 bit char.
   *
   * @return    The read 16 bit char. 
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public char readChar() throws IOException, CardTerminalException {
    byte[] bytes = new byte[2];
    this.read(bytes);
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
    return in.readChar();
  }
  
  /** Reads a 64 bit double.
   *
   * @return    The read 64 bit double. 
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public double readDouble() throws IOException, CardTerminalException {
    byte[] bytes = new byte[8];
    this.read(bytes);
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
    return in.readDouble();
  }
  
  /** Reads a 32 bit float.
   * 
   * @return    The read 32 bit float.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public float readFloat() throws IOException, CardTerminalException {
    byte[] bytes = new byte[4];
    this.read(bytes);
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
    return in.readFloat();
  }
  
  /** Reads bytes, blocking until all bytes are read.
   *
   * @param     b 
   *            The buffer into which the data is read 
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void readFully(byte b[]) 
    throws IOException, CardTerminalException {
      readFully(b, 0, b.length);
  }
  
  /** Reads bytes, blocking until all bytes are read.
   *
   * @param     b 
   *            The buffer into which the data is read 
   * @param     off 
   *            The start offset of the data 
   * @param     len 
   *            The number of bytes to read 
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void readFully(byte b[], int off, int len) 
    throws IOException, CardTerminalException {
      int readCount = 0;

      for (int i = 0; i < len; i += readCount) {
	readCount = this.read(b, off + i, len -i);
	if (readCount < 0)
	  throw new EOFException();
      }
  }
  
  /** Reads a 32 bit int.
   *
   * @return    The read 32 bit integer. 
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public int readInt() throws IOException, CardTerminalException {
    byte[] bytes = new byte[4];
    this.readFully(bytes);
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
    return in.readInt();
  }
  
  /** Reads a line terminated by a '\n' or EOF.
   *
   * @return    A string containing the read line.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public String readLine() throws IOException, CardTerminalException {
    StringBuffer input = new StringBuffer();
    int c;
    
    while (((c = read()) != -1) && (c != '\n')) {
      input.append((char)c);
    }

    if ((c == -1) && (input.length() == 0)) {
      return null;
    }

    return input.toString();
  }
  
  /** Reads a 64 bit long.
   *
   * @return    The read 64 bit long. 
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public long readLong() throws IOException, CardTerminalException {
    byte[] bytes = new byte[8];
    this.readFully(bytes);
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
    return in.readLong();
  }
  
  /** Reads a 16 bit short.
   *
   * @return    The read 16 bit short.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public short readShort() throws IOException, CardTerminalException {
    byte[] bytes = new byte[2];
    this.readFully(bytes);
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
    return in.readShort();
  }
  
  /** Reads an unsigned 8 bit byte.
   *
   * @return    The 8 bit byte read.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public int readUnsignedByte() throws IOException, CardTerminalException {
    int ch = this.read();
    if (ch < 0)
      throw new EOFException();
    return ch;
  }
  
  /** Reads 16 bit unsigned short.
   *
   * @return    The read 16 bit unsigned short. 
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public int readUnsignedShort() throws IOException, CardTerminalException {
    byte[] bytes = new byte[2];
    this.readFully(bytes);
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
    return in.readUnsignedShort();
  }
  
  /** Skips the number of bytes specified.
   *
   * @param     n 
   *            The number of bytes to skip
   * @return    The number of bytes actually skipped.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public int skipBytes(int n) throws IOException, CardTerminalException {
    long length = file.getLength();

    if (filePointer + n > length)
      throw new EOFException("skip");

    filePointer += n;
    return n;
  }
  
  /** Reads a UTF formatted String.
   *
   * @return    A string with the read UTF string.
   * @exception java.io.IOException 
   *            Thrown If an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public String readUTF() throws IOException, CardTerminalException {
    return DataInputStream.readUTF(this);
  }
  
  /** Writes a byte of data. This method will block until the byte is
   * actually written.
   * 
   * @param     b 
   *            The byte to be written 
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void write(int b) throws IOException, CardTerminalException {
    byte[] data = new byte[1];

    data[0] = (byte)b;
    fileSystem.write(file.getPath(), filePointer, data);
    filePointer++;

    return;
  }
  
  /** Writes an array of bytes. Will block until the bytes are
   * actually written.
   *
   * @param     b 
   *            The data to be written 
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void write(byte b[]) throws IOException, CardTerminalException {
    fileSystem.write(file.getPath(), filePointer, b);
    filePointer += b.length;

    return;
  }
  
  /** Writes a slice of a byte array.
   * 
   * @param     b 
   *            The data to be written 
   * @param     off 
   *            The start offset in the data 
   * @param     len 
   *            The number of bytes that are written 
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void write(byte b[], int off, int len) 
    throws IOException, CardTerminalException {
      byte[] data = new byte[len];

      System.arraycopy(b, off, data, 0, len);
      this.write(data);

      return;
  }
  
  /** Writes a boolean.
   *
   * @param     v 
   *            The boolean value
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void writeBoolean(boolean v) 
    throws IOException, CardTerminalException {
      this.write(v ? 1 : 0);
  }
  
  /** Writes a byte.
   *
   * @param     v 
   *            The byte to write.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void writeByte(int v) 
    throws IOException, CardTerminalException {
      this.write(v);
  }
  
  /** Write a String as a sequence of bytes.
   *
   * @param     s 
   *            The String to write.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void writeBytes(String s) throws IOException, CardTerminalException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(baos);
    out.writeBytes(s);
    this.write(baos.toByteArray());
  }
  
  /** Writes a character.
   *
   * @param     v 
   *            The char to write
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void writeChar(int v) 
    throws IOException, CardTerminalException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(2);
      DataOutputStream out = new DataOutputStream(baos);
      out.writeChar(v);
      this.write(baos.toByteArray());
  }

  /** Writes a String as a sequence of chars.
   *
   * @param     s 
   *            The String to write.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void writeChars(String s) throws IOException, CardTerminalException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(2);
    DataOutputStream out = new DataOutputStream(baos);
    out.writeChars(s);
    this.write(baos.toByteArray());
  }
  
  /** Writes a double.
   *
   * @param     v 
   *            The double to write.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void writeDouble(double v) throws IOException, CardTerminalException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(2);
    DataOutputStream out = new DataOutputStream(baos);
    out.writeDouble(v);
    this.write(baos.toByteArray());
  }
  
  /** Writes a float.
   *
   * @param     v 
   *            The float to write
   * @exception java.io.IOException 
   *            Thrown If an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void writeFloat(float v) throws IOException, CardTerminalException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(2);
    DataOutputStream out = new DataOutputStream(baos);
    out.writeFloat(v);
    this.write(baos.toByteArray());
  }
  
  /** Write an integer.
   *
   * @param     v 
   *            The integer to write.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void writeInt(int v) 
    throws IOException, CardTerminalException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
      DataOutputStream out = new DataOutputStream(baos);
      out.writeInt(v);
      this.write(baos.toByteArray());
  }
  
  /** Writes a long.
   *
   * @param     v 
   *            The long to write.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void writeLong(long v) 
    throws IOException, CardTerminalException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
      DataOutputStream out = new DataOutputStream(baos);
      out.writeLong(v);
      this.write(baos.toByteArray());
  }
  
  /** Writes a short.
   * 
   * @param     v 
   *            The short to write
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void writeShort(int v) 
    throws IOException, CardTerminalException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(2);
      DataOutputStream out = new DataOutputStream(baos);
      out.writeShort(v);
      this.write(baos.toByteArray());
  }
  
  /** Writes a String in UTF format.
   *
   * @param     str 
   *            The String to write.
   * @exception java.io.IOException 
   *            Thrown if an I/O error has occurred. 
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown when the smart card has been removed.
   */
  public void writeUTF(String str) throws IOException, CardTerminalException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(2);
    DataOutputStream out = new DataOutputStream(baos);
    out.writeUTF(str);
    this.write(baos.toByteArray());
  }

  /** Let the super class free any resources.
    *
    * @exception java.io.IOException 
    *            An I/O error occurred during resource deallocation.
    */
  protected void finalize() throws IOException {
    super.finalize();
  }


} // class CardRandomByteAccess
