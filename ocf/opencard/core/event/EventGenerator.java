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

package opencard.core.event;


import java.util.Enumeration;
import java.util.Vector;

import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.terminal.Observer;
import opencard.core.terminal.Pollable;
import opencard.core.util.Tracer;

/**
 * The <tt>EventGenerator</tt> singleton acts as a generator and multicaster for
 * card terminal events. The singleton instance can be obtained by calling the static
 * method  <tt>getGenerator</tt>.
 * It periodically polls the terminals and generates <tt>CARD_INSERTED</tt> or
 * <tt>CARD_REMOVED</tt> events if it detects a card insertion or removal, respectively.
 * The generated events are sent to all <tt>CTListener</tt>s which have been registered
 * using the method <tt>addCTListener</tt>. If not interested in card terminal events
 * any longer, <tt>CTListener</tt>s can remove themselves from the notification list of
 * the registry by calling the method <tt>removeCTListener</tt>.
 * For the case that cards were inserted before an application was started, the method
 * <tt>createEventsForPresentCards</tt> creates events for inserted cards a posteriori.
 * <p>
 *
 * @version $Id: EventGenerator.java,v 1.2 1999/11/03 12:37:15 damke Exp $
 *
 * @see CardTerminalEvent
 * @see CTListener
 * @see CardTerminal
 * @see CardTerminalRegistry
 */
public final class EventGenerator implements Observer, Runnable {

  private static Tracer ctracer = new Tracer(EventGenerator.class);

  /** The listeners to which events are to be multicasted. */
  private Vector listeners = new Vector();

  /** The card terminal registry singleton. */
  private static EventGenerator theGenerator = null;

  /** The thread that does polling. */
  private Thread t = null;

  private static boolean running;
  
  private static int pollInterval = 500;

  /** The pollable terminals. */
  private Vector pollables = new Vector();


  /**
   * Gets the unique instance of <tt>CardTerminalRegistry</tt>.
   * (Singleton Pattern)
   *
   * @return The card terminal registry singleton.
   */
  public static EventGenerator getGenerator() {
    if (theGenerator==null) {
      theGenerator=new EventGenerator();
      // since events package can be used on top of terminal package
      // it must register itsself at the terminals package
      CardTerminalRegistry.getRegistry().setObserver(theGenerator);
    }
    return theGenerator;
  }

  /**
   * Adds a <tt>CTListener</tt>.
   *
   * @param listener The <tt>CTListener</tt> to be added.
   *
   * @see #removeCTListener(CTListener)
   */
  public void addCTListener(CTListener listener) {
    listeners.addElement(listener); // ls is the vector of listeners
  }


  /**
   * Generates events for cards which are already inserted. This method
   * should be called when using the event-driven paradigm (in contrast
   * to the procedural <TT>waitForCard ()</TT> approach) and the
   * application needs to handle cards that are already inserted.
   * This method iterates over all registered terminals.
   *
   * @param     ctListener
   *            the Card Terminal Listener to which the events shall be sent.
   * @exception CardTerminalException
   *            thrown in case of errors in a registered CardTerminal.
   */
  public void createEventsForPresentCards(CTListener ctListener)
  throws CardTerminalException {

    Enumeration e = CardTerminalRegistry.getRegistry().getCardTerminals();

    while (e.hasMoreElements()) {
      CardTerminal ct = (CardTerminal)e.nextElement();
      int slots = ct.getSlots();
      for (int i=0; i<slots; i++) {
        if (ct.isCardPresent(i)) {
          ctListener.cardInserted(new CardTerminalEvent(ct, CardTerminalEvent.CARD_INSERTED, i));
        }
      }
    }
  }


  /**
   * Removes a <tt>CTListener</tt>.
   *
   * @param ctListener The <tt>CTListener</tt> to be removed.
   *
   * @see #addCTListener(CTListener)
   */
  public void removeCTListener(CTListener ctListener) {
    listeners.removeElement(ctListener);            // l is the vector of listeners
  }

  /**
   * Sets the poll interval in ms
   *
   * @param duration in ms
   */
  public void setPollInterval(int duration) {

    pollInterval = duration;
  }


