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

import java.io.FileNotFoundException;

import opencard.core.OpenCardException;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceInabilityException;
import opencard.core.terminal.CardTerminalException;
import opencard.core.util.Tracer;
import opencard.opt.service.CardServiceObjectNotAvailableException;


/**
 * Represents a file or directory on the smartcard.
 * A card file object is a combination of an absolute path to the file
 * represented, and the file information that was obtained from the
 * smartcard on creation of the object. It provides methods to navigate
 * through the smartcard's file system, and to obtain meta information
 * about the files and directories therein. To access file contents,
 * classes like <tt>CardFileInputStream</tt>, <tt>CardFileOutputStream</tt>,
 * <tt>CardRandomByteAccess</tt>, or <tt>CardRandomRecordAccess</tt> can
 * be instantiated.
 *
 * <p>
 * To instantiate class <tt>CardFile</tt>, a <tt>FileAccessCardService</tt>
 * has to be provided. This service is used in the constructor to check
 * whether the file that shall be represented actually exists, and to query
 * meta information about the file. A card file object representing a
 * directory or, in smartcard terminology, a dedicated file (DF), can be
 * used to access other directories or files in the respective subtree of
 * the smartcard's file system. The path names for these files must be
 * relative to the represented directory. An absolute path will be created
 * for newly created card file objects, which will use the same file service
 * that was used by the parent object.
 * <br>
 * Smartcards use 16 bit numbers to identify files and directories. Human
 * friendly names are supported by a software layer, which has no platform
 * dependencies. Card file names are represented by instances of class
 * <tt>CardFilePath</tt>, for which different path components are defined.
 * Class <tt>CardFile</tt> supports only path components that can be
 * interpreted by the underlying file service. These components are
 * file IDs, and optionally short file IDs and application identifiers.
 * The string representation of a file ID is a colon, followed by a four
 * digit hex number specifying the afforementioned 16 bit number. These
 * file IDs can be concatenated to a path, for example <tt>":BEEB:CAFE"</tt>.
 * Support for symbolic path names, like <tt>"/mama/papa/dog"</tt> may be
 * added by derived classes.
 * <br>
 * <tt>CardFilePath</tt> objects can be created directly from strings.
 * To reduce memory requirements and to speed up operation, applications
 * should create each path only once and use it directly later on, instead
 * of using strings and creating paths any time they are needed. To
 * enforce this behavior, only constructors of class <tt>CardFile</tt>
 * accept strings instead of paths. The path created can be obtained
 * using <tt>CardFile.getPath</tt>.
 *
 * <p>
 * Since the path to the file is part of this class, it corresponds to class
 * <tt>java.io.File</tt>. Many of the methods found there are implemented
 * here, too. Some others are implemented with slightly different signatures,
 * but comparable functionality. However, there are significant differences
 * between a standard disk file system and the file system on a smartcard,
 * which are reflected in <tt>CardFile</tt>.
 * <br>
 * Class <tt>java.io.file</tt> represents only a <i>filename</i>, on which
 * symbolic operations can be performed, and which can be used to access an
 * actual file on the underlying file system. Class <tt>CardFile</tt>
 * represents actual files. It cannot be instantiated for files that do not
 * exist. An operation like <tt>exists()</tt> does therefore not make sense.
 * Other examples for methods that are not provided are <tt>renameTo</tt>
 * and both <tt>list</tt> methods, since smartcards do not support file
 * renaming, and typically do not support browsing directories.
 * <br>
 * Traditional disk based file systems maintain file attributes like the
 * time of the last modification, or access conditions. Smartcards do not
 * support creation or modification times, and the access conditions are
 * too complex to be mapped on methods as simple as <tt>canRead</tt> and
 * <tt>canWrite</tt>. Support for platform dependent file separators, which
 * is provided via attributes like <tt>pathSeparator</tt> in class
 * <tt>java.io.File</tt>, do not make sense for smartcards either. The
 * string representation of card file paths is interpreted by a platform
 * neutral software layer.
 *
 * <p>
 * Class <tt>CardFile</tt> provides methods like <tt>create</tt> and
 * <tt>delete</tt>, which are not supported by the interface
 * <tt>FileAccessCardService</tt>. These methods are available only if
 * an implementation of <tt>FileSystemCardService</tt> has been used to
 * create the card file object. This is checked with the Java operator
 * <tt>instanceof</tt>, so the methods may even be available if an
 * application is not aware of it.
 * <br>
 * The assumption for this implementation is that an application that needs
 * creational methods will request a <tt>FileSystemCardService</tt>, while
 * an application that does not need them will not invoke those methods.
 * There is a small chance for programming errors if the file service used
 * for testing implements both interfaces and there is no service that
 * implements only <tt>FileAccessCardService</tt>.
 *
 *
 * @author  Dirk Husemann  (hud@zurich.ibm.com)
 * @author  Reto Hermann   (rhe@zurich.ibm.com)
 * @author  Peter Trommler (trp@zurich.ibm.com)
 * @author  Roland Weber  (rolweber@de.ibm.com)
 * @version $Id: CardFile.java,v 1.2 2005/09/19 10:21:22 asc Exp $
 *
 * @see opencard.opt.iso.fs.CardFileInputStream
 * @see opencard.opt.iso.fs.CardFileOutputStream
 * @see opencard.opt.iso.fs.CardRandomByteAccess
 * @see opencard.opt.iso.fs.CardRandomRecordAccess
 *
 * @see opencard.opt.iso.fs.CardFilePath
 * @see #getPath
 *
 * @see opencard.opt.iso.fs.FileAccessCardService
 * @see opencard.opt.iso.fs.FileSystemCardService
 *
 * @see java.io.File
 * @see java.io.File#exists
 * @see java.io.File#renameTo
 * @see java.io.File#list()
 * @see java.io.File#list(java.io.FilenameFilter)
 * @see java.io.File#canRead
 * @see java.io.File#canWrite
 * @see java.io.File#pathSeparator
 */
