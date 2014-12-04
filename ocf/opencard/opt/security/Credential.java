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


/**
 * A tag interface for cryptographic credentials.
 * Most smartcards are able to protect the access to the data stored on
 * them by means of cryptography. In order to overcome such protection,
 * the terminal side of an application will have to provide some kind of
 * secret data to the card services it is using. That secret data is
 * referred to as credentials. Credentials are collected in instances
 * of class <tt>CredentialStore</tt>.
 * <br>
 * Credentials can take different formats, depending on the smartcard used
 * as well as on the card service that provides access to the smartcard.
 * For example, a credential may be a key to the DES or 3DES algorithm, or a
 * private key to the RSA algorithm.
 * It does not even have to be a simple key. For example, a credential may
 * provide the implementation of the algorithm, like DES or 3DES, along with
 * the key.
 * A card service requiring such a credential will define an interface,
 * extending this one, which provides the cryptographic operations. These
 * operations are then executed by invoking the application's credential.
 * <br>
 * Encapsulating the algorithm within the credential has several advantages.
 * First of all, the terminal application does not have to provide the secret
 * data to the service directly. Second, the credential can be implemented as
 * a pure software solution, optionally using a cryptographic framework like
 * the Java Cryptography Extension (JCE), but it may also use a hardware
 * implementation of the cryptographic algorithm. The card services will
 * not have to be adapted to the platform in order to make use of such
 * frameworks or special hardware. Last but not least, card services that
 * include cryptographic code are subject to US export restrictions.
 *
 * <p>
 * Since there are few cryptographic algorithms ferquently used by smartcards,
 * namely DES and 3DES, credential interfaces for these algorithms may be
 * defined by OCF in the future.
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @version $Id: Credential.java,v 1.1.1.1 1999/10/05 15:08:47 damke Exp $
 *
 * @see opencard.core.service.CardService
 * @see CredentialBag
 * @see CredentialStore
 */
public interface Credential
{
  // no methods
}
