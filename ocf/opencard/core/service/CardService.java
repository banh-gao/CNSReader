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


import opencard.core.terminal.CardTerminalException;
import opencard.core.util.Tracer;

/**
 * Provides specific smart card functionality to applications.
 * This functionality may be, for example, an ISO 7816-4 file system or
 * a ISO 7816-7 data base system. A concrete card service is almost
 * always smart card operating system specific.
 *
 * <p>
 * Communication with a smart card takes place through a <tt>CardChannel</tt>
 * that the card service either allocates from a <tt>CardServiceScheduler</tt>
 * or gets from a third party, for example another card service or the
 * corresponding <tt>SmartCard</tt> object if <tt>beginMutex</tt> is
 * invoked there.
 * <br>
 * The methods to allocate and release card channels provided here are
 * aware of channels that have been pre-set by a third party. A public
 * method providing card functionality in a class derived from
 * <tt>CardService</tt> will typically have the following structure:
 * <p>
 * <blockquote><pre>
 * public RetType doSomeThingWithCard(...)
 *     throws CardServiceException, CardTerminalException
 * {
 *   ... // check parameters
 *   RetType retvalue = null;
 *   try {
 *     allocateCardChannel(); // ensure there is a channel
 *     CommandAPDU  command  = ...;
 *     ResponseAPDU response =
 *              getCardChannel().sendCommandAPDU(command);
 *     ... // evaluate response, maybe send further commands
 *     retvalue = ...;
 *   } finally {             // despite any exceptions
 *     releaseCardChannel(); // free the card channel
 *   }
 *   return retvalue;
 * }
 * </blockquote></pre>
 *
 *
 * @author  Dirk Husemann  (hud@zurich.ibm.com)
 * @author  Reto Hermann   (rhe@zurich.ibm.com)
 * @author  Thomas Schaeck (schaeck@de.ibm.com)
 * @author  Roland Weber   (rolweber@de.ibm.com)
 * @version $Id: CardService.java,v 1.1.1.1 1999/10/05 15:34:31 damke Exp $
 *
 * @see opencard.core.service.CardChannel
 * @see opencard.core.service.CardServiceScheduler
 * @see opencard.core.service.SmartCard#beginMutex
 */
public abstract class CardService
{
  private Tracer itracer = new Tracer(this, CardService.class);
  private static Tracer ctracer = new Tracer(CardService.class);


  /** The controlling smartcard object. */
  private SmartCard smart_card = null;

  /** The scheduler at which to allocate card channels. */
  private CardServiceScheduler cs_scheduler = null;

  /** The currently used card channel. */
  private CardChannel card_channel = null;

  /** The type of card. */
  private CardType type = null;

  /**
   * Whether the current channel is provided or allocated.
   * This attribute holds <tt>true</tt> if a channel was provided
   * by <tt>setCardChannel</tt>.
   *
   * @see #setCardChannel
   */
  private boolean is_provided = false;

  /** The mode of operation. Relevant when allocating channels. */
  private boolean is_blocking = false;

  /** The dialog to use to obtain CHVs */
  private CHVDialog chv_dialog = null;



  // construction /////////////////////////////////////////////////////////////


  /**
   * Creates a new card service, which is not yet initialized.
   * Before this new service can be used, <tt>initialize</tt> has to be
   * invoked. This two phase creation scheme has been developed to avoid
   * using <tt>java.lang.reflect</tt> to instantiate card services.
   *
   * @see #initialize
   */
  protected CardService()
  {
    ctracer.debug("<init>", "default constructor of " + this);
  }


  // access ///////////////////////////////////////////////////////////////////


  /**
   * Sets the channel to use for communicating with the smartcard.
   * Setting the channel with this method avoids allocating a channel
   * before each operation, and releasing it afterwards. This can be
   * used to issue a series of commands to the smartcard that has to
   * or should be processed without intervening commands, for example
   * to avoid additional SELECT operations or multiple password queries
   * that would have to be performed by the service otherwise.
   * <br>
   * This method is typically invoked from <tt>SmartCard.beginMutex</tt>.
   * All services used by an application will be provided with the same
   * channel, and no other application will have access to the smartcard
   * (unless it supports multiple logical channels). A second use for
   * this method are services that are built on top of other services
   * and provide their own channel, the so-called <i>meta services</i>.
   * <br>
   * Pre-setting a channel does not mean that <tt>allocateCardChannel</tt>
   * and <tt>realeaseCardChannel</tt> cannot be invoked. Their implementation
   * in this class just avoids the invocations of the scheduler if a channel
   * is already available. They still check whether the scheduler is alive,
   * or whether it has died since the smartcard has been removed.
   * Derived services may add more functionality there, making the invocation
   * of those methods necessary even if a channel has been pre-set.
   * <br>
   * This method has intentionally not been declared <tt>final</tt>.
   * A service that uses customized channels may want to prepare the
   * channel for future use here.
   *
   * @param channel   the channel to use, or <tt>null</tt> to reset
   *
   */
  public void setCardChannel(CardChannel channel)
  {
    card_channel = channel;
    is_provided = (channel != null);
  }

  /**
   * Gets the card channel to use for communicating with the smartcard.
   * The channel returned has either been allocated using
   * <tt>allocateCardChannel</tt>, or was provided by a third party
   * via <tt>setCardChannel</tt>. If neither is true, this method
   * will return <tt>null</tt>.
   * <br>
   * If a service uses customized channels that provide additional
   * methods, it is suggested to implement a method <tt>getMyChannel</tt>
   * with a more special return type than here. That method could
   * call this one and down-cast the channel returned.
   *
   * @return  the channel for communication with the smartcard
   */
  final public CardChannel getCardChannel()
  {
    return card_channel;
  }