public class CardFile implements CardFileInfo
{
  /** The tracer for CardFile: tracing on class name. */
  private final static Tracer ctracer = new Tracer(CardFile.class);


  /**
   * The path to the file represented.
   */
  private CardFilePath file_path = null;

  /**
   * The underlying file access card service.
   */
  private FileAccessCardService file_access = null;

  /**
   * The underlying file system card service.
   * If the card file object is created with a <tt>FileAccessCardService</tt>
   * only, this attribute holds <tt>null</tt>. Otherwise, it holds the same
   * reference as <tt>file_access</tt>.
   *
   * @see #file_access
   */
  private FileSystemCardService file_system = null;

  /**
   * The parent directory.
   * This attribute is typically holds <tt>null</tt> and is initialized
   * on request, that is when <tt>getParent</tt> is invoked.
   *
   * @see #getParent
   */
  private CardFile file_parent = null;

  /**
   * The information about the file represented.
   */
  protected CardFileInfo file_info = null;



  // construction /////////////////////////////////////////////////////////////


  /**
   * Creates a root card file.
   * The new card file represents the top level directory, that is the
   * master file (MF) of the smartcard's file hierarchy.
   * <I>
   * This constructor replaces the <tt>mount</tt> method formerly found
   * in <tt>FileSystemCardService</tt>.
   * </I>
   *
   * @param service  the file service to use for accessing the smartcard
   *
   * @exception FileNotFoundException
   *    if the file to represent could not be found
   * @exception OpenCardException
   *    if anything else went wrong
   */
  public CardFile(FileAccessCardService service)
       throws FileNotFoundException, OpenCardException
  {
    this(service.getRoot(), service);
  }


  /**
   * Creates a card file object for the specified absolute path.
   *
   * @param service     the underlying file service
   * @param abspath     the absolute path to the file to represent
   *
   * @exception FileNotFoundException
   *    if the file to represent could not be found
   * @exception OpenCardException
   *    if anything else went wrong
   */
  public CardFile(FileAccessCardService service, CardFilePath abspath)
       throws FileNotFoundException, OpenCardException
  {
    // clone the path to avoid later modification
    this(new CardFilePath(abspath), service);
  }


  /**
   * Creates a card file object for an absolute path given as string.
   *
   * @param service     the underlying file service
   * @param abspath     a string holding the absolute path
   *                    to the file to represent
   *
   * @exception FileNotFoundException
   *    if the file to represent could not be found
   * @exception OpenCardException
   *    if anything else went wrong
   */
  public CardFile(FileAccessCardService service, String abspath)
       throws FileNotFoundException, OpenCardException
  {
    // convert the string to a new path
    this(new CardFilePath(abspath), service);
  }


  /**
   * Creates a card file for the specified relative path.
   * The relative path is appended to the absolute path of the card file
   * specified as base. If the relative path consists of a single component,
   * the specified base card file object is considered to be the parent file
   * of the newly created one.
   *
   * @param base     the file representing the base directory
   * @param relpath  the path to the file, relative to the base directory
   *
   * @exception FileNotFoundException
   *    if the file to represent could not be found
   * @exception OpenCardException
   *    if anything else went wrong
   */
  public CardFile(CardFile base, CardFilePath relpath)
       throws FileNotFoundException, OpenCardException
  {
    this((new CardFilePath(base.file_path)).append(relpath), base.file_access);
    if (relpath.numberOfComponents() == 1)
      file_parent = base;
  }


