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

import opencard.opt.security.SecurityDomain;

/**
 * The <tt>AppletInfo</tt> encapsulates the descriptive information about
 * an applet stored on a smart card (e.g. security Domain or AppletID). <p>
 * AppletInfo is a generic class which is card type independent.
 * It can be subclassed to add more specific informations which
 * are specific for a card type (e.g. for JavaCard or EMV compliant card).<p>
 *
 * @author   Thomas Stober (tstober@de.ibm.com)
 * @author   Reto Hermann (rhe@zurich.ibm.com)
 * @author   Christophe.Muller@research.gemplus.com
 * @version  $Id: AppletInfo.java,v 1.3 2000/01/14 09:32:26 damke Exp $
 * @since    OCF1.2
 *
 */

public class AppletInfo {

  // The instance tracer
//  private Tracer itracer = new Tracer(this, AppletInfo.class);

  /** Applet label */
  protected String label = null;

  /** Applet ID */
  protected AppletID aid = null;

  /** Security Domain (e.g. Applet Path on ISO cards or applets on JavaCards) */
  protected SecurityDomain domain = null;

  /** Free data */
  protected Object data = null;


/** 
 * Creates a new AppletInfo instance.
 */
public AppletInfo() {
}
/** 
 * Get the applet identifier (AppletID).<p>
 *
 * @return An Applet ID.
 */
public AppletID getAppletID() {
	return aid;
}
/** 
 * Get the applet data.<p>
 *
 * @return A data object related to the applet.
 */
public Object getData() {
	return data;
}
/** 
 * Get the applet security domain<p>
 *
 * @return A SecurityDomain
 */
public SecurityDomain getDomain() {
	return domain;
}
/** 
 * Get the applet label.<p>
 *
 * @return A label string describing the applet.
 */
public String getLabel() {
	return label;
}
/** 
 * Set the applet identifier (AppletID).<p>
 *
 * @param _aid An Applet ID.
 */
public void setAppletID(AppletID _aid) {
	aid = _aid;
}
/** 
 * Set the applet data.<p>
 */
public void setData(Object _data) {
	data = _data;
}
/** 
 * Set the applet security domain<p>
 *
 * @param _domain A SecurityDomain
 */
public void setDomain(SecurityDomain _domain) {
	domain = _domain;
}
/** 
 * Set the applet label.<p>
 */
public void setLabel(String _label) {
	label = _label;
}
/**
 * Return the applet information as a string.
 *
 * @return The AppletInfo as a string.
 */
public String toString() {
	StringBuffer at = new StringBuffer("AppletInfo:\n\t");
	if (aid != null) {
		at.append("AID: ").append(aid.toString()).append("\n\t");
	}
	if (label != null) {
		at.append("Label: ").append(label).append("\n\t");
	}
	if (domain != null) {
		at.append("Domain: ").append(domain).append("\n\t");
	}
	return at.toString();
}
}
