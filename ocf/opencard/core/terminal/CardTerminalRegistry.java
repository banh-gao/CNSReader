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

package opencard.core.terminal;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import opencard.core.event.CTListener;
import opencard.core.event.EventGenerator;
import opencard.core.util.Tracer;


/**
 * The <tt>CardTerminalRegistry</tt> keeps track of the installed <tt>CardTerminal</tt>s
 * registered within a system. You can get an enumeration of the registered
 * <tt>CardTerminal</tt>s via <tt>CardTerminalRegistry.getCardTerminals()</tt>.<p>
 * NOTE that there may be a difference between the terminals physically attached to your
 * computer and those registered in the registry.<P>
 * Using the <tt>add()/remove ()</tt> methods you can dynamically add to and remove
 * card terminals from the registry. Usually however this will be done automatically by
 * <TT>SmartCard.start ()</TT>  based on the information provided by the
 * Opencard properties.<p>
 *
 * @author  Dirk Husemann   (hud@zurich.ibm.com)
 * @author  Peter Trommler  (trp@zurich.ibm.com)
 * @author  Mike Wendler    (mwendler@de.ibm.com)
 * @author  Stephan Breideneich (sbreiden@de.ibm.com)
 * @version $Id: CardTerminalRegistry.java,v 1.3 1999/11/03 12:37:17 damke Exp $
 *
 * @see opencard.core.terminal.CardTerminal
 * @see opencard.core.terminal.CardTerminalFactory
 * @see opencard.core.terminal.CardID
 */

public final class CardTerminalRegistry {

  private Observer observer=null;

  /** table with all registered CTListener-objects */
  protected Hashtable ctListeners = new Hashtable();

  private static Tracer ctracer = new Tracer(CardTerminalRegistry.class);

  private static final CardTerminalRegistry registry = new CardTerminalRegistry();
  private Vector registeredTerminals = new Vector ();

  /**
  * The Observer is the bridge to the opencard.core.event package
  * which creates events for card insertion/card removal
  */
  public void setObserver(Observer o) {
    observer=o;
  }

  /**
   * Constructs the system wide <tt>CardTerminalRegistry</tt>.
   */
  private CardTerminalRegistry() {}


  /**
   * Gets the system wide <tt>CardTerminalRegistry</tt>. This conforms to
   * the Singleton pattern.
   *
   * @return the one system-wide card terminal registry.
   */
  public synchronized static CardTerminalRegistry getRegistry() {

    return registry;
  }


  // instance methods -----------------------------------------------


  /**
   * Adds a <tt>CardTerminal</tt> instance to the registry.
   * Should be called by the CardTerminalFactory when creating
   * a card terminal.
   *
   * @param     terminal
   *            The <tt>CardTerminal</tt> to add.
   * @exception thrown in case of errors in the CardTerminal.open() method.
   */
  public void add(CardTerminal terminal)
    throws CardTerminalException {

    registeredTerminals.addElement(terminal);
    terminal.open();
  }


  /**
   * Adds a <tt>Pollable</tt> card terminal to the observer's
   * list of pollable terminals.
   * Should be called by pollable terminals in their open() method.
   *
   * @param     p
   *      The <tt>Pollable</tt> terminal to add.
   */
  public void addPollable(Pollable p) {
    if (observer!=null) {
      observer.updateTerminals(p,true);
    }
  }

  /**
   * Iterates over the registered terminals and searches for one with the given
   * name.
   *
   * @param    name
   *           The name of the card terminal to search for.
   * @return   A <TT>CardTerminal</TT> instance with <TT>name</TT> or <tt>null</tt> if none
   *           could be found.
   */
  public CardTerminal cardTerminalForName (String name) {

    CardTerminal terminal = null;

    // iterate over all registered terminals and find the
    // terminal named <tt>name</tt>
    synchronized (registeredTerminals) {
      for (int i = 0; i < countCardTerminals(); i++) {
        if (((CardTerminal)registeredTerminals.elementAt(i)).getName().equals(name)) {
          terminal = (CardTerminal) registeredTerminals.elementAt (i);
          break;    // terminal found - interrupt iteration
        }
      }
    }

    return terminal;
  }


