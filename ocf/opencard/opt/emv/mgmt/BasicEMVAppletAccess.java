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

package opencard.opt.emv.mgmt;

import java.util.Vector;

import opencard.core.OpenCardConstants;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.SlotChannel;
import opencard.core.util.Tracer;
import opencard.opt.applet.mgmt.AbstractAppletAccessor;
import opencard.opt.applet.mgmt.AppletAccessCardService;
import opencard.opt.applet.mgmt.InvalidAppletInfoException;
import opencard.opt.util.TLV;

/**
 * The <tt>BasicEMVAppletAccess</tt> provides an implementation of an
 * <tt>AppletAccessCardService</tt> for EMV compliant cards supporting
 * a T=1 protocol (T=0 is not implemented).<p>
 * This class is derived from the abstract implementation
 * <tt>AbstractAppletAccessor</tt>, which provides the more generic
 * and card independent functionality.
 * In order to support similar cards, with slightly changed command set,
 * this class be be subclassed in order to get adopted implementations.<p>
 *
 * @author   Thomas Stober (tms@de.ibm.com)
 * @version  $Id: BasicEMVAppletAccess.java,v 1.1 1999/11/23 10:24:17 damke Exp $
 *
 * @see opencard.opt.applet.mgmt.AppletAccessCardService
 * @see opencard.opt.applet.mgmt.AbstractAppletAccessor
 */

public class BasicEMVAppletAccess extends AbstractAppletAccessor
  implements AppletAccessCardService, OpenCardConstants, EMVTags

