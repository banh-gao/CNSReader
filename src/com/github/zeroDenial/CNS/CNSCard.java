package com.github.zeroDenial.CNS;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import opencard.core.OpenCardException;
import opencard.core.service.CardServiceException;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminalException;
import opencard.opt.iso.fs.CardFile;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.iso.fs.FileAccessCardService;
import opencard.opt.security.PrivateKeyFile;
import opencard.opt.security.PublicKeyFile;
import opencard.opt.signature.SignatureCardService;
import org.bouncycastle.asn1.x509.X509NameTokenizer;
import org.bouncycastle.util.encoders.Base64;
import com.github.zeroDenial.utils.AttributeMap;
import com.github.zeroDenial.utils.AttributeMap.Attribute;

public class CNSCard {

	// Dati emissione carta
	public static final Attribute<String> CARD_ID = Attribute.create("CARD_ID", String.class);
	public static final Attribute<CardType> CARD_TYPE = Attribute.create("CARD_TYPE", CardType.class);
	public static final Attribute<String> ISSUER_CODE = Attribute.create("ISSUER_CODE", String.class);

	// Carta Nazionale Servizi (CNS)
	public static final Attribute<Integer> EMITTENTE = Attribute.create("EMITTENTE", Integer.class);
	public static final Attribute<Date> DATA_EMIS = Attribute.create("DATA_EMIS", Date.class);
	public static final Attribute<Date> DATA_SCAD = Attribute.create("DATA_SCAD", Date.class);
	public static final Attribute<String> COGNOME = Attribute.create("COGNOME", String.class);
	public static final Attribute<String> NOME = Attribute.create("NOME", String.class);
	public static final Attribute<Date> DATA_NASC = Attribute.create("DATA_NASC", Date.class);
	public static final Attribute<String> SESSO = Attribute.create("SESSO", String.class);

	// Carta Identita Elettronica (CIA)
	public static final Attribute<Integer> STATURA = Attribute.create("STATURA", Integer.class);
	public static final Attribute<String> COD_FISC = Attribute.create("COD_FISC", String.class);
	public static final Attribute<Integer> CITTADINANZA = Attribute.create("CITTADINANZA", Integer.class);
	public static final Attribute<String> COM_NASC = Attribute.create("COM_NASC", String.class);
	public static final Attribute<String> STATO_NASC = Attribute.create("STATO_NASC", String.class);
	public static final Attribute<String> DOC_NASC = Attribute.create("DOC_NASC", String.class);
	public static final Attribute<String> COM_RESI = Attribute.create("COM_RESI", String.class);
	public static final Attribute<String> VIA_RESI = Attribute.create("VIA_RESI", String.class);
	public static final Attribute<String> ESPATRIO = Attribute.create("ESPATRIO", String.class);

	public enum CardType {
		CITIZEN_NATIONAL('5'),
		CITIZEN_REGIONAL('6'),
		CITIZEN_OTHER('7'),
		OPERATOR_NATIONAL('8'),
		OPERATOR_REGIONAL('9');

		private final char code;

		private CardType(char code) {
			this.code = code;
		}

		public static CardType decode(char v) {
			for (CardType t : values())
				if (t.code == v)
					return t;
			return null;
		}
	}

	static final String CARDID_PATH = ":1000:1003";
	static final String CERTIFICATE_PATH = ":1100:1101";
	static final String PRIV_KEY_PATH = ":1100:1101";
	static final String PUB_KEY_PATH = ":3f01";
	static final String DATI_PERS_PATH = ":1100:1102";

	static final String DATI_PERS_HASH_ALG = "SHA1";
	static final Charset CARD_CHARSET = Charset.forName("US-ASCII");

	static final String CARD_ID_REGEX = "[0-9]{16}";

	private final FileAccessCardService facs;
	private final SignatureCardService scs;

	private CNSCard(SmartCard sc) throws CardServiceException {
		try {
			facs = (FileAccessCardService) sc.getCardService(FileAccessCardService.class, true);
			// TODO: scs = (SignatureCardService)
			// sc.getCardService(SignatureCardService.class, true);
			scs = null;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static CNSCard getInstance(SmartCard sc) throws CardServiceException {
		return new CNSCard(sc);
	}

	public byte[] signData(byte[] data, String signAlg) throws CardServiceException, InvalidKeyException, CardTerminalException {
		// create the private key reference
		CardFilePath path = new CardFilePath(":3F00:C200");
		PrivateKeyFile keyRef = new PrivateKeyFile(path, 0);
		return scs.signData(keyRef, signAlg, data);
	}

	public boolean verifySign(byte[] data, String signAlg, byte[] sign) throws CardServiceException, InvalidKeyException, CardTerminalException {
		CardFilePath path = new CardFilePath(PUB_KEY_PATH);
		PublicKeyFile keyRef = new PublicKeyFile(path, 1);
		return scs.verifySignedData(keyRef, signAlg, data, sign);
	}

	public X509Certificate getCertificate() throws FileNotFoundException, OpenCardException, CertificateException {
		byte[] data = readFile(CERTIFICATE_PATH);

		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(data));
	}

	public AttributeMap getPersonalData() throws IOException, OpenCardException, ParseException {
		byte[] data = readFile(DATI_PERS_PATH);
		return PersonalDataParser.decode(data);
	}

	public AttributeMap getCardInfo() throws IOException, OpenCardException {
		byte[] data = readFile(CARDID_PATH);
		return CardIdParser.decode(new String(data, CNSCard.CARD_CHARSET));
	}

	public boolean checkDataConsistency() throws Exception {
		byte[] datiPersonali = readFile(DATI_PERS_PATH);
		String codFiscale = PersonalDataParser.decode(datiPersonali).get(COD_FISC);

		String cardId = getCardInfo().get(CARD_ID);
		String datiPersHash = new String(Base64.encode(MessageDigest.getInstance(DATI_PERS_HASH_ALG).digest(datiPersonali)), CARD_CHARSET);

		String expectedCN = codFiscale + "/" + cardId + "." + datiPersHash;

		X509Certificate cert = getCertificate();
		System.out.println(expectedCN);
		System.out.println(new X509NameTokenizer(cert.getSubjectDN().getName()).nextToken());

		// return
		// expectedCN.equals(IETFUtils.valueToString(cn.getFirst().getValue()));
		return false;
	}

	private byte[] readFile(String path) throws FileNotFoundException, OpenCardException {
		CardFile root = new CardFile(facs);
		CardFile file = new CardFile(root, path);
		return facs.read(file.getPath(), 0, file.getLength());
	}
}

class PersonalDataParser {

