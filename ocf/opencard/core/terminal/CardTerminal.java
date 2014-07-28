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
import java.util.Properties;
import java.util.Vector;

import opencard.core.util.Tracer;


/**
 * The <tt>CardTerminal</tt> class represents a physical card terminal. It is
 * assumed that a certain card terminal provides (at least one) slot for a
 * smart card or a transceiver for a contactless smart card. Please NOTE that
 * throughout OpenCard Framework <I>inserting</I> or <I>removing</I> a card may
 * equally well mean that a connection between a contactless card and a transceiver
 * was established or ended respectively.
 * <BR>Depending on the features of the specific card terminal a concrete
 * implementation might also implement additional, optional, interfaces (as found in
 * <a href="Package-opencard.opt.terminal"><tt>opencard.opt.terminal</tt></a>.
 * <P>Similar to the <a href="http://www.smartcardsys.com/">PCSC smart card specification</a>
 * we assume that the card terminal will automatically power up the smart card and
 * retrieve the ATR response (available through <tt>cardID()</tt> as a <tt>CardID</tt>
 * object).
 * <P><tt>CardTerminal</tt> objects are created by a <tt>CardTerminalFactory</tt> and
 * registered in the <tt>CardTerminalRegistry</tt>. To get an enumeration of the available
 * <tt>CardTerminal</tt>s invoke <tt>CardTerminalRegistry.getCardTerminals ()</tt>.
 *
 * @author Dirk Husemann       (hud@zurich.ibm.com)
 * @author Peter Trommler      (trp@zurich.ibm.com)
 * @author Mike Wendler        (mwendler@de.ibm.com)
 * @author Stephan Breideneich (sbreiden@de.ibm.com)
 * @version $Id: CardTerminal.java,v 1.2 2005/09/19 10:21:22 asc Exp $
 *
 * @see opencard.core.terminal.CardTerminalRegistry
 * @see opencard.core.terminal.CardTerminalFactory
 * @see opencard.core.terminal.CardID
 */

public abstract class CardTerminal {

  private Tracer itracer        = new Tracer(this, CardTerminal.class);
  private static Tracer ctracer = new Tracer(CardTerminal.class);

  /** slot channels of this terminal (used for communication to cards) */
  private   Vector channels       = new Vector ();

  /** the installed slots in the terminal
  * @deprecated */
  protected Vector slots          = new Vector ();
  private int slotCount = 0;


  /** Useful information about this card terminal:
   * <ul>
   * <li>the name,
   * <li>the type, and
   * <li>the address (usually meaning the COM port)
   * </ul>
   * as registered with the <tt>CardTerminalRegistry</tt>.
   */
  protected final String name;
  protected final String type;
  protected final String address;


  private Object ctListenerMonitor = new String("ctListenerMonitor");


  /**
   * Instantiates a <tt>CardTerminal</tt> object.
   *
   * @param     name
   *		        The user friendly name.
   * @param     type
   *		        The terminal type.
   * @param     address
   *		        An identifier for the driver to locate the terminal.
   */
  protected CardTerminal(String name, String type, String address) {

    ctracer.debug("<init>", "(" + name + ", " + type + ", " + address + ")");

    this.name     = name;
    this.type     = type;
    this.address  = address;
  }


  // instance methods ------------------------------------------------------------


  /**
   * @deprecated
   */
  public Enumeration enumerateSlots() {

    return slots.elements();
  }


  /** Query the card terminal about its features.<p>
   *
   * Each feature is represented by a property. The standard
   * features are
   * <ul>
   * <li>name: as configured and registered with the <tt>CardTerminalRegistry</tt>
   * <li>type: as configured and registered with the <tt>CardTerminalRegistry</tt>
   * <li>address: as configured and registered with the <tt>CardTerminalRegistry</tt>
   * <li>slots: the number of slots belonging to this <tt>CardTerminal</tt>
   * </ul>
   * In addition, each <tt>CardTerminal</tt> implementation provides a set of
   * card terminal specific features.<p>
   *
   * @return A list of properties describing the features of this
   *	       card terminal.
   */
  public final Properties features () {

    Properties features = new Properties();

    internalFeatures (features);

    if (features == null) {
      itracer.debug("features ",
        "implementation error in terminal class -> got 'null' instead of Enumeration object");
      features = new Properties();
    }

    features.put("name", getName () );
    features.put("type", getType () );
    features.put("address", getAddress () );
    features.put("slots", String.valueOf (slotCount) );

    return features;
  }


