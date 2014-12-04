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
import java.util.Hashtable;
import java.util.Vector;

import opencard.core.event.CardTerminalEvent;
import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.SlotChannel;
import opencard.core.util.Tracer;


/**
 * The <tt>CardServiceRegistry</tt> keeps track of <tt>CardServiceFactories</tt>.
 * When asked for a certain <tt>CardService</tt> it searches through its registered
 * <TT>CardServiceFactory</TT>s to find one which is able to create this.
 * NOTE that there exists only one system-wide instance of this class which can be
 * accessed via <TT>CardServiceRegistry.getRegistry ()</TT>.
 *
 * @author  Dirk Husemann       (hud@zurich.ibm.com)
 * @author  Reto Hermann        (rhe@zurich.ibm.com)
 * @author  Mike Wendler        (mwendler@de.ibm.com)
 * @author  Stephan Breideneich (sbreiden@de.ibm.com)
 * @author  Roland Weber        (rolweber@de.ibm.com)
 * @author  Thomas Schaeck      (schaeck@de.ibm.com)
 *
 * @version $Id: CardServiceRegistry.java,v 1.2 1999/10/22 14:43:28 damke Exp $
 *
 * @see opencard.core.service.CardService
 * @see opencard.core.service.CardServiceFactory
 * @see opencard.core.service.CardServiceScheduler
 * @see opencard.core.service.CardChannel
 */

public final class CardServiceRegistry {

  private Tracer itracer        = new Tracer(this, CardServiceRegistry.class);
  private static Tracer ctracer = new Tracer(CardServiceRegistry.class);

  /** The reference to the one and only <tt>CardServiceRegistry</tt>. */
  private final static CardServiceRegistry registry = new CardServiceRegistry();

  /** Registered <tt>CardServiceFactories</tt>. */
  private Vector factories = new Vector();

  /** Keep track of Slots for which a SlotChannel already exists.
  * each entry's key is the hashCode of the CardTerminal plus the slotID
  */
  private Hashtable slot2channel = new Hashtable();


  /**
   * Because there is <b>one</b> <tt>CardServiceRegistry</tt> per system
   * ('Singleton' pattern) the constructor is private (prevent construction
   * outside of this class).
   */
  private CardServiceRegistry () {
    ctracer.debug("<init>", "instantiating");
  }
  /**
   * Adds a <tt>CardServiceFactory</tt> to the registry.
   *
   * @param factory  The <tt>CardServiceFactory</tt> to add.
   */
  public void add (CardServiceFactory factory) {
    itracer.debug("add", " " + factory);
    factories.addElement(factory);
  }
  /**
   * Allocates a <tt>CardServiceScheduler</tt> for <tt>slot</tt>.
   */
  private CardServiceScheduler allocateCardServiceScheduler(SlotChannel channel)
  throws CardTerminalException
  {
    CardServiceScheduler scheduler = null;

    // Synchronize to implement atomic test-and-set behavior.
    synchronized (channel) {
      scheduler = (CardServiceScheduler) channel.getScheduler();
      if (scheduler == null) {
        // This is a new SlotChannel, so we need to allocate a scheduler
        itracer.debug("allocateCardServiceScheduler",
                      "instantiating CardServiceScheduler");
        scheduler = new CardServiceScheduler(channel);
        channel.setScheduler(scheduler);

        // Is there a PrimaryCardServiceFactory? We take the first that we find
        // The primary CardServiceFactory sets up the smartcard. It my do things
        // such as protocol selection etc.
        Enumeration factories = getCardServiceFactories();
        while (factories.hasMoreElements()) {
          CardServiceFactory factory = (CardServiceFactory) factories.nextElement();
          if (factory instanceof PrimaryCardServiceFactory) {
            itracer.debug("allocateCardServiceScheduler",
                          "setting up card via PrimaryCardServiceFactory" + factory);
            ((PrimaryCardServiceFactory) factory).setupSmartCard(channel);
            break;
          }
        }
      }
    }
    return scheduler;
  }
  // protected methods --------------------------------------------------------


