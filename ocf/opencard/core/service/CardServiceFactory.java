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


package opencard.core.service;

import java.util.Enumeration;

import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminalException;
import opencard.core.util.Tracer;

/** A <tt>CardServiceFactory</tt> can instantiate <tt>CardService</tt>s for
 * a specific smart card. Typically, the <tt>CardServiceRegistry</tt> will
 * instantiate a <tt>CardServiceFactory</tt> once a smart card has been inserted
 * for which a <tt>waitForCard()</tt> method was invoked.<p>
 * 
 * Note: Subclasses should implement the getCardType() and getClasses()
 * methods which can communicate with the card to classify the card.
 * OCF 1.1 style card service factories should instead derive from
 * <tt>opencard.opt.service.OCF11CardServiceFactory</tt> which still
 * offers the deprecated knows() and cardServiceClasses() methods.
 *
 * @author  Dirk Husemann (hud@zurich.ibm.com)
 * @author  Reto Hermann (rhe@zurich.ibm.com)
 * @version $Id: CardServiceFactory.java,v 1.2 1999/11/03 12:37:16 damke Exp $
 *
 * @see opencard.opt.service.OCF11CardServiceFactory
 * @see opencard.core.service.CardService
 * @see opencard.core.service.PrimaryCardServiceFactory
 * @see opencard.core.terminal.CardID
 */
public abstract class CardServiceFactory {
  private Tracer itracer = new Tracer(this, CardServiceFactory.class);
  private Tracer ctracer = new Tracer(CardServiceFactory.class);

  /** Instantiate a <tt>CardServiceFactory</tt>. */
  public CardServiceFactory() {
    ctracer.debug("<init>", "instantiating");
  }

  /** Instantiate a <tt>CardService</tt> implementing the class <tt>clazz</tt>.
   *
   * @param     clazz
   *		The <tt>Class</tt> object for which an implementing <tt>CardService</tt>
   *		is requested.
   * @param     cid
   *		A <tt>CardID</tt> object representing the smart card for which
   *		the <tt>CardService</tt> is requested.
   * @param     scheduler
   *		The controlling <tt>CardServiceScheduler</tt>
   * @param     card
   *		The <tt>SmarCard</tt> object requesting the <tt>CardService</tt>
   * @param     block
   *		Specifies the waiting behavior of the newly created <tt>CardService</tt>;
   *		if true it will wait for <tt>CardChannel</tt> (i.e., block).
   * @return    An instance of the requested <tt>CardService</tt>, or <tt>null</tt> if
   *		the requested <tt>CardService</tt> cannot be instantiated.
   */
  protected CardService getCardServiceInstance(Class clazz, CardType type,
					       CardServiceScheduler scheduler,
					       SmartCard card, boolean block)
      throws CardServiceException {
    Class serviceClass = getClassFor(clazz, type);
    if (serviceClass != null)
      return newCardServiceInstance(serviceClass, type, scheduler, card, block);
    return null;
  }

  /** Locate the <tt>CardService</tt> class that implements <tt>clazz</tt>.
   * @param     clazz
   *		The <tt>Class</tt> object for which an implementing <tt>CardService</tt>
   *		is requested.
   * @param     type
   *		A <tt>CardType</tt> object representing the smart card for which
   *		the <tt>CardService</tt> is requested.
   * @return    The class object of the <tt>CardService</tt> class that implements
   *		<tt>clazz</tt> if there is one; <tt>null</tt> otherwise.
   */
  protected Class getClassFor(Class clazz, CardType type) {
    // check all classes of this factory
    // return the first that can be assigned to the requested class
    Enumeration services = getClasses(type);
    while (services.hasMoreElements()) {
      Class serviceClass = (Class)services.nextElement();
      itracer.debug("getClassFor", "checking " + serviceClass);

      if (clazz.isAssignableFrom(serviceClass))
        return serviceClass;
    }
    return null;
  }

