package opencard.opt.security;

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

import java.math.BigInteger;
import java.security.PrivateKey;

import opencard.core.util.Tracer;

/** Contains an RSA private key.
  * Implements interface java.security.PrivateKey.
  * For an alternative class that allows signing via the chinese remainder's algorithm
  * see RSACRTKey.
  *
  * In this package OpenCard provides key classes for common algorithms
  * like RSA, DSA (or DES) that each concrete card service implementing
  * a card service interface should support instead of defining its own
  * key classes.
  * Only for new PKA algorithms that OpenCard does not yet support a
  * card service may define its own key classes.
  *
  * @author  Michael Baentsch (mib@zurich.ibm.com)
  * @version $Id: RSAPrivateKey.java,v 1.2 1999/10/20 13:15:00 damke Exp $
  *
  * @see java.security.PrivateKey
  * @see RSACRTKey
  */
public class RSAPrivateKey implements PrivateKey {

  private Tracer itracer = new Tracer(this, RSAPrivateKey.class);

  /** Modulus */
  protected BigInteger m_ = null;

  /** Exponent */
  protected BigInteger e_ = null;

  /** input data length */
  protected int inputLength_;

  /** output data length */
  protected int outputLength_;

  /** Key length (in bits) */
  protected int keyLength_;

  /** Produce an <tt>RSAPrivateKey</tt> from the given byte arrays.<p>
   *
   * @param    m
   *           Modulus
   * @param    e
   *           private exponent
   * @param    inputLength
   *           Number of bytes accepted for input to signature routine.
   * @param    outputLength
   *           Number of bytes produced by signature routine.
   * @param    keyLength
   *           The nominal size of the key in bits.
   */
  public RSAPrivateKey(byte[] m, byte[] e, int inputLength,
					   int outputLength, int keyLength) {
	m_ = new BigInteger(1, m);
	e_ = new BigInteger(1, e);
	keyLength_ = keyLength;
	inputLength_ = inputLength;
	outputLength_ = outputLength;
  }
  /** Produce an <tt>RSAPrivateKey</tt> from the given byte arrays.<p>
   *
   * @param    m
   *           Modulus
   * @param    e
   *           private exponent
   */
  public RSAPrivateKey(BigInteger e, BigInteger m) {
	m_ = m;
	e_ = e;
	keyLength_ = m.bitLength();
	inputLength_ = (keyLength_+7)/8;
	outputLength_ = (keyLength_+7)/8;

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
	return inputLength_;
  }
  /** Returns the number of bytes to be generated by a signing operation
	* with this key.<p>
	*
	* @return Output data length.
	*/
  public int maxOutputLength() {
	return outputLength_;
  }
  /** Return modulus of this key.<p>
	*
	* @return Modulus of this key
	*/
  public BigInteger modulus() {
	return m_;
  }
  /** Return Private exponent.<p>
	*
	* @return Private exponent of this key
	*/
  public BigInteger privateExponent() {
	return e_;
  }
}
