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


import java.util.Hashtable;

import opencard.core.OpenCardRuntimeException;
import opencard.core.event.CTListener;
import opencard.core.event.CardTerminalEvent;
import opencard.core.event.EventGenerator;
import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.SlotChannel;
import opencard.core.util.Tracer;


/**
 * Manager for logical channels to an inserted smartcard.
 * For each smart card known to the system and in use by some
 * application there is one <tt>CardServiceScheduler</tt> that manages the
 * access to the physical smart card. This is acchieved by allocating the
 * available communication channels to the <tt>CardService</tt>s that want
 * to access the card. The <tt>CardServiceScheduler</tt> takes care of
 * state management and issues such as suspending active and resuming
 * suspended <tt>CardServices</tt>.
 *
 * <p>
 * <i>Note that the current version supports only one logical channel per
 * smart card.</i> To support multiple channels, the internal structure of
 * this class as well as the public interface has to be changed. One
 * way to change the public interface would be to add a method named
 * <tt>setCustomChannels</tt>, which works similiar to
 * <tt>setCustomChannel</tt> but expects an array of channels that
 * have to be managed. A second method to set only the number of
 * channels to manage should then be added, too. In any case, it is
 * a card service factory's responsibility to switch the scheduler
 * to multi-channel support.
 *
 *
 * @author  Dirk Husemann (hud@zurich.ibm.com)
 * @author  Reto Hermann  (rhe@zurich.ibm.com)
 * @author  Mike Wendler  (mwendler@de.ibm.com)
 * @author  Roland Weber  (rolweber@de.ibm.com)
 * @version $Id: CardServiceScheduler.java,v 1.2 2005/09/19 10:21:22 asc Exp $
 *
 * @see opencard.core.service.CardService
 * @see opencard.core.service.CardChannel
 * @see opencard.core.service.SmartCard
 * @see opencard.core.service.CardServiceFactory
 */

public final class CardServiceScheduler implements CTListener {
  /** The tracer for CardServiceScheduler: tracing on class name. */
  private Tracer itracer  = new Tracer(this, CardServiceScheduler.class);
  private static Tracer ctracer = new Tracer(CardServiceScheduler.class);

  /** Through the <tt>SlotChannel</tt> we talk to the smart card. */
  private SlotChannel slot_channel = null;

  /** This is the slot from which we expect a card removed notification. */
  private int card_slot = 0;
  private CardTerminal card_terminal = null;

  /** A reference to the most recently allocated channel. */
  private CardChannel current_channel = null;

  /** Reference counter on issued <tt>SmartCard</tt>s. */
  private int smartcard_refs = 0;

  /** The state of this <tt>CardServiceScheduler</tt>. */
  private boolean is_alive = false;

  /** The channel that can be allocated here. */
  private CardChannel free_channel = null;

  /**
  * @deprecated
  */
  private boolean is_customized = false;

  /** Card type (per factory) as determined by the factories. */
  private Hashtable card_types = new Hashtable();

  /** threads waiting for a CardChannel */
  private int threads = 0;


  /**
   * Instantiates a new scheduler that is tied to the given slot channel.
   *
   * @param     slotchannel
   *            the physical channel to use for communicating with the card
   */
  public CardServiceScheduler(SlotChannel slotchannel)
  {
    ctracer.debug("<init>", "slotChannel " + slotchannel);

    slot_channel = slotchannel;
    card_terminal= slotchannel.getCardTerminal();
    card_slot    = slotchannel.getSlotNumber();
    is_alive     = true;
    free_channel = new CardChannel(slotchannel);

    //slot_channel.getCardTerminal().addCTListener(this);
    EventGenerator.getGenerator().addCTListener(this);
  }


  // instance methods ---------------------------------------------------------


  /**
   * @deprecated use CardChannel.setState() instead
   */
  public void setCustomChannel(CardChannel channel)
  throws InvalidCardChannelException
  {
    if (is_customized)
      throw new InvalidCardChannelException("scheduler already customized");
    if (free_channel == null)
      throw new InvalidCardChannelException("channel in use");

    free_channel.closeFinal();          // dispose of standard channel
    free_channel = channel;             // use custom channel

    is_customized = true;
  }


  /**
   * @deprecated
   */
  public void useDefaultChannel()
  throws InvalidCardChannelException
  {
    if (!is_customized)
      return;                                     // nothing to be undone
    if (free_channel == null)
      throw new InvalidCardChannelException("custom channel in use");

    free_channel.closeFinal();                    // dispose of custom channel
    free_channel = new CardChannel(slot_channel); // use standard channel

    is_customized = false;
  }


  /**
   * @deprecated
   */
  final public boolean isCustomized()
  {
    return is_customized;
  }


