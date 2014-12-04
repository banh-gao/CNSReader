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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;



import de.cardcontact.opencard.security.SecureChannel;
import de.cardcontact.opencard.security.SecureChannelCredential;
import de.cardcontact.opencard.service.CardServiceUnexpectedStatusWordException;
import de.cardcontact.opencard.service.isocard.CHVCardServiceWithControl;
import de.cardcontact.opencard.service.isocard.CHVManagementCardService;
import de.cardcontact.opencard.service.isocard.FileSystemSendAPDU;
import de.cardcontact.opencard.service.isocard.IsoConstants;
import de.cardcontact.opencard.service.isocard.IsoFileControlInformation;
import de.cardcontact.opencard.service.eac20.EAC20;
import de.cardcontact.opencard.terminal.android.cgcard.CGMicroSDCardTerminal;
import de.cardcontact.tlv.ConstructedTLV;
import de.cardcontact.tlv.ObjectIdentifier;
import de.cardcontact.tlv.PrimitiveTLV;
import de.cardcontact.tlv.TLVEncodingException;
import de.cardcontact.tlv.Tag;
import de.cardcontact.tlv.cvc.CardVerifiableCertificate;
import opencard.core.service.CHVUtils;
import opencard.core.service.CardChannel;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceInabilityException;
import opencard.core.service.CardServiceInvalidParameterException;
import opencard.core.service.CardServiceOperationFailedException;
import opencard.core.service.DefaultCHVDialog;
import opencard.core.service.InvalidCardChannelException;
import opencard.core.terminal.CHVControl;
import opencard.core.terminal.CHVEncoder;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalIOControl;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ExtendedVerifiedAPDUInterface;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.VerifiedAPDUInterface;
import opencard.core.util.HexString;
import opencard.opt.applet.AppletID;
import opencard.opt.applet.BasicAppletCardService;
import opencard.opt.iso.fs.CardFileAppID;
import opencard.opt.iso.fs.CardFileFileID;
import opencard.opt.iso.fs.CardFileInfo;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.iso.fs.CardIOException;
import opencard.opt.iso.fs.FileSystemCardService;
import opencard.opt.security.CredentialBag;
import opencard.opt.security.PrivateKeyRef;
import opencard.opt.security.PublicKeyRef;
import opencard.opt.security.SecureService;
import opencard.opt.security.SecurityDomain;
import opencard.opt.service.CardServiceObjectNotAvailableException;
import opencard.opt.service.CardServiceResourceNotFoundException;
import opencard.opt.service.CardServiceUnexpectedResponseException;

/**
 * Class implementing a SmartCard HSM card service
 * 
 * @author lew
 *
 */