  /**
   * Gets the number of registered <tt>CardTerminals</tt>.
   *
   * @return the number of terminals registered in the registry
   */
  public int countCardTerminals() {

    return registeredTerminals.size();
  }


  /**
   * Gets all registered <tt>CardTerminal</tt> instances.
   *
   * @return     An <tt>Enumeration</tt> of the currently registered
   *       <tt>CardTerminal</tt>s.
   */
  public Enumeration getCardTerminals() {

    return ((Vector)(registeredTerminals.clone())).elements();
  }

  /** Remove the card terminal named <tt>name</tt>.<p>
   *
   * @param     name
   *            The name of the card terminal to unregister.
   * @return    True if the terminal was unregistered successfully.
   * @exception CardTerminalException
   *            thrown if error occurred
   */
  public boolean remove(String name)
    throws CardTerminalException {

    CardTerminal ct = null;

    // iterate over all registered terminals and find the
    // terminal named <tt>name</tt>
    synchronized (registeredTerminals) {
      for (int i = 0; i < registeredTerminals.size(); i++) {
        ct = (CardTerminal)registeredTerminals.elementAt(i);

        if (ct.getName().equals(name)) {
          ct.close();
          registeredTerminals.removeElementAt(i);
          break;    // leave loop here
        }
      }
    }

    return false;
  }


  /**
   * Closes the card terminal and removes it from the registry.
   *
   * @param     terminal
   *            The reference to the <tt>CardTerminal</tt> object to unregister.
   * @return    <TT>true</TT> if the terminal was removed from the
   *            registry successfully, <TT>false</TT> otherwise
   * @exception CardTerminalException
   *            thrown when error occurred in terminal.close().
   * @see       CardTerminal#close
   */
  public boolean remove(CardTerminal terminal)
    throws CardTerminalException {

    // clean up the resources used by the terminal
    ctracer.debug("remove", "closing " + terminal);

    terminal.close();

    ctracer.debug("remove", "removing " + terminal);

    boolean taskAccomplished = registeredTerminals.removeElement (terminal);

    ctracer.debug("remove", "status " + taskAccomplished);
    return taskAccomplished;
  }


  /**
   * Removes a <tt>Pollable</tt> card terminal from the
   * observer's list of terminals to be polled.
   *
   * @param     p
   *            The <tt>Pollable</tt> to be removed.
   */
  public boolean removePollable(Pollable p) {
    if (observer!=null) {
      return observer.updateTerminals(p,false);
    }
    return false;
  }

  /**
   * Notify listeners that a card was inserted into a slot of a terminal.
   *
   * @param     terminal
   *            terminal where a card was inserted
   * @param     slot
   *            slot where a card was inserted
   */
  protected void cardInserted(CardTerminal terminal, int slotID) {
    if (observer!=null) {
      observer.updateCards(terminal, slotID, true);
    }
  }

  /**
   * Notify listeners that a card was removed from a slot of this terminal.
   * (utility method).
   *
   * @param     slot
   *            slot number of the slot where a card was removed
   */
  protected void cardRemoved(CardTerminal terminal, int slotID) {

    if (observer!=null) {
      observer.updateCards(terminal, slotID, false);
    }
  }



  /**
   * @deprecated
   */
  public void setPollInterval(int duration) {

    EventGenerator.getGenerator().setPollInterval(duration);
  }

  /**
   * @deprecated use EventGenerator.addCTListener
   */
  public void addCTListener(CTListener listener) {
    EventGenerator.getGenerator().addCTListener(listener);
  }



  /**
   * @deprecated use EvenGenerator.createEventsForPresentCards
   */
  public void createEventsForPresentCards(CTListener ctListener)
    throws CardTerminalException {
    EventGenerator.getGenerator().createEventsForPresentCards(ctListener);
  }



  /**
   * @deprecated
   */
  public int getPollInterval() {
    return EventGenerator.getGenerator().getPollInterval();
  }


  /**
   * @deprecated use EventGenerator.removeCTListener() instead
   */
  public void removeCTListener(CTListener listener) {

    EventGenerator.getGenerator().removeCTListener(listener);
  }


}
