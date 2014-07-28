package com.github.zeroDenial.CNS;
import java.io.IOException;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatiPersonali {

	private static final Charset DATA_CHARSET = Charset.forName("US-ASCII");

	private static final int SKIPPED_HEAD = 6;

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("ddMMyyyy");

	private int emittente;
	private Date dataEmissione;
	private Date dataScadenza;

	private String cognome;
	private String nome;
	private Date dataNascita;
	private String sesso;
	private int statura;
	private int cittadinanza;

	private String codFiscale;

	private String comuneNascita;
	private String statoNascita;
	private String docNascita;

	private String comuneResidenza;
	private String viaResidenza;

	private String notaEspatrio;

	public static DatiPersonali decode(byte[] data) throws Exception {
		StringReader r = new StringReader(new String(data, DATA_CHARSET));

		r.skip(SKIPPED_HEAD);

		DatiPersonali d = new DatiPersonali();

		String field = readField(r);
		if (!field.isEmpty())
			d.emittente = Integer.valueOf(field);

		field = readField(r);
		if (!field.isEmpty())
			d.dataEmissione = DATE_FORMAT.parse(field);

		field = readField(r);
		if (!field.isEmpty())
			d.dataScadenza = DATE_FORMAT.parse(field);

		field = readField(r);
		if (!field.isEmpty())
			d.cognome = field;

		field = readField(r);
		if (!field.isEmpty())
			d.nome = field;

		field = readField(r);
		if (!field.isEmpty())
			d.dataNascita = DATE_FORMAT.parse(field);

		field = readField(r);
		if (!field.isEmpty())
			d.sesso = field;

		field = readField(r);
		if (!field.isEmpty())
			d.statura = Integer.valueOf(field);

		field = readField(r);
		if (!field.isEmpty())
			d.codFiscale = field;

		field = readField(r);
		if (!field.isEmpty())
			d.cittadinanza = Integer.valueOf(field);

		field = readField(r);
		if (!field.isEmpty())
			d.comuneNascita = field;

		field = readField(r);
		if (!field.isEmpty())
			d.statoNascita = field;

		field = readField(r);
		if (!field.isEmpty())
			d.docNascita = field;

		field = readField(r);
		if (!field.isEmpty())
			d.comuneResidenza = field;

		field = readField(r);
		if (!field.isEmpty())
			d.viaResidenza = field;

		field = readField(r);
		if (!field.isEmpty())
			d.notaEspatrio = field;

		return d;
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

	private static int readFieldLength(StringReader r) throws IOException {
		CharBuffer strLen = CharBuffer.allocate(2);
		r.read(strLen);
		return Integer.valueOf(strLen.rewind().toString(), 16);
	}

	public int getEmittente() {
		return emittente;
	}

	public Date getDataEmissione() {
		return dataEmissione;
	}

	public Date getDataScadenza() {
		return dataScadenza;
	}

	public String getCognome() {
		return cognome;
	}

	public String getNome() {
		return nome;
	}

	public Date getDataNascita() {
		return dataNascita;
	}

	public String getSesso() {
		return sesso;
	}

	public int getStatura() {
		return statura;
	}

	public int getCittadinanza() {
		return cittadinanza;
	}

	public String getCodFiscale() {
		return codFiscale;
	}

	public String getComuneNascita() {
		return comuneNascita;
	}

	public String getStatoNascita() {
		return statoNascita;
	}

	public String getDocNascita() {
		return docNascita;
	}

	public String getComuneResidenza() {
		return comuneResidenza;
	}

	public String getViaResidenza() {
		return viaResidenza;
	}

	public String getNotaEspatrio() {
		return notaEspatrio;
	}

	@Override
	public String toString() {
		return "DatiPersonali [emittente=" + emittente + ", dataEmissione=" + dataEmissione + ", dataScadenza=" + dataScadenza + ", cognome=" + cognome + ", nome=" + nome + ", dataNascita=" + dataNascita + ", sesso=" + sesso + ", statura=" + statura + ", cittadinanza=" + cittadinanza + ", codFiscale=" + codFiscale + ", comuneNascita=" + comuneNascita + ", statoNascita=" + statoNascita + ", docNascita=" + docNascita + ", comuneResidenza=" + comuneResidenza + ", viaResidenza=" + viaResidenza + ", notaEspatrio=" + notaEspatrio + "]";
	}
}
