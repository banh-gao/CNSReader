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


/**
 * Java cryptography standard algorithm names as used by the signature card services.
 * <br>
 * For an exact definition of the algorithms see
 * <a href = "http://java.sun.com/products/jdk/1.2/docs/guide/security/CryptoSpec.html#AppA">
 * http://java.sun.com/products/jdk/1.2/docs/guide/security/CryptoSpec.html</a>.
 *
 * @version $Id: JCAStandardNames.java,v 1.1.1.1 1999/10/05 15:08:48 damke Exp $
 *
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 */
public interface JCAStandardNames {
  /** SHA-1 hash and RSA pka algorithm */
  public static final String SHA1_RSA      = "SHA1withRSA";
  /** RSA pka algorithm (without hashing) */
  public static final String RAW_RSA       = "RSA";
  /** DSA pka algorithm including SHA-1 hashing */
  public static final String SHA1_DSA      = "SHA1withDSA";
  /** DSA assymetric PKA algorithm (without hashing) */
  public static final String RAW_DSA       = "DSA";
  /** SHA-1 hash */
  public static final String SHA1          = "SHA1";
  /** MD5 hash */
  public static final String MD5           = "MD5";
  /** zero padding */
  public static final String ZERO_PADDING  = "ZEROPADDING";
  /**PKCS # 1 padding */
  public static final String PKCS_PADDING  = "PKCS#1";
  /**PKCS # 8 padding */
  public static final String PKCS8_PADDING = "PKCS#8";
  /** ISO 9796 */
  public static final String ISO_PADDING   = "ISO9796";
  /** DES ciphering */
  public static final String DES_CIPHER    = "DES";
}