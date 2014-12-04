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

package opencard.opt.emv.mgmt;


import java.util.Enumeration;
import java.util.Vector;

import opencard.core.service.CardServiceFactory;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.CardType;
import opencard.core.terminal.CardID;

/**
 * A primitive factory for creating <tt>BasicEMVAppletAccess</tt>
 * Card Services. This factory includes no Card Recognition and
 * assumes to handle EMV compliant cards. The factory is intended
 * for usage when testing the AppletAccessCardService with EMV cards.<p>
 *
 * @author   Thomas Stober (tms@de.ibm.com)
 * @version  $Id: EMVCardServiceFactory.java,v 1.1 1999/11/23 10:24:17 damke Exp $
 *
 * @see opencard.opt.applet.mgmt.AppletAccessCardService
 * @see opencard.opt.applet.mgmt.AbstractAppletAccessor
 */

public class EMVCardServiceFactory extends CardServiceFactory
{
  private static Vector services=new Vector();


  // define the CardServices available for smartcard for windows
  static
  {
    services.addElement(opencard.opt.emv.mgmt.BasicEMVAppletAccess.class);
  }

  public EMVCardServiceFactory()
  {
  }



  /**
   * Return an enumeration of known <tt>CardService</tt> classes.
   *
   * @param     type
   *		The <tt>CardType</tt> of the smart card for which
   *		the enumeration is requested.
   * @return    An <tt>Enumeration</tt> of class objects.
   */
  protected Enumeration getClasses(CardType type) {

      return services.elements();
  }


  /**
   * Checks whether this factory can instantiate services for a given card.<p>
   *
   */
  public CardType getCardType(CardID cid, CardServiceScheduler scheduler)
  {
    CardType type = null;
    byte[] historicals = cid.getHistoricals();

    // There is no card recognition supported.
    // This factory assumes, it it creating card services for EMV
    // compliant cards
    type = new CardType(1);

    return type;
  }

}
