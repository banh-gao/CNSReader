package de.cardcontact.tlv.cvc;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.spec.ECField;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.logging.Logger;

import de.cardcontact.tlv.ConstructedTLV;
import de.cardcontact.tlv.ObjectIdentifier;
import de.cardcontact.tlv.PrimitiveTLV;
import de.cardcontact.tlv.TLV;
import de.cardcontact.tlv.TLVEncodingException;
import de.cardcontact.tlv.Tag;

public class CardVerifiableCertificate extends Certificate{
		
	/** Authentication Template */
	private static Tag TAG_AT;
	
	/** CV Certificate*/
	private static Tag TAG_CVC;
	
	/** CV Certificate*/
	private static Tag TAG_BODY;
	
	/** CAR */
	private static Tag TAG_CAR;

	/** CHR */
	private static Tag TAG_CHR;
	
	/** Public Key */
	private static Tag TAG_PUK;
	
	/** Public Key Algorithm*/
	private static Tag TAG_PK_ALGORITHM;
	
	/** Prime Modulus / Modulus*/
	private static Tag TAG_PK_MODULUS;
	
	/** Public Exponent*/
	private static Tag TAG_PK_EXPONENT;
	
	/** First coefficient a*/
	private static Tag TAG_PK_C_A;
	
	/** Second coefficient b*/
	private static Tag TAG_PK_C_B;
	
	/** Base point G*/
	private static Tag TAG_PK_BASE_POINT;
	
	/** Order of the base point*/
	private static Tag TAG_PK_ORDER;
	
	/** Public point y*/
	private static Tag TAG_PK_PUBLIC_P;
	
	/** Cofactor f*/
	private static Tag TAG_PK_COFACTOR;
	
	/** TA constants */
	private static final ObjectIdentifier ID_TA_ECDSA = new ObjectIdentifier("0.4.0.127.0.7.2.2.2.2");
	
	private final static Logger log = Logger.getLogger(CardVerifiableCertificate.class.getName());
	
	/** The encoded certificate */
	private byte[] bin;
	
	private ConstructedTLV tlv;
	
	private ConstructedTLV cvc;
	
	private ConstructedTLV body;
		
	private PrimitiveTLV signature;
	
	private PublicKey publicKey;
		
	/** Domain Parameter*/
	private byte[] domainParam;
	
	public CardVerifiableCertificate(String type, byte[] certificate) throws CertificateException {
		
		super(type);
		
		initTags();
		
		this.bin = certificate;	
								
		try {
			tlv = new ConstructedTLV(bin);
		
			log.fine(tlv.dump(4));
			
//			System.out.println(tlv.dump());
//			byte[] devAutEnc = tlv.getBytes();
//			
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			bos.write(certificate, devAutEnc.length, certificate.length - devAutEnc.length);
//			ConstructedTLV issuerCert = new ConstructedTLV(bos.toByteArray());
//			System.out.println(issuerCert.dump());
//			byte[] issuerEnc = issuerCert.getBytes();
			
			
			if (tlv.getTag().equals(TAG_AT)) {
				cvc = (ConstructedTLV) tlv.get(0);
				body = (ConstructedTLV)cvc.get(0);
				signature = (PrimitiveTLV) cvc.get(1);
			} else if (tlv.getTag().equals(TAG_CVC)) {
				cvc = tlv;
				body = (ConstructedTLV)cvc.get(0);
				signature = (PrimitiveTLV) cvc.get(1);
			} else {
				throw new CertificateException("This is not a Card Verifiable Certificate");
			}			
		} catch (TLVEncodingException e) {
			log.fine(e.getLocalizedMessage());
			throw new CertificateParsingException(e);
		} 
				
				
	}
	
	private void initTags() {
		try {
			TAG_AT = new Tag(0x67);
			TAG_CVC = new Tag(0x7F21);
			TAG_BODY = new Tag(0x7F4E);
			TAG_CAR = new Tag(0x42);
			TAG_CHR = new Tag(0x5F20);
			TAG_PUK = new Tag(0x7F49);
			TAG_PK_ALGORITHM = new Tag(0x06);
			TAG_PK_MODULUS = new Tag(0x81);
			TAG_PK_EXPONENT = new Tag(0x82);
			TAG_PK_C_A = new Tag(0x82);
			TAG_PK_C_B = new Tag(0x83);
			TAG_PK_BASE_POINT = new Tag(0x84);
			TAG_PK_ORDER = new Tag(0x85);
			TAG_PK_PUBLIC_P = new Tag(0x86);
			TAG_PK_COFACTOR = new Tag(0x87);
			
			
			
		} catch (TLVEncodingException e) {
			log.fine(e.getLocalizedMessage());
		}
	}
	
