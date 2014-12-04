/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2006 CardContact Software & System Consulting
 * |'##> <##'|  Andreas Schwier, 32429 Minden, Germany (www.cardcontact.de)
 *  --------- 
 *
 *  This file is part of OpenSCDP.
 *
 *  OpenSCDP is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *
 *  OpenSCDP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSCDP; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package de.cardcontact.tlv;

/**
 * Class to implement ASN.1 SEQUENCE object
 *  
 * @author Andreas Schwier (info@cardcontact.de)
 */
public class Sequence extends ConstructedTLV {

	/**
	 * Create constructed TLV with Tag set to ASN.1 SEQUENCE
	 *
	 */	
	public Sequence() {
		super(new Tag(Tag.SEQUENCE, Tag.UNIVERSAL, true));
	}

	

	/**
	 * Create object from parse buffer
	 * 
	 * This should not be called directly. Use TLV.factory() methods instead
	 * 
	 * @param pb
	 */
	public Sequence(ParseBuffer pb) throws TLVEncodingException {
		super(pb);

		if ((tag.getNumber() != Tag.SEQUENCE) || !(tag.isConstructed())) {
			throw new TLVEncodingException("Invalid tag for SEQUENCE");
		}
	}
	
	
	
	/**
	 * Copy constructor
	 * 
	 * Initialise with existing ConstructedTLV object. Does not perform
	 * a deep copy. The tag and list of contained objects is reassigned.
	 * 
	 * Caution: If applied to a TLV object embedded in a complex structure
	 * remember to update the reference to this object in the parent node.
	 * 
	 * @param tlv
	 * 		ConstructedTLV
	 * 
	 * @throws UnsupportedOperationException
	 * 		
	 */
	public Sequence(TLV tlv) throws UnsupportedOperationException {
		super(tlv);
	}
}
