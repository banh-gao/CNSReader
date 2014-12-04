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

import java.util.Enumeration;
import java.util.Vector;

import opencard.core.service.CardChannel;
import opencard.core.service.CardService;
import opencard.core.service.CardServiceException;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.SlotChannel;
import opencard.opt.applet.AppletID;
import opencard.opt.applet.AppletInfo;
import opencard.opt.applet.AppletSelector;
import opencard.opt.applet.ISOAppletSelector;

/**
 * The <tt>AbstractAppletAccessor</tt> is an abstract class providing
 * card type independent base functionality for implementations of an
 * <tt>AppletAccessCardService</tt>. It is however an adapter in the sense
 * that it implements a part of the <tt>AppletAccessCardService></tt>
 * interface and provide the following implementations which can be useful
 * for concrete implementations of the <tt>AppletAccessCardService</tt>
 * interface:
 * <ul>
 * <li> A <tt>selectApplet</tt> method that uses ISO-standardized APDU code.
 *      Note: this particular feature is accessed through a helper
 *      object that is by default an "<tt>ISOAppletSelector</tt>" but can
 *      be changed by subclasses with <tt>setAppletSelector</tt>.
 * <li> A method to "list" the available applets on the card. Note that
 *      although this method is implemented, it relies on the fact that
 *      an abstract "internalList" method must be implemented by subclasses
 *      and called before it, this is why the class is an "abstract" one.
 * <li> A method "getInfo" to get the related AppletInfo object of a
 *      specific applet specified by AID.
 * <li> A method "exists" to check if a specified applet (given its AID)
 *      is available on the card. It relies on internalList as well.
 * </ul>
 *
 * @author   Thomas Stober (tms@de.ibm.com)
 * @author   Christophe.Muller@research.gemplus.com
 * @version  $Id: AbstractAppletAccessor.java,v 1.3 1999/03/23 13:32:59
 tstober Exp $
 * @since    OCF1.2
 *
 */

public abstract class AbstractAppletAccessor
    extends CardService
    implements AppletSelector, AppletAccessCardService {

    /**
     * A manager that implements at least "AppletSelector"
     * in order to perform the low-level applet selection.
     */
    private AppletSelector selector_ = new ISOAppletSelector();

//    private Tracer itracer
//	= new Tracer(this, AbstractAppletAccessor.class);
//    private static Tracer ctracer
//	= new Tracer(AbstractAppletAccessor.class);

    /** The AppletInfo Informations on the Card */
    private Vector applets = null;
    public Vector getApplets() { return applets; }
    public void setApplets(Vector apps) { applets = apps; }


    /////////////// construction  ///////////////////////////////////////////

    /**
     * Instantiate a <tt>AbstractAppletAccessor</tt> object.
     */
    public AbstractAppletAccessor() throws CardServiceException {
	super();
    }


    /////////////// service  ///////////////////////////////////////////


    /** Set the applet selector.<p>
     *  Subclasses may need to redefine the way applet selection
     *  is performed. They can do that by specifying a class implementing
     *  "AppletSelector" and call setAppletSelector with a new helper
     *  object that will be used in place of the default ISO implementation.
     */
    protected void setAppletSelector(AppletSelector s) {
	selector_ = s;
    }

    /** Get the applet selector.<p>
     *
     * @return A helper object that implements the "AppletSelector" interface.
     */
    protected AppletSelector getAppletSelector() {
	return selector_;
    }

    /**
     * Selects the card applet with the given application ID using the
     * given channel for communication with the card.
     *
     * @param channel   The <tt>CardChannel</tt> to be used for sending
     *                  the select command to the card.
     * @param appletID The application identifier of the applet to be selected.
     *
     * @return The <tt>AppletInfo</tt> returned by the select command.
     */
    public AppletInfo selectApplet(CardChannel channel, AppletID appletID)
	throws CardServiceException, CardTerminalException {

	return selector_.selectApplet(channel, appletID);

    }

    /**
     * List the applets info informations.<p>
     * This ABSTRACT method has to be implemented by card standard
     * specific subclasses.<p>
     * Subclasses will need to call this method and then <tt>setApplets</tt>
     * in order to initialize the <tt>applets</tt> attribute.
     *
     * @return   A Vector of <tt>AppletInfo</tt> Object
     *           representing the card-resident applets.
     * @exception opencard.core.service.CardServiceException
     *            Thrown when the list cannot be presented.
     */
    protected abstract Vector internalList(SlotChannel channel)
	throws CardServiceException;


    /**
     * List the available card-resident applets as array.<p>
     *
     * @return   An array of <tt>AppletInfo</tt>s of the
     *           card-resident applets.
     * @exception opencard.core.service.CardServiceException
     *            Thrown when the list cannot be presented.
     */
    public AppletInfo[] list() throws CardServiceException
    {
	AppletInfo[] result = null;

	if (applets!=null) {
	    // copy the applets vector to the result array!
	    result = new AppletInfo[applets.size()];
	    Enumeration e = applets.elements();
	    for (int i = 0; i < result.length; i++) {
		result[i] = (AppletInfo) e.nextElement();
	    }
	    return result;
	} else {
	    throw new CardServiceException("Directory information not available");
	}
    }


    /**
     * Reads the <tt>AppletInfo from the directory on the card.
     * <p>
     *
     * @param     aid
     *            The <tt>AppletID</tt> object referring to the applets
     *            whose Info we want to read.
     * @return    An <tt>AppletInfo</tt> of the applets.
     *            If no applets info was found for the given <tt>AppletID</tt>
     *            a <tt>null</tt pointer is returned
     * @exception opencard.core.service.CardServiceException
     *            Thrown when error occurs during execution of the operation.
     */
    public AppletInfo getInfo( AppletID aid)
	throws CardServiceException
    {
	AppletInfo foundApplet = null;

	if (applets!=null) {
	    // search in the applets vector for appletID!
	    Enumeration e = applets.elements();
	    while ((e.hasMoreElements())&&(foundApplet==null)) {
		AppletInfo aAppletInfo = (AppletInfo) e.nextElement();
		if (((AppletID)aAppletInfo.getAppletID()).equals(aid))
		    foundApplet = aAppletInfo;
	    }
	    return foundApplet;
	} else {
	    throw new CardServiceException("Directory information not available");
	}
    }


    /**
     * Check whether the card-resident applets with the specified
     * <tt>AppletID</tt> exists on the card.<p>
     *
     * @param    appletIdentifier
     *           The <tt>AppletID</tt> object referring to the applet
     *           whose existence we want to check.
     * @return   <tt>true</tt> if card-resident applet exists, otherwise
     *           return <tt>false</tt>.
     */
    public boolean exists( AppletID appletIdentifier)
	throws CardServiceException
    {
	if (getInfo(appletIdentifier)==null){
	    return false;
	} else {
	    return true;
	}
    }

}
