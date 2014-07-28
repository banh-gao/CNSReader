package com.github.zeroDenial.CNS;
import java.nio.charset.Charset;

public class CardInfo {

	private static final Charset ID_CHARSET = Charset.forName("US-ASCII");

	private static final String CODE_REGEX = "[0-9]{16}";

	enum CardType {
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

	private CardType cardType;

	private String id;

	private String issuerCode;

	public static CardInfo decode(byte[] rawCode) {

		String id = new String(rawCode, ID_CHARSET);

		if (!id.matches(CODE_REGEX))
			throw new IllegalArgumentException("Invalid id");

		if (!checkCode(id))
			throw new IllegalArgumentException("Card id checksum failed");

		CardInfo cardId = new CardInfo();

		cardId.id = id;

		cardId.cardType = CardType.decode(id.charAt(0));
		if (cardId.cardType == null)
			throw new IllegalArgumentException("Unsupported card type");

		switch (cardId.cardType) {
			case CITIZEN_NATIONAL :
			case OPERATOR_NATIONAL :
			case CITIZEN_REGIONAL :
			case OPERATOR_REGIONAL :
				cardId.issuerCode = id.substring(1, 4);
				break;
			case CITIZEN_OTHER :
				cardId.issuerCode = id.substring(1, 7);
				break;
		}

		return cardId;
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

	public String getId() {
		return id;
	}

	public CardType getCardType() {
		return cardType;
	}

	public String getIssuerCode() {
		return issuerCode;
	}

	@Override
	public String toString() {
		return "CardID [cardType=" + cardType + ", id=" + id + ", issuerCode=" + issuerCode + "]";
	}

}
