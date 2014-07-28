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

import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;
import opencard.opt.security.SecureService;
import opencard.opt.service.CardServiceInterface;


/**
 * Interface to a card service for ISO 7816-4 file access functionality.
 * This service provides read and write access to transparent as well as
 * to structured files. In addition, there are methods to check whether
 * a file exists and to access information stored in a file header. Both
 * methods can be mapped onto the ISO-specified SELECT command.
 * <br>
 * This is a low-level interface that requires careful programming when
 * used directly. Especially the <tt>CardFilePath</tt> objects used for
 * identifying files on the smartcard are somehow tricky, as described
 * below. Higher level access is provided by classes like <tt>CardFile</tt>,
 * <tt>CardFileInputStream</tt>, <tt>CardFileOutputStream</tt>,
 * <tt>CardRandomByteAccess</tt>, or <tt>CardRandomRecordAccess</tt>.
 * They can be created on top of a <tt>FileAccessCardService</tt>.
 * The rest of this comment specifies the conditions that have to be
 * satisfied if this interface is implemented or used directly.
 *
 * <p> <hr>
 * <tt>CardFilePath</tt> objects, which are used as arguments in almost
 * all methods, are mutable. Since they are first class candidates for
 * keys in hashtables, there are some conditions that must be satisfied
 * by applications as well as by card services. For the card service side,
 * there are two restrictions:
 * <ol>
 * <li> A path given as an argument must not be modified by the service.
 *      If modifications to the path are necessary, it has to be copied,
 *      and the copy can be modified. Therefore, an application can rely
 *      on the path to be the same before and after an invocation of a
 *      card service method.
 *      </li>
 * <li> A path given as an argument may be modified by the application
 *      after the invoked card service method returned. If a card service
 *      needs to store a path across invocations, for example for caching
 *      purposes, the path has to be copied and the copy can be stored.
 *      Therefore, an application is allowed to modify a path, regardless
 *      of whether it has been used as an argument to a card service or
 *      not.
 *      </li>
 * </ol>
 * The restrictions imposed on applications are more complex. However,
 * these are <i>common sense</i> restrictions that will typically be
 * satisfied by any reasonable application program. As a rule of thumb,
 * path supplied as arguments must be as simple as possible, but not
 * simpler. These kind of paths are referred to as <i>canonical paths</i>.
 * <ol>
 * <li> A path given as an argument to a card service must be <i>absolute</i>.
 *      If the path consists only of file ids and short file ids, the first
 *      component of the path must be the id of the master file (MF). A path
 *      to the MF can be obtained by invoking <tt>getRoot</tt>. This path
 *      can then be copied and extended.
 *      <br>
 *      A path that contains an application identifier is implicitly
 *      absolute, since application identifiers are supported only as
 *      the first component in a path. Hierarchical applications are not
 *      supported by OCF, even if a smartcard does. There may be additional
 *      restrictions imposed on the layout of smartcards to enforce a
 *      correct behavior of card services.
 *      <br>
 *      This restriction guarantees to the card service that selection of
 *      the full path will select the correct file on the smartcard, no
 *      matter what file has been selected before.
 *      </li>
 * <li> A path given as an argument to a card service must be <i>straight</i>.
 *      It is not allowed for a path to switch to parent directories, even
 *      if the smartcard would support this feature. It is also unacceptable
 *      to include the master file in a path, unless it is the first component.
 *      <br>
 *      This restriction almost guarantees to the card service that any file
 *      on the smartcard is identified by a unique path. The path can therefore
 *      be used as a key in hashtables or other dictionaries that store
 *      information related to a file. It is also possible to cut off the
 *      leading components of a path if the path to the currently selected
 *      directory (DF) is a prefix of the path to select. Besides, this
 *      restriction reduces memory consumption and speeds up operations.
 *      </li>
 * <li> If a file on the smartcard is referenced by a path with an application
 *      name, it should not be referenced by another path without application
 *      name. Together with the preceeding restriction, this basically means:
 *      a file on the smartcard should be referenced by a unique identifier.
 *      The reason for this has been mentioned in the preceeding restriction.
 *      <br>
 *      In this last restriction, the term <i>should</i> is used instead of
 *      <i>must</i>. This is because an application programmer cannot know
 *      about the potential programmers of other applications that refer to
 *      the same file on the card. One could decide to use a path with an
 *      application name, while another one could decide to use a path with
 *      file ids only.
 *      <br>
 *      In this case, if both applications run simultaneously, and the card
 *      services created for them use a shared data structure like a file
 *      cache, the same file may be referenced by two different paths. This
 *      could lead to inconsistencies in the shared data, for example if one
 *      of the applications deletes the file. However, this case is too
 *      unlikely to worry about.
 *      <br>
 *      If different card services, or different instances of one card
 *      service, use the same smartcard without cooperating on shared data,
 *      inconsistencies in cached data cannot be avoided at all, no matter
 *      what restrictions are put on path names.
 *      </li>
 * </ol>
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @version $Id: FileAccessCardService.java,v 1.1.1.1 1999/10/05 15:08:47 damke Exp $
 *
 * @see #getRoot
 * @see CardFilePath
 * @see CardFile
 * @see CardFileInputStream
 * @see CardFileOutputStream
 * @see CardRandomByteAccess
 * @see CardRandomRecordAccess
 */