public class SmartCardHSMCardService extends BasicAppletCardService implements
FileSystemCardService, CHVCardServiceWithControl, CHVManagementCardService, SecureService, 
KeyGenerationCardServiceWithSpec, DecipherCardService, FileSystemSendAPDU {


	/**
	 * SmartCardHSMCardService log
	 */
	private final static Logger log = Logger.getLogger(SmartCardHSMCardService.class.getName());


	/**
	 * CardFilePath containing the master file
	 */
	private static final CardFilePath mf = new CardFilePath("#E82B0601040181C31F0201");



	/**
	 * The application identifier
	 */
	private final static AppletID AID = new AppletID(new byte[] {(byte)0xE8, (byte)0x2B, (byte)0x06, (byte)0x01, (byte)0x04, (byte)0x01, (byte)0x81, (byte)0xC3, (byte)0x1F, (byte)0x02, (byte)0x01});



	public static final String ALGO_PADDING_PKCS1_PSS = "PKCS1_PSS";



	/**
	 * This HashMap returns by a given padding algorithm a HashMap 
	 * which contains the corresponding algorithm identifier
	 */
	private static HashMap<String, HashMap<String, Byte>> ALGORITHM_PADDING = new HashMap<String, HashMap<String, Byte>>();



	/**
	 * ECDH algorithm id
	 */
	private static byte ECDH = (byte)0x80;



	/**
	 * Algorithm id used for decipher
	 */
	private static byte NONE_WITH_RSA_DECRIPTION = (byte)0x21;


	
	/**
	 * Wrap algorithm id
	 */
	private static final byte WRAP = (byte)0x92;
	
	
	
	/**
	 * Unwrap algorithm id
	 */
	private static final byte UNWRAP = (byte)0x93;
	
	
	
	/**
	 * Number for User PIN
	 */
	private static final int USER_PIN = 0x81;
	
	
	
	 /**
	  * Number for SO PIN
	  */
	private static final int SO_PIN = 0x88;
	
	

	/**
	 * This HashMap returns by a given alias the corresponding SmartCardHSMEntry
	 */
	private final HashMap<String, SmartCardHSMEntry> namemap = new HashMap<String, SmartCardHSMEntry>(200);



	/**
	 * This HashMap returns by a given key id the corresponding private key reference
	 */
	private final HashMap<Byte, SmartCardHSMKey> idmap = new HashMap<Byte, SmartCardHSMKey>(100); 


	
	private final HashMap<Byte, Certificate> certIDMap = new HashMap<Byte, Certificate>(100);
	
	

	/**
	 * A Vector containing CA id's
	 */
	private final Vector<Byte> caid = new Vector<Byte>();



	/**
	 * The maximum number of keys that can be stored on the card
	 */
	private static final int KEY_CAPACITY  = 60; 



	/**
	 * Prefix for private keys
	 */
	private static final byte KEYPREFIX = (byte) 0xCC;



	/**
	 * Prefix for private key description
	 */
	private static final byte PRKDPREFIX = (byte) 0xC4;



	/**
	 * Prefix for EE certificates
	 */
	public static final byte EECERTIFICATEPREFIX = (byte) 0xCE;



	/**
	 * Prefix for CA certificates
	 */
	public static final byte CACERTIFICATEPREFIX = (byte) 0xCA;

	
	
	public static final byte CERTDESCRIPTIONPREFIX = (byte) 0xC8;
	
	

	/**
	 * True if secure messaging shall be used
	 */
	private boolean doSecureMessaging = false;

	

	/**
	 * True if terminal pin pad shall be used. 
	 * Default: false
	 */
	private boolean usePinPad = false;

	
	/**
	 * Maximum chunk size for read/write operations on Micro SD cards
	 */
	private static final short MAX_CHUNK_SIZE_MICROSD = 450;
	
	/**
	 * Maximum chunk size for read/write operations
	 */
	private static final short MAX_CHUNK_SIZE_READ = 1000;
	private static final short MAX_CHUNK_SIZE_WRITE = 994;
		
	/**
	 * Credentials for secure messaging
	 */
	private SecureChannelCredential credential;
	
	
	private static final byte[] ROOT_CA = "DESRCACC100001".getBytes();
	
	
	
	private static final byte[] UT_CA = "UTSRCACC100001".getBytes();

	
	
	private static final byte[] rootCert = new byte[] {
		(byte)0x7F,(byte)0x21,(byte)0x82,(byte)0x01,(byte)0xB4,(byte)0x7F,(byte)0x4E,(byte)0x82,
		(byte)0x01,(byte)0x6C,(byte)0x5F,(byte)0x29,(byte)0x01,(byte)0x00,(byte)0x42,(byte)0x0E,
		(byte)0x44,(byte)0x45,(byte)0x53,(byte)0x52,(byte)0x43,(byte)0x41,(byte)0x43,(byte)0x43,
		(byte)0x31,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x31,(byte)0x7F,(byte)0x49,
		(byte)0x82,(byte)0x01,(byte)0x1D,(byte)0x06,(byte)0x0A,(byte)0x04,(byte)0x00,(byte)0x7F,
		(byte)0x00,(byte)0x07,(byte)0x02,(byte)0x02,(byte)0x02,(byte)0x02,(byte)0x03,(byte)0x81,
		(byte)0x20,(byte)0xA9,(byte)0xFB,(byte)0x57,(byte)0xDB,(byte)0xA1,(byte)0xEE,(byte)0xA9,
		(byte)0xBC,(byte)0x3E,(byte)0x66,(byte)0x0A,(byte)0x90,(byte)0x9D,(byte)0x83,(byte)0x8D,
		(byte)0x72,(byte)0x6E,(byte)0x3B,(byte)0xF6,(byte)0x23,(byte)0xD5,(byte)0x26,(byte)0x20,
		(byte)0x28,(byte)0x20,(byte)0x13,(byte)0x48,(byte)0x1D,(byte)0x1F,(byte)0x6E,(byte)0x53,
		(byte)0x77,(byte)0x82,(byte)0x20,(byte)0x7D,(byte)0x5A,(byte)0x09,(byte)0x75,(byte)0xFC,
		(byte)0x2C,(byte)0x30,(byte)0x57,(byte)0xEE,(byte)0xF6,(byte)0x75,(byte)0x30,(byte)0x41,
		(byte)0x7A,(byte)0xFF,(byte)0xE7,(byte)0xFB,(byte)0x80,(byte)0x55,(byte)0xC1,(byte)0x26,
		(byte)0xDC,(byte)0x5C,(byte)0x6C,(byte)0xE9,(byte)0x4A,(byte)0x4B,(byte)0x44,(byte)0xF3,
		(byte)0x30,(byte)0xB5,(byte)0xD9,(byte)0x83,(byte)0x20,(byte)0x26,(byte)0xDC,(byte)0x5C,
		(byte)0x6C,(byte)0xE9,(byte)0x4A,(byte)0x4B,(byte)0x44,(byte)0xF3,(byte)0x30,(byte)0xB5,
		(byte)0xD9,(byte)0xBB,(byte)0xD7,(byte)0x7C,(byte)0xBF,(byte)0x95,(byte)0x84,(byte)0x16,
		(byte)0x29,(byte)0x5C,(byte)0xF7,(byte)0xE1,(byte)0xCE,(byte)0x6B,(byte)0xCC,(byte)0xDC,
		(byte)0x18,(byte)0xFF,(byte)0x8C,(byte)0x07,(byte)0xB6,(byte)0x84,(byte)0x41,(byte)0x04,
		(byte)0x8B,(byte)0xD2,(byte)0xAE,(byte)0xB9,(byte)0xCB,(byte)0x7E,(byte)0x57,(byte)0xCB,
		(byte)0x2C,(byte)0x4B,(byte)0x48,(byte)0x2F,(byte)0xFC,(byte)0x81,(byte)0xB7,(byte)0xAF,
		(byte)0xB9,(byte)0xDE,(byte)0x27,(byte)0xE1,(byte)0xE3,(byte)0xBD,(byte)0x23,(byte)0xC2,
		(byte)0x3A,(byte)0x44,(byte)0x53,(byte)0xBD,(byte)0x9A,(byte)0xCE,(byte)0x32,(byte)0x62,
		(byte)0x54,(byte)0x7E,(byte)0xF8,(byte)0x35,(byte)0xC3,(byte)0xDA,(byte)0xC4,(byte)0xFD,
		(byte)0x97,(byte)0xF8,(byte)0x46,(byte)0x1A,(byte)0x14,(byte)0x61,(byte)0x1D,(byte)0xC9,
		(byte)0xC2,(byte)0x77,(byte)0x45,(byte)0x13,(byte)0x2D,(byte)0xED,(byte)0x8E,(byte)0x54,
		(byte)0x5C,(byte)0x1D,(byte)0x54,(byte)0xC7,(byte)0x2F,(byte)0x04,(byte)0x69,(byte)0x97,
		(byte)0x85,(byte)0x20,(byte)0xA9,(byte)0xFB,(byte)0x57,(byte)0xDB,(byte)0xA1,(byte)0xEE,
		(byte)0xA9,(byte)0xBC,(byte)0x3E,(byte)0x66,(byte)0x0A,(byte)0x90,(byte)0x9D,(byte)0x83,
		(byte)0x8D,(byte)0x71,(byte)0x8C,(byte)0x39,(byte)0x7A,(byte)0xA3,(byte)0xB5,(byte)0x61,
		(byte)0xA6,(byte)0xF7,(byte)0x90,(byte)0x1E,(byte)0x0E,(byte)0x82,(byte)0x97,(byte)0x48,
		(byte)0x56,(byte)0xA7,(byte)0x86,(byte)0x41,(byte)0x04,(byte)0x6D,(byte)0x02,(byte)0x5A,
		(byte)0x80,(byte)0x26,(byte)0xCD,(byte)0xBA,(byte)0x24,(byte)0x5F,(byte)0x10,(byte)0xDF,
		(byte)0x1B,(byte)0x72,(byte)0xE9,(byte)0x88,(byte)0x0F,(byte)0xFF,(byte)0x74,(byte)0x6D,
		(byte)0xAB,(byte)0x40,(byte)0xA4,(byte)0x3A,(byte)0x3D,(byte)0x5C,(byte)0x6B,(byte)0xEB,
		(byte)0xF2,(byte)0x77,(byte)0x07,(byte)0xC3,(byte)0x0F,(byte)0x6D,(byte)0xEA,(byte)0x72,
		(byte)0x43,(byte)0x0E,(byte)0xE3,(byte)0x28,(byte)0x7B,(byte)0x06,(byte)0x65,(byte)0xC1,
		(byte)0xEA,(byte)0xA6,(byte)0xEA,(byte)0xA4,(byte)0xFA,(byte)0x26,(byte)0xC4,(byte)0x63,
		(byte)0x03,(byte)0x00,(byte)0x19,(byte)0x83,(byte)0xF8,(byte)0x2B,(byte)0xD1,(byte)0xAA,
		(byte)0x31,(byte)0xE0,(byte)0x3D,(byte)0xA0,(byte)0x62,(byte)0x87,(byte)0x01,(byte)0x01,
		(byte)0x5F,(byte)0x20,(byte)0x0E,(byte)0x44,(byte)0x45,(byte)0x53,(byte)0x52,(byte)0x43,
		(byte)0x41,(byte)0x43,(byte)0x43,(byte)0x31,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,
		(byte)0x31,(byte)0x7F,(byte)0x4C,(byte)0x10,(byte)0x06,(byte)0x0B,(byte)0x2B,(byte)0x06,
		(byte)0x01,(byte)0x04,(byte)0x01,(byte)0x81,(byte)0xC3,(byte)0x1F,(byte)0x03,(byte)0x01,
		(byte)0x01,(byte)0x53,(byte)0x01,(byte)0xC0,(byte)0x5F,(byte)0x25,(byte)0x06,(byte)0x01,
		(byte)0x02,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x09,(byte)0x5F,(byte)0x24,(byte)0x06,
		(byte)0x03,(byte)0x02,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x08,(byte)0x5F,(byte)0x37,
		(byte)0x40,(byte)0x9D,(byte)0xBB,(byte)0x38,(byte)0x2B,(byte)0x17,(byte)0x11,(byte)0xD2,
		(byte)0xBA,(byte)0xAC,(byte)0xB0,(byte)0xC6,(byte)0x23,(byte)0xD4,(byte)0x0C,(byte)0x62,
		(byte)0x67,(byte)0xD0,(byte)0xB5,(byte)0x2B,(byte)0xA4,(byte)0x55,(byte)0xC0,(byte)0x1F,
		(byte)0x56,(byte)0x33,(byte)0x3D,(byte)0xC9,(byte)0x55,(byte)0x48,(byte)0x10,(byte)0xB9,
		(byte)0xB2,(byte)0x87,(byte)0x8D,(byte)0xAF,(byte)0x9E,(byte)0xC3,(byte)0xAD,(byte)0xA1,
		(byte)0x9C,(byte)0x7B,(byte)0x06,(byte)0x5D,(byte)0x78,(byte)0x0D,(byte)0x6C,(byte)0x9C,
		(byte)0x3C,(byte)0x2E,(byte)0xCE,(byte)0xDF,(byte)0xD7,(byte)0x8D,(byte)0xEB,(byte)0x18,
		(byte)0xAF,(byte)0x40,(byte)0x77,(byte)0x8A,(byte)0xDF,(byte)0x89,(byte)0xE8,(byte)0x61,
		(byte)0xCA
	};
	
	
	
	private static final byte[] utCert = new byte[] {
		(byte)0x7F,(byte)0x21,(byte)0x82,(byte)0x01,(byte)0xB4,(byte)0x7F,(byte)0x4E,(byte)0x82,
		(byte)0x01,(byte)0x6C,(byte)0x5F,(byte)0x29,(byte)0x01,(byte)0x00,(byte)0x42,(byte)0x0E,
		(byte)0x55,(byte)0x54,(byte)0x53,(byte)0x52,(byte)0x43,(byte)0x41,(byte)0x43,(byte)0x43,
		(byte)0x31,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x31,(byte)0x7F,(byte)0x49,
		(byte)0x82,(byte)0x01,(byte)0x1D,(byte)0x06,(byte)0x0A,(byte)0x04,(byte)0x00,(byte)0x7F,
		(byte)0x00,(byte)0x07,(byte)0x02,(byte)0x02,(byte)0x02,(byte)0x02,(byte)0x03,(byte)0x81,
		(byte)0x20,(byte)0xA9,(byte)0xFB,(byte)0x57,(byte)0xDB,(byte)0xA1,(byte)0xEE,(byte)0xA9,
		(byte)0xBC,(byte)0x3E,(byte)0x66,(byte)0x0A,(byte)0x90,(byte)0x9D,(byte)0x83,(byte)0x8D,
		(byte)0x72,(byte)0x6E,(byte)0x3B,(byte)0xF6,(byte)0x23,(byte)0xD5,(byte)0x26,(byte)0x20,
		(byte)0x28,(byte)0x20,(byte)0x13,(byte)0x48,(byte)0x1D,(byte)0x1F,(byte)0x6E,(byte)0x53,
		(byte)0x77,(byte)0x82,(byte)0x20,(byte)0x7D,(byte)0x5A,(byte)0x09,(byte)0x75,(byte)0xFC,
		(byte)0x2C,(byte)0x30,(byte)0x57,(byte)0xEE,(byte)0xF6,(byte)0x75,(byte)0x30,(byte)0x41,
		(byte)0x7A,(byte)0xFF,(byte)0xE7,(byte)0xFB,(byte)0x80,(byte)0x55,(byte)0xC1,(byte)0x26,
		(byte)0xDC,(byte)0x5C,(byte)0x6C,(byte)0xE9,(byte)0x4A,(byte)0x4B,(byte)0x44,(byte)0xF3,
		(byte)0x30,(byte)0xB5,(byte)0xD9,(byte)0x83,(byte)0x20,(byte)0x26,(byte)0xDC,(byte)0x5C,
		(byte)0x6C,(byte)0xE9,(byte)0x4A,(byte)0x4B,(byte)0x44,(byte)0xF3,(byte)0x30,(byte)0xB5,
		(byte)0xD9,(byte)0xBB,(byte)0xD7,(byte)0x7C,(byte)0xBF,(byte)0x95,(byte)0x84,(byte)0x16,
		(byte)0x29,(byte)0x5C,(byte)0xF7,(byte)0xE1,(byte)0xCE,(byte)0x6B,(byte)0xCC,(byte)0xDC,
		(byte)0x18,(byte)0xFF,(byte)0x8C,(byte)0x07,(byte)0xB6,(byte)0x84,(byte)0x41,(byte)0x04,
		(byte)0x8B,(byte)0xD2,(byte)0xAE,(byte)0xB9,(byte)0xCB,(byte)0x7E,(byte)0x57,(byte)0xCB,
		(byte)0x2C,(byte)0x4B,(byte)0x48,(byte)0x2F,(byte)0xFC,(byte)0x81,(byte)0xB7,(byte)0xAF,
		(byte)0xB9,(byte)0xDE,(byte)0x27,(byte)0xE1,(byte)0xE3,(byte)0xBD,(byte)0x23,(byte)0xC2,
		(byte)0x3A,(byte)0x44,(byte)0x53,(byte)0xBD,(byte)0x9A,(byte)0xCE,(byte)0x32,(byte)0x62,
		(byte)0x54,(byte)0x7E,(byte)0xF8,(byte)0x35,(byte)0xC3,(byte)0xDA,(byte)0xC4,(byte)0xFD,
		(byte)0x97,(byte)0xF8,(byte)0x46,(byte)0x1A,(byte)0x14,(byte)0x61,(byte)0x1D,(byte)0xC9,
		(byte)0xC2,(byte)0x77,(byte)0x45,(byte)0x13,(byte)0x2D,(byte)0xED,(byte)0x8E,(byte)0x54,
		(byte)0x5C,(byte)0x1D,(byte)0x54,(byte)0xC7,(byte)0x2F,(byte)0x04,(byte)0x69,(byte)0x97,
		(byte)0x85,(byte)0x20,(byte)0xA9,(byte)0xFB,(byte)0x57,(byte)0xDB,(byte)0xA1,(byte)0xEE,
		(byte)0xA9,(byte)0xBC,(byte)0x3E,(byte)0x66,(byte)0x0A,(byte)0x90,(byte)0x9D,(byte)0x83,
		(byte)0x8D,(byte)0x71,(byte)0x8C,(byte)0x39,(byte)0x7A,(byte)0xA3,(byte)0xB5,(byte)0x61,
		(byte)0xA6,(byte)0xF7,(byte)0x90,(byte)0x1E,(byte)0x0E,(byte)0x82,(byte)0x97,(byte)0x48,
		(byte)0x56,(byte)0xA7,(byte)0x86,(byte)0x41,(byte)0x04,(byte)0xA0,(byte)0x41,(byte)0xFE,
		(byte)0xB2,(byte)0xFD,(byte)0x11,(byte)0x6B,(byte)0x2A,(byte)0xD1,(byte)0x9C,(byte)0xA6,
		(byte)0xB7,(byte)0xEA,(byte)0xCD,(byte)0x71,(byte)0xC9,(byte)0x89,(byte)0x2F,(byte)0x94,
		(byte)0x1B,(byte)0xB8,(byte)0x8D,(byte)0x67,(byte)0xDC,(byte)0xEE,(byte)0xC9,(byte)0x25,
		(byte)0x01,(byte)0xF0,(byte)0x70,(byte)0x01,(byte)0x19,(byte)0x57,(byte)0xE2,(byte)0x21,
		(byte)0x22,(byte)0xBA,(byte)0x6C,(byte)0x2C,(byte)0xF5,(byte)0xFF,(byte)0x02,(byte)0x93,
		(byte)0x6F,(byte)0x48,(byte)0x2E,(byte)0x35,(byte)0xA6,(byte)0x12,(byte)0x9C,(byte)0xCB,
		(byte)0xBA,(byte)0x8E,(byte)0x93,(byte)0x83,(byte)0x83,(byte)0x6D,(byte)0x31,(byte)0x06,
		(byte)0x87,(byte)0x9C,(byte)0x40,(byte)0x8E,(byte)0xF0,(byte)0x87,(byte)0x01,(byte)0x01,
		(byte)0x5F,(byte)0x20,(byte)0x0E,(byte)0x55,(byte)0x54,(byte)0x53,(byte)0x52,(byte)0x43,
		(byte)0x41,(byte)0x43,(byte)0x43,(byte)0x31,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30,
		(byte)0x31,(byte)0x7F,(byte)0x4C,(byte)0x10,(byte)0x06,(byte)0x0B,(byte)0x2B,(byte)0x06,
		(byte)0x01,(byte)0x04,(byte)0x01,(byte)0x81,(byte)0xC3,(byte)0x1F,(byte)0x03,(byte)0x01,
		(byte)0x01,(byte)0x53,(byte)0x01,(byte)0xC0,(byte)0x5F,(byte)0x25,(byte)0x06,(byte)0x01,
		(byte)0x02,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x09,(byte)0x5F,(byte)0x24,(byte)0x06,
		(byte)0x03,(byte)0x02,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x08,(byte)0x5F,(byte)0x37,
		(byte)0x40,(byte)0x91,(byte)0x4D,(byte)0xD0,(byte)0xFA,(byte)0x00,(byte)0x61,(byte)0x5C,
		(byte)0x44,(byte)0x04,(byte)0x8D,(byte)0x14,(byte)0x67,(byte)0x43,(byte)0x54,(byte)0x00,
		(byte)0x42,(byte)0x3A,(byte)0x4A,(byte)0xD1,(byte)0xBD,(byte)0x37,(byte)0xFD,(byte)0x98,
		(byte)0xD6,(byte)0xDE,(byte)0x84,(byte)0xFD,(byte)0x80,(byte)0x37,(byte)0x48,(byte)0x95,
		(byte)0x82,(byte)0x32,(byte)0x5C,(byte)0x72,(byte)0x95,(byte)0x6D,(byte)0x4F,(byte)0xDF,
		(byte)0xAB,(byte)0xC6,(byte)0xED,(byte)0xBA,(byte)0x48,(byte)0x18,(byte)0x4A,(byte)0x75,
		(byte)0x4F,(byte)0x37,(byte)0xF1,(byte)0xBE,(byte)0x51,(byte)0x42,(byte)0xDD,(byte)0x1C,
		(byte)0x27,(byte)0xD6,(byte)0x65,(byte)0x69,(byte)0x30,(byte)0x8C,(byte)0xE1,(byte)0x9A,
		(byte)0xAF
	};

	
	
	private static final byte[] issuerCert = new byte[] { (byte)0x30, (byte)0x82, (byte)0x02, (byte)0xBC, (byte)0x30, (byte)0x82, (byte)0x02, (byte)0x60, (byte)0xA0, (byte)0x03, (byte)0x02, (byte)0x01, (byte)0x02, (byte)0x02, (byte)0x01, (byte)0x01, (byte)0x30, (byte)0x0C, (byte)0x06, (byte)0x08, (byte)0x2A, (byte)0x86, (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x04, (byte)0x03, (byte)0x02, (byte)0x05, (byte)0x00, (byte)0x30, (byte)0x54, (byte)0x31, (byte)0x2F, (byte)0x30, (byte)0x2D, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04, (byte)0x03, (byte)0x0C, (byte)0x26, (byte)0x43, (byte)0x61, (byte)0x72, (byte)0x64, (byte)0x43, (byte)0x6F, (byte)0x6E, (byte)0x74, (byte)0x61, (byte)0x63, (byte)0x74, (byte)0x20, (byte)0x44, (byte)0x65, (byte)0x76, (byte)0x69, (byte)0x63, (byte)0x65, (byte)0x20, (byte)0x41, (byte)0x75, (byte)0x74, (byte)0x68, (byte)0x65, (byte)0x6E, (byte)0x74, (byte)0x69, (byte)0x63, (byte)0x61, (byte)0x74, (byte)0x69, (byte)0x6F, (byte)0x6E, (byte)0x20, (byte)0x43, (byte)0x41, (byte)0x20, (byte)0x31, (byte)0x31, (byte)0x14, (byte)0x30, (byte)0x12, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04, (byte)0x0A, (byte)0x0C, (byte)0x0B, (byte)0x43, (byte)0x61, (byte)0x72, (byte)0x64, (byte)0x43, (byte)0x6F, (byte)0x6E, (byte)0x74, (byte)0x61, (byte)0x63, (byte)0x74, (byte)0x31, (byte)0x0B, (byte)0x30, (byte)0x09, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04, (byte)0x06, (byte)0x13, (byte)0x02, (byte)0x44, (byte)0x45, (byte)0x30, (byte)0x1E, (byte)0x17, (byte)0x0D, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x5A, (byte)0x17, (byte)0x0D, (byte)0x34, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x5A, (byte)0x30, (byte)0x54, (byte)0x31, (byte)0x2F, (byte)0x30, (byte)0x2D, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04, (byte)0x03, (byte)0x0C, (byte)0x26, (byte)0x43, (byte)0x61, (byte)0x72, (byte)0x64, (byte)0x43, (byte)0x6F, (byte)0x6E, (byte)0x74, (byte)0x61, (byte)0x63, (byte)0x74, (byte)0x20, (byte)0x44, (byte)0x65, (byte)0x76, (byte)0x69, (byte)0x63, (byte)0x65, (byte)0x20, (byte)0x41, (byte)0x75, (byte)0x74, (byte)0x68, (byte)0x65, (byte)0x6E, (byte)0x74, (byte)0x69, (byte)0x63, (byte)0x61, (byte)0x74, (byte)0x69, (byte)0x6F, (byte)0x6E, (byte)0x20, (byte)0x43, (byte)0x41, (byte)0x20, (byte)0x31, (byte)0x31, (byte)0x14, (byte)0x30, (byte)0x12, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04, (byte)0x0A, (byte)0x0C, (byte)0x0B, (byte)0x43, (byte)0x61, (byte)0x72, (byte)0x64, (byte)0x43, (byte)0x6F, (byte)0x6E, (byte)0x74, (byte)0x61, (byte)0x63, (byte)0x74, (byte)0x31, (byte)0x0B, (byte)0x30, (byte)0x09, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x04, (byte)0x06, (byte)0x13, (byte)0x02, (byte)0x44, (byte)0x45, (byte)0x30, (byte)0x82, (byte)0x01, (byte)0x33, (byte)0x30, (byte)0x81, (byte)0xEC, (byte)0x06, (byte)0x07, (byte)0x2A, (byte)0x86, (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x02, (byte)0x01, (byte)0x30, (byte)0x81, (byte)0xE0, (byte)0x02, (byte)0x01, (byte)0x01, (byte)0x30, (byte)0x2C, (byte)0x06, (byte)0x07, (byte)0x2A, (byte)0x86, (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x01, (byte)0x01, (byte)0x02, (byte)0x21, (byte)0x00, (byte)0xA9, (byte)0xFB, (byte)0x57, (byte)0xDB, (byte)0xA1, (byte)0xEE, (byte)0xA9, (byte)0xBC, (byte)0x3E, (byte)0x66, (byte)0x0A, (byte)0x90, (byte)0x9D, (byte)0x83, (byte)0x8D, (byte)0x72, (byte)0x6E, (byte)0x3B, (byte)0xF6, (byte)0x23, (byte)0xD5, (byte)0x26, (byte)0x20, (byte)0x28, (byte)0x20, (byte)0x13, (byte)0x48, (byte)0x1D, (byte)0x1F, (byte)0x6E, (byte)0x53, (byte)0x77, (byte)0x30, (byte)0x44, (byte)0x04, (byte)0x20, (byte)0x7D, (byte)0x5A, (byte)0x09, (byte)0x75, (byte)0xFC, (byte)0x2C, (byte)0x30, (byte)0x57, (byte)0xEE, (byte)0xF6, (byte)0x75, (byte)0x30, (byte)0x41, (byte)0x7A, (byte)0xFF, (byte)0xE7, (byte)0xFB, (byte)0x80, (byte)0x55, (byte)0xC1, (byte)0x26, (byte)0xDC, (byte)0x5C, (byte)0x6C, (byte)0xE9, (byte)0x4A, (byte)0x4B, (byte)0x44, (byte)0xF3, (byte)0x30, (byte)0xB5, (byte)0xD9, (byte)0x04, (byte)0x20, (byte)0x26, (byte)0xDC, (byte)0x5C, (byte)0x6C, (byte)0xE9, (byte)0x4A, (byte)0x4B, (byte)0x44, (byte)0xF3, (byte)0x30, (byte)0xB5, (byte)0xD9, (byte)0xBB, (byte)0xD7, (byte)0x7C, (byte)0xBF, (byte)0x95, (byte)0x84, (byte)0x16, (byte)0x29, (byte)0x5C, (byte)0xF7, (byte)0xE1, (byte)0xCE, (byte)0x6B, (byte)0xCC, (byte)0xDC, (byte)0x18, (byte)0xFF, (byte)0x8C, (byte)0x07, (byte)0xB6, (byte)0x04, (byte)0x41, (byte)0x04, (byte)0x8B, (byte)0xD2, (byte)0xAE, (byte)0xB9, (byte)0xCB, (byte)0x7E, (byte)0x57, (byte)0xCB, (byte)0x2C, (byte)0x4B, (byte)0x48, (byte)0x2F, (byte)0xFC, (byte)0x81, (byte)0xB7, (byte)0xAF, (byte)0xB9, (byte)0xDE, (byte)0x27, (byte)0xE1, (byte)0xE3, (byte)0xBD, (byte)0x23, (byte)0xC2, (byte)0x3A, (byte)0x44, (byte)0x53, (byte)0xBD, (byte)0x9A, (byte)0xCE, (byte)0x32, (byte)0x62, (byte)0x54, (byte)0x7E, (byte)0xF8, (byte)0x35, (byte)0xC3, (byte)0xDA, (byte)0xC4, (byte)0xFD, (byte)0x97, (byte)0xF8, (byte)0x46, (byte)0x1A, (byte)0x14, (byte)0x61, (byte)0x1D, (byte)0xC9, (byte)0xC2, (byte)0x77, (byte)0x45, (byte)0x13, (byte)0x2D, (byte)0xED, (byte)0x8E, (byte)0x54, (byte)0x5C, (byte)0x1D, (byte)0x54, (byte)0xC7, (byte)0x2F, (byte)0x04, (byte)0x69, (byte)0x97, (byte)0x02, (byte)0x21, (byte)0x00, (byte)0xA9, (byte)0xFB, (byte)0x57, (byte)0xDB, (byte)0xA1, (byte)0xEE, (byte)0xA9, (byte)0xBC, (byte)0x3E, (byte)0x66, (byte)0x0A, (byte)0x90, (byte)0x9D, (byte)0x83, (byte)0x8D, (byte)0x71, (byte)0x8C, (byte)0x39, (byte)0x7A, (byte)0xA3, (byte)0xB5, (byte)0x61, (byte)0xA6, (byte)0xF7, (byte)0x90, (byte)0x1E, (byte)0x0E, (byte)0x82, (byte)0x97, (byte)0x48, (byte)0x56, (byte)0xA7, (byte)0x02, (byte)0x01, (byte)0x01, (byte)0x03, (byte)0x42, (byte)0x00, (byte)0x04, (byte)0x4C, (byte)0x01, (byte)0xEA, (byte)0x36, (byte)0xC5, (byte)0x06, (byte)0x5F, (byte)0xF4, (byte)0x7E, (byte)0x8F, (byte)0x06, (byte)0x76, (byte)0xA7, (byte)0x7C, (byte)0xDC, (byte)0xED, (byte)0x6C, (byte)0x8F, (byte)0x74, (byte)0x5E, (byte)0x67, (byte)0x84, (byte)0xF7, (byte)0x80, (byte)0x7F, (byte)0x55, (byte)0x20, (byte)0x12, (byte)0x4F, (byte)0x81, (byte)0xED, (byte)0x05, (byte)0x41, (byte)0x12, (byte)0xDC, (byte)0xE4, (byte)0x71, (byte)0xCA, (byte)0x00, (byte)0x34, (byte)0x42, (byte)0x83, (byte)0x0A, (byte)0x10, (byte)0xC7, (byte)0x5B, (byte)0x31, (byte)0xF9, (byte)0xBF, (byte)0xAD, (byte)0xD6, (byte)0x06, (byte)0x28, (byte)0xF4, (byte)0x71, (byte)0x31, (byte)0x62, (byte)0x8C, (byte)0x72, (byte)0x54, (byte)0xAD, (byte)0x8B, (byte)0x95, (byte)0x6A, (byte)0xA3, (byte)0x45, (byte)0x30, (byte)0x43, (byte)0x30, (byte)0x0E, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x1D, (byte)0x0F, (byte)0x01, (byte)0x01, (byte)0xFF, (byte)0x04, (byte)0x04, (byte)0x03, (byte)0x02, (byte)0x01, (byte)0x06, (byte)0x30, (byte)0x12, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x1D, (byte)0x13, (byte)0x01, (byte)0x01, (byte)0xFF, (byte)0x04, (byte)0x08, (byte)0x30, (byte)0x06, (byte)0x01, (byte)0x01, (byte)0xFF, (byte)0x02, (byte)0x01, (byte)0x01, (byte)0x30, (byte)0x1D, (byte)0x06, (byte)0x03, (byte)0x55, (byte)0x1D, (byte)0x0E, (byte)0x04, (byte)0x16, (byte)0x04, (byte)0x14, (byte)0x7A, (byte)0x2F, (byte)0xBB, (byte)0x93, (byte)0x7D, (byte)0xCC, (byte)0xE2, (byte)0x03, (byte)0x81, (byte)0x0F, (byte)0x6E, (byte)0xCE, (byte)0x60, (byte)0x9A, (byte)0xB8, (byte)0xAD, (byte)0xB5, (byte)0xF1, (byte)0x36, (byte)0xB5, (byte)0x30, (byte)0x0C, (byte)0x06, (byte)0x08, (byte)0x2A, (byte)0x86, (byte)0x48, (byte)0xCE, (byte)0x3D, (byte)0x04, (byte)0x03, (byte)0x02, (byte)0x05, (byte)0x00, (byte)0x03, (byte)0x48, (byte)0x00, (byte)0x30, (byte)0x45, (byte)0x02, (byte)0x20, (byte)0x37, (byte)0x33, (byte)0x53, (byte)0xF3, (byte)0x28, (byte)0x12, (byte)0x2D, (byte)0x63, (byte)0xF5, (byte)0x32, (byte)0xB7, (byte)0x6F, (byte)0xFF, (byte)0x2F, (byte)0xF5, (byte)0xB9, (byte)0xDA, (byte)0x50, (byte)0xA7, (byte)0xA5, (byte)0x84, (byte)0xD8, (byte)0x5C, (byte)0xE1, (byte)0x0B, (byte)0x9B, (byte)0x6C, (byte)0xEA, (byte)0xF0, (byte)0xD9, (byte)0xCD, (byte)0x9E, (byte)0x02, (byte)0x21, (byte)0x00, (byte)0x9D, (byte)0x1E, (byte)0x3B, (byte)0xBB, (byte)0xBD, (byte)0xF4, (byte)0x02, (byte)0x6E, (byte)0xCC, (byte)0x3D, (byte)0x8A, (byte)0xD3, (byte)0x46, (byte)0x7A, (byte)0x06, (byte)0xA8, (byte)0x33, (byte)0x45, (byte)0xB4, (byte)0xA2, (byte)0x28, (byte)0x8E, (byte)0xCD, (byte)0x86, (byte)0x2A, (byte)0x8B, (byte)0xF2, (byte)0x05, (byte)0xD5, (byte)0x95, (byte)0xCE, (byte)0xE9};

	

	/* Set Algorithms*/	
	static {
		HashMap<String, Byte> v15 = new HashMap<String, Byte>();		
		v15.put("SHA1withRSA", (byte)0x31);
		v15.put("SHA256withRSA", (byte)0x33);				

		HashMap<String, Byte> pss = new HashMap<String, Byte>();
		pss.put("SHA1withRSA", (byte)0x41);
		pss.put("SHA256withRSA", (byte)0x43);

		HashMap<String, Byte> none = new HashMap<String, Byte>();
		none.put("NONEwithRSA", (byte)0x20);
		none.put("NONEwithECDSA", (byte)0x70);

		none.put("SHA1withECDSA", (byte)0x71);
		none.put("SHA224withECDSA", (byte)0x72);
		none.put("SHA256withECDSA", (byte)0x73);

		none.put("DEFAULT_ALGORITHM", (byte)0xA0);


		HashMap<String, Byte> defaultAlg = new HashMap<String, Byte>();
		defaultAlg.put("SHA1withRSA", (byte)0x31);
		defaultAlg.put("SHA256withRSA", (byte)0x33);	
		defaultAlg.put("NONEwithRSA", (byte)0x20);
		defaultAlg.put("NONEwithECDSA", (byte)0x70);		
		defaultAlg.put("SHA1withECDSA", (byte)0x71);
		defaultAlg.put("SHA224withECDSA", (byte)0x72);
		defaultAlg.put("SHA256withECDSA", (byte)0x73);		
		defaultAlg.put("DEFAULT_ALGORITHM", (byte)0xA0);

		ALGORITHM_PADDING.put("PKCS1_V15", v15);
		ALGORITHM_PADDING.put("PKCS1_PSS", pss);
		ALGORITHM_PADDING.put("NONE", none);
		ALGORITHM_PADDING.put("DEFAULT", defaultAlg);		
	}


	
	public SmartCardHSMCardService() {
		super();
	}


	
	/**
	 * Enable or disable the pin pad
	 * @param usePinPad
	 */
	public void useClassThreePinPad(boolean usePinPad) {
		this.usePinPad = usePinPad;
	}


	
	/**
	 * Calculate credential and set the flag for secure messaging
	 * 
	 * @throws CardServiceException
	 * @throws CardTerminalException
	 */
	public void initSecureMessaging() throws CardServiceException, CardTerminalException {
	
		ECPublicKey devAutPubKey = getDevAutPK();		

		EAC20 eac = new EAC20(this, devAutPubKey);
		credential = eac.performChipAuthentication();

		doSecureMessaging = true;
	}


	
	/**
	 * Returns true if the card terminal has a pin pad.
	 * @return true if class 3 card terminal
	 */
	private boolean hasSendVerifiedCommandAPDU(){
		allocateCardChannel();
		opencard.core.terminal.CardTerminal ct = getCardChannel().getCardTerminal();
		boolean hasSendVerifiedCommandAPDU = ct instanceof VerifiedAPDUInterface;

		if (ct instanceof ExtendedVerifiedAPDUInterface) {
			ExtendedVerifiedAPDUInterface terminal = (ExtendedVerifiedAPDUInterface) ct;
			hasSendVerifiedCommandAPDU = terminal.hasSendVerifiedCommandAPDU();
		}
		releaseCardChannel();
		return hasSendVerifiedCommandAPDU;
	}

	

	/**
	 * Send a command with secure messaging to the card.
	 * @param com the command apdu
	 * @return
	 */
	private ResponseAPDU sendSecMsgCommand(CommandAPDU com) {
		ResponseAPDU res = null;

		SecureChannel sc = credential.getSecureChannel();

		com = sc.wrap(com, credential.getUsageQualifier());

		try {
			res = sendCommandAPDU(AID, com);
		} catch (CardTerminalException e) {
			log.fine(e.getLocalizedMessage());
		} catch (CardServiceException e) {
			log.fine(e.getLocalizedMessage());
		}	
		if (res.getLength() == 2) {
			return res;
		}
		res = sc.unwrap(res, credential.getUsageQualifier());
		return res;
	}

	

	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public void closeApplication(SecurityDomain domain)
	throws CardServiceException, CardTerminalException {		
	}



	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public int getPasswordLength(SecurityDomain domain, int number)
	throws CardServiceException, CardTerminalException {
		return 0;
	}



	/**
	 * Get password from a callback mechanism or from a terminal pin pad
	 * and send it to the card.
	 * This method uses default CHVControl settings.
	 * @return true if verification was successful
	 * @throws CardServiceException
	 * @throws CardTerminalException
	 */
	public boolean verifyPassword() throws CardServiceException, CardTerminalException {
		boolean verified = false;
		CardTerminalIOControl ioctl = 
			new CardTerminalIOControl(0, 30, CardTerminalIOControl.IS_NUMBERS, "" );
		CHVControl cc =
			new CHVControl( "Enter your password", 1, CHVEncoder.STRING_ENCODING, 0, ioctl);

		// Check if the card is already verified
		verified = getSecurityStatus();
		if (verified) {
			return true;
		}
		
		if (usePinPad && hasSendVerifiedCommandAPDU()) {
			// verify pin with the terminal's pin pad
			// after that the secure channel will be 
			// re-established if doSecureChannel is set to true
			verified = verifyPassword(null, 0, cc, null);
		} else {
			// Obtain the pin from a given callback.
			// The default callback prompt a gui.
			String password = this.getCHVDialog().getCHV(-1);
			byte[] passbytes = CHVUtils.encodeCHV(cc, password);
			verified = this.verifyPassword(null, 0, passbytes);
		}

		return verified;		
	}

	
	
	/**
	 * Get the card's security status
	 * 
	 * @return true if the card is in a verified state, false otherwise
	 * @throws CardServiceException
	 * @throws CardTerminalException
	 */
	public boolean getSecurityStatus() throws CardServiceException, CardTerminalException {
		boolean result = false;
		CommandAPDU com = new CommandAPDU(40);
		ResponseAPDU res = new ResponseAPDU(2);

		com.setLength(0);
		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_VERIFY);
		com.append((byte)0);
		com.append((byte)0x81);	//Local PIN 1

		if (doSecureMessaging) {
			com.append((byte)0x00);
			res = sendSecMsgCommand(com);
		} else {
			res = sendCommandAPDU(AID, com);
		}

		if (res.sw() == IsoConstants.RC_OK) {
			result = true;
		} else if ((res.sw() & 0xFFF0) == IsoConstants.RC_WARNING0LEFT) {
			result = false;
		} else {
			throw new CardServiceUnexpectedStatusWordException("VERIFY" ,res.sw());
		}
		return result;
	}
	
	

	/**
	 * @param domain not in use, set to null
	 * @param number not in use, set to 0
	 * @param password The password data that has to be verified.
	 *        If password is null, the card will return their actual security status.
	 */
	@Override
	public boolean verifyPassword(SecurityDomain domain, int number,
			byte[] password) throws CardServiceException, CardTerminalException {
		
		if (getSecurityStatus()) {
			return true;
		}
		
		boolean result = false;
		//CardChannel channel;
		CommandAPDU com = new CommandAPDU(40);
		ResponseAPDU res = new ResponseAPDU(2);

		com.setLength(0);
		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_VERIFY);
		com.append((byte)0);
		com.append((byte)0x81);	//Local PIN 1

		if (password == null) {
			// Obtain the pin from a given callback.
			// The default callback prompt a gui.
			CardTerminalIOControl ioctl = 
				new CardTerminalIOControl(0, 30, CardTerminalIOControl.IS_NUMBERS, "" );
			CHVControl cc =
				new CHVControl( "Enter your password", 1, CHVEncoder.STRING_ENCODING, 0, ioctl);
			DefaultCHVDialog dialog = new DefaultCHVDialog();
			String passString = dialog.getCHV(-1);
			if (passString == null) {
				return false;
			}
			password = CHVUtils.encodeCHV(cc, passString);
		}

		com.append((byte)password.length);
		System.arraycopy(password, 0, com.getBuffer(), 5, password.length);
		com.setLength(5 + password.length);

		if (doSecureMessaging) {
			com.append((byte)0x00);
			res = sendSecMsgCommand(com);
		} else {
			res = sendCommandAPDU(AID, com);
		}

		if (res.sw() == IsoConstants.RC_OK) {
			result = true;
		} else if ((res.sw() & 0xFFF0) == IsoConstants.RC_WARNING0LEFT) {
			result = false;
		} else {
			throw new CardServiceUnexpectedStatusWordException("VERIFY" ,res.sw());
		}
		return result;
	}



	/**
	 * If there is a class 3 card terminal the pin will be entered on the terminal's pin pad.
	 * Otherwise a callback mechanism will be used.
	 * To guarantee the functionality of the class 3 terminal the command apdu will never send with 
	 * secure messaging.
	 * 
	 * @param domain not in use, set to null
	 * @param number not in use, set to 0
	 * @param password not in use, set to null
	 */
	@Override
	public boolean verifyPassword(SecurityDomain domain, int number,
			CHVControl cc, byte[] password) throws CardServiceException,
			CardTerminalException {

		boolean result = false;
		CommandAPDU com = new CommandAPDU(40);
		ResponseAPDU res = new ResponseAPDU(2);

		com.setLength(0);
		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_VERIFY);
		com.append((byte)0);
		com.append((byte)0x81);	//Local PIN 1

		try {
			res = sendVerifiedAPDU(getChannel(), AID, com, cc, -1);			
		} catch (CardServiceException e) {
			throw e;
		} finally {
			releaseCardChannel();
		}

		if (doSecureMessaging) {

			/* re-establish secure channel
			 * because it broke up by sending 
			 * a plain apdu to the card
			 */
			doSecureMessaging = false; // ensure that manage se and general authenticate using plain apdus 
			initSecureMessaging();
		}

		if (res.sw() == IsoConstants.RC_OK) {
			result = true;
		} else if ((res.sw() & 0xFFF0) == IsoConstants.RC_WARNING0LEFT) {
			result = false;
		} else {
			throw new CardServiceUnexpectedStatusWordException("VERIFY" ,res.sw());
		}
		return result;
	}
	
	
	/**
	 * @param domain not in use, set to null
	 * @param number not in use, set to 0
	 */
	@Override
	public PasswordStatus getPasswordStatus(SecurityDomain domain, int number)
			throws CardServiceException, CardTerminalException {
		PasswordStatus status;
		CommandAPDU com = new CommandAPDU(40);
		ResponseAPDU res = new ResponseAPDU(2);

		com.setLength(0);
		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_VERIFY);
		com.append((byte)0);
		com.append((byte)0x81);	//Local PIN 1

		if (doSecureMessaging) {
			com.append((byte)0x00);
			res = sendSecMsgCommand(com);
		} else {
			res = sendCommandAPDU(AID, com);
		}

		if (res.sw() == IsoConstants.RC_OK) {
			status = PasswordStatus.VERIFIED;
		} else if (res.sw() == IsoConstants.RC_WARNING1LEFT) {
			status = PasswordStatus.LASTTRY;
		} else if (res.sw() == IsoConstants.RC_WARNING2LEFT) {
			status = PasswordStatus.RETRYCOUNTERLOW;
		} else if ((res.sw() & 0xFFF0) == IsoConstants.RC_WARNING0LEFT) {
			status = PasswordStatus.NOTVERIFIED;
		} else if (res.sw() == IsoConstants.RC_AUTHMETHLOCKED) {
			status = PasswordStatus.BLOCKED;
		} else if (res.sw() == IsoConstants.RC_REFDATANOTUSABLE) {
			status = PasswordStatus.NOTINITIALIZED;
		} else {
			throw new CardServiceUnexpectedStatusWordException("VERIFY" ,res.sw());
		}
		return status;
	}
	
	
	
	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public void appendRecord(CardFilePath file, byte[] data)
	throws CardServiceException, CardTerminalException {

		throw new CardServiceInabilityException("appendRecord() ist not implemented");
	}



	/**
	 * Determine if file exists.
	 * 
	 * @param file the path to the file
	 * @return true or false if file doesn't exist
	 * 
	 * @see opencard.opt.iso.fs.FileAccessCardService#exists(CardFilePath)
	 */
	@Override
	public boolean exists(CardFilePath file) throws CardServiceException,
	CardTerminalException {

		try	{
			getFileInfo(file);			
		}
		catch(CardServiceObjectNotAvailableException e) {
			return false;
		}
		return true;
	}



	/**
	 * Queries information about a file. If the file doesn't exists throws a CardServiceObjectNotAvailableException
	 * 
	 * @return information about the file
	 * @throws CardServiceObjectNotAvailableException if the file doesn't exists
	 * @see opencard.opt.iso.fs.FileAccessCardService#getFileInfo(opencard.opt.iso.fs.CardFilePath)
	 */
	@Override
	public CardFileInfo getFileInfo(CardFilePath file)
	throws CardServiceException, CardTerminalException {

		CommandAPDU com = new CommandAPDU(32);
		ResponseAPDU rsp;

		//Enumeration e = file.components();
		//Object path = e.nextElement();
		Object path = file.tail();
		boolean isAID = path instanceof CardFileAppID;

		byte[] pathBytes;

		if (isAID) {
			pathBytes = ((CardFileAppID)path).toByteArray();
		} else {
			pathBytes = ((CardFileFileID)path).toByteArray();
		}


		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_SELECT_FILE);
		com.append(isAID? IsoConstants.SC_AID : IsoConstants.SC_EF);
		com.append(IsoConstants.SO_RETURNFCI);
		com.append((byte)pathBytes.length);
		com.append(pathBytes);
		//com.append((byte)0x02);
		//CardFileFileID fid = (CardFileFileID)file.tail();
		//com.append(fid.toByteArray());
		com.append((byte)0x00);

		if (doSecureMessaging) {
			if (isAID) {
				// select with plain APDU and re-establish secure channel
				rsp = sendCommandAPDU(AID, com);
				initSecureMessaging();
			} else {
				rsp = sendSecMsgCommand(com);	
			}			
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() == 0x6A82) {
			throw new CardServiceObjectNotAvailableException(path + " not found.");
		}

		return new IsoFileControlInformation(rsp.data());	
	}



	/**
	 * Return the application path.
	 * 
	 * @see opencard.opt.iso.fs.FileAccessCardService#getRoot()
	 */
	@Override
	public CardFilePath getRoot() {
		return mf;
	}



	/**
	 * READ BINARY
	 * 
	 * @param file the path to the file
	 * @param offset
	 * @param length
	 */
	@Override
	public byte[] read(CardFilePath file, int offset, int length)
	throws CardServiceException, CardTerminalException {

		CommandAPDU com = new CommandAPDU(14);
		ResponseAPDU rsp = null;
		int chunksize = MAX_CHUNK_SIZE_READ;

		// Check parameter
		if ((offset < 0) || ((length != READ_SEVERAL) && (length < 0))) {
			throw new CardServiceInvalidParameterException
			("read: offset = " + offset + ", length = " + length);
		}

		/*
		 * For Certgate Micro SD card we will get problems if the response is bigger than 500 bytes
		 * Therefore we us a short chunk size here
		 */
		if (this.getChannel().getCardTerminal() instanceof CGMicroSDCardTerminal) {			
			chunksize = MAX_CHUNK_SIZE_MICROSD;
		}
		
		releaseCardChannel();
		
		/*
		 * Read the data in parts of at least 1000 bytes 
		 * to ensure that the length of bytes doesn't exceed 
		 * the card terminals extended length apdu capabilities.
		 */
		if (length == READ_SEVERAL || length > chunksize) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] part;
			offset = 0;
			do {
				part = read(file, offset, chunksize);

				/*
				 * The byte array's length can only be less or equal 1000.
				 * If it's less, the end of file has been reached.
				 */
				if (part.length == chunksize) {
					buffer.write(part, 0, part.length);
					offset += chunksize;
				} else {					
					assert(part[part.length - 2] == 0x62);
					assert(part[part.length - 1] == (byte) 0x82);

					// In case of end of file, the SW '6282' is append at the buffer. Remove it
					buffer.write(part, 0, part.length - 2);
					return buffer.toByteArray();
				}
			} while(true);						
		} 

		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_READ_BINARY_ODD);		
		CardFileFileID fid = (CardFileFileID)file.tail();	//p1 = msb fid, p2 = lsb fid
		com.append(fid.toByteArray());
		com.append((byte)0x00);								// Three byte Lc
		com.append((byte)0x00);
		com.append((byte)0x04);
		com.append((byte)0x54);								// Data
		com.append((byte)0x02);
		com.append((byte)(offset >> 8));
		com.append((byte)offset);
		com.append((byte)(length >> 8));					//Le
		com.append((byte)length);			


		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() == IsoConstants.RC_EOF) {
			return rsp.getBytes();
		}
		if (rsp.sw() != IsoConstants.RC_OK) {
			throw new CardServiceUnexpectedStatusWordException("READ BINARY" ,rsp.sw());
		}
		//return rsp.getBytes();
		return rsp.data(); // TODO testing (changed return buffer)

	}



	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public byte[] readRecord(CardFilePath file, int recordNumber)
	throws CardServiceException, CardTerminalException {

		throw new CardServiceInabilityException("readRecord(CardFilePath file, int recordNumber) is not implemented");
	}



	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public byte[][] readRecords(CardFilePath file, int number)
	throws CardServiceException, CardTerminalException {

		throw new CardServiceInabilityException("readRecords(CardFilePath file, int number) is not implemented");
	}



	/**
	 * Not implemented, use write(CardFilePath file, int offset, byte[] data)
	 * 
	 * @deprecated
	 */
	@Override
	public void write(CardFilePath file, int foffset, byte[] source,
			int soffset, int length) throws CardServiceException,
			CardTerminalException {

		throw new CardServiceInabilityException("write(CardFilePath file, int foffset, byte[] source, int soffset, int length) is not implemented");
	}



	/**
	 * @param file the path to the file
	 * @param offset 
	 * @param data
	 */
	@Override
	public void write(CardFilePath file, int offset, byte[] data)
	throws CardServiceException, CardTerminalException {

		CommandAPDU com; 
		ResponseAPDU rsp = null;
		int chunksize = MAX_CHUNK_SIZE_WRITE;
		
		// Check parameter
		if ((offset < 0) || offset >  0xFFFF) {
			throw new CardServiceInvalidParameterException
			("write: offset = " + offset);
		}

		/*
		 * For Certgate Micro SD card we will get problems if the response is bigger than 500 bytes
		 * Therefore we us a short chunk size here
		 */
		if (this.getChannel().getCardTerminal() instanceof CGMicroSDCardTerminal) {			
			chunksize = MAX_CHUNK_SIZE_MICROSD;
		}
		
		releaseCardChannel();
		
		/*
		 * Update the card's EF in parts of at least 994 bytes 
		 * to ensure that the length of bytes doesn't exceed 
		 * the card terminals extended length apdu capabilities.
		 */
		if (data != null && data.length > chunksize) {
			int length = data.length;
			byte[] part;
			offset = 0;
			do {
				if (length > chunksize) {
					part = new byte[chunksize];
				} else {
					part = new byte[length];
				}
				System.arraycopy(data, offset, part, 0, part.length);
				write(file, offset, part);
				offset += part.length;
				length -= part.length;
			} while(length != 0);

			return;
		} 

		// Wrap c-data
		ByteArrayOutputStream cdata = new ByteArrayOutputStream();
		cdata.write((byte)0x54);
		cdata.write((byte)0x02);
		cdata.write((byte)(offset >> 8));
		cdata.write((byte)offset);
		cdata.write((byte)0x53);
		if (data == null) {
			cdata.write(0);
		} else {
			ByteArrayOutputStream dataLength = new ByteArrayOutputStream();
			lengthToByteArrayOutputStream(data.length, dataLength);
			try {
				cdata.write(dataLength.toByteArray());
				cdata.write(data);
			} catch (IOException e) {
				log.fine(e.getLocalizedMessage());
			}			
		}
		// Create command apdu

		ByteArrayOutputStream capdu = new ByteArrayOutputStream();
		capdu.write((byte)IsoConstants.CLA_ISO);
		capdu.write((byte)IsoConstants.INS_UPDATE_BINARY_ODD);

		CardFileFileID fid = (CardFileFileID)file.tail();
		try {
			capdu.write(fid.toByteArray());			//p1 = msb fid, p2 = lsb fid
		} catch (IOException e) {
			log.fine(e.getLocalizedMessage());
		}

		int length = cdata.size();
		capdu.write((byte)0x00);					//three byte length field
		capdu.write((byte)(length >> 8));
		capdu.write((byte)length);
		try {
			capdu.write(cdata.toByteArray());
		} catch (IOException e) {
			log.fine(e.getLocalizedMessage());
		}
		com = new CommandAPDU(capdu.size());
		System.arraycopy(capdu.toByteArray(), 0, com.getBuffer(), 0, capdu.size());
		com.setLength(capdu.size());

		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() != 0x9000) {
			throw new CardServiceUnexpectedStatusWordException("UPDATE BINARY", rsp.sw());
		}
		if (rsp.getLength() > 2) {
			throw new CardServiceUnexpectedResponseException("No response expected");
		}		
	}



	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public void writeRecord(CardFilePath file, int recordNumber, byte[] data)
	throws CardServiceException, CardTerminalException {
		throw new CardServiceInabilityException("writeRecord() is not implemented");
	}



	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public void provideCredentials(SecurityDomain domain, CredentialBag creds)
	throws CardServiceException {

		//throw new CardServiceInabilityException("provideCredentials() is not implemented");
	}



	/**
	 * Helper function for getSize() and getLengthFieldSize()
	 * 
	 * @param length
	 * @return
	 */		
	protected static int getLengthFieldSizeHelper(int length) {
		int size = 1;		
		if (length >= 0x80)
			size++;
		if (length >= 0x100)
			size++;
		return size;
	}



	/**
	 * Encode length field in byte array
	 *  
	 * @param length
	 * 		Length to be encoded
	 * @param bos
	 * 		ByteArrayOutputStream to copy length into
	 */
	protected static void lengthToByteArrayOutputStream(int length, ByteArrayOutputStream bos) {
		int size = getLengthFieldSizeHelper(length);
		int i = 0;

		if (size > 1) {
			bos.write((byte)(0x80 | (size - 1)));
			i = (size - 2) * 8;
		}

		for (; i >= 0; i -= 8) {
			bos.write((byte)(length >> i));
		}
	}



	/**
	 * Create a new file.
	 * Internal use of write(CardFilePath path, int offset, byte[] data)
	 * 
	 * @param parent The parent CardFilePath
	 * @param data File identifier encoded as FCP data object
	 */
	@Override
	public void create(CardFilePath parent, byte[] data)
	throws CardServiceException, CardTerminalException {
		if (data.length != 4) throw new CardServiceException("Unknown data encoding");
		String s = ":" + HexString.hexifyShort(data[2], data[3]);
		CardFilePath path = new CardFilePath(s);

		write(path, 0, null);
	}



	/**
	 * Delete elementary files or key objects
	 */
	@Override
	public void delete(CardFilePath file) throws CardServiceException,
	CardTerminalException {

		CommandAPDU com = new CommandAPDU(7);
		ResponseAPDU rsp;

		CardFileFileID data = (CardFileFileID)file.tail();
		byte[] fid = data.toByteArray();

		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_DELETE_FILE);
		com.append((byte)0x02); //Delete EF under current DF
		com.append((byte)0x00); //First occurance
		com.append((byte)0x02); //Lc
		System.arraycopy(fid, 0, com.getBuffer(), com.getLength(), fid.length);
		com.setLength(5 + fid.length);

		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() != 0x9000) {
			throw new CardServiceUnexpectedStatusWordException("DELETE FILE", rsp.sw());
		}
		if (rsp.getLength() > 2) {
			throw new CardServiceUnexpectedResponseException("No response expected");
		}				
	}



	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public void invalidate(CardFilePath file)
	throws CardServiceInabilityException, CardServiceException,
	CardTerminalException {
		throw new CardServiceInabilityException("invalidate(CardFilePath file) is not implemented");
	}



	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public void rehabilitate(CardFilePath file)
	throws CardServiceInabilityException, CardServiceException,
	CardTerminalException {
		throw new CardServiceInabilityException("rehabilitate(CardFilePath file) is not implemented");
	}


	
	/**
	 * Get both passwords, the current password and the new one from a callback mechanism
	 * and send it to the card. 
	 * This method uses default CHVControl settings.
	 * @return true if verification was successful
	 * @throws CardServiceException
	 * @throws CardTerminalException
	 */
	public boolean changeReferenceData() throws CardServiceException, CardTerminalException {
		boolean modiefied;
		CardTerminalIOControl ioctl = 
			new CardTerminalIOControl(0, 30, CardTerminalIOControl.IS_NUMBERS, "" );
		CHVControl cc =
			new CHVControl( "Enter your password", 1, CHVEncoder.STRING_ENCODING, 0, ioctl);

		//TODO There is no useful prompting
		String currentPassword = this.getCHVDialog().getCHV(-1);
		String newPassword = this.getCHVDialog().getCHV(-1);
		byte[] currentPassbytes = CHVUtils.encodeCHV(cc, currentPassword);
		byte[] newPassbytes = CHVUtils.encodeCHV(cc, newPassword);
		modiefied = changeReferenceData(null, 0x81, null, currentPassbytes, newPassbytes);

		return modiefied;		
	}

	

	/**
	 * Change the User PIN or SO PIN.
	 * 
	 * @param domain Not used
	 * @param number Must be one of 0x81 for User PIN or 0x88 for SO PIN
	 * @param cc Not used
	 * @param currentPassword
	 * @param newPassword
	 * @throws CardServiceException 
	 * @throws CardTerminalException 
	 */
	@Override
	public boolean changeReferenceData(SecurityDomain domain, int number,
			CHVControl cc, byte[] currentPassword, byte[] newPassword) 
	throws CardTerminalException, CardServiceException {

		if ((number != USER_PIN) && (number != SO_PIN)) {
			throw new CardServiceInvalidParameterException("Parameter \"number\" must be one of 0x81 or 0x88");
		}
		
		CommandAPDU com = new CommandAPDU(5 + currentPassword.length + newPassword.length);
		ResponseAPDU res;
		boolean result = false;

		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_CHANGE_CHV);
		com.append((byte)0x0);
		com.append((byte)number); //USER_PIN or SO_PIN
		com.append((byte)(currentPassword.length + newPassword.length));
		com.append(currentPassword);
		com.append(newPassword);
		
		if (doSecureMessaging) {
			res = sendSecMsgCommand(com);
		} else {
			res = sendCommandAPDU(AID, com);
		}

		if (res.sw() == IsoConstants.RC_OK) {
			result = true;
		} else if ((res.sw() & 0xFFF0) == IsoConstants.RC_WARNING0LEFT) {
			result = false;
		} else {
			throw new CardServiceUnexpectedStatusWordException("VERIFY" ,res.sw());
		}
		return result;
	}



	/**
	 * The device is initialized with a User PIN during device initialization. 
	 * If this User PIN is blocked it can be reset 
	 * using the SO PIN (initialization code) of the device.
	 * 
	 * 
	 * @param domain Not in use
	 * @param number Set to local PIN '81'
	 * @param cc Not in use
	 * @param unblockingCode The code to unblock the card
	 * @param newPassword The new password or null
	 * @throws CardServiceException 
	 * @throws CardTerminalException
	 */
	@Override
	public boolean resetRetryCounter(SecurityDomain domain, int number,
			CHVControl cc, byte[] unblockingCode, byte[] newPassword) throws CardTerminalException, CardServiceException {
		
		if ((number != USER_PIN) && (number != SO_PIN)) {
			throw new CardServiceInvalidParameterException("Parameter \"number\" must be one of 0x81 or 0x88");
		}
		
		CommandAPDU com = new CommandAPDU(40);
		ResponseAPDU rsp;
		Boolean result = false;
		
		// CLA
		com.append(IsoConstants.CLA_ISO);
		// INS
		com.append(IsoConstants.INS_UNBLOCK_CHV);
		// If SO PIN followed by new User PIN then P1 is 0x00,
		// otherwise for SO PIN only P1 is 0x01
		com.append(newPassword == null ? (byte)0x01 : (byte)0x00);
		// P2
		com.append((byte)number);
		// Lc
		com.append(newPassword == null ? (byte)0x08 : (byte)(newPassword.length + unblockingCode.length));
		// C-Data
		com.append(unblockingCode);
		if (newPassword != null) {
			com.append(newPassword);
		} 
		//com.append((byte)0);
		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() == IsoConstants.RC_OK) {
			result = true;
		} else if ((rsp.sw() & 0xFFF0) == IsoConstants.RC_WARNING0LEFT) {
			result = false;
		} else {
			throw new CardServiceUnexpectedStatusWordException("VERIFY" ,rsp.sw());
		}
		
		return result;
	}



	/**
	 * Initialize the smartcard.
	 * This clears all cryptographic material and transparent files.
	 * It also sets the user PIN, generate a random Device Key Encryption Key 
	 * and defines the basic configuration options.
	 * 
	 * @param config The configuration options (default '0001')
	 * @param initPin Set the user pin
	 * @param initCode 8 byte code that protects unauthorized re-initialization
	 * @param retryCounter Initial value for the retry counter
	 * @throws CardTerminalException
	 * @throws CardServiceException
	 * @throws TLVEncodingException 
	 */
	public void initialize(byte[] config, byte[] initPin, byte[] initCode, byte retryCounter) 
	throws CardTerminalException, CardServiceException, TLVEncodingException {

		CommandAPDU com = new CommandAPDU(40);
		ResponseAPDU rsp;
		ConstructedTLV data = new ConstructedTLV(0x30);
		data.add(new PrimitiveTLV(0x80, config));
		data.add(new PrimitiveTLV(0x81, initPin));
		data.add(new PrimitiveTLV(0x82, initCode));
		data.add(new PrimitiveTLV(0x91, new byte[] {retryCounter}));

		com.append(IsoConstants.CLA_HSM);
		com.append(IsoConstants.INS_INITIALIZE);
		com.append((byte)0x0); //p1
		com.append((byte)0x0); //p2
		com.append((byte)data.getLength());
		com.append(data.getValue());

		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}
		if (rsp.sw() != IsoConstants.RC_OK) {
			throw new CardServiceUnexpectedStatusWordException("INITIALIZE" ,rsp.sw());
		}
	}

	
	
	/**
	 * Initialize the smartcard.
	 * This clears all cryptographic material and transparent files.
	 * It also sets the user PIN, defines the basic configuration options
	 * and the number of Device Key Encryption Key shares for key wrapping/unwrapping.
	 * 
	 * @param config the configuration options (default '0001')
	 * @param initPin Set the user pin
	 * @param initCode 8 byte code that protects unauthorized re-initialization
	 * @param retryCounter Initial value for the retry counter
	 * @param noOfShares Number of Device Key Encryption Key shares
	 * @throws CardTerminalException
	 * @throws CardServiceException
	 * @throws TLVEncodingException 
	 */
	public void initialize(byte[] config, byte[] initPin, byte[] initCode, byte retryCounter, byte noOfShares) 
	throws CardTerminalException, CardServiceException, TLVEncodingException {

		CommandAPDU com = new CommandAPDU(40);
		ResponseAPDU rsp;
		ConstructedTLV data = new ConstructedTLV(0x30);
		data.add(new PrimitiveTLV(0x80, config));
		data.add(new PrimitiveTLV(0x81, initPin));
		data.add(new PrimitiveTLV(0x82, initCode));
		data.add(new PrimitiveTLV(0x91, new byte[] {retryCounter}));
		data.add(new PrimitiveTLV(0x92, new byte[] {noOfShares}));

		com.append(IsoConstants.CLA_HSM);
		com.append(IsoConstants.INS_INITIALIZE);
		com.append((byte)0x0); //p1
		com.append((byte)0x0); //p2
		com.append((byte)data.getLength());
		com.append(data.getValue());

		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}
		if (rsp.sw() != IsoConstants.RC_OK) {
			throw new CardServiceUnexpectedStatusWordException("INITIALIZE" ,rsp.sw());
		}
	}
	


	/**
	 * Initiate the generation of a fresh key pair for the selected key object.
	 * 
	 * Generating a new key pair requires a successful verification of the User PIN.
	 * @param keyId the ID for the key to be generated
	 * @param signingId  the ID for signing authenticated request
	 * @param spec the AlgorithmParameterSpec containing the domain parameter
	 */
	@Override
	public byte[] generateKeyPair(byte keyId, byte signingId, SmartCardHSMPrivateKeySpec spec) throws CardTerminalException, CardServiceException, TLVEncodingException {

		CommandAPDU com = new CommandAPDU(300);
		ResponseAPDU rsp;
		boolean isMicroSD = false;
		int chunksize = MAX_CHUNK_SIZE_READ;
		
		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_GENERATE_KEYPAIR);

		//P1: Key identifier of the key to be generated
		com.append(keyId); 

		//P2: Key identifier for signing authenticated request
		com.append(signingId); 

		byte[] data;
		try {
			data = spec.getCData();

			//Three byte length field
			int length = data.length;
			com.append((byte)0x00);
			com.append((byte)(length >> 8));
			com.append((byte)length);

			//Copy command data
			System.arraycopy(data, 0, com.getBuffer(), com.getLength(), data.length);
			com.setLength(7 + data.length);
		} catch (IOException e) {
			log.fine(e.getLocalizedMessage());
		}
		
		/*
		 * For Certgate Micro SD card we will get problems if the response is bigger than 500 bytes
		 * Therefore we do not add LE and the card stores the result in an EF
		 */
		if (this.getChannel().getCardTerminal() instanceof CGMicroSDCardTerminal) {			
			isMicroSD = true;
			chunksize = MAX_CHUNK_SIZE_MICROSD;
		} else {
			// Le
			com.append((byte)0x00);
			com.append((byte)0x00);
		}
		
		releaseCardChannel();
		
		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() != 0x9000) {
			throw new CardServiceUnexpectedStatusWordException("GENERATE ASYMMETRIC KEY PAIR", rsp.sw());
		}
		
		// Read the generated CSR from the specific EF using chunks
		if (isMicroSD) {
			data = read(new CardFilePath(":" + HexString.hexify(EECERTIFICATEPREFIX) + HexString.hexify(keyId)), 0, READ_SEVERAL);
		} else {
			data = rsp.data();				
		}
		
		return data; 
	}


	
	/**
	 * Import a single key share of the Device Encryption Key.
	 * 
	 * @return The total number of shares, outstanding shares and the KCV
	 * @throws CardServiceException 
	 * @throws CardTerminalException 
	 */
	public byte[] importDKEKShare(byte[] keyShare) throws CardTerminalException, CardServiceException {
		CommandAPDU com = new CommandAPDU(300);
		ResponseAPDU rsp;
		
		if (keyShare.length != 0x20) {
			throw new CardServiceInvalidParameterException("The DKEK share must have a length of 32 bytes.");
		}
		
		// CLA
		com.append(IsoConstants.CLA_ISO);
		// INS
		com.append((byte)0x52);
		// P1
		com.append((byte)0x00);
		// P2
		com.append((byte)0x00);
		// Lc
		com.append((byte)keyShare.length);
		// C-Data
		com.append(keyShare);
		// Le
		com.append((byte)0x00);		

		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() != 0x9000) {
			throw new CardServiceUnexpectedStatusWordException("GENERATE ASYMMETRIC KEY PAIR", rsp.sw());
		}
		return rsp.data(); 
	}
	
	
	
	/**
	 * The Wrap command allows the terminal to extract a private or secret key value 
	 * encrypted under the Device Key Encryption Key.
	 * 
	 * @param kid The key identifier
	 * @return 
	 * @throws CardServiceException 
	 * @throws CardTerminalException 
	 */
	public byte[] wrapKey(byte kid) throws CardTerminalException, CardServiceException {
		CommandAPDU com = new CommandAPDU(300);
		ResponseAPDU rsp;
						
		// CLA
		com.append((byte)0x80);
		// INS
		com.append((byte)0x72);
		// P1
		com.append((byte)kid);
		// P2
		com.append((byte)WRAP);
		// Le
		com.append((byte)0x00);		
		com.append((byte)0x00);
		com.append((byte)0x00);
		
		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() != 0x9000) {
			throw new CardServiceUnexpectedStatusWordException("WRAP KEY", rsp.sw());
		}
		return rsp.data(); 
	}
	
	
	
	/**
	 * The Unwrap command allows the terminal to import a private or secret key value 
	 * and meta data encrypted under the Device Key Encryption Key.
	 * 
	 * @param kid The key identifier
	 * @throws CardServiceException 
	 * @throws CardTerminalException 
	 */
	public boolean unwrapKey(byte kid, byte[] key) throws CardTerminalException, CardServiceException {
		CommandAPDU com = new CommandAPDU(7 + key.length);
		ResponseAPDU rsp;
						
		// CLA
		com.append((byte)0x80);
		// INS
		com.append((byte)0x74);
		// P1
		com.append((byte)kid);
		// P2
		com.append((byte)UNWRAP);
		// Lc
		com.append((byte)0x00);
		com.append((byte)(key.length >> 8));
		com.append((byte)key.length);
		// D-Data
		com.append(key);
				
		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() != 0x9000) {
			throw new CardServiceUnexpectedStatusWordException("UNWRAP KEY", rsp.sw());
		}
		return true; 
	}
	
	

	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public void generateKeyPair(PrivateKeyRef privateDest,
			PublicKeyRef publicDest, int strength, String keyAlgorithm)
	throws CardServiceException, InvalidKeyException,
	CardTerminalException {
	}



	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public PublicKey readPublicKey(PublicKeyRef pulicKey, String keyAlgorithm)
	throws CardServiceException, InvalidKeyException,
	CardTerminalException {
		return null;
	}



	@Override
	public byte[] signData(PrivateKeyRef privateKey, String signAlgorithm,
			byte[] data) throws CardServiceException, CardTerminalException {

		return signData(privateKey, signAlgorithm, "DEFAULT", data);			
	}



	/**
	 * Create a signature. 
	 */
	@Override
	public byte[] signData(PrivateKeyRef privateKey, String signAlgorithm,
			String padAlgorithm, byte[] data) throws CardServiceException,
			CardTerminalException {

		CommandAPDU com = new CommandAPDU(300);
		ResponseAPDU rsp;

		if (signAlgorithm.contains("RSA") && !(privateKey instanceof SmartCardHSMKey)
				|| (signAlgorithm.contains("ECDSA") && !(privateKey instanceof SmartCardHSMKey))) {
			throw new CardServiceOperationFailedException("Algorithm and key don't match.");
		}				

		if (ALGORITHM_PADDING.containsKey(padAlgorithm)) {
			if (ALGORITHM_PADDING.get(padAlgorithm).containsKey(signAlgorithm)) {					
				com.append((byte)0x80);
				com.append(IsoConstants.INS_SIGN);
				//P1: Key identifier
				int keyNo = ((SmartCardHSMKey)privateKey).getKeyID();
				com.append((byte)keyNo); 
				//P2:Algorithm Identifier
				com.append(ALGORITHM_PADDING.get(padAlgorithm).get(signAlgorithm));
				//Three byte length field
				int length = data.length;
				com.append((byte)0x00);
				com.append((byte)(length >> 8));
				com.append((byte)length);
				//Copy command data
				System.arraycopy(data, 0, com.getBuffer(), com.getLength(), data.length);
				com.setLength(7 + data.length);
				//Le
				com.append((byte)0x00);
				com.append((byte)0x00);

				if (doSecureMessaging) {
					rsp = sendSecMsgCommand(com);
				} else {
					rsp = sendCommandAPDU(AID, com);
				}

				if (rsp.sw() != 0x9000) {
					throw new CardServiceUnexpectedStatusWordException("SIGN", rsp.sw());
				}
				return rsp.data();
			}			
		}
		throw new CardServiceOperationFailedException("There is no matching algorithm.");
	}



	/**	 
	 * Create a signature.
	 * 
	 * If the referenced key type is RSA then the hash will be padded according
	 * to the EMSA-PKCS1-v1_5 encoding.
	 * The data will be send to the card which performs a Plain RSA signature operation.
	 * 
	 * If the key is of type ECC then the hash will be send to the card which performs
	 * a Plain ECDSA operation.
	 * 
	 * @param privateKey the SmartCardHSMKey
	 * @param signAlgorithm String containing the signing algorithm
	 * @param hash
	 */
	@Override
	public byte[] signHash(PrivateKeyRef privateKey, String signAlgorithm,
			byte[] hash) throws CardServiceException, InvalidKeyException,
			CardTerminalException {

		if (signAlgorithm.equals("NONEwithECDSA")) {
			return signData(privateKey, "NONEwithECDSA", hash);
		} else if (signAlgorithm.equals("SHA1withRSA") || signAlgorithm.equals("SHA256withRSA")) {
			return signHash(privateKey, signAlgorithm, "PKCS1_V15", hash);
		} else if (signAlgorithm.equals("NONEwithRSA")) {
			return signHash(privateKey, signAlgorithm, "PKCS1_V15", hash);
		} else {
			throw new CardServiceOperationFailedException("Algorithm for hash object required.");
		}
	}



	/**
	 * Create a signature.
	 * 
	 * If the referenced key is of type RSA the hash will be padded according to
	 * the pad algorithm. 
	 * The data will be send to the card which performs a Plain RSA signature operation.
	 * 
	 * If the key is of type ECC then the hash will be send to the card which performs
	 * a Plain ECDSA operation.
	 * 
	 * @param privateKey the SmartCardHSMKey
	 * @param signAlgorithm String containing the signing algorithm
	 * @param padAlgorithm String containing the padding algorithm
	 * @param hash
	 */
	@Override
	public byte[] signHash(PrivateKeyRef privateKey, String signAlgorithm,
			String padAlgorithm, byte[] hash) throws CardServiceException,
			CardTerminalException {

		if (padAlgorithm.equals("PKCS1_V15")) {
			if (privateKey instanceof SmartCardHSMKey) {
				byte[] em;
				ObjectIdentifier oid = null;
				if (signAlgorithm.equals("SHA1withRSA")) {
					oid = new ObjectIdentifier("1.3.14.3.2.26");
				} else if (signAlgorithm.equals("SHA256withRSA")) {
					oid = new ObjectIdentifier("2.16.840.1.101.3.4.2.1");
				} else if (signAlgorithm.equals("NONEwithRSA")) {
					em = padWithPKCS1v15(hash, ((SmartCardHSMKey)privateKey).getKeySize());
					return signData(privateKey, "NONEwithRSA", "NONE", em);
				} else {
					throw new CardServiceOperationFailedException("There is no matching algorithm.");
				}
				try {
					byte[] digestInfo = buildDigestInfo(oid, hash);
					em = padWithPKCS1v15(digestInfo, ((SmartCardHSMKey)privateKey).getKeySize());
					return signData(privateKey, "NONEwithRSA", "NONE", em);
				} catch (TLVEncodingException e) {
					log.fine(e.getLocalizedMessage());
				}

			}
			else {
				throw new CardServiceOperationFailedException("Algorithm and key don't match.");
			}
		} else if (padAlgorithm.equals("PKCS1_PSS")) {
			if (privateKey instanceof SmartCardHSMKey) {
				String id = "";
				MessageDigest md = null;
				if (signAlgorithm.equals("SHA1withRSA")) {
					id = "SHA1";
				} else if (signAlgorithm.equals("SHA256withRSA")) {
					id = "SHA256";
				} else {
					throw new CardServiceOperationFailedException("There is no matching algorithm.");
				}
				
				try {
					md = MessageDigest.getInstance(id);
				} catch (NoSuchAlgorithmException e) {
					throw new CardServiceOperationFailedException("Unable to get instance of message digest : " + e.getLocalizedMessage());
				}

				EMSAPSSEncoder encoder = new EMSAPSSEncoder(md, ((SmartCardHSMKey) privateKey).getKeySize());
				
				byte[] pssblock = null;
				
				try {
					pssblock = encoder.encode(hash);
				} catch (IOException e) {
					throw new CardServiceOperationFailedException("Unable to create PSS encoding : " + e.getLocalizedMessage());
				}

				
				return signData(privateKey, "NONEwithRSA", "NONE", pssblock);
				
			} else {
				throw new CardServiceOperationFailedException("Algorithm and key don't match.");
			}


		} else if (padAlgorithm.equals("NONE")) {
			if (signAlgorithm.equals("NONEwithECDSA")) {
				if (privateKey instanceof SmartCardHSMKey) {
					hash = verifyHashLength(((SmartCardHSMKey) privateKey).getKeySize(), hash);
					return signData(privateKey, "NONEwithECDSA", "NONE", hash);
				}
				else {
					throw new CardServiceOperationFailedException("Alogrithm and key don't match.");
				}
			} else {
				throw new CardServiceOperationFailedException("There is no matching algorithm.");
			}


		} else {
			throw new CardServiceOperationFailedException("There is no matching algorithm.");
		}

		return null;
	}



	/*
	 * This helper method verifies that the hash length matches the length
	 * of the key order.
	 * 
	 * The hash will be filled up with leading zeros if it is too short 
	 * or it will be shortened MSB first if it is too long.  
	 */
	private byte[] verifyHashLength(int keySize, byte[] hash) {
		int length = keySize / 8;
		byte[] paddedHash = new byte[length];
		if (hash.length == length) {
			// Valid hash
			return hash;
		} else if (hash.length < length) {
			// Hash too short. Fill up hash with leading 0 
			System.arraycopy(hash, 0, paddedHash, paddedHash.length - hash.length, hash.length);
			return paddedHash;
		} else {
			// Hash too long. Shorten hash to key order length - MSB first
			System.arraycopy(hash, 0, paddedHash, 0, paddedHash.length);
			return paddedHash;
		}		
	}



	/*
	 * Helper to encode the hash according to EMSA-PKCS1-v1_5
	 */
	private byte[] buildDigestInfo(ObjectIdentifier oid, byte[] hash) throws TLVEncodingException {

		//build the digest info
		ConstructedTLV digestInfo = new ConstructedTLV(0x30);

		ConstructedTLV algorithmID = new ConstructedTLV(0x30);
		algorithmID.add(oid);
		algorithmID.add(new PrimitiveTLV(Tag.NULL, null));

		PrimitiveTLV digest = new PrimitiveTLV(Tag.OCTET_STRING, hash);

		digestInfo.add(algorithmID);
		digestInfo.add(digest);
		byte[] t = digestInfo.getBytes();

		return t;
	}



	private byte[] padWithPKCS1v15(byte[] t, int keySize) throws CardServiceOperationFailedException {

		int emLen = keySize / 8;			
		if (emLen < t.length + 11) {
			throw new CardServiceOperationFailedException("Intended encoded message length too short.");
		}

		/* pad t into em
		   em = 0x00 || 0x01 || ps || 0<00 || t */
		byte[] em = new byte[emLen];				
		em[0] = 0x00;
		em[1] = 0x01;

		int psLen = emLen - t.length - 3;
		int j = 2;
		for (int i = 0; i < psLen; i++, j++) {
			em[j] = (byte)0xFF;
		}
		em[j] = 0x0;

		System.arraycopy(t, 0, em, j + 1, t.length);
		return em;
	}



	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public boolean verifySignedData(PublicKeyRef publicKey,
			String signAlgorithm, byte[] data, byte[] signature)
	throws CardServiceException, InvalidKeyException,
	CardTerminalException {
		throw new CardServiceInabilityException
		("verifySignedData(PublicKeyRef publicKey, String signAlgorithm, byte[] data, byte[] signature)");
	}



	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public boolean verifySignedData(PublicKeyRef publicKey,
			String signAlgorithm, String padAlgorithm, byte[] data,
			byte[] signature) throws CardServiceException, InvalidKeyException,
			CardTerminalException {
		throw new CardServiceInabilityException
		("verifySignedData(PublicKeyRef publicKey, String signAlgorithm, String padAlgorithm, byte[] data, byte[] signature)");
	}



	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public boolean verifySignedHash(PublicKeyRef publicKey,
			String signAlgorithm, byte[] hash, byte[] signature)
	throws CardServiceException, InvalidKeyException,
	CardTerminalException {
		throw new CardServiceInabilityException
		("verifySignedHash(PublicKeyRef publicKey, String signAlgorithm, byte[] hash, byte[] signature)");
	}



	/**
	 * Not implemented
	 * 
	 * @deprecated
	 */
	@Override
	public boolean verifySignedHash(PublicKeyRef publicKey,
			String signAlgorithm, String padAlgorithm, byte[] hash,
			byte[] signature) throws CardServiceException, InvalidKeyException,
			CardTerminalException {
		throw new CardServiceInabilityException
		("verifySignedHash(PublicKeyRef publicKey, String signAlgorithm, String padAlgorithm, byte[] hash, byte[] signature)");
	}



	/**
	 * Enumerate all currently used file and key identifier.
	 * 
	 * @return Even number of bytes that compose a list of 16 bit file identifier
	 * @throws CardTerminalException
	 * @throws CardServiceException
	 */
	public byte[] enumerateObjects() throws CardTerminalException, CardServiceException {
		CommandAPDU com = new CommandAPDU(7);
		ResponseAPDU rsp;

		com.append(IsoConstants.CLA_HSM);
		com.append(IsoConstants.INS_ENUM_OBJECTS);
		//P1
		com.append((byte)0x00);
		//P2
		com.append((byte)0x00);
		//Three byte Le for extended length APDU
		com.append((byte)0x00);
		com.append((byte)0x00);
		com.append((byte)0x00);

		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() != IsoConstants.RC_OK) {
			throw new CardServiceUnexpectedStatusWordException("ENUMERATE OBJECTS" ,rsp.sw());
		}
		return rsp.data();
	}



	/**
	 * Request random byte values generated by the build in random number generator.
	 * 
	 * @param length
	 * @return Random bytes
	 * @throws CardTerminalException
	 * @throws CardServiceException
	 */
	public byte[] generateRandom(int length) throws CardTerminalException, CardServiceException {
		CommandAPDU com = new CommandAPDU(7);
		ResponseAPDU rsp;

		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_GET_CHALLENGE);
		//P1
		com.append((byte)0x00);
		//P2
		com.append((byte)0x00);
		//Three byte Le for extended length APDU
		com.append((byte)0x00);
		com.append((byte)(length >> 8));
		com.append((byte)length);

		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() != IsoConstants.RC_OK) {
			throw new CardServiceUnexpectedStatusWordException("GET CHALLENGE" ,rsp.sw());
		}
		return rsp.data();
	}
	


	/**
	 * The device decrypts using the private key a cryptogram 
	 * enciphered with the public key and returns the plain value.
	 * 
	 * @param privateKey the private SmartCardHSMKey
	 * @param cryptogram 
	 * @return the plain value
	 */
	@Override
	public byte[] decipher(SmartCardHSMKey privateKey, byte[] cryptogram) throws CardTerminalException, CardServiceException {

		CommandAPDU com = new CommandAPDU(400);
		ResponseAPDU rsp;

		com.append(IsoConstants.CLA_HSM);
		com.append(IsoConstants.INS_DECIPHER);
		//P1: Key Id
		com.append((byte)privateKey.getKeyID());
		//P2: Alg Id
		com.append(NONE_WITH_RSA_DECRIPTION);
		//Lc
		com.append((byte)0x00);
		com.append((byte)(cryptogram.length >> 8));
		com.append((byte)cryptogram.length);
		com.append(cryptogram);
		//Le		
		com.append((byte)0x00);
		com.append((byte)0x00);		

		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() != IsoConstants.RC_OK) {
			throw new CardServiceUnexpectedStatusWordException("DECIPHER" ,rsp.sw());
		}
		return rsp.data();	// changed buffer from getBytes() to data
	}



	/**
	 * The device calculates a shared secret point using an EC Diffie-Hellman
	 * operation. The public key of the sender must be provided as input to the command.
	 * The device returns the resulting point on the curve associated with the private key.
	 * 
	 * @param privateKey Key identifier of the SmartCardHSM private key
	 * @param pkComponents Concatenation of '04' || x || y point coordinates of ECC public Key
	 * @return Concatenation of '04' || x || y point coordinates on EC curve
	 * @throws CardServiceException
	 * @throws CardTerminalException
	 */
	@Override
	public byte[] performECCDH(SmartCardHSMKey privateKey, byte[] pkComponents)
	throws CardServiceException, CardTerminalException {

		CommandAPDU com = new CommandAPDU(200);
		ResponseAPDU rsp;

		com.append(IsoConstants.CLA_HSM);
		com.append(IsoConstants.INS_DECIPHER);
		//P1: Key Id
		com.append((byte)privateKey.getKeyID());
		//P2: Alg Id
		com.append(ECDH);
		//Lc
		com.append((byte)0x00);
		com.append((byte)(pkComponents.length >> 8));
		com.append((byte)pkComponents.length);
		com.append(pkComponents);
		//Le		
		com.append((byte)0x00);
		com.append((byte)0x00);		

		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() != IsoConstants.RC_OK) {
			throw new CardServiceUnexpectedStatusWordException("PERFORM ECCDH" ,rsp.sw());
		}
		return rsp.data();	// changed buffer from getBytes() to data
	}

	

	/**
	 * Select algorithms and keys for security operations.
	 * 
	 * @param data
	 * @return
	 * @throws InvalidCardChannelException
	 * @throws CardTerminalException
	 * @throws CardServiceException
	 */
	public byte[] manageSE(byte[] data) throws CardTerminalException, CardServiceException {

		CommandAPDU com = new CommandAPDU(100);
		ResponseAPDU rsp;

		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_MANAGE_SE);
		com.append((byte)(IsoConstants.UQ_COM_DEC_INTAUT |	IsoConstants.P1_MSE_SET));	// SET for computation, verification, decipherment and key agreement
		com.append(IsoConstants.CRT_AT);	// CRT for authentication
		com.append((byte)0x0C);
		com.append(data);

		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() != IsoConstants.RC_OK) {
			throw new CardServiceUnexpectedStatusWordException("MANAGE SE" ,rsp.sw());
		}
		return rsp.data();	
	}

	

	/**
	 * The GENERAL AUTHENTICATE command allows the terminal to perform an 
	 * explicit authentication of the device and 
	 * agree secret session keys KS_ENC and KS_MAC for secure messaging.
	 * 
	 * @param data the dynamic authentication data template
	 * @return Dynamic Authentication Template
	 * @throws CardTerminalException
	 * @throws CardServiceException
	 */
	public byte[] generalAuthenticate(byte[] data) throws CardTerminalException, CardServiceException {

		CommandAPDU com = new CommandAPDU(100);
		ResponseAPDU rsp;

		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_GENERAL_AUTH1);
		com.append((byte)0x00);			// P1
		com.append((byte)0x00);			// P2
		com.append((byte)data.length);	// Lc
		com.append(data);				// C-Data
		com.append((byte)0x00);			// Le

		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		if (rsp.sw() != IsoConstants.RC_OK) {
			throw new CardServiceUnexpectedStatusWordException("GENERAL AUTHENTICATE" ,rsp.sw());
		}
		return rsp.data();	
	}


	
	/**
	 * Return a Vector containing all aliases 
	 * that are used on the SmartCardHSM.
	 * 
	 * @return Vector of aliases 
	 * @throws TLVEncodingException 
	 * @throws CertificateException 
	 * @throws CardServiceException 
	 * @throws CardTerminalException 
	 */
	public Vector<String> getAliases() throws CardTerminalException, CardServiceException, CertificateException, TLVEncodingException {
		if (this.namemap.isEmpty()) {
				enumerateEntries();
		}		
		Set<String> set = this.namemap.keySet();
		Vector<String> v = new Vector<String>(set);
		return v;
	}
	
	
	
	/**
	 * Add a new key to the map of keys
	 * @param key the SmartCardHSMKey
	 */
	public void addKeyToMap(SmartCardHSMKey key) {
		String label = key.getLabel();
		byte id = key.getKeyID();

		SmartCardHSMEntry entry = namemap.get(label);
		if (entry == null) {
			entry = new SmartCardHSMEntry(key);
			namemap.put(label, entry);
		} else {
			entry.setKey(key);
		}

		idmap.put(id, key);		
	}



	/**
	 * Add a certificate to the map
	 * 
	 * @param cert the certificate
	 * @param isEECertificate true for EE certificates, false for CA certificates
	 * @param id
	 * @param label
	 */
	public void addCertToMap(Certificate cert, boolean isEECertificate, byte id, String label) {
		SmartCardHSMEntry entry = namemap.get(label);
		if (entry == null) {
			entry = new SmartCardHSMEntry(cert, isEECertificate, id);
			namemap.put(label, entry);
		} else {
			entry.setCert(cert, isEECertificate, id);			
		}
	}



	/**
	 * Remove an entry both from map and card.
	 * 
	 * @param label
	 * @throws CardServiceException
	 * @throws CardTerminalException
	 * @throws CardIOException
	 */
	public void removeEntry(String label) throws CardServiceException, CardTerminalException, CardIOException {
		String path;
		SmartCardHSMEntry entry = namemap.get(label);

		if (entry == null) throw new CardServiceResourceNotFoundException("Entry " + label + " not found.");

		if (entry.isKeyEntry()) {
			byte keyID = entry.getKey().getKeyID();

			// Remove private key
			path = ":" + HexString.hexify(KEYPREFIX) + HexString.hexify(keyID);
			delete(new CardFilePath(path));

			// Remove public key certificate
			path = ":" + HexString.hexify(PRKDPREFIX) + HexString.hexify(keyID);
			delete(new CardFilePath(path));			
		} 
		if (entry.isCertificateEntry()) {
			byte certID = entry.getId();

			if (entry.isEECertificate()) {
				// Remove EE Certificate
				path = ":" + HexString.hexify(EECERTIFICATEPREFIX) + HexString.hexify(certID);
				delete(new CardFilePath(path));				
			} else {
				// Remove CA Certificate
				path = ":" + HexString.hexify(CACERTIFICATEPREFIX) + HexString.hexify(certID);
				delete(new CardFilePath(path));
				path = ":" + HexString.hexify(CERTDESCRIPTIONPREFIX) + HexString.hexify(certID);
				delete(new CardFilePath(path));	
			}
		}

		idmap.remove(entry.getId());
		namemap.remove(label);
		certIDMap.remove(label);
	}

	

	/**
	 * Check if the label exists.
	 * 
	 * @param label the key label
	 * @return true if label is available
	 */
	public boolean containsLabel(String label) {
		return namemap.containsKey(label);
	}



	/**
	 * Get a Entry object
	 * 
	 * @param label
	 * @return SmartCardHSMEntry
	 */
	public SmartCardHSMEntry getSmartCardHSMEntry(String label) {
		SmartCardHSMEntry entry = namemap.get(label);
		return entry;
	}
	


	/**
	 * Enumerate SmartCardHSM entries. 
	 * 
	 * @return the aliases of all SmartCardHSM entries
	 * @throws CardTerminalException
	 * @throws CardServiceException
	 * @throws TLVEncodingException
	 * @throws CertificateException
	 */
	public Vector<String> enumerateEntries() throws CardTerminalException, CardServiceException, TLVEncodingException, CertificateException {
		byte[] fobs = enumerateObjects();
		Vector<String> aliases = new Vector<String>();
		Byte kid;

		// Add Device Authentication Certificate				
		try {
			byte[] certBin = read(new CardFilePath(":2F02"), 0, READ_SEVERAL);
			Certificate cert = new CardVerifiableCertificate("CVC", certBin);
			addCertToMap(cert, true, (byte) 0x00, "DeviceAuthentication");
			aliases.add("DeviceAuthentication");
		} catch (CardServiceUnexpectedStatusWordException e) {
			log.fine(e.getLocalizedMessage());
			throw new CardServiceException("No Device Authentication Certificate found");			
		}


		// Process keys
		for (int i = 0; i < fobs.length; i += 2) {
			if (fobs[i] == KEYPREFIX) {
				kid = fobs[i + 1];
				if (kid > 0) {
					idmap.put(kid, new SmartCardHSMKey(kid, "", (short)0));
					log.finer("Added key #" + kid);					
				}
			}
		}

		// Find the corresponding label to the key and add it to the name map
		for (int i = 0; i < fobs.length; i += 2) {			
			if (fobs[i] == PRKDPREFIX) {
				kid = fobs[i + 1];
				String path = ":C4" + HexString.hexify(kid);
				CardFilePath file = new CardFilePath(path);
				byte[] descbin = read(file, 0, READ_SEVERAL);		
				SmartCardHSMKey key = idmap.get(kid);

				if (key != null) {
					key.setDescription(descbin);
					aliases.add(key.getLabel());
					addKeyToMap(key);
					log.finer("Put " + key.getLabel() + " to the keylist...");
				}
			}
		}
		
		// Add CA certificates to the name map
		for (int i = 0; i < fobs.length; i += 2) {
			if (fobs[i] == CACERTIFICATEPREFIX) {
				byte id = fobs[i + 1];
				caid.add(id);
				String path =":CA" + HexString.hexify(id);
				CardFilePath file = new CardFilePath(path);
				byte[] certBin = read(file, 0, READ_SEVERAL);
				try {
					ByteArrayInputStream inStream = new ByteArrayInputStream(certBin);
					CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
					X509Certificate cert = (X509Certificate)cf.generateCertificate(inStream);
					inStream.close();

					// The label will be obtained in later step
					
					certIDMap.put(id, cert);
				} catch (CertificateException e) {
					log.fine(e.getLocalizedMessage());
				} catch (IOException e) {
					log.fine(e.getLocalizedMessage());
				} catch (NoSuchProviderException e) {
					log.fine(e.getLocalizedMessage());
				}			
			}
		}

		// Find the corresponding label to the CA certificate and add it to the name map
		for (int i = 0; i < fobs.length; i += 2) {			
			if (fobs[i] == CERTDESCRIPTIONPREFIX) {
				byte id = fobs[i + 1];
				String path = ":" + HexString.hexify(CERTDESCRIPTIONPREFIX) + HexString.hexify(id);
				CardFilePath file = new CardFilePath(path);
				byte[] encLabel = read(file, 0, READ_SEVERAL);		
				CertificateDescription cd = new CertificateDescription();
				String label = cd.getLabel(encLabel);
							
				Certificate cert = certIDMap.get(id);
				if (cert == null) {
					throw new CardServiceException("No corresponding CA certificate for this certificate description found");
				}
				addCertToMap(cert, false, id, label);
				aliases.add(label);
			}
		}
		
		// Add CE certificates to the name map
		for (int i = 0; i < fobs.length; i += 2) {

			if (fobs[i] == EECERTIFICATEPREFIX) {
				byte id = fobs[i + 1];
				CardFilePath file = new CardFilePath(":CE" + HexString.hexify(id));
				byte[] certBin = read(file, 0, READ_SEVERAL);
				Certificate cert;
				try {
					cert = new CardVerifiableCertificate("CVC", certBin);
				} catch (CertificateException e) {
					ByteArrayInputStream inStream = new ByteArrayInputStream(certBin);
					CertificateFactory cf = null;
					try {
						cf = CertificateFactory.getInstance("X.509", "BC");
					} catch (NoSuchProviderException e2) {
						log.fine(e.getLocalizedMessage());
					}
					cert = (X509Certificate)cf.generateCertificate(inStream);
					try {
						inStream.close();
					} catch (IOException e1) {
						log.fine(e.getLocalizedMessage());
					}
				}
				SmartCardHSMKey key = idmap.get(id);
				/*
				 * No certificate for the key - skip it
				 */
				if (key == null) {
					continue;
				}
				addCertToMap(cert, true, id, key.getLabel());
			}			
		}
		log.finer("Keylist: " + aliases);
		return aliases;
	}



	/**
	 * Determine an unused CA identifier
	 * 
	 * @return a free CA identifier or -1 if all identifier in use
	 * @throws TLVEncodingException 
	 * @throws CertificateException 
	 * @throws CardServiceException 
	 * @throws CardTerminalException 
	 */
	public byte determineFreeCAId () throws CardTerminalException, CardServiceException, CertificateException, TLVEncodingException {
		enumerateEntries();

		if (caid.isEmpty()) {
			return 0;
		} else {
			byte id = (byte) (caid.lastElement() + 1);
			if (id > 0xFF) { 
				return -1;
			} else {
				return id;
			}
		}
	}



	/**
	 * Determine an unused key identifier
	 *
	 * @return a free key identifier or -1 if all key identifier in use
	 */
	public byte determineFreeKeyId () {
		for (int i = 1; i < KEY_CAPACITY; i++) {
			if (idmap.get((byte)i) == null) {
				return (byte)i;
			}
		}
		return -1;
	}



	/**
	 * Create a PKCS#15 PrivateECCKey description
	 * 
	 * @param keyid the key identifier
	 * @param label the key label
	 * @param keysize
	 * @return the PrivateECCKey description
	 * @throws TLVEncodingException
	 */
	public byte[] buildPrkDForECC(byte keyid, String label, short keysize) throws TLVEncodingException {
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
		PrimitiveTLV   part201 = new PrimitiveTLV(Tag.INTEGER, new byte[] {(byte)(keysize >> 8), (byte)keysize});		
		part200.add(part2000);
		part20.add(part200);
		if (keysize > 0) {
			part20.add(part201);
		}
		part2.add(part20);

		prkd.add(part0);
		prkd.add(part1);
		prkd.add(part2);

		return prkd.getBytes();
	}

	/* Ausgelagert in eine neue Klasse
	 * 
	public byte[] parsePRKD(byte[] prkd) throws TLVEncodingException {
		ConstructedTLV tlv = new ConstructedTLV(prkd);
		Tag sequence = new Tag(Tag.SEQUENCE);
		
		// Get Label
		ConstructedTLV tmp = (ConstructedTLV)tlv.get(0);
		String label = new String(tmp.get(0).getValue());
		
		// Get Key ID
		tmp = (ConstructedTLV)tlv.get(1);
		byte keyid = tmp.get(0).getValue()[0];
		
		// Get key size and return new PRKD
		if (tlv.getTag() == sequence) {
			tmp = (ConstructedTLV)tlv.get(2);
			tmp = (ConstructedTLV)tmp.get(1);
			byte[] size = tmp.getValue();
			short modulussize = (short)(size[0] ^ size[1]);
			
			//return buildPrkDForRSA(keyid, label, modulussize);
		} else {
			short keysize = 0;
			tmp = (ConstructedTLV)tlv.get(2);
			if (tmp.getChildCount() == 2) {
				tmp = (ConstructedTLV)tmp.get(1);
				byte[] size = tmp.getValue();
				keysize = (short)(size[0] ^ size[1]);	
			}
			
			//return buildPrkDForECC(keyid, label, keysize);
		}
		
		tlv = new ConstructedTLV(0x30);
		tlv.add(new PrimitiveTLV(Tag.OCTET_STRING, new byte[] {keyid}));
		tlv.add(new PrimitiveTLV(Tag.UTF8String, label.getBytes()));
		
		new PrimitiveTLV(Tag.INTEGER, new byte[] {(byte)(modulussize >> 8), (byte)modulussize});
		
		return tlv.getBytes();
	}
	*/

	/**
	 * Create a PKCS#15 PrivateRSAKey description
	 * 
	 * @param keyid the key identifier
	 * @param label the key label
	 * @param modulussize
	 * @return the PrivateRSAKey description 
	 * @throws TLVEncodingException
	 */
	public byte[] buildPrkDForRSA(byte keyid, String label, short modulussize) throws TLVEncodingException {
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
		PrimitiveTLV part201 = new PrimitiveTLV(Tag.INTEGER, new byte[] {(byte)(modulussize >> 8), (byte)modulussize});
		part200.add(part2000);
		part20.add(part200);
		part20.add(part201);
		part2.add(part20);

		prkd.add(part0);
		prkd.add(part1);
		prkd.add(part2);

		return prkd.getBytes();
	}
	
	
	
	/**
	 * Store the private key description on the card
	 * 
	 * @throws CardIOException 
	 * @throws CardTerminalException 
	 * @throws CardServiceException 
	 */
	public void storePRKD(PrivateKeyDescription prkd) throws CardServiceException, CardTerminalException, CardIOException {
		String path = ":C4" + HexString.hexify(prkd.getKeyID());
		log.finer("path: " + path);
		write(new CardFilePath(path), 0, prkd.getEncoded());
	}
	
	

	private CardChannel getChannel() {
		this.allocateCardChannel();
		return this.getCardChannel();

	}



	@Override
	public ResponseAPDU sendCommandAPDU(CardFilePath path, CommandAPDU com,
			int usageQualifier) throws CardServiceException,
			CardTerminalException {
		ResponseAPDU rsp;

		if (doSecureMessaging) {
			rsp = sendSecMsgCommand(com);
		} else {
			rsp = sendCommandAPDU(AID, com);
		}

		return rsp;
	}
	
	
	
	/**
	 * Return the Device Authentication Certificate Chain
	 * as CardVerifiableCertificate array.
	 * <ul>
	 * 	<li>At position 0 there is the DevAutCertifiacte</li>
	 * 	<li>At position 1 there is the IssuerCertifiacte if it exits.
	 * 		Otherwise the array has a length of 1</li>
	 * </ul>
	 * 
	 * @return CardVerifiableCertificate[]
	 * @throws CardServiceException
	 * @throws CardTerminalException
	 * @throws CardIOException
	 * @throws TLVEncodingException
	 * @throws CertificateException
	 */
	private CardVerifiableCertificate[] getCertificateChain() throws CardServiceException, CardTerminalException {
		CardVerifiableCertificate[] certs;
		byte[] devAutEnc;
		CardVerifiableCertificate devAutCert;
		CardVerifiableCertificate issuerCert;
		
		// Read certificate(s)
		byte[] certBytes = read(new CardFilePath(":2F02"), 0, 0);
				
		try {
			devAutEnc = new ConstructedTLV(certBytes).getBytes();
		} catch (TLVEncodingException e) {
			log.fine(e.getLocalizedMessage());
			throw new CardServiceException("Unexptected TLV encoding error");
		}
				
		try {
			devAutCert = new CardVerifiableCertificate("CVC", devAutEnc);
		} catch (CertificateException e) {
			log.fine(e.getLocalizedMessage());
			throw new CardServiceException("Unexptected CardVerifiableCertificate error");
		}
		
		if (devAutEnc.length == certBytes.length){
			// Only the Device Authentication Certificate is stored on the card
			certs = new CardVerifiableCertificate[1];					
			certs[0] = devAutCert;			
		} else {
			// The Device Authentication Certificate and
			// the Issuer Certificate are read
			certs = new CardVerifiableCertificate[2];
			certs[0] = devAutCert;
									
			// The Issuer Cert starts directly after the DevAutCert
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(certBytes, devAutEnc.length, certBytes.length - devAutEnc.length);	
						
			try {
				issuerCert = new CardVerifiableCertificate("CVC", bos.toByteArray());
			} catch (CertificateException e) {
				log.fine(e.getLocalizedMessage());
				throw new CardServiceException("Unexptected CardVerifiableCertificate error");
			}
			certs[1] = issuerCert;
		}		
		return certs;		
	}
	
	
	
	public ECPublicKey getDevAutPK() throws CardServiceException, CardTerminalException {
				
		CardVerifiableCertificate[] certs = getCertificateChain();
				
		if (certs.length == 1) {
			// Read Issuer Certificate
			ByteArrayInputStream bis = new ByteArrayInputStream(issuerCert);
			 
			PublicKey issuerPK;
			try {
				CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
				issuerPK = cf.generateCertificate(bis).getPublicKey();
			} catch (CertificateException e) {
				log.fine(e.getLocalizedMessage());
				throw new CardServiceException("Unexpected CertificateException");
			} catch (NoSuchProviderException e) {
				log.fine(e.getLocalizedMessage());
				throw new CardServiceException("Missing \"BC\" Provider");
			};
			

			// Verify Device Authentication Certificate
			try {
				certs[0].verify(issuerPK);
			} catch (CertificateException e) {
				log.fine(e.getLocalizedMessage());
				throw new CardServiceException("The Device Authentication Certificate isn't valid.");
			} catch (InvalidKeyException e) {
				log.fine(e.getLocalizedMessage());
				throw new CardServiceException("Unexpected InvalidKeyException");
			} catch (NoSuchAlgorithmException e) {
				log.fine(e.getLocalizedMessage());
				throw new CardServiceException("Unexpected NoSuchAlgorithmException");
			} catch (NoSuchProviderException e) {
				log.fine(e.getLocalizedMessage());
				throw new CardServiceException("Unexpected NoSuchProviderException");
			} catch (SignatureException e) {
				log.fine(e.getLocalizedMessage());
				throw new CardServiceException("Unexpected SignatureException");
			}
			return (ECPublicKey)certs[0].getPublicKey();
		} else {			
			// Find matching authority reference for the Device Authentication Certificate			
			byte[] car = certs[1].getCAR();
			CardVerifiableCertificate rootCVC;
			if (java.util.Arrays.equals(car, ROOT_CA)) {
				try {
					rootCVC = new CardVerifiableCertificate("CVC", rootCert);
				} catch (CertificateException e) {
					log.fine(e.getLocalizedMessage());
					throw new CardServiceException("Unexpected CertificateException");
				}
			} else if (java.util.Arrays.equals(car, UT_CA)){
				try {
					rootCVC = new CardVerifiableCertificate("CVC", utCert);
				} catch (CertificateException e) {
					log.fine(e.getLocalizedMessage());
					throw new CardServiceException("Unexpected CertificateException");
				}								
			} else {
				throw new CardServiceException("No matching authority reference found for Device Authentication Certificate");
			}
			
			// Get domain parameter from Root CA
			byte[] domainParam = rootCVC.getDomainParameter();
					
			
			PublicKey rootPK = rootCVC.getPublicKey();
			
			try {
				// Verify Root Certificate
				rootCVC.verify(rootPK);
				
				// Verify Issuer Certificate
				certs[1].verify(rootPK);			
				
				// Verify Device Authentication Certificate
				certs[0].verify(certs[1].getPublicKey(domainParam));				
			} catch (CertificateException e) {
				log.fine(e.getLocalizedMessage());
				throw new CardServiceException("The Device Authentication Certificate isn't valid.");
			} catch (InvalidKeyException e) {
				log.fine(e.getLocalizedMessage());
				throw new CardServiceException("Unexpected InvalidKeyException");
			} catch (NoSuchAlgorithmException e) {
				log.fine(e.getLocalizedMessage());
				throw new CardServiceException("Unexpected NoSuchAlgorithmException");
			} catch (NoSuchProviderException e) {
				log.fine(e.getLocalizedMessage());
				throw new CardServiceException("Unexpected NoSuchProviderException");
			} catch (SignatureException e) {
				log.fine(e.getLocalizedMessage());
				throw new CardServiceException("Unexpected SignatureException");
			}
			return (ECPublicKey)certs[0].getPublicKey(domainParam);
		}
	}
}
