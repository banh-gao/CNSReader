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

import opencard.core.OpenCardRuntimeException;

/**
 * A <tt>ResponseAPDU</tt> represents a Response Application Protocol Data Unit
 * received from the smart card in response to a previous <tt>CommandAPDU</tt>.
 * A response APDU consists of an optional body and a mandatory trailer.
 *
 * @author Dirk Husemann  (hud@zurich.ibm.com)
 * @author Peter Trommler (trp@zurich.ibm.com)
 * @author Reto Hermann   (rhe@zurich.ibm.com)
 * @author Mike Wendler   (mwendler@de.ibm.com)
 * @version  $Id: ResponseAPDU.java,v 1.1.1.1 1999/10/05 15:34:31 damke Exp $
 *
 */

public class ResponseAPDU extends APDU {

  /**
   * Creates a new object of this type and initializes it with 
   * the given apdu buffer. The internal buffer's length is set to the 
   * length of the buffer passed.
   *
   * @param apdu   the byte array to be used for holding the APDU
   *
   * @exception OpencardRuntimeException
   *                 thrown when apdu is invalid
   *
   * @see #getLength
   */
  public ResponseAPDU (byte[] apdu) {

    super(apdu);
    
    if (apdu.length < 2)
      throw new OpenCardRuntimeException("invalid response adpu, "
                                         + "length must be at least 2 bytes");
    apdu_length = apdu.length;
  }


  /**
   * Constructs an object of this type with the given buffer size.
   * A new buffer with the given size is allocated. The length of the
   * internally buffered APDU is set to 0.
   *
   * @param size  the size of the buffer to create
   *
   * @exception OpencardRuntimeException
   *              thrown when apdu is invalid
   *
   * @see #getLength
   */
  public ResponseAPDU (int size) {
    super(size);

    if (size < 2)
      throw new OpenCardRuntimeException("invalid response adpu, "
                                         + "length must be at least 2 bytes");
  }


  /**
   * Gets the data fields of the APDU.
   *
   * @return a byte array containing the APDU data field
   */
  public byte[] data() {
    if (apdu_length > 2 ) {
      byte[] data = new byte[apdu_length - 2];
      System.arraycopy(apdu_buffer, 0, data, 0, apdu_length - 2);
      return data;
    }
    else
      return null;
  } // data


  /**
   * Gets the value of <tt>SW1</tt> and <tt>SW2</tt> as a short integer.
   * It is computed as: (((sw1<<8)&0xFF00) | (sw2&0xFF)).
   *
   * @return    The value (((sw1<<8)&0xFF00) | (sw2&0xFF)) as integer.
   */
  public final int sw() {
      return (((sw1() << 8) & 0xFF00) | (sw2() & 0xFF));
  }


  /**
   * Gets the value of <tt>SW1</tt> as a byte.
   *
   * @return    The value of SW1
   */
  public final byte sw1() {
    return apdu_buffer[apdu_length - 2];
  }


  /**
   * Gets the value of <tt>SW1</tt> as a byte.
   *
   * @return    The value of SW2
   */
  public final byte sw2() {
    return apdu_buffer[apdu_length - 1];
  }

}
