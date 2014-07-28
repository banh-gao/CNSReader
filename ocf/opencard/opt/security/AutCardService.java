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


import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;
import opencard.opt.service.CardServiceInterface;



/*
 * A card service interface for authentication of card and outside world.
 * SmartCards typically support <i>external authentication</i> and
 * <i>internal authentication</i>. External authentication means that
 * the outside world authenticates itself against the card. The card
 * may request this kind of authentication before giving access to
 * data stored on it.
 * <br>
 * On the other hand, internal authentication means that the card
 * authenticates itself against the outside world. This is one way
 * for a host or terminal application (being part of the outside world)
 * to detect and reject forged smartcards.
 *
 * @version $Id: AutCardService.java,v 1.1 1999/11/10 13:55:45 damke Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public interface AutCardService extends CardServiceInterface
{
  /**
   * Determines the required length of a challenge for internal authentication.
   * The terminal or host application can then prepare a random challenge of
   * the given length to invoke <tt>internalAuthenticate</tt>.
   *
   * @param key      a reference identifying the key to be used for
   *                    internal authentication in a subsequent invocation
   *
   * @return    the number of bytes in an appropriate random challenge.
   *            The card may be able to deal with other sizes of random
   *            challenges. For example, if a public key algorithm is
   *            used, the card may be able to pad shorter challenges to
   *            the required block length. The value returned here could
   *            the be the maximum length that a random challenge may have.
   *            For secret key algorithms, the value returned typically
   *            is the block size, and no other sizes are acceptable.
   *
   * @exception CardServiceException
   *            if this service encountered an error.
   *            This may occur if the service needs to contact the smartcard
   *            in order to determine the challenge length. An exception may
   *            also be thrown if the service is unable to locate the security
   *            domain.
   * @exception CardTerminalException
   *            if the underlying card terminal encountered an error
   *            when communicating with the smartcard
   */
  public int getChallengeLength(KeyRef key)
       throws CardServiceException, CardTerminalException
  ;


  /**
   * Requests an internal authentication from the smartcard.
   * The smartcard will take the random challenge passed as argument,
   * perform some operations involving a secret, and return the result
   * of these operations. The terminal or host application can then
   * check the result to verify that the secret used by the card is
   * the expected one, thereby authenticating the card.
   *
   * @param keyref      a reference identifying the key to be used for
   *                    internal authentication
   * @param challenge   the random challenge to be used by the smartcard
   *
   * @return    a byte array holding the result of the smartcard's operations.
   *            It is up to the application to verify that this result is the
   *            expected one. (You wouldn't want to trust a smartcard just
   *            because this method returned <tt>true</tt>, would you?)
   *
   * @exception CardServiceException
   *            if this service encountered an error
   * @exception CardTerminalException
   *            if the underlying card terminal encountered an error
   *            when communicating with the smartcard
   */
  public byte[] internalAuthenticate(KeyRef keyref, byte[] challenge)
       throws CardServiceException, CardTerminalException
  ;


  /**
   * Performs an external authentication against the smartcard.
   * The smartcard will provide a random challenge, which is signed
   * using the credential passed as an argument. The result of this
   * operation is sent back to the smartcard and verified there.
   * If the credential provided by the terminal or host application
   * is the one expected by the smartcard, the external authentication
   * succeeds. Otherwise, it fails. Note that repeated failed attempts
   * to authenticate may cause the smartcard to permanently block the
   * key that is used to check the authentication.
   *
   * @param keyref      a reference identifying the key to be used for
   *                    external authentication
   * @param credential  the secret to be used to authenticate the application
   *                    against the smartcard. The secret will be used only
   *                    once, and before this method returns. The application
   *                    may take actions to ensure that the secret cannot be
   *                    used arbitrarily often, or after this method returned.
   *
   * @return    <tt>true</tt> if the smartcard accepted the credential and
   *            therefore considers the outside world to be authenticated,
   *            <tt>false</tt> if the smartcard rejected to authenticate
   *            the outside world.
   *
   * @exception CardServiceException
   *            if this service encountered an error. In this context, it
   *            is not considered an error if the smartcard rejected the
   *            authentication. However, an exception will be thrown if the
   *            referred key on the smartcard is blocked so that authentication
   *            using this key becomes impossible.
   * @exception CardTerminalException
   *            if the underlying card terminal encountered an error
   *            when communicating with the smartcard
   */
  public boolean externalAuthenticate(KeyRef keyref, SignCredential credential)
       throws CardServiceException, CardTerminalException
  ;


  /**
   * Resets the achieved external authentications on the smartcard.
   * Typically, other permanent access conditions that have been satisfied
   * will also be reset. If the card does not allow to reset access
   * conditions for a specific application, it is expected that <i>all</i>
   * access conditions for all on-card applications are reset.
   *
   * <p>
   * The name of this method implies that an application on the smartcard
   * is first selected and then <i>opened</i> by performing external
   * authentication, giving access to the application data. By resetting the
   * external authentication, the on-card application therefore gets
   * <tt>closed</tt>.
   *
   * <p>
   * External authentication as well as Card Holder Verification (CHV) can
   * be required to satisfy access conditions that are imposed by the card.
   * The operation expected to be performed when invoking this method is to
   * reset the state in the smartcard so that these access conditions are no
   * longer satisfied. This avoids that another terminal or host application
   * accesses the smartcard's data without proving it's authorization first.
   * The signature of the method here is the same as in the interface
   * <tt>CHVCardService</tt>, since both are expected to do the same thing.
   *
   * @param domain      the security domain for which to reset external
   *                    authentications
   *
   * @exception CardServiceException
   *            if this service, or the underlying implementation,
   *            encountered an error
   * @exception CardTerminalException
   *            if the underlying terminal encountered an error while
   *            communicating with the smartcard
   *
   * @see CHVCardService#closeApplication
   */
  public void closeApplication(SecurityDomain domain)
       throws CardServiceException, CardTerminalException
  ;

} // interface AutCardService
