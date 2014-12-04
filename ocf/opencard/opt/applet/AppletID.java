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
 * <tt>AppletID</tt> encapsulates the applet identifier as
 * defined by ISO 7816-5. Presently the AppletID <p>
 * consists out of an array of at most 16 bytes.
 * It is derived from the class ID which defines the handling of byte
 * arrays which are used as IDs.
 *
 * @author  Reto Hermann   (rhe@zurich.ibm.com)
 * @author  Thomas Schaeck (schaeck@de.ibm.com)
 * @author  Thomas Stober  (tstober@de.ibm.com)
 *
 * @version $Id: AppletID.java,v 1.4 2000/01/14 09:32:26 damke Exp $
 * @since   OCF1.2
 *
 * @see opencard.opt.applet.ID
 */

public class AppletID extends ID implements SecurityDomain {
/** 
 * Constructs the AppletID from a byte array.
 *
 * @param bytes Byte array for initialization of the ID.
 */
public AppletID(byte[] bytes) {
	super(bytes);
}
/** 
 * Constructs the AppletID from a hexadecimal string.
 *
 * @param appletID Hexadecimal string for initialization of the ID.
 */
public AppletID(String appletID) {
	super(appletID);
}
/**
 * Checks whether this ID equals another object.
 *
 * @param object The object to be compared with this ID.
 */
public boolean equals(Object object) {
	if ((object != null) && (object instanceof AppletID)) {
		return super.equals(object);
	}
	return false;
}
}
