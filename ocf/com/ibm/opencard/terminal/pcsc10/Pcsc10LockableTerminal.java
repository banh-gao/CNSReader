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

package com.ibm.opencard.terminal.pcsc10;

import java.util.Properties;

import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.Pollable;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.SlotChannel;
import opencard.core.util.Tracer;
import opencard.opt.terminal.AbstractLockableTerminal;
import opencard.opt.terminal.TerminalCommand;
import opencard.opt.terminal.TerminalLockedException;

/** Implementation of a lockable  OpenCard <tt>CardTerminal</tt> for PCSC.
 *
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 * @version $Id: Pcsc10LockableTerminal.java,v 1.2 2005/09/19 10:21:22 asc Exp $
 *
 * @see opencard.opt.terminal.AbstractLockableTerminal
 */
public final class Pcsc10LockableTerminal 
extends AbstractLockableTerminal

implements TerminalCommand,
Pollable {

  private Tracer itracer = new Tracer(this, Pcsc10LockableTerminal.class);

  /** The reference to the PCSC ResourceManager for this card terminal. */
  private OCFPCSC1 pcsc;

  /** The context to the PCSC ResourceManager */
  private int context = 0;

  /** The state of this card terminal. */
  private boolean closed;

  /** the lock state of this card terminal */
  private boolean locked = false;

  /** Is a card inserted currently? */
  private boolean cardInserted;

  /** The cardHandle */
  private int cardHandle = 0;

  /* states returned by SCardGetStatusChange */
  private static final int SCARD_STATE_MUTE=0x200;
  private static final int SCARD_STATE_PRESENT=0x020;

  /** The <tt>CardID</tt> of the presently inserted card. */
  private CardID cid = null;

  /** The <tt>ATR</tt> of the presently inserted card. */
  private byte[] cachedATR;


  /** Instantiate an <tt>Pcsc10Terminal</tt>.
   *
   * @param     name
   *            The user friendly name.
   * @param     type
   *            The terminal type (here "PCSC")
   * @param     address
   *            not used
   * @exception CardTerminalException
   *            Thrown when a problem occured.
   */
  protected Pcsc10LockableTerminal(String name, String type,String address)
  throws CardTerminalException {

    super(name, type, address);

    try {
      itracer.debug("Pcsc10LockableTerminal", "connect to PCSC 1.0 resource manager");

      // load native library
      OCFPCSC1.loadLib();
      pcsc = new OCFPCSC1();

      /* connect to the PCSC resource manager */
      context = pcsc.SCardEstablishContext(Pcsc10Constants.SCARD_SCOPE_USER);

      itracer.debug("Pcsc10LockableTerminal", "Driver initialized");

    } catch (PcscException e) {
      throw translatePcscException(e);
    }

    /* add one slot */
    addSlots(1);
  }


  /** Open the card terminal: We register with the <tt>CardTerminalRegistry</tt>
   *  as a <tt>Pollable</tt> card terminal.
   */
  public void open() 
  throws CardTerminalException {

    CardTerminalRegistry.getRegistry().addPollable((Pollable)this);
    closed = false;
  }


  /** Close the connection to the card terminal.
   *  Could be used by unregister to free up the resources used by the
   *  terminal.
   *
   * @exception opencard.core.terminal.CardTerminalException
   *            Thrown if there are problems with closing the
   *            connection
   */
  public void close() 
  throws CardTerminalException {

    if (!closed) {
      itracer.debug("close", "disable polling");

      CardTerminalRegistry.getRegistry().removePollable((Pollable)this);

      closed = true;

      // is card inserted and powered?
      if (cardInserted && cid != null) {
        itracer.debug("close", "card inserted - try to power down card");

        cid = null;    // invalidate cardID

        cardDisconnect(Pcsc10Constants.SCARD_EJECT_CARD);

      } else
        itracer.debug("close", "no card inserted");

      try {
        itracer.debug("close", "release context");
        pcsc.SCardReleaseContext(context);

      } catch (PcscException e) {
        throw translatePcscException(e);
      }
    } else {
      itracer.debug("close", "Terminal already closed!");
      throw new CardTerminalException("Pcsc10LockableTerminal: already closed");
    }
  }


  /** Implementation of <tt>CardTerminal.internalReset()</tt>. */
  protected CardID internalReset(int slot, int ms) 
  throws CardTerminalException {

    // check if cardHandle exists
    if (isCardConnected()) {
      itracer.debug("internalReset", "cardHandle exists - try reconnect");
      cid = null;    // invalidate CardID

      Integer returnedProtocol = new Integer(0);

      try {
        pcsc.SCardReconnect(cardHandle,
                            Pcsc10Constants.SCARD_SHARE_EXCLUSIVE,
                            Pcsc10Constants.SCARD_PROTOCOL_T0 | Pcsc10Constants.SCARD_PROTOCOL_T1,
                            Pcsc10Constants.SCARD_RESET_CARD,
                            returnedProtocol);

        cid = new CardID(this,0, cachedATR);
      } catch (PcscException e) {
        throw translatePcscException(e);
      }

      return getCardID(slot);

    } else {
      itracer.debug("internalReset", "card reset failed - no card inserted");
      return null;
    }
  }



  /** Query the card terminal about its features.<p>
   *
   *  @return features
   *
   */
  protected Properties internalFeatures(Properties features) {
    return features;
  }


  /** Check whether there is a smart card present.
   *
   * @param  slot
   *         Number of the slot to check (must be 0 for PCSC)
   * @return True if there is a smart card inserted in the card
   *         terminals slot.
   */
  public synchronized boolean isCardPresent(int slot) 
  throws CardTerminalException {

    // check if terminal is already closed...
    if (!closed) {

      // check for the right slot-number
      if (slot != 0)
        throw new CardTerminalException("Invalid Slot number: " + slot);

      boolean result = pcscIsCardPresent();

      // locked terminal must update connection mode
      // from SHARE_DIRECT to SHARE_EXCLUSIVE and reverse
      if ((result!=cardInserted) && locked) {
        updateConnectionMode(result);
      }

      return result;
    } else
      return false; // return "no card inserted", because terminal is already closed
  }

  private boolean pcscIsCardPresent() throws CardTerminalException {

    /* fill in the data structure for the state request */
    PcscReaderState[] rState = new PcscReaderState[1];
    rState[0] = new PcscReaderState();
    rState[0].CurrentState = Pcsc10Constants.SCARD_STATE_UNAWARE;
    rState[0].Reader = getName();

    try {
      /* set the timeout to 1 second */
      pcsc.SCardGetStatusChange(context, 1, rState);
    } catch (PcscException e) {
      throw translatePcscException(e);
    }

    // PTR 0219: check if a card is present but unresponsive
    if ( ((rState[0].EventState & SCARD_STATE_MUTE)!=0)
         &&
         ((rState[0].EventState & SCARD_STATE_PRESENT)!=0)) {

      throw new CardTerminalException("Card present but unresponsive in slot 0");
    }

    /* SCardGetStatusChange does not work in SHARE_DIRECT mode */
    if ( ((rState[0].EventState & Pcsc10Constants.SCARD_STATE_UNKNOWN)!=0)
         ||
         ((rState[0].EventState & Pcsc10Constants.SCARD_STATE_UNAVAILABLE)!=0)) {

      // use SCardGetAttrib to test card presence 
      byte[] response= new byte[1];

      try {
        response = pcsc.SCardGetAttrib(cardHandle, Pcsc10Constants.SCARD_ATTR_ICC_PRESENCE);
      } catch (PcscException e) {
        throw translatePcscException(e);
      }


      if (response[0]==2) {
        itracer.debug("isCardPresent", "SCardGetAttrib: card present");
        return true;
      } else {
        itracer.debug("isCardPresent", "SCardGetAttrib: no card present "+response[0]);
        return false;
      }

    } else {
      cachedATR = rState[0].ATR;

      // check the length of the returned ATR. if ATR is empty / null, no card is inserted
      if (cachedATR != null) {
        if (cachedATR.length > 0)
          return true;
        else
          return false;
      } else
        return false;
    }
  }

  private void updateConnectionMode(boolean cardPresent) throws CardTerminalException {
    try {
      Integer returnedProtocol = new Integer(0);
      if (cardPresent) {
        itracer.debug("isCardPresent", "status change, reconnecting share exclusive");
        pcsc.SCardReconnect(cardHandle,
                            Pcsc10Constants.SCARD_SHARE_EXCLUSIVE,
                            Pcsc10Constants.SCARD_PROTOCOL_T0 | Pcsc10Constants.SCARD_PROTOCOL_T1,
                            Pcsc10Constants.SCARD_UNPOWER_CARD,
                            returnedProtocol);

        cachedATR = pcsc.SCardGetAttrib(cardHandle, Pcsc10Constants.SCARD_ATTR_ATR_STRING);

      } else {
        itracer.debug("isCardPresent", "status change, reconnecting share direct");
        pcsc.SCardReconnect(cardHandle,
                            Pcsc10Constants.SCARD_SHARE_DIRECT,
                            Pcsc10Constants.SCARD_PROTOCOL_OPTIMAL,
                            Pcsc10Constants.SCARD_LEAVE_CARD,
                            returnedProtocol);
        cachedATR = null;
      }
    } catch (PcscException e) {
      throw translatePcscException(e);
    }

  }

  /** @deprecated
   */
  public CardID getCardID(int slot, int timeout) 
  throws CardTerminalException {

    return getCardID(slot);
  }


  /** Return the <tt>CardID</tt> of the presently inserted card. Will returned
   *  the cached card if slot's status has not changed; otherwise it will
   *  really retrieve the <tt>CardID</tt>.<p>
   *
   *  @param      slot
   *      slot number
   *  @return     A <tt>CardID</tt> object representing the inserted smart card.
   *  @exception  opencard.core.terminal.CardTerminalException
   *      thrown when problem occured getting the ATR of the card
   */
  public CardID getCardID(int slot)  
  throws CardTerminalException {
    // ... the PCSC card terminal has only one slot
    if (slot != 0)
      throw new CardTerminalException("Invalid slot number: " + slot);

    return cid;
  }

  /** 
   * The internal openSlotChannel method.
   *  <tt>lockableOpenSlotChannel</tt> is executed at the beginning of openSlotChannel.
   *
   * @param     slotID
   *    The number of the slot for which a <tt>SlotChannel</tt> is requested.
   */
  protected synchronized void lockableOpenSlotChannel(int slotID)
  throws CardTerminalException {
    if (!locked) {
      cardConnect();
    }
  }

  /**
   * The internal closeSlotChannel method.
   *  <tt>internalCloseSlotChannel</tt> is executed at the end of closeSlotChannel.
   *
   * @param     sc
   *		The <tt>SlotChannel</tt> to close.
   * @exception CardTerminalException
   *            thrown in case of errors closing the card (e.g. error disconnecting the card).
   */
  protected void internalCloseSlotChannel(SlotChannel sc)
  throws CardTerminalException
  {
    super.internalCloseSlotChannel(sc);
    if (!locked) {
      cardDisconnect(Pcsc10Constants.SCARD_LEAVE_CARD);
    }
  }

  /** returns true if card is connected */
  private boolean isCardConnected() {
    return((cardHandle != 0) && (this.cardInserted==true));
  }

  /** get card handle */
  private int getCardHandle() {
    return cardHandle;
  }

  /** connect to the card */
  private void cardConnect() throws CardTerminalException {

    // we use the EXCLUSIVE mode of PCSC, so we cannot connect to the reader without a card inserted

    Integer returnedProtocol = new Integer(0);    
    try {
      itracer.debug("cardConnect", "connect to smartcard");
      cardHandle = pcsc.SCardConnect(context,
                                     getName(),
                                     Pcsc10Constants.SCARD_SHARE_EXCLUSIVE,
                                     Pcsc10Constants.SCARD_PROTOCOL_T0 | Pcsc10Constants.SCARD_PROTOCOL_T1,
                                     returnedProtocol);
      itracer.debug("cardConnect", "got card handle: " + cardHandle);                   
    } catch (PcscException e) {
      throw translatePcscException(e);
    }
  }

  /** encapsulates SCardDisconnect
   *
   * @param disposition
   */
  private void cardDisconnect(int disposition) throws CardTerminalException {
    if (cardHandle != 0) {
      try {
        itracer.debug("cardDisconnect", "disconnect smartcard - cardHandle=" + cardHandle);
        pcsc.SCardDisconnect(cardHandle, disposition);
      } catch (PcscException e) {
        throw translatePcscException(e);
      } finally {
        itracer.debug("cardDisconnect", "invalidate card handle");
        cardHandle = 0;
      }
    } else
      itracer.debug("cardDisconnect", "cardHandle already 0 - disconnect impossible");
  }


  /** Send control command to terminal.
   *
   * @param     cmd
   *            a byte array containing the command to be sent to the card terminal
   * @return    Response from terminal.
   * @exception opencard.core.terminal.CardTerminalException
   *            Exception thrown by driver.
   * @see       opencard.opt.terminal.TerminalCommand
   */
  public byte[] sendTerminalCommand(byte[] cmd)
  throws CardTerminalException {
     return sendTerminalCommand(cmd, null);
  }

  /** Send control command to terminal.
   *
   * @param     cmd
   *            a byte array containing the command to be sent to the card terminal
   * @return    Response from terminal.
   * @exception opencard.core.terminal.CardTerminalException
   *            Exception thrown by driver.
   * @see       opencard.opt.terminal.TerminalCommand
   */
  public byte[] sendTerminalCommand(byte[] cmd, Object lockHandle)
  throws CardTerminalException {
    if (cardHandle == 0)
      throw new CardTerminalException("no card present", this);


    // check if caller is lock owner
    if (locked) {

      // check that no other thread has locked the terminal or a slot
      if ((this.getTerminalLockHandle() != lockHandle) 
          &&
          (this.getSlotLockHandle(0) != lockHandle)) {
        throw new TerminalLockedException("terminal locked, invalid lock handle", this);
      }
    }

    try {
      byte[] responseData = pcsc.SCardControl(cardHandle, 0, cmd);
      return responseData;

    } catch (PcscException e) {
      throw translatePcscException(e);
    }
  }


  /** The implementation of <tt>CardTerminal.internalSendAPDU()</tt>.
   *
   * @param slot
   *        logical slot number
   * @param capdu
   *        C-APDU to send to the card
   * @param ms
   *        timeout in ms (not supported, ignored)
   */
  protected ResponseAPDU internalSendAPDU(int slot, CommandAPDU capdu, int ms) 
  throws CardTerminalException {

    // ... the Pcsc card terminal has only one slot
    if (slot != 0)
      throw new CardTerminalException("Invalid slot: " + slot);

    itracer.debug("internalSendAPDU", "sending " + capdu);

    byte [] responseData = null;
    try {
      responseData = pcsc.SCardTransmit(cardHandle, capdu.getBytes());
    } catch (PcscException e) {

      // check for SemaphoreTimeout
      if (e.returnCode() == 0x79) {
        // try a card reset (reconnect) with timeout 5 seconds
        internalReset(0,5000);
        throw new CardTerminalException("PC/SC Error: semaphore timeout - perhaps forbidden or wrong card command.");
      } else
        throw translatePcscException(e);
    }
    ResponseAPDU rAPDU = new ResponseAPDU(responseData);
    itracer.debug("internalSendAPDU", "receiving " + rAPDU);
    return rAPDU;
  }


  /** Signal to observers that an inserted card was removed.<p>
   *
   * @param slotID
   *        slot number
   */
  protected void cardRemoved(int slotID) {

    super.cardRemoved(slotID);
    if (!locked) {
      try {
        cardDisconnect(Pcsc10Constants.SCARD_LEAVE_CARD);
      } catch (CardTerminalException cte) {
        // ignore this exception
      }
    }
  }

  /** This method is normally used by the <tt>CardTerminalRegistry</tt> to
   *  generate the <tt>OpenCard</tt> events if the Slot implementation does
   *  not support events itself.
   */
  public void poll() 
  throws CardTerminalException {

    if (!closed) {
      try {
        boolean newStatus = isCardPresent(0);
        if (cardInserted != newStatus) {
          itracer.debug("poll", "status change");
          cardInserted = !cardInserted;
          // ... notify listeners
          if (cardInserted) {
            cid = new CardID(this,0, cachedATR); // U.Steinmueller Infineon
            cardInserted(0);
          } else {
            cardRemoved(0);
          }
        } else {
          // ... no change took place
          itracer.debug("poll", "no status change");
        }
      } catch (CardTerminalException cte) {
        itracer.debug("poll", cte);

        // make sure the CardTerminalException is 
        // propagated to listeners waiting for a card
        cardInserted(0);
      }
    }
  }


  private void readerConnect() throws CardTerminalException {

    // we use the DIRECT mode of PCSC, so we can connect to the reader even without a card inserted

    Integer returnedProtocol = new Integer(0);    
    try {
      itracer.debug("readerConnect", "connect to reader");
      cardHandle = pcsc.SCardConnect(context,
                                     getName(),
                                     Pcsc10Constants.SCARD_SHARE_DIRECT,
                                     Pcsc10Constants.SCARD_PROTOCOL_OPTIMAL,
                                     returnedProtocol);
      itracer.debug("readerConnect", "got card handle: " + cardHandle);                   
    } catch (PcscException e) {
      throw translatePcscException(e);
    }
  }


  /** translate the PcscException into CardTerminalException.<p>
    */
  protected CardTerminalException translatePcscException(PcscException e) {
    return new CardTerminalException("Pcsc10LockableTerminal: " + e.getMessage(), this);
  }


  protected synchronized void internalLock() throws CardTerminalException {
    // lock the one and only slot
    internalLockSlot(0);
  }

  protected synchronized void internalUnlock() throws CardTerminalException{
    // unlock the one and only slot
    internalUnlockSlot(0);
  }

  protected synchronized void internalLockSlot(int slotNr) throws CardTerminalException{
    // connect to reader in mode SHARE_DIRECT
    readerConnect();

    locked = true;
  }
  protected synchronized void internalUnlockSlot(int slotNr) throws CardTerminalException{

    // disconnect from reader, leave card in slot
    cardDisconnect(Pcsc10Constants.SCARD_LEAVE_CARD);

    locked = false;
  }

}
