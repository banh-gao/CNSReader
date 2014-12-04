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

package opencard.opt.util;


import opencard.core.service.CardService;
import opencard.core.service.CardServiceInvalidParameterException;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;


/**
 * A card service for low level communication with smartcards.
 * It allows to send application-provided command APDUs to the card and
 * returns the response APDUs. The factory that creates this service is
 * <tt>PassThruCardServiceFactory</tt>.
 * <br>
 * This class serves as an example how a card service can be implemented.
 * However, it contradicts the idea of card services. The responsibility
 * for composing the APDUs sent to the card and interpreting the responses
 * lies with the card services, not with the application.
 * Since most people start getting familiar with smartcards by building
 * APDUs themselves, this service is provided here as a "quick starter"
 * for some test programs. Nevertheless, it's use is strongly discouraged
 * when implementing real applications.
 * <br>
 * OpenCard applications should <b>never</b> build APDUs, since APDUs are
 * card specific. The same applies to interpreting the responses received
 * from the smartcard. Instead, they should use high-level interfaces that
 * can be implemented for different cards. These interfaces can be standard
 * interfaces, like <tt>FileAccessCardService</tt> for ISO compatible, file
 * system based smartcards, or they can be defined by an application.
 *
 * <p>
 * When designing an application that supports smartcards, there will
 * typically be a need for two kinds of card specific code. First, the
 * ATR sent by a smartcard has to be interpreted to make sure that the
 * card inserted is supported by the application. An OCF application
 * should put the respective code into a card service factory, or use an
 * existing factory that knows the ATR(s).
 * <br>
 * The second part of the card specific code will build command APDUs and
 * interpret responses to these commands. This part of the code can provide
 * rather general operations, like <i>select a file</i> or <i>read some
 * data from a file</i>. In this case, a standard interface should be used.
 * On the other hand, this part of the code can also provide specialized
 * functionality, like <i>read the serial number</i> or <i>read the card
 * holder's name</i>. In this case, the application should define it's own
 * interface, and a card service implementing this interface has to be
 * developed.
 * <br>
 * Later on, if another card with different APDUs and responses has to be
 * supported by the same application, just a service for that new card has
 * to be developed, implementing the same interface as the first one. The
 * factory used to create the first service will be extended, so it can
 * decide which service to use, depending on a smartcard's ATR. The
 * application itself does not have to be changed at all.
 *
 *
 * @author Thomas Schaeck (schaeck@de.ibm.com)
 * @author Roland Weber  (rolweber@de.ibm.com)
 *
 * @version $Id: PassThruCardService.java,v 1.1.1.1 1999/10/05 15:08:48 damke Exp $
 *
 * @see PassThruCardServiceFactory
 * @see opencard.opt.iso.fs.FileAccessCardService
 */
public class PassThruCardService extends CardService
{
  /**
   * Creates a new low level card service that is not yet initialized.
   * Initialization is done by invoking <tt>initialize</tt> in the base class.
   *
   * @see opencard.core.service.CardService#initialize(CardServiceScheduler, SmartCard, boolean )
   */
  public PassThruCardService()
  {
    super();
  }


  /**
   * Sends a <tt>CommandAPDU</tt> to the smart card.
   * @param     command    The <tt>CommandAPDU</tt> to send.
   * @return    The resulting <tt>ResponseAPDU</tt> as
   *            received from the card.
   *
   * @exception CardTerminalException
   *            if the terminal encountered an error while
   *            trying to communicate with the smartcard
   */
  public ResponseAPDU sendCommandAPDU(CommandAPDU command)
       throws CardTerminalException
  {
    // first, some static checking is done on the parameters
    if (command == null)
      throw new CardServiceInvalidParameterException("command = null");

    // this variable is needed to store the return value
    ResponseAPDU response = null;

    // Here is the actual implementation of this service method:
    // - allocate a channel to the smartcard
    // - prepare the APDU to be sent (of course not in this trivial service)
    // - send the command and receive the response, using the allocated channel
    // - evaluate the response (of course not in this trivial service)
    // - release the allocated channel, even if an error occurred
    // Optionally, the preparation of the APDU can be done before allocating
    // the channel. Likewise, postprocessing of the response can be delayed
    // until the channel has been freed.
    try {
      allocateCardChannel();

      response = getCardChannel().sendCommandAPDU(command);

    } finally {
      releaseCardChannel();
    }

    return response;

  } // sendCommandAPDU


} // class PassThruCardService

