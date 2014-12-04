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

package opencard.opt.terminal;


import java.util.Hashtable;

import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;

/** <tt>AbstractLockableTerminal</tt> is a base class
 *  for implementing lockable CardTerminals.
 *
 * @author  Peter Bendel(peter_bendel@de.ibm.com)
 * @version $Id: AbstractLockableTerminal.java,v 1.1 1999/10/22 14:43:29 damke Exp $
 *
 * @see opencard.core.terminal.CardTerminal
 * @see opencard.opt.terminal.Lockable
 */

 public abstract class AbstractLockableTerminal extends CardTerminal
   implements Lockable {

    /** the trust ticket for the whole terminal */
    private Object lockHandle_ = null;

    /** the trust tickets for each slot */
    private Hashtable slotHandles_ = new Hashtable();


    /** accessors for subclasses */
    protected Object getTerminalLockHandle() {return lockHandle_;}
    protected Thread getSlotLockHandle(int slot)
     {return (Thread)slotHandles_.get(new Integer(slot));}


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
  protected AbstractLockableTerminal(String name, String type, String address) {
    super(name,type,address);
  }


    /** Lock the whole terminal including all slots, pinpad, display and
    * other resources.
    * <p>
    * A thread can call CardTerminal.lock() to lock a card terminal
    * and all of its slots and CardTerminal.unlock() to return
    * ownership of the lock. The lock() method can only be called
    * successfully when the card terminal has no slot channels open
    * and no other thread has locked a slot. The lock() call also
    * prevents other threads from using the pin pad and display of
    * the card terminal and from sending terminal commands.
    * <p>
    * Checking these conditions and maintaining the lock owner
    * and list of locked slots is done in this abstract class.
    * The actual locking is delegated to specific subclasses.
    * <p>
    * Important: whether the caller may send a terminal command MUST
    * be checked in the subclass, since this abstract class
    * cannot know whether the subclass implements the TerminalCommand
    * interface.
    * <p>
    * @return a handle (trust ticket) for the lock owner
    * @exception TerminalLockedException locking failed
    */
    public final synchronized Object lock() throws CardTerminalException {

      // check that neither the terminal nor a slot is locked
      if (lockHandle_ != null) {
        throw new TerminalLockedException("terminal already locked", this);
      }

      if (!slotHandles_.isEmpty()) {
        throw new TerminalLockedException("terminal has locked slots", this);
      }

      // locking only allowed if no SlotChannel is open
      int slots = getSlots();
      for (int i = 0; i < slots; i++) {
        if (!isSlotChannelAvailable(i)) {
          throw new TerminalLockedException("terminal has open SlotChannel(s)", this);
        }
      }

      // delegates terminal specifics of locking to subclass
      internalLock();

      lockHandle_ = new Integer(0);

      return lockHandle_;
    }

    /**
    * Unlock the whole terminal including all slots, pinpad, other resources.
    * This call can only be called by a thread that has previously locked
    * the terminal using lock().
    * It should not be used to unlock after having used lockSlot().
    * @exception TerminalLockedException unlocking failed
    */
    public synchronized void unlock(Object handle) throws CardTerminalException {


       if (lockHandle_ == null) {
         throw new TerminalLockedException("terminal not locked", this);
       }

      // check that no other thread has locked the terminal or a slot
      if (lockHandle_!= handle) {
        throw new TerminalLockedException("caller not lock owner", this);
      }

      // do the actual unlocking in subclass
      internalUnlock();

      lockHandle_ = null;
    }


    /*
    * Locks the slot with the given slot number, returns a
    * reference to the new slot owner on success.
    * <p>
    * A thread can call CardTerminal.lockSlot() to lock a particular
    * slot and CardTerminal.unlockSlot() to return ownership of
    * the lock. The lockSlot() method can only be called successfully
    * when there is no open SlotChannel for the slot to be locked and
    * when the terminal is not locked as a whole using lock().
    * <p>
    * @param slotNr number of the slot to be locked
    * @return a handle (trust ticket) for the lock owner
    * @exception TerminalLockedException locking failed
    */
    public Object lockSlot(int slotNr) throws CardTerminalException {

      // check that no thread has locked the terminal as a whole
      if (lockHandle_ != null) {
        throw new TerminalLockedException("terminal is locked", this);
      }

      Object slotHandle = getSlotLockHandle(slotNr);

      // check that no other thread has locked the slot
      if (slotHandle != null) {
        throw new TerminalLockedException("slot is already locked", this);
      }

      // check that no SlotChannel is open for the slot
      if (isSlotChannelAvailable(slotNr)) {
        throw new TerminalLockedException("slot is in use, Slotchannel exists", this);
      }

      // delegate implementation specifics to subclass
      internalLockSlot(slotNr);

      slotHandle = new Integer(slotNr);

      slotHandles_.put(new Integer(slotNr), slotHandle);

      return slotHandle;
    }

    /*
    * Unlocks the slot with the given slot number.
    * @exception TerminalLockedException unlocking failed
    */
    public void unlockSlot(int slotNr, Object handle) throws CardTerminalException {

      // check that no more SlotChannel is open
      if (!isSlotChannelAvailable(slotNr)) {
        throw new TerminalLockedException("can not unlock slot while SlotChannel is open",this);
      }

      // check that no other thread has locked the slot
      Object slotHandle = getSlotLockHandle(slotNr);

      // check that no other thread has locked the slot
      if (slotHandle != handle)  {
        throw new TerminalLockedException("invalid handle", this);
      }

      if (slotHandle != null) {
        // do the actual unlocking in subclass
        internalUnlockSlot(slotNr);

        slotHandles_.remove(new Integer(slotNr));
      }
    }


    protected abstract void internalLock() throws CardTerminalException;
    protected abstract void internalUnlock() throws CardTerminalException;
    protected abstract void internalLockSlot(int slotNr) throws CardTerminalException;
    protected abstract void internalUnlockSlot(int slotNr) throws CardTerminalException;


    final protected void internalOpenSlotChannel(int slotID)
       throws CardTerminalException {
       // check if caller has the right to call

      // is the whole terminal locked?
      if (lockHandle_ != null) {
          throw new TerminalLockedException("terminal is locked", this);
      } else {
        // is the slot locked ?
        Object slotHandle = getSlotLockHandle(slotID);

        if (slotHandle != null) {
          throw new TerminalLockedException("slot is locked", this);
        }
      }

       // and delegates the rest to subclass
       lockableOpenSlotChannel(slotID);
    }


    final protected void internalOpenSlotChannel(int slotID, Object lockHandle)
       throws CardTerminalException {
       // check if caller has the right to call

      // is the whole terminal locked?
      if ((lockHandle_ != null) && (lockHandle_ != lockHandle)) {
          throw new TerminalLockedException("terminal locked, invalid handle", this);
      } else {
        // is the slot locked ?
        Object slotHandle = getSlotLockHandle(slotID);

        if ((slotHandle != null) && (slotHandle != lockHandle)) {
          throw new TerminalLockedException("slot is locked, invalid handle", this);
        }
      }

       // and delegates the rest to subclass
       lockableOpenSlotChannel(slotID);
    }

    protected abstract void lockableOpenSlotChannel(int slot)
       throws CardTerminalException;



 }
