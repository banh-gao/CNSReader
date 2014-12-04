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

import java.util.Hashtable;

import opencard.core.service.CardChannel;
import opencard.core.service.CardService;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CHVControl;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.Tracer;

/**
 * This service offers methods to applications or proxies derived
 * from it, which send a given command APDU to the card applet
 * identified by the given application identifier and which return the
 * result APDU. All instances associated with the same physical card
 * share a common state object attached to the the used channel by the
 * first <tt>BasicAppletCardService</tt> instance. This state object,
 * an instance of <tt>CardState</tt>, keeps track of the currently
 * selected applet and is used by <tt>BasicAppletCardService</tt>s to
 * avoid unnecessary selection of applets.
 *
 * @author  Frank Seliger (seliger@de.ibm.com)
 * @author  Thomas Schaeck (schaeck@de.ibm.com)
 * @author  Christophe.Muller@research.gemplus.com
 * @version $Id: BasicAppletCardService.java,v 1.3 2000/01/14 09:32:27 damke Exp $
 * @since   OCF1.2
 *
 * @see opencard.core.service.CardServiceScheduler
 * @see opencard.core.service.CardChannel
 * @see opencard.opt.applet.AppletID
 */
public class BasicAppletCardService extends CardService {
	/**
	 * A manager that implements at least "AppletSelector"
	 * in order to perform the low-level applet selection.
	 */
	private AppletSelector selector_ = new ISOAppletSelector();

	private static Tracer ctracer = new Tracer(BasicAppletCardService.class);