  /**
   * Gets the duration of the poll interval in ms.
   *
   * @return the poll interval duration in ms
   */
  public int getPollInterval() {

    return pollInterval;
  }


  /**
   * Periodically checks all <TT>Pollable</TT> terminals. The terminals
   * implementation must then make sure to generate the proper events
   * whenever they detect that a card was inserted or removed from one
   * of their slots.
   */
  public void run() {

    while (running) {
      try {
        // sleep at beginning of loop because each pollable has already been
        // polled initially while registering
        Thread.sleep(pollInterval);
        synchronized(pollables) {
          Enumeration e = pollables.elements();

          while (e.hasMoreElements()) {
            Pollable p = (Pollable)e.nextElement();
            try {
              p.poll();
            } catch (RuntimeException rte) {
              // ignore runtime exceptions thrown by poll
              ctracer.debug("run", rte);
            } catch (CardTerminalException cte) {
              ctracer.debug("run", cte);
            }
          }
        }
      } catch (InterruptedException ie) {
      }   // ignore
    }
  }


  /**
   * Notify listeners that a card was inserted into or removed from a slot of a terminal.
   *
   * @param     terminal
   *            terminal where a card was inserted/removed
   * @param     slotID
   *            slot where a card was inserted/removed
   * @param     cardInserted true if a card was inserted, false, if a card was removed
   */
  public void updateCards(CardTerminal terminal, int slotID, boolean cardInserted) {

    ctracer.debug("updateCards", "card "+(cardInserted?"inserted":"removed")+" slotID = " + slotID);

    if (!listeners.isEmpty()) {
      CTListener ctl = null;
      for (Enumeration e = listeners.elements(); e.hasMoreElements(); ) {
        ctl = (CTListener)e.nextElement();
        try {
          if (cardInserted) {
            ctl.cardInserted(new CardTerminalEvent(terminal,
                                                   CardTerminalEvent.CARD_INSERTED,
                                                   slotID));
          } else {
            ctl.cardRemoved(new CardTerminalEvent(terminal,
                                                  CardTerminalEvent.CARD_REMOVED,
                                                  slotID));
          }
        } catch (RuntimeException rte) {
          // ignore runtime exceptions
          ctracer.critical("updateCards", rte);
        } catch (CardTerminalException cte) {
          // ignore this
          ctracer.critical("updateCards", cte);
        }
      }
      ctracer.debug("updateCards", "notified CTListeners");
    }
  }

  /** Keep track of pollable terminals, used by CardTerminalRegistry */

  /**                                                              .
   * Keep track of pollable terminals, used by CardTerminalRegistry
   *
   * @param     p
   *            terminal that was added/removed to the registry
   * @param     terminalAdded true if a terminal was added. False,
   *                          if a terminal was removed.
   */
  public boolean updateTerminals(Pollable p, boolean terminalAdded) {

    synchronized(pollables) {
      if (terminalAdded) {
        ctracer.debug("updateTerminals", "new pollable Terminal = " + p);

        pollables.addElement(p);

        try {
          // avoid duplicate events for cards already present
          // terminal should have current state, BEFORE any listener
          // has a chance to register itsself
          p.poll();
        } catch (RuntimeException rte) {
          // ignore runtime exceptions thrown by poll
          ctracer.debug("run", rte);
        } catch (CardTerminalException cte) {
          ctracer.debug("run", cte);
        }

        // start polling thread if not already started
        if (t==null) {
          t = new Thread(this, "OCF Polling");
          t.setDaemon(true);
          running = true;
          t.start();
        }
        return true;
      } else {
        ctracer.debug("updateTerminals", "remove pollable Terminal = " + p);
        boolean r = pollables.removeElement(p);
        
        if (pollables.size() <= 0) {
        	running = false;
        	t = null;
        }
        return r;
      }
    }
  }

  /**
   * Creates a <tt>EventGenerator</tt> object.
   * This method is private to avoid accidential instantiation.
   * <tt>EventGenerator</tt> implements the singleton pattern, only the method
   * <tt>getGenerator</tt> can be used to obtain the unique instance.
   */
  private EventGenerator() {
  }

}