  /**
   * Returns the slot channel for this scheduler.
   *
   * @return  the slot channel for this scheduler
   */
  final public SlotChannel getSlotChannel()
  {
    return slot_channel;
  }


  /**
   * Allocates a card channel.
   * The allocated channel has to be freed using <tt>releaseCardChannel</tt>.
   *
   * @param     applicant
   *            The object requesting the card channel. This parameter will
   *            be useful when support for multi-channel cards is implemented.
   *            It allows to implement channel affinity.
   * @param     block
   *            If <i>true</i> the calling thread will be suspended until a
   *            <tt>CardChannel</tt> becomes available; if <i>false</i>
   *            <tt>allocateCardChannel()</tt> will return null at once.
   *
   * @return    The allocated card channel,
   *            or <tt>null</tt> if none has been allocated.
   *
   * @exception CardTerminalException
   *            if the terminal encountered an error. This can only happen
   *            if the smartcard was removed, in which case the scheduler
   *            will close down. When closing down, the underlying slot
   *            channel is closed down, too. This may result in this exception.
   *
   * @see #releaseCardChannel
   */
  public synchronized CardChannel allocateCardChannel(Object applicant,
                                                      boolean block    )
  throws CardTerminalException
  {
    assertLiveness();

    // ... exit at once if others are waiting and user does not want
    //     to wait
    if ((threads > 0) && !block)
      return null;

    itracer.debug("allocateCardChannel", "applicant " + applicant);

    threads++; // Number of threads which called allocate, but not yet release.
    if (threads > 1) {
      // There already are threads, so we must wait.
      try {
        wait();
      } catch (InterruptedException ie) {
        itracer.alert("allocateCardChannel", ie);
        threads--;
        return null;
      }

      if (!isAlive()) {  // ... make sure scheduler is still alive
        itracer.warning("allocateCardChannel",
                        "scheduler died while waiting for CardChannel");
        return null;
      }
    }
    free_channel.open();
    current_channel = free_channel;
    free_channel = null;

    return current_channel;

  } // allocateCardChannel


  /**
   * Releases a card channel.
   * The channel to release must have been allocated using
   * <tt>allocateCardChannel</tt>.
   *
   * @param     channel   the card channel to release
   *
   * @exception InvalidCardChannelException
   *            The <tt>CardChannel</tt> has not been allocated here.
   *
   * @see #allocateCardChannel
   */
  public synchronized void releaseCardChannel(CardChannel channel)
  throws InvalidCardChannelException {
    itracer.debug("releaseCardChannel", "releasing " + channel);

    if ((current_channel != channel) && is_alive)
      throw new InvalidCardChannelException
      ("channel not current channel");

    channel.close();
    current_channel = null;
    free_channel = channel;


    // We are done. Wake up another thread.
    threads--;
    notify();
  } // releaseCardChannel

  /**
   * Reset the card associated with this CardServiceScheduler
   * @param     ch
   *            If the caller already has a channel he can provide it.
   *            Otherwise the scheduler will allocate the channel itsself.
   * @param     block
   *            If <i>true</i> the calling thread will be suspended until a
   *            <tt>CardChannel</tt> becomes available; if <i>false</i>
   *            <tt>allocateCardChannel()</tt> will return null at once.
   * @return CardID ATR of the card reset.
   *         null iff a channel is in use and the request was non-blocking
   * @exception CardTerminalException Reset failed
   **/
  final public CardID reset(CardChannel ch, boolean warm, boolean block) throws CardTerminalException
  {
    // allocate all card channels to make sure noone works with the card
    // during reset
    boolean release=false;
    try {

      if (ch==null) {
        ch = allocateCardChannel(this, block);
        release = true;
      }
      if (ch==null) {
        return null;
      }
      // reset the card
      CardID id = slot_channel.reset(warm);
      // reset the state in all card channels
      ch.setState(null);
      return id;
    } finally {
      if (release) {
        releaseCardChannel(ch);
      }

    }

  }



  /**
   * Dummy method.
   * Since the scheduler is interested only in the removal of the associated
   * smartcard, card insertion events are ignored. This method has to be
   * implemented anyway, since it is required by <tt>CTListener</tt>.
   *
   * @see opencard.core.event.CTListener
   */
  public void cardInserted(CardTerminalEvent ctEvent)
  {
    ; // empty method
  }


  /**
   * Signals that a smartcard has been removed.
   * If the removed card is the one associated with this scheduler, the
   * scheduler will shut down. Any channels to the smartcard will be closed.
   * This method is required by the interface <tt>CTListener</tt>.
   *
   * @param ctEvent  an event indicating that a smartcard has been removed
   *
   * @see opencard.core.event.CTListener
   */
  public void cardRemoved(CardTerminalEvent ctEvent)
  throws CardTerminalException
  {
    if ((ctEvent.getSlotID() == card_slot) &&
        (ctEvent.getCardTerminal() == card_terminal)) {
      itracer.debug("cardRemoved", "event " + ctEvent);
      closeDown();
    }
  } // cardRemoved



