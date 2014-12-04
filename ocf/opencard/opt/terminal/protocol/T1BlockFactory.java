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

package opencard.opt.terminal.protocol;

/** <tt>T1BlockFactory</tt>
 *
 * creates T1Block-objects - for detailed informations see ISO-7816-3.
 *
 * @author  Stephan Breideneich (sbreiden@de.ibm.com)
 * @version $Id: T1BlockFactory.java,v 1.1.1.1 1999/10/05 15:08:48 damke Exp $
 *
 */
public class T1BlockFactory {

  /** <tt>createIBlock</tt>
   *
   * creates I-Block according to ISO7816-3
   *
   * @param sourceID
   *        the sourceID of the frame for the NAD-byte (0-7)
   * @param destID
   *        the destID of the frame for the NAD-byte (0-7)
   * @param edcAlg
   *        the used algorithm for calculation of the EDC byte
   *        possible values: T1Block.USE_LDR, T1Block.USE_CRC
   * @param sendSequenceNumber
   *        the sequence number of the frame
   *        internal used value is (sendSequenceNumber mod 2)
   * @param moreDataBit
   *        true for chained block (not implemented), false for normal operation
   * @param data
   *        application data
   * @see   T1Block
   */
  public static T1Block createIBlock(int sourceID, int destID, int edcAlg, 
                                     int sendSequenceNumber, boolean moreDataBit, byte[] data) 
                                     throws T1Exception {

    // set sendSequenceBit
    int pcb = 0xFF & ((sendSequenceNumber % 2) << 6);

    // set moreDataBit
    if (moreDataBit)
      pcb = 0xFF & (pcb | 0x20);

    // create I-Block
    return new T1Block(sourceID, destID, pcb, data, edcAlg);
  }

  /** <tt>createRBlock</tt>
   *
   * creates R-Block according to ISO7816-3
   *
   * @param sourceID
   *        the sourceID of the frame for the NAD-byte (0-7)
   * @param destID
   *        the destID of the frame for the NAD-byte (0-7)
   * @param edcAlg
   *        the used algorithm for calculation of the EDC byte
   *        possible values: EDC_LDR, EDC_CRC
   * @param sequenceNumber
   *        the sequence number of the related frame
   *        internal used value is (sequenceNumber mod 2)
   * @param errInfo
   *        indicates the error
   *        possible values: T1Block.ERROR_NONE, T1Block.ERROR_EDC, T1Block.ERROR_OTHER
   * @see   T1Block
   */
  public static T1Block createRBlock(int sourceID, int destID, int edcAlg, int sequenceNumber, int errInfo) 
                        throws T1Exception {

    // setup PCB-byte for R-Block
    int pcb = 0xFF & (0x80 | ((sequenceNumber % 2) << 4));

    // add errInfo
    pcb = 0xFF & (pcb | errInfo);

    // create R-Block
    return new T1Block(sourceID, destID, pcb, null, edcAlg);
  }

  /** <tt>createSBlock</tt>
   *
   * creates S-Block according to ISO7816-3
   *
   * @param sourceID
   *        the sourceID of the frame for the NAD-byte (0-7)
   * @param destID
   *        the destID of the frame for the NAD-byte (0-7)
   * @param edcAlg
   *        the used algorithm for calculation of the EDC byte
   *        possible values: EDC_LDR, EDC_CRC
   * @param statusInfo
   *        indicates the error<br>
   *        possible values:<br>
   *        T1Block.S_RESYNCH_REQUEST, T1Block.S_RESYNCH_RESPONSE,<br>
   *        T1Block.S_IFS_REQUEST, T1Block.S_IFS_RESPONSE,<br>
   *        T1Block.S_ABORT_REQUEST, T1Block.S_ABORT_RESPONSE,<br>
   *        T1Block.S_WTX_REQUEST, T1Block.S_WTX_RESPONSE,<br>
   *        T1Block.S_VPP_STATE_ERROR_RESPONSE
   * @param data
   *        application data
   * @see   T1Block
   */
  public static T1Block createSBlock(int sourceID, int destID, int edcAlg, int statusInfo, byte[] data) 
                        throws T1Exception {

    // setup PCB-byte for S-Block
    int pcb = 0xC0;

    // add statusInfo
    pcb = 0xFF & (pcb | statusInfo);

    // create I-Block
    return new T1Block(sourceID, destID, pcb, data, edcAlg);
  }

}