	private transient static final Charset DATA_CHARSET = Charset.forName("US-ASCII");

	private transient static final int SKIPPED_HEAD = 6;

	private transient static final DateFormat DATE_FORMAT = new SimpleDateFormat("ddMMyyyy");

	public static AttributeMap decode(byte[] data) throws IOException, ParseException {
		StringReader r = new StringReader(new String(data, DATA_CHARSET));

		r.skip(SKIPPED_HEAD);

		AttributeMap m = new AttributeMap();

		parseAndSetField(CNSCard.EMITTENTE, m, r);
		parseAndSetField(CNSCard.DATA_EMIS, m, r);
		parseAndSetField(CNSCard.DATA_SCAD, m, r);
		parseAndSetField(CNSCard.COGNOME, m, r);
		parseAndSetField(CNSCard.NOME, m, r);
		parseAndSetField(CNSCard.DATA_NASC, m, r);
		parseAndSetField(CNSCard.SESSO, m, r);

		parseAndSetField(CNSCard.STATURA, m, r);
		parseAndSetField(CNSCard.COD_FISC, m, r);
		parseAndSetField(CNSCard.CITTADINANZA, m, r);
		parseAndSetField(CNSCard.COM_NASC, m, r);
		parseAndSetField(CNSCard.STATO_NASC, m, r);
		parseAndSetField(CNSCard.DOC_NASC, m, r);
		parseAndSetField(CNSCard.COM_RESI, m, r);
		parseAndSetField(CNSCard.VIA_RESI, m, r);
		parseAndSetField(CNSCard.ESPATRIO, m, r);

		return m;
	}

	private static String readField(StringReader r) {
		try {
			int fldLength = readFieldLength(r);
			CharBuffer buf = CharBuffer.allocate(fldLength);
			r.read(buf);
			return buf.rewind().toString();
		} catch (Exception e) {
			return "";
		}
	}

	private static <T> void parseAndSetField(Attribute<T> attr, AttributeMap m, StringReader r) throws ParseException {
		String field = readField(r);
		if (field.isEmpty())
			return;

		Object v;

		if (attr.getType().isAssignableFrom(Integer.class))
			v = Integer.valueOf(field);
		else if (attr.getType().isAssignableFrom(Date.class))
			v = DATE_FORMAT.parse(field);
		else
			v = field;

		m.put(attr, attr.cast(v));

	}

	private static int readFieldLength(StringReader r) throws IOException {
		CharBuffer strLen = CharBuffer.allocate(2);
		r.read(strLen);
		return Integer.valueOf(strLen.rewind().toString(), 16);
	}
}

class CardIdParser {

	public static AttributeMap decode(String id) {
		if (!id.matches(CNSCard.CARD_ID_REGEX))
			throw new IllegalArgumentException("Invalid id");

		if (!checkCode(id))
			throw new IllegalArgumentException("Card id checksum failed");

		AttributeMap m = new AttributeMap();

		m.put(CNSCard.CARD_ID, id);

		CNSCard.CardType t = CNSCard.CardType.decode(id.charAt(0));
		if (t == null)
			throw new IllegalArgumentException("Unsupported card type");

		m.put(CNSCard.CARD_TYPE, t);

		switch (t) {
			case CITIZEN_NATIONAL :
			case OPERATOR_NATIONAL :
			case CITIZEN_REGIONAL :
			case OPERATOR_REGIONAL :
				m.put(CNSCard.ISSUER_CODE, id.substring(1, 4));
				break;
			case CITIZEN_OTHER :
				m.put(CNSCard.ISSUER_CODE, id.substring(1, 7));
				break;
		}

		return m;
	}

	private static boolean checkCode(String code) {
		int checkDigit = Character.getNumericValue(code.charAt(15));

		code = code.substring(0, 15);

		int somma = 0, daSommare, cifraRaddoppiata;

		if (code.length() % 2 != 0)
			code = "0" + code;

		for (int i = 0; i < code.length(); i++) {
			int cifraCorrente = Character.getNumericValue(code.charAt(i));
			if ((i % 2) != 0) {
				cifraRaddoppiata = cifraCorrente * 2;
				if (cifraRaddoppiata >= 10)
					daSommare = 1 + (cifraRaddoppiata % 10);
				else
					daSommare = cifraRaddoppiata;
			} else
				daSommare = cifraCorrente;

			somma += daSommare;
		}

		if (somma % 10 == 0)
			return checkDigit == 0;
		else
			return checkDigit == (10 - (somma % 10));
	}
}