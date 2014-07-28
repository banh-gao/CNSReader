/*
 * Copyright © 1998 Gemplus SCA
 * Av. du Pic de Bertagne - Parc d'Activités de Gémenos
 * BP 100 - 13881 Gémenos CEDEX
 * 
 * "Code derived from the original OpenCard Framework".
 * 
 * Everyone is allowed to redistribute and use this source  (source
 * code)  and binary (object code),  with or  without modification,
 * under some conditions:
 * 
 *  - Everyone  must  retain  and/or  reproduce the above copyright
 *    notice,  and the below  disclaimer of warranty and limitation
 *    of liability  for redistribution and use of these source code
 *    and object code.
 * 
 *  - Everyone  must  ask a  specific prior written permission from
 *    Gemplus to use the name of Gemplus.
 * 
 * DISCLAIMER OF WARRANTY
 * 
 * THIS CODE IS PROVIDED "AS IS",  WITHOUT ANY WARRANTY OF ANY KIND
 * (INCLUDING,  BUT  NOT  LIMITED  TO,  THE IMPLIED  WARRANTIES  OF
 * MERCHANTABILITY  AND FITNESS FOR  A  PARTICULAR PURPOSE)  EITHER
 * EXPRESS OR IMPLIED.  GEMPLUS DOES NOT WARRANT THAT THE FUNCTIONS
 * CONTAINED  IN THIS SOFTWARE WILL MEET THE USER'S REQUIREMENTS OR
 * THAT THE OPERATION OF IT WILL BE UNINTERRUPTED OR ERROR-FREE. NO
 * USE  OF  ANY  CODE  IS  AUTHORIZED  HEREUNDER EXCEPT UNDER  THIS
 * DISCLAIMER.
 * 
 * LIMITATION OF LIABILITY
 * 
 * GEMPLUS SHALL NOT BE LIABLE FOR INFRINGEMENTS OF  THIRD  PARTIES
 * RIGHTS. IN NO EVENTS, UNLESS REQUIRED BY APPLICABLE  LAW,  SHALL
 * GEMPLUS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES OF ANY CHARACTER  INCLUDING,
 * WITHOUT LIMITATION, DAMAGES FOR LOSS OF GOODWILL, WORK STOPPAGE,
 * COMPUTER FAILURE OR MALFUNCTION, OR ANY AND ALL OTHER DAMAGES OR
 * LOSSES, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. ALSO,
 * GEMPLUS IS  UNDER NO  OBLIGATION TO MAINTAIN,  CORRECT,  UPDATE, 
 * CHANGE, MODIFY, OR OTHERWISE SUPPORT THIS SOFTWARE.
 */
package opencard.opt.database; 

import java.util.Enumeration;
import java.util.Vector;

import opencard.core.service.CardServiceFactory;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.CardType;
import opencard.core.terminal.CardID;

/**
 * This class is the implementation of a CardServiceFactory that
 * provides access to a BasicDatabase CardService for a smartcard
 * that is compliant with ISO7816-7.
 *
 * @version $Id: BasicDatabaseFactory.java,v 1.1 1999/12/06 15:46:05 damke Exp $<br>
 * Changes: 
 * @author  Christophe.Muller@research.gemplus.com<br>
 *    <ul>
 *    <li> It supports the new style (1.1.1) card recognition (getCardType)
 *    <li> It uses a naming pattern where each CardService that could be
 *         instanciated provides its own <tt>knows</tt> method that does
 *         the card recognition.
 *    <li> It can detect theoritically any smartcard that is ISO7816-7
 *         compliant. To do that it uses a temporary SlotChannel (see
 *         <tt>CardServiceFactory</tt> class) and it:
 *         <ol>
 *            <li>sends a 'PRESENT USER ("PUBLIC")' command w/o password, and
 *            <li>expects a 9000 response.
 *         </ol><br>
 *         See the <tt>knows</tt> method for more details.
 *    </ul><br>
 * Note: For proprietary implementations, i.e., classes inheriting from
 * <tt>Basicdatabase</tt>, it is still possible to redefine an other
 * <tt>knows</tt> method that would be simpler (e.g., that would analyze
 * the card ATR in order to check that the card if of the right type). But
 * in this case, it is also necessary to redefine a factory because the
 * <tt>knows</tt> method is static and so the class exact name must be
 * known and specified by the factory.
 * 
 * @author  HAMEL Arnaud
 * @author  DANGREMONT Cedric
 * @author  Christophe.Muller@research.gemplus.com
 * @version $Id: BasicDatabaseFactory.java,v 1.1 1999/12/06 15:46:05 damke Exp $
 * @since   OCF1.2
 * 
 * @see BasicDatabase#knows
 */

public class BasicDatabaseFactory extends CardServiceFactory { 

    public final static int DATABASE_CARDTYPE = 0;

    public BasicDatabaseFactory() {
    }
   
    protected CardType getCardType(CardID cid, CardServiceScheduler sched) {
	if (BasicDatabase.knows(cid, sched)) {
            return new CardType(DATABASE_CARDTYPE);
	} else {
            return CardType.UNSUPPORTED;
	}
    }
   
    protected Enumeration getClasses(CardType type) {
	if (type.getType() == DATABASE_CARDTYPE) {
            Vector classes = new Vector ();
            classes.addElement(BasicDatabase.class);
            return (classes.elements());
	} else {
            System.out.println("getClasses: no classes - strange!");
            return null;
	}
    }
}
