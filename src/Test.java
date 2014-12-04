import opencard.core.service.CardRequest;
import opencard.core.service.SmartCard;
import opencard.opt.iso.fs.FileAccessCardService;
import com.github.zeroDenial.CNS.CNSCard;

public class Test {

	public static void main(String[] args) {
		try {

			SmartCard.start();
			CardRequest cr = new CardRequest(CardRequest.ANYCARD, null, FileAccessCardService.class);
			CNSCard card = CNSCard.getInstance(SmartCard.waitForCard(cr));
			System.out.println(card.getPersonalData());
			System.out.println(card.getCardInfo());
			System.out.println(card.getCertificate());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
