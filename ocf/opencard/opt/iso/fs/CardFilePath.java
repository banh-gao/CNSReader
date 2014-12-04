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

import java.util.Enumeration;
import java.util.Vector;

import opencard.opt.security.SecurityDomain;

/** <tt>CardFilePath</tt> encapsulates the various ways of addressing
  * files on a smart card:
  * <ul>
  *    <li><b>File ID paths:</b> A sequence of two byte file IDs as
  *        defined in ISO 7816-4
  *    <li><b>Short File ID:</b>
  *           One byte short file ID (0..31) for selecting EFs.
  *    <li><b>Application ID:</b>
  *           5-16 bytes for selecting applications as defined
  *           by ISO 7816-4/5
  *    <li><b>Symbolic paths:</b> A sequence of symbolic names
  * </ul>
  *
  * <b>Note that <tt>CardFilePath</tt> is a <i>mutable</i> object
  * like <tt>StringBuffer</tt>.</b>
  *
  * @author  Dirk Husemann (hud@zurich.ibm.com)
  * @author  Reto Hermann (rhe@zurich.ibm.com)
  * @version $Id: CardFilePath.java,v 1.2 1999/11/03 12:37:17 damke Exp $
  *
  * @see opencard.opt.iso.fs.CardFile
  */
public class CardFilePath implements SecurityDomain {
  /** There are <b>two</b> kinds of path component separators:
    *
    * <ul>
    *    <li><tt>SYM_SEPARATOR</tt> used for symbolic paths
    *            (for example <tt>"/wuff/oink"</tt>)
    *    <li><tt>FID_SEPARATOR</tt> used for file ID paths
    *            (for example <tt>":CAFF:EEBA:BE00"</tt>)
    * </ul>
    *
    * Although represented as a string, both separators really are
    * just one character long.
    */
  public final static String SYM_SEPARATOR = "/";
  public final static String FID_SEPARATOR = ":";

  public final static String APPID_PREFIX = "#";
  public final static String PARTIALAPPID_POSTFIX = "*";

  public final static String ROOTFILEID = ":3F00";

  /** Check a given position <tt>pos</tt> of string <tt>cand</tt> for
    * seperators characters. Ignore double occurences.
    *
    * @param     cand
    *            String to check.
    * @param     pos
    *            Position to check
    * @param     len
    *            Length of string
    * @return    <tt>1</tt> if there is a true separator (i.e., not a double
    *            occurence) at <tt>pos</tt>; <tt>2</tt> if there is an escaped
    *            seperator; <tt>0</tt> if there is none.
    */
  private static int separatorAt(String cand, int pos, int len) {
    char seps[] = {FID_SEPARATOR.charAt(0), SYM_SEPARATOR.charAt(0)};

    for (int i = 0; i  < seps.length; i++) {
      char s = seps[i];
      // ... is there a separator at pos?
      if (cand.charAt(pos) == s) {
	  // ... yes: is this really a separator; that is,
          //     at pos+1 we don't have another one?
	  if (((pos+1) < len) && (cand.charAt(pos+1) != s))
	      return 1;
	  // ... this is an escaped separator
	  return 2;
      }
    }
    // ... no separator at pos
    return 0;
  }

  // end of static stuff //////////////////////////////////////////////////////


  /** CardFilePath array containing the path components. */
  protected CardFilePathComponent[] components = null;

  /** String cache for the last toString(), avoids repeated invocations.
   *  Remember to clear it if the path gets changed! */
  private String string_representation = null;


  /** Disable public use of empty constructor. */
  private CardFilePath() {}