	/**
	 * Parsing the certificate and generate the public key
	 * @throws TLVEncodingException 
	 * @throws CertificateException 
	 */
	private void extractPublicKey() throws CertificateException {
		if (isECDSA(getPublicKeyOID())) {
			try {
				publicKey = getECPublicKey();
			} catch (TLVEncodingException e) {
				log.fine(e.getLocalizedMessage());
				throw new CertificateParsingException(e);
			}
		} else {
			publicKey = getRSAPublicKey();
		}
	}
	
	private boolean isECDSA(ObjectIdentifier oid) {
		int[] oidArray = oid.getObjectIdentifier();
		int[] ecdsa = ID_TA_ECDSA.getObjectIdentifier();
		for (int i = 0; i < ecdsa.length; i++) {
			if (oidArray[i] != ecdsa[i]) return false;
		}
		return true;
	}
	
	private ObjectIdentifier getPublicKeyOID() throws CertificateException {
		ConstructedTLV pdo = (ConstructedTLV) body.findTag(TAG_PUK, null);
		PrimitiveTLV oid = null;
		try {
			oid = (PrimitiveTLV) pdo.findTag(new Tag(Tag.OBJECT_IDENTIFIER), null);
		} catch (TLVEncodingException e) {
			log.fine(e.getLocalizedMessage());
		}
		if (oid == null) {
			log.fine("No OID found.");
			throw new CertificateException("No OID found.");
		}
		return new ObjectIdentifier(oid.getValue());
	}
	
	
	public byte[] getAlgorithm() throws TLVEncodingException {
		ConstructedTLV pk = getPublicKeyFromCertificate();
		byte[] alg = ((PrimitiveTLV)pk.get(0)).getValue();
		return alg;
	}
	
	public BigInteger getModulus() throws TLVEncodingException {
		ConstructedTLV pk = getPublicKeyFromCertificate();
		byte[]mod = ((PrimitiveTLV)pk.get(1)).getValue();
		BigInteger modulus = byteArrayToUnsignedBigInteger(mod);
			
		return modulus;
	}
	
	
	public BigInteger getExponent() throws TLVEncodingException {
		ConstructedTLV pk = getPublicKeyFromCertificate();
		PrimitiveTLV exp = (PrimitiveTLV)pk.get(2);
		BigInteger exponent = byteArrayToUnsignedBigInteger(exp.getValue());
		
		return exponent;
	}
	
	
	private ECPublicKeySpec getECPublicKeySpecFromDomain() throws TLVEncodingException {
		ConstructedTLV pk = getPublicKeyFromCertificate();
		ConstructedTLV domain = new ConstructedTLV(domainParam);
				
		byte[] prime;
		TLV tlv = pk.findTag(TAG_PK_MODULUS, null);
		if (tlv == null) {
			prime = domain.findTag(TAG_PK_MODULUS, null).getValue();
		} else {
			prime = tlv.getValue();
		}
		
		ECField field = new ECFieldFp(byteArrayToUnsignedBigInteger(prime));
		
		tlv = pk.findTag(TAG_PK_C_A, null);
		BigInteger a;
		if (tlv == null) {
			a = byteArrayToUnsignedBigInteger(domain.findTag(TAG_PK_C_A, null).getValue());
		} else {
			a = byteArrayToUnsignedBigInteger(tlv.getValue());
		}
				
		tlv = pk.findTag(TAG_PK_C_B, null);
		BigInteger b;
		if (tlv == null) {
			b = byteArrayToUnsignedBigInteger(domain.findTag(TAG_PK_C_B, null).getValue());
		} else {
			b = byteArrayToUnsignedBigInteger(tlv.getValue());
		}
		 
		EllipticCurve curve = new EllipticCurve(field, a, b);
		
		tlv = pk.findTag(TAG_PK_BASE_POINT, null);
		byte[] basePoint;	
		if (tlv == null) {
			basePoint = domain.findTag(TAG_PK_BASE_POINT, null).getValue();
		} else {
			basePoint = tlv.getValue();
		}		
		
		ECPoint g = getECPoint(basePoint);
		
		tlv = pk.findTag(TAG_PK_ORDER, null);
		BigInteger n;
		if (tlv == null) {
			n = byteArrayToUnsignedBigInteger(domain.findTag(TAG_PK_ORDER, null).getValue());
		} else {
			n = byteArrayToUnsignedBigInteger(tlv.getValue());
		}	
				
		tlv = pk.findTag(TAG_PK_COFACTOR, null);
		int h;
		if (tlv == null) {
			h = domain.findTag(TAG_PK_COFACTOR, null).getValue()[0];
		} else {
			h = tlv.getValue()[0];
		}	
		 		
		ECParameterSpec params = new ECParameterSpec(curve, g, n, h);
		
		tlv = pk.findTag(TAG_PK_PUBLIC_P, null);
		byte[] publicPoint;
		if (tlv == null) {
			publicPoint = domain.findTag(TAG_PK_PUBLIC_P, null).getValue();	
		} else {
			publicPoint = tlv.getValue();
		}	
		
		ECPoint y = getECPoint(publicPoint);		
		
		return new ECPublicKeySpec(y, params);
	}
	
	
	
