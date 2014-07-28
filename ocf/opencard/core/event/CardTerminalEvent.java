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


import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.Slot;


/**
 * A <tt>CardTerminalEvent</tt> signals the insertion or removal of a
 * smart card.
 *
 * @author  Dirk Husemann       (hud@zurich.ibm.com)
 * @author  Stephan Breideneich (sbreiden@de.ibm.com)
 * @author  Mike Wendler        (mwendler@de.ibm.com)
 * @version $Id: CardTerminalEvent.java,v 1.2 1999/10/22 16:07:33 damke Exp $
 */

public class CardTerminalEvent extends OpenCardEvent {

  /** a card was inserted */
  public final static int CARD_INSERTED = 0x01;

  /** a card was removed */
  public final static int CARD_REMOVED  = 0x02;

  /** the slot where a card was inserted or removed */
  private int slot_ = 0;


  /**
   * Constructs an object of this class.
   *
   * @param terminal
   *   the object that created this event
   * @param id
   *   one of [<TT>CARD_INSERTED</TT>/<TT>CARD_REMOVED</TT>]
   * @param slot
   *   the slot where a card was inserted or removed
   */
  public CardTerminalEvent (CardTerminal terminal, int id, int slot) {
    super (terminal, id);

    slot_ = slot;
  }


  /**
   * Gets the <tt>CardTerminal</tt> associated with the event.
   *
   * @return the terminal causing this event.
   */
  public CardTerminal getCardTerminal() {
    return (CardTerminal)getSource();
  }


  /**
   * @return the <tt>Slot</tt> object where a card was inserted or removed.
   * @deprecated use getSlotID() instead
   */
  public Slot getSlot() {
    return new Slot((CardTerminal)getSource(), slot_);
  }

  /**
   * @return the <tt>slot</tt> number where a card was inserted or removed.
   */
  public int getSlotID() {
    return slot_;
  }


  /**
   * @return a string representation of this object
   */
  public String toString() {

    StringBuffer sb = new StringBuffer (super.toString () );

    switch(id) {
      case CARD_INSERTED:
        sb.append("\ncard inserted in slot "+slot_);
        break;
      case CARD_REMOVED:
        sb.append ("\ncard removed from slot "+slot_);
        break;
    }
    sb.append ("\nterminal ");
    sb.append (getSource());

    return sb.toString ();
  }
}
