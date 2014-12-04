/*
 * Copyright © 1998 Gemplus SCA
 * Av. du Pic de Bertagne - Parc d'Activités de Gémenos
 * BP 100 - 13881 Gémenos CEDEX
 * 
 * "Code derived from the original OpenCard Framework".
 * 
 * Everyone is allowed to redistribute and use this source  (source
 * code)  and binary (object code),  with or  without modification,
 * under some conditions:
 * 
 *  - Everyone  must  retain  and/or  reproduce the above copyright
 *    notice,  and the below  disclaimer of warranty and limitation
 *    of liability  for redistribution and use of these source code
 *    and object code.
 * 
 *  - Everyone  must  ask a  specific prior written permission from
 *    Gemplus to use the name of Gemplus.
 * 
 * DISCLAIMER OF WARRANTY
 * 
 * THIS CODE IS PROVIDED "AS IS",  WITHOUT ANY WARRANTY OF ANY KIND
 * (INCLUDING,  BUT  NOT  LIMITED  TO,  THE IMPLIED  WARRANTIES  OF
 * MERCHANTABILITY  AND FITNESS FOR  A  PARTICULAR PURPOSE)  EITHER
 * EXPRESS OR IMPLIED.  GEMPLUS DOES NOT WARRANT THAT THE FUNCTIONS
 * CONTAINED  IN THIS SOFTWARE WILL MEET THE USER'S REQUIREMENTS OR
 * THAT THE OPERATION OF IT WILL BE UNINTERRUPTED OR ERROR-FREE. NO
 * USE  OF  ANY  CODE  IS  AUTHORIZED  HEREUNDER EXCEPT UNDER  THIS
 * DISCLAIMER.
 * 
 * LIMITATION OF LIABILITY
 * 
 * GEMPLUS SHALL NOT BE LIABLE FOR INFRINGEMENTS OF  THIRD  PARTIES
 * RIGHTS. IN NO EVENTS, UNLESS REQUIRED BY APPLICABLE  LAW,  SHALL
 * GEMPLUS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES OF ANY CHARACTER  INCLUDING,
 * WITHOUT LIMITATION, DAMAGES FOR LOSS OF GOODWILL, WORK STOPPAGE,
 * COMPUTER FAILURE OR MALFUNCTION, OR ANY AND ALL OTHER DAMAGES OR
 * LOSSES, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. ALSO,
 * GEMPLUS IS  UNDER NO  OBLIGATION TO MAINTAIN,  CORRECT,  UPDATE, 
 * CHANGE, MODIFY, OR OTHERWISE SUPPORT THIS SOFTWARE.
 */
package opencard.opt.database;


/**
 * A <code>SecurityAttribute</code> is a wrapper for a byte array
 * that holds a security attribute (DO, or Data Object) to be passed
 * as parameter to some functions of a <code>DatabaseCardService</code>
 * (such as <code>createTable</code>, <code>createView</code>, or
 * <code>presentUser</code>). Note that the ISO7816-7 standard
 * does not specify what information should be provided and in what
 * form. This is up to proprietary sub-classes to add semantics to
 * this raw data.
 * 
 * <p>Important Note: as <strong>when</strong> security attributes
 * are specified in a Command APDU, it is <strong>always</strong>
 * in the form of: 'Lp <DO>' (see the ISO7816-7 standard) where 'Lp'
 * is the Parameter length and 'DO' (Data Object) the actual byte array,
 * the <code>getBytes</code> method of <code>SecurityAttribute</code>
 * is returning the security information in this form (i.e., with the
 * data length added as one byte at the beginning of the returned byte array.
 * 
 * @author  Arnaud HAMEL
 * @author  Cedric DANGREMONT
 * @author  Christophe.Muller@research.gemplus.com
 * @version $Id: SecurityAttribute.java,v 1.1 1999/12/06 15:46:06 damke Exp $
 * @since   OCF1.2
 * 
 * @see opencard.opt.database.DatabaseCardService#createTable(java.lang.String, java.lang.String, byte, opencard.opt.database.SecurityAttribute)
 * @see opencard.opt.database.DatabaseCardService#createView(java.lang.String, java.lang.String, java.lang.String, java.lang.String, opencard.opt.database.SecurityAttribute)
 * @see opencard.opt.database.DatabaseCardService#presentUser(java.lang.String, opencard.opt.database.SecurityAttribute)  
 */

public class SecurityAttribute extends Object {
    private byte[] dataObject = null;
    
    public SecurityAttribute (byte dObj[]) {
	super();
	this.dataObject = new byte[dObj.length];
	System.arraycopy(dObj, 0, dataObject, 0, dObj.length);
    }
    
    public SecurityAttribute (String password) {
	super();
	this.dataObject = password.getBytes();
    }
    
    /**
     * Copies the byte values from this object data into the destination
     * byte array. In the first element of the returned array, the length
     * of the data is inserted, thus the returned array is suitable to be
     * directly included into an APDU command.
     * 
     * @return      The resultant byte array
     */
    public byte[] getBytes() {
	return DataObject.bodyAPDU(dataObject);
    }

    public byte getDataLength() {
	return (byte)dataObject.length;
    }

}