  /**
   * Creates a card file for the relative path specified as string.
   * The relative path is converted from a string to a <tt>CardFilePath</tt>.
   * Then, the constructor expecting a path is invoked.
   *
   * @param base        the file representing the base directory
   * @param relpath     the path to the file, relative to the base directory
   *
   * @exception FileNotFoundException
   *    if the file to represent could not be found
   * @exception OpenCardException
   *    if anything else went wrong
   *
   * @see CardFile#CardFile(opencard.opt.iso.fs.CardFile, opencard.opt.iso.fs.CardFilePath)
   */
  public CardFile(CardFile base, String relpath)
       throws FileNotFoundException, OpenCardException
  {
    this(base, new CardFilePath(relpath));
  }


  /**
   * Creates a card file for the specified path component.
   * The path component is interpreted as a relative path. It is appended
   * to the path of the file specified as the base, and the base is stored
   * as the parent of the newly created card file object.
   *
   * @param base        the file representing the base directory
   * @param comp        the file in the base directory that will be represented
   *
   * @exception FileNotFoundException
   *    if the file to represent could not be found
   * @exception OpenCardException
   *    if anything else went wrong
   */
  public CardFile(CardFile base, CardFilePathComponent comp)
       throws FileNotFoundException, OpenCardException
  {
    this((new CardFilePath(base.file_path)).append(comp), base.file_access);
    file_parent = base;
  }


  /**
   * Creates a card file, for internal purposes.
   * This is the generic constructor on which all other constructors are
   * mapped. The file service is passed as the second argument instead of
   * the first to distinguish this constructor from the public one.
   * <br>
   * The path given as argument is stored immediately. If it has to be cloned
   * to protect it from being modified, that has to be done by the caller.
   * That's why this constructor is <i>internal</i>.
   *
   * @param path     the absolute path to the file represented
   * @param service  the file service to use for accessing the smartcard
   *
   * @exception FileNotFoundException
   *            If the file does not exist on the smartcard.
   * @exception CardServiceException
   *            If the underlying file service encountered an error.
   * @exception CardTerminalException
   *            If communication to the smartcard failed.
   */
  protected CardFile(CardFilePath path, FileAccessCardService service)
       throws FileNotFoundException,
              CardServiceException, CardTerminalException
  {
    file_path   = resolvePath(path);
    file_access = service;
    file_parent = null;         // computed on demand

    if (service instanceof FileSystemCardService)
      file_system = (FileSystemCardService) service;

    try {
      file_info = service.getFileInfo(path);

    } catch (CardServiceObjectNotAvailableException ona) {
      throw new FileNotFoundException(path.toString());
    }

  } // constructor


  // access ///////////////////////////////////////////////////////////////////

  /**
   * Returns the canonical path of the file represented.
   * The path returned is not allowed to be modified. It is an absolute
   * path, so it is a suitable argument for the methods in the interfaces
   * <tt>FileAccessCardService</tt> and <tt>FileSystemCardService</tt>.
   * <br>
   * A canonical path consists only of components that can be interpreted
   * by the underlying file service. These components are file ids and,
   * optionally, short file ids and application ids. The canonical path
   * never contains symbolic path components, even if support for these
   * components is added in a derived class. That is the reason why this
   * method has been declared <tt>final</tt>.
   *
   * @return   the absolute, canonical path to the file represented
   *
   * @see #getAbsolutePath
   */
  final public CardFilePath getCanonicalPath()
  {
    return file_path;
  }


  /**
   * Returns the absolute path of the file represented.
   * In this class, the absolute path is the same as the canonical path.
   * If support for symbolic path names is added in a dervied class, the
   * absolute path may contain symbolic components, while the canonical
   * path may not.
   *
   * @return  the absolute path to the file represented
   *
   * @see #getCanonicalPath
   */
  public CardFilePath getAbsolutePath()
  {
    return file_path;
  }


  /**
   * Returns the path of the file represented.
   * This method is identical to <tt>getCanonicalPath</tt>, except for
   * the name which is less clumsy. The path returned is not allowed to
   * be modified. It is a suitable argument to methods in the interfaces
   * <tt>FileAccessCardService</tt> and <tt>FileSystemCardService</tt>.
   *
   * @return the absolute, canonical path to the file represented
   *
   * @see #getCanonicalPath
   * @see FileAccessCardService
   * @see FileSystemCardService
   */
  final public CardFilePath getPath()
  {
    return getCanonicalPath();
  }


