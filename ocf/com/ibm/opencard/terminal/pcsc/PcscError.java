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

package com.ibm.opencard.terminal.pcsc;

import java.util.Hashtable;

/** 
 * returns text message from given PCSC error code
 *
 * @author  Stephan Breideneich (sbreiden@de.ibm.com)
 * @version $Id: PcscError.java,v 1.1.1.1 2004/04/08 10:29:27 asc Exp $
 */
public class PcscError {

  private static Hashtable errorTable = new Hashtable();

  /** initialize the error table */
  static {
    errorTable.put(new Integer(0x80100001), 
                   "An internal consistency check failed.");
    errorTable.put(new Integer(0x80100002),
                   "The action was cancelled by an SCardCancel request.");
    errorTable.put(new Integer(0x80100003),
                   "The supplied handle was invalid.");
    errorTable.put(new Integer(0x80100004),
                   "One or more of the supplied parameters could not " 
                   + "be properly interpreted.");
    errorTable.put(new Integer(0x80100005),
                   "Registry startup information is missing or invalid.");
    errorTable.put(new Integer(0x80100006),
                   "Not enough memory available to complete this command.");
    errorTable.put(new Integer(0x80100007),
                   "An internal consistency timer has expired.");
    errorTable.put(new Integer(0x80100008),
                   "The data buffer to receive returned data is too " 
                   + "small for the returned data");
    errorTable.put(new Integer(0x80100009),
                   "The specified reader name is not recognized.");
    errorTable.put(new Integer(0x8010000A),
                   "The user-specified timeout value has expired.");
    errorTable.put(new Integer(0x8010000B),
                   "The smart card cannot be accessed because of other "
                   + "connections outstanding");
    errorTable.put(new Integer(0x8010000C),
                   "The operation requires a Smart Card, but no " 
                   + "Smart Card is currently in the device");
    errorTable.put(new Integer(0x8010000D),
                   "The specified smart card name is not recognized.");
    errorTable.put(new Integer(0x8010000E),
                   "The system could not dispose of the media "
                   + "in the requested manner.");
    errorTable.put(new Integer(0x8010000F),
                   "The requested protocols are incompatible with the "
                   + "protocol currently in use with the smart card.");
    errorTable.put(new Integer(0x80100010),
                   "The reader or smart card is not ready to accept commands.");
    errorTable.put(new Integer(0x80100011),
                   "One or more of the supplied parameters values could "
                   + "not be properly interpreted");
    errorTable.put(new Integer(0x80100012),
                   "The action was cancelled by the system, presumably to "
                   + "log off or shut down.");
    errorTable.put(new Integer(0x80100013),
                   "An internal communications error has been detected.");
    errorTable.put(new Integer(0x80100014),
                   "An internal error has been detected, "
                   + "but the source is unknown.");
    errorTable.put(new Integer(0x80100015),
                   "An ATR obtained from the registry "
                   + "is not a valid ATR string.");
    errorTable.put(new Integer(0x80100016),
                   "An attempt was made to end a non-existent transaction.");
    errorTable.put(new Integer(0x80100017),
                   "The specified reader is not currently available for use.");
    errorTable.put(new Integer(0x80100018),
                   "Internal flag to force server termination.");
    errorTable.put(new Integer(0x80100019),
                   "The PCI Receive buffer was too small.");
    errorTable.put(new Integer(0x8010001A),
                   "The reader driver does not meet "
                   + "minimal requirements for support.");
    errorTable.put(new Integer(0x8010001B),
                   "The reader driver did not produce a unique reader name.");
    errorTable.put(new Integer(0x8010001C),
                   "The smart card does not meet "
                   + "minimal requirements for support.");
    errorTable.put(new Integer(0x8010001D),
                   "The Smart card resource manager is not running.");
    errorTable.put(new Integer(0x8010001E),
                   "The Smart card resource manager has shut down.");
    errorTable.put(new Integer(0x80100065),
                   "The reader cannot communicate with the smart card, "
                   + "due to ATR configuration conflicts.");
    errorTable.put(new Integer(0x80100066),
                   "The smart card is not responding to a reset.");
    errorTable.put(new Integer(0x80100067),
                   "Power has been removed from the smart card, "
                   + "so that further communication is not possible.");
    errorTable.put(new Integer(0x80100068),
                   "The smart card has been reset, "
                   + "so any shared state information is invalid.");
    errorTable.put(new Integer(0x80100069),
                   "The smart card has been removed, so that further "
                   + "communication is not possible.");
  }

  /** returns the associated message to the error code.
   */
  public static String getMessage(int errorCode) {
    return (String)errorTable.get(new Integer(errorCode));
  }
}
