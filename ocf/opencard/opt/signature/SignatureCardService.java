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

import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;
import opencard.opt.security.PrivateKeyRef;
import opencard.opt.security.PublicKeyRef;
import opencard.opt.security.SecureService;
import opencard.opt.service.CardServiceInterface;


/** <tt>SignatureCardService</tt>
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
 * @see KeyGenerationCardService
 * @see KeyImportCardService
 *
 * @author  Michael Baentsch (mib@zurich.ibm.com)
 * @author  Thomas Schaeck   (schaeck@de.ibm.com)
 * @author  Reto Hermann     (rhe@zurich.ibm.com)
 * @author  Peter Bendel     (peter_bendel@de.ibm.com)
 *
 * @version $Id: SignatureCardService.java,v 1.2 1999/10/22 16:07:35 damke Exp $
 */
public interface SignatureCardService extends CardServiceInterface,
                                              SecureService {

  /**
   * Generate a digital Signature.
   * First hash the data, then pad the hash and then
   * apply the PKA algorithm to the padded hash.
   * <p>
   * The padding algorithm is chosen as defined in the Java Cryptography Architecture Specification.
   * <p>
   * The standard algorithm name must be specified as defined in the
   * Java Cryptography Architecture API Specification & Reference,
   * for example
   * <DL COMPACT>
   * <DT>MD5withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                   AsymmetricCipher algorithm with the MD5 MessageDigest Algorithm.
   * <DT>MD2withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                   AsymmetricCipher algorithm with the MD2 MessageDigest Algorithm.
   * <DT>SHA1withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                    AsymmetricCipher algorithm with the SHA-1 MessageDigest Algorithm.
   * <DT>SHA1withDSA<DD>Digital Signature Algorithm, as defined in Digital Signature Standard,
   *                    NIST FIPS 186.  This standard defines a digital signature algorithm
   *                    that uses the RawDSA asymmetric transformation along with the SHA-1
   *                    message digest algorithm.
   * </DL>
   *
   * @param privateKey    a reference to the private key on card to be used for signing
   * @param signAlgorithm standard digital signature algorithm name
   * @param data          data to be signed
   *
   * @return signature
   *
   * @exception InvalidKeyException
   *            Thrown when the key is not valid or does not match the requested algorithm.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   *
   * @see JCAStandardNames
   */
  public byte[] signData  (PrivateKeyRef privateKey,
                           String signAlgorithm,
                           byte[] data)
  throws CardServiceException, InvalidKeyException,CardTerminalException;

  /**
   * Generate a digital Signature (overload method that allows to specify
   * the padding algorithm).
   * First hash the data, then pad the hash and then
   * apply the PKA algorithm to the padded hash.
   * <p>
   * The standard algorithm name must be specified as defined in the
   * Java Cryptography Architecture API Specification & Reference,
   * for example
   * <DL COMPACT>
   * <DT>MD5withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                   AsymmetricCipher algorithm with the MD5 MessageDigest Algorithm.
   * <DT>MD2withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                   AsymmetricCipher algorithm with the MD2 MessageDigest Algorithm.
   * <DT>SHA1withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                    AsymmetricCipher algorithm with the SHA-1 MessageDigest Algorithm.
   * <DT>SHA1withDSA<DD>Digital Signature Algorithm, as defined in Digital Signature Standard,
   *                    NIST FIPS 186.  This standard defines a digital signature algorithm
   *                    that uses the RawDSA asymmetric transformation along with the SHA-1
   *                    message digest algorithm.
   * </DL>
   *
   * @param privateKey    a reference to the private key on card to be used for signing
   * @param signAgorithm standard digital signature algorithm name
   * @param padAlgorithm padding algorithm name, for example one of
   *         ISO9796,
   *         PKCS#1,
   *         ZEROPADDING
   * @param data          data to be signed
   *
   * @return signature
   *
   * @exception InvalidKeyException
   *            Thrown when the key is not valid or does not match the requested algorithm.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   *
   * @see JCAStandardNames
   */
  public byte[] signData  (PrivateKeyRef privateKey,
                           String signAgorithm,
                           String padAlgorithm,
                           byte[] data)
  throws CardServiceException, InvalidKeyException,CardTerminalException;

  /**
   * Generate a digital Signature on the provided hash.
   * Since hashing of large amounts of data may be slow if performed on card
   * this method allows to hash outside the card service and just perform
   * the signature operation on card.
   * Pad the hash and then
   * apply the PKA algorithm to the padded hash.
   * <p>
   * The padding algorithm is chosen as defined in the Java Cryptography Architecture Specification.
   * <p>
   * Use a key algorithm name (not a digital signature algorithm name, because digital
   * signature algorithms include hashing)
   * a defined in the Java Cryptography Architecture API Specification & Reference,
   * for example
   * <DL COMPACT>
   * <DT>    DSA<DD>  The asymmetric transformation described in NIST FIPS 186, described
   *                  as the "DSA Sign Operation" and the "DSA Verify Operation", prior to
   *                  creating a digest.  The input to DSA is always 20 bytes long.
   *
   * <DT>    RSA<DD>  The Rivest, Shamir and Adleman AsymmetricCipher algorithm. RSA
   *                  Encryption as defined in the RSA Laboratory Technical Note PKCS#1.
   * </DL>
   *
   * @param privateKey
   *         a reference to the private key on card to be used for signing
   * @param signAgorithm standard key algorithm name
   * @param hash the hash/digest to be signed
   *
   * @return signature
   *
   * @exception java.security.InvalidKeyException
   *            Thrown when the key is not valid or does not match the requested algorithm.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   *
   * @see JCAStandardNames
   */
  public byte[] signHash  (PrivateKeyRef privateKey,
                           String signAgorithm,
                           byte[] hash)
  throws CardServiceException, InvalidKeyException,CardTerminalException;

  /**
   * Generate a digital Signature on the provided hash
   * (Overloaded method that allows to specify the padding algorithm).
   * Since hashing of large amounts of data may be slow if performed on card
   * this method allows to hash outside the card service and just perform
   * the signature operation on card.
   * Pad the hash and then
   * apply the PKA algorithm to the padded hash.
   * <p>
   * Use a key algorithm name (not a digital signature algorithm name, because digital
   * signature algorithms include hashing)
   * a defined in the Java Cryptography Architecture API Specification & Reference,
   * for example
   * <DL COMPACT>
   * <DT>    DSA<DD>  The asymmetric transformation described in NIST FIPS 186, described
   *                  as the "DSA Sign Operation" and the "DSA Verify Operation", prior to
   *                  creating a digest.  The input to DSA is always 20 bytes long.
   *
   * <DT>    RSA<DD>  The Rivest, Shamir and Adleman AsymmetricCipher algorithm. RSA
   *                  Encryption as defined in the RSA Laboratory Technical Note PKCS#1.
   * </DL>
   *
   * @param privateKey
   *         a reference to the private key on card to be used for signing
   * @param signAgorithm standard key algorithm name
   * @param padAlgorithm padding algorithm name, for example one of
   *         ISO9796,
   *         PKCS#1,
   *         ZEROPADDING
   * @param hash the hash/digest to be signed
   *
   * @return signature
   *
   * @exception java.security.InvalidKeyException
   *            Thrown when the key is not valid or does not match the requested algorithm.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   *
   * @see JCAStandardNames
   */
  public byte[] signHash  (PrivateKeyRef privateKey,
                           String signAgorithm,
                           String padAlgorithm,
                           byte[] hash)
  throws CardServiceException, InvalidKeyException,CardTerminalException;

  /**
   * Verify a digital Signature including hashing.
   * First hash the data, then pad the hash,
   * apply the PKA algorithm to the padded hash, then compare the result
   * to the provided signature.
   * <p>
   * The padding algorithm is chosen as defined in the Java Cryptography Architecture Specification.
   * <p>
   * The standard algorithm name must be specified as defined in the
   * Java Cryptography Architecture API Specification & Reference,
   * for example
   * <DL COMPACT>
   * <DT>MD5withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                   AsymmetricCipher algorithm with the MD5 MessageDigest Algorithm.
   * <DT>MD2withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                   AsymmetricCipher algorithm with the MD2 MessageDigest Algorithm.
   * <DT>SHA1withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                    AsymmetricCipher algorithm with the SHA-1 MessageDigest Algorithm.
   * <DT>SHA1withDSA<DD>Digital Signature Algorithm, as defined in Digital Signature Standard,
   *                    NIST FIPS 186.  This standard defines a digital signature algorithm
   *                    that uses the RawDSA asymmetric transformation along with the SHA-1
   *                    message digest algorithm.
   * </DL>
   *
   * @param publicKey
   *         a reference to the public key on card to be used for signature validation
   * @param signAlgorithm standard digital signature algorithm name
   * @param data the data for which the signature should be verified
   * @param signature signature to be verified
   *
   * @return True if signature valdidation was successfull
   *
   * @exception java.security.InvalidKeyException
   *            Thrown when the key is not valid or does not match the requested algorithm.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   *
   * @see JCAStandardNames
   */
  public boolean verifySignedData(PublicKeyRef publicKey,
                                  String signAlgorithm,
                                  byte[] data,
                                  byte[] signature)
  throws CardServiceException, InvalidKeyException,CardTerminalException;

  /**
   * Verify a digital Signature including hashing
   * (overload method that allows to specify the padding algorithm to be used).
   * First hash the data, then pad the hash,
   * apply the PKA algorithm to the padded hash, then compare the result
   * to the provided signature.
   * <p>
   * The standard algorithm name must be specified as defined in the
   * Java Cryptography Architecture API Specification & Reference,
   * for example
   * <DL COMPACT>
   * <DT>MD5withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                   AsymmetricCipher algorithm with the MD5 MessageDigest Algorithm.
   * <DT>MD2withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                   AsymmetricCipher algorithm with the MD2 MessageDigest Algorithm.
   * <DT>SHA1withRSA<DD>The Signature algorithm obtained by combining the RSA
   *                    AsymmetricCipher algorithm with the SHA-1 MessageDigest Algorithm.
   * <DT>SHA1withDSA<DD>Digital Signature Algorithm, as defined in Digital Signature Standard,
   *                    NIST FIPS 186.  This standard defines a digital signature algorithm
   *                    that uses the RawDSA asymmetric transformation along with the SHA-1
   *                    message digest algorithm.
   * </DL>
   *
   * @param publicKey
   *         a reference to the public key on card to be used for signature validation
   * @param signAlgorithm standard digital signature algorithm name
   * @param padAlgorithm padding algorithm name, for example one of
   *         ISO9796,
   *         PKCS#1,
   *         ZEROPADDING
   * @param data the data for which the signature should be verified
   * @param signature signature to be verified
   *
   * @return True if signature valdidation was successfull
   *
   * @exception java.security.InvalidKeyException
   *            Thrown when the key is not valid or does not match the requested algorithm.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   *
   * @see JCAStandardNames
   */
  public boolean verifySignedData(PublicKeyRef publicKey,
                                  String signAlgorithm,
                                  String padAlgorithm,
                                  byte[] data,
                                  byte[] signature)
  throws CardServiceException, InvalidKeyException,CardTerminalException;

  /**
   * Verify a digital Signature.
   * Since hashing of large amounts of data may be slow if performed on card
   * this method allows to hash outside the card service and just perform
   * the signature verificationoperation on card.
   * Pad the provided hash,
   * apply the PKA algorithm to the padded hash, then compare the result
   * to the provided signature.
   * <p>
   * The padding algorithm is chosen as defined in the Java Cryptography Architecture Specification.
   * <p>
   * Use a key algorithm name (not a digital signature algorithm name, because digital
   * signature algorithms include hashing)
   * a defined in the Java Cryptography Architecture API Specification & Reference,
   * for example
   * <DL COMPACT>
   * <DT>    DSA<DD>  The asymmetric transformation described in NIST FIPS 186, described
   *                  as the "DSA Sign Operation" and the "DSA Verify Operation", prior to
   *                  creating a digest.  The input to DSA is always 20 bytes long.
   *
   * <DT>    RSA<DD>  The Rivest, Shamir and Adleman AsymmetricCipher algorithm. RSA
   *                  Encryption as defined in the RSA Laboratory Technical Note PKCS#1.
   * </DL>
   *
   *
   * @param publicKey
   *         a reference to the public key on card to be used for signature validation
   * @param signAlgorithm standard key algorithm name
   * @param hash
   *         The hash for which the signature should be verified.
   * @param signature signature to be verified
   *
   * @return True if signature valdidation was successfull
   *
   * @exception java.security.InvalidKeyException
   *            Thrown when the key is not valid or does not match the requested algorithm.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   *
   * @see JCAStandardNames
   */
  public boolean verifySignedHash(PublicKeyRef publicKey,
                                  String signAlgorithm,
                                  byte[] hash,
                                  byte[] signature)
  throws CardServiceException, InvalidKeyException,CardTerminalException;

  /**
   * Verify a digital Signature
   * (overloaded method that allows to specify the padding algorithm to be used).
   * Since hashing of large amounts of data may be slow if performed on card
   * this method allows to hash outside the card service and just perform
   * the signature verification operation on card.
   * Pad the provided hash,
   * apply the PKA algorithm to the padded hash, then compare the result
   * to the provided signature.
   * <p>
   * Use a key algorithm name (not a digital signature algorithm name, because digital
   * signature algorithms include hashing)
   * a defined in the Java Cryptography Architecture API Specification & Reference,
   * for example
   * <DL COMPACT>
   * <DT>    DSA<DD>  The asymmetric transformation described in NIST FIPS 186, described
   *                  as the "DSA Sign Operation" and the "DSA Verify Operation", prior to
   *                  creating a digest.  The input to DSA is always 20 bytes long.
   *
   * <DT>    RSA<DD>  The Rivest, Shamir and Adleman AsymmetricCipher algorithm. RSA
   *                  Encryption as defined in the RSA Laboratory Technical Note PKCS#1.
   * </DL>
   *
   *
   * @param publicKey
   *         a reference to the public key on card to be used for signature validation
   * @param signAlgorithm standard key algorithm name
   * @param padAlgorithm padding algorithm name, for example one of
   *         ISO9796,
   *         PKCS#1,
   *         ZEROPADDING
   * @param hash
   *         The hash for which the signature should be verified.
   * @param signature signature to be verified
   *
   * @return True if signature valdidation was successfull
   *
   * @exception java.security.InvalidKeyException
   *            Thrown when the key is not valid or does not match the requested algorithm.
   * @exception CardServiceException any subclass of CardServiceException
   * @exception CardTerminalException any subclass of CardTerminalException
   *
   * @see JCAStandardNames
   */
  public boolean verifySignedHash(PublicKeyRef publicKey,
                                  String signAlgorithm,
                                  String padAlgorithm,
                                  byte[] hash,
                                  byte[] signature)
  throws CardServiceException, InvalidKeyException,CardTerminalException;
}