/*
 * This interface is based on the first version of the
 * interface FileSystemCardService, which was developed by
 *
 * @author Dirk Husemann (hud@zurich.ibm.com)
 * @author Reto Hermann (rhe@zurich.ibm.com)
 */
public interface FileAccessCardService
    extends CardServiceInterface, SecureService
{
  /**
   * Magic number for <tt>read</tt> and <tt>readRecords</tt>.
   * This constant can be passed as the <tt>length</tt> argument to the
   * <tt>read</tt> method, if an unspecified number of bytes should be
   * read. It can also be passed as the <tt>number</tt> argument to
   * <tt>readRecords</tt>, to read all records in a structured file.
   *
   * @see #read
   * @see #readRecords
   */
  public final static int READ_SEVERAL = -1;


  /**
   * Returns the absolute path to the master file (MF) of the smartcard.
   * For ISO compliant cards, the master file has the fixed id 0x3f00, so
   * this method will typically be implemented in the following way:
   * <p>
   * <pre><blockquote>
   * private final static CardFilePath master_file
   *                = new CardFilePath(":3f00");
   *
   * public final CardFilePath getRoot()
   * {
   *   return master_file;
   * }
   * </blockquote></pre>
   * <p>
   * The value returned is <b>not</b> allowed <b>to be modified</b>.
   * When taking a look at the sample implementation above, it should
   * be obvious why.
   * There are no exceptions thrown by this method, since it does
   * not require interaction with the smartcard.
   *
   * @return    the path to the master file
   */
  public CardFilePath getRoot();


  /**
   * Checks whether a file exists.
   *
   * @param file   the path to the file to query
   * @return   <tt>true</tt> if the file exists, <tt>false</tt> otherwise
   *
   * @exception opencard.core.service.CardServiceException
   *            if the service encountered an error
   * @exception opencard.core.terminal.CardTerminalException
   *            if the terminal encountered an error
   */
  public boolean exists(CardFilePath file)
       throws CardServiceException, CardTerminalException;


  /**
   * Queries information about a file.
   *
   * @param file   the path to the file to query
   * @return   information about the file,
   *           or <tt>null</tt> if it doesn't exist
   *
   * @exception opencard.core.service.CardServiceException
   *            if the service encountered an error
   * @exception opencard.core.terminal.CardTerminalException
   *            if the terminal encountered an error
   */
  public CardFileInfo getFileInfo(CardFilePath file)
       throws CardServiceException, CardTerminalException;


  /**
   * Reads a given amount of data from a transparent file.
   * Transparent files are similiar to files in traditional file systems.
   * They provide random access to an array of bytes.
   * <br>
   * Instead of specifying a number of bytes to read, the constant
   * <tt>READ_SEVERAL</tt> can be passed. The service will then read at
   * least one byte, probably more. Only one read command will be sent to
   * the card in this case, that means a maximum of about 255 bytes can
   * be returned. If the specified <tt>offset</tt> points to the end of
   * the file, that is if not even one byte can be read, <tt>null</tt> is
   * returned, but no exception will be thrown.
   *
   * @param file     the path to the file to read from
   * @param offset   the index of the first byte to read (0 for first)
   * @param length   the number of bytes to read, or <tt>READ_SEVERAL</tt>.
   *                 If 0 is passed, the behavior is implementation dependent.
   *
   * @return    an array holding the data read from the file,
   *            or <tt>null</tt> if a read with length <tt>READ_SEVERAL</tt>
   *            has been performed at the end of the file
   *
   * @exception opencard.core.service.CardServiceException
   *            if the service encountered an error
   * @exception opencard.core.terminal.CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #READ_SEVERAL
   */
  public byte[] read(CardFilePath file, int offset, int length)
       throws CardServiceException, CardTerminalException;


  /**
   * Reads a record from a structured file.
   * Structured files consist of records. Each record is an array of bytes.
   * Data is addressed only in terms of records, and records are always
   * read completely. The record size will be determined by the card service.
   * ISO 7816-4 specifies the following structured file types:
   * <p>
   * <dl>
   * <dt><b>linear fixed</b></dt>
   *     <dd>An array of records, with absolute addressing. All records
   *         have the same, fixed size.
   *     </dd>
   * <dt><b>linear variable</b></dt>
   *     <dd>An array of records, with absolute addressing. Every record
   *         may have a different size.
   *     </dd>
   * <dt><b>cyclic fixed</b></dt>
   *     <dd>A ring buffer of records, with relative addressing. All records
   *         have the same, fixed size. Cyclic files are typically used for
   *         keeping logs on transactions.
   *     </dd>
   * </dl>
   * <p>
   * Files with a cyclic structure may not be easily accessible with this
   * method, since the absolute addressing may be interpreted in different
   * ways by different cards. For example, the <i>first</i> record may be
   * the record that is physically stored first on the card, or it may be
   * the record that was last written into the ring buffer. The method
   * <tt>readRecords</tt> is the preferred way to read cyclic files.
   *
   * @param file         the path to the file to read from
   * @param recordNumber the index of the record to read (0 for first)
   *
   * @return    an array holding the record read. If the record has
   *            length 0, which may happen with linear variable files,
   *            an array of length 0 is returned.
   *
   * @exception opencard.core.service.CardServiceException
   *            if the service encountered an error
   * @exception opencard.core.terminal.CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #readRecords
   */
  public byte[] readRecord(CardFilePath file, int recordNumber)
       throws CardServiceException, CardTerminalException;


  /**
   * Reads consecutive records from a structured file.
   * For a discussion of structured file types, see <tt>readRecord</tt>.
   * The first record read will always be the first in the structured file.
   * For linear files with fixed or variable record size, <i>first</i> is
   * interpreted as an absolute record number. For cyclic files, <i>first</i>
   * refers to the record most recently written. Starting with that first
   * record, the specified number of consecutive records will be read. In
   * the case of a cyclic file, the second record will be the second most
   * recently written record, and so on.
   * <br>
   * Typically, smartcards will implement absolute addressing for cyclic
   * files, where the first record is the least recently written, and the
   * following are sorted by decreasing time of writing. In this case,
   * this method can be implemented by repeated invocations of
   * <tt>readRecord</tt>.
   * <br>
   * The magic number <tt>READ_SEVERAL</tt> may be passed as the number
   * of records to read. In this case, all records in the file are read.
   * This is especially useful with linear variable files, where the number
   * of records in the file cannot be determined via file attributes.
   *
   * @param file     the path to the file to read from
   * @param number   the number of records to read, or <tt>READ_SEVERAL</tt>.
   *                 If 0 is passed, the behavior is implementation dependent.
   *
   * @return    an array holding the records read,
   *            where the records are arrays themselves
   *
   * @exception opencard.core.service.CardServiceException
   *            if the service encountered an error
   * @exception opencard.core.terminal.CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #readRecord
   * @see #READ_SEVERAL
   */
  public byte[][] readRecords(CardFilePath file, int number)
       throws CardServiceException, CardTerminalException;


  /**
   * Writes data to a transparent file, using part of an array.
   * This method corresponds to the UPDATE BINARY command defined in
   * ISO 7816-4. The term <tt>write</tt> has been chosen since it is
   * more natural for programmers that are used to traditional file
   * systems. For an explanation of the term <i>transparent file</i>,
   * see <tt>read</tt>.
   * To write an array completely, the convenience method <tt>write</tt>
   * with three arguments can be used.
   *
   * @param file     the path to the file to write to
   * @param foffset  the file index of the first byte to overwrite
   *                 (0 for first byte in file)
   * @param source   an array holding the data to write
   * @param soffset  the array index of the first byte to write
   * @param length   the number of bytes to write
   *
   * @exception opencard.core.service.CardServiceException
   *            if the service encountered an error
   * @exception opencard.core.terminal.CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #read
   * @see #write(opencard.opt.iso.fs.CardFilePath, int, byte[])
   */
  public void write(CardFilePath file, int foffset,
                    byte[] source, int soffset, int length)
       throws CardServiceException, CardTerminalException;


  /**
   * Writes data to a transparent file, using a complete array.
   * This is a convenience method for <tt>write</tt> with five arguments.
   * It does not allow to specify an array index and the number of bytes
   * to write. Instead, it always writes the complete array passed.
   * Typically, this method will be implemented as follows:
   * <p>
   * <pre><blockquote>
   * final public void write(CardFilePath file, int offset, byte[] data)
   *  {
   *    write(file, offset, data, 0, data.length);
   *  }
   * </blockquote></pre>
   *
   * @param file        the path to the file to write to
   * @param offset      the file index of the first byte to overwrite
   *                    (0 for first byte in file)
   * @param data        the data to write to the file
   *
   * @exception opencard.core.service.CardServiceException
   *            if the service encountered an error
   * @exception opencard.core.terminal.CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #write(opencard.opt.iso.fs.CardFilePath, int, byte[], int, int)
   */
  public void write(CardFilePath file, int offset, byte[] data)
       throws CardServiceException, CardTerminalException;


  /**
   * Writes data to a structured file.
   * This method corresponds to the UPDATE RECORD command defined in
   * ISO 7816-4. The term <tt>write</tt> has been chosen since it is
   * more natural for programmers that are used to traditional file
   * systems. For a discussion of structured file types, see
   * <tt>readRecord</tt>.
   * <br>
   * A record is always written completely. For linear fixed files, the
   * size of the input record must be exactly the file's record size.
   * For files with variable record sizes, the size of the input record
   * must not exceed the maximum size for the record that will be
   * overwritten. That maximum size is typically the initial size of the
   * record when the smartcard was initialized. For cyclic files, this
   * method is not necessarily supported. Use <tt>appendRecord</tt> instead.
   *
   * @param file         the path to the file to write to
   * @param recordNumber the index of the record to overwrite (0 for first)
   * @param data         the data to write to the file
   *
   * @exception opencard.core.service.CardServiceException
   *            if the service encountered an error
   * @exception opencard.core.terminal.CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #readRecord
   * @see #appendRecord
   */
  public void writeRecord(CardFilePath file, int recordNumber, byte[] data)
       throws CardServiceException, CardTerminalException;


  /**
   * Appends data to a structured file.
   * For a discussion of structured file types, see <tt>readRecord</tt>.
   * For linear files with variable record size, this method appends a new
   * record at the end of the file. Typically, the space for appending a
   * record must have been allocated at the time the file was created.
   * For cyclic files, this method overwrites the oldest record in the
   * ring buffer, which then becomes the newest. The size of the record to
   * append has to match the file's record size exactly. For linear files
   * with a fixed record size, this method is not necessarily supported.
   * Use <tt>writeRecord</tt> instead.
   *
   * @param file        the path to the file to append to
   * @param data        the data to write to the new record
   *
   * @exception opencard.core.service.CardServiceException
   *            if the service encountered an error
   * @exception opencard.core.terminal.CardTerminalException
   *            if the terminal encountered an error
   *
   * @see #readRecord
   * @see #writeRecord
   */
  public void appendRecord(CardFilePath file, byte[] data)
       throws CardServiceException, CardTerminalException;

 
} // interface FileAccessCardService