  /** Create a <tt>CardFilePath</tt> from a String.
    * The string representation uses the following notation:
    *
    * <dl>
    *    <dt><tt>:XXYY:ZZAA</tt>
    *    <dd>A path of file IDs; always starts with a colon (':'); always
    *        uses two bytes (with leading <tt>0</tt> if necessary); individual
    *        file IDs are separated by colons (':') as well
    *    <dt><tt>:ZZ</tt> or <tt>:XXYY:ZZ</tt>
    *    <dd>A short file ID or a path ending in a short file ID; short
    *        file IDs can only occur at the end of path
    *    <dt><tt>#AABBCCDDEE..QQ</tt>
    *    <dd>A 5-16 byte application ID as a sequence of bytes; must start with a
    *        hash ('#')
    *    <dt><tt>Some string</tt>
    *    <dd>A 5-16 byte application ID as a string; can contain at most
    *        16 characters
    *    <dt><tt>#AABB*</tt>
    *    <dd>A partial application ID as a sequence of bytes
    *    <dt><tt>some string*</tt>
    *    <dd>A partial application ID as a string.
    *    <dt><tt>/some/path/using/symbolic/names</tt>
    *    <dd>A sequence of symbolic names started and separated by slashes ('/')
    * </dl>
    *
    * Special characters [:/*#] need to be escaped by repeating them.<p>
    *
    * @param     path
    *            The string representation of the path.
    * @exception CardIOException
    *            Thrown either when the path has a silly format (e.g., file ID components
    *            followed by an application ID) or when the path contains malformed
    *            components (e.g., a short file ID containing just one nibble instead of a
    *            full byte) or when the path is empty
    */
  public CardFilePath(String path) throws CardIOException {
    if (path.length() == 0)
      throw new CardIOException("empty path");

    // ... locate seperators and setup indices; ignore double seperators
    Vector pis = new Vector();
    for (int i = 0; i < path.length(); i++) {
	switch (separatorAt(path, i, path.length())) {
	case 0:
	    break;
	case 1:
	    pis.addElement(new Integer(i));
	    break;
	case 2:
	    i++;
	}
    }

    // ... set up the components array; if the path starts with a separator
    //     pis.size() will return the exact number of components else we are
    //     off by one (see RFC 968)
    int compTotal = pis.size() == 0 ? 1 :
      ((Integer)pis.elementAt(0)).intValue() == 0 ? pis.size() : pis.size() + 1;
    components = new CardFilePathComponent[compTotal];

    // ... take note of the last index we encountered
    int last = 0;
    // ... look at each component and determine its type
    int compIdx = 0;
    scrutinize: for (int i = 0; i < pis.size(); i++) {
      // ... extract the path component
      int pi = ((Integer) pis.elementAt(i)).intValue();
      String comp = null;
      if (pi == 0) {
	if (path.length() > 1)
	  continue scrutinize;
	else
	  pi = 1;
      }

      comp = path.substring(last, pi);
      last = pi;

      components[compIdx] = CardFilePathComponent.createComponent(comp, compIdx, path, compTotal);
      compIdx++;
    }
    if (compIdx < compTotal) {
      // ... extract the last component
      String comp = path.substring(last);
      components[compIdx] = CardFilePathComponent.createComponent(comp, compIdx, path, compTotal);
    }
  }

  /** Instantiate a <tt>CardFilePath</tt> from an array of bytes. Gobble up
    * two bytes at a time and turn them into a two byte file ID. A remaining
    * byte is turned into a short file ID.
    *
    * @param     bites
    *            An array of bytes containing file IDs; the lowest pair
    *            (<tt>bites[0]</tt> and <tt>bites[1]</tt>) form the first
    *            path component; <tt>bites[0]</tt> is the high order byte
    *            and <tt>bites[1]</tt> is the low order byte.
    */
  public CardFilePath(byte[] bites) {
    this.components = new CardFilePathComponent[bites.length/2 + bites.length%2];
    int cursor = 0;
    for (int i = 0; i < bites.length; i += 2) {
      if ((i+1) == bites.length) {
	this.components[cursor++] = new CardFileShortFileID(bites[i]);
      } else {
	this.components[cursor++] = new CardFileFileID(bites[i], bites[i+1]);
      }
    }
  }

