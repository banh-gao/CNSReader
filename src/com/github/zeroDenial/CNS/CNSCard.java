package com.github.zeroDenial.CNS;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import opencard.core.OpenCardException;
import opencard.core.service.CardServiceException;
import opencard.core.service.SmartCard;
import opencard.opt.iso.fs.CardFile;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.iso.fs.FileAccessCardService;
import opencard.opt.security.PrivateKeyFile;
import opencard.opt.security.PublicKeyFile;
import opencard.opt.signature.SignatureCardService;
import org.bouncycastle.asn1.x509.X509NameTokenizer;
import org.bouncycastle.util.encoders.Base64;

public class CNSCard {

	private static final String CARDID__PATH = ":1000:1003";
	private static final String CERTIFICATE_PATH = ":1100:1101";
	private static final String PRIV_KEY_PATH = ":1100:1101";
	private static final String PUB_KEY_PATH = ":3f01";
	private static final String DATI_PERS_PATH = ":1100:1102";

	private static final String DATI_PERS_HASH_ALG = "SHA1";
	private static final Charset DATI_PERS_HASH_CHARSET = Charset.forName("US-ASCII");

	private final FileAccessCardService facs;
	private final SignatureCardService scs;

	public CNSCard(SmartCard sc) throws CardServiceException {
		try {
			facs = (FileAccessCardService) sc.getCardService(FileAccessCardService.class, true);
			// TODO: scs = (SignatureCardService)
			// sc.getCardService(SignatureCardService.class, true);
			scs = null;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public CardInfo getCardInfo() throws Exception {
		return CardInfo.decode(readFile(CARDID__PATH));
	}

	public byte[] signData(byte[] data, String signAlg) throws Exception {
		// create the private key reference
		CardFilePath path = new CardFilePath(":3F00:C200");
		PrivateKeyFile keyRef = new PrivateKeyFile(path, 0);
		return scs.signData(keyRef, signAlg, data);
	}

	public boolean verifySign(byte[] data, String signAlg, byte[] sign) throws Exception {
		CardFilePath path = new CardFilePath(PUB_KEY_PATH);
		PublicKeyFile keyRef = new PublicKeyFile(path, 1);
		return scs.verifySignedData(keyRef, signAlg, data, sign);
	}

	public X509Certificate getCertificate() throws Exception {
		byte[] data = readFile(CERTIFICATE_PATH);

		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(data));
	}

	public DatiPersonali getPersonalData() throws Exception {
		byte[] data = readFile(DATI_PERS_PATH);
		return DatiPersonali.decode(data);
	}

	private byte[] readFile(String path) throws FileNotFoundException, OpenCardException {
		CardFile root = new CardFile(facs);
		CardFile file = new CardFile(root, path);
		return facs.read(file.getPath(), 0, file.getLength());
	}

	public boolean checkDataConsistency() throws Exception {
		byte[] datiPersonali = readFile(DATI_PERS_PATH);
		String codFiscale = DatiPersonali.decode(datiPersonali).getCodFiscale();

		String cardId = getCardInfo().getId();
		String datiPersHash = new String(Base64.encode(MessageDigest.getInstance(DATI_PERS_HASH_ALG).digest(datiPersonali)), DATI_PERS_HASH_CHARSET);

		String expectedCN = codFiscale + "/" + cardId + "." + datiPersHash;

		X509Certificate cert = getCertificate();
		System.out.println(expectedCN);
		System.out.println(new X509NameTokenizer(cert.getSubjectDN().getName()).nextToken());

		// return
		// expectedCN.equals(IETFUtils.valueToString(cn.getFirst().getValue()));
		return false;
	}
}