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


import opencard.core.util.HexString;

/**
 * ID - A wrapper class for unsigned byte arrays.<BR>
 *
 * @author   Thomas Stober (tstober@de.ibm.com)
 * @version  $Id: ID.java,v 1.5 2000/01/14 09:32:27 damke Exp $
 * @since    OCF1.2
 *
 * @see opencard.core.util.HexString
 */

public class ID extends Object
{
  /** this is the thing we want to hide to make life easier*/
  byte[] array_ = null;

/** 
 * Constructs the AppletID from a byte array.
 *
 * @param array Byte array for initialization of the ID.
 */
public ID(byte[] array) {
	array_ = new byte[array.length];
	System.arraycopy(array, 0, array_, 0, array.length);
}
/** 
 * Construct the ID from a hexadecimal string.
 *
 * @param hexString Hexadecimal string for initialization of the ID.
 */
public ID(String hexString) {
	array_ = HexString.parseHexString(hexString);
}
/**
 * Checks whether this ID equals another object.
 *
 * @param object The object to be compared with this ID.
 */
public boolean equals(Object object) {
	if ((object != null) && (object instanceof ID)) {
		byte[] array = ((ID) object).getBytes();
		if (array.length == array_.length) {
			for (int i = 0; i < array.length; i++) {
				if (array[i] != array_[i]) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	} 
	return false;
}
/**
 * Returns the ID as a byte array.
 *
 * @return The ID as a byte array.
 */
public byte[] getBytes() {
	return array_;
}
/**
 * Returns a hash code for the ID.
 */
public int hashCode() {
	return toString().hashCode();
}
/**
 * Returns the ID as a string.
 *
 * @return The ID as a string.
 */
public String toString() {
	return HexString.hexify(array_);
}
}
