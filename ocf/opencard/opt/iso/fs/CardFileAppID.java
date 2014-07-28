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

import java.io.UnsupportedEncodingException;

import opencard.core.OpenCardConstants;

/** <tt>CardFileAppID</tt> contains a single <tt>CardFilePath</tt> 
  * component, an <i>application ID</i>.
  *
  * @author  Dirk Husemann (hud@zurich.ibm.com)
  * @version $Id: CardFileAppID.java,v 1.1.1.1 1999/10/05 15:08:47 damke Exp $
  */
public class CardFileAppID extends CardFilePathComponent implements OpenCardConstants {
  private boolean asByteSequence = false;

  /** Instantiate an application ID path component.
    *
    * @param   comp
    *          The String containing the path component.
    */
  public CardFileAppID(String comp) {
    super(comp);
    if (comp.startsWith(CardFilePath.APPID_PREFIX)) {
      asByteSequence = true;
      if ((comp.length()-1) % 2 != 0)
	throw new IllegalArgumentException("odd number of characters");
    }
  }

  /** Return a byte array representing this application ID.
    *
    * @return    A byte array containing the application ID.
    * @exception CardIOException
    *            Thrown when we cannot extract the bytes from the
    *            component using the ISO 8859-1 encoding.
    */
  public byte[] toByteArray() {
    // ... the simple case first
    if (!asByteSequence) {
      try {
	return comp.getBytes(APPID_ENCODING);
      } catch (UnsupportedEncodingException e) {
	throw new CardIOException("cannot retrieve byte representation for \"" +
				  comp + "\" via " + APPID_ENCODING + "encoding");
      }
    } else {
      // ... now the more complex case
      int bytes = (comp.length()-1)/2;
      byte[] ba = new byte[bytes];
      for(int i = 0; i < bytes; i++) {
	ba[i] = (byte)(Integer.parseInt(comp.substring(i*2+1, i*2+3), 16) & 0xFF);
      }
      return ba;
    }
  }

  /** Compare two application ID components.
    *
    * @param    comp
    *           An object of type <tt>CardFileAppID</tt>.
    * @return   True if <tt>comp</tt> is of type <tt>CardFileAppID</tt>
    *            <b>and</b> describes the same path component.
    */
  public boolean equals(Object comp) {
    if (!(comp instanceof CardFileAppID))
      return false;

    byte[] a = toByteArray();
    byte[] b = ((CardFileAppID)comp).toByteArray();

    if (a.length != b.length)
      return false;
    for (int i = 0; i < a.length; i++)
      if (a[i] != b[i])
	return false;

    return true;
  }

  /** Return a hashcode for this application ID */
  public int hashCode() {
    // equals() uses the byte array, hashCode() must comply
    byte[] a = toByteArray();
    int hash = 0;

    for( int i=0; i < a.length; i++ ) {
      // this is not the very best hash function I could imagine... (RW)
      hash = ((hash & 0xff000000) >>> 21) + (hash << 8) + a[i];
    }

    return hash;
  }
}
