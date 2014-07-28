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


import opencard.core.terminal.CardTerminalException;

/** <tt>Lockable</tt> is a generic interface for locking a terminal or
 *  individual slots.
 *
 * @author  Peter Bendel(peter_bendel@de.ibm.com)
 * @version $Id: Lockable.java,v 1.1 1999/10/22 14:43:29 damke Exp $
 *
 * @see opencard.core.terminal.CardTerminal
 */
public interface Lockable {

    /*
    * Locks the entire card terminal, including all slots as well
    * as pin pad and display if available, returns a reference to
    * the new lock owner on success, null on failure.
    * @return a trust ticket for the lock owner
    */
    public Object lock() throws CardTerminalException;

    /*
    * Locks the slot with the given slot number, returns a
    * reference to the new slot owner on success, null on failure.
    * @param slotNr number of the slot to be locked
    * @return a trust ticket for the lock owner
    */
    public Object lockSlot(int slotNr) throws CardTerminalException;

    /*
    * Unlocks the slot with the given slot number.
    * @param slotNr the slot to be unlocked
    * @param handle the trust ticket obtained when locking
    */
    public void unlockSlot(int slotNr, Object handle) throws CardTerminalException;

    /*
    *  Unlocks the entire card terminal, including all slots as
    *  well as pin pad and display if available.
    * @param handle the trust ticket obtained when locking
    */
    public void unlock(Object handle) throws CardTerminalException;
}

