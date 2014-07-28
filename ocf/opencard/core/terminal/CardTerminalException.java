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


import opencard.core.OpenCardException;


/**
 * Through a <tt>CardTerminalException</tt> we signal all exceptions
 * upstream.
 *
 * @author  Dirk Husemann       (hud@zurich.ibm.com)
 * @author  Mike Wendler        (mwendler@de.ibm.com)
 * @author  Stephan Breideneich (sbreiden@de.ibm.com)
 * @version $Id: CardTerminalException.java,v 1.2 1999/10/22 16:07:34 damke Exp $
 */

public class CardTerminalException extends OpenCardException {

  private CardTerminal terminal = null;
  private int          slot     = 0;


  /**
   * The empty constructor.
   */
  public CardTerminalException () {

    super ();
  }


  /**
   * Constructs an object of this class.
   *
   * @param     s
   *            message telling a bit more about the cause of this exception
   */
  public CardTerminalException (String s) {

    super (s);
  }


  /**
   * Constructs an object of this class. Information about the terminal
   * where this exception originated is given.
   *
   * @param     s
   *            A message telling a bit more about the cause of
   *            this exception
   * @param     aTerminal
   *            The <tt>CardTerminal</tt> where the exception
   *            originated.
   */
  public CardTerminalException (String s, CardTerminal aTerminal) {
    super (s);

    terminal = aTerminal;
  }


  /**
   * @deprecated use CardTerminalException(String, CardTerminal, int)
   */
  public CardTerminalException (String s, CardTerminal aTerminal, Slot aSlot) {
    super (s);

    terminal = aTerminal;
    slot = aSlot.getSlotID();
  }

  /**
   * Constructs an object of this class. Information about the terminal
   * and slot where this exception originated is given.
   *
   * @param     s
   *            A message telling a bit more about the cause of
   *            this exception
   * @param     aTerminal
   *            The <tt>CardTerminal</tt> where the exception originated.
   * @param     slotID
   *            The <tt>slot number</tt> where the exception originated.
   */
  public CardTerminalException (String s, CardTerminal aTerminal, int slotID) {
    super (s);

    terminal = aTerminal;
    slot = slotID;
  }


  /**
   * Gets the <tt>CardTerminal</tt> object where this exception occurred.
   *
   * @return    The reference to the <tt>CardTerminal</tt>.
   */
  public CardTerminal getCardTerminal() {
    return (terminal);
  }


  /**
   * Gets the <tt>Slot</tt> object where this exception occurred.
   *
   * @return    The reference to the <tt>Slot</tt>.
   */
  public int getSlot() {
    return slot;
  }

}
