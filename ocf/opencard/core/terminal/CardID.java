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

package opencard.core.terminal;


import opencard.core.util.HexString;

/**
 * Represents a smartcard's ATR (Answer To Reset). In addition to the ATR
 * itself, the <tt>Slot</tt> where the card is inserted can be stored.
 *
 * The ATR is used for identifying cards. It usually contains some so-called
 * <i>historical characters</i> which can be used to determine the type of
 * card. Since the historical characters can be defined by the card issuer,
 * they may also give a hint on the applications supported by the smartcard.
 *
 * @author  Dirk Husemann       (hud@zurich.ibm.com)
 * @author  Mike Wendler        (mwendler@de.ibm.com)
 * @author  Stephan Breideneich (sbreiden@de.ibm.com)
 * @author  Thomas Schaeck      (schaeck@de.ibm.com)
 * @author  Roland Weber        (rolweber@de.ibm.com)
 *
 * @version $Id: CardID.java,v 1.3 1999/10/22 16:07:34 damke Exp $
 */
public class CardID
{
  /** The represented ATR. */
  protected byte[] atr;

  /**
   * The <i>historical characters</i> of the ATR.
   * There can be at most 15 historical characters.
   * If there are no historical characters, this attribute
   * holds <tt>null</tt>, not an empty array.
   */
  protected byte[] historicals;

  /**
   * The slot which holds the card with this ATR.
   */
  protected int slotNr = 0;
  protected CardTerminal terminal = null;

  /**
   * The cached result of <tt>toString</tt>.
   * @see #toString
   */
  protected String cachedResult = null;


  // construction /////////////////////////////////////////////////////////////

  /**
   * Instantiates a new card ID representing the given ATR.
   *
   * @param     answerToResetResponse
   *            a byte array holding the ATR to represent
   * @exception CardTerminalException
   *            if the ATR is invalid
   */
  public CardID (byte[] answerToResetResponse)
    throws CardTerminalException {

    atr = (byte[]) answerToResetResponse.clone();
    // ... assert minimum length: TS + T0 character must be present
    if (atr.length < 2)
      throw new CardTerminalException
        ("Illegal ATR response (length " + atr.length +
         " < 2): " + HexString.hexify(answerToResetResponse));

    if ((atr[0] == 0x3B) || (atr[0] == 0x3F)) {
        if ((atr[1] & 0x0f) > 0) {
            historicals = new byte[atr[1] & 0x0f];

            // count interface characters, calculate offset of historicals
            int offset=1; // position of current TDi, start with T0
            boolean tdiPresent = true;
            while (tdiPresent) {
                if ((atr[offset] & 0x80)!=0) tdiPresent = true; else tdiPresent = false;
                offset+= ((atr[offset] & 0x80) >>> 7) +
                ((atr[offset] & 0x40) >>> 6) +
                ((atr[offset] & 0x20) >>> 5) +
                ((atr[offset] & 0x10) >>> 4);
            }
            offset++;

            // retrieve the historical bytes
            System.arraycopy(atr, offset,historicals, 0, historicals.length);
            int histCtr = historicals.length-1;
        }
    }
  }

  /**
   * @deprecated use CardID(CardTerminal, int, byte[])
   */
  public CardID (Slot slot, byte[] answerToResetResponse)
    throws CardTerminalException {

    this (answerToResetResponse);
    this.slotNr = slot.getSlotID();
    this.terminal = slot.getCardTerminal();
  }


  /**
   * Instantiates a new card ID representing the given ATR from the given slot.
   * @param     terminal
   *            the terminal where the card with this ATR is inserted
   * @param     slotID
   *            the slot where the card with this ATR is inserted.
   * @param     answerToResetResponse
   *            a byte array holding the ATR
   * @exception CardTerminalException
   *            if the ATR is invalid
   */
  public CardID (CardTerminal terminal, int slotID, byte[] answerToResetResponse)
    throws CardTerminalException {

    this (answerToResetResponse);
    this.slotNr = slotID;
    this.terminal=terminal;
  }

  // access ///////////////////////////////////////////////////////////////////


  /**
   * Gets the represented ATR.
   * The returned byte array holds a copy of the ATR.
   * It can safely be modified.
   *
   * @return   a byte array holding the ATR
   */
  public byte[] getATR() {

    return (atr != null) ? (byte[])atr.clone() : null;
  }


  /**
   * Gets the historical characters.
   * Despite their name, the historical characters are byte values.
   * They do not have to be printable characters. Therefore, they
   * are returned as a byte array, not as a string. The byte array
   * is newly allocated and can safely be modified.
   *
   * @return    a byte array containing the historical characters,
   *            or <tt>null</tt> if the ATR does not include any
   */
  public byte[] getHistoricals() {

    // must use clone() here to prevent applications from modifying
    // internal data!
    return ((historicals != null) ? (byte[])historicals.clone() : null) ;
  }


  /**
   * Gets the instantiating <tt>Slot</tt>.
   *
   * @return    the <tt>Slot</tt> where the card with this ATR is inserted,
   *            or <tt>null</tt> if unknown
   * @deprecated use getSlotID(), getCardTerminal() instead
   */
  public Slot getSlot() {
    return new Slot(terminal, slotNr);
  }


  /**
   * Gets the instantiating <tt>slot id</tt>.
   *
   * @return the SlotID or 0 if unknown
   * @deprecated use getSlotID() instead
   */
  public int getSlotID() {
    return slotNr;
  }


  /**
   * Gets the instantiating <tt>terminal</tt>.
   *
   * @return the terminal or null if unknown
   * @deprecated use getSlotID() instead
   */
  public CardTerminal getCardTerminal() {
    return terminal;
  }





  // system ///////////////////////////////////////////////////////////////////


  /**
   * Compares this with another <tt>CardID</tt> object.
   * They are equal if the represented ATRs are equal.
   *
   * @param obj  the <tt>CardID</tt> to compare with
   * @return     <tt>true</tt> if the object represents the same ATR,
   *             <tt>false</tt> if it represents a different ATR or
   *             no ATR at all
   */
  public boolean equals (Object obj) {

    if (!(obj instanceof CardID))
      return false;

    CardID target = (CardID) obj;

    // ... cannot match if atr.lengths are different
    if (target.atr.length != atr.length)
      return false;

    // ... OK, now for the tedious part
    for (int i = 0; i < atr.length; i++)
      if (atr[i] != target.atr[i])
        return false;

    return true;
  }


  /**
   * Returns a String representation of this <tt>CardID</tt> object.
   *
   * @return   A String representation of this <tt>CardID</tt>
   *           object.
   */
  public String toString() {

    if (cachedResult == null) {
     cachedResult = super.toString() + " ATR: " + HexString.hexify(atr);
    }
    return cachedResult;
  }

} // class CardID
