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

package opencard.opt.terminal;


/** 
 * A <tt>ISOCommandAPDU</tt> represents an ISO 7816-4 conformant
 * Command Protocol Data Unit which is send to a smart card. 
 * A response from the smart card is in turn represented by
 * a <tt>ResponsePDU</tt>.<p>
 *
 * An APDU according to ISO 7816-4 has the following format:
 * <pre>
 *                  HEADER         |        BODY
 *         CLA    INS    P1    P2  |  Lc    Data    Le
 *
 * </pre>
 * whereby some or all of the body parts are optional.
 *
 * @author   Stefan Hepper (sthepper@de.ibm.com)
 * @author   Dirk Husemann (hud@zurich.ibm.com)
 * @author   Reto Hermann  (rhe@zurich.ibm.com)
 * @author   Mike Wendler  (mwendler@de.ibm.com)
 * @version  $Id: ISOCommandAPDU.java,v 1.1.1.1 1999/10/05 15:08:48 damke Exp $
 *
 */

import opencard.core.terminal.CommandAPDU;

public class ISOCommandAPDU extends CommandAPDU {

  /** The length of the data field of the APDU. */
  protected int lc;

  /** The expected length of the ResponseAPDU. */
  protected int le;

  /** Constants for the 7 cases of ISO CommandAPDUs  */
  public final static int CASE_1       = 0x02; // 00000010
  public final static int CASE_2S      = 0x08; // 00001000
  public final static int CASE_2E      = 0x0C; // 00001100
  public final static int CASE_3S      = 0x20; // 00100000
  public final static int CASE_3E      = 0x30; // 00110000
  public final static int CASE_4S      = 0x80; // 10000000
  public final static int CASE_4E      = 0xC0; // 11000000

  /** Constants for addressing in the APDU header array.  */
  public final static int CLASS       = 0;
  public final static int INSTRUCTION = 1;
  public final static int P1          = 2;
  public final static int P2          = 3;


  /** 
   * Constructs a new ISO command APDU (ISO 7816-4 CASE 1).
   *
   * @param     classByte
   *            The <tt>CLA</tt> byte as specfied in ISO 7816-4.
   * @param     instruction
   *            The <tt>INS</tt> byte.
   * @param     p1
   *            Parameter byte <tt>P1</tt>.
   * @param     p2
   *            Parameter byte <tt>P2</tt>.
   */
  public ISOCommandAPDU(byte classByte, byte instruction, byte p1, byte p2) {
    this(4, classByte, instruction, p1, p2, null, -1);  // size = header length
  } // ISOCommandAPDU (byte, byte, byte, byte)

  /** 
   * Constructs a new ISO command APDU (ISO 7816-4 CASE 1).
   *
   * @param     size
   *            the size of the APDU buffer to create
   * @param     classByte
   *            The <tt>CLA</tt> byte as specfied in ISO 7816-4.
   * @param     instruction
   *            The <tt>INS</tt> byte.
   * @param     p1
   *            Parameter byte <tt>P1</tt>.
   * @param     p2
   *            Parameter byte <tt>P2</tt>.
   */
  public ISOCommandAPDU(int size, byte classByte, byte instruction, byte p1, byte p2) {
    this(size, classByte, instruction, p1, p2, null, -1);
  } // ISOCommandAPDU (int, byte, byte, byte, byte)


  /** 
   * Constructs a new ISO command APDU (ISO 7816-4 CASE 2).
   *
   * @param     classByte
   *            The <tt>CLA</tt> byte as specfied in ISO 7816-4.
   * @param     instruction
   *            The <tt>INS</tt> byte.
   * @param     p1
   *            Parameter byte <tt>P1</tt>.
   * @param     p2
   *            Parameter byte <tt>P2</tt>.
   * @param     le
   *            An integer value giving the expected length of the response APDU.
   *            This value can be in the range of -1 to 65536, where -1 means no
   *            length is expected and 0 means the maximum length supported
   *            is expected.
   */
  public ISOCommandAPDU(byte classByte, byte instruction, byte p1, byte p2, int le) {
    this(4+2, classByte, instruction, p1, p2, null, le);  // size = header length + le
  } // ISOCommandAPDU (byte, byte, byte, byte, int)


