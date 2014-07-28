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

import opencard.core.util.HexString;

/** <tt>T1Block</tt>
 *
 * represents T1 block (see ISO7816-3)
 *
 * @author  Stephan Breideneich (sbreiden@de.ibm.com)
 * @version $Id: T1Block.java,v 1.2 1999/10/20 13:15:00 damke Exp $
 */
public class T1Block {

  /**
   * T1 flags (see ISO7816-3)
   */

  /** use LDR (XOR) algorithm for EDC byte */
  public static final int EDC_LDR                    = 1;

  /** use CRC16 algorithm for EDC byte - not implemented */
  public static final int EDC_CRC                    = 2;

  /** indicates error free operation */
  public static final int ERROR_NONE                 = 0;

  /** indicates EDC error */
  public static final int ERROR_EDC                  = 1;

  /** indicates other error */
  public static final int ERROR_OTHER                = 2;

  /** S_RESYNCH_REQUEST */
  public static final int S_RESYNCH_REQUEST          = 0x00;

  /** S_RESYNCH_RESPONSE */
  public static final int S_RESYNCH_RESPONSE         = 0x20;

  /** S_IFS_REQUEST */
  public static final int S_IFS_REQUEST              = 0x01;

  /** S_IFS_RESPONSE */
  public static final int S_IFS_RESPONSE             = 0x21;

  /** S_ABORT_REQUEST */
  public static final int S_ABORT_REQUEST            = 0x02;

  /** S_ABORT_RESPONSE */
  public static final int S_ABORT_RESPONSE           = 0x22;

  /** S_WTX_REQUEST */
  public static final int S_WTX_REQUEST              = 0x03;

  /** S_WTX_RESPONSE */
  public static final int S_WTX_RESPONSE             = 0x23;

  /** S_VPP_STATE_ERR_RESPONSE */
  public static final int S_VPP_STATE_ERROR_RESPONSE = 0x24;


  /**
   * T1 blocktype masks
   */

  /** I-block */
  public static final int I_BLOCK                    = 0x00;

  /** R-block */
  public static final int R_BLOCK                    = 0x80;

  /** S-block */
  public static final int S_BLOCK                    = 0xC0;


  /** Node address (NAD) */
  private byte   NAD = 0;

  /** Protocol control byte (PCB) */
  private byte   PCB = 0;

  /** Length (LEN) of information field */
  private int    LEN = 0; // PTR 0197: int instead byte to avoid overflow

  /** Information field (DAT) */
  private byte[] DAT = null;

  /** EDC information */
  private int    EDC = 0;

  /** used EDC algorithm: EDC_LDR or EDC_CRC */
  private int    algForEDC = 0;


  /** <tt>Constructor</tt>
   *
   * @param src
   *        source-address for T1 block
   * @param dest
   *        destination-address for T1-Block
   * @param pcb
   *        protocol control byte
   * @param dat
   *        info data within T1 block
   *        set to null for no data available
   * @param edcInfo
   *        EDC_LDR (XOR) or EDC_CRC
   */
  public T1Block(int src, int dest, int pcb, byte[] dat, int edcInfo)
         throws T1BlockLengthException, T1BlockEDCErrorException {

    NAD = (byte)(((dest & 0x7) << 4) + (src & 0x7));
    PCB = (byte)pcb;

    if (dat != null) {
      if (dat.length > 254)
        throw new T1DataPacketTooLongException("info-field of I-BLOCK must not be greater than 254 bytes");

      LEN = dat.length;
    } else
      LEN = 0;

    DAT = dat;
    algForEDC = edcInfo;
    EDC = calcEDC();
  }

  /** create Block object from raw T1-Block */
  public T1Block(byte[] rawBytes, int edcInfo)
         throws T1BlockLengthException, T1BlockEDCErrorException {
    NAD = rawBytes[0];
    PCB = rawBytes[1];
    LEN = rawBytes[2]&0xff;

    // save type of EDC algorithm
    algForEDC = edcInfo;

    // length of rawBytes correct? (LDR-mode)
    if (rawBytes.length == LEN + 4) {
      DAT = new byte[LEN];
      System.arraycopy(rawBytes, 3, DAT, 0, LEN);
      EDC = rawBytes[rawBytes.length - 1];
    } else
    // length of rawBytes correct? (CRC-mode)
    if (rawBytes.length == LEN + 5) {

      disableCRC();

      DAT = new byte[LEN];
      System.arraycopy(rawBytes, 3, DAT, 0, LEN);
      EDC = (rawBytes[rawBytes.length - 2] << 8) + rawBytes[rawBytes.length - 1];

    } else
      throw new T1BlockLengthException("block length mismatch detected");

    if (!checkEDC())
      throw new T1BlockEDCErrorException("EDC error detected");
  }

  /** calcEDC
   *
   * calculates the EDC-field according to the used EDC algorithm
   */
  public int calcEDC() {
    int edc = 0;

    // if CRC is used throw exception
    disableCRC();

    if (algForEDC == EDC_LDR) {
      edc = 0;
      edc = NAD ^ PCB ^ (byte)LEN;

      if (DAT != null)
        for (int i=0; i<DAT.length; i++)
          edc = edc ^ DAT[i];

    }
      return edc;
  }

  /** checks the correctness of the given EDC byte (true, if EDC is correct) */
  public boolean checkEDC() {
    boolean retVal = false;

    return (EDC == calcEDC());
  }

