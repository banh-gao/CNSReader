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

/**
 * Exception thrown when a credential that was provided is invalid.
 * An application's credential may be invalid because it is not in a format
 * that can be interpreted by the service, or if it is not accepted by the
 * smartcard.
 * A smartcard's credential is invalid if it does not correspond with a
 * credential provided by the application. This may be detected if the
 * smartcard has to append a cryptographic MAC (message authentication code)
 * to it's response, and the verification of that MAC fails. Of course, this
 * may also happen if the smartcard's response was modified on it's way
 * to the service.
 *
 * @author  Thomas Schaeck (schaeck@de.ibm.com)
 * @author  Roland Weber  (rolweber@de.ibm.com)
 *
 * @version $Id: CardServiceInvalidCredentialException.java,v 1.1.1.1 1999/10/05 15:34:31 damke Exp $
 */
public class CardServiceInvalidCredentialException
    extends CardServiceUsageException
{
  /**
   * Creates a new exception without detail message.
   */
  public CardServiceInvalidCredentialException()
  {
    super();
  }

  /**
   * Creates a new exception with the given detail message.
   *
   * @param msg   a string indicating why this exception is thrown
   */
  public CardServiceInvalidCredentialException(String msg)
  {
    super(msg);
  }

} // class CardServiceInvalidCredentialException