  /** 
   * Constructs a new ISO command APDU (ISO 7816-4 CASE 2).
   *
   * @param     size
   *            the size of the APDU buffer to create
   * @param     classByte
   *            The <tt>CLA</tt> byte as specfied in ISO 7816-4.
   * @param     instruction
   *            The <tt>INS</tt> byte.
   * @param     p1
   *            Parameter byte <tt>P1</tt>.
   * @param     p2
   *            Parameter byte <tt>P2</tt>.
   * @param     le
   *            An integer value giving the expected length of the response APDU.
   *            This value can be in the range of -1 to 65536, where -1 means no
   *            length is expected and 0 means the maximum length supported
   *            is expected.
   */
  public ISOCommandAPDU(int size, byte classByte, byte instruction, byte p1, byte p2, int le) {
    this(size, classByte, instruction, p1, p2, null, le);
  } // ISOCommandAPDU (int, byte, byte, byte, byte, int)



  /** 
   * Constructs a new ISO command APDU (ISO 7816-4 CASE 3).
   *
   * @param     classByte
   *            The <tt>CLA</tt> byte as specfied in ISO 7816-4.
   * @param     instruction
   *            The <tt>INS</tt> byte.
   * @param     p1
   *            Parameter byte <tt>P1</tt>.
   * @param     p2
   *            Parameter byte <tt>P2</tt>.
   * @param     data
   *            The command APDU data as a byte array. The length lc (which is
   *            part of the body of the APDU) is derived from the array length.
   */
  public ISOCommandAPDU(byte classByte, byte instruction, byte p1, byte p2, byte[] data) {
    this(data.length+5+4, classByte, instruction, p1, p2, data, -1);  // 5 = max overhead for coding data; 4 = header bytes
  } // ISOCommandAPDU (byte, byte, byte, byte, byte)

  /** 
   * Constructs a new ISO command APDU (ISO 7816-4 CASE 3).
   *
   * @param     size
   *            the size of the APDU buffer to create
   * @param     classByte
   *            The <tt>CLA</tt> byte as specfied in ISO 7816-4.
   * @param     instruction
   *            The <tt>INS</tt> byte.
   * @param     p1
   *            Parameter byte <tt>P1</tt>.
   * @param     p2
   *            Parameter byte <tt>P2</tt>.
   * @param     data
   *            The command APDU data as a byte array. The length lc (which is
   *            part of the body of the APDU) is derived from the array length.
   */
  public ISOCommandAPDU(int size, byte classByte, byte instruction, byte p1, byte p2, byte[] data) {
    this(size, classByte, instruction, p1, p2, data, -1);
  } // ISOCommandAPDU (int, byte, byte, byte, byte, byte)


  /** 
   * Constructs a new ISO command APDU (ISO 7816-4 CASE 4).
   *
   * @param     classByte
   *            The <tt>CLA</tt> byte as specfied in ISO 7816-4.
   * @param     instruction
   *            The <tt>INS</tt> byte.
   * @param     p1
   *            Parameter byte <tt>P1</tt>.
   * @param     p2
   *            Parameter byte <tt>P2</tt>.
   * @param     data
   *            The command APDU data as a byte array. The length lc (which is
   *            part of the body of the APDU) is derived from the array length.
   * @param     le
   *            An integer value giving the expected length of the response APDU.
   *            This value can be in the range of -1 to 65536, where -1 means no
   *            length is expected and 0 means the maximum length supported
   *            is expected.
   */
  public ISOCommandAPDU(byte classByte, byte instruction, byte p1, byte p2, byte[] data, int le) {
    this(data.length+5+4, classByte, instruction, p1, p2, data, le);   // 5 = max overhead for coding data; 4 = header bytes
  } // ISOCommandAPDU (byte, byte, byte, byte, byte, int)



