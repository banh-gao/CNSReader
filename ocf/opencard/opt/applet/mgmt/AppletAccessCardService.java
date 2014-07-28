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


package opencard.opt.applet.mgmt;

import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;
import opencard.opt.applet.AppletID;
import opencard.opt.applet.AppletInfo;
import opencard.opt.service.CardServiceInterface;

/**
 * The <tt>AppletAccessCardService</tt> interface defines a set of
 * calls for accessing card-resident applets.<p>
 * <p>
 * The set of methods supports access to an applet directory and
 * retrieve informations like listed applets, security domain,
 * label, etc.. These informations are stored in an AppletInfo.<p>
 * <p>
 * This interface is typically implemented for card technology handling
 * multi-application management different (e.g. EMV compliant cards,
 * JavaCards, ...)<p>
 * <p>
 *
 * @author   Reto Hermann (rhe@zurich.ibm.com)
 * @author   Thomas Stober (tms@de.ibm.com)
 * @author   Christophe.Muller@research.gemplus.com
 * @version  $Id: AppletAccessCardService.java,v 1.1 1999/11/02 18:36:56 damke Exp $
 * @since    OCF1.2
 *
 * @see opencard.opt.applet.AppletInfo
 * @see opencard.opt.applet.AppletID
 */

public interface AppletAccessCardService extends CardServiceInterface {

    /**
     * List the available card-resident applets on the smart card.<p>
     *
     * @return   An array of <tt>AppletInfo</tt>s of the
     *           card-resident applets.
     * @exception opencard.core.service.CardServiceException
     *            Thrown when error occurs during execution of the operation.
     */
    public AppletInfo[] list()
	throws CardServiceException, CardTerminalException;

    /**
     * Check whether the card-resident applet with the specified
     * <tt>AppletID</tt> exists on the card.<p>
     *
     * @param    appletIdentifier
     *           The <tt>AppletID</tt> object referring to the applet
     *           whose existence we want to check.
     * @return   <tt>true</tt> if card-resident applet exists, otherwise
     *           return <tt>false</tt>.
     * @exception opencard.core.service.CardServiceException
     *            Thrown when error occurs during execution of the operation.
     */
    public boolean exists(AppletID appletIdentifier)
	throws CardServiceException, CardTerminalException;

    /**
     * Reads the <tt>AppletInfo</tt> of a given applet ID from
     * the cards list of applets.
     * <p>
     *
     * @param     appletIdentifier
     *            The <tt>AppletID</tt> object referring to the applet
     *            whose info we want to read.
     * @return    An <tt>AppletInfo</tt> of the applets.
     *            If no applet info was found for the given <tt>AppletID</tt>
     *            a <tt>null</tt pointer is returned
     * @exception opencard.core.service.CardServiceException
     *            Thrown when error occurs during execution of the operation.
     */
    public AppletInfo getInfo(AppletID appletIdentifier)
	throws CardServiceException, CardTerminalException;

}