  /**
   * Gets a <tt>CardService</tt> class object for <tt>clazz</tt>.
   *
   * @param clazz The class that the requested <tt>CardService</tt>
   *              should be an instance of.
   * @param cid   A <tt>CardID</tt> object representing the smart card
   *              for which the <tt>CardService</tt> is requested.
   * @param scheduler The <tt>CardServiceScheduler<tt> for the card
   *                  to be inspected.
   *
   * @return    The <tt>CardService</tt> class object if available; otherwise
   *            <tt>null</tt>.
   */
  protected Class getCardServiceClassFor (Class clazz, CardID cid,
                                          CardServiceScheduler scheduler) {
    itracer.debug("getCardServiceClass", "for " + clazz + " and " + cid);

    Enumeration factoryEnum = getCardServiceFactories();
    while (factoryEnum.hasMoreElements()) {
      CardServiceFactory factory = (CardServiceFactory) factoryEnum.nextElement();
      itracer.debug("getCardServiceClass", "checking " + factory);

      // has the factory already inspected the card?
      CardType type = scheduler.getCardTypeFor(factory);

      // if not, let the factory inspect the card
      if (type==null) {
        try {
          type = factory.getCardType(cid,scheduler);
          scheduler.setCardTypeFor(factory,type);
        } catch (CardTerminalException t) {
          itracer.debug("getCardServiceClass",t);
          t.printStackTrace();
          continue;
        }
      }

      // ... skip this factory if it cannot deal with the provided card
      if (CardType.UNSUPPORTED==type)
        continue;

      Class cardServiceClass = factory.getClassFor(clazz, type);
      itracer.debug("getCardServiceClass", "factory " + factory +
                    " produced " + cardServiceClass);
      if (cardServiceClass != null)
        return cardServiceClass;
    }
    itracer.info("getCardServiceClass", "no CardService for " + clazz);
    return null;
  }
  /**
   * Gets all registered card service factories.
   * A registered factory is one that has been passed to <tt>add</tt>, but
   * has not yet been removed using <tt>remove</tt>.
   *
   * @return an enumeration of the registered card service factories
   */
  public final Enumeration getCardServiceFactories() {
    return((Vector) factories.clone()).elements();
  }
  /**
   * Tries to instantiate a <tt>CardService</tt> that is an instance of <tt>clazz</tt>
   * and that works with the smart card represented by <tt>cid</tt>.
   *
   * @param     clazz      The class that the requested <tt>CardService</tt> should be
   *                       an instance of.
   * @param     cid        A <tt>CardID</tt> object representing the smart card for which
   *                       the <tt>CardService</tt> is requested.
   * @param     scheduler  The controlling <tt>CardServiceScheduler</tt>
   * @param     card       The <tt>SmarCard</tt> object requesting the <tt>CardService</tt>
   * @param     block      Specifies the waiting behavior of the newly created <tt>CardService</tt>;
   *                       if true it will wait for <tt>CardChannel</tt> (i.e., block).
   *
   * @exception java.lang.ClassNotFoundException
   *            Thrown when no fitting <tt>CardService</tt> could be found.
   */
  protected CardService getCardServiceInstance(Class clazz, CardID cid,
                                               CardServiceScheduler scheduler, SmartCard card, boolean block)
  throws ClassNotFoundException
  {
    itracer.debug("getCardServiceInstance", "for " + clazz + " from " + card);

    Enumeration factories = getCardServiceFactories();
    while (factories.hasMoreElements()) {
      CardServiceFactory factory = (CardServiceFactory)factories.nextElement();
      itracer.debug("getCardServiceInstance", "checking " + factory);

      // has the factory already inspected the card?
      CardType type = scheduler.getCardTypeFor(factory);

      // if not, let the factory inspect the card
      if (type==null) {
        try {
          type = factory.getCardType(cid,scheduler);
          scheduler.setCardTypeFor(factory,type);
        } catch (CardTerminalException t) {
          itracer.debug("getCardServiceInstance",t);
          t.printStackTrace();
          continue;
        }
      }

      // ... skip this factory if it cannot deal with the provided card
      if (CardType.UNSUPPORTED==type)
        continue;

      try {
        CardService service =
        factory.getCardServiceInstance(clazz, type, scheduler, card, block);
        itracer.debug("getCardServiceInstance", "factory " + factory + " produced " + service);
        if (service != null)
          return service;
      } catch (CardServiceException csx) {
        itracer.info("getCardServiceInstance",
                     "factory " + factory + " failed: " + csx);
      }
    }
    itracer.info("getCardServiceInstance",
                 "no CardService for " + clazz + " found");
    throw new ClassNotFoundException("CardService implementing " + clazz.toString());
  }
  /**
   * Gets a reference to the system-wide <tt>CardServiceRegistry</tt> object
   *  ('Singleton' pattern).
   *
   * @return   Reference to the system-wide <tt>CardServiceRegistry</tt>.
   */
  public static CardServiceRegistry getRegistry() {
    return registry;
  }
/**
   * Gets a <tt>SmartCard</tt> object based on a received
   * <tt>CardTerminalEvent</tt>.
   *
   * @param     ctEvent  A <tt>CardTerminalEvent</tt> event received
   *                     from a terminal.
   * @param     req      A <tt>CardRequest</tt> object describing what
   *                     kind of <tt>SmartCard</tt> is requested.
   * @param     lockHandle handle obtained by lock owner when locking the
   *                       terminal
   *
   * @return    A <tt>SmartCard</tt> object or <tt>null</tt> if not successful.
   */
  protected SmartCard getSmartCard(CardTerminalEvent ctEvent, CardRequest req,
                                   Object lockHandle) throws CardTerminalException {

    itracer.debug("getSmartCard", "CTEvent " + ctEvent);

    boolean newScheduler=false;

    CardTerminal terminal = (CardTerminal) ctEvent.getSource();
    SlotChannel channel = null;
    int slotID = ctEvent.getSlotID();
    CardID cid = terminal.getCardID(slotID);
    Integer hashCode = new Integer(terminal.hashCode()+slotID);

    // allocate a CardServiceScheduler

    // Here we need a synchronized statement to assure atomic test-and-set semantics for the
    // slot channel.
    synchronized (slot2channel) {
      channel = (SlotChannel) slot2channel.get(hashCode);
      if ((channel != null) && (channel.getLockHandle() == lockHandle)) {
        itracer.debug("getSmartCard", "secondary waitForCard(); don't need to open SlotChannel again");
      } else {
        // get new channel and store it (this will also check validity of lock handle)
        channel = terminal.openSlotChannel(slotID,lockHandle);
        slot2channel.put(hashCode, channel);
      }
    }
    CardServiceScheduler scheduler = (CardServiceScheduler)channel.getScheduler();
    if (scheduler==null) {
      scheduler=allocateCardServiceScheduler(channel);
      newScheduler=true;
    }

    // check if card request is satisfied

    if ((req != null) && !isCardRequestSatisfied(req, cid, terminal,scheduler)) {
      itracer.info("getSmartCard", "CardRequest " + req + " cannot be satisfied with " + cid);
      if (newScheduler) {
        scheduler.closeDown();
      }
      return null;
    }

    // create a smart card

    itracer.debug("getSmartCard", "using CardServiceScheduler " + scheduler);
    return scheduler.createSmartCard(cid);
  }
  // private methods ---------------------------------------------------------


