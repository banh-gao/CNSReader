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


import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.DSAPrivateKey;

import opencard.core.OpenCardRuntimeException;
import opencard.opt.signature.JCAStandardNames;

/** Sample credential to perform a DSA signing operation.
 * This credential does not hash a message nor pad input blocks which are too small.
 * It only accepts data which matches the key's input length and performs the
 * raw DSA algorithm.
 *
 * This credential can be used for external authentication.
 *
 * @see PKACredential
 * @see SignCredential
 */
public class DSASignCredential implements PKACredential, SignCredential {

  protected DSAPrivateKey privKey = null;
  protected Signature JCAsignature = null;

  /** Constructor using a DSA private key 
   */
  public DSASignCredential(DSAPrivateKey privateKey) {
     privKey = privateKey;
  }

  /** Compute a signature over a block of data.
   * @param data Data to be signed. Must have length
   *             getInputLength().
   * @exception OpenCardRuntimeException
   *            Invalid input block length or invalid key.
   */
  public byte[] sign(byte[] data) throws OpenCardRuntimeException {
    if (data.length != getInputLength()) {
      throw new OpenCardRuntimeException
        ("DSASignCredential: invalid input block length");
    }

    try {
      // get new JCA signature provider
      if (JCAsignature == null) {
        JCAsignature = Signature.getInstance(JCAStandardNames.SHA1_DSA);
        JCAsignature.initSign(privKey);
      }
      // sign data
      JCAsignature.update(data);
      return JCAsignature.sign();

    } catch (NoSuchAlgorithmException e) {
      throw new OpenCardRuntimeException
        ("DSASignCredential: no JCA provider for DSA available");
    } catch (InvalidKeyException e) {
      throw new OpenCardRuntimeException
        ("DSASignCredential: key rejected by JCA DSA provider");
    } catch (SignatureException e) {
      throw new OpenCardRuntimeException
        ("DSASignCredential: SignatureException: " + e.getMessage());
    }
  }

  public int getInputLength() {
    return (privKey.getParams().getP().bitLength() / 8 );
  }  
}

