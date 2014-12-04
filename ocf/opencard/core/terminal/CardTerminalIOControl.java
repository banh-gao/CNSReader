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

/** The <tt>CardTerminalIOControl</tt> class provides a means of
 * fine-tuning the way a <tt>CardTerminal</tt> processes user input.<p>
 *
 * You can specify
 * <dl>
 *     <dt><b>Maximal number of input characters</b>
 *     <dd>The maximal number of input characters permissible.
 *     <dt><b>Timeout value</b>
 *     <dd>The time to wait for user input (in seconds).
 *     <dt><b>Input character set</b>
 *     <dd>The set of characters to accept for input.
 *     <dt><b>Terminator character set</b>
 *     <dd>The set of characters that terminate the user input.
 *     <dt><b>Input blender</b>
 *     <dd>An object of type <tt>CardTerminalIOBlender</tt> that will process
 *         user input.
 * </dl>
 * Note, that if you provide a <tt>CardTerminalIOBlender</tt> it will override
 * cause these setting to be ignored.<p>
 *
 * @author    Dirk Husemann (hud@zurich.ibm.com)
 * @version   $Id: CardTerminalIOControl.java,v 1.1.1.1 1999/10/05 15:34:31 damke Exp $
 *
 * @see opencard.core.terminal.CardTerminal
 * @see opencard.core.terminal.CardTerminalIOBlender
 */
public class CardTerminalIOControl {

  /** Predefined set of input characters: Numbers only
   */
  public static final String IS_NUMBERS = "0123456789";

  private int maxInputChars = 0;
  private int timeout = 0;
  private String inputSet = null;
  private String terminatorSet = null;
  private CardTerminalIOBlender blender = null;


  /** Constructor.<p>
   *
   * @param    maxInputChars
   *           The maximal number of input characters to accept; if
   *           <tt>0</tt> then assume no limit.
   * @param    timeout
   *           The number of seconds to wait for user input.
   * @param    inputSet
   *           A string specifying what characters to accept as user
   *           input.
   * @param    terminatorSet
   *           A string specifying the characters that terminate the
   *           user input.
   */
  public CardTerminalIOControl(int maxInputChars, int timeout, String inputSet,
			       String terminatorSet) {

    this.maxInputChars = maxInputChars;
    this.timeout = timeout;
    this.inputSet = inputSet;
    this.terminatorSet = terminatorSet;
  }


  /** Constructor using an I/O blender.
   *
   *  @param    blender
   *            An object of type <tt>CardTerminalIOBlender</tt> that
   *            will process the user input.
   */
  public CardTerminalIOControl(CardTerminalIOBlender blender) {

    this.blender = blender;
  }


  /** Return the current <tt>CardTerminalIOBlender</tt>.<p>
   *
   *  @return   The current <tt>CardTerminalIOBlender</tt>.
   */
  public CardTerminalIOBlender blender() {

    return this.blender;
  }


  /** Return the current input character set.<p>
   *
   *  @return   The current input character set.
   */
  public String inputSet() {

    return this.inputSet;
  }


  /** Return the current terminator character set.<p>
   *
   *  @return   The current terminator character set.
   */
  public String terminatorSet() {

    return this.terminatorSet;
  }


  /** Return the current maximal number of input characters.<p>
   *
   *  @return   The current limit.
   */
  public int maxInputChars() {

    return maxInputChars;
  }


  /** Return the current timeout value.<p>
   *
   *  @return   The current timeout.
   */
  public int timeout() {

    return timeout;
  }
}
