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

package opencard.core.terminal;

import opencard.core.util.HexString;

/**
 * An <tt>APDU</tt> represents a Application Protocol Data Unit which is the
 * basic unit of communication with a smart card.
 *
 * @see opencard.core.terminal.CommandAPDU
 * @see opencard.core.terminal.ResponseAPDU
 *
 * @author   Dirk Husemann       (hud@zurich.ibm.com)
 * @author   Reto Hermann        (rhe@zurich.ibm.com)
 * @author   Mike Wendler        (mwendler@de.ibm.com)
 * @author   Stephan Breideneich (sbreiden@de.ibm.com)
 * @version  $Id: APDU.java,v 1.1.1.1 1999/10/05 15:34:31 damke Exp $
 *
 */

public abstract class APDU {

  /** A buffer to hold the re-usable command APDU. */
  protected byte[] apdu_buffer  = null;

  /** The length of the command APDU currently in the buffer. */
  protected int    apdu_length  = 0;


  /**
   * Creates a new re-usable APDU and initializes it with 
   * the given buffer. The internal buffer's length is set to the 
   * length of the buffer passed.
   *
   * @param buffer   the byte array to be used for holding the APDU
   *
   * @see #getLength
   */
  public APDU(byte[] buffer) {
    apdu_buffer = buffer;
    apdu_length = buffer.length;
  }


  /**
   * Creates a new re-usable APDU and initializes it with 
   * the given buffer.
   * The buffer is assumed to hold an APDU. The length of the
   * internally buffered APDU is set to <tt>length</tt>.
   *
   * @param buffer  the byte array to be used for holding the APDU
   * @param length  the length of the APDU currently in the buffer
   *
   * @exception IndexOutOfBoundsException
   *    <tt>length</tt> exceeds the size of the array <tt>bytes</tt>.
   *
   * @see #getLength
   */
  public APDU(byte[] buffer, int length) {
    apdu_buffer = buffer;
    apdu_length = length;
  }


  /**
   * Creates a new re-usable APDU with a given buffer size.
   * A new buffer with the given size is allocated. The length of the
   * internally buffered APDU is set to 0.
   *
   * @param size  the size of the buffer to create
   *
   * @see #getLength
   */
  public APDU(int size) {
    apdu_buffer = new byte[size];
    apdu_length = 0;
  }


  /**
   * Appends the given byte array to the internally buffered APDU.
   *
   * @param bytes the byte array to be appended
   *
   * @exception java.lang.IndexOutOfBoundsException
   *            The buffer size is exceeded.
   */
  public void append(byte[] bytes)
    throws IndexOutOfBoundsException {
    System.arraycopy(bytes, 0, apdu_buffer, apdu_length, bytes.length);
    apdu_length += bytes.length;
  }


  /**
   * Appends the given byte to the internally buffered APDU.
   *
   * @param b the byte to be appended
   *
   * @exception java.lang.IndexOutOfBoundsException
   *            The buffer size is exceeded.
   */
  public void append(byte b)
    throws IndexOutOfBoundsException {

    apdu_buffer[apdu_length++] = b;
  }


  /**
   * Returns the internal APDU buffer.
   * This method allows complex manipulations of the buffered APDU,
   * for example MAC calculation. If the length of the APDU is changed
   * by such an operation, <tt>setLength</tt> has to be used to store
   * the new length.
   *
   * @return  the buffer that holds the current APDU
   *
   * @see #setLength
   */
  final public byte[] getBuffer() {
    return apdu_buffer;
  }


  /**
   * Gets the byte at the specified position in the buffer.
   * The byte is converted to a positive integer in the range 0..255.
   * This method can only be used to access the APDU currently stored.
   * It is not possible to read beyond the end of the APDU.
   *
   * @param index   the position in the buffer
   * @return        the value at the given position,
   *                or -1 if the position is invalid
   *
   * @see #setByte
   * @see #getLength
   */
  final public int getByte(int index) {
    if (index >= apdu_length)
      return -1;                // read beyond end of APDU

    return (((int)apdu_buffer[index]) & 255);
  }


  /**
   * Returns a byte array holding the buffered APDU.
   * The byte array returned gets allocated with the exact size of the
   * buffered APDU. To get direct access to the internal buffer, use
   * <tt>getBuffer</tt>.
   *
   * @return  the buffered APDU, copied into a new array
   *
   * @see #getBuffer
   */
  final public byte[] getBytes() {
    byte[] apdu = new byte[apdu_length];
    System.arraycopy(apdu_buffer, 0, apdu, 0, apdu_length);
    return apdu;
  }


  /**
   * Returns the length of the buffered APDU.
   *
   * @return  the length of the APDU currently stored
   */
  final public int getLength() {
    return apdu_length;
  }


  /**
   * Sets the byte at the specified position in the buffer.
   * The byte is passed as an integer, for consistence with <tt>getByte</tt>.
   * This method can only be used to <i>modify</i> an APDU already stored.
   * It is not possible to set bytes beyond the end of the current APDU.
   * The method will behave as a no-op if this happens.
   * Use <tt>append(byte)</tt> to extend the APDU.
   *
   * @param index   the position in the buffer
   * @param value   the byte to store there
   *
   * @see #getByte
   * @see #append(byte)
   */
  final public void setByte(int index, int value) {
    if (index < apdu_length)
      apdu_buffer[index] = (byte) value;
  }


  /**
   * Sets the length of valid range within the APDU buffer.
   * This method can be used to cut off the end of the APDU.
   * It can also be used to increase the size of the APDU. In this case,
   * it is the caller's responsibility to fill the additional bytes with
   * useful information.
   *
   * @param length new length of the valid range
   *
   * @exception java.lang.IndexOutOfBoundsException
   *            thrown when the buffer size is exceeded
   */
  final public void setLength(int length)
    throws IndexOutOfBoundsException {

    if (length > apdu_buffer.length)
      throw new IndexOutOfBoundsException();
    apdu_length = length;
  }


  /**
   * Returns a human-readable string representation of this APDU.
   * This method does not use caching but creates the string from
   * scratch on each invocation.
   *
   * @return a hex dump of the APDU currently stored
   */
  public String toString() {

    StringBuffer sb = new StringBuffer("");
    sb.append(super.toString());
    sb.append("\n");
    sb.append(HexString.dump(this.apdu_buffer,0,this.apdu_length));
    return sb.toString();
  }

}
