/*
 *  ---------
 * |.##> <##.|  
 * |#       #|  
 * |#       #|  Copyright (c) 2011-2012 CardContact Software & System Consulting
 * |'##> <##'|  Andreas Schwier, 32429 Minden, Germany (www.cardcontact.de)
 *  --------- 
 */


package de.cardcontact.opencard.service.eac20;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.Signer;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECField;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;

import de.cardcontact.opencard.security.IsoCredentialStore;
import de.cardcontact.opencard.security.IsoSecureChannel;
import de.cardcontact.opencard.security.IsoSecureChannelCredential;
import de.cardcontact.opencard.security.SecureChannel;
import de.cardcontact.opencard.security.SecureChannelCredential;
import de.cardcontact.opencard.service.smartcardhsm.SmartCardHSMCardService;
//import de.cardcontact.smartcardhsmprovider.SmartCardHSMProvider;
import de.cardcontact.tlv.ConstructedTLV;
import de.cardcontact.tlv.PrimitiveTLV;
import de.cardcontact.tlv.TLVEncodingException;
import opencard.core.service.CardChannel;
import opencard.core.service.CardService;
import opencard.core.service.CardServiceException;
import opencard.core.service.InvalidCardChannelException;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.security.CredentialBag;
import opencard.opt.security.CredentialStore;

/**
 * Class implementing an EAC2.0 service.
 * At this time only the SmartCardHSMCardService is supported.
 * 
 * @author lew
 *
 */
public class EAC20 {

	private final static Logger log = Logger.getLogger(EAC20.class.getName());
	
	/**
	 * ManageSE data
	 * protocol: id-CA-ECDH-3DES-CBC-CBC
	 */
	private byte[] mseData = {(byte) 0x80, 0x0A, 0x04, 0x00, 0x7F, 0x00, 0x07, 0x02, 0x02, 0x03, 0x02, 0x01};

	private SmartCardHSMCardService hsms;

	private CardChannel channel;

	private ECPrivateKey prkCA;

	private ECPublicKey pukCA;

	private byte[] ephemeralPublicKeyIfd;
	
	private ECPublicKey devAuthPK;
	
	private SecretKey kenc;
	
	private SecretKey kmac;
	
	private IsoSecureChannel sc;
	
	private IsoSecureChannelCredential credential;
	
	private CredentialStore store;
	
	/** 
	 * for SmartCardHSM 
	 */
	private CardFilePath securityDomain = new CardFilePath("#E82B0601040181C31F0201");

