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


import opencard.core.event.CTListener;
import opencard.core.event.CardTerminalEvent;
import opencard.core.event.EventGenerator;
import opencard.core.terminal.CardTerminalException;
import opencard.core.util.Tracer;

/**
 * CardWaiter provides a mechanism for waiting for a smartcard. A temporary card
 * waiter object is registered as a card terminal event listener. Its waitForCard
 * method blocks until its cardInserted method receives an event from which a
 * smart card object matching the request passed in the constructor can be
 * constructed.
 *
 * @author  Thomas Schaeck (schaeck@de.ibm.com)
 *
 * @version $Id: CardWaiter.java,v 1.2 1999/10/22 14:43:28 damke Exp $
 *
 * @see opencard.core.service.SmartCard
 */
class CardWaiter implements CTListener {
  private static Tracer ctracer = new Tracer (CardWaiter.class);

  /** SmartCard object created from last received event. */
  private SmartCard smartCard_ = null;

  /** CardRequest to be satisfied when waiting. */
  private CardRequest cardRequest_ = null;

  /** lock handle provided by lock owner */
  private Object lockHandle_ = null;

  /** Exception potentially catched in cardInserted(), which can only can be re-thrown
    in waitForCard(). */
  private CardTerminalException cardTerminalException_ = null;
  /**
   * Creates new instance.
   * Register as a CTListener to be informed when a card is inserted.
   *
   * @param r The request specifying wait parameters.
   */
  public CardWaiter(CardRequest cardRequest, Object lockHandle) throws CardTerminalException
  {
    ctracer.debug("<init>", "Request: " + cardRequest);
    cardRequest_ = cardRequest;
    lockHandle_ = lockHandle;
  }
  /**
   * Reacts on card insertion event. Tries to obtain a SmartCard object and
   * stores it to be obtained by <tt>waitForCard</tt> later on.
   *
   * @param ctEvent CardTerminalEvent reveived.
   */
  public void cardInserted(CardTerminalEvent ctEvent) {
    if (smartCard_ ==null) {

      ctracer.debug("cardInserted", "CTEvent: " + ctEvent);
      try {
        // It is necessary to obtain the smart card object here, because
        // getSmartCard() checks whether the event matches the request.
        smartCard_ = SmartCard.getSmartCard(ctEvent, cardRequest_, lockHandle_);

        // Storing the event here and getting the smart card object in waitForCard()
        // won't work. The trick is to notify only when a MATCHING event occurrs.
        // This prevents useless re-waiting in waitForCard.
        if (smartCard_ != null) {
          synchronized (this) {
            notify();
          }
        }
      } catch (CardTerminalException e) {
        // We can't throw the exception here, so we store it and notify "this" so
        // that the thread blocked in waitForCard() is awakened and can rethrow the
        // exception immediately.
        cardTerminalException_ = e;
        synchronized (this) {
          notify();
        }
      }
    }
  }
  /**
   * Reacts on card removal events. Not of interest here.
   *
   * @param ctEvent CardTerminalEvent reveived.
   */
  public void cardRemoved(CardTerminalEvent e) {}
  /**
   * Waits for a smartcard as specified in the request set when invoking the
   * constructor.
   *
   * @return A SmartCard object on success, null otherwise.
   */
  public synchronized SmartCard waitForCard() throws CardTerminalException {
    long timeout = cardRequest_.getTimeout() * 1000;

    // If the wait mode is ANYCARD, we also must check cards which are
    // already present in a slot. This will cause the cardInserted method
    // to be called for each present card.
    if (cardRequest_.getWaitBehavior() == CardRequest.ANYCARD)
      EventGenerator.getGenerator().createEventsForPresentCards(this);

    try {
      EventGenerator.getGenerator().addCTListener(this);

      // Wait until the timeout expires or a card matching the request is inserted.
      while ((!cardRequest_.isTimeoutSet() || timeout > 0) && smartCard_ == null) {
        // Wait for insertion of a matching card. cardInserted() will notify.
        long waitStartTime = System.currentTimeMillis();
        try {
          if (cardRequest_.isTimeoutSet()) {
            wait(timeout);
          } else {
            wait();
          }
        } catch (InterruptedException e) {
          // ignore, continue waiting
        }

        // Compute how long we still have to wait.
        timeout -= (System.currentTimeMillis() - waitStartTime);

        // If a card terminal exception occurred in cardInserted(),
        // we re-throw it here.
        if (cardTerminalException_ != null) {
          throw cardTerminalException_;
        }
      }
      ctracer.debug("waitForCard", "SmartCard obtained: " + smartCard_);
      return smartCard_;
    } finally {
      // Remove "this" as listener from the card terminal registry under any
      // circumstances. Otherwise a memory leak would result from a growing
      // list of card terminal event listeners.
      EventGenerator.getGenerator().removeCTListener(this);
    }
  }
}
