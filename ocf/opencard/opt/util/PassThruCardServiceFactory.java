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

package opencard.opt.util;

import java.util.Enumeration;
import java.util.Vector;

import opencard.core.terminal.CardID;
import opencard.opt.service.OCF11CardServiceFactory;

/**
 * A factory for creating low level card services for any smartcard.
 * The only service this factory can create is <tt>PassThruCardService</tt>,
 * which is available for all smartcards. This class serves as an example
 * for implementing a simple card service factories, and provides a
 * "quick start" for people who want to become familiar with smartcards
 * and the OpenCard Framework.
 * <br>
 * To use the service instantiated by this factory, add
 * <tt>opencard.opt.util.PassThruCardServiceFactory</tt> to the
 * attribute <tt>OpenCard.services</tt> in your <tt>opencard.properties</tt>
 * file, preferrably as the last entry.
 *
 * @author Thomas Schaeck (schaeck@de.ibm.com)
 * @author Roland Weber  (rolweber@de.ibm.com)
 *
 * @version $Id: PassThruCardServiceFactory.java,v 1.1.1.1 1999/10/05 15:08:48 damke Exp $
 *
 * @see PassThruCardService
 */
public final class PassThruCardServiceFactory extends OCF11CardServiceFactory
{
  /**
   * A vector holding the supported card service classes.
   * It is used to create an enumeration in <tt>cardServiceClasses</tt>.
   *
   * @see #cardServiceClasses
   */
  private final Vector service_classes = new Vector();


  /**
   * Instantiates a new factory for low level card services.
   */
  public PassThruCardServiceFactory()
  {
    super();
    service_classes.addElement(PassThruCardService.class);
  }


  /**
   * Tests whether this factory supports a given smartcard.
   * The smartcard is identified by it's ATR (Answer To Reset), which is
   * encapsulated in a <tt>CardID</tt> object. Since all smartcards can
   * receive APDUs and send responses to them, this factory supports all
   * cards. If a smartcard is supported, the card services this factory
   * is able to instantiate can be obtained using <tt>cardServiceClasses</tt>.
   *
   * @param cardID    the ATR of the smartcard to test for
   * @return   <tt>true</tt>, since this factory supports all smartcards
   *
   * @see #cardServiceClasses
   */
  public final boolean knows(CardID cardID)
  {
    return true;
  }


  /**
   * Returns an enumeration of known card service classes.
   * <i>Known</i> classes are those that can be instantiated by this
   * factory for the given smartcard. To check whether a factory is able
   * to instantiate any service at all for that smartcard, <tt>knows</tt>
   * can be invoked. Since this factory supports all smartcards, and can
   * only instantiate one service, this method returns a constant value.
   *
   * @param cardID    the ATR of the smartcard to test for
   * @return    an enumeration holding <tt>PassThruCardService.class</tt>
   *            as it's only element
   *
   * @see #knows
   */
  public final Enumeration cardServiceClasses(CardID cardID)
  {
    return service_classes.elements();
  }

} // class PassThruCardServiceFactory