  /** Return the address of this <tt>CardTerminal</tt>.<p>
   *
   * @return   The address of this <tt>CardTerminal</tt> as a <tt>String</tt>.
   */
  public String getAddress() {

    return address;
  }


  /** Return the answer-to-reset (ATR) response of the card inserted in slot
   *  <tt>slotID</tt> as a <tt>CardID</tt>.
   * Before calling this method the caller should make sure that a card is
   * present, otherwise null may be returned.
   * <p>
   * This call should NOT block if no card is present.
   *
   * @param     slotID
   *		        slot id.
   * @return    The ATR response in form of a <tt>CardID</tt> object
   *            or null if no card is present.
   * @exception CardTerminalException
   *	    	    Thrown in case of problems in the card terminal.
   */
  public abstract CardID getCardID(int slotID)
    throws CardTerminalException;


  /**
   * @deprecated
   */
  public CardID getCardID(int slotID, int ms)
    throws CardTerminalException {
    return getCardID(slotID);
  }


  /** Return the name associated with this <tt>CardTerminal</tt>.<p>
   *
   * @return   The name of this <tt>CardTerminal</tt> as a <tt>String</tt>.
   */
  public String getName() {

    return name;
  }


  /**
   * @deprecated
   */
  public Slot getSlot(int slotID)
    throws IndexOutOfBoundsException {

    if (slotID >= slots.size())
      throw new IndexOutOfBoundsException("slotID too large");

    return (Slot)slots.elementAt(slotID);
  }


  /** Return the type of this <tt>CardTerminal</tt>.<p>
   *
   * @return   The type of this <tt>CardTerminal</tt> as a <tt>String</tt>.
   */
  public String getType() {

    return type;
  }


  /**
   * @deprecated  use getSlots() instead
   */
  public Slot[] slots() {

    synchronized (slots) {
      Slot[] result = new Slot[slots.size()];
      slots.copyInto(result);

      return result;
    }
  }


  /** Return the number of <tt>slots</tt> belonging to this <tt>CardTerminal</tt>
   * object.
   */
  public int getSlots() {
    return slotCount;
  }


  /** Check whether there is a smart card present in a particular slot.
   *
   * @param     slotID
   *	          slot to check for a card.
   * @exception CardTerminalException
   *            thrown in case of problems getting the status from CardTerminal
   *            (or other serious problems in the terminal).
   * @return    True if there is a smart card inserted in the slot.
   */
  public abstract boolean isCardPresent(int slotID)
    throws CardTerminalException;


  /**
   * @deprecated use isCardPresent(int) instead
   */
  public boolean isCardPresent(Slot slot)
    throws CardTerminalException {

    return isCardPresent (slot.getSlotID () );
  }


  /** Check whether a <tt>SlotChannel</tt> is available for a
   *  particular slot.
   *
   * @param     slotID
   *		        The slot to check.
   * @return    True if there is a <tt>SlotChannel</tt> available.
   */
  public synchronized boolean isSlotChannelAvailable(int slotID) {

    return getSlotChannel(slotID) == null;
  }


  /**
   * @deprecated use isSlotChannelAvailable(int) instead
   */
  public synchronized boolean isSlotChannelAvailable(Slot slot) {

    return isSlotChannelAvailable(slot.getSlotID());
  }


  /**
   * Initializes the CardTerminal. Implementations of this method must carry out all
   * steps required to set the concrete terminal into a proper state. After invoking
   * this method it should be possible to communicate with the terminal properly.
   *
   * @exception CardTerminalException
   *            thrown in case of initialization-errors
   *            (e.g. couldn't setup transfer protocol between CardTerminal and reader).
   */
  public abstract void open()
    throws CardTerminalException;


  /**
   * Closes the CardTerminal. Implementations of this method must carry out all
   * steps required to close the concrete terminal and free resources held by it.
   *
   * @exception CardTerminalException
   *            thrown in case of initialization-errors
   *            (e.g. couldn't setup transfer protocol between CardTerminal and reader).
   */
  public abstract void close()
    throws CardTerminalException;


