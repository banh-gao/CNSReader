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

package com.ibm.opencard.terminal.pcsc10;

import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.util.Tracer;

/** <tt>CardTerminalFactory</tt> for lockable PCSC card terminals.
  *
  * @author  Stephan Breideneich (sbreiden@de.ibm.com)
  * @version $Id: Pcsc10LockableTerminalFactory.java,v 1.2 2005/09/19 10:21:22 asc Exp $
  *
  * @see opencard.core.terminal.CardTerminalFactory
  */
public class Pcsc10LockableTerminalFactory extends Pcsc10CardTerminalFactory {
  private Tracer itracer = new Tracer(this, Pcsc10LockableTerminalFactory.class);


  /** Instantiate and initialize an <tt>Pcsc10LockableTerminalFactory</tt>.
   *
   * @exception CardTerminalException
   *                    Thrown when a problem occured.
   */
  public Pcsc10LockableTerminalFactory()
    throws CardTerminalException {
    super();
  }

  /** create a specific <tt>CardTerminal</tt> object that knows how to handle
   *  a specific card terminal and register it to the CardTerminalRegistry.
   *  @param     ctr
   *		 the CardTerminalRegistry for registration-process
   *  @param     terminalInfo
   *             null - not needed for this factory
   *  @see       opencard.core.terminal.CardTerminalFactory
   */
  public void createCardTerminals(CardTerminalRegistry ctr, String[] terminalInfo)
    throws CardTerminalException {

    // add the terminals found in the PCSC ResourceManager
    String[] terminals = ListReaders();
    for (int i=0;i<terminals.length;i++) {
      ctr.add(new Pcsc10LockableTerminal(terminals[i], "PCSC10", ""));
    }
  }

}

