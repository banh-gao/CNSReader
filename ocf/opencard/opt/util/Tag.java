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


package opencard.opt.util;

/** This class represents Tags as defined in the Basic Encoding Rules
 * for ASN.1 defined in ISO 8825. A tag consists of two class bits (0 - 4),
 * a flag indicating wether the TLV is constructed or primitive.<p>
 *
 * The encoding is like this (C = class bit, c = composed flag, X = data bit):
 *
 * <pre>
 * Range from 0x0 - 0x1E:
 * C C c X X X X X
 * <br>
 * Range from 0x1F - 0x80:
 * C C c 1 1 1 1 1 0 X X X X X X X
 * <br>
 * Range from 0x81 - 0x4000:
 * C C c 1 X X X X 1 X X X X X X X 0 X X X X X X X
 * <br>
 * Range from 0x4001 - 0x200000:
 * C C c 1 X X X X 1 X X X X X X X 1 X X X X X X X 0 X X X X X X X
 * <br>
 * Range from 0x200001 - 0x10000000:
 * C C c 1 X X X X 1 X X X X X X X 1 X X X X X X X 0 X X X X X X X 0 X X X X X X X
 * </pre>
 * @author Thomas Schaeck
 * @version 0.5
 *
 * @see opencard.opt.util.TLV
 */

public class Tag {
  /** tag number */
  private int     tag;
  /** tag class, 0 - 4 */
  private byte    tagclass;
  /** constructed flag */
  private boolean constructed;

  /** Create a null tag.<p>
   */
  public Tag() {
    tag         = 0;
    tagclass    = 0;
    constructed = false;
  }

  /** Clone a tag.<p>
   *
   * @param t
   *        The <tt>Tag</tt> object to be cloned.
   */
  public Tag(Tag t) {
    this.tag         = t.tag;
    this.tagclass    = t.tagclass;
    this.constructed = t.constructed;
  }

  /** Creates a tag from a given tag value, class and constructed flag.<p>
   *
   * @param tag
   *        An integer representing the value of the tag.
   * @param tagClass
   *        A byte value representing the class of the tag.
   * @param constructed
   *        A boolean value <tt>true</tt> signals that the tag is
   *        constructed, <tt>false</tt> signals that the tag is
   *        primitive.
   */
  public Tag(int tag, byte tagClass, boolean constructed) {
    this.tag         = tag;
    this.tagclass    = tagClass;
    this.constructed = constructed;
  }

  /** Create a tag from binary representation.<p>
   *
   * @param binary
   *        The byte array from which the tag shall be generated.
   * @param offset
   *        An integer value giving the offset into the the byte array
   *        from where to start.
   */
  public Tag(byte[] binary, int[] offset) {
    fromBinary(binary, offset);
  }

  public Tag(byte[] binary) {
    int[] offset = new int[1];
    offset[0] = 0;
    fromBinary(binary, offset);
  }

  /** Return the number of bytes which are required to BER-code
   * the tag value.<p>
   *
   * @return An integer giving the number of bytes.
   */
  public int size() {
    if      (tag < 0x1F)       return 1;
    else if (tag < 0x80)       return 2;
    else if (tag < 0x4000)     return 3;
    else if (tag < 0x200000)   return 4;
    return 5;
  }

  /** Initialize the <tt>Tag</tt> object from a BER-coded
   * binary representation.<p>
   *
   * @param binary
   *        A byte array containing the BER-coded tag.
   * @param offset
   *        An integer giving an offset into the byte array
   *        from where to start.
   */
  public void fromBinary(byte[] binary, int[] offset) {
    // Get class of tag (encoded in bits 7,6 of first byte)
    tagclass = (byte) ((binary[offset[0]] & 0xC0) >>> 6);

    // Get constructed flag (encoded in bit 5 of first byte)
    if ((binary[offset[0]] & (byte) 0x20) == (byte) 0x20)
      constructed = true;           // This is a constructed TLV
    else
      constructed = false;          // This is a primitive TLV

    // Get tag number (encoded in bits 4-0 of first byte and optionally
    // several following bytes.
    tag = 0;
    if ((binary[offset[0]] & (byte) 0x1F) == (byte) 0x1F)
      // it's a multi byte tag
      do {
        offset[0]++;
        tag *= 128;
        tag += binary[offset[0]] & 0x7F;
      } while ((binary[offset[0]] & 0x80) == 0x80);
    else
      // it's a one byte tag
      tag = binary[offset[0]] & (byte) 0x1F;
    offset[0]++;
  }