  /** Open a <tt>SlotChannel</tt> on <tt>Slot</tt> number <tt>slotID</tt>.
   *
   * @param     slotID
   *		        The number of the slot for which a <tt>SlotChannel</tt>
   *		        is requested.
   * @exception InvalidSlotChannelException
   *            Thrown when slot channel is already allocated.
   * @exception IndexOutOfBoundsException
   *		        Thrown when <tt>slotID</tt> is out of bounds.
   * @exception CardTerminalException
   *            Thrown when internalOpenSlotChannel failed.
   */
  public final SlotChannel openSlotChannel(int slotID)
    throws InvalidSlotChannelException,
           IndexOutOfBoundsException,
           CardTerminalException {

    return openSlotChannel(slotID, null);
  }

  /** Open a <tt>SlotChannel</tt> on <tt>Slot</tt> number <tt>slotID</tt>.
   *
   * @param     slotID
   *		        The number of the slot for which a <tt>SlotChannel</tt>
   *		        is requested.
   * @param     lockHandle
   *            trust ticket obtained from locking the terminal
   * @exception InvalidSlotChannelException
   *            Thrown when slot channel is already allocated.
   * @exception IndexOutOfBoundsException
   *		        Thrown when <tt>slotID</tt> is out of bounds.
   * @exception CardTerminalException
   *            Thrown when internalOpenSlotChannel failed.
   */
  public final SlotChannel openSlotChannel(int slotID, Object lockHandle)
    throws InvalidSlotChannelException,
           IndexOutOfBoundsException,
           CardTerminalException {

    itracer.debug("openSlotChannel", "for slot #" + slotID);

    if (lockHandle == null) {
       internalOpenSlotChannel(slotID);
    } else {
       internalOpenSlotChannel(slotID, lockHandle);
    }


    if (slotID >= this.slots.size())
      throw new IndexOutOfBoundsException("slot id out of bounds");

    SlotChannel channel = null;
    synchronized(channels) {
      if (!isSlotChannelAvailable (slotID) )
        throw new InvalidSlotChannelException("slot channel already allocated", this);

      channel = new SlotChannel(this, slotID, lockHandle);
      channels.addElement(channel);
    } // synchronized

    itracer.debug("openSlotChannel", "new SlotChannel is " + channel);

    return channel;
  }


  /**
   * @deprecated use openSlotChannel(int) instead
   */
  public final synchronized SlotChannel openSlotChannel(Slot slot)
    throws InvalidSlotChannelException,
           IndexOutOfBoundsException,
           CardTerminalException {

    return openSlotChannel(slot.getSlotID(), null);
  }


  /**
   * Closes a <tt>SlotChannel</tt>.
   *
   * @param     sc
   *            The <tt>SlotChannel</tt> to close.
   * @exception InvalidSlotChannelException
   *            when slot channel of the slot in the passed <TT>SlotChannel</TT> is not the
   *            same as the one passed
   * @exception CardTerminalException
   *            thrown in case of problem in <TT>internalCloseSlotChannel</TT>
   */
  public final void closeSlotChannel (SlotChannel sc)
    throws InvalidSlotChannelException,
           CardTerminalException {

    assertSlotChannelValid(sc);

    synchronized(channels) {
      channels.removeElement(sc);
    }

    internalCloseSlotChannel(sc);
  }



  /**
   * @deprecated use reset(SlotChannel)
   */
  public final CardID reset(SlotChannel sc, int ms)
    throws InvalidSlotChannelException,
           CardTerminalException {

    return reset(sc);
  }


  /** Reset a smart card inserted in a slot.
   *
   * @param     sc
   *		        The open <tt>SlotChannel</tt> attached to the slot.
   * @exception InvalidSlotChannelException
   *		        Thrown when the supplied <tt>SlotChannel</tt> is not valid.
   * @exception CardTerminalException
   *            Thrown when error occurred in <tt>internalReset</tt>
   */
  public final CardID reset(SlotChannel sc)
    throws InvalidSlotChannelException,
           CardTerminalException {

    assertSlotChannelValid(sc);
    return internalReset(sc.getSlotNumber(), -1);
  }


  
  /** Reset a smart card inserted in a slot.
  *
  * @param     sc
  *             The open <tt>SlotChannel</tt> attached to the slot.
  * @param     warm
  *             Perform a warm reset rather than a cold reset
  * @exception InvalidSlotChannelException
  *             Thrown when the supplied <tt>SlotChannel</tt> is not valid.
  * @exception CardTerminalException
  *            Thrown when error occurred in <tt>internalReset</tt>
  */
 public final CardID reset(SlotChannel sc, boolean warm)
   throws InvalidSlotChannelException,
          CardTerminalException {

   assertSlotChannelValid(sc);
   return internalReset(sc.getSlotNumber(), warm);
 }


