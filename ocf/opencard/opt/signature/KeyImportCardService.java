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
package opencard.opt.signature;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;
import opencard.opt.security.PrivateKeyRef;
import opencard.opt.security.PublicKeyRef;

/** <tt>KeyImportCardService</tt>
 * The SignatureCardService offers methods to generate and verify a signature
 * as well as key import, verification and generation methods. Many smartcard
 * applications will work with existing keys imported during initialization or
 * personalization instead of importing/generating them at runtime. Thus the
 * functionality for
 * <ul>
 * <li>signature generation and verification
 * <li>key import and verification
 * <li>key generation
 * </ul>
 * is split into three interfaces "SignatureCardService", "KeyImportCardService"
 * and "KeyGenerationCardService". Card service realizations of these interfaces
 * need only implement the functionality needed by the application or supported
 * by the specific card. This solution also offers potential for downsizing
 * opencard for use in embedded devises with small memory resources.
 *
 * @see SignatureCardService
 * @see KeyGenerationCardService
 *
 * @author  Peter Bendel     (peter_bendel@de.ibm.com)
 *
 * @version $Id: KeyImportCardService.java,v 1.1.1.1 1999/10/05 15:08:48 damke Exp $
 */

public interface KeyImportCardService extends SignatureCardService {

  /**
   * Import and validate a private key.
   * Key import means storing the key on card for future use in operations
   * that involve PKA keys like signing or authentication.
   * Key validation means to verify a signature provided for the key.
   * If validation is successfull a card may set the key's status to verified
   * and only allow sensitive operations using verified keys.
   *
   * @param destination
   *         Reference to the location on card where the imported key should be placed.
   * @param privateKey
   *         The actual key to be imported
   * @param keyInfo
   *         Additional special information to be stored with the key file.
   * @param signature
   *         Signature for validation of the key.
   * @param validationKey
   *         Key on card used for validation of signature.
   * @exception java.security.InvalidKeyException
   *         The key type is not supported by the specific card service.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  public boolean importAndValidatePrivateKey(PrivateKeyRef destination,
                                             PrivateKey privateKey,
                                             byte[] keyInfo,
                                             byte[] signature,
                                             PublicKeyRef validationKey)
  throws CardServiceException,InvalidKeyException,CardTerminalException;

  /**
   * Import and validate a public key to the card.
   * Key import means storing the key on card for future use in operations
   * that involve PKA keys like signature verification or authentication.
   * Key validation means to verify a signature provided for the key.
   * If validation is successfull a card may set the key's status to verified
   * and only allow sensitive operations using verified keys.
   *
   * @param destination
   *         Reference to the location on card where the imported key should be placed.
   * @param key
   *         The actual key.
   * @param keyInfo
   *         Additional special information to be stored with the key file.
   * @param signature
   *         Signature for validation of the key.
   * @param validationKey
   *         Key on card used for validation of signature.
   *
   * @exception java.security.InvalidKeyException
   *         The key type is not supported by the specific card service.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  public boolean importAndValidatePublicKey(PublicKeyRef destination,
                                            PublicKey key,
                                            byte[] keyInfo,
                                            byte[] signature,
                                            PublicKeyRef validationKey)
  throws CardServiceException,InvalidKeyException,CardTerminalException;

  /**
   * Import a private key.
   * Key import means storing the key on card for future use in operations
   * that involve PKA keys like signing or authentication.
   *
   * @param destination
   *         Reference to the location on card where the imported key should be placed.
   * @param key
   *         Private Key to store on card.
   * @param keyInfo
   *         Special information stored within the key file.
   * @exception java.security.InvalidKeyException
   *         The key type is not supported by the specific card service.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  public void importPrivateKey(PrivateKeyRef destination,
                               PrivateKey key,
                               byte[] keyInfo)
  throws CardServiceException,InvalidKeyException,CardTerminalException;

  /**
   * Import a public key to the card.
   * Key import means storing the key on card for future use in operations
   * that involve PKA keys like signature verification or authentication.
   *
   * @param destination
   *         Reference to the location on card where the imported key should be placed.
   * @param key
   *         The actual key.
   * @param keyInfo
   *         Additional special information to be stored with the key file.
   * @exception java.security.InvalidKeyException
   *         The key type is not supported by the specific card service.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  public void importPublicKey(PublicKeyRef destination,
                              PublicKey key,
                              byte[] keyInfo)
  throws CardServiceException,InvalidKeyException,CardTerminalException;
}