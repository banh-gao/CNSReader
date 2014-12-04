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

package opencard.core.service;


import opencard.core.terminal.CHVControl;
import opencard.core.terminal.CHVEncoder;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalIOControl;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.SlotChannel;
import opencard.core.util.Tracer;

/** The system user interaction handler.<p>
  * This class provides a <em>trusted path</em> to the OpenCard Framework.
  *
  * @author Peter Trommler (trp@zurich.ibm.com)
  * @author Thomas Schaeck (schaeck@de.ibm.com)
  * @version $Id: CardHolderVerificationGUI.java,v 1.2 2005/09/19 10:21:22 asc Exp $
  */
public final class CardHolderVerificationGUI {
  private Tracer itracer = new Tracer(this, CardHolderVerificationGUI.class);

  /** some internal components of the Dialog */
  private int chvNumber = -1;
  private CHVDialog chvDialog = new DefaultCHVDialog();
  protected CardHolderVerificationGUI() {}
  /** Clear the display.<p>
	*/
  protected void clearDisplay() {
	chvNumber = -1;
  }
  /** Display a message.<p>
   *
   * @param   message
   *          The <tt>String</tt> to display.
   */
  protected void display(int num) {
	chvNumber = num;
  }
  /** Return keyboard (i.e., keyboard, PIN pad, etc.) input.<p>
	* @param  ioControl
	*         A <tt>CardTerminalIOControl</tt> object that specifies
	*         how the user input should look like.
	* @return A <tt>String</tt> containing the input.
	*/
  protected String keyboardInput(CardTerminalIOControl ioControl, CHVDialog customCHVDialog)
  {
	if (customCHVDialog != null)
	  return customCHVDialog.getCHV(chvNumber);
	else
	  return chvDialog.getCHV(chvNumber);
  }
  /** Prompt the user for a password, etc.<p>
	*
	* @param  prompt
	*         The message to be displayed.
	* @param  ioControl
	*         A <tt>CardTerminalIOControl</tt> object that specifies
	*         how the user input should look like.
	* @return A <tt>String</tt> containing the input.
	*/
  protected String promptUser(int chvNumber, CardTerminalIOControl ioControl, CHVDialog customCHVDialog) {
	clearDisplay();
	display(chvNumber);
	return keyboardInput(ioControl, customCHVDialog);
  }

  /** @deprecated */
  public ResponseAPDU sendVerifiedAPDU(SlotChannel slotchan,
									   CommandAPDU command,
									   CHVControl  control,
									   CHVDialog   dialog,
									   int         timeout)
	   throws CardTerminalException, CardServiceInvalidCredentialException
  {
    return sendVerifiedAPDU(slotchan,command,control,dialog);
  }

  /**
   * Queries for a PIN and sends it to the smartcard.
   * This method expects a command including a PIN to send to the smartcard.
   * The PIN or password within the command is pre-initialized with padding
   * bytes. This method queries the password from the user, stores it in the
   * command, and sends the resulting command to the smartcard.
   * The smartcard's response is returned.
   * <br>
   * This method is invoked by <tt>CardChannel.sendVerifiedAPDU</tt>
   * only if the card terminal does not take responsibility for querying
   * and filling in the password. If the terminal implements the interface
   * <tt>VerifiedAPDUInterface</tt>, the channel will use the card terminal
   * to do the job.
   *
   * @param slotchan   the physical channel to the smartcard
   * @param command    the APDU to send, password still missing
   * @param control    the parameters needed to query and fill in the password,
   *                   for example the message for querying, and an offset in
   *                    the command APDU
   * @param dialog     a dialog provided by the application,
   *                   or <tt>null</tt> to use a default dialog
   *
   * @exception CardTerminalException
   *            An error occurred while sending the command.
   * @exception CardServiceInvalidCredentialException
   *            The user entered an empty password, or cancelled the
   *            password input. No command has been sent to the smartcard.
   *
   * @see CardChannel#sendVerifiedAPDU
   */
  public ResponseAPDU sendVerifiedAPDU(SlotChannel slotchan,
									   CommandAPDU command,
									   CHVControl  control,
									   CHVDialog   dialog)
	   throws CardTerminalException, CardServiceInvalidCredentialException
  {
	  String password = promptUser(control.chvNumber(), control.ioControl(),
				dialog);
		if (password == null)
			throw new CardServiceInvalidCredentialException("CHV cancelled");
		
		byte[] passbytes = CHVUtils.encodeCHV(control, password);  
	  
	

	// Now, the password has to be copied into the command APDU.
	// The CHV control tells us where
	int length = passbytes.length;
	int offset = control.passwordOffset();

	if (command.getLength() < 5) {
		command.append((byte)length);
		System.arraycopy(passbytes, 0, command.getBuffer(), 5, length);
		command.setLength(5 + length);
	} else {
		for (int i = 0; i < length; i++) {
			command.setByte(5 + offset + i, passbytes[i]);
		}
	}
	
	

	// Now send the command to the card, using the physical channel.
	if (!slotchan.isOpen())
	    throw new CardTerminalException("SlotChannel closed");

	ResponseAPDU response = slotchan.sendAPDU(command);

	return response;

  } // sendVerifiedAPDU
} // class CardHolderVerificationGUI
