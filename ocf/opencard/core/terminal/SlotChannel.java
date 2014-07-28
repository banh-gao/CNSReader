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


import opencard.core.util.APDUTracer;
import opencard.core.util.Tracer;

/** A <tt>SlotChannel</tt> serves a dual purpose: It is a gate object
 * providing access to the smart card and, in addition, is used to send and
 * receive APDUs and generally interact with the card.<p>
 *
 * @author  Dirk Husemann (hud@zurich.ibm.com)
 * @author  Peter Trommler (trp@zurich.ibm.com)
 * @version $Id: SlotChannel.java,v 1.1.1.1 2004/04/08 10:29:27 asc Exp $
 * @see     opencard.core.terminal.Slot
 */

public final class SlotChannel {
    private Tracer itracer = new Tracer(this, SlotChannel.class);
    private APDUTracer aPDUTracer = null;
    
    /** The "owning" <tt>CardTerminal</tt> object. */
    private final CardTerminal terminal;
    /** The number of the attached slot. */
    private final int slotID;
    /** The current state of this <tt>SlotChannel</tt>. <tt>false</tt> means the slot
        channel is closed, <tt>true</tt> means it is open. */
    private boolean slotChannelState = false;
    /** The current scheduler. */
    private Object scheduler;

    /** The lock handle of the lock owner of the terminal/slot
     */
    private Object lockHandle;

    /** Instantiate a <tt>SlotChannel</tt>.
     *
     * @param     terminal
     *            The terminal to which the slot belongs.
     * @param     slotID
     *            The number of the slot to which this
     *            <tt>SlotChannel</tt> is attached.
     * @param     lockHandle
     *            the owner who holds a lock on the slot
     */
    public SlotChannel(CardTerminal terminal, int slotID, Object lockHandle) {
        this.terminal = terminal;
        this.slotID = slotID;
        this.slotChannelState = true;
        this.lockHandle = lockHandle;
    }

    /* Internal method used by card service registry.
     * Gets the current scheduler associated with this slot channel.
     *
     * @return scheduler The <tt>CardServiceScheduler</tt> currently
     *                   associated with this slot channel.
     */
    public Object getScheduler() {
      return scheduler;
    }

    /* Internal method used by card service registry.
     * Sets the current scheduler.
     *
     * @param The <tt>CardServiceScheduler</tt> to be associated with
     *        the slot channel.
     */
    public void setScheduler(Object scheduler) {
      this.scheduler = scheduler;
    }

    /** Send a <tt>CommandAPDU</tt> on this <tt>SlotChannel</tt>.
     *
     * @param     capdu
     *            The <tt>CommandAPDU</tt> to send.
     * @exception CardTerminalException
     *            Thrown when terminal.sendAPDU failed.
     */
    public ResponseAPDU sendAPDU(CommandAPDU capdu)
      throws CardTerminalException {
        if (aPDUTracer == null) {
            return this.terminal.sendAPDU(this, capdu);
        }
        this.aPDUTracer.traceCommandAPDU(this, capdu);
        ResponseAPDU rapdu = this.terminal.sendAPDU(this, capdu);
        this.aPDUTracer.traceResponseAPDU(this, rapdu);
        return rapdu;
    }

    /**
     * @deprecated
     */
    public ResponseAPDU sendAPDU(CommandAPDU capdu, int ms)
      throws CardTerminalException {
        return this.terminal.sendAPDU(this, capdu);
    }

    /** Return the <tt>Slot</tt> object associated with this <tt>SlotChannel</tt>.
     * @deprecated use getSlotNumber(), getCardTerminal() instead
     */
    public Slot getSlot() {
        return this.terminal.getSlot(slotID);
    }

    /** Return the slot number of the associated slot.*/
    public int getSlotNumber() {
        return this.slotID;
    }

    /** Return the <tt>CardTerminal</tt>.
     *
     * @return   The <tt>CardTerminal</tt> instance owning this
     *           <tt>SlotChannel</tt>.
     */
    public CardTerminal getCardTerminal() {
        return this.terminal;
    }

    /** Return the slot owner.
     *
     * @return   The Thread object that holds a lock for this slot
     *           or null if the slot is not locked.
     */
    public Object getLockHandle() {
        return this.lockHandle;
    }

    /**
     *  @deprecated
     */
    public CardID reset(int ms)
      throws CardTerminalException {
        return this.terminal.reset(this);
    }

    /** Reset the smart card attached to this <tt>SlotChannel</tt>'s
     * slot.
     *
     * @param warm Perform warm reset if true
     * @return    The <tt>CardID</tt> of the attached card.
     * @exception CardTerminalException
     *            Thrown when terminal.reset failed.
     */
    public CardID reset(boolean warm)
      throws CardTerminalException {

        return this.terminal.reset(this, warm);
    }

    /** Reset the smart card attached to this <tt>SlotChannel</tt>'s
     * slot.
     *
     * @return    The <tt>CardID</tt> of the attached card.
     * @exception CardTerminalException
     *            Thrown when terminal.reset failed.
     */
    public CardID reset()
    throws CardTerminalException {

      return reset(false);
    }

    /** Check whether this <tt>SlotChannel</tt> is open.
        nnn   *
        * @return    True if it is; false otherwise.
        */
    public boolean isOpen() {
        return slotChannelState;
    }

    /** Close this <tt>SlotChannel</tt>. Once a <tt>SlotChannel</tt> is
     * closed, it cannot be used any longer.
     * @exception CardTerminalException
     *            Thrown when terminal.closeSlotChannel failed.
     */
    public void close()
      throws CardTerminalException {

        itracer.debug("close", "closing channel");
        slotChannelState = false;
        this.terminal.closeSlotChannel(this);
    }

    /** Return the <tt>CardID</tt> object of the inserted smart card.
     *
     * @return    The <tt>CardID</tt> of the inserted smart card.
     * @exception CardTerminalException
     *            Thrown when terminal.getCardID failed.
     */
    public CardID getCardID()
      throws CardTerminalException {

        return this.terminal.getCardID(this.slotID);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append("\n+ state ").append(slotChannelState ? " open" : " closed");
        return sb.toString();
    }
    
    /**
     * Set a tracer to log all command and response APDUs send over this slot channel
     * @param tracer the APDU tracer
     */
    public void setAPDUTracer(APDUTracer tracer) {
        aPDUTracer = tracer;
    }
    
    /**
     * Return the APDU tracer for this slot channel
     * 
     * @return the APDU tracer
     */
    public APDUTracer getAPDUTracer() {
    	return aPDUTracer;
    }
}
