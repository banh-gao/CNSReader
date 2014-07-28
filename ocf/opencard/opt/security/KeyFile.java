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
package opencard.opt.security;

import opencard.opt.iso.fs.CardFilePath;

/**
 * Base class for references to keys stored on a smart card in an ISO file.
 * Identifies the key by specifying the directory the key is contained in
 * as well as a key number.
 * <br>
 * This class is abstract. To reference keys, use one of the
 * derived classes <tt>PublicKeyFile</tt>, <tt>PrivateKeyFile</tt>,
 * or <tt>SecretKeyFile</tt>.
 *
 * @author  Peter Bendel     (peter_bendel@de.ibm.com)
 * @author  Roland Weber     (rolweber@de.ibm.com)
 *
 * @version $Id: KeyFile.java,v 1.1 1999/11/10 13:55:45 damke Exp $
 */
public abstract class KeyFile implements KeyRef {

  /** path to the key file */
  CardFilePath path = null;

  /** key number within directory */
  int keyNr=0;

  /**
   * Constructor from directory path and key number
   * @param path opencard.opt.iso.fs.CardFilePath
   * @param keyNr int
   */
  protected KeyFile(CardFilePath directory, int keyNr) {
    this.path=directory;
    this.keyNr = keyNr;
  }

  /**
   * adhere to interface java.security.Key
   */
  public String getAlgorithm() {
    return null;
  }

  /**
   * get the directory containing the key
   */
  public CardFilePath getDirectory() {
    return path;
  }

  /**
   * adhere to interface java.security.Key
   */
  public byte[] getEncoded() {
    return null;
  }

  /**
   * adhere to interface java.security.Key
   */
  public String getFormat() {
    return null;
  }

  /**
   * get the number of the key
   */
  public int getKeyNumber() {
    return keyNr;
  }

} // KeyFile