  /**
   * Returns the name of the file represented.
   * The name does not include the path to the file.
   * If support for symbolic names is added in a derived class,
   * a symbolic name is returned.
   *
   * @return  the name of the file
   */
  public String getName()
  {
    return file_path.tail().toString();
  }


  /**
   * Returns the identifier of the file represented.
   *
   * @return  the identifier of the file
   *
   * @see CardFileInfo#getFileID
   */
  final public short getFileID()
  {
    return file_info.getFileID();
  }


  /**
   * Checks whether the file represented is a directory.
   * Directories are also referred to as dedicated files (DF).
   *
   * @return    <tt>true</tt> if the file represented is a directory,
   *            <tt>false</tt> otherwise
   *
   * @see CardFileInfo#isDirectory
   */
  final public boolean isDirectory()
  {
    return file_info.isDirectory();
  }


  /**
   * Checks whether the file represented is a non-directory file.
   * This method is complementary to <tt>isDirectory</tt>.
   *
   * @return    <tt>true</tt> if the file represented is not a directory,
   *            <tt>false</tt> otherwise
   * @see #isDirectory
   */
  final public boolean isFile()
  {
    return !isDirectory();
  }


  /**
   * Checks whether this card file represents a dedicated file (directory).
   * This method is identical to <tt>isDirectory</tt>, but has a shorter
   * name that uses SmartCard terminology.
   *
   * @return    <tt>true</tt> if the file represented is a DF,
   *            <tt>false</tt> otherwise
   * @see #isDirectory
   */
  final public boolean isDF()
  {
    return isDirectory();
  }


  /**
   * Checks whether this card file represents an elementary file.
   * This method is identical to <tt>isFile</tt>, but has a shorter
   * name that uses SmartCard terminology.
   *
   * @return    <tt>true</tt> if the file represented is an EF,
   *            <tt>false</tt> otherwise
   * @see #isFile
   */
  final public boolean isEF()
  {
    return isFile();
  }


  /**
   * Checks whether the file represented is a transparent file.
   *
   * @return <tt>true</tt> if this file is transparent
   * @see CardFileInfo#isTransparent
   */
  final public boolean isTransparent()
  {
    return file_info.isTransparent();
  }


  /**
   * Checks whether the file represented is a cyclic file.
   *
   * @return <tt>true</tt> if this file is cyclic
   * @see CardFileInfo#isCyclic
   */
  final public boolean isCyclic()
  {
    return file_info.isCyclic();
  }


  /**
   * Checks whether the file represented is a variable record file.
   *
   * @return <tt>true</tt> if this file is a variable record file
   * @see CardFileInfo#isVariable
   */
  final public boolean isVariable()
  {
    return file_info.isVariable();
  }


  /**
   * Returns the length of the file represented.
   *
   * @return  the number of bytes in the file
   * @see CardFileInfo#getLength
   */
  final public int getLength()
  {
    return file_info.getLength();
  }


  /**
   * Returns the record size of the file represented.
   *
   * @return  the size of a record in the file
   * @see CardFileInfo#getRecordSize
   */
  final public int getRecordSize()
  {
    return file_info.getRecordSize();
  }


  /**
   * Returns the header of the file represented.
   * In case this method is removed from the interface <tt>CardFileInfo</tt>,
   * it will be removed here, too. Currently, it must be available since this
   * class implements that interface. However, the preferred method to get
   * the file header from a card file is <tt>getFileInfo().getHeader()</tt>.
   *
   * @return  the header of the file
   * @see #getFileInfo
   * @see CardFileInfo
   * @see CardFileInfo#getHeader
   */
  final public byte[] getHeader()
  {
      if (file_info == null) {
          return null;
      }
      return file_info.getHeader();
  }


  /**
   * Returns the underlying file access card service.
   *
   * @return the underlying service for file access
   */
  final protected FileAccessCardService getFileAccessService()
  {
    return file_access;
  }


  /**
   * Returns the underlying file system card service.
   * If the underlying file service is not an instance of
   * <tt>FileSystemCardService</tt>, <tt>null</tt> is returned.
   *
   * @return    the underlying service for file system operations,
   *            or <tt>null</tt> if not available
   */
  final protected FileSystemCardService getFileSystemService()
  {
    return file_system;
  }


  // service //////////////////////////////////////////////////////////////////


  /**
   * Provides a hook for resolving symbolic path components.
   * This method is meant to be overridden in derived classes that support
   * symbolic path components. It is invoked by the constructors of this
   * class. It's argument is a path that has been provided to one of them.
   * It should return a path in which all symbolic components have been
   * resolved and which satisfies the conditions described in
   * <tt>getCanonicalPath</tt>.
   * <br>
   * The path provided as argument must not be changed. The path
   * returned will not be changed. The implementation provided here
   * just returns the argument.
   *
   * @param path  a path that may contain symbolic components
   * @return      a resolved path
   *
   * @see #getCanonicalPath
   */
  protected CardFilePath resolvePath(CardFilePath path)
  {
    return path;
  }


