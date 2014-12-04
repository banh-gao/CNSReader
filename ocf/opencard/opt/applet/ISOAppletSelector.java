package opencard.opt.applet;

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

import opencard.core.service.CardChannel;
import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.Tracer;
import opencard.opt.service.CardServiceUnexpectedResponseException;

/**
 * The <tt>ISOAppletSelector</tt> is a class providing the 
 * <tt>selectApplet</tt> functionality as specified in
 * the <tt>AppletSelector</tt> interface. It uses the SELECT FILE
 * command as requested by ISO7816-5 and standardized by ISO7816-4 norms.
 * It is a helper class, not a regular CardService, as it will always
 * use a CardChannel provided by client CardServices.
 *
 * @author   Christophe.Muller@research.gemplus.com
 * @version  $Id: ISOAppletSelector.java,v 1.5 2000/01/14 09:32:27 damke Exp $
 * @since    OCF1.2
 *
 * @see opencard.opt.applet.AppletSelector
 */

public class ISOAppletSelector
	implements AppletSelector {

	// A default class byte for a SELECT card command
	protected final static byte SELECT_CLASS             = (byte) 0x00;

	// The instruction byte for a SELECT card command
	protected final static byte SELECT_INS               = (byte) 0xA4;

	// P1 = 04 (Selection by DF name)
	protected final static byte SELECT_P1                = (byte) 0x04;

	// P2 = 00 (First occurence)
	protected final static byte SELECT_P2                = (byte) 0x00;

	// SW1 and SW2 Normal Processing
	protected final static short SW_OK                   = (short) 0x9000;

	// Warning and Errors Status 
	protected final static byte SELECT_SW1_WARNING       = (byte) 0x62;
	protected final static byte SELECT_SW1_ERROR         = (byte) 0x6A;
	protected final static byte SELECT_SW2_INVALID       = (byte) 0x83;
	protected final static byte SELECT_SW2_UNFORMAT      = (byte) 0x84;
	protected final static byte SELECT_SW2_UNSUPPORTED   = (byte) 0x81;
	protected final static byte SELECT_SW2_NOTFOUND      = (byte) 0x82;
	protected final static byte SELECT_SW2_INCORRECTP1P2 = (byte) 0x86;
	protected final static byte SELECT_SW2_INCORRECTLC   = (byte) 0x87;

	/** Maximum APDU size allowed by card */
	protected final static int MAX_APDU_SIZE = 192;

	private static Tracer ctracer = new Tracer(ISOAppletSelector.class);



/**
 * Instantiate a <tt>ISOAppletSelector</tt> object.
 */
public ISOAppletSelector() {
	super();
}
/**
 * Selects a Card Applet with the specified Application Identifier
 * in the Card.<p> Sends the ISO 7816-5 <code>SELECT</code>
 * card command (equal to the ISO 7816-4 <code>SELECT FILE</code>
 * command). If this command succeeds (response=OK), returns an
 * AppletInfo with the applet AID and the obtained ResponseAPDU
 * stored in the "data" field.
 *
 * @param channel the card channel to be used during this operation.
 * @param appletID the card applet application identifier to select.
 */
public synchronized AppletInfo selectApplet(CardChannel channel, AppletID appletID) throws CardTerminalException, CardServiceException {
	CommandAPDU selectCommand = null;
	ResponseAPDU selectResponse = null;
	AppletInfo result = null;

	// if no channel was given, throw an exception
	if (channel == null) {
		throw new CardServiceException("selectApplet: No CardChannel!");
	}

	// Create and send command
	ctracer.debug("selectApplet", "selecting " + appletID);
	selectCommand = new CommandAPDU(6 + appletID.getBytes().length);
	selectCommand.setLength(0);
	selectCommand.append(SELECT_CLASS); //CLA
	selectCommand.append(SELECT_INS); //INS
	selectCommand.append(SELECT_P1); //P1
	selectCommand.append(SELECT_P2); //P2
	selectCommand.append((byte) appletID.getBytes().length); //Length In
	selectCommand.append(appletID.getBytes()); //Input Data
	selectResponse = channel.sendCommandAPDU(selectCommand);
	ctracer.debug("selectApplet", "Selection response sw = 0x" + Integer.toHexString(selectResponse.sw()));

	// Check result
	switch ((short) (selectResponse.sw() & 0xFFFF)) {
		case SW_OK :
			// Response is OK
			result = new AppletInfo();
			result.setAppletID(appletID);
			result.setData(selectResponse);
			break;
		default :
			// 1st) solution => Problem during selection: Throw an exception
			// Cons: we loose the ResponseAPDU object..!
			// throw new CardServiceUnexpectedResponseException("Status Words are 0x" + Integer.toHexString((short) (selectResponse.sw() & 0xFFFF)));
			// 2nd) solution => we provide a result object w/ an impossible
			// AID and the entire ResponseAPDU object. Caller can test
			// either the AID, or the ResponseAPDU SW code. This also
			// alows to parse more carefully the error codes in subclasses.
			// 
			// result = new AppletInfo();
			// result.setAppletID(new AppletID("BAD".getBytes()));
			// result.setData(selectResponse);
			// break;
			throw new CardServiceUnexpectedResponseException("Status Words are 0x" + Integer.toHexString((short) (selectResponse.sw() & 0xFFFF)));
	}
	return result;
}
}
