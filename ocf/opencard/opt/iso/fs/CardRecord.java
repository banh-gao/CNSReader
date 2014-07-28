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


/** The <tt>CardRecord</tt> class is used by the <tt>CardRandomRecordAccess</tt>
  * and <tt>CardRandomByteAccess</tt> classes. Essentially, it just is an
  * encapsulation of a byte array.<p>
  *
  * @author    Dirk Husemann (hud@zurich.ibm.com)
  * @version   $Id: CardRecord.java,v 1.2 1999/11/03 12:37:18 damke Exp $
  *
  * @see opencard.opt.iso.fs.CardRandomRecordAccess
  * @see opencard.opt.iso.fs.CardRandomByteAccess
  */
public class CardRecord {
  /** The underlying byte array.<p>
    */
  protected byte[] record = null;

  /** Instantiate an empty <tt>CardRecord</tt>.<p>
    *
    * @param    size
    *           The size of the new <tt>CardRecord</tt>.
    */
  public CardRecord(int size) {
    record = new byte[size];
  }

  /** Instantiate a <tt>CardRecord</tt> using an already
    * allocated byte array.<p>
    *
    * @param    b
    *           The byte array to use for the <tt>CardRecord</tt>.
    */
  public CardRecord(byte[] b) {
    record = new byte[b.length];
    System.arraycopy(b, 0, record, 0, b.length);
  }

  /** Return the bytes contained in the record.<p>
    *
    * @return contents of the record.
    */
  public byte[] bytes() {
    return record;
  }
}
