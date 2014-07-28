/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2012 CardContact Software & System Consulting
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


package de.cardcontact.opencard.service.smartcardhsm;

import de.cardcontact.tlv.ConstructedTLV;
import de.cardcontact.tlv.PrimitiveTLV;
import de.cardcontact.tlv.TLVEncodingException;
import de.cardcontact.tlv.Tag;

/**
 * PKCS#15 private key description for RSA and EC
 * 
 * @author lew
 */
public class PrivateKeyDescription {
	
	public static final byte RSA = 1;
	
	public static final byte EC = 2;
	
	private byte keyid;
	
	private String label;
	
	private short size;
	
	private byte type;
	
	private byte[] encoded;	
	
	public PrivateKeyDescription(byte keyid, String label, short size, byte type) throws TLVEncodingException {
		this.keyid = keyid;
		this.label = label;
		this.size = size;
		this.type = type;
		
		if (type == RSA) {
			makeForRSA();
		} else {
			makeForEC();
		}
	}
	
	public PrivateKeyDescription(byte[] prkd) throws TLVEncodingException {
		this.encoded = prkd;
		parseEncoded();		
	}
	
	private void makeForEC() throws TLVEncodingException {
		ConstructedTLV prkd = new ConstructedTLV(0xA0);

		ConstructedTLV part0 = new ConstructedTLV(Tag.SEQUENCE | Tag.CONSTRUCTED);
		part0.add(new PrimitiveTLV(Tag.UTF8String, label.getBytes()));

		ConstructedTLV part1 = new ConstructedTLV(Tag.SEQUENCE | Tag.CONSTRUCTED);
		part1.add(new PrimitiveTLV(Tag.OCTET_STRING, new byte[] {keyid}));
		part1.add(new PrimitiveTLV(Tag.BIT_STRING, new byte[] {0x07, 0x20, (byte) 0x80}));

		ConstructedTLV part2 = new ConstructedTLV(0xA1);
		ConstructedTLV part20 = new ConstructedTLV(Tag.SEQUENCE | Tag.CONSTRUCTED);
		ConstructedTLV part200 = new ConstructedTLV(Tag.SEQUENCE | Tag.CONSTRUCTED);
		PrimitiveTLV part2000 = new PrimitiveTLV(Tag.OCTET_STRING, "".getBytes());
		PrimitiveTLV   part201 = new PrimitiveTLV(Tag.INTEGER, new byte[] {(byte)(size >> 8), (byte)size});		
		part200.add(part2000);
		part20.add(part200);
		if (size > 0) {
			part20.add(part201);
		}
		part2.add(part20);

		prkd.add(part0);
		prkd.add(part1);
		prkd.add(part2);

		encoded = prkd.getBytes();
	}
	
	private void makeForRSA() throws TLVEncodingException {
		ConstructedTLV prkd = new ConstructedTLV(0x30);

		ConstructedTLV part0 = new ConstructedTLV(Tag.SEQUENCE | Tag.CONSTRUCTED);
		part0.add(new PrimitiveTLV(Tag.UTF8String, label.getBytes()));

		ConstructedTLV part1 = new ConstructedTLV(Tag.SEQUENCE | Tag.CONSTRUCTED);
		part1.add(new PrimitiveTLV(Tag.OCTET_STRING, new byte[] {keyid}));
		part1.add(new PrimitiveTLV(Tag.BIT_STRING, new byte[] {0x02, 0x74}));

		ConstructedTLV part2 = new ConstructedTLV(0xA1);
		ConstructedTLV part20 = new ConstructedTLV(Tag.SEQUENCE | Tag.CONSTRUCTED);
		ConstructedTLV part200 = new ConstructedTLV(Tag.SEQUENCE | Tag.CONSTRUCTED);
		PrimitiveTLV part2000 = new PrimitiveTLV(Tag.OCTET_STRING, "".getBytes());
		PrimitiveTLV part201 = new PrimitiveTLV(Tag.INTEGER, new byte[] {(byte)(size >> 8), (byte)size});
		part200.add(part2000);
		part20.add(part200);
		part20.add(part201);
		part2.add(part20);

		prkd.add(part0);
		prkd.add(part1);
		prkd.add(part2);
		
		encoded = prkd.getBytes();
	}
	
	private void parseEncoded() throws TLVEncodingException {
		ConstructedTLV tlv = new ConstructedTLV(encoded);
		Tag sequence = new Tag(0x30);
		PrimitiveTLV content;
		System.out.println(tlv.dump());
		// Get Label
		ConstructedTLV tmp = (ConstructedTLV)tlv.get(0);
		label = new String(tmp.get(0).getValue());
		
		// Get Key ID
		tmp = (ConstructedTLV)tlv.get(1);
		keyid = tmp.get(0).getValue()[0];
		
		// Get key size and return new PRKD
		if (tlv.getTag().equals(sequence)) {
			type = RSA;
			tmp = (ConstructedTLV)tlv.get(2);
			tmp = (ConstructedTLV)tmp.get(0);
			content = (PrimitiveTLV)tmp.get(1);
			byte[] modulussize = content.getValue();
			size = (short)(modulussize[0] << 8 + modulussize[1]);
		} else {
			type = EC;
			size = 0;
			tmp = (ConstructedTLV)tlv.get(2);
			tmp = (ConstructedTLV)tmp.get(0);
			if (tmp.getChildCount() == 2) {
				content = (PrimitiveTLV)tmp.get(1);
				byte[] keysize = content.getValue();
   				size = (short)((keysize[0] & 0xFF) << 8);
				size |= (short)((keysize[1] & 0xFF) << 8);	
			}			
		}		
	}
	
	public byte getKeyID() {
		return keyid;
	}

	public String getLabel() {
		return label;
	}

	public short getSize() {
		return size;
	}

	public byte getType() {
		return type;
	}

	public byte[] getEncoded() {
		return encoded;
	}
	
}