  /**
   * Returns a human-readable string representation of this scheduler.
   *
   * @return  a string representing this scheduler
   */
  public String toString() {
    StringBuffer sb = new StringBuffer(super.toString());
    sb.append(", ").append((is_alive? "is" : "not")).append(" alive");
    if (threads > 0) {
      sb.append("\n++  channel is allocated");
    }
    if (threads > 1) {
      sb.append("\n++ "+ (threads-1) +" threads waiting for channel");
    }
    return sb.toString();
  }


  // protected methods --------------------------------------------------------


  /**
   * Checks whether this scheduler is alive.
   *
   * @return <TT>true</TT> if it is, <TT>false</TT> otherwise
   */
  final protected boolean isAlive() //@@@ synchronized?
  throws CardTerminalException
  {
    // first check the slot channel, just to be sure
    if (is_alive && ((slot_channel == null) ||
                     !slot_channel.isOpen()   ))
      closeDown();

    return is_alive;
  }


  /**
   * Creates a new <tt>SmartCard</tt> object.
   * A reference counter gets incremented.
   *
   * @param     cid
   *            The <tt>CardID</tt> representing the smart card.
   * @return    A <tt>SmartCard</tt> attached to this
   *            <tt>CardServiceScheduler</tt>.
   *
   * @exception CardTerminalException
   *            The terminal encountered an error.
   */
  protected synchronized SmartCard createSmartCard(CardID cid)
  throws CardTerminalException
  {
    itracer.debug("createSmartCard", "creating SmartCard");
    SmartCard card = null;

    try {
      assertLiveness();
      card = new SmartCard (this, cid);
      smartcard_refs++;                // only after successful creation!

    } catch (OpenCardRuntimeException ocre) {
      itracer.debug("createSmartCard", "could not create SmartCard");
    }

    return(card);
  }


  /**
   * Releases a <tt>SmartCard</tt> object.
   * If the reference counter reaches 0, the <tt>SlotChannel</tt> is closed
   * and the <tt>CardServiceRegistry</tt> gets told that our job is done.
   *
   * @param     card
   *            The <tt>SmartCard</tt> object to release.
   */
  protected synchronized void releaseSmartCard(SmartCard card)
  throws CardTerminalException
  {
    itracer.debug("releaseSmartCard", "releasing " + card);

    smartcard_refs--;
    if (smartcard_refs < 1) {
      itracer.info("releaseSmartCard", "no more SmartCards, closing down");
      closeDown();
    }
  }

  /** Store the CardType determined by a factory.
   * @param factory The factory that determined the card type
   * @param type    The type determined by the factory for the
   *                card associated with the factory.
   */
  void setCardTypeFor(CardServiceFactory factory, CardType type) {
    card_types.put(factory, type);
  }

  /** Retrieve the CardType determined by a factory.
    * @return The CardType if the factory has already inspected the card.
    *         null, if the factory has not yet inspected the card.
    */
  CardType getCardTypeFor(CardServiceFactory factory) {
    return(CardType)card_types.get(factory);
  }



  // private methods ----------------------------------------------------------

  /** Throw an exception if this scheduler is not alive. */
  private void assertLiveness() throws CardTerminalException
  {
    if ( !isAlive() )
      throw new InvalidCardChannelException("CardServiceScheduler dead");
  }


  /**
   * Clean up after the smartcard is no longer available or needed.
   * Package visibility.
   */
  synchronized void closeDown() throws CardTerminalException
  {

    itracer.debug("closeDown", "closing down scheduler");

    if (!is_alive) // only close once
      return;

    // - remove reference in scheduler before slot channel is closed
    // - set state to dead
    // - revoke channel allocation
    // - cleanup managed channel

    CardServiceRegistry.getRegistry().releaseScheduler(this);

    is_alive = false;

    if (current_channel != null) {
      itracer.warning("closeDown", "closing " + current_channel);
      releaseCardChannel(current_channel); // becomes  free_channel
    }
    free_channel.closeFinal();  // close the channel for good


    // - deregister as CTListener
    // - close SlotChannel, if still open
    // - deblock waiting card services, so they can clean up
    // - delete this Scheduler at the registry

    EventGenerator.getGenerator().removeCTListener(this);

    if (slot_channel.isOpen()) {
      slot_channel.close();
      slot_channel = null;
    }

    notifyAll();
  } // closeDown

} // class CardServiceScheduler
