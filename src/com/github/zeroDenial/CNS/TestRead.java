package com.github.zeroDenial.CNS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import opencard.core.service.CardRequest;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.opt.iso.fs.FileAccessCardService;

public class TestRead {

	public static void main(String[] args) {
		try {
			SmartCard.start();

			@SuppressWarnings("unchecked")
			List<CardTerminal> terminals = Collections.list(CardTerminalRegistry.getRegistry().getCardTerminals());

			CardTerminal term = terminals.get(0);

			System.out.println(String.format("Extracting card information from terminal %s ...", term.getName()));

			CardRequest cr = new CardRequest(CardRequest.ANYCARD, term, FileAccessCardService.class);

			SmartCard sc = SmartCard.waitForCard(cr);

			CNSCard cns = new CNSCard(sc);

			cns.checkDataConsistency();

			DatiPersonali persData = cns.getPersonalData();

			System.out.println(persData);

			File dir = new File(persData.getCognome() + "_" + persData.getNome());
			dir.mkdir();

			// Scrittura dati personali
			PrintWriter persWrt = new PrintWriter(new File(dir, "PERSONAL_DATA.txt"));
			persWrt.println(persData);
			persWrt.close();

			// Scrittura ID Carta
			PrintWriter idWrt = new PrintWriter(new File(dir, "CARD_ID.txt"));
			idWrt.println(cns.getCardInfo());
			idWrt.close();

			// Scrittura certificato
			OutputStream certOut = new FileOutputStream(new File(dir, "cert.der"));
			certOut.write(cns.getCertificate().getEncoded());
			certOut.close();

			System.out.println(String.format("Information extracted in directory %s", dir.getAbsolutePath()));

			sc.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally { // even in case of an error...
			try {
				SmartCard.shutdown();
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}
}
