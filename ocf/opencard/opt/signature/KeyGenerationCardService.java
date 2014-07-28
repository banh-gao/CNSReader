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
import java.security.PublicKey;

import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;
import opencard.opt.security.PrivateKeyRef;
import opencard.opt.security.PublicKeyRef;


/** <tt>KeyGenerationCardService</tt>
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
 * @see KeyImportCardService
 *
 * @author  Peter Bendel     (peter_bendel@de.ibm.com)
 *
 * @version $Id: KeyGenerationCardService.java,v 1.1.1.1 1999/10/05 15:08:48 damke Exp $
 */
public interface KeyGenerationCardService extends SignatureCardService {

  /**
   * Generate a pair of PKA keys on card.
   *
   * @param privateDest
   *         Location on card where the private key should be stored.
   * @param publicDest
   *         Location on card where the public key should be stored
   * @param strength
   *         number of bits in the generated key
   * @param keyAlgorithm
   *         Standard Algorithm names as defined in the
   *         Java Cryptography Architecture API Specification & Reference
   *         for example
   *         DSA:  Digital Signature Algorithm, as defined in Digital Signature Standard,
   *               NIST FIPS 186.
   *         RSA: The Rivest, Shamir and Adleman AsymmetricCipher algorithm.
   * @exception java.security.InvalidKeyException
   *            Thrown when the key files do not match the requested strength or algorithm.
   * @exception CardServiceException
   *            Thrown when the card does not support the requested strength or algorithm.
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  public void generateKeyPair ( PrivateKeyRef privateDest,
                                PublicKeyRef publicDest,
                                int strength,
                                String keyAlgorithm)
  throws CardServiceException, InvalidKeyException,CardTerminalException;

  /**
   * Read a public key (that was generated on the card) from the card.
   *
   * @param pulicKey
   *         Reference to the key on card that should be read.
   * @param keyAlgorithm
   *         Standard Algorithm names as defined in the
   *         Java Cryptography Architecture API Specification & Reference
   *         for example
   *         DSA:  Digital Signature Algorithm, as defined in Digital Signature Standard,
   *               NIST FIPS 186.
   *         RSA: The Rivest, Shamir and Adleman AsymmetricCipher algorithm.
   * @return key The public key
   * @exception java.security.InvalidKeyException
   *            Thrown when the key file does not match the requested algorithm.
   * @exception CardServiceException
   *            access conditions do not allow reading the key, key is not found
   * @exception CardTerminalException any subclass of CardTerminalException
   */
  public PublicKey readPublicKey ( PublicKeyRef pulicKey,
                                   String keyAlgorithm)
  throws CardServiceException, InvalidKeyException,CardTerminalException;
}