	private ECPublicKeySpec getECPublicKeySpec() throws TLVEncodingException {
		ConstructedTLV pk = getPublicKeyFromCertificate();
		
		//First create ECParameterSpec
		
		byte[] prime = pk.findTag(TAG_PK_MODULUS, null).getValue();
		ECField field = new ECFieldFp(byteArrayToUnsignedBigInteger(prime));
		BigInteger a = byteArrayToUnsignedBigInteger(pk.findTag(TAG_PK_C_A, null).getValue());
		BigInteger b = byteArrayToUnsignedBigInteger(pk.findTag(TAG_PK_C_B, null).getValue());
		EllipticCurve curve = new EllipticCurve(field, a, b);
		
		byte[] basePoint = pk.findTag(TAG_PK_BASE_POINT, null).getValue();
		ECPoint g = getECPoint(basePoint);
		
		BigInteger n = byteArrayToUnsignedBigInteger(pk.findTag(TAG_PK_ORDER, null).getValue());
		int h = pk.findTag(TAG_PK_COFACTOR, null).getValue()[0];
		
		ECParameterSpec params = new ECParameterSpec(curve, g, n, h);
		
		byte[] publicPoint = pk.findTag(TAG_PK_PUBLIC_P, null).getValue();
		ECPoint y = getECPoint(publicPoint);		
		
		return new ECPublicKeySpec(y, params);
	}
	
	private BigInteger byteArrayToUnsignedBigInteger(byte[] data) {
		byte[] absoluteValue = new byte[data.length + 1];
		System.arraycopy(data, 0, absoluteValue, 1, data.length);
		return new BigInteger(absoluteValue);
	}
	
	private ECPoint getECPoint(byte[] data) {
		int length = (data.length - 1) / 2;
		byte[] x = new byte[length];
		byte[] y = new byte[length];
		System.arraycopy(data, 1, x, 0, length);
		System.arraycopy(data, 1 + length, y, 0, length);		
		ECPoint g = new ECPoint(byteArrayToUnsignedBigInteger(x), byteArrayToUnsignedBigInteger(y));
		return g;
	}
	
	private ConstructedTLV getPublicKeyFromCertificate() throws TLVEncodingException {
		ConstructedTLV pk = (ConstructedTLV)body.get(2);
		
		return pk;
	}
	
	
	@Override
	public byte[] getEncoded() {
		return this.bin;
	}

	private PublicKey getECPublicKey() throws TLVEncodingException {
		PublicKey key = null;
		try {
			KeyFactory fact = KeyFactory.getInstance("EC");
			ECPublicKeySpec spec;
			if (domainParam != null) {
				spec = getECPublicKeySpecFromDomain();
			} else {
				spec = getECPublicKeySpec();
			}
			key = fact.generatePublic(spec);
		} catch (NoSuchAlgorithmException e) {
			log.fine(e.getLocalizedMessage());
		} catch (InvalidKeySpecException e) {
			log.fine(e.getLocalizedMessage());
		} 
		return key;
	}
	
	private PublicKey getRSAPublicKey() throws CertificateException {
		ConstructedTLV puk = (ConstructedTLV) body.findTag(TAG_PUK, null);
		
		if (puk == null) {
			throw new CertificateException("Certificate doesn't contain a public key object.");
		}
		
		byte[]mod = ((PrimitiveTLV)puk.findTag(TAG_PK_MODULUS, null)).getValue();
		BigInteger modulus = byteArrayToUnsignedBigInteger(mod);
		
		PrimitiveTLV exp = (PrimitiveTLV)puk.findTag(TAG_PK_EXPONENT, null);
		BigInteger exponent = byteArrayToUnsignedBigInteger(exp.getValue());
		RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
		PublicKey key = null;
		try {
			KeyFactory fact = KeyFactory.getInstance("RSA", "SunRsaSign");
			key = fact.generatePublic(spec);
		} catch (NoSuchAlgorithmException e) {
			log.fine(e.getLocalizedMessage());
		} catch (NoSuchProviderException e) {
			log.fine(e.getLocalizedMessage());
		} catch (InvalidKeySpecException e) {
			log.fine(e.getLocalizedMessage());
		}
		return key;
	}