	/**
	 * OID: id-CA-ECDH-3DES-CBC-CBC
	 */
	private byte[] protocol =  {0x04, 0x00, 0x7F, 0x00, 0x07, 0x02, 0x02, 0x03, 0x02, 0x01};

	
	/**
	 * @param hsms SmartCardHSMCardService
	 * @param devAuthPK device authentication public key
	 */
	public EAC20(SmartCardHSMCardService hsms, ECPublicKey devAuthPK) {
		this.hsms = hsms;
		this.devAuthPK = devAuthPK;		
	}

	
	/**
	 * Perform chip authentication and establish a secure channel
	 * 
	 * @return IsoSecureChannelCredential
	 * @throws CardTerminalException
	 * @throws CardServiceException 
	 */
	public SecureChannelCredential performChipAuthentication() throws CardServiceException, CardTerminalException {
		
		generateEphemeralCAKeyPair();		
				
		hsms.manageSE(mseData);		

		byte[] dadobin = doGeneralAuthenticate();
		
		ConstructedTLV dado = null;
		try {
			dado = new ConstructedTLV(dadobin);
		} catch (TLVEncodingException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		
		PrimitiveTLV nonceDO = (PrimitiveTLV) dado.get(0);
		PrimitiveTLV authTokenDO = (PrimitiveTLV) dado.get(1);

		byte[] nonce = nonceDO.getValue();
		byte[] authToken = authTokenDO.getValue();

		ECPoint q = devAuthPK.getW();
		ECParameterSpec ecParameterSpec = prkCA.getParams();
		ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(q, ecParameterSpec);

		Key otherKey = null;
		try {
			otherKey = KeyFactory.getInstance("EC").generatePublic(ecPublicKeySpec);
		} catch (InvalidKeySpecException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		
		byte[] k = null;
		try {
			KeyAgreement ka = KeyAgreement.getInstance("ECDH", "BC");
			ka.init(prkCA);
			ka.doPhase(otherKey, true);
			k = ka.generateSecret();
		} catch (NoSuchAlgorithmException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		} catch (InvalidKeyException e) {			
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		} catch (NoSuchProviderException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		
		kenc = deriveKey(k, 1, nonce);		
		kmac = deriveKey(k, 2, nonce);		
		
		boolean verified = verifyAuthenticationToken(authToken);
		assert(verified == true);
		
		sc = new IsoSecureChannel();
		sc.setEncKey(kenc);
		sc.setMacKey(kmac);
		sc.setMACSendSequenceCounter(new byte[8]);
		// The SmartCardHSM uses a shared sequence counter for mac and enc
		//sc.setEncryptionSendSequenceCounter(new byte[8]); 
		
		credential = new IsoSecureChannelCredential(SecureChannel.ALL, sc);
		store = new IsoCredentialStore();
		((IsoCredentialStore)store).setSecureChannelCredential(securityDomain, credential);
		
		
		
		return credential;
	}
	
	/**
	 * Generate ephemeral private and public CA keys.
	 * For generation a Bouncy Castle provider has to be registered.
	 */
	private void generateEphemeralCAKeyPair() {
		KeyPairGenerator keyGen = null;
		try {
			keyGen = KeyPairGenerator.getInstance("EC", "BC");
		} catch (NoSuchAlgorithmException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		} catch (NoSuchProviderException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		
		BigInteger prime = new BigInteger("A9FB57DBA1EEA9BC3E660A909D838D726E3BF623D52620282013481D1F6E5377", 16); 
		ECField field = new ECFieldFp(prime);
		BigInteger a, b;
		a = new BigInteger("7D5A0975FC2C3057EEF67530417AFFE7FB8055C126DC5C6CE94A4B44F330B5D9", 16);
		b = new BigInteger("26DC5C6CE94A4B44F330B5D9BBD77CBF958416295CF7E1CE6BCCDC18FF8C07B6", 16);
		EllipticCurve curve = new EllipticCurve(field, a, b);
		
		BigInteger x = new BigInteger("8BD2AEB9CB7E57CB2C4B482FFC81B7AFB9DE27E1E3BD23C23A4453BD9ACE3262", 16);
		BigInteger y = new BigInteger("547EF835C3DAC4FD97F8461A14611DC9C27745132DED8E545C1D54C72F046997", 16);
		ECPoint g = new ECPoint(x, y);
		BigInteger n = new BigInteger("A9FB57DBA1EEA9BC3E660A909D838D718C397AA3B561A6F7901E0E82974856A7", 16);
		int h = 1;
		ECParameterSpec params = new ECParameterSpec(curve, g, n, h);
		
		try {
			keyGen.initialize(params);
		} catch (InvalidAlgorithmParameterException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		KeyPair kp = keyGen.generateKeyPair();
		
		prkCA = (ECPrivateKey) kp.getPrivate();
		pukCA = (ECPublicKey) kp.getPublic();
	}


	/**
	 * Build the authentication template consisting of
	 * the public point (qx, qy) of the public key. <br>
	 * In a previous step, the public key has to be generated with generateEphemeralCAKeyPair()
	 * 
	 * @return dadobin, the authentication template from the card containing nonce and token
	 * @throws CardServiceException 
	 * @throws CardTerminalException 
	 */
	private byte[] doGeneralAuthenticate() throws CardTerminalException, CardServiceException {
				
		byte[] qx = unsignedBigIntegerToByteArray(pukCA.getW().getAffineX(), 256);
		byte[] qy = unsignedBigIntegerToByteArray(pukCA.getW().getAffineY(), 256);
		
		ephemeralPublicKeyIfd = new byte[(qx.length * 2) + 1];

		ephemeralPublicKeyIfd[0] = 0x04;
		System.arraycopy(qx, 0, ephemeralPublicKeyIfd, 1, qx.length);
		System.arraycopy(qy, 0, ephemeralPublicKeyIfd, 1 + qx.length, qy.length);

		
		byte[] authTemplate = null;
		try {
			ConstructedTLV dado = new ConstructedTLV(0x7C);
			PrimitiveTLV tmp = new PrimitiveTLV(0x80, ephemeralPublicKeyIfd);
			dado.add(tmp);

			authTemplate = dado.getBytes();
		} catch (TLVEncodingException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}

		return hsms.generalAuthenticate(authTemplate);	
	}

	
	
	public SecureChannelCredential getCredential() {
		return credential;
	}
	
	
	/**
	 * Generate 
	 * @param k
	 * @param counter
	 * @param nonce
	 * @return SecretKey
	 * @throws NoSuchAlgorithmException
	 */
	private SecretKey deriveKey(byte[] k, int counter, byte[] nonce) {
	
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			bos.write(k);
			bos.write(nonce);
		} catch (IOException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		
		bos.write(0);
		bos.write(0);
		bos.write(0);
		bos.write(counter);
		
		byte[] input = bos.toByteArray();
		
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		digest.update(input);
		byte[] md = digest.digest();
		
		byte[] keyBin = new byte[24];
		System.arraycopy(md, 0, keyBin, 0, 16);
		System.arraycopy(md, 0, keyBin, 16, 8);
		
		DESedeKeySpec desedeKeySpec = null;
		try {
			desedeKeySpec = new DESedeKeySpec(keyBin);
		} catch (InvalidKeyException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		
		SecretKeyFactory skf = null;
		SecretKey key = null;
		try {
			skf = SecretKeyFactory.getInstance("DESede");
			key = skf.generateSecret(desedeKeySpec);
		} catch (InvalidKeySpecException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		
		return key;
	}
	
	
	/**
	 * Calculate and verify the authentication token over the public key received from
	 * the other side
	 * @param authToken the MAC over the authentication data
	 * @return true if the MAC is valid
	 */
	public boolean verifyAuthenticationToken(byte[] authToken) {
		byte[] at = null;

		byte[] t = encodePublicKey();

		try {
			Mac mac = Mac.getInstance("ISO9797ALG3Mac");
			mac.init(kmac);
			mac.update(t);
			at = mac.doFinal();
		} catch (NoSuchAlgorithmException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		} catch (InvalidKeyException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}

		return Arrays.equals(at, authToken);
	}
	
	
	/**
	 * Encode public key to EAC 2.0 format
	 * 
	 * @return
	 * @throws TLVEncodingException
	 */
	public byte[] encodePublicKey() {
		ConstructedTLV t = null;
		try {
			t = new ConstructedTLV(0x7F49);
			t.add(new PrimitiveTLV(0x06, protocol));
			t.add(new PrimitiveTLV(0x86, ephemeralPublicKeyIfd));
		} catch (TLVEncodingException e) {
			log.log(Level.WARNING, e.getLocalizedMessage(), e);
		}	
		
		return t.getBytes();
	}
	
	/**
	 * Convert unsigned big integer into byte array, stripping of a
	 * leading 00 byte
	 *
	 * This conversion is required, because the Java BigInteger is a signed
	 * value, whereas the byte arrays containing key components are unsigned by default
	 * 
	 * @param bi    BigInteger value to be converted
	 * @param size  Number of bits
	 * @return      Byte array containing unsigned integer value
	 */
	protected static byte[] unsignedBigIntegerToByteArray(BigInteger bi, int size) {
		byte[] s = bi.toByteArray();
		size = (size >> 3) + ((size & 0x7) == 0 ? 0 : 1);
		byte[] d = new byte[size];
		int od = size - s.length;
		int os = 0;
		if (od < 0) {  // Number is longer than expected
			if ((od < -1) || s[0] != 0) {   // If it is just a leading zero, then we cut it off
				throw new IllegalArgumentException("Size mismatch converting big integer to byte array");
			}
			os = -od;
			od = 0;
		}
		size = size - od;

		System.arraycopy(s, os, d, od, size);
		return d;
	}
}
