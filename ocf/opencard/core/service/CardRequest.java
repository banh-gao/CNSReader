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


import opencard.core.terminal.CardTerminal;
import opencard.core.util.Tracer;


/**
 * A <tt>CardRequest</tt> is used for <tt>waitForCard()</tt> invocations and specifies
 * what kind of smart card an application is waited for.<p>
 *
 * @author   Dirk  Husemann (hud@zurich.ibm.com)
 * @author   Peter Trommler (trp@zurich.ibm.com)
 * @author   Mike  Wendler  (mwendler@de.ibm.com)
 * @version  $Id: CardRequest.java,v 1.3 1999/11/03 12:37:16 damke Exp $
 *
 * @see opencard.core.terminal.CardTerminalRegistry
 * @see opencard.core.terminal.CardTerminal
 * @see opencard.core.terminal.Slot
 */

public class CardRequest {

  private Tracer ctracer = new Tracer(CardRequest.class);

  /**
   * Wait behavior if cards already inserted are to be considered.
   * This is the default wait behavior.
   */
  public final static int ANYCARD = 1;

  /**
   * Wait behavior if cards already inserted are to be ignored.
   */
  public final static int NEWCARD = 2;


  /** The filter for interesting card IDs. */
  private CardIDFilter cardIDFilter = null;

  /** The timeout. Only valid if <tt>timeoutSet</tt> holds <tt>true</tt>. */
  private int timeout = 0;

  /** Whether a timeout has been specified. */
  private boolean timeoutSet = false;

  /** A card service that has to be available for the card. */
  private Class cardServiceClass = null;

  /** The terminal in which the card has to be available. */
  private CardTerminal terminal = null;

  /** The wait behavior, <tt>ANYCARD</tt> or <tt>NEWCARD</tt>. */
  private int how = ANYCARD;

  /**
  * Create a new CardRequest.
  * The constructor contains the most commonly used card request attributes.
  * Additional attributes can be set using the setXXX methods.
  * @param waitBehavior specify whether cards already present should
  *                      be included or ignored. This parameter is mandatory.
  * @param terminal      specify the terminal in which the card has to be
  *                      available. Specify null for all terminals.
  * @param cardServiceClass specify the card service interface (or
  *                         implementation) class that should be available
  *                         for the card. Specify null for all cards.
  *
  * <p>To set a timeout use <tt>setTimeout<tt>.
  * To set your own filter use <tt>setFilter<tt>.
  *
  * @see #NEWCARD
  * @see #ANYCARD
  * @see #setTimeout
  * @see #setFilter
  */
  public CardRequest(int waitBehavior, CardTerminal terminal,
                     Class cardServiceClass) {
    how=waitBehavior;
    this.terminal=terminal;
    this.cardServiceClass=cardServiceClass;
  }



  /**
   * Sets the <tt>timeout</tt> value of this <tt>CardRequest</tt>.
   * To unset the timeout, use a negative value.
   *
   * @param    timeout
   *           The timeout in seconds.
   */
  public void setTimeout(int timeout) {
    if (timeout < 0) {
      this.timeout = 0;
      timeoutSet   = false;
    } else {
      this.timeout = timeout;
      timeoutSet   = true;
    }
  }


  /**
   * Sets the filter of this <tt>CardRequest</tt>.
   * Only card IDs that pass this filter can be used to satisfy this request.
   * If the filter is set to <tt>null</tt>, all card IDs can be used.
   *
   * @param filter   the filter for card IDs that may satisfy this request
   */
  public void setFilter(CardIDFilter filter) {
    cardIDFilter = filter;
  }



  /**
   * Gets the filter of this <tt>CardRequest</tt>.
   *
   * @return the filter for card IDs, or <tt>null</tt> if not set
   */
  public CardIDFilter getFilter() {
    return cardIDFilter;
  }


  /**
   * Gets the service required by this <tt>CardRequest</tt>.
   *
   * @return    the <tt>CardService</tt> that has to be supported,
   *            or <tt>null</tt> if not set
   */
  public Class getCardServiceClass() {
    return cardServiceClass;
  }


  /**
   * Gets the <tt>CardTerminal</tt> of this <tt>CardRequest</tt>.
   *
   * @return    the <tt>CardTerminal</tt> which has to satisfy this request,
   *            or <tt>null</tt> if not set
   */
  public CardTerminal getCardTerminal() {
    return terminal;
  }


  /**
   * Gets the <tt>timeout</tt> value of this <tt>CardRequest</tt>.
   *
   * @return   The timeout value in seconds, or <tt>null</tt> if not set.
   */
  public int getTimeout() {
    return timeout;
  }


  /**
   * Return the wait behavior of the <tt>CardRequest</tt>.
   *
   * @return the waiting behavior, either <tt>ANYCARD</tt> or </tt>NEWCARD</tt>
   *
   * @see #ANYCARD
   * @see #NEWCARD
   */
  public int getWaitBehavior() {
    return how;
  }


  /**
   * Determines whether a timeout period is set.
   *
   * @return    <TT>true</TT> if the timeout has been set,
   *            <TT>false</TT> otherwise
   */
  public boolean isTimeoutSet() {
    return timeoutSet;
  }


  /**
   * Returns a string representation of this card request.
   *
   * @return a human-readable representation of this request
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer(super.toString());

    String s = " unknown wait behavior";
    switch (how)
      {
      case ANYCARD:
        s = " ANYCARD";
        break;
      case NEWCARD:
        s = " NEWCARD";
        break;
      }
    sb.append(s);

    if (cardIDFilter != null)
      sb.append("\nfilter   = ").append(cardIDFilter);
    if (cardServiceClass != null)
      sb.append("\nservice  = ").append(cardServiceClass);
    if (terminal != null)
      sb.append("\nterminal = ").append(terminal);
    if (timeoutSet)
      sb.append("\ntimeout  = ").append(timeout);

    return sb.toString();
  }

  /**
   * @deprecated
   */
  public CardRequest() {
    ctracer.debug("<init>", "plain constructor");
  }


  /**
   * @deprecated
   */
  public CardRequest(int timeout) {
    ctracer.debug("<init>", "(" + timeout + ")");
    setTimeout(timeout);
  }


  /**
   * @deprecated
   */
  public CardRequest(CardIDFilter filter) {
    ctracer.debug("<init>", "(" + filter + ")");
    setFilter(filter);
  }


  /**
   * @deprecated
   */
  public CardRequest(CardIDFilter filter, int timeout) {
    ctracer.debug("<init>", "(" + filter + "," + timeout + ")");
    setFilter(filter);
    setTimeout(timeout);
  }



  /**
   * @deprecated
   */
  public CardRequest(Class cardServiceClass) {
    ctracer.debug("<init>", "(" + cardServiceClass + ")");
    setCardServiceClass(cardServiceClass);
  }


  /**
   * @deprecated
   */
  public CardRequest(Class cardServiceClass, int timeout) {
    ctracer.debug("<init>", "(" + cardServiceClass + ", " + timeout + ")");
    setCardServiceClass(cardServiceClass);
    setTimeout(timeout);
  }



  /**
   * @deprecated
   */
  public void setCardServiceClass(Class cardServiceClass) {
    this.cardServiceClass = cardServiceClass;
  }


  /**
   * @deprecated
   */
  public void setCardTerminal(CardTerminal terminal) {
    this.terminal = terminal;
  }




  /**
   * @deprecated
   */
  public void setWaitBehavior(int how) {
    this.how = how;
  }



} // class CardRequest
