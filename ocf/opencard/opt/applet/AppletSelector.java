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

/**
 * The <tt>AppletSelector</tt> interface defines the <strong>minimum</strong>
 * features that are necessary for working with card-resident applets.<p>
 * <p>
 *
 * @author   Christophe.Muller@research.gemplus.com
 * @version  $Id: AppletSelector.java,v 1.2 2000/01/14 09:32:26 damke Exp $
 * @since    OCF1.2
 *
 * @see opencard.core.service.CardChannel
 * @see opencard.opt.applet.AppletID
 * @see opencard.opt.applet.AppletInfo
 */

public interface AppletSelector {

	/**
	 * Select a given applet with the specified <tt>AppletID</tt>.<p>
	 *
	 * @param    channel
	 *           The <tt>CardChannel</tt> object that must be used
	 *           if it is specified (if this argument is null,
	 *           the function must allocate a new card channel).
	 * @param    appletIdentifier
	 *           The <tt>AppletID</tt> object referring to the applet
	 *           to be selected.
	 * @return   applet info<br>
	 *           an <tt>AppletInfo</tt> object with the information that
	 *           has been provided by the card (or at the minimum
	 *           an AppletInfo object including the Applet AID).
	 *           A <tt>ResponseAPDU</tt> object can be stored in the
	 *           "data" attribute of the returned AppletInfo in order to
	 *           provide more detailed information to the caller.
	 * @exception opencard.core.terminal.CardTerminalException
	 *            Thrown when error occurs during the communication.
	 * @exception opencard.core.service.CardServiceException
	 *            Thrown when error occurs during execution of the operation.
	 */
	public AppletInfo selectApplet(CardChannel channel,
				   AppletID appletIdentifier)
	throws CardServiceException, CardTerminalException;
}
