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


/**
 * Represents a command APDU that can be modified.
 *
 * @author  Thomas Schaeck (schaeck@de.ibm.com)
 * @author  Roland Weber   (rolweber@de.ibm.com)
 * @author  Mike Wendler   (mwendler@de.ibm.com)
 * @version $Id: CommandAPDU.java,v 1.1.1.1 1999/10/05 15:34:31 damke Exp $
 */

public class CommandAPDU extends APDU {

  /**
   * Creates a new re-usable command APDU and initializes it with 
   * the given buffer. The internal buffer's length is set to the 
   * length of the buffer passed.
   *
   * @param buffer   the byte array to be used for holding the APDU
   *
   * @see #getLength
   */
  public CommandAPDU (byte[] buffer) {
    super(buffer);
  }


  /**
   * Creates a new re-usable command APDU and initializes it with 
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
  public CommandAPDU (byte[] buffer, int length)
    throws IndexOutOfBoundsException {

    super(buffer, length);
  }


  /**
   * Creates a new re-usable command APDU with a given buffer size.
   * A new buffer with the given size is allocated. The length of the
   * internally buffered APDU is set to 0.
   *
   * @param size  the size of the buffer to create
   *
   * @see #getLength
   */
  public CommandAPDU (int size) {
    super(size);
  }

}
