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


/**
 * An interface for card services with cryptographic security mechanisms.
 * It defines a standard way to pass credentials to a card service. It does
 * not define any methods that could be used to access the smartcard.
 * Therefore, it does not make sense for an application to request a card
 * service that implements this interface.
 * <br>
 * Applications have to provide credentials to card services in order to
 * satisfy access conditions involving cryptography. The credentials are
 * collected in card-specific credential stores, which are collected in
 * credential bags. The bag is what has to be provided to the card service.
 * <br>
 * One example for a card service that extends this interface is the service
 * that provides access to ISO file systems, <tt>FileAccessCardService</tt>.
 * Other services that require credentials should extend it, too. An
 * application can then check by the <tt>instanceof</tt> operator whether
 * a service may need credentials or not. Card service implementors can
 * either support the credentials provided in opencard.opt.security or 
 * support credentials in a base class common to all services that need them.
 * <br>
 * Smartcards that support cryptographic security typically support different
 * key domains. For example, file system based smartcards use elementary
 * files to store keys, and in each directory, different keys may be defined.
 * The card resident parts of each application supported by a smartcard will
 * be stored in an application directory, so each application can use it's
 * private credentials. Therefore, along with the credential bag to use, a
 * security domain has to be given. The security domain can for example bed
 * specified by a path to the directory in which the application resides.
 * <br>
 * SecurityDomain is simply a tag interfaces for classes which can be used
 * to identify a security domain. Different card services may support 
 * different types of SecurityDomain. For file system based smartcards
 * class opencard.opt.iso.fs.CardFilePath implements the SecurityDomain interface.
 *
 *
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 *
 * @version $Id: SecureService.java,v 1.1.1.1 1999/10/05 15:08:48 damke Exp $
 *
 * @see opencard.core.service.CardService
 * @see Credential
 * @see CredentialStore
 * @see CredentialBag
 * @see opencard.opt.iso.fs.CardFilePath
 * @see opencard.opt.iso.fs.FileAccessCardService
 */
public interface SecureService
{
  /**
   * Provides credentials to a card service.
   * The security domain should be specified as the path to the directory
   * in which the application's card resident parts are located. The bag
   * of credentials should hold a credential store suitable for the respective
   * card and card service implementation. Only credentials in that store will
   * (and can) be used by the service.
   *
   * @param domain      the security domain for which to provide credentials
   * @param creds       the credentials for that domain
   *
   * @see opencard.core.service.CardService
   *
   * @exception CardServiceException
   *            If the card service could not process the credentials,
   *            if the SecurityDomain is invalid.   
   */
  public void provideCredentials(SecurityDomain domain, CredentialBag creds)
       throws CardServiceException;


} // interface SecureService