{
  private Tracer itracer = new Tracer(this, BasicEMVAppletAccess.class);
  private static Tracer ctracer = new Tracer(BasicEMVAppletAccess.class);

	// A default class byte for a SELECT card command
	public final static byte CLASS             = (byte) 0x00;

	// The instruction bytes for card command
	public final static byte SELECT_INS  = (byte) 0xA4;
	public final static byte SELECT_P1   = (byte) 0x00; //Select by FileID
	public final static byte SELECT_P2   = (byte) 0x00; //No Response Data
	public final static byte SELECT_LE   = (byte) 0x00; //
	public final static byte READ_INS    = (byte) 0xB2;
                          // READ_P1     = Record Number
	public final static byte READ_P2     = (byte) 0x04; //Read by Record number
	public final static byte READ_LE     = (byte) 0x00; //

    // address of the directory file on the card
    public static byte[] DIR_PATH  = {(byte) 0x2F, (byte) 0x00};

	// SW1 and SW2 Normal Processing
	public final static int SW_OK                = (int) 0x9000;

	// Warning and Errors Status
	public final static int SW_WRONG_LENGTH      = (int) 0x6700;
	public final static int SW_INCORRECT_INS     = (int) 0x6E00;
	public final static int SW_INCORRECT_CLA     = (int) 0x6D00;
	public final static int SW_INCORRECT_P1P2    = (int) 0x6A86;
	public final static int SW_INCORRECT_LC      = (int) 0x6A87;
	public final static int SW_DATA_CORRUPTED    = (int) 0x6281;
	public final static int SW_INCOMPATIBLE      = (int) 0x6981;
	public final static int SW_NOTSUPPORTED      = (int) 0x6A81;
	public final static int SW_FILENOTFOUND      = (int) 0x6A82;
	public final static int SW_MFCFILENOTFOUND   = (int) 0x9404;
	public final static int SW_RECORDNOTFOUND    = (int) 0x6A83;
    public final static int SW_FILEINVALIDATED   = (int) 0x6283;

	public final static byte  SW1_INCORRECT_P1P2   = (byte) 0x6A;
	public final static byte  SW1_RESPONSE_PENDING = (byte) 0x61;
	public final static byte  SW1_INCORRECT_LE     = (byte) 0x6C;


  /////////////// constructing  ///////////////////////////////////////////

  /**
   * Instantiate a <tt>BasicEMVAppletAccess</tt> object.
   */
  public BasicEMVAppletAccess() throws CardServiceException
  {
    super();
  }


  /**
   * Initializes this CardService and retrieves the Cards directory into
   * a vector.
   *
   * @param scheduler   where to allocate channels
   * @param smartcard   which smartcard to contact
   * @param blocking    whether operation shall be blocking
   *
   * @exception CardServiceException thrown, when Directory could not
   *                    be read
   *
   */
  public void initialize (  CardServiceScheduler scheduler,
                            SmartCard            smartcard,
                            boolean              blocking )
    throws CardServiceException
  {

    super.initialize(scheduler, smartcard, blocking);

    // initial read of applets on card
    setApplets(internalList(scheduler.getSlotChannel()));
  }


  /////////////// system  ///////////////////////////////////////////

  /**
   * Reads the applications info informations from the EMV Directory.<p>
   * Implements the abstract method of the superclass AbstractAppletAccess
   * for EMV compliant cards.<p>
   *
   * @return   An Vector of <tt>EMVAppletInfo</tt> Object
   *           representing the card-resident applets.
   * @exception opencard.core.service.CardServiceException
   *            Thrown when the list cannot be presented.
   */
  protected Vector internalList(SlotChannel channel) throws CardServiceException {

    Vector records = new Vector();  //tempary storage for application infos

    // preparations
    boolean eof=false;
    int i=0;

    try {
      // Select the EF_DIR
      selectDirectory(channel);

      // read the directory
      while (!eof) {

        try {

          // read the next Record from the cards directory
          i++;
          byte[] data = readRecord(channel, i);

          if (data==null){
            // End of Directory
            eof=true;
            itracer.info("list", "End of EF_DIR has been reached!");
          } else {
            // add an Application Info object to the vector
            EMVAppletInfo aAppletInfo = new EMVAppletInfo(new TLV(data));
            records.addElement(aAppletInfo);
          }

        } catch (InvalidAppletInfoException iate) {
          // Invalid Application Entry
          itracer.info("list", "Invalid Application Info was ignored!");
        }

      } //end while

    } catch (CardTerminalException cte) {
      // Invalid Application Entry
      itracer.info("list", "CardTerminalException - abort!");
    }

    // return the result
    return records;

  }



  /**
   * Supporting method to select a applet directory file
  */
  private void selectDirectory(SlotChannel slotChannel)
  throws CardTerminalException  {

    // Assemble Command APDU
    CommandAPDU select_command = new CommandAPDU(5+DIR_PATH.length);
    select_command.append(CLASS);              // CLA
    select_command.append(SELECT_INS);         // INS
    select_command.append(SELECT_P1);          // P1
    select_command.append(SELECT_P2);          // P2
    select_command.append((byte) (DIR_PATH.length)); // Length of file adress of EF_DIR
    select_command.append(DIR_PATH);        // file adress of EF_DIR
    //select_command.append(SELECT_LE);          // LE

    // send command
    ResponseAPDU  select_response = slotChannel.sendAPDU(select_command);

    // Evaluate
    if (select_response.sw()!=SW_OK) {
      itracer.error("list", "error occurred while selecting info information file ("+select_response.sw()+")");
      itracer.error("list", "Command Sent:"+select_command.toString());
      itracer.error("list", "Response returned:"+select_response.toString());
      // Detailed error analysis
      traceResponseCode(select_response);

      throw new CardTerminalException();
    }
  }


  /**
   * Supporting method to read a record from the applet directory file
  */
  private byte[] readRecord(SlotChannel channel, int index)
  throws CardTerminalException {

    // Assemble Command APDU
    CommandAPDU commmandAPDU = new CommandAPDU(5);
    commmandAPDU.append(CLASS);        // CLA --> ISO Class
    commmandAPDU.append(READ_INS);     // INS --> Read Record Command
    commmandAPDU.append((byte) index); // P1  --> record number
    commmandAPDU.append(READ_P2);      // P2  --> read by record number
    commmandAPDU.append(READ_LE);      // Le  --> read entire Record

    // send command
    ResponseAPDU response  = channel.sendAPDU(commmandAPDU);

    // Evaluate
    if  ((response!=null)&& (response.sw()==SW_OK)) {
      // Command execution performed without error
      return response.data();
    } else {
      // what went wrong??
      itracer.debug("list", "Command Sent:"+commmandAPDU.toString());
      itracer.debug("list", "Response returned:"+response.toString());
      if (response.sw()!=SW_OK) {
         itracer.debug("list", "Error reading EF_DIR! ("+response.sw()+")");
         traceResponseCode(response);
      } else {
         itracer.debug("list", "No data returned from EF_DIR!");
      }
      return null;
    }
  }


  /**
   * Supporting method to display trace info, when error codes are
   * returned from the card
  */
  private void traceResponseCode (ResponseAPDU response)
  {
      // Status Word 1+2:
      if ((response.sw()== SW_FILENOTFOUND)||(response.sw()== SW_MFCFILENOTFOUND)) {
         itracer.debug("list", "File not found");
      } else
      if (response.sw()== SW_WRONG_LENGTH) {
         itracer.debug("list", "Wrong length specified");
      } else
      if (response.sw()== SW_INCORRECT_INS) {
         itracer.debug("list", "Invalid Instruction Byte");
      } else
      if (response.sw()== SW_INCORRECT_CLA) {
         itracer.debug("list", "Invalid Class Byte");
      } else
      if (response.sw()== SW_INCORRECT_LC) {
         itracer.debug("list", "Wrong command length specified");
      } else
      if (response.sw()== SW_DATA_CORRUPTED) {
         itracer.debug("list", "Returned data corrupted");
      } else
      if (response.sw()== SW_INCOMPATIBLE) {
         itracer.debug("list", "Record File incompatible with file struccture");
      } else
      if (response.sw()== SW_NOTSUPPORTED) {
         itracer.debug("list", "command not supported");
      } else
      if (response.sw()== SW_FILENOTFOUND) {
         itracer.debug("list", "File not found");
      } else
      if (response.sw()== SW_RECORDNOTFOUND) {
         itracer.debug("list", "Record not found");
      } else
      if (response.sw()== SW_FILEINVALIDATED) {
         itracer.debug("list", "File is invalidated");
      }

      // Status Word 1:
      if (response.sw1()== SW1_INCORRECT_P1P2) {
         itracer.debug("list", "Directory File not found on this card");
      } else
      if (response.sw1()== SW1_RESPONSE_PENDING) {
         itracer.debug("list", "Response from Card is pending");
      } else
      if (response.sw1()== SW1_INCORRECT_LE) {
         itracer.debug("list", "Wrong expected length specified");
      }
  }

}