  /** Send a <tt>CommandAPDU</tt> on a slot.
   *
   * @param     sc
   *		        The open <tt>SlotChannel</tt> attached to the slot.
   * @param     capdu
   *		        The <tt>CommandAPDU</tt> to send.
   * @return    The <tt>ResponseAPDU</tt> as received from the card.
   * @exception InvalidSlotChannelException
   *		        Thrown when the supplied <tt>SlotChannel</tt> is not valid.
   * @exception CardTerminalException
   *            thrown in case of problems in the CardTerminal.
   */
  public final ResponseAPDU sendAPDU(SlotChannel sc, CommandAPDU capdu)
    throws InvalidSlotChannelException,
           CardTerminalException {

    assertSlotChannelValid(sc);
    return internalSendAPDU(sc.getSlotNumber(), capdu, -1);
  }


  /** Send a <tt>CommandAPDU</tt> on a slot.
   * @deprecated
   */
  public final ResponseAPDU sendAPDU(SlotChannel sc, CommandAPDU capdu, int ms)
    throws InvalidSlotChannelException,
           CardTerminalException {

    assertSlotChannelValid(sc);
    return internalSendAPDU(sc.getSlotNumber(), capdu, ms);
  }


  /** Returns a printable representation of this <tt>CardTerminal</tt> object.<p>
   *
   * @return   A <tt>String</tt> representing this <tt>CardTerminal</tt> object.
   */
  public String toString() {

    StringBuffer result = new StringBuffer();
    result.append(super.toString()).append("\n");
    result.append("+ name    ").append(name).append("\n");
    result.append("+ type    ").append(type).append("\n");;
    result.append("+ addr    ").append(address);
    return result.toString();
  }


  // protected methods -------------------------------------------------------------


  /** Add <tt>Slots</tt> to the <tt>CardTerminal</tt>.
   * Used by the concrete <tt>CardTerminal</tt> implementations.
   *
   * @param     numberOfSlots
   *		        The number of slots to add.
   * @exception CardTerminalException
   *            Thrown when instanciating of Slot failed.
   */
  protected void addSlots(int numberOfSlots)
    throws CardTerminalException {

    slotCount+=numberOfSlots;

    // remove following section when removing deprecates
    for (int i = 0; i < numberOfSlots; i++)
      slots.addElement(new Slot(this, i));
    slots.trimToSize();
    // end remove following slot
  }


  /**
   * Notify listeners that a card was removed from a slot of this terminal.
   * (utility method).
   *
   * @param      slot
   *             slot number of the slot where a card was removed
   *
   */
  protected void cardRemoved(int slotID) {

    itracer.debug("cardRemoved", "slotID " + slotID);

    CardTerminalRegistry.getRegistry().cardRemoved(this, slotID);

  }

  /**
   * Notify listeners that a card was inserted into a slot of this terminal.
   * (utility method).
   *
   * @param     slot
   *            slot number of the slot where a card was inserted
   *
   */
  protected void cardInserted(int slotID) {

    itracer.debug("cardInserted", "slotID " + slotID + ", " + ")");

    CardTerminalRegistry.getRegistry().cardInserted(this, slotID);

  }

  /** The <tt>CardTerminal</tt> internal <tt>features()</tt> method
   * to be provided by the concrete implementation.
   * This default implementation just returns the parameter provided.
   * Concrete implementations should override this method.
   *
   * @param     features
   *		        A <tt>Properties</tt> object that needs to be enhanced
   *		        with the card terminal specific features.
   * @return    The enriched <tt>Properties</tt> object.
   */
  protected Properties internalFeatures(Properties features) {
    return features;
  }


  /**
   * The internal openSlotChannel method.
   *  <tt>internalOpenSlotChannel</tt> is executed at the beginning of openSlotChannel.
   *
   * @param     slotID
   *		        The number of the slot for which a <tt>SlotChannel</tt> is requested.
   * @exception CardTerminalException
   *            thrown in case of errors opening the card (e.g. error powering card).
   */
  protected void internalOpenSlotChannel(int slotID)
    throws CardTerminalException
  {}

