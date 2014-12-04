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


package opencard.opt.service;

import java.util.Enumeration;

import opencard.core.service.CardServiceFactory;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.CardType;
import opencard.core.terminal.CardID;
import opencard.core.util.Tracer;

/** A <tt>CardServiceFactory</tt> can instantiate <tt>CardService</tt>s for
 * a specific smart card. Typically, the <tt>CardServiceRegistry</tt> will
 * instantiate a <tt>CardServiceFactory</tt> once a smart card has been inserted
 * for which a <tt>waitForCard()</tt> method was invoked.<p>
 *
 * Note: This CardServiceFactory subclass maps the new style
 * CardServiceFactory interface (using getCardType() and getClasses())
 * to the OCF 1.1 style card service factories
 * using the deprecated knows() and cardServiceClasses() methods.
 * Existing factories should inherit from this class to preserve
 * compatibility.
 *
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 * @version $Id: OCF11CardServiceFactory.java,v 1.3 1999/11/03 12:37:19 damke Exp $
 *
 * @see opencard.core.service.CardService
 * @see opencard.core.service.CardServiceFactory
 * @see opencard.core.service.CardType
 * @see opencard.core.terminal.CardID
 */
public abstract class OCF11CardServiceFactory extends CardServiceFactory {
  private Tracer ctracer = new Tracer(OCF11CardServiceFactory.class);

  /** Instantiate a <tt>CardServiceFactory</tt>. */
  public OCF11CardServiceFactory() {
    ctracer.debug("<init>", "instantiating");
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
   * @see #getClasses
   *
   */
  protected CardType getCardType(CardID cid,
                                          CardServiceScheduler scheduler)
  {
     CardType type=null;

     if (knows(cid)) {
       type = new CardType();
       type.setInfo(cid);
       return type;
     } else {
       return CardType.UNSUPPORTED;
     }
  }

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
  protected Enumeration getClasses(CardType type) {
     return cardServiceClasses((CardID)(type.getInfo()));
  }


  /** Indicate whether this <tt>CardServiceFactory</tt> "knows" the smart card OS
   * represented by <tt>cid</tt> and might be able to instantiate <tt>CardService</tt>s
   * for it.
   *
   * @param     cid
   *		A <tt>CardID</tt> received from a <tt>Slot</tt>.
   */
  protected abstract boolean knows(CardID cid);

  /** Return an enumeration of known <tt>CardService</tt> classes.
   *
   * @param     cid
   *		The <tt>CardID</tt> of the smart card for which
   *		the enumeration is requested.
   * @return    An <tt>Enumeration</tt> of class objects.
   */
  protected abstract Enumeration cardServiceClasses(CardID cid);
}