	public PublicKey getPublicKey(byte[] domainParam) {		
		this.domainParam = domainParam;
		try {
			extractPublicKey();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return publicKey;
	}
	
	@Override
	public PublicKey getPublicKey() {
		try {
			extractPublicKey();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return publicKey;
	}

	/**
	 * Set the following domain parameter:
	 * <ul>
	 * 	<li>Prime modulus</li>
	 * 	<li>First coefficient a</li>
	 * 	<li>Second coefficient b</li>
	 * 	<li>Base point G</li>
	 * 	<li>Order of the base point</li>
	 * 	<li>Cofactor f</li>
	 * </ul>
	 * Other domain parameter will be ignored
	 * @param param The domain parameter TLV encoded
	 */
	public void setDomainParameter(byte[] param) {
		ConstructedTLV tlv = null;
		try {
			tlv = new ConstructedTLV(param);
		} catch (TLVEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		ConstructedTLV publicKey = (ConstructedTLV)body.findTag(TAG_PUK, null);
		publicKey.add(tlv.findTag(TAG_PK_MODULUS, null));
		publicKey.add(tlv.findTag(TAG_PK_C_A, null));
		publicKey.add(tlv.findTag(TAG_PK_C_B, null));
		publicKey.add(tlv.findTag(TAG_PK_BASE_POINT, null));
		publicKey.add(tlv.findTag(TAG_PK_ORDER, null));
		publicKey.add(tlv.findTag(TAG_PK_COFACTOR, null));
	}
	
	/**
	 * 
	 * @return The domain parameter
	 */
	public byte[] getDomainParameter() {
		ConstructedTLV publicKey = (ConstructedTLV)body.findTag(TAG_PUK, null);
		
		return publicKey.getBytes();
	}
	
	@Override
	public String toString() {
		return tlv.dump();
	}
	
	
	/**
	 * 
	 * @return The Certification Authority Reference
	 */
	public byte[] getCAR() {
		PrimitiveTLV car = (PrimitiveTLV) this.body.findTag(TAG_CAR, null);
		return car.getValue();
	}

	
	/**
	 * 
	 * @return The Certificate Holder Reference
	 */
	public byte[] getCHR() {
		ConstructedTLV chr = (ConstructedTLV) this.body.findTag(TAG_CHR, null);
		return chr.getValue();
	}
	
	
	/**
	 * Wrap a ECDSA signature in the format r || s into a TLV encoding as defined by RFC 3279
	 * 
	 * @return ASN.1 encoded byte array containing two signed integer r and s
	 */
	private byte[] wrapECDSASignature() {
		byte[] signature = this.signature.getValue();
		int len = signature.length / 2;
		
		byte[] r = new byte[len + 1];
		System.arraycopy(signature, 0, r, 1, len);
		
		byte[] s = new byte[len + 1];
		System.arraycopy(signature, len, s, 1, len);
		
		ConstructedTLV sig = null;
		try {
			PrimitiveTLV wr = new PrimitiveTLV(Tag.INTEGER, r);
			PrimitiveTLV ws = new PrimitiveTLV(Tag.INTEGER, s);
			sig = new ConstructedTLV(new Tag(0x30));	// Tag 0x30 is an ASN.1 Sequence
			sig.add(wr);
			sig.add(ws);
		} catch (TLVEncodingException e) {
			log.fine(e.getLocalizedMessage());
		}
		return sig.getBytes();
	}

	@Override
	public void verify(PublicKey puk) throws CertificateException,
	NoSuchAlgorithmException, InvalidKeyException,
	NoSuchProviderException, SignatureException {
		
		Signature verifier = Signature.getInstance("SHA256withECDSA", "BC");
		verifier.initVerify(puk);
		verifier.update(this.body.getBytes());
		boolean verified = verifier.verify(wrapECDSASignature());
		
		if (!verified) throw new CertificateException("Certificate verification failed.");
	}



	@Override
	public void verify(PublicKey arg0, String arg1)
			throws CertificateException, NoSuchAlgorithmException,
			InvalidKeyException, NoSuchProviderException, SignatureException {
		// TODO Auto-generated method stub
		
	}
}
