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

package opencard.opt.service;


import opencard.core.service.CHVDialog;
import opencard.core.service.SmartCard;


/**
 * An interface to the public methods in <tt>CardService</tt>.
 * The optional parts of OCF define interfaces to standard card
 * services, for example for file access. Since interfaces cannot
 * extend classes, this interface provides access to the public
 * methods in class <tt>SmartCard</tt>. It will be extended by the
 * standard service interfaces.
 * <br>
 * Without this interface, applications would have to downcast from
 * a particular interface to class <tt>CardService</tt> in order to
 * access these methods. The exception to this rule is the method
 * <tt>getCHVDialog</tt>, which is not intended to be invoked by an
 * application.
 *
 * @version $Id: CardServiceInterface.java,v 1.2 1999/11/03 12:37:18 damke Exp $
 *
 * @author Roland Weber (rolweber@de.ibm.com)
 *
 * @see opencard.core.service.CardService
 * @see opencard.core.service.CardService#getCHVDialog
 */
public interface CardServiceInterface
{
  /**
   * Provides an application-specific dialog for CHV input.
   * If an application does not set it's own dialog, a default
   * dialog will be used if password input is required.
   *
   * @param dialog   the dialog to use for querying a password or PIN
   *
   * @see opencard.core.service.CardService#setCHVDialog
   */
  public void setCHVDialog(CHVDialog dialog);


  /**
   * Returns the corresponding smartcard object.
   * Every service has been created using a particular instance of
   * <tt>SmartCard</tt>. This method can be used to obtain the instance
   * of <tt>SmartCard</tt> that has been used to create the service
   * for which it is invoked.
   *
   * @return  the smartcard object associated with this service
   *
   * @see opencard.core.service.CardService#getCard
   */
  public SmartCard getCard();


} // interface CardServiceInterface
