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

/**
 * Determine the characteristics of a card holder verification.
 *
 * @author Peter Trommler      (trp@zurich.ibm.com)
 * @author Thomas Schaeck      (schaeck@de.ibm.com)
 * @author Stephan Breideneich (sbreiden@de.ibm.com)
 * @version $Id: CHVControl.java,v 1.1.1.1 1999/10/05 15:34:31 damke Exp $
 */
public class CHVControl {

  /** private fields */
  private String prompt;
  private String applicationID;
  private String passwordEncoding;
  private int passwordOffset;
  private CardTerminalIOControl ioControl;
  private int chvNumber;

  /**
   * The constructor setting all fields.
   * @param prompt           a string to be displayed to ask the user to enter
   *                         his password
   * @param applicationID    an identification to tell the user which application
   *                         needs CHV
   * @param passwordEncoding identification of the encoder to convert the
   *                         password <tt>String</tt> to a byte array
   * @param passwordOffset   offset of the password within the body of the
   *                         verify APDU
   * @param ioControl        parameters for keyboard input
   */
  public CHVControl(String prompt, 
                    String applicationID, 
                    String passwordEncoding, 
                    int passwordOffset,
                    CardTerminalIOControl ioControl) {

    this.prompt = prompt;
    this.applicationID = applicationID;
    this.passwordEncoding = passwordEncoding;
    this.passwordOffset = passwordOffset;
    this.ioControl = ioControl;
  }

  /**
   * The constructor setting all fields.
   * @param prompt           a string to be displayed to ask the user to enter
   *                         his password
   * @param chvNumber        number of CHV to be requested from the user
   * @param passwordEncoding identification of the encoder to convert the password
   *                         <tt>String</tt> to a byte array
   * @param passwordOffset   offset of the password within the body of the
   *                         verify APDU
   * @param ioControl        parameters for keyboard input
   */
  public CHVControl(String prompt, 
                    int chvNumber, 
                    String passwordEncoding, 
                    int passwordOffset,
                    CardTerminalIOControl ioControl) {

    this.prompt = prompt;
    this.chvNumber = chvNumber;
    this.passwordEncoding = passwordEncoding;
    this.passwordOffset = passwordOffset;
    this.ioControl = ioControl;
  }

  /**
   *
   * @return the prompt string
   */
  public String prompt() {
    return prompt;
  }

  /*****************************************************************************
  * Get the application ID string.<p>
  * The application ID gives the user information on the application
  * he is expected to give card holder verification for.
  * @return the applicateion id string.
  *****************************************************************************/
  public String applicationID() {
    return applicationID;
  }

  /*****************************************************************************
  * Get the chvNumber for which a CHV is required.
  * @return CHV number
  *****************************************************************************/
  public int chvNumber()
  {
    return chvNumber;
  }

  /*****************************************************************************
  * Get the password encoding.<p>
  * The password encoding defines the method to obtain the byte array
  * from the String read from the user. The default is to use the default
  * encoding of the machine.
  * @return password encoding information
  *****************************************************************************/
  public String passwordEncoding() {
    return passwordEncoding;
  }

  /*****************************************************************************
  * Offset of the password in the body of the command APDU
  * @return the offset
  *****************************************************************************/
  public int passwordOffset() {
    return passwordOffset;
  }

  /*****************************************************************************
  * Get the I/O control parameters.
  * @return a reference to a <tt>CardTerminalIOControl</tt> instance
  *****************************************************************************/
  public CardTerminalIOControl ioControl() {
    return ioControl;
  }
}