  /**
   * Sets the CHV dialog to be used for getting passwords from the user.
   * This method is intentionally not declared <tt>final</tt>, since a
   * card service that uses helper objects may want to pass the dialog
   * to one of them.
   *
   * @param dialog the CHV dialog to be used
   */
  public void setCHVDialog(CHVDialog dialog)
  {
    chv_dialog = dialog;
  }


  /**
   * Returns the dialog for CHV input.
   * The dialog has to be set using <tt>setCHVDialog</tt>.
   *
   * @return  the dialog to use to query a password from the user
   *
   * @see #setCHVDialog
   */
  final public CHVDialog getCHVDialog()
  {
    return chv_dialog;
  }


  /**
   * Gets the smartcard object associated with this service.
   * Services are requested at a particular instance of <tt>SmartCard</tt>
   * which can be used to identify the smartcard and also represents the
   * application that requested the service. This method returns the
   * smartcard object that was used to request this service.
   *
   * @return  the smartcard object for this service
   */
  final public SmartCard getCard()
  {
    return smart_card;
  }



  // service //////////////////////////////////////////////////////////////////


  /**
   * Initializes this service.
   * This method is an extension to the constructor. It is invoked by the
   * <tt>CardServiceFactory</tt> after creating a service using the default
   * constructor. The service cannot be used until this method has been
   * invoked and returned without throwing an exception.
   * <br>
   * Derived services may override this method to perform extended
   * initialization. However, the implementation in this class <b>has
   * to be invoked</b> anyway. The preferred way to do this is by
   * invoking <tt>super.initialize</tt> at the beginning of the redefined
   * method. This mimics the construction mechanism, where the invocation
   * of the base class constructors has to be the first statement in the
   * constructors of derived classes.
   * <br>
   * This method has visibility <tt>protected</tt> since it is meant to
   * be invoked only from class <tt>CardServiceFactory</tt>. If services
   * should be instantiatable from somewhere else, they may redefine it
   * with <tt>public</tt> visibility, or whatever is appropriate.
   *
   * @param scheduler   where this service is going to allocate channels
   * @param smartcard   which smartcard has to be supported by this service
   * @param blocking    whether channel allocation is going to be blocking
   *
   * @exception CardServiceException
   *    if the service could not be initialized. The object created via the
   *    default constructor may not be used if this happens.
   *
   * @see CardServiceFactory
   */
  protected void initialize(CardServiceScheduler scheduler,
                            SmartCard            smartcard,
                            boolean              blocking)
       throws CardServiceException
  {
    itracer.debug("initialize", "(" + scheduler + "," + smartcard + ")");

    smart_card   = smartcard;
    cs_scheduler = scheduler;
    is_blocking  = blocking;
//    this.type = type;
  }


  /**
   * Allocates a card channel iff one is required.
   * If a channel has been provided by <tt>setCardChannel</tt>, none
   * has to be allocated. <tt>releaseCardChannel</tt> will take care
   * of this and release the channel only if it actually has been
   * allocated here. After calling this method, a card channel will
   * be available and can be obtained via <tt>getCardChannel</tt>.
   *
   * @exception InvalidCardChannelException
   *            The controlling <tt>CardServiceScheduler</tt> has quit.
   *            The service is non-blocking and the channel is in use.
   *
   * @see #setCardChannel
   * @see #getCardChannel
   * @see #releaseCardChannel
   * @see CardChannel
   * @see CardServiceScheduler
   */
  protected void allocateCardChannel()
       throws InvalidCardChannelException
  {
    assertSchedulerStillAlive();

    if (!is_provided)
      {
        itracer.debug("allocateCardChannel", "allocating");
        try {
          card_channel = cs_scheduler.allocateCardChannel(this, is_blocking);
          if (card_channel==null) {
            throw new InvalidCardChannelException("channel in use");
          }
        } catch (CardTerminalException ctx) {
          throw new InvalidCardChannelException(ctx.toString());
        }
      }
  }


  /**
   * Releases the allocated card channel.
   * If the channel has not been allocated by <tt>allocateCardChannel</tt>
   * but was provided via <tt>setCardChannel</tt>, it will not be released.
   *
   * @exception InvalidCardChannelException
   *            The controlling <tt>CardServiceScheduler</tt> has quit.
   *
   * @see #allocateCardChannel
   * @see #setCardChannel
   * @see CardChannel
   * @see CardServiceScheduler
   */
  protected void releaseCardChannel()
       throws InvalidCardChannelException
  {
    assertSchedulerStillAlive();

    if (!is_provided)
      {
        itracer.debug("releaseCardChannel", "releasing");
        cs_scheduler.releaseCardChannel(card_channel);
        card_channel = null;
      }
  }


  /**
   * Checks whether the scheduler is still alive.
   * The scheduler will die if the smartcard is removed.
   * In this case, some cleanup is performed and an exception
   * will be thrown.
   *
   * @exception InvalidCardChannelException
   *    the scheduler is no longer alive
   */
  private void assertSchedulerStillAlive()
       throws InvalidCardChannelException
  {
    boolean alive = false;
    try {
      alive = cs_scheduler.isAlive();
    } catch (CardTerminalException ctx) {
      throw new InvalidCardChannelException(ctx.toString());
    }
    if (!alive)
      {
        card_channel = null;
        cs_scheduler = null;
        throw new InvalidCardChannelException("card removed?");
      }
  }

} // class CardService
