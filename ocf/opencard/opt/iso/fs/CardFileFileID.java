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

import opencard.core.util.HexString;

/** <tt>CardFileFileID</tt> contains a single <tt>CardFilePath</tt> 
  * component, a <i>two byte file ID</i>.
  *
  * @author  Dirk Husemann (hud@zurich.ibm.com)
  * @version $Id: CardFileFileID.java,v 1.1.1.1 1999/10/05 15:08:47 damke Exp $
  */
public class CardFileFileID extends CardFilePathComponent {
  /** Cache the two byte file ID for fast access */
  protected byte[] fileID = null;

  /** Instantiate a file ID path component.
    *
    * @param   comp
    *          The String containing the path component.
    */
  public CardFileFileID(String comp) {
    super(comp);
    if (!comp.startsWith(CardFilePath.FID_SEPARATOR))
      throw new IllegalArgumentException("file ID components must start with "
                                         + CardFilePath.SYM_SEPARATOR);
    if (comp.length() != 5) 
      throw new IllegalArgumentException("file ID component " +
					 comp + " must contain two bytes");
    fileID = new byte[] {
      (byte)(Integer.parseInt(comp.substring(1,3), 16) & 0xFF),
      (byte)(Integer.parseInt(comp.substring(3), 16) & 0xFF) 
    };
  }

  /** Instantiate a file ID path component.
    *
    * @param   fileID
    *          A short containing the file ID.
    */
  public CardFileFileID(short fileID) {
    this((new StringBuffer(":").append(HexString.hexifyShort(fileID & 0xFFFF))).toString());
  }

  /** Instantiate a file ID path component.
    *
    * @param   hi
    *          The high byte of the fileID (e.g., 0xCA for :CAFE)
    * @param   lo
    *          The low byte of the fileID (e.g., 0xFE for :CAFE)
    */
  public CardFileFileID(byte hi, byte lo) {
    this((short)(((hi & 0xFF)<<8) | (lo & 0xFF)));
  }

  /** Return the <tt>short</tt> representation of this component.
    *
    * @return   A <tt>short</tt> representing this component.
    */
  public short toShort() {
    return (short)(((fileID[0] & 0xFF)<<8 | 
		    (fileID[1] & 0xFF)) & 0xFFFF);
  }

  /** Return the byte array representing this component.
    *
    * @return   A byte array that represents this component.
    */
  public byte[] toByteArray() {
    return fileID;
  }

  /** Fill in the byte array that represents this component.
    * @param    bites
    *           The target byte array.
    * @param    off
    *           The offset into the target byte array.
    */
  public void toByteArray(byte[] bites, int off) {
    bites[off] = fileID[0];
    bites[off+1] = fileID[1];
  }

  /** Compare two file ID components.
    *
    * @param    comp
    *           An object of type <tt>CardFileFileID</tt>.
    * @return   True if <tt>comp</tt> is of type <tt>CardFilePathComponent</tt>
    *            <b>and</b> describes the same path component.
    */
  public boolean equals(Object comp) {
    if (!(comp instanceof CardFileFileID))
      return false;
    return toShort() == ((CardFileFileID)comp).toShort();
  }

  /** Return a hashcode for this file ID component */
  public int hashCode() {
    return toShort();
  }
}
