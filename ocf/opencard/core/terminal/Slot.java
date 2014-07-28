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


import opencard.core.OpenCardRuntimeException;
import opencard.core.util.Tracer;

/** A <tt>Slot</tt> object represents an individual slot of a
 * card terminal.<p>
 *
 * @author   Peter Trommler      (trp@zurich.ibm.com)
 * @author   Stephan Breideneich (sbreiden@de.ibm.com)
 * @version  $Id: Slot.java,v 1.3 1999/11/03 12:37:17 damke Exp $
 *
 * @see opencard.core.terminal.CardTerminal
 * @deprecated use slot number instead
 */
public final class Slot {
  private Tracer itracer = new Tracer(this, Slot.class);

  /** The "owning" <tt>CardTerminal</tt>.
   */
  protected CardTerminal terminal;

  /** The slot identifier.
   */
  protected int slotID;

  /** Instantiate a <tt>Slot</tt> object belonging to the <tt>CardTerminal</tt>
   * object <tt>terminal</tt>.
   *
   * @param     terminal
   *            The card terminal to which this <tt>Slot</tt> belongs to
   * @param     slotID
   *            Slot number (0 = first slotID)
   */
  public Slot(CardTerminal terminal, int slotID) {

    if (terminal == null)
      throw new OpenCardRuntimeException("terminal is null");

    if (slotID < 0)
      throw new OpenCardRuntimeException("slotID < 0");

    this.terminal = terminal;
    this.slotID = slotID;
  }

  /** Check whether there is a smart card present.
   *
   * @return    True if there is a smart card inserted in the slot.
   * @exception CardTerminalException
   *            Thrown when terminal.isCardPresent failed.
   */
  public boolean isCardPresent()
    throws CardTerminalException {

    return this.terminal.isCardPresent(slotID);
  }

  /**
    * @deprecated
    */
  public CardID getCardID(int timeout) throws CardTerminalException {
      return this.terminal.getCardID(slotID);
  }

  /** Return the <tt>CardID</tt> object of the presently inserted smart card. In the case
    * this slot is in use already (i.e., there is already a <tt>CardService</tt> attached
    * to this slot), this method returns a <i>cached</i> <tt>CardID</tt> immediately.<p>
    *
    * @return    The <tt>CardID</tt> object for the currently inserted smart card.
    * @exception opencard.core.terminal.CardTerminalException
    *            Thrown when problems in the card terminal occur.
    */
  public CardID getCardID() throws CardTerminalException {
      return this.terminal.getCardID(slotID);
  }

  /** Return a reference to the "owning" <tt>CardTerminal</tt> object.
   *
   * @return    The reference to the owning <tt>CardTerminal</tt> object.
   */
  public CardTerminal getCardTerminal() {
    return terminal;
  }

  /** Return the Slot number.
    *
    * @return the slot number.
    */
  public int getSlotID() {
    return this.slotID;
  }
}
