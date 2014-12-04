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

import java.util.Enumeration;
import java.util.Hashtable;

import opencard.core.terminal.CardID;


/**
 * A container for cryptographic credentials.
 * Smartcards may protect access to the data stored on them by means of
 * cryptography. The card services that are used to access that data will
 * then require credentials to overcome this protection. The cryptographic
 * algorithms that have to be used depend on the smartcard's OS. The kind
 * of credential that has to be presented to the card service depends on
 * the algorithm to support as well as on the service. For example, if the
 * algorithm is DES, the credential may be a DES key, or an implementation
 * of the DES algorithm for a specific key.
 * <br>
 * As a result of these dependencies, card services supporting a particular
 * smartcard or smartcard family will define specific credentials that have
 * to be provided to them. Additionally, they will define a store into which
 * only these credentials can be put. This class is the abstract base class
 * of such stores. Methods to put credentials into it as well as to retrieve
 * them have to be added in dervied classes, since at least the type of the
 * credentials is service specific.
 * <br>
 * Applications will collect their credentials for a specific smartcard in
 * a credential store. Credential stores are collected in instances of class
 * <tt>CredentialBag</tt>, so the same application may support different
 * smartcards without having to worry about which one is inserted.
 *
 *
 * @author   Reto Hermann (rhe@zurich.ibm.com)
 * @author   Thomas Schaeck (schaeck@de.ibm.com)
 * @author   Roland Weber (rolweber@de.ibm.com)
 * @version  $Id: CredentialStore.java,v 1.1.1.1 1999/10/05 15:08:47 damke Exp $
 *
 * @see Credential
 * @see CredentialBag
 * @see opencard.core.service.CardService
 */
public abstract class CredentialStore
{
  /** A place to store the credentials. */
  private Hashtable credentialTable = null;


  /**
   * Creates a new generic store for credentials.
   */
  protected CredentialStore()
  {
    credentialTable = new Hashtable();
  }


  /**
   * Tests whether this store supports a particular card.
   * Providers of card services that require credentials have to provide
   * an appropriate <tt>CredentialStore</tt> class which supports the
   * same cards as the services.
   *
   * @param cardID   the ATR of the smartcard to test for
   * @return    <tt>true</tt> if the card is supported,
   *            <tt>false</tt> otherwise
   */
  public abstract boolean supports(CardID cardID);


  /**
   * Instantiates a new credential store.
   * The class to instantiate has to provide a default constructor.
   *
   * @param className   a subclass of <tt>CredentialStore</tt> to instantiate
   * @return            a new instance of the argument class,
   *                    or <tt>null</tt> if the instantiation failed
   */
  public static CredentialStore getInstance(String className)
  {
    CredentialStore store = null;
    try {
      Class clazz  = Class.forName(className);
      store        = (CredentialStore) clazz.newInstance();
    } catch (Exception e) {
      // ignore
    }
    return store;

  } // getInstance


  /**
   * Stores a credential.
   * The credential can be retrieved using <tt>fetchCredential</tt> with
   * an identifier equal to the one passed on storing it.
   * <br>
   * The credentials are stored in a hashtable. The identifier therefore
   * has to implement <tt>hashCode</tt> and <tt>equals</tt> appropriately.
   * This method is protected since stores supporting a particular card
   * will require a particular kind of credentials to be stored in them.
   * They will also define an appropriate identifier for credentials.
   *
   * @param credID   an identifier for the credential
   * @param cred     the credential to be stored
   *
   * @see #fetchCredential
   * @see java.util.Hashtable
   * @see java.lang.Object#hashCode
   * @see java.lang.Object#equals
   */
  protected final void storeCredential(Object credID, Credential cred)
  {
    if (cred != null)
      credentialTable.put(credID, cred);
  }


  /**
   * Retrieves a credential.
   * This method returns the last credential that was passed to
   * <tt>storeCredential</tt> with an identifier that equals the
   * argument.
   *
   * @param credID  an identifier for the credential to retrieve
   *
   * @return    the credential for the given identifier,
   *            or <tt>null</tt> if not found
   *
   * @see #storeCredential
   */
  protected final Credential fetchCredential(Object credID)
  {
    return (Credential) credentialTable.get(credID);
  }


  /**
   * Gets the identifiers of all credentials stored.
   *
   * @return  an enumeration of all identifiers in this store
   *
   * @see #storeCredential
   */
  protected final Enumeration getCredentialIDs()
  {
    return credentialTable.keys();
  }

} // class CredentialStore