  /** Clone the <tt>path</tt> object.
    *
    * @param     path
    *            The <tt>CardFilePath</tt> object to clone.
    */
  public CardFilePath(CardFilePath path) {
    this.components = new CardFilePathComponent[path.components.length];
    for (int i = 0; i < this.components.length; i++)
      this.components[i] = path.components[i];
  }

  // end of constructors //////////////////////////////////////////////////////


  /** Return an enumeration of the components of this <tt>CardFilePath</tt>.
    *
    * @return     An Enumeration of <tt>CardFilePathComponent</tt> objects.
    */
  public Enumeration components() {
    // ... ah, the joys of anonymous classes ...
    return new Enumeration() {
      int idx = 0;

      public boolean hasMoreElements() {
	return idx < CardFilePath.this.components.length;
      }

      public Object nextElement() {
	return CardFilePath.this.components[idx++];
      }
    };
  }

  /** Append to this <tt>CardFilePath</tt> object.
    *
    * @param     path
    *            The <tt>CardFilePath</tt> object to append.
    */
  public CardFilePath append(CardFilePath path) {
    // ... sanity checks
    if ((path.components[0] instanceof CardFileAppID) ||
	(path.components[0] instanceof CardFilePartialAppID))
      throw new CardIOException
        ("attempt to append (partial) application ID " + path);
    if (components[components.length-1] instanceof CardFileShortFileID)
      throw new CardIOException
        ("attempt to append after short file ID " + this);

    // ... allocate new components array and copy in all elements
    CardFilePathComponent[] oldComponents = components;
    components = new CardFilePathComponent[oldComponents.length +
                                           path.components.length];
    for (int i = 0; i < oldComponents.length; i++)
      components[i] = oldComponents[i];
    for (int i = 0; i < path.components.length; i++)
      components[oldComponents.length + i] = path.components[i];

    // clear toString() cache
    string_representation = null;

    return this;
  }

  /** Append to this <tt>CardFilePath</tt> object.
    *
    * @param     comp
    *            The <tt>CardFilePathComponent</tt> object to append.
    */
  public CardFilePath append(CardFilePathComponent comp) {
    // ... sanity checks
    if ((comp instanceof CardFileAppID) ||
	(comp instanceof CardFilePartialAppID))
      throw new CardIOException
        ("attempt to append (partial) application ID " + comp);
    if (components[components.length-1] instanceof CardFileShortFileID)
      throw new CardIOException
        ("attempt to append after short file ID " + this);

    // ... allocate new components array and copy in all elements
    CardFilePathComponent[] oldComponents = components;
    components = new CardFilePathComponent[oldComponents.length + 1];
    for (int i = 0; i < oldComponents.length; i++)
      components[i] = oldComponents[i];
    components[oldComponents.length] = comp;

    // clear toString() cache
    string_representation = null;

    return this;
  }

  /** Check whether this path starts with <tt>prefix</tt>. Note
    * that <tt>prefix</tt> must be a <i>true</i> prefix of this
    * path (i.e., after <tt>chompPrefix(prefix)</tt> this path
    * would not be empty).
    *
    * @param     prefix
    *            The potentially common prefix.
    * @return    True if this path starts with <tt>prefix</tt>
    */
  public boolean startsWith(CardFilePath prefix) {
    if (prefix.numberOfComponents() >= this.numberOfComponents())
      return false;

    // ... start at the front (where else, really) and compare
    //     each component
    for (int i = 0; i < prefix.numberOfComponents(); i++)
      if (prefix.components[i].equals(this.components[i]))
	continue;
      else return false;

    return true;
  }

  /** Check whether this <tt>CardFilePath</tt> is equal to
    * another.
    *
    * @param     filePath
    *            The path to compare with.
    * @return    True if both paths are equal.
    */
  public boolean equals(Object filePath) {
    if (!(filePath instanceof CardFilePath))
      return false;
    CardFilePath path = (CardFilePath) filePath;

    if (this.numberOfComponents() != path.numberOfComponents())
      return false;

    // ... start at the front (where else, really) and compare
    //     each component
    for (int i = 0; i < path.numberOfComponents(); i++)
      if (path.components[i].equals(this.components[i]))
	continue;
      else return false;

    return true;
  }

