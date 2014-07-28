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



/**
 * A card service interface for Card Holder Verification (CHV).
 * CHV is typically performed by querying a password or PIN
 * (Personal Identification Number) from the user and presenting
 * it to the card for verification. The user's input has to be
 * converted into the format understood by the smartcard, for
 * example by BCD-packing and padding a PIN. The methods in this
 * interface expect this converted data, which is referred to as
 * <i>password</i>. Please note that there is no assumption on how
 * the password data is obtained and preprocessed. For example,
 * biometric scanning could be used instead of querying a PIN.
 *
 * @version $Id: CHVCardService.java,v 1.1 1999/11/10 09:05:21 damke Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 */
public interface CHVCardService extends CardServiceInterface
{
  /**
   * Determines the padded length of a password.
   * The returned value is required to pad passwords for verification.
   *
   * @param domain      The security domain in which the password resides.
   *                    <tt>null</tt> can be passed to refer to the root
   *                    domain on the smartcard.
   *                    <br>
   *                    For file system based smartcards, the security
   *                    domain is specified as a <tt>CardFilePath</tt>.
   *                    The root domain then corresponds to the master file.
   * @param number      The number of the password. This parameter
   *                    is used to distinguish between different passwords
   *                    in the same security domain.
   * @return  the number of data bytes for the specified password
   *
   * @exception CardServiceException
   *            if this service encountered an error.
   *            This may occur if the service needs to contact the smartcard
   *            in order to determine the password length. An exception may
   *            also be thrown if the service is unable to locate the security
   *            domain.
   * @exception CardTerminalException
   *            if the underlying card terminal encountered an error
   *            when communicating with the smartcard
   */
  public int getPasswordLength(SecurityDomain domain, int number)
       throws CardServiceException, CardTerminalException
  ;


  /**
   * Checks a password for card holder verification.
   * Note that repeated verification of a wrong password will typically
   * block that password on the smartcard.
   *
   * @param domain      The security domain in which to verify the password.
   *                    <tt>null</tt> can be passed to refer to the root
   *                    domain on the smartcard.
   *                    <br>
   *                    For file system based smartcards, the security
   *                    domain is specified as a <tt>CardFilePath</tt>.
   *                    The root domain then corresponds to the master file.
   * @param number      The number of the password to verify. This parameter
   *                    is used to distinguish between different passwords
   *                    in the same security domain.
   * @param password    The password data that has to be verified.
   *                    If the data is supplied, it has to be padded to the
   *                    length returned by <tt>getPasswordLength</tt> for
   *                    that password.
   *                    <br>
   *                    <tt>null</tt> may be passed to indicate that this
   *                    service should use a protected PIN path facility,
   *                    if available. Alternatively, this service may query
   *                    the password by some other, implementation-dependend
   *                    means. In any case, the service implementation will
   *                    require knowledge about the encoding of the password
   *                    data on the smartcard.
   *
   * @exception CardServiceException
   *            if this service encountered an error.
   *            In this context, it is not considered an error if the password
   *            to be verified is wrong. However, if the password is blocked
   *            on the smartcard, an exception will be thrown.
   * @exception CardTerminalException
   *            if the underlying card terminal encountered an error
   *            when communicating with the smartcard
   */
  public boolean verifyPassword(SecurityDomain domain, int number,
                                byte[] password)
       throws CardServiceException, CardTerminalException
  ;


  /**
   * Resets the achieved card holder verifications on the smartcard.
   * Typically, other permanent access conditions that have been satisfied
   * will also be reset. If the card does not allow to reset access
   * conditions for a specific application, it is expected that <i>all</i>
   * access conditions for all on-card applications are reset.
   *
   * <p>
   * The name of this method implies that an application on the smartcard
   * is first selected and then <i>opened</i> by performing card holder
   * verification, giving access to the application data. By resetting the
   * card holder verification, the on-card application therefore gets
   * <i>closed</i>.
   *
   * @param domain      the security domain for which to reset card holder
   *                    verifications
   *
   * @exception CardServiceException
   *            if this service, or the underlying implementation,
   *            encountered an error
   * @exception CardTerminalException
   *            if the underlying terminal encountered an error while
   *            communicating with the smartcard
   */
  public void closeApplication(SecurityDomain domain)
       throws CardServiceException, CardTerminalException
  ;

} // interface CHVCardService
