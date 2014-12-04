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
 * Type of a card as determined by a cardservice factory.
 * The CardID represents the ATR of a unique card while the
 * CardType represents a classification of cards according to
 * the card classification scheme used by the cardservice factory.
 * In most cases the card service factory will simply use
 * a numeric value to classify a card.
 * Optionally any information describing the card type can be
 * attached using setCardInfo.
 *
 * @version $Id: CardType.java,v 1.1.1.1 1999/10/05 15:34:31 damke Exp $
 *
 * @author Peter Bendel (peter_bendel@de.ibm.com)
 *
 * @see opencard.core.terminal.CardID
 * @see opencard.core.service.CardServiceFactory
 */
public class CardType {
  /** 
   * Reserved instance of card type to be used for cards that are not
   * supported by a card service factory.
   */
  static public  CardType UNSUPPORTED = new CardType();

  /** each card type is represented as an int by default */
  private int type;
  /** optionally additonal info may be associated with the card type */
  private Object info;

  /** Default constructor */
  public CardType() {
  }

  /** Constructor from integer. Should be used by CardServiceFactory subclasses */
  public CardType(int type) {
    this.type=type;
  }

  /** Accessor for numeric type */
  public int getType() {
    return type;
  }

  /** Attach additional information with the CardType that can be used
   * when instantiating card services.
   */
  public void setInfo(Object cardInfo) {
    this.info=cardInfo;
  };

  public Object getInfo() {
    return info;
  };



}
