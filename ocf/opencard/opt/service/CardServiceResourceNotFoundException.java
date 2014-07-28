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


import opencard.core.service.CardServiceException;


/**
 * Exception thrown when required resources are not found.
 * This exception can indicate a misuse of a service or service component
 * as well as an error that was not caused by the application.
 * <br>
 * A situation in which this exception would be thrown is a service
 * that supports user friendly names for card objects, and is missing
 * a dictionary to resolve them.
 * The error would have been caused by the application if it did not
 * provide a dictionary. The error would not have been caused by the
 * application if it did provide a URL, and the dictionary could not
 * be loaded due to a network error.
 *
 * @author  Thomas Schaeck (schaeck@de.ibm.com)
 * @author  Roland Weber  (rolweber@de.ibm.com)
 *
 * @version $Id: CardServiceResourceNotFoundException.java,v 1.1.1.1 1999/10/05 15:08:48 damke Exp $
 */
public class CardServiceResourceNotFoundException
    extends CardServiceException
{
  /**
   * Creates a new exception without detail message.
   */
  public CardServiceResourceNotFoundException()
  {
    super();
  }

  /**
   * Creates a new exception with the specified detail message.
   *
   * @param message   a string indicating why this exception is thrown
   */
  public CardServiceResourceNotFoundException(String message)
  {
    super(message);
  }

} // class CardServiceResourceNotFoundException
