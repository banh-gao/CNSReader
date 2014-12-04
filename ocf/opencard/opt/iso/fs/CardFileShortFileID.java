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

/** <tt>CardFileShortFileID</tt> contains a single <tt>CardFilePath</tt> 
  * component, a <i>short file ID</i>.
  */
public class CardFileShortFileID extends CardFilePathComponent {
  /** Instantiate a short file ID path component.
    *
    * @param   comp
    *          The String containing the path component.
    */
  public CardFileShortFileID(String comp) {
    super(comp);
    if (!comp.startsWith(CardFilePath.FID_SEPARATOR))
      throw new IllegalArgumentException("short file ID must start with " +
					 CardFilePath.SYM_SEPARATOR);
    if (comp.length() != 3) 
      throw new IllegalArgumentException("short file ID component " +
					 comp + " must be a single byte");
  }

  /** Instantiate a short file ID path component.
    *
    * @param   bite
    *          The byte value of the short fileID.
    */
  public CardFileShortFileID(byte bite) {
    super();
    comp = ":" + HexString.hexify(bite & 0xFF);
  }

  /** Return the <tt>byte</tt> representation of this component.
    *
    * @return   A <tt>byte</tt> representing this component.
    */
  public byte toByte() {
    return (byte)((Integer.parseInt(comp.substring(1), 16)) & 0xFF);
  }

  /** Compare two file ID components.
    *
    * @param    comp
    *           An object of type <tt>CardFileFileID</tt>.
    * @return   True if <tt>comp</tt> is of type <tt>CardFilePathComponent</tt>
    *            <b>and</b> describes the same path component.
    */
  public boolean equals(Object comp) {
    if (!(comp instanceof CardFileShortFileID))
      return false;
    return toByte() == ((CardFileShortFileID)comp).toByte();
  }

  /** Return a hashcode for this file ID component. */
  public int hashCode() {
    return 0xfa354c00 + toByte();
  }
}