  /**
   * Return the parent <tt>CardFile</tt>.
   * This method operates on the path name of the file. Note that it can
   * climb above the <tt>CardFile</tt> that has been used as the base for
   * creating this one. If the root of the directory structure is reached,
   * or if an error occurred, <tt>null</tt> is returned.
   *
   * @return     the parent file, or <tt>null</tt> if an error occurred
   */
  public CardFile getParent()
  {
    if (file_parent == null)
      {
        // the parent is unknown, compute it now
        CardFilePath parent = new CardFilePath(file_path);
        try {
          if (parent.chompTail()) // if after trimming something is left
            file_parent = new CardFile(this, parent);
        } catch (Exception e) {
          // can't do anything about it...
          // note that unchecked exceptions are caught here, too
        }
      }
    return file_parent;

  } // getParent


  /**
   * Tests whether a given file exists.
   * The file to test for has to be specified by a path that is relative
   * to the file represented by this card file object.
   *
   * @param relpath   path to the file to check for existance
   * @return    <tt>true</tt> if the file exists,
   *            <tt>false</tt> if it doesn't
   */
  public boolean exists(CardFilePath relpath)
  {
    CardFilePath path = new CardFilePath(file_path);
    path.append(relpath);

    boolean does = false;       // does it exist ?
    try {
      does = file_access.exists(path);

    } catch (CardServiceObjectNotAvailableException ona) {
      // obviously, it doesn't exist
    } catch (OpenCardException iox) {
      // something went wrong
    }
    return does;

  } // exists


  /**
   * Returns information about the file represented.
   * Since the interface <tt>CardFileInfo</tt> is implemented by
   * this class too, the information typically needed is accessible
   * directly. This method is intended to provide access to card or
   * implementation specific information. It returns the information
   * obtained by an invocation of <tt>getFileInfo</tt> in the interface
   * <tt>FileAccessCardService</tt>. It should be used if the header of
   * the represented file must be accessed, and it has to be used if
   * the object returned by the file service has to be down-casted.
   *
   * @return   the information returned by the underlying file service
   *
   * @see FileAccessCardService#getFileInfo
   */
  final public CardFileInfo getFileInfo()
  {
    return file_info;
  }


  /**
   * Creates a file on the smartcard.
   * This method can be used to create dedicated as well as elementary
   * files. The new file will be located in the directory represented by
   * this card file. This method is available only if the underlying card
   * service is an instance of <tt>FileSystemCardService</tt>. The parameter
   * block specifying the file to be created is card-specific. For details,
   * see <tt>create</tt> in the interface.
   *
   * @param data  a card-specific parameter block specifying the file to create
   *
   * @exception CardServiceInabilityException
   *            if the underlying card service does not support creating files
   * @exception OpenCardException
   *            if something else went wrong
   *
   * @see FileSystemCardService
   * @see FileSystemCardService#create
   */
  final public void create(byte[] data)
       throws CardServiceInabilityException, OpenCardException
  {
    if (file_system == null)
      throw new CardServiceInabilityException
        ("create() requires FileSystemCardService");

    file_system.create(file_path, data);
  }


  /**
   * Deletes a file on the smartcard.
   * This method is available only if the underlying card service is an
   * instance of <tt>FileSystemCardService</tt>.
   *
   * @param relpath     the path to the file to delete,
   *                    relative to this card file
   *
   * @exception CardServiceInabilityException
   *            if the underlying card service does not support deleting files
   * @exception OpenCardException
   *            if something else went wrong
   *
   * @see FileSystemCardService
   */
  public void delete(CardFilePath relpath)
       throws CardServiceInabilityException, OpenCardException
  {
    if (file_system == null)
      throw new CardServiceInabilityException
        ("delete() requires FileSystemCardService");

    CardFilePath path = new CardFilePath(file_path);
    path.append(relpath);

    file_system.delete(path);
  }


  // system ///////////////////////////////////////////////////////////////////


  /**
   * Returns a hash code for this object.
   *
   * @return    a hash code
   */
  public int hashCode()
  {
    return this.file_path.hashCode();
  }


  /**
   * Returns a human-readable string representation of this card file object.
   *
   * @return    a string representing this card file object
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer("CardFile ");

    sb.append(getAbsolutePath());

    return sb.toString();
  }


} // class CardFile
