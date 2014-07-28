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

import opencard.core.terminal.CardTerminalIOControl;

/** User interaction takes place through the <tt>display()</tt> method (to
 * display information to the user), the <tt>keyboardInput()</tt> method
 * (to retrieve information from the user) and the <tt>promptUser()</tt>
 * method to combine display and input in a single call.<p>
 *
 * @author  Dirk Husemann (hud@zurich.ibm.com)
 * @version $Id: UserInteraction.java,v 1.2 1999/11/03 12:37:19 damke Exp $
 *
 * @see opencard.core.terminal.CardTerminal
 * @see opencard.core.terminal.CardTerminalIOControl
 */
public interface UserInteraction {
  /** Display a message.<p>
   *
   * @param   message
   *	      The <tt>String</tt> to display.
   */
  public void display(String message);

  /** Clear the display.<p>
    */
  public void clearDisplay();

  /** Return keyboard (i.e., keyboard, PIN pad, etc.) input.<p>
    *
    * @param  ioControl
    *	      A <tt>CardTerminalIOControl</tt> object that specifies
    *	      how the user input should look like.
    * @return A <tt>String</tt> containing the input.
    */
  public String keyboardInput(CardTerminalIOControl ioControl);

  /** Prompt the user for a password, etc.<p>
    * This method displays a message and reads a password from
    * the user. The implementation of this method depends on the
    * ioBlender implementation of the terminal. E.g. a PIN input
    * could be implemented by the terminals numerical keyboard
    * whereas an alphanumeric password might be implemented through
    * the <tt>UserInteractionHandler</tt>.
    *
    * @param  prompt
    *	      The message to be displayed.
    * @param  ioControl
    *	      A <tt>CardTerminalIOControl</tt> object that specifies
    *	      how the user input should look like.
    * @return A <tt>String</tt> containing the input.
    */
  public String promptUser(String prompt, CardTerminalIOControl ioControl);
}
