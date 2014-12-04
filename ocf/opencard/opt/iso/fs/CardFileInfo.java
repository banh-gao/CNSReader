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


/**
 * Provides information about the structure of a file.
 * File information is obtained from the smartcard by <i>selecting</i>
 * a file. The file may be a dedicated file (DF, also referred to as
 * directory) or an elementary file (EF). The smartcard typically responds
 * with the selected file's header.
 * <br>
 * Unlike the select command itself, the file header is not subject to
 * standards, so it cannot be encapsulated in a predefined class. This
 * interface defines some methods that should be implementable regardless
 * of the particular card and it's select response. Most of these methods
 * provide information about the structure of an elementary file.
 * <br>
 * The comments to the methods required by this interface assume knowledge
 * of the file types defined by ISO 7816. Some information about these file
 * types can be found in the comments to <tt>FileAccessCardService</tt>.
 * The term <i>the file</i>, which is frequently used in the comments,
 * refers to the file on the smartcard for which an instance of this
 * interface provides information.
 *
 * @version $Id: CardFileInfo.java,v 1.1.1.1 1999/10/05 15:08:47 damke Exp $
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see FileAccessCardService
 */
/*
 * This interface replaces a class that was developed by:
 * @author    Michael Baentsch (mib@zurich.ibm.com)
 * @author    Dirk Husemann (hud@zurich.ibm.com)
 * @author    Reto Hermann (rhe@zurich.ibm.com)
 */
public interface CardFileInfo
{
  /**
   * Returns the identifier of the file.
   * It is most likely that this method is of no particular use, but since
   * the file identifier is the most basic information about a file at all,
   * it is included here anyway.
   *
   * @return the identifier of the file
   */
  public short getFileID();


  /**
   * Tests whether the file is a DF.
   *
   * @return <tt>true</tt> if the file is a DF, <tt>false</tt> otherwise
   */
  public boolean isDirectory();


  /**
   * Tests whether the file is a transparent file.
   * The value returned is valid only if the file is not a DF,
   * that is if <tt>isDirectory</tt> returns <tt>false</tt>.
   *
   * @return <tt>true</tt> if the file is a transparent file
   *
   * @see #isDirectory
   */
  public boolean isTransparent();


  /**
   * Tests whether the file is a cyclic file.
   * The value returned is valid only if the file is not a DF and not a
   * transparent file, that is if <tt>isDirectory</tt> as well as
   * <tt>isTransparent</tt> return <tt>false</tt>.
   *
   * @return <tt>true</tt> if the file is a cyclic file
   *
   * @see #isDirectory
   * @see #isTransparent
   */
  public boolean isCyclic();


  /**
   * Tests whether the file is a variable record file.
   * The value returned is valid only if the file is not a DF and not a
   * transparent file, that is if <tt>isDirectory</tt> as well as
   * <tt>isTransparent</tt> return <tt>false</tt>.
   *
   * @return    <tt>true</tt> if the file is a structured file with
   *            variable record size
   *
   * @see #isDirectory
   * @see #isTransparent
   */
  public boolean isVariable();


  /**
   * Returns the length of the file.
   * If the file is a transparent file, that is if <tt>isDirectory</tt>
   * returns <tt>false</tt> and <tt>isTransparent</tt> returns <tt>true</tt>,
   * this method returns the number of bytes in the file.
   * <br>
   * If the file is a structured file with fixed record length, either
   * cyclic or non-cyclic, this method returns also returns the number
   * of bytes in the file, that is the size of a record multiplied by
   * the number of bytes in a record.
   * <br>
   * If the file is a DF, a structured file with variable record length,
   * or some card specific file type, the value returned by this method
   * is implementation dependent.
   *
   * @return the number of bytes in the file
   *
   * @see #isDirectory
   * @see #isTransparent
   */
  public int getLength();


  /**
   * Returns the record size of the file.
   * The value returned is valid only if the file is a structured file
   * with fixed record size, that is if <tt>isDirectory</tt>,
   * <tt>isTransparent</tt> and <tt>isVariable</tt> return <tt>false</tt>.
   *
   * @return the size of a record of the file
   *
   * @see #isDirectory
   * @see #isTransparent
   * @see #isVariable
   */
  public int getRecordSize();


  /**
   * Returns the file header.
   * This method actually returns the smartcard's response (or expected
   * response) to a selection of the file. For an EF, this is the file
   * header which may have been expanded by some application data, if
   * the card OS allows to do so. For a DF, this is the file header,
   * also optionally expanded, or some other data that has been defined
   * as the select response for the DF. The EMV standard requires the
   * select response for a DF to be defineable by applications.
   * <br>
   * This method is not invoked by the OpenCard Framework, and it is
   * not yet clear whether it will be useful for applications. However,
   * removing a method from this interface will be easier than adding
   * it later, so it is currently required. File services that do not
   * want to support this method may simply return <tt>null</tt>.
   *
   * @return  the smartcard's response to a selection of the file,
   *          or <tt>null</tt> if not supported
   */
  public byte[] getHeader();


} // interface CardFileInfo