  /** returns byte array with raw block data */
  public byte[] getBlock() {
    byte[] block;

    // if CRC is used throw exception
    disableCRC();

    if (DAT != null) {
      block = new byte[DAT.length + 4];
      System.arraycopy(DAT, 0, block, 3, DAT.length);
    } else
      block = new byte[4];

    block[0] = NAD;
    block[1] = PCB;
    block[2] = (byte)LEN;
    block[block.length - 1] = (byte)(0xFF & EDC); // only LDR!!!!!

    return block;
  }

  public int getBlockType() throws T1UnknownBlockException {
    if ((~PCB & 0x80) != 0)
      return I_BLOCK;

    if ((PCB & 0xC0) == 0x80)
      return R_BLOCK;

    if ((PCB & 0xC0) == 0xC0)
      return S_BLOCK;

    throw new T1UnknownBlockException();
  }

  public byte getNAD() {
    return NAD;
  }

  public int getSourceID() {
    return (getNAD() & 0x07);
  }

  public int getDestID() {
    return ((getNAD() >> 4) & 0x07);
  }

  public byte getPCB() {
    return PCB;
  }

  public int getLEN() {
    return LEN;
  }

  public byte[] getDATA() {
    return DAT;
  }

  public int getEDC() {
    return EDC;
  }

  public int getEDCAlgorithm() {
    return algForEDC;
  }

  /** <tt>getControlBits</tt> returns block-specific controlbits without the blocktype
   */
  public int getControlBits() throws T1Exception {
    switch(getBlockType()) {
      case I_BLOCK:
        return getPCB() & 0x7F;

      case R_BLOCK:
      case S_BLOCK:
        return getPCB() & 0x3F;
    }
    return 0;
  }

  /** disableCRC is used as long as CRC algorithm is not implemented */
  private void disableCRC() {
    if (algForEDC == EDC_CRC)
      throw new T1BlockNotImplementedFeatureException("CRC-algorithm is not implemented!");
  }

  /** toString returns informations about this block object (not yet optimized) */
  public String toString() {
    int blockType;

    StringBuffer info = new StringBuffer("T1-BLOCK\n--------\n");

    // source- and dest-ID
    info.append("sourceID   = " + getSourceID() + "\n");
    info.append("destID     = " + getDestID() + "\n");
    info.append("PCB        = " + HexString.hexify(PCB) + " (" + Integer.toBinaryString(PCB & 0xFF) + ")\n");

    try {
      blockType = getBlockType();

      // display block-specific data
      switch(blockType) {
        case I_BLOCK:
          info.append("blocktype  = I-BLOCK\n");
          info.append("  N(S)     = " + (PCB >> 6) + "\n");
          info.append("  chaining = " + (((PCB & 0x20) == 0x20) ? "yes\n" : "no\n"));
          info.append("  infolen  = " + LEN + "\n");
          if (getLEN() > 0)
            info.append("  infodata = " + HexString.hexify(DAT) + "\n");
          break;

        case R_BLOCK:
          info.append("blocktype  = R-BLOCK\n");
          info.append("  status   = ");
          switch(PCB & 0x0F) {
            case 0x00: info.append("error-free\n"); break;
            case 0x01: info.append("EDC or parity error\n"); break;
            case 0x02: info.append("other errors\n"); break;
            default:   info.append("unknown option\n"); break;
          }
          info.append("  N(R)     = " + ((PCB >> 4) & 0x01) + "\n");
          break;

        case S_BLOCK:
          info.append("blocktype  = S-BLOCK\n");
          switch(PCB & 0x3F) {
            case S_RESYNCH_REQUEST:
              info.append("  status   = RESYNCH request\n");
              break;
            case S_RESYNCH_RESPONSE:
              info.append("  status   = RESYNCH response\n");
              break;
            case S_IFS_REQUEST:
              info.append("  status   = IFS request\n");
              break;
            case S_IFS_RESPONSE:
              info.append("  status   = IFS response\n");
              break;
            case S_ABORT_REQUEST:
              info.append("  status   = ABORT request\n");
              break;
            case S_ABORT_RESPONSE:
              info.append("  status   = ABORT response\n");
              break;
            case S_WTX_REQUEST:
              info.append("  status   = WTX request\n");
              break;
            case S_WTX_RESPONSE:
              info.append("  status   = WTX response\n");
              break;
            case S_VPP_STATE_ERROR_RESPONSE:
              info.append("  status   = VPP state error response\n");
              break;
            default:
          }
          info.append("  infolen  = " + LEN + "\n");
          if (getLEN() > 0)
            info.append("  infodata = " + HexString.hexify(DAT) + "\n");
          break;
      }

      // EDC: used algorithm and result
      info.append("EDC algrthm= " + ((algForEDC == EDC_LDR) ? "LDR\n" : "CRC16\n"));
      info.append("saved EDC  = " + ((algForEDC == EDC_LDR) ? HexString.hexify(EDC) : HexString.hexifyShort(EDC)));
      info.append(" (calculated: " + ((algForEDC == EDC_LDR) ? HexString.hexify(calcEDC()) : HexString.hexifyShort(calcEDC())) + ")\n");

      info.append("raw bytes  = " + HexString.hexify(getBlock()) + "\n");


    } catch (T1UnknownBlockException e) {
      info.append("blocktype= UNKNOWN");
    }

    info.append("\n");

    return info.toString();
  }

  public int getSourceAddress() {
    return getNAD() & 0xF;
  }

  public int getDestinationAddress() {
    return (getNAD() >> 4) & 0xF;
  }

  // only valid for R-Blocks
  public int getRequestedSequenceNumber() throws T1Exception {
    return (getControlBits() >> 4);
  }
}