  /** 
   * Constructs a new ISO command APDU (ISO 7816-4 CASE 4).
   *
   * @param     size
   *            the size of the APDU buffer to create
   * @param     classByte
   *            The <tt>CLA</tt> byte as specfied in ISO 7816-4.
   * @param     instruction
   *            The <tt>INS</tt> byte.
   * @param     p1
   *            Parameter byte <tt>P1</tt>.
   * @param     p2
   *            Parameter byte <tt>P2</tt>.
   * @param     data
   *            The command APDU data as a byte array. The length lc (which is
   *            part of the body of the APDU) is derived from the array length.
   * @param     le
   *            An integer value giving the expected length of the response APDU.
   *            This value can be in the range of -1 to 65536, where -1 means no
   *            length is expected and 0 means the maximum length supported
   *            is expected.
   */
  public ISOCommandAPDU(int size, byte classByte, byte instruction, byte p1, byte p2, byte[] data, int le) {
    super(size);
    // initialize properly for encoding
    this.le = le;
    this.lc = (data == null ? 0 : data.length);
    // encode
    byte[] body = this.encode(data);
    int L = (body == null ? 0 : body.length);
    // fill buffer
    this.apdu_buffer[CLASS] = classByte;
    this.apdu_buffer[INSTRUCTION] = instruction;
    this.apdu_buffer[P1] = p1;
    this.apdu_buffer[P2] = p2;
    if (body != null)
      System.arraycopy(body, 0, this.apdu_buffer, 4, L);

    this.apdu_length = 4+L;
  } // ISOCommandAPDU (int, byte, byte, byte, byte, byte, int)


  /** 
   * Gets the class byte.
   *
   * @return Class byte of the APDU.
   */
  public byte getCLA() {
    return this.apdu_buffer[CLASS];
  } // getCLA

  /** 
   * Gets the instruction byte.
   *
   * @return A byte value.
   */
  public byte getINS() {
    return this.apdu_buffer[INSTRUCTION];
  } // getINS

  /** 
   * Gets the P1 byte.
   *
   * @return A byte value.
   */
  public byte getP1() {
    return this.apdu_buffer[P1];
  } // getP1

  /** 
   * Gets the P2 byte.
   *
   * @return A byte value.
   */
  public byte getP2() {
    return this.apdu_buffer[P2];
  } // getP2


  /** 
   * Gets the length <tt>lc</tt> of the data.
   *
   * @return An integer value giving the length. The value 0
   *         indicates that there is no body.<br>
   */
  public int getLC() {
    return this.lc;
  } // getLC


  /** 
   * Gets the expected length <tt>le</tt> of the response APDU.
   *
   * @return An integer value giving the length. The value -1
   *         indicates that no value is specified.
   */
  public int getLE() {
    return this.le;
  } // getLE


  /** 
   * Gets the <tt>CASE</tt> of this <tt>ISOCommandAPDU</tt>. ISO 7816-4
   * distinguishes 7 cases. In addition, coding can be proprietary (e.g.,
   * in the context of secure messaging).
   * <UL>
   *   <LI> Case_1:       CLA INS P1 P2
   *   <LI> Case_2S/E:    CLA INS P1 P2 Le (short/extended)
   *   <LI> Case_3S/E:    CLA INS P1 P2 Lc Data (short/extended)
   *   <LI> Case_4S/E:    CLA INS P1 P2 Lc Data Le (short/extended)
   * </UL>
   * 
   * @return An integer value indicating the case.
   */
  public int getIsoCase() {
    boolean shortCase=(this.lc<256)&&(this.le<256);

    if (this.lc <= 0) { // there is no data & no Lc
      if (this.le < 0) // there is no length expected
        return CASE_1;
      else // there is a length expected
        return (shortCase ? CASE_2S : CASE_2E);
    } 
    else { // there is data & hence Lc
      if (this.le < 0) // there is no length expected
        return (shortCase ? CASE_3S : CASE_3E);
      else // there is a length expected
        return (shortCase ? CASE_4S : CASE_4E);
    }
  } // getIsoCase