  /** Return the length of the common CardFilePath prefix (if at all).
    * prefix.
    *
    * @param     path
    *            The other <tt>CardFilePath</tt> to check against.
    * @return    The length of the common prefix (<tt>0</tt> indicates
    *            that only the MF is common).
    */
  public int commonPrefixLength(CardFilePath path) {
    // ... get the greatest common length
    int gcl = (this.components.length <= path.components.length ?
	       this.components.length : path.components.length);
    // ... figure out the index of the last common component
    int i = 0;
    for (i = 0; i < gcl; i++) {
      if (!this.components[i].equals(path.components[i]))
	break;
    }
    return i;
  }

  /** Return the longest commmon prefix with another <tt>CardFilePath</tt>.
    *
    * @param     path
    *            The other <tt>CardFilePath</tt>.
    * @return    A new <tt>CardFilePath</tt> object containing the
    *            longest common prefix.
    * @exception java.lang.IllegalArgumentException
    *            Thrown when both paths do not share a common prefix.
    */
  public CardFilePath greatestCommonPrefix(CardFilePath path) {
    int cpl = commonPrefixLength(path);
    if (cpl == 0)
      throw new IllegalArgumentException("no common prefix");

    // ... now clone the common prefix into a new CardFilePath object
    CardFilePath common = new CardFilePath();
    common.components = new CardFilePathComponent[cpl+1];
    for (int i = 0; i < common.components.length; i++)
      common.components[i] = this.components[i];

    return common;
  }


  /** Chomp of the <tt>prefix</tt> of this path.
    *
    * @param     prefix
    *            The prefix to chomp off.
    * @return    The chomped path.
    * @exception java.lang.IllegalArgumentException
    *            Thrown when the <tt>prefix</tt> is not.
    */
  public CardFilePath chompPrefix(CardFilePath prefix) {
    if (!startsWith(prefix))
      throw new IllegalArgumentException("illegal prefix " +
					 prefix + " for path " +
					 toString());
    CardFilePathComponent[] oldComponents = components;
    components = new CardFilePathComponent[oldComponents.length -
                                           prefix.components.length];
    for (int i = 0; i < components.length; i++)
      components[i] = oldComponents[prefix.components.length+i];

    // clear toString() cache
    string_representation = null;

    return this;
  }

  /** Chomp off the last component of the path.
    * This is a no-op if the path consists of a single component.
    *
    * @return    True if chomping succeeded.
    */
  public boolean chompTail() {
    if (components.length == 1) {
      return false;
    } else {
      // ... trim components array
      CardFilePathComponent[] oldComponents = components;
      components = new CardFilePathComponent[oldComponents.length-1];
      for (int i = 0; i < components.length; i++)
	components[i] = oldComponents[i];

      // clear toString() cache
      string_representation = null;

      return true;
    }
  }

  /** Return the last <tt>CardFilePathComponent</tt> of this
    * <tt>CardFilePath</tt> object.
    *
    * @return   The last <tt>CardFilePathComponent</tt> of this
    *           <tt>CardFilePath</tt>.
    */
  public CardFilePathComponent tail() {
    return components[components.length-1];
  }

  /** Return the number components in this path.
    *
    * @return    The number of <tt>CardFilePathComponent</tt>s
    *            in this <tt>CardFilePath</tt>.
    */
  public int numberOfComponents() {
    return components.length;
  }

  /** Overrides Object.hashCode() since we already provide <tt>equals()</tt>.
    *
    * @return    The hash code.
    */
  public int hashCode() {
    return toString().hashCode();
  }

  /** Return a string representation of this object.
    *
    * @return   A string representing this object.
    */
  public String toString() {
    if (string_representation == null) {
      StringBuffer path = new StringBuffer();
      for(int i=0; i < components.length; i++)
        path.append(((CardFilePathComponent)components[i]).toString());
      string_representation = path.toString();
    }
    return string_representation;
  }
}
