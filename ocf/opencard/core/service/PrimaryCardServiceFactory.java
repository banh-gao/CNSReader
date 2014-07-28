/*
 * Copyright © 1997 - 1999 IBM Corporation.
 * 
 * Redistribution and use in source (source code) and binary (object code)
 * forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 1. Redistributed source code must retain the above copyright notice, this
 * list of conditions and the disclaimer below.
 * 2. Redistributed object code must reproduce the above copyright notice,
 * this list of conditions and the disclaimer below in the documentation
 * and/or other materials provided with the distribution.
 * 3. The name of IBM may not be used to endorse or promote products derived
 * from this software or in any other form without specific prior written
 * permission from IBM.
 * 4. Redistribution of any modified code must be labeled "Code derived from
 * the original OpenCard Framework".
 * 
 * THIS SOFTWARE IS PROVIDED BY IBM "AS IS" FREE OF CHARGE. IBM SHALL NOT BE
 * LIABLE FOR INFRINGEMENTS OF THIRD PARTIES RIGHTS BASED ON THIS SOFTWARE.  ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IBM DOES NOT WARRANT THAT THE FUNCTIONS CONTAINED IN THIS
 * SOFTWARE WILL MEET THE USER'S REQUIREMENTS OR THAT THE OPERATION OF IT WILL
 * BE UNINTERRUPTED OR ERROR-FREE.  IN NO EVENT, UNLESS REQUIRED BY APPLICABLE
 * LAW, SHALL IBM BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  ALSO, IBM IS UNDER NO OBLIGATION
 * TO MAINTAIN, CORRECT, UPDATE, CHANGE, MODIFY, OR OTHERWISE SUPPORT THIS
 * SOFTWARE.
 */


package opencard.core.service;

import opencard.core.terminal.SlotChannel;

/** Of all the <tt>CardServiceFactories</tt> available for a particular smart
 * card, one must be the <i>primary</i> <tt>CardServiceFactory</tt>. The
 * <tt>CardServiceScheduler</tt> will invoke the <tt>setupSmartCard()</tt>
 * method when a smart card is accessed the first time.<p>
 *
 * @author  Dirk Husemann (hud@zurich.ibm.com)
 * @version $Id: PrimaryCardServiceFactory.java,v 1.1.1.1 1999/10/05 15:34:31 damke Exp $
 *
 * @see opencard.core.service.CardServiceRegistry
 * @see opencard.core.service.CardServiceFactory
 * @see opencard.core.service.CardService
 */
public interface PrimaryCardServiceFactory {
  /** Before a smart card is accessed the first time, <tt>CardServiceRegistry</tt>
   * will invoke <tt>setupSmartCard()</tt> on the first <tt>PrimaryCardServiceFactory</tt>
   * it finds.
   *
   * @param     slotChannel
   *		The <tt>SlotChannel</tt> to the smart card.
   */
  public void setupSmartCard(SlotChannel slotChannel);
}

 // $Log: PrimaryCardServiceFactory.java,v $
 // Revision 1.1.1.1  1999/10/05 15:34:31  damke
 // Import OCF1.1.1 from Zurich
 //
 // Revision 1.2  1999/08/09 11:18:30  ocfadmin
 // replacing OCF 1.1 with updates of OCF 1.1.1 (aka Hudson) as of Mai 1999 (by J.Damke)
 //
 // Revision 1.4  1998/04/14 14:22:18  breid
 // CVS Log-Keyword added
 //
