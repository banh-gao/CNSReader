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

//import de.cardcontact.opencard.terminal.smartcardio.SmartCardIOTerminalVerifyPINDirect;
import opencard.core.terminal.CHVControl;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ExtendedVerifiedAPDUInterface;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.SlotChannel;
import opencard.core.terminal.VerifiedAPDUInterface;

import opencard.core.util.Tracer;


/**
 * A communication channel to a smartcard.
 * A channel is used to exchange APDUs with an associated smartcard.
 * Additionally, it can be used to access some other resources related
 * to the associated card, like the terminal into which it is inserted.
 * <br>
 * <tt>CardChannel</tt> is the card service layer pendant to the terminal
 * layer's <tt>SlotChannel</tt>. The slot channel is a physical channel,
 * onto which several logical card channels can be multiplexed if the
 * smartcard supports logical channels.
 *
 * @author  Dirk Husemann (hud@zurich.ibm.com)
 * @author  Reto Hermann  (rhe@zurich.ibm.com)
 * @author  Mike Wendler  (mwendler@de.ibm.com)
 * @author  Roland Weber  (rolweber@de.ibm.com)
 * @version $Id: CardChannel.java,v 1.2 1999/10/22 16:07:33 damke Exp $
 *
 * @see opencard.core.terminal.SlotChannel
 * @see opencard.core.service.CardServiceScheduler
 */

public class CardChannel
{
  private Tracer itracer        = new Tracer(this, CardChannel.class);
  private static Tracer ctracer = new Tracer(CardChannel.class);


  /** The physical channel to use.*/
  private SlotChannel slot_channel = null;

  /** Whether this channel is currently opened. */
  private boolean is_open = false;

  /** Whether this channel is closed for good. */
  private boolean is_jammed = false;

  /** An attribute for service cooperation. */
  private Object  channel_state = null;


  // construction /////////////////////////////////////////////////////////////

  /**
   * Instantiate a new logical card channel.
   * The new channel has the given underlying physical slot
   * channel which will be used to contact the smartcard.
   *
   * @param    slotchannel
   *           the physical channel to the smartcard
   */
  protected CardChannel(SlotChannel slotchannel)
  {
    slot_channel   = slotchannel;

    is_open   = false;  // must be opened
    is_jammed = false;  // can be opened

    ctracer.debug("<init>", "(" + slotchannel + ")" );
  }


  // access ///////////////////////////////////////////////////////////////////


  /**
   * Checks whether this channel is currently open.
   * A channel can be used for communication with the associated smartcard
   * only while it's open.
   *
   * @return <tt>true</tt> if this channel is open, <tt>false</tt> otherwise
   */
  final public boolean isOpen()
  {
    return is_open;
  }


  /**
   * Makes sure this channel is currently open.
   * If this is the case, nothing is done. If not, an exception is thrown.
   *
   * @exception InvalidCardChannelException  iff this channel is not open
   */
  private void assertCardChannelOpen()
       throws InvalidCardChannelException
  {
    if (!is_open)
      throw new InvalidCardChannelException("CardChannel not open");
  }


  /**
   * Returns the card terminal associated with this channel.
   * The card channel is a logical channel, which is mapped onto a
   * physical slot channel. The slot channel is associated with the
   * slot into which the smartcard is inserted. The slot itself is
   * associated with a card terminal. That card terminal is queried
   * using this method.
   *
   * @return    The terminal that owns the underlying slot channel.
   *
   * @see opencard.core.terminal.SlotChannel
   * @see opencard.core.terminal.Slot
   */
  public CardTerminal getCardTerminal() {
    assertCardChannelOpen();

    return slot_channel.getCardTerminal();
  }



  /**
   * Return the slot channel associated with this channel.
   * @return the slot channel
   */
  public SlotChannel getSlotChannel() {
	assertCardChannelOpen();
	
	return slot_channel;
  }
  
  
  
  /**
   * Stores a service specific object associated with this channel.
   * The object stored can be retrieved by <tt>getState</tt>. By
   * convention, this method is invoked only by a card service that
   * has currently allocated this channel. The state object can, for
   * example, be used to store the current selection. It allows card
   * services to cooperate without knowing about each other, as long
   * as they use the same conventions for the objects stored here.
   * The state is associated with a channel rather than a smartcard,
   * since a smartcard may support several logical channels with
   * partially independent states.
   *
   * @param state       the object to associate with this channel,
   *                    or <tt>null</tt> to reset previous associations
   *
   * @see #getState
   */
  public final void setState(Object state)
  {
    channel_state = state;
  }


  /**
   * Retrieves the service specific object associated with this channel.
   * This method returns the argument to the last invocation of
   * <tt>setState</tt>. By convention, it is invoked only by a card
   * service that has currently allocated this channel. The object
   * retrieved has been stored by a service that used this channel
   * before.
   *
   * @return    the object currently associated with this channel,
   *            or <tt>null</tt> if there is no association
   *
   * @see #setState
   */
  public final Object getState()
  {
    return channel_state;
  }


  // service //////////////////////////////////////////////////////////////////