	/**
	 * This is a pseudo AID for the CardState.
	 * Normal applets can not have the same AID
	 * accidentially, because real AIDs consist of
	 * at least 5 bytes.
	 */
	private final static AppletID CARD_STATE_AID = new AppletID("SAID".getBytes());
/** Get the applet selector.<p>
 *
 * @return A helper object that implements the "AppletSelector" interface.
 */
protected AppletSelector getAppletSelector() {
	return selector_;
}
/**
 * Gets the associated state object. We store the
 * <tt>CardState</tt> in the same way as the state of an applet
 * proxy or applet. This has the advantage that the state is
 * guaranteed to exist only once per card channel and thus per
 * card.
 *
 * @return The state object representing the state of the card. 
 */
protected CardState getCardState(CardChannel channel) {
	return (CardState) ((Hashtable) channel.getState()).get(CARD_STATE_AID);
}
/**
 * Instantiates a <tt>BasicAppletCardService</tt> and tie it both
 * to its <tt>CardServiceScheduler</tt> and its using
 * <tt>SmartCard</tt> object.
 *
 * @param scheduler The scheduler of this <tt>CardExecutiveCardService</tt>.
 * @param card      The controlling </tt>SmartCard</tt> object.
 * @param blocking  Specify the wait behavior for obtaining a <tt>CardChannel</tt> 
 *                  from the <tt>CardServiceScheduler</tt>.
 */
protected void initialize(CardServiceScheduler scheduler, SmartCard card, boolean blocking) throws CardServiceException {
	super.initialize(scheduler, card, blocking);
	ctracer.debug("<init>", "(" + scheduler + "," + card + "," + blocking + ")");
	try {
		allocateCardChannel();
		//System.out.println("BasicAppletCardService - allocated CardChannel()");
		CardChannel channel = getCardChannel();
		Hashtable channelState = (Hashtable) channel.getState();
		if (channelState == null) {
			channelState = new Hashtable();
			channel.setState(channelState);
		}
		if (channelState.get(CARD_STATE_AID) == null) {
			channelState.put(CARD_STATE_AID, new CardState());
		}
	} finally {
		releaseCardChannel();
	}
}
/**
 * Selects the card applet with the given application ID using the
 * given channel for communication with the card.
 *
 * @param channel   The <tt>CardChannel</tt> to be used for sending the select
 *                  command to the card.
 * @param appletID The application identifier of the applet to be selected.
 *
 */
private void selectApplet(CardChannel channel, AppletID appletID) throws CardServiceException, CardTerminalException {
	CardState state = getCardState(channel);
	if ((state.getSelectedAppletID() == null) || (!state.getSelectedAppletID().equals(appletID))) {
		ctracer.debug("selectApplet", "selecting " + appletID);
		AppletInfo info = selector_.selectApplet(channel, appletID);

		// Check return status
		ResponseAPDU response = (ResponseAPDU) info.getData();
		AppletID previouslySelectedAID = state.setSelectedAppletID(appletID);

		// Notify the state associated with AID prev...
		if (previouslySelectedAID != null) {
			Hashtable channelState = (Hashtable) getCardChannel().getState();
			ctracer.debug("selectApplet", "previouslySelectedAID = " + previouslySelectedAID);
			ctracer.debug("selectApplet", "channelState = " + channelState);
			AppletState appState = (AppletState) (channelState.get(previouslySelectedAID));
			appState.appletDeselected();
		}
	} else {
		// Was already selected, we did nothing.
	}
}
/**
 * Sends a <tt>CommandAPDU</tt> to the applet on the card that has
 * the given application identifier using the given channel.
 *
 * @param     channel      channel to be used for sending APDUs to the smart card
 * @param     appletID    application identifier of destination applet
 * @param     commandAPDU  <tt>CommandAPDU</tt> to send
 *
 * @return    The resulting <tt>ResponseAPDU</tt> as
 *            received from the card.
 */
protected ResponseAPDU sendCommandAPDU(CardChannel channel, AppletID appletID, CommandAPDU commandAPDU) throws CardTerminalException, CardServiceException {
	ctracer.debug("sendCommandAPDU(channel,...)", "sending " + commandAPDU + " to <" + appletID + ">");
	selectApplet(channel, appletID);
	return channel.sendCommandAPDU(commandAPDU);
}
/**
 * Sends a <tt>CommandAPDU</tt> to the applet on the card that has
 * the given application identifier.
 *
 * @param appletID    application identifier of destination applet
 * @param commandAPDU  <tt>CommandAPDU</tt> to send
 *
 * @return The resulting <tt>ResponseAPDU</tt> as received from the card.
 */
public ResponseAPDU sendCommandAPDU(AppletID appletID, CommandAPDU commandAPDU) throws CardTerminalException, CardServiceException {
	ctracer.debug("sendCommandAPDU(...)", "sending " + commandAPDU + " to " + appletID);
	try {
		allocateCardChannel();
		return sendCommandAPDU(getCardChannel(), appletID, commandAPDU);
	} finally {
		releaseCardChannel();
	}
}
/**
 * Send a verify CHV command APDU to the card applet with the
 * given application identifier after filling in the password
 * obtained from the CHV dialog currently associated with this
 * card service.
 *
 * @param channel          The <tt>CardChannel</tt> to be used for
 * sending the command APDU.
 * @param appletID         The application identifier of the applet
 * to which the verification APDU shall be sent.
 * @param verificationAPDU The command APDU for password verification
 * into which the password shall be inserted.
 * @param chvControl       The CHV control to be used for password input.
 * @param timeout          The timeout to be used.
 *
 * @return The response APDU returned by the card as response
 * to the verify password command.
 */
protected ResponseAPDU sendVerifiedAPDU(CardChannel channel, AppletID appletID, CommandAPDU verificationAPDU, CHVControl chvControl, int timeout) throws CardServiceException, CardTerminalException {
	// If necessary, select the applet first
	selectApplet(channel, appletID);
	return channel.sendVerifiedAPDU(verificationAPDU, chvControl, getCHVDialog(), -1);
}
	/** Set the applet selector.<p>
	 *  Subclasses may need to redefine the way applet selection
	 *  is performed. They can do that by specifying a class implementing
	 *  "AppletSelector" and call setAppletSelector with a new helper
	 *  object that will be used in place of the default ISO implementation.
	 */
	protected void setAppletSelector(AppletSelector s) {
	selector_ = s;
	}
}