  /**
   * Checks whether a <tt>CardRequest</tt> is satisfied by a particular card.
   *
   * @param    req      The <tt>CardRequest</tt> object to satisfy.
   * @param    cid      A <tt>CardID</tt> object representing the card.
   * @param    terminal The <tt>CardTerminal</tt> holding the card.
   * @param    scheduler The <tt>CardServiceScheduler</tt> to access the card.
   *
   * @return   <tt>true</tt> if the card satisfies the <tt>CardRequest</tt>
   *           object; <tt>false</tt> otherwise.
   */
  private boolean isCardRequestSatisfied(CardRequest req,
                                         CardID cid,
                                         CardTerminal terminal,
                                         CardServiceScheduler scheduler)
  {
    // Check whether specified CardID specified in request matches what we got
    CardIDFilter filter = req.getFilter();

    if ((filter != null) && !filter.isCandidate(cid)) {
      itracer.info("isCardRequestSatisfied", "filtered out by " + filter);
      return false;
    }

    // Check whether CardTerminal specified in request matches what we got
    if ((req.getCardTerminal() != null) && (req.getCardTerminal() != terminal)) {
      itracer.info("isCardRequestSatisfied", "requested terminal " +
                   req.getCardTerminal() + " does not match receiving terminal "
                   + terminal);
      return false;
    }

    // Check whether we can provide the requested CardService
    if ((req.getCardServiceClass() != null)
        && (getCardServiceClassFor(req.getCardServiceClass(), cid,scheduler)
            == null)) {
      itracer.info("isCardRequestSatisfied", "requested CardService class " +
                   req.getCardServiceClass() + " not supported for  " + cid);
      return false;
    }
    return true;
  }
  /**
   * Releases a <tt>CardServiceScheduler</tt>.
   *
   * @param scheduler  The <tt>CardServiceScheduler</tt> to release.
   */
  protected void releaseScheduler(CardServiceScheduler scheduler) {
    int hashCode = scheduler.getSlotChannel().getCardTerminal().hashCode()+
                   scheduler.getSlotChannel().getSlotNumber();


    slot2channel.remove(new Integer(hashCode));
  }
  /**
   * Removes the passed <tt>CardServiceFactory</tt> from the registry.
   *
   * @param     factory
   *            The <tt>CardServiceFactory</tt> to add.
   */
  public void remove(CardServiceFactory factory) {
    itracer.debug("remove", " " + factory);
    factories.removeElement(factory);
  }
  /**
   * Gets a meaningful <tt>String</tt> representation of this
   * <tt>CardServiceRegistry</tt>.
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(super.toString());
    Enumeration factories = getCardServiceFactories();
    while (factories.hasMoreElements()) {
      CardServiceFactory factory = (CardServiceFactory) factories.nextElement();
      sb.append("++ registered factory ").append(factory).append("\n");
    }
    return sb.toString();
  }
}
