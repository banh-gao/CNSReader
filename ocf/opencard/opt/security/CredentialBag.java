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

import java.lang.reflect.Array;
import java.util.Vector;

import opencard.core.terminal.CardID;

/**
 * A container for stores that hold cryptographic credentials.
 * Smartcards may protect access to the data stored on them by means of
 * cryptography. Applications have to provide cryptographic credentials
 * to the card services they are using, so the services can overcome this
 * protection. The credentials for a particular smartcard are collected
 * in instances of class <tt>CredentialStore</tt>. These stores are
 * collected in instances of this class.
 * <br>
 * An application that supports different smartcards will set up a store
 * for each of the cards. Then, it puts all these stores into a bag. This
 * bag is passed to the card service that is used to access a particular
 * smartcard that has been inserted. The card service will pick the right
 * store from the bag and use the credentials in that store. That way, the
 * application does not have to worry about which particular card it is
 * currently working with.
 *
 * @author   Reto Hermann (rhe@zurich.ibm.com)
 * @author   Roland Weber (rolweber@de.ibm.com)
 * @version  $Id: CredentialBag.java,v 1.1.1.1 1999/10/05 15:08:47 damke Exp $
 *
 * @see Credential
 * @see CredentialStore
 * @see opencard.core.service.CardService
 */
public class CredentialBag
{
  /** The container to hold the <tt>CredentialStore</tt> objects. */
  protected Vector credentialBag;


  /** Instantiates an empty credential bag. */
  public CredentialBag()
  {
    credentialBag = new Vector();
  }

  /**
   * Adds a store to this bag.
   *
   * @param credstore
   *        The <tt>CredentialStore</tt> object to be added.
   */
  public void addCredentialStore(CredentialStore credstore)
  {
    if((credstore != null) &&
       !credentialBag.contains(credstore))
      credentialBag.addElement(credstore);
  }


  /**
   * Retrieves a single store from this bag.
   * This method searches for a store that supports a particular
   * smartcard and is an instance of a given class or interface.
   * One matching store is returned. To get all matching stores,
   * use <tt>getCredentialStores</tt>.
   *
   * @param cardID  the identifier of the smartcard to support
   * @param clazz   the type of the store to return.
   *                Use <tt>CredentialStore.class</tt> if the
   *                type does not matter.
   *
   * @return  a store matching the criteria,
   *          or <tt>null</tt> if none is found
   *
   * @see #getCredentialStores
   */
  final public CredentialStore getCredentialStore(CardID cardID, Class clazz)
  {
    if (clazz == null)
      clazz = CredentialStore.class;

    CredentialStore store = null;
    int      size  = credentialBag.size();

    for(int i=0; ((store==null)&&(i<size)); i++)
      {
        CredentialStore cs = (CredentialStore) credentialBag.elementAt(i);

        if (clazz.isInstance(cs) && cs.supports(cardID))
          store = cs;
      }

    return store;
  }


  /**
   * Retrieves stores from this bag.
   * This method searches for all stores that support a particular smartcard
   * and are instances of a given class or interface.
   * The selection by card is necessary since an application may provide
   * different keys for different smartcards it resides on. The additional
   * selection by the type (or class) of the store is also necessary, since
   * a card service that searches for an appropriate store will require
   * particular access methods not defined in the base class.
   *
   * @param cardID  the identifier of the smartcard to support
   * @param clazz   the type of the store to return.
   *                Use <tt>CredentialStore.class</tt> if the
   *                type does not matter.
   *
   * @return  an array holding all matching stores,
   *          or <tt>null</tt> if none is found.
   *          The array element type is the class passed as argument, so the
   *          whole array can be down-casted instead of each element.
   */
  final public CredentialStore[] getCredentialStores(CardID cardID,
                                                     Class clazz)
  {
    if (clazz == null)
      clazz = CredentialStore.class;

    // We have to check each element in the vector and then copy the matching
    // elements to a new array. To minimize the number of invocations of
    // CredentialStore.supports, the matching elements are copied to an
    // oversized array with generic type. The matches are copied to a second
    // array of appropriate size and type, which is then returned.

    int              size = credentialBag.size();
    CredentialStore[] csa = new CredentialStore [size];
    int             count = 0;

    for(int i=0; i<size; i++)
      {
        CredentialStore cs = (CredentialStore) credentialBag.elementAt(i);
        if (clazz.isInstance(cs) && cs.supports(cardID))
          csa[count++] = cs;
      }

    // now copy the matches to a new array of appropriate size

    CredentialStore[] stores = null;

    if (count > 0)
      {
        stores = (CredentialStore[]) Array.newInstance(clazz, count);
        for(int i=0; i<count; i++)
          stores[i] = csa[i];
      }

    return stores;

  } // getCredentialStores

} // class CredentialBag
