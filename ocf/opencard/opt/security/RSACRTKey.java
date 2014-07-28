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
import java.security.PrivateKey;

import opencard.core.util.Tracer;

/** Contains an RSA key in a form suitable for
  * fast signing via the chinese remainder's algorithm<p>.
  *
  * In this package OpenCard provides key classes for common algorithms
  * like RSA, DSA (or DES) that each concrete card service implementing
  * a card service interface should support instead of defining its own
  * key classes.
  * Only for new PKA algorithms that OpenCard does not yet support a
  * card service may define its own key classes.
  *
  * @author  Michael Baentsch (mib@zurich.ibm.com)
  * @version $Id: RSACRTKey.java,v 1.1.1.1 1999/10/05 15:08:48 damke Exp $
  *
  * @see java.security.PrivateKey
  */
public class RSACRTKey implements PrivateKey {

  private Tracer itracer = new Tracer(this, RSACRTKey.class);

  /** Prime p */
  protected BigInteger p = null;

  /** Prime q */
  protected BigInteger q = null;

  /** (inverse of q) mod p */
  protected BigInteger qm = null;

  /** d mod (p-1) */
  protected BigInteger dp = null;

  /** d mod (q-1) */
  protected BigInteger dq = null;

  /** input data length */
  protected int inputLength;

  /** output data length */
  protected int outputLength;

  /** Key length (in bits) */
  protected int keyLength;

  /** Produce an <tt>RSACRTKey</tt> from the given byte arrays.<p>
   *
   * @param    p
   *           Prime p.
   * @param    q
   *           Prime q.
   * @param    qm           (inverse of q) mod p
   * @param    dp
   *           d mod (p-1)
   * @param    dq
   *           d mod (q-1)
   * @param    inputLength
   *           Number of bytes accepted for input to signature routine.
   * @param    outputLength
   *           Number of bytes produced by signature routine.
   * @param    keyLength
   *           The nominal size of the key in bits.
   */
  public RSACRTKey(byte[] p, byte[] q, byte[] qm, byte[] dp, byte[] dq,
               int inputLength, int outputLength, int keyLength) {
    this.p = new BigInteger(1, p);
    this.q = new BigInteger(1, q);
    this.qm = new BigInteger(1, qm);
    this.dp = new BigInteger(1, dp);
    this.dq = new BigInteger(1, dq);
    this.keyLength = keyLength;
    this.inputLength = inputLength;
    this.outputLength = outputLength;
  }

  /** Produce an <tt>RSACRTKey</tt> from the given BigIntegers.<p>
   *
   * @param    p            Prime p.
   * @param    q            Prime q.
   * @param    qm           (inverse of q) mod p
   * @param    dp           d mod (p-1)
   * @param    dq           d mod (q-1)
   */
  public RSACRTKey(BigInteger p, BigInteger q, BigInteger qm,
               BigInteger dp, BigInteger dq) {
    this.p = p;
    this.q = q;
    this.qm = qm;
    this.dp = dp;
    this.dq = dq;
    this.keyLength = p.multiply(q).bitLength();
    this.inputLength = (keyLength+7)/8;
    this.outputLength = (keyLength+7)/8;
  }

  /** Coefficient of CRT representation.<p>
   *
   * @return Coefficient of CRT representation ((inverse of q) mod p)
   */
  public BigInteger coefficient() {
    return qm;
  }

  /** First Exponent of CRT representation.<p>
   *
   * @return First Exponent of CRT representation
   */
  public BigInteger exponent1() {
    return dp;
  }

  /** Second Exponent of CRT representation.<p>
   *
   * @return Second exponent of CRT representation
   */
  public BigInteger exponent2() {
    return dq;
  }

  /** Conformance to the java.security interface
   * @see java.security.PrivateKey
   */
  public String getAlgorithm() {
    return ("RSA");
  }

  /** Conformance to the java.security interface
   * @see java.security.PrivateKey
   */
  public byte[] getEncoded() {
    return null;
  }

  /** Conformance to the java.security interface
    * @see java.security.PrivateKey
    */
  public String getFormat() {
    return null;
  }

  /** Returns the number of bytes to be input into a signing operation
   * with this key.<p>
   *
   * @return Input data length.
   */
  public int getInputLength() {
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

  /** First Prime of CRT representation.<p>
   *
   * @return First prime of CRT representation
   */
  public BigInteger prime1() {
    return p;
  }

  /** Second Prime of CRT representation.<p>
   *
   * @return Second prime of CRT representation
   */
  public BigInteger prime2() {
    return q;
  } 
}
