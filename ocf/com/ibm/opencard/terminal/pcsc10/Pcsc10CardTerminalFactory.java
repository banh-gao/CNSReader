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
import opencard.core.terminal.CardTerminalFactory;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.util.Tracer;

/** <tt>CardTerminalFactory</tt> for PCSC card terminals.
  *
  * @author  Stephan Breideneich (sbreiden@de.ibm.com)
  * @version $Id: Pcsc10CardTerminalFactory.java,v 1.2 2005/09/19 10:21:22 asc Exp $
  *
  * @see opencard.core.terminal.CardTerminalFactory
  */
public class Pcsc10CardTerminalFactory implements CardTerminalFactory {
  private Tracer itracer = new Tracer(this, Pcsc10CardTerminalFactory.class);

  /** The reference to the PCSC ResourceManager for this card terminal. */
  private OCFPCSC1 pcsc = null;

  /** Instantiate and initialize an <tt>Pcsc10TerminalFactory</tt>.
   *
   * @exception CardTerminalException
   *                    Thrown when a problem occured.
   */
  public Pcsc10CardTerminalFactory()
    throws CardTerminalException {
    super();
    open();
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

	String terminalType = "PCSC10";
	
	if (terminalInfo.length >= 2) {
		terminalType = terminalInfo[1];
	}
	
    // add the terminals found in the PCSC ResourceManager
    String[] terminals = ListReaders();
    if (terminals != null) {
        for (int i=0;i<terminals.length;i++) {
            ctr.add(new Pcsc10CardTerminal(terminals[i], terminalType, ""));
        }
    }
  }

  /** initialize the factory (setup the PC/SC-driver) */
  public void open() 
    throws CardTerminalException {

    // factory already opened?
    if (pcsc == null) {
      try {
        itracer.debug("Pcsc10CardTerminalFactory", "connect to PCSC 1.0 resource manager");
        OCFPCSC1.loadLib();
        pcsc = new OCFPCSC1();
        itracer.debug("Pcsc10CardTerminalFactory", "Driver initialized");
      } catch (PcscException e) {
        throw new CardTerminalException("Pcsc10CardTerminalFactory: " + e.getMessage());
      }
    }
  }

  /** deinitialize the PC/SC-driver */
  public void close() 
    throws CardTerminalException {

    pcsc = null;
  }

  /** get the actual PC/SC reader list
   *
   *  @exception opencard.core.terminal.CardTerminalException
   *             thrown when error occured
   */
  protected String[] ListReaders() throws CardTerminalException
  {
    itracer.debug("Pcsc10CardTerminalFactory", "get reader list from PC/SC");
    String[] terminals = null;
    try {
      terminals = pcsc.SCardListReaders(null);
    } catch (PcscException e) {
      throw new CardTerminalException("Pcsc10CardTerminalFactory: " + e.getMessage());
    }
    return terminals;
  }

}

// $Log: Pcsc10CardTerminalFactory.java,v $
// Revision 1.2  2005/09/19 10:21:22  asc
// Reorganized imports and removed warning "class is never used"
//
// Revision 1.1.1.1  2004/04/08 10:29:27  asc
// Import into CardContact CVS
//
// Revision 1.13  1999/10/22 07:31:14  pbendel
// RFC 17-1 Terminal locking mechanism using lock handle
//
// Revision 1.12  1999/04/01 13:11:28  pbendel
// native browser support RFC-0005 (pbendel)
//
// Revision 1.11  1998/08/13 14:39:12  cvsusers
// *** empty log message ***
//
// Revision 1.10  1998/07/09 12:35:40  breid
// PTR117: parameter specification added
//
// Revision 1.9  1998/04/30 14:49:36  breid
// open() modified: now open() creates pcsc-object only at the first call
//
// Revision 1.8  1998/04/22 20:08:30  breid
// support for T0 implemented
//
// Revision 1.7  1998/04/14 16:17:04  breid
// createTerminal() removed
//
// Revision 1.6  1998/04/09 13:40:17  breid
// AutoConfigurationFeature added
//
// Revision 1.5  1998/04/08 17:45:42  breid
// *** empty log message ***
//
// Revision 1.4  1998/04/08 17:21:58  breid
// exceptions modified
//
// Revision 1.3  1998/04/08 16:28:41  breid
// createCardTerminals(), open(), close() added
//
// Revision 1.2  1998/04/08 12:02:15  breid
// tested with StockBroker
//
// Revision 1.1  1998/04/07 12:43:05  breid
// initial version
//

