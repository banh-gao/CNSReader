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
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CHVControl;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.Tracer;

/**
 * <tt>AppletProxy</tt> is the base class for all applet proxies. This class is
 * derived from the class <tt>BasicAppletCardService</tt>. It adds an attribute
 * that holds the application identifier of the applet to which the applet
 * proxy is associated. It provides methods similar to those of the base class,
 * except that the application identifier parameter is not necessary because
 * every <tt>AppletProxy</tt> knows the AID of the applet associated with it.
 *
 * @author  Thomas Schaeck (schaeck@de.ibm.com)
 * @version $Id: AppletProxy.java,v 1.3 2000/01/14 09:32:26 damke Exp $
 * @since   OCF1.2
 */
public abstract class AppletProxy extends BasicAppletCardService {

	/** Application Identifier of the applet to which this proxy belongs */
	private AppletID appletID_ = null;

	private static Tracer ctracer = new Tracer(AppletProxy.class);
/**
 * Gets the application identifier of the applet to which this proxy belongs.
 *
 * @return The application identifier of the associated applet.
 */
public AppletID getAppletID() {
	return appletID_;
}
/**
 * Instantiate an <tt>AppletProxy</tt> and associates it with the card
 * applet with the given application identifier.
 *
 * @param appletID  The application identifier of the applet to be
 *                  associated with the new <tt>AppletProxy</tt>.
 * @param scheduler The scheduler of this <tt>CardService</tt>.
 * @param card      The controlling </tt>SmartCard</tt> object.
 * @param blocking  Specify the wait behavior for obtaining a
 *                  <tt>CardChannel</tt> from the <tt>CardServiceScheduler</tt>.
 */
protected void initialize(AppletID appletID, CardServiceScheduler scheduler, SmartCard card, boolean blocking) throws CardServiceException {
	ctracer.debug("<init>", "(" + appletID + "," + scheduler + "," + card + "," + blocking + ")");
	super.initialize(scheduler, card, blocking);
	appletID_ = appletID;
}
/**
 * Sends an APDU to the applet to which this proxy belongs, using the given channel.
 *
 * @param channel      The <tt>CardChannel</tt> to be used for sending the
 *                     command APDU to the card.
 * @param commandAPDU  The <tt>CommandAPDU</tt> to be sent.
 *
 * @return The cards response to the command APDU sent.
 */
protected ResponseAPDU sendCommandAPDU(CardChannel channel, CommandAPDU commandAPDU) throws CardTerminalException, CardServiceException {
	return sendCommandAPDU(channel, appletID_, commandAPDU);
}
/**
 * Sends an APDU to the applet to which this proxy belongs.
 *
 * @param commandAPDU The <tt>CommandAPDU</tt> to be sent to the applet
 *
 * @return The <tt>ResponseAPDU</tt> returned by the applet.
 */
public ResponseAPDU sendCommandAPDU(CommandAPDU commandAPDU) throws CardTerminalException, CardServiceException {
	return sendCommandAPDU(appletID_, commandAPDU);
}
/**
 * Send a verify CHV command APDU to the card after filling in the
 * password obtained from the CHV dialog currently associated with
 * this card service.
 *
 * @param channel          The <tt>CardChannel</tt> to be used
 *                         for sending the command APDU.
 * @param verificationAPDU The command APDU for password verification
 *                         into which the password shall be inserted.
 * @param chvControl       The CHV control to be used for password input.
 * @param timeout          The timeout to be used.
 *
 * @return The response APDU returned by the card as response to the
 *         verify password command.
 */
protected ResponseAPDU sendVerifiedAPDU(CardChannel channel, CommandAPDU verificationAPDU, CHVControl chvControl, int timeout) throws CardServiceException, CardTerminalException {
	return sendVerifiedAPDU(channel, appletID_, verificationAPDU, chvControl, timeout);
}
}