  /**
   * The internal openSlotChannel method for locked terminals (default implementation).
   * Lockable terminals MUST overwrite this method.
   *  <tt>internalOpenSlotChannel</tt> is executed at the beginning of openSlotChannel.
   *
   * @param     slotID
   *		        The number of the slot for which a <tt>SlotChannel</tt> is requested.
   * @param     lockHandle
   *            the trust ticket that the lock owner obtained when locking the terminal
   *            or slot
   * @exception CardTerminalException
   *            thrown in case of errors opening the card (e.g. error powering card).
   */
  protected void internalOpenSlotChannel(int slotID, Object lockHandle)
    throws CardTerminalException
  { internalOpenSlotChannel(slotID); }

  /**
   * The internal closeSlotChannel method.
   *  <tt>internalCloseSlotChannel</tt> is executed at the end of closeSlotChannel.
   *
   * @param     SlotChannel
   *		The <tt>SlotChannel</tt> to close.
   * @exception CardTerminalException
   *            thrown in case of errors closing the card (e.g. error disconnecting the card).
   */
  protected void internalCloseSlotChannel(SlotChannel sc)
    throws CardTerminalException
  {}


  /** The internal reset method to be provided by the concrete implementation.
   *
   * @param     slot
   *		        The slot number of the slot to be resetted.
   * @param     ms
   *		        To be ignored. If the card does not respond within the time
   *            specified for the protocol an exception should be thrown.
   * @return    The <tt>CardID</tt> of the card.
   * @exception CardTerminalException
   *            thrown in case of errors during reset
   */
  protected abstract CardID internalReset(int slot, int ms)
    throws CardTerminalException;


  /** The internal reset method may be overwritten by the actual implementation to support warm resets.
  *
  * @param     slot
  *             The slot number of the slot to be resetted.
  * @param     ms
  *             To be ignored. If the card does not respond within the time
  *            specified for the protocol an exception should be thrown.
  * @return    The <tt>CardID</tt> of the card.
  * @exception CardTerminalException
  *            thrown in case of errors during reset
  */
 protected CardID internalReset(int slot, boolean warm) throws CardTerminalException {
     return(internalReset(slot, -1));
 }


  /** The <tt>internalSendAPDU</tt>  method to be provided by
   * the concrete implementation.
   *
   * @param     slot
   *		        The slot number of the slot to be resetted.
   * @param     capdu
   *		        The <tt>CommandAPDU</tt> to send.
   * @param     ms
   *		        To be ignored. If the card does not respond within the time
   *            specified for the protocol an exception should be thrown.
   * @return    A <tt>ResponseAPDU</tt>.
   * @exception CardTerminalException
   *            thrown in case of errors in the CardTerminal (e.g. errors during data exchange)
   */
  protected abstract ResponseAPDU internalSendAPDU(int slot, CommandAPDU capdu, int ms)
    throws CardTerminalException;


  // private methods -------------------------------------------------------------


  /**
   * Private helper method to assert that a <tt>SlotChannel</tt> is legitimate.
   *
   * @exception InvalidSlotChannelException
   *		        Thrown when the supplied <tt>SlotChannel</tt> is not valid.
   */
  private void assertSlotChannelValid(SlotChannel sc)
    throws InvalidSlotChannelException {

    if (getSlotChannel(sc.getSlotNumber()) != sc)
      throw new InvalidSlotChannelException("illegal SlotChannel", this);
  }


  /**
   * Lookup a <tt>SlotChannel</tt> that has <tt>slotID</tt>
   */
  private SlotChannel getSlotChannel(int slotID) {

    synchronized(channels) {
      Enumeration channs = channels.elements();
      while (channs.hasMoreElements()) {
        SlotChannel channel = (SlotChannel)channs.nextElement();
        if (channel.getSlotNumber() == slotID)
          return channel;
      }
    } // synchronized

    return null;
  }

  /** default implementation for deprecated method in interface VerifiedAPDUInterface.
  * Note: throws an exception if this terminal does not implement VerifiedAPDUInterface
  */
  public ResponseAPDU sendVerifiedCommandAPDU(SlotChannel chann, CommandAPDU capdu, CHVControl vc, int ms)
    throws CardTerminalException
  {
    VerifiedAPDUInterface v = (VerifiedAPDUInterface)this;
    return v.sendVerifiedCommandAPDU(chann,capdu,vc);
  }
}
