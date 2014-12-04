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

/** A <tt>CardTerminalIOBlender</tt> processes (blends) user input for a
  * <tt>CardTerminal</tt>. You can set a <tt>CardTerminalIOBlender</tt>
  * through the <tt>CardTerminalIOControl</tt> parameter of
  * <tt>CardTerminal.keyboardInput()</tt>.<p>
  *
  * @author    Dirk Huseman (hud@zurich.ibm.com)
  * @version   $Id: CardTerminalIOBlender.java,v 1.2 1999/11/03 12:37:16 damke Exp $
  *
  * @see opencard.core.terminal.CardTerminalIOControl
  * @see opencard.core.terminal.CardTerminal
  */
public abstract class CardTerminalIOBlender {
  /** A reference to our containing <tt>CardTerminalIOControl</tt>
   *  object.<p>
   */
  protected CardTerminalIOControl ioControl;

  /** The constructor.<p>
   *
   *  @param ioControl
   *         Control structure to be used by this I/O blender.
   */
  public CardTerminalIOBlender(CardTerminalIOControl ioControl) {
    this.ioControl = ioControl;
  }

  /** Process a single character of user input.<p>
   *
   * @param     character
   *            The input character from the card reader.
   * @return    <tt>-1</tt> if the end of input has been reached;
   *            <tt>0</tt> else.
   */
  public abstract int input(int character);

  /** Return the end product.<p>
   *
   * @return    The resulting string.
   */
  public abstract String result();
}
