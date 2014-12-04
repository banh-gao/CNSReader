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


/**
 * Encapsulates the state of a multi-applicative card. All instances
 * of the class <tt>BasicAppletCardService</tt> associated with the
 * same physical card share a common state object to ensure a
 * consistent view.
 *
 * @author  Thomas Schaeck (schaeck@de.ibm.com)
 * @author  Frank Seliger  (seliger@de.ibm.com
 * @author  Christophe.Muller@research.gemplus.com
 * @version $Id: CardState.java,v 1.2 2000/01/14 09:32:27 damke Exp $
 * @since   OCF1.2
 */
public class CardState {


	/** The application identifier of the currently selected applet. */
	protected AppletID selectedAppletID_ = null;

/**
 * Create a JavaCard state object.
 */
protected CardState() {
	super();
}
/**
 * Gets the Application Identifier of the currently selected Applet.
 *
 * @return application ID of selected Applet
 */
public AppletID getSelectedAppletID() {
	return selectedAppletID_;
}
/**
 * Sets the Application Identifier of the currently selected card applet.
 *
 * @param newAppletID application ID of new selected card applet.
 *
 * @return The applet ID of the previously selected applet.
 */
public AppletID setSelectedAppletID(AppletID newAppletID) {
	// Remember the previous selected applet ID to be returned.
	AppletID oldSelectedAppletID = selectedAppletID_;
	selectedAppletID_ = newAppletID;
	return oldSelectedAppletID;
}
}