  /** Utility method to instantiate a <tt>CardService</tt>.
   *
   * @param     clazz
   *		The class of the <tt>CardService</tt> to instantiate.
   * @param     scheduler
   *		The controlling scheduler.
   * @param     card
   *		The owning <tt>SmartCard</tt> object.
   * @param     blocking
   *		Whether to run the new <tt>CardService</tt> in blocking mode.
   * @return    The instantiated <tt>CardService</tt> object or <tt>null</tt> if
   *		the requested class could not be instantiated.
   *
   * @exception CardServiceException
   *    if the service could be instantiated using the default constructor,
   *    but encountered an error when <tt>initialize</tt> was invoked
   *
   * @see CardService#CardService()
   * @see CardService#initialize
   */
  protected CardService newCardServiceInstance(Class clazz, 
                                               CardType type,
                                               CardServiceScheduler scheduler,
					                                     SmartCard card, boolean blocking)
    throws CardServiceException {
    itracer.debug("newCardServiceInstance", "instantiating " + clazz);

    CardService instance = null;

    try {
      instance = (CardService) clazz.newInstance();
    } catch (NoSuchMethodError nsme) {
      // instantiation failed, return null
    } catch (IllegalAccessException iax) {
      // instantiation failed, return null
    } catch (InstantiationException ix) {
      // instantiation failed, return null
    }

    if (instance != null)
      instance.initialize(scheduler, card, blocking);

    return instance;
  }

  /** Indicate whether this <tt>CardServiceFactory</tt> "knows" the smart card OS
   * and/or installed card applications
   * and might be able to instantiate <tt>CardService</tt>s for it. 
   * <p>
   * This method replaces the former knows() method.
   * Note: OCF 1.1 style card service factories should instead derive from
   * <tt>opencard.opt.service.OCF11CardServiceFactory</tt> which still
   * offers the knows() and cardServiceClasses() methods.
   * <p>
   * Should return a CardType that contains enough information to answer
   * the getClassFor() method.
   * <p>
   * The factory can inspect the card (communicate with the card) using
   * the provided CardServiceScheduler if the CardID information is insufficient
   * to classify the card.
   * 
   *
   * @param     cid
   *		A <tt>CardID</tt> received from a <tt>Slot</tt>.
   * @param scheduler
   *    A <tt>CardServiceScheduler</tt> that can be used to communicate with
   *    the card to determine its type.
   *
   * @return A valid CardType if the factory can instantiate services for this
   *         card.
   *         CardType.UNSUPPORTED if the factory does not know the card.
   *
   */
  protected abstract CardType getCardType(CardID cid, 
                                          CardServiceScheduler scheduler)
                              throws CardTerminalException;

  /** Return an enumeration of known <tt>CardService</tt> classes.
   * <p>Replaces the former cardServiceClasses() method.
   * Note: OCF 1.1 style card service factories should instead derive from
   * <tt>opencard.opt.service.OCF11CardServiceFactory</tt> which still
   * offers the knows() and cardServiceClasses() methods.
   *
   * @param     type
   *		The <tt>CardType</tt> of the smart card for which
   *		the enumeration is requested.
   * @return    An <tt>Enumeration</tt> of class objects.
   */
  protected abstract Enumeration getClasses(CardType type);
}

// $Log: CardServiceFactory.java,v $
// Revision 1.2  1999/11/03 12:37:16  damke
// remove JAVADOC warnings (done by P.Bendel)
//
// Revision 1.3  1999/11/03 11:25:12  oc-cvs
// remove JAVADOC Warnings
//
// Revision 1.1.1.1  1999/10/05 15:34:31  damke
// Import OCF1.1.1 from Zurich
//
// Revision 1.2  1999/08/09 11:18:27  ocfadmin
// replacing OCF 1.1 with updates of OCF 1.1.1 (aka Hudson) as of Mai 1999 (by J.Damke)
//
// Revision 1.23  1999/03/11 13:32:43  pbendel
// DCR0064: allow factory to communicate with card
//
// Revision 1.22  1998/09/18 07:20:48  cvsusers
// Undeprecated CardService instantiation. (rolweber)
//
// Revision 1.21  1998/08/12 13:24:26  cvsusers
// Revised CardService instantiation. (rolweber)
//
// Revision 1.20  1998/08/12 11:27:46  cvsusers
// Added throws clause. (rolweber)
//
// Revision 1.19  1998/07/23 06:57:57  cvsusers
// Fixed bug with extended interfaces in getCardServiceClassFor().
//
// Revision 1.18  1998/04/14 14:22:10  breid
// CVS Log-Keyword added
//