  /**
   * Gets a byte array representing the tag.
   *
   * @return the tag as a byte array
   */
  public byte[] getBytes()
  {
    int[] offset = new int[1];
    offset[0] = 0;
    byte[] result = new byte[size()];
    toBinary(result, offset);
    return result;
  }

  /** Convert the tag to binary representation.<p>
   *
   * @param binary
   *        A byte array to which the BER-coded binary representation
   *        of the tag shall be written.
   * @param offset
   *        An integer value giving an offset into the byte array from
   *        where to start.
   */
  public void toBinary(byte[] binary, int[] offset) {
    byte classes[] = {(byte)0x00, (byte)0x40, (byte)0x80, (byte)0xC0};
    int count = 0;

    binary[offset[0]] |= classes[tagclass];  // encode class
    if (constructed)                   // encode constructed bit
      binary[offset[0]] |= 0x20;

    if (tag < 31)                      // encode tag number
      binary[offset[0]] |= (byte) tag;
    else {
      binary[offset[0]] |= 0x1F;
      for (count = this.size()-2; count > 0; count--) {
        offset[0]++;
        binary[offset[0]] = (byte) ( 0x80 | (( tag >> (count * 7)) & 0x7f));
      }
      offset[0]++;
      binary[offset[0]] = (byte) (tag & 0x7f);
    }
    offset[0]++;
  }

  /** Set the tag number, class and constructed flag of this
   * <tt>Tag</tt> to the given values.<p>
   *
   * @param tag
   *        An integer value giving the tag value.
   * @param tagclass
   *        A byte value giving the class.
   * @param constructed
   *        A boolean representing the constructed flag.
   */
  public void set(int tag, byte tagclass, boolean constructed) {
    this.tag         = tag;
    this.tagclass    = tagclass;
    this.constructed = constructed;
  }

  /** Set the constructed flag of this <tt>Tag</tt> to the given value.<p>
   *
   * @param constructed
   *        A boolean representing the constructed flag.
   */
  public void setConstructed(boolean constructed) {
    this.constructed = constructed;
  }

  /** Get the code of the tag.<p>
   *
   * @return An integer value representing the tag's code.
   */
  public int code() {
    return tag;
  }

  /** Check whether this <tt>Tag</tt> is constructed.<p>
   *
   * @return <tt>true</tt> if it is constructed, <tt>false</tt> otherwise.
   */
  public boolean isConstructed() {
    return constructed;
  }

  /** Compute a hash code for this tag.<p>
   *
   * @return An integer value representing the hash code.
   */
  public int hashCode() {
    return tag+tagclass;
  }

  /** Check for equality.<p>
  *
  * @return <tt>true</tt>, if this <tt>Tag</tt> instance equals the given
  *         tag, <tt>false</tt> otherwise.
  */
  public boolean equals(Object o) {
    return ((this.tag          == ((Tag) o).tag) &&
	    (this.tagclass     == ((Tag) o).tagclass));
  }

  /** Get a string representation for this tag.<p>
   *
   * @return The string representation.
   */
  public String toString() {
    return "("+tag+","+tagclass+","+constructed+")";
  }
}
// $Log: Tag.java,v $
// Revision 1.1.1.1  1999/10/05 15:08:48  damke
// Import OCF1.1.1 from Zurich
//
// Revision 1.2  1999/08/09 11:18:53  ocfadmin
// replacing OCF 1.1 with updates of OCF 1.1.1 (aka Hudson) as of Mai 1999 (by J.Damke)
//
// Revision 1.2  1998/09/16 14:13:55  cvsusers
// fixed comment
//
// Revision 1.1  1998/08/10 14:49:02  cvsusers
// moved here from opencard.core.util (rolweber)
//
// Revision 1.7  1998/06/19 16:51:40  schaeck
// Tag.java
//
// Revision 1.6  1998/06/08 11:04:16  schaeck
// added new constructor taking byte array
//
// Revision 1.5  1998/04/14 16:29:32  schaeck
// Adapted to change in SmartCard.start
//
// Revision 1.4  1998/03/06 09:46:32  hud
// ~ dropping preprocessor usage
// ~ switching trace utility
// ~ bug fixes (opencard.core.terminal.Slot, opencard.opt.util.OpenCardConfigurationLoader)
//
// Revision 1.3  1998/02/16 09:19:40  hud
// + M4 Tracing macros
//
// Revision 1.2  1998/01/19 13:28:17  hud
// changed package names (+ .core. component)
//
// Revision 1.1  1997/12/09 16:27:40  rhe
// Initial version; tidied code, removed some dependencies
//
//
