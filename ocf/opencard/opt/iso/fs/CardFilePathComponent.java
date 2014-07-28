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

/** <tt>CardFilePathComponent</tt> contains a single <tt>CardFilePath</tt> 
  * component.
  * 
  * @author  Dirk Husemann (hud@zurich.ibm.com)
  * @version $Id: CardFilePathComponent.java,v 1.1.1.1 1999/10/05 15:08:47 damke Exp $
  */
abstract public class CardFilePathComponent {
  protected String comp = null;

  // ... empty constructor
  protected CardFilePathComponent() {}

  /** The common constructor.
    * 
    * @param     comp
    *            The string representation of this component.
    */
  protected CardFilePathComponent(String comp) {
    this.comp = comp;
  }

  /** Check whether a given character might be a hexadecimal.
    * 
    * @param     c
    *            The candidate to check.
    * @return    True if <tt>c</tt> might be a hexadecimal.
    */
  private static boolean hex(char c) {
    return (c >= '0' && c <= '9') ||
      (c >= 'a' && c <= 'f') ||
      (c >= 'A' && c <= 'F');
  }

  /** Create the correct <tt>CardFilePathComponent</tt>
    * object from a string.
    *
    * @param     comp
    *            The component as a string.
    * @param     pos
    *            The position of the component within the 
    *            containing path.
    * @return    A single component <tt>CardFilePath</tt>
    * @exception CardIOException
    *            Thrown when the component is malformed or 
    *            in the wrong place.
    */
  protected static CardFilePathComponent createComponent(String comp, int pos, String path, int total) 
    throws CardIOException {
      // ... is it a symbolic name?
      if (comp.startsWith(CardFilePath.SYM_SEPARATOR)) 
	return new CardFileSymbolicName(comp);
      
      // ... is it a file ID?
      if (comp.startsWith(CardFilePath.FID_SEPARATOR)) {
	// ... check for non-hex characters; skip separator
	for (int i = 1; i < comp.length(); i++)
	  if (!hex(comp.charAt(i)))
	    throw new CardIOException("file ID can only contain hexadecimal characters (" +
				      comp + ")");
	// ... a two byte file ID ":XXYY" ?
	if (comp.length() == 1)
	  throw new CardIOException("illegal empty file ID ':' \"" + comp + "\"");
	else if (comp.length() > 3)
	  return new CardFileFileID(comp);
	else if ((comp.length() == 3) && (pos == (total - 1)))
	  return new CardFileShortFileID(comp);
	else if ((comp.length() == 3) && (pos != (total - 1)))
	  throw new CardIOException("short file ID \"" + comp + 
				    "\" of path " + path + 
				    " must be the last component" );
	else throw new CardIOException("malformed file ID component " +
				       comp);
      }
      
      // ... is it an application ID as bytes?
      if (comp.startsWith(CardFilePath.APPID_PREFIX) && 
	  !comp.startsWith(CardFilePath.APPID_PREFIX + CardFilePath.APPID_PREFIX)) {
	// ... check for non-hex characters; skip separator
	for (int i = 1; i < comp.length(); i++)
	  if (!hex(comp.charAt(i)))
	    throw new CardIOException("file ID can only contain hexadecimal characters (" +
				      comp + ")");
	if (comp.endsWith(CardFilePath.PARTIALAPPID_POSTFIX) &&
	    !comp.endsWith(CardFilePath.PARTIALAPPID_POSTFIX + CardFilePath.PARTIALAPPID_POSTFIX))
	  return new CardFilePartialAppID(comp);
	else return new CardFileAppID(comp);
      }
      
      // ... it must be an application ID as a string
      if (comp.endsWith(CardFilePath.PARTIALAPPID_POSTFIX) &&
	  !comp.endsWith(CardFilePath.PARTIALAPPID_POSTFIX + CardFilePath.PARTIALAPPID_POSTFIX))
	return new CardFilePartialAppID(comp);
      else return new CardFileAppID(comp);
  }

  /** Two <tt>CardFilePathComponent</tt>s are equal if they are of the
    * same type (an instance of <tt>CardFilePathComponent</tt>) and 
    * describe the same path object.
    *
    * @param     comp
    *            An <tt>Object</tt> which should be of type 
    *            <tt>CardFilePathComponent</tt>
    * @return    True if <tt>comp</tt> is of type <tt>CardFilePathComponent</tt>
    *            <b>and</b> describes the same path component.
    */
  public boolean equals(Object comp) {
    if (!(comp instanceof CardFilePathComponent))
      return false;
    return this.comp.equals(((CardFilePathComponent)comp).comp);
  }

  /** Overrides <tt>Object.toString()</tt> and returns a
    * string representation of this component..
    */
  public String toString() {
    return comp;
  }
}