  /**
   * Appends the given byte array to the data field of the APDU.
   *
   * @param bytes the byte array to be appended
   * @exception java.lang.IndexOutOfBoundsException
   *            The buffer size is exceeded or a le exists in buffer.
   */
  public void append(byte[] bytes)
    throws IndexOutOfBoundsException {
    if ( le != -1 )  // there exists already an le in byte buffer
      throw new IndexOutOfBoundsException("An le value exists in APDU buffer, therefore no append is possible");      // so appending bytes makes no sense
    super.append(bytes);
    lc += bytes.length;
  }

  /**
   * Appends the given byte to the buffered APDU.
   *
   * @param b the byte to be appended
   *
   * @exception java.lang.IndexOutOfBoundsException
   *            The buffer size is exceeded or a le exists in buffer.
   */
  public void append(byte b)
    throws IndexOutOfBoundsException {
    if ( le != -1 )  // there exists already an le in byte buffer
      throw new IndexOutOfBoundsException("An le value exists in APDU buffer, therefore no append is possible");      // so appending bytes makes no sense
    super.append(b);
    lc++;
  }


  /** 
   * Gets a string representation of this APDU.
   *
   * @return A string describing this APDU.<br>
   *         <b>NOTE:</b> For non-ISO conform Command APDU's
   *         the body of the Command APDU is not formatted.
   */
  public String toString() {
    StringBuffer ret = new StringBuffer("APDU_Buffer = ");
    ret.append(makeHex(getBytes()));
    ret.append(" (hex) | lc = ");
    ret.append(lc);
    ret.append(" | le = ");
    ret.append(le);

    // make hex representation of byte array
    return ret.toString();
  }




  // private methods -----------------------------------------------


  /** 
   * Internal method: encode the body of this ISOCommandAPDU into a byte array.
   */
  private byte[] encode(byte[] data) {
    int lc = (data==null ? 0 : data.length);
    int L  = 0;

    // decide whether short or extended encoding is to be used
    boolean useShort = (lc<256)&&(le<256);

    // compute total length of body
    L += lc; // length due to data bytes

    if (lc>0) // need to code Lc
      L = (useShort ? L+1 : L+3); // coding Lc as 1 or 3 bytes

    if (le>=0) // need to code Le
      L = (useShort ? L+1 : L+2); // coding Le as 1 or 2 bytes

    if (L==0) 
      return null;

    // perform encoding
    byte[] body = new byte[L];
    int l=0;

    if (lc>0) { // need to code Lc

      if (useShort) // Lc fits in a single byte
        body[l++]=(byte)(lc&0xFF);
      else { // Lc requires 3 bytes
        body[l]  =(byte)0x00; // marker indicating extended encoding
        body[l+1]=(byte)((lc>>8)&0xFF);
        body[l+2]=(byte)(lc&0xFF);
        l+=3;
      }

      // code data
      System.arraycopy(data, 0, body, l, lc);
      l += lc;
    } // if (lc > 0)

    if (le>=0) { // need to code Le

      if (useShort) // Le fits in a single byte
        body[l++]=(byte) (le&0xFF); // handle coding of 256
      else { // Le fits in two bytes
        body[l]  =(byte)((le>>8)&0xFF);
        body[l+1]=(byte)(le&0xFF);
      }

    } // if (le >= 0)
    return body;
  } // encode

  /**
   * Make hex string from byte array.
   *
   * @param buffer  byte data to print
   * @return hex string
   */
  private String makeHex(byte[] buffer) {
    byte current;
    int length = buffer.length;
    String blank = "";  // it's easier to change
    StringBuffer ret = new StringBuffer(2*length);

    // do for each half byte
    for(int i=0;i<(2*length);i++)
      {
	// mask half byte and move it to the right
	current = i%2==1 ? (byte) (buffer[(i/2)] & 0x0F)
	  : (byte) ((buffer[(i/2)] >> 4) & 0x0F);
	
	// convert half byte to ASCII char		       
	ret.append((char) (current < 0x0A ? current+0x30 : current+0x37) + (i%2==1 ? blank : ""));
      }
    return ret.toString();
  }	
  
  

}

