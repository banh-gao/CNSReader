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


import java.math.BigInteger;
import java.security.PublicKey;

import opencard.core.util.Tracer;

/** Contains a public RSA key.
  * implements the java.security.PrivateKey interface
  *
  * In this package OpenCard provides key classes for common algorithms
  * like RSA, DSA (or DES) that each concrete card service implementing
  * a card service interface should support instead of defining its own
  * key classes.
  * Only for new PKA algorithms that OpenCard does not yet support a
  * card service may define its own key classes.
  *
  * @author  Michael Baentsch (mib@zurich.ibm.com)
  * @version $Id: RSAPublicKey.java,v 1.1.1.1 1999/10/05 15:08:48 damke Exp $
  *
  * @see java.security.PrivateKey
  */
public class RSAPublicKey implements PublicKey {

  private Tracer itracer = new Tracer(this, RSAPublicKey.class);

  /** Length of public exponent */
  protected int el;

  /** Public exponent */
  protected BigInteger e = null;

  /** Modulus */
  protected BigInteger m = null;

  /** input data length */
  protected int inputLength;

  /** output data length */
  protected int outputLength;

  /** Key length (in bits) */
  protected int keyLength;

  /** Produce an <tt>RSAPublicKey</tt> from the given byte arrays.<p>
   *
   * @param    eLength
   *           Length of public exponent.
   * @param    e
   *           Public Exponent.
   * @param    m
   *           Modulus
   * @param    keyLength
   *           The nominal size of the key in bits.
   */
  public RSAPublicKey(int eLength, byte[] e, byte[] m, int keyLength) {
    this.el = eLength;
    this.e = new BigInteger(1, e);
    this.m = new BigInteger(1, m);
    this.keyLength = keyLength;
    this.inputLength = m.length;
    this.outputLength = m.length;
  }

  /** Produce an <tt>RSAPublicKey</tt> from the given BigIntegers.<p>
   *
   * @param    e  Public Exponent.
   * @param    m  Modulus.
   */
  public RSAPublicKey(BigInteger e, BigInteger m) {
    this.e = e;
    this.m = m;
    this.el = (e.bitLength()+7) / 8;
    this.keyLength = m.bitLength();
    this.inputLength = (keyLength+7)/8;
    this.outputLength = (keyLength+7)/8;
  }

  /** Conformance to the java.security interface
   * @see java.security.PublicKey
   */
  public String getAlgorithm() {
    return ("RSA");
  }

  /** Conformance to the java.security interface
   * @see java.security.PublicKey
   */
  public byte[] getEncoded() {
    return null;
  }

  /** Conformance to the java.security interface
   * @see java.security.PublicKey
   */
  public String getFormat() {
    return null;
  }

  /** Returns the number of bytes to be input into a signing operation
   * with this key.<p>
   *
   * @return Input data length.
   */
  public int maxInputLength() {
    return inputLength;
  }

  /** Returns the number of bytes to be generated by a signing operation
   * with this key.<p>
   *
   * @return Output data length.
   */
  public int maxOutputLength() {
    return outputLength;
  }

  /** Return modulus of this key.<p>
   *
   * @return Modulus of this key
   */
  public BigInteger modulus() {
    return m;
  }

  /** Return Public exponent.<p>
   *
   * @return Public exponent of this key
   */
  public BigInteger publicExponent() {
    return e;
  }
}
