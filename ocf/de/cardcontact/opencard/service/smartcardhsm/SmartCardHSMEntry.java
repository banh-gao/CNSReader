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

import java.security.cert.Certificate;



/**
 * This class gives a representation of an SmartCardHSM entry.
 * 
 * An Entry can contain either a private key reference with corresponding public key certificate,
 * or a CA certificate.
 * 
 * @author lew
 *
 */
public class SmartCardHSMEntry {



	/**
	 * The private key reference
	 */
	private SmartCardHSMKey key = null;



	/**
	 * The Certificate is either an EE certificate or a CA certificate
	 */
	private Certificate cert = null;



	/**
	 * @return true if the certificate is an EE certificate, false if it is an CA certificate
	 */
	private boolean isEECertificate;



	/**
	 * The key or certificate id
	 */
	private byte id;



	/**
	 * SmartCardHSMEntry constructor
	 * 
	 * @param key Reference to the private key on the card
	 */
	public SmartCardHSMEntry(SmartCardHSMKey key) {
		this.key = key;
		this.id = key.getKeyID();
	}



	/**
	 * SmartCardHSMEntry constructor
	 * 
	 * @param cert Certificate
	 * @param isEECertificate true for EE certificates false for CA certificates
	 * @param id The certificate ID
	 */
	public SmartCardHSMEntry(Certificate cert, boolean isEECertificate, byte id) {
		this.cert = cert;
		this.isEECertificate = isEECertificate;
		this.setId(id);
	}



	/**
	 * @return true for EE certificates false for CA certificates
	 */
	public boolean isEECertificate() {
		return isEECertificate;
	}



	public boolean isCertificateEntry() {
		return cert != null;
	}



	public boolean isKeyEntry() {
		return key != null;
	}



	public SmartCardHSMKey getKey() {
		return key;
	}



	public void setKey(SmartCardHSMKey key) {
		this.key = key;
	}



	public void setCert(Certificate cert, boolean isEECertificate, byte id) {
		this.cert = cert;
		this.isEECertificate = isEECertificate;
		this.id = id;
	}



	public Certificate getCert() {
		return cert;
	}



	public void setId(byte id) {
		this.id = id;
	}



	public byte getId() {
		return id;
	}
}