  /**
   * Sends a <tt>CommandAPDU</tt> to the smart card.
   * Receives and returns the smartcard's response.
   *
   * @param     cmdAPDU
   *            the <tt>CommandAPDU</tt> to send
   * @return    the response from the smartcard
   * @exception InvalidCardChannelException
   *            This channel is currently not open.
   */
  public ResponseAPDU sendCommandAPDU(CommandAPDU cmdAPDU)
       throws InvalidCardChannelException, CardTerminalException
  {
    assertCardChannelOpen();

    itracer.debug("sendCommandAPDU", cmdAPDU.toString());
    ResponseAPDU rapdu = slot_channel.sendAPDU(cmdAPDU);
    itracer.debug("response: ", rapdu.toString());

    return rapdu;
  }
  /** @deprecated */
  final public ResponseAPDU sendVerifiedAPDU(CommandAPDU  command,
                                             CHVControl   control,
                                             CHVDialog    dialog,
                                             int          timeout)
       throws InvalidCardChannelException, CardTerminalException,
              CardServiceInvalidCredentialException
  {
    return sendVerifiedAPDU(command, control, dialog);
  }

  /**
   * Sends a command including a PIN to the smart card within a given time.
   * The APDU to send will be modified on it's way to the card by storing a
   * PIN or password which has been entered by the card owner. This method
   * supports GUI password dialogs as well as a card terminal's encrypting
   * pinpad, if available.
   * The smartcard's response to the command is received and returned
   * if the operation completes within the specified timeout.
   * <br>
   * The decision whether the terminal or a GUI dialog is used for
   * querying the password is crucial to security. Currently, the GUI
   * is used only if the terminal does not implement the interface
   * <tt>VerifiedAPDUInterface</tt>. This behavior must not be changed
   * in derived classes, therefore this method is <tt>final</tt>.
   *
   * @param command     the <tt>CommandAPDU</tt> to send
   * @param control     the verification parameters to use
   * @param dialog      the dialog to use to query a password.
   *                    Ignored if the terminal takes the reponsibility
   *                    for querying the password or PIN. The default
   *                    dialog is used if this argument is <tt>null</tt>.
   *
   * @return   the response from the smartcard
   *
   * @exception InvalidCardChannelException
   *            This channel is currently not open.
   * @exception CardTerminalException
   *            The terminal encountered an error.
   * @exception CardServiceInvalidCredentialException
   *            The user cancelled password input.
   *
   * @see opencard.core.terminal.VerifiedAPDUInterface
   * @see opencard.core.service.CardHolderVerificationGUI
   */
  final public ResponseAPDU sendVerifiedAPDU(CommandAPDU  command,
                                             CHVControl   control,
                                             CHVDialog    dialog)
       throws InvalidCardChannelException, CardTerminalException,
              CardServiceInvalidCredentialException
  {
	  assertCardChannelOpen();

	  itracer.debug("sendVerifiedAPDU", "(" + command + ")");

	  ResponseAPDU response = null;
	  /*
	   * If the terminal provides IO facilities, send via the terminal.
	   * Otherwise, use a GUI dialog for password query.
	   */

	  CardTerminal ct = getCardTerminal();
	  
	  boolean hasSendVerifiedCommandAPDU = ct instanceof VerifiedAPDUInterface;
	  
	  if (ct instanceof ExtendedVerifiedAPDUInterface) {
		  ExtendedVerifiedAPDUInterface terminal = (ExtendedVerifiedAPDUInterface) ct;
		  hasSendVerifiedCommandAPDU = terminal.hasSendVerifiedCommandAPDU();
	  }
	  
	  if(hasSendVerifiedCommandAPDU) {
		  
		  VerifiedAPDUInterface terminal = (VerifiedAPDUInterface) ct;
		  response = terminal.sendVerifiedCommandAPDU(slot_channel, command, control);
	  
	  } else {
		  
		  CardHolderVerificationGUI gui = new CardHolderVerificationGUI();
		  response = gui.sendVerifiedAPDU(slot_channel, command, control, dialog);
	  }

	  itracer.debug("sendVerifiedAPDU", "response: " + response);

	  return response;
  }


  /**
   * Opens this <tt>CardChannel</tt>.
   * This method cannot be invoked after the channel has been
   * closed for good by an invocation of <tt>closeFinal()</tt>.
   * The default visibility is (would be) sufficient since it gets called
   * by the scheduler, which resides in the same package.
   *
   * @exception InvalidCardChannelException
   *            This channel is already open, or closed for good.
   */
  public void open()
       throws InvalidCardChannelException
  {
    itracer.debug("open", "opening CardChannel");
    if (is_jammed)
      throw new InvalidCardChannelException("cannot be opened anymore");
    if (is_open)
      throw new InvalidCardChannelException("already open");

    is_open = true;
  }


  /**
   * Closes this <tt>CardChannel</tt>.
   * It can still be opened again. While the channel is not open,
   * invocations of the send methods will throw an exception.
   */
  public void close()
  {
    is_open = false;
    itracer.debug("close", "CardChannel closed");
  }


  /**
   * Closes this <tt>CardChannel</tt> so it cannot be opened anymore.
   */
  protected void closeFinal()
  {
    itracer.debug("closeFinal", "closing for good");

    close();

    is_jammed = true;
  }


  /** Tries to clean up. */
  public void finalize()
  {
    closeFinal();
  }


  /**
   * Returns a string representation of this card channel.
   *
   * @return  a human-readable representation of this channel
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer(super.toString());
    sb.append(", ").append(is_open  ?"is":"not").append(" open");
    sb.append(", ").append(is_jammed?"is":"not").append(" jammed");
    return sb.toString();
  }

} // class CardChannel
