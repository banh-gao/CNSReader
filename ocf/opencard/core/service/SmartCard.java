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


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import opencard.core.OpenCardConstants;
import opencard.core.OpenCardRuntimeException;
import opencard.core.event.CardTerminalEvent;
import opencard.core.event.EventGenerator;
import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminal;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalFactory;
import opencard.core.terminal.CardTerminalRegistry;
import opencard.core.util.APDUTracer;
import opencard.core.util.OpenCardConfigurationProvider;
import opencard.core.util.OpenCardPropertyLoadingException;
import opencard.core.util.SystemAccess;
import opencard.core.util.Tracer;


/**
 * The <tt>SmartCard</tt> object is the point of access to the OpenCard
 * framework for the application. <tt>CardServices</tt> are accessible
 * through the services of the <tt>SmartCard</tt> object.
 * <P>A <tt>SmartCard</tt> object is always tied to the controlling
 * <tt>CardServiceScheduler</tt>.
 *
 * @author  Dirk Husemann       (hud@zurich.ibm.com)
 * @author  Mike Wendler        (mwendler@de.ibm.com)
 * @author  Stephan Breideneich (sbreiden@de.ibm.com)
 * @author  Thomas Schaeck      (schaeck@de.ibm.com)
 *
 * @version $Id: SmartCard.java,v 1.2 2005/09/19 10:17:31 asc Exp $
 *
 * @see opencard.core.service.CardService
 */
public final class SmartCard {
  /** the version of OCF */
  private static final String VERSION="OCF1.2;IBM Reference Implementation with OpenSCDP extensions ";

  private Tracer itracer        = new Tracer (this, SmartCard.class);
  private static Tracer ctracer = new Tracer (SmartCard.class);

  /** References to the controlling <TT>CardServiceScheduler</TT>,
   * <TT>CardID</TT> and <TT>CardChannel</TT>. */
  private CardServiceScheduler scheduler          = null;
  private CardID               cid                = null;
  private CardChannel          mutexCardChannel   = null;
  private APDUTracer           aPDUTracer         = null;
  
  private static boolean OCFisStarted    = false;

  /** Vector for keeping track of allocated <TT>CardServices</TT>. */
  private Vector allocatedCS = new Vector();

  private static Vector smartCardCache = new Vector();

  /** Reference counter for keeping track of the users that called start() */
  private static int refCount_ = 0;

  /** return version information about OCF
  * It returns a string of the form
  * <p>OCF1.2;IBM Reference Implementation, Build Hudson, 21-May-1999</p>
  * <p>
  * The first part indicates the API. It always starts with "OCF", followed
  * by an API version number, and terminate by a semicolon. Currently, this
  * the API version number is simply the OCF release number. When a formal
  * OCF specification becomes available, it would refer to the version of
  * that specification it implements. After the semicolon, vendor specific
  * information is included. The format of the vendor specific information
  * is not specified.
  */
  public static String getVersion() {
    Package p = SmartCard.class.getPackage();
    if ((p.getSpecificationVersion() == null) || (p.getImplementationVersion() == null)) {
      return VERSION + "(Unknown Version)";
    }
    return VERSION + p.getSpecificationVersion() + "." + p.getImplementationVersion();
  }

  /**
   * Instantiates a <tt>SmartCard</tt> object that is tied to
   * <tt>scheduler</tt>.
   *
   * @param scheduler The controlling <tt>CardServiceScheduler</tt>.
   * @param cid       The <tt>CardID</tt> object representing the physical
   *                  smart card.
   */
  public SmartCard(CardServiceScheduler scheduler, CardID cid) {
    ctracer.debug("<init>", "scheduler " + scheduler + ", cid " + cid);

    this.scheduler = scheduler;
    this.cid = cid;
  }
  /**
   * (Helper) Make sure that this <tt>SmartCard</tt> object is still open.
   */
  private void assertSmartCardOpen() {
    // As SmartCard is no CTListener, we need to check liveness of the
    // scheduler to assert that the smartcard has not been removed.
    try {
      if (! scheduler.isAlive())
        throw new OpenCardRuntimeException("SmartCard closed");
    } catch (CardTerminalException e) {
      throw new OpenCardRuntimeException("SmartCard closed");
    }
  }
  /** Gain exclusive access to the card. <b>Be sure to call <tt>endMutex()</tt>
   * eventually!</b>.
   *
   * @exception java.lang.InterruptedException
   * 	   Thrown when no exclusive access to the card could be gained.
   * @exception CardTerminalException
   *            Thrown when the terminal encountered an error.
   *
   * @see opencard.core.service.CardService#setCardChannel
   * @see opencard.core.service.CardServiceScheduler#allocateCardChannel
   */
  public void beginMutex()
  throws InterruptedException, CardTerminalException
  {
    synchronized(scheduler) {
      ctracer.debug("beginMutex", "entry "+this);
      assertSmartCardOpen();
      mutexCardChannel = scheduler.allocateCardChannel(this, true);
      if (mutexCardChannel == null)
        throw new InterruptedException("beginMutex");

      // ... cruise through all CS and preset their channel to mutexCardChannel
      Enumeration css = allocatedCS.elements();
      while (css.hasMoreElements()) {
        CardService cs = (CardService)css.nextElement();
        cs.setCardChannel(mutexCardChannel);
      }

      ctracer.debug("beginMutex", "exit "+this);
    }
  }
/**
 * Closes this <tt>SmartCard</tt> object and signals to OCF that
 * the allocated resources are not any longer required.
 *
 * @exception CardTerminalException
 *            Thrown when an error is encountered during cleanup.
 */
  public synchronized void close() throws CardTerminalException {
    // ... do nothing if we are closed already
    if (scheduler != null) {
      itracer.debug("close", "SmartCard closing");
      // ... make sure we end mutex operation; need to do this before
      //     we set the state to SMARTCARD_CLOSED
      if (mutexCardChannel != null)
        endMutex();
      scheduler.releaseSmartCard(this);
      // scheduler = null;
    }
    smartCardCache.removeElement(this);
  }
  /**
   * Configures the <TT>CardServiceRegistry</TT> with the <TT>CardServiceFactory</TT>
   * classes stated in the property "OpenCard.services".
   *
   * @exception CardServiceException
   *    if at least one of the service factories in the properties file
   *    could not be instantiated. OpenCard may still be able to run with
   *    those that could be instantiated.
   */
  private static void configureServiceRegistry()
  throws CardServiceException
  {
    CardServiceRegistry serviceRegistry = CardServiceRegistry.getRegistry();

    StringTokenizer recordTokenizer =
    getRegistryEntry(OpenCardConstants.CARD_SERVICE_REGISTRY_TAG);

    if (recordTokenizer != null) {
      StringBuffer message = new StringBuffer(); // for error handling

      // ... parse the record into elements
      while (recordTokenizer.hasMoreElements()) {
        String factoryName = (String) recordTokenizer.nextElement();

        try {
          Class factoryClass = Class.forName(factoryName);
          serviceRegistry.add((CardServiceFactory) factoryClass.newInstance());
        } catch (ClassNotFoundException cnfe) {
          ctracer.error("<configureServiceRegistry>", cnfe.getMessage());
          message.append("\nClass \"").append(factoryName).append("\" not found");
        } catch (InstantiationException ie) {
          ctracer.error ("<configureServiceRegistry>", ie.getMessage());
          message.append("\nClass \"").append(factoryName).append("\" not instantiatable");
        } catch (IllegalAccessException iae) {
          ctracer.error ("<configureServiceRegistry>", iae.getMessage());
          message.append("\nClass \"").append(factoryName).append("\" constructor not accessible");
        }
      }

      if (message.length() > 0)
        throw new CardServiceException(message.toString());
    } else {
      ctracer.debug("configureServiceRegistry", "no services entry in properties");
    }
  } // configureServiceRegistry
  /**
   * Configures the <TT>CardTerminalRegistry</TT>.
   * The configuration data is taken from the property
   * <i>OpenCard.terminals</i>.
   *
   * @exception ClassNotFoundException
   *            if a terminal factory class could not be found
   * @exception CardTerminalException
   *            if a terminal factory could not instantiate it's terminals
   */
  private static void configureTerminalRegistry()
  throws ClassNotFoundException,
  CardTerminalException
  {
    CardTerminalRegistry d_terminalRegistry = CardTerminalRegistry.getRegistry();

    StringTokenizer recordTokenizer =
    getRegistryEntry (OpenCardConstants.CARD_TERMINAL_REGISTRY_TAG);
    Hashtable factories = new Hashtable ();

    if (recordTokenizer != null) {
      // ... parse the record into elements
      while (recordTokenizer.hasMoreElements () ) {
        String record = (String) recordTokenizer.nextElement();
        handleTerminalFactoryEntries (record, factories, d_terminalRegistry);
      }
    } else {
      ctracer.debug("configureTerminalRegistry", "no terminals in properties");
    }
  } // configureTerminalRegistry
  /**
   * Releases exclusive access to the card.
   */
  public synchronized void endMutex() {
    synchronized (scheduler) {
      ctracer.debug("endMutex", "entry "+this);
      assertSmartCardOpen();
      scheduler.releaseCardChannel(mutexCardChannel);
      mutexCardChannel = null;

      // ... cruise through all CS and preset their channel to null
      Enumeration css = allocatedCS.elements();
      while (css.hasMoreElements()) {
        CardService cs = (CardService)css.nextElement();
        cs.setCardChannel(null);
      }
      ctracer.debug("endMutex", "exit "+this);
    }

  }
  /**
   * Finalizer: close this <tt>SmartCard</tt> in case it is still open.
   */
  protected void finalize() {
    try {
      close();
    } catch (CardTerminalException ctx) {
      itracer.error("finalize", ctx.toString());
    }
  }
  /**
   * Gets the <tt>CardID</tt> object representing this smart card.
   *
   * @return    The <tt>CardID</tt> object.
   */
  public CardID getCardID() {
    return cid;
  }
  // instance methods ---------------------------------------------------------


  /**
   * Tries to instantiate a <tt>CardService</tt> for the smart card that
   * implements class <tt>clazz</tt>.
   *
   * @param clazz The class that the <tt>CardService</tt> shall implement.
   * @param block If true, indicates that the <tt>CardService</tt> should run in
   *		     blocking mode.
   *
   * @exception java.lang.ClassNotFoundException
   *		   Thrown when no <tt>CardService</tt> implementing <tt>clazz</tt>
   *		   exists for this card.
   * @exception opencard.core.service.CardServiceException
   *            Thrown when a service was found but failed to initialize.
   */
  public CardService getCardService(Class clazz, boolean block)
  throws ClassNotFoundException, CardServiceException
  {
    CardService cs = null;

    itracer.debug("getCardService", "(" + clazz + ")");

    assertSmartCardOpen();
    cs = CardServiceRegistry.getRegistry().
         getCardServiceInstance(clazz, cid, scheduler, this, block);

    // ... check whether mutex is in force
    if (mutexCardChannel != null && mutexCardChannel.isOpen()) {
      cs.setCardChannel(mutexCardChannel);
    }
    allocatedCS.addElement(cs);
    ctracer.debug("getCardService", allocatedCS.size() + " elements in cache");

    return cs;
  }
  /**
   *  Returns a tokenizer for the given property entry.
   */
  private static StringTokenizer getRegistryEntry (String tag) {
    // ... get the property
    String registryProps = SystemAccess.getSystemAccess().getProperty (tag);
    ctracer.debug("getRegistryEntry", "tag " + tag + " = " + registryProps);

    return(registryProps != null)
    ? new StringTokenizer(registryProps)
    : null;
  }
  /**
   * @deprecated use getSmartCard(CardTerminalEvent, CardRequest)
   */
  public static SmartCard getSmartCard(CardTerminalEvent ctEvent)
  throws CardTerminalException {
    return getSmartCard(ctEvent, null, null);
  }
  /**
   * Gets a <tt>SmartCard</tt> object for a received <tt>CardTerminalEvent</tt> provided
   * that the <tt>CardRequest</tt> can be satisfied.
   *
   * @param ctEvent The received <tt>CardTerminalEvent</tt>.
   * @param req     A <tt>CardRequest</tt> object describing the kind of
   *		       smart card that we are interested in.
   *
   * @return    A <tt>SmartCard</tt> object.
   */
  public static SmartCard getSmartCard(CardTerminalEvent ctEvent, CardRequest req)
  throws CardTerminalException {
     return getSmartCard(ctEvent, req, null);
  }

  /**
   * Gets a <tt>SmartCard</tt> object for a received <tt>CardTerminalEvent</tt> provided
   * that the <tt>CardRequest</tt> can be satisfied.
   *
   * @param ctEvent The received <tt>CardTerminalEvent</tt>.
   * @param req     A <tt>CardRequest</tt> object describing the kind of
   *		       smart card that we are interested in.
   * @param lockHandle the handle obtained by the lock owner when locking a
   *                   slot or terminal.
   *
   * @return    A <tt>SmartCard</tt> object.
   */
  public static SmartCard getSmartCard(CardTerminalEvent ctEvent, CardRequest req, Object lockHandle)
  throws CardTerminalException {
    SmartCard newCard = CardServiceRegistry.getRegistry().getSmartCard(ctEvent, req, lockHandle);

    if (newCard != null) {
      smartCardCache.addElement(newCard);
      ctracer.debug("getSmartCard", smartCardCache.size() + " elements in cache");
    }

    return newCard;
  }
  private static void handleTerminalFactoryEntries(String record,
                                                   Hashtable factories, CardTerminalRegistry terminalRegistry)
  throws ClassNotFoundException, CardTerminalException {

    CardTerminalFactory aFactory = null;
    String [] params = null;

    StringTokenizer elementTokenizer = new StringTokenizer(record, "|");
    int elements = elementTokenizer.countTokens();

    if (elements > 0) {
      params = new String[elements - 1];

      int counter = 0;
      String factoryName = null;

      while (elementTokenizer.hasMoreElements()) {
        if (counter == 0)
          factoryName = (String) elementTokenizer.nextElement();
        else
          params[counter - 1] = (String) elementTokenizer.nextElement();

        counter++;
      }

      aFactory = (CardTerminalFactory) factories.get(factoryName);
      if (aFactory == null) {
        try {
          Class factoryClass = Class.forName(factoryName);
          aFactory = (CardTerminalFactory) factoryClass.newInstance();
          factories.put (factoryName, aFactory);
        } catch (InstantiationException ie) {
          throw new CardTerminalException(ie.toString());
        } catch (IllegalAccessException iae) {
          throw new CardTerminalException(iae.toString());
        }
      }
      aFactory.createCardTerminals(terminalRegistry, params);
    }
  } // handleTerminalFactoryEntries
  /**
   * Determines whether the startup process has already been carried out during this
   * session.
   *
   * NOTE however that this says nothing at all about success or failure of
   * this process!
   *
   * @return true if the setup process has been carried out during this session,
   *         false otherwise.
   */
  public static boolean isStarted () {
    return OCFisStarted;
  }
/**
 * Shuts down the entire OpenCard Framework. This is meant to be the last method to
 * be invoked in any application in order to close OpenCard properly.
 * This process will cause <TT>CardTerminalRegistry</TT> and
 * <TT>CardServiceRegistry</TT> to cleanup themselves and leave OpenCard in a clean state.
 */
  public static void shutdown() throws CardTerminalException {
    ctracer.debug("<shutdown>", "shutdown OpenCard");
    refCount_--;

    if (refCount_ == 0) {
      // Iterate over cached SmartCard objects to close them explicitly.
      // Note that close removes the SmartCard object from the cache.
      while (!smartCardCache.isEmpty()) {
        ((SmartCard) smartCardCache.firstElement()).close();
      }

      // Remove all card terminals from CardTerminalRegistry singleton.
      CardTerminalRegistry terminalRegistry = CardTerminalRegistry.getRegistry();
      Enumeration terminals = terminalRegistry.getCardTerminals();
      while (terminals.hasMoreElements()) {
        terminalRegistry.remove((CardTerminal) terminals.nextElement());
      }

      // Remove all card service factories from CardServiceRegistry singleton.
      CardServiceRegistry serviceRegistry = CardServiceRegistry.getRegistry();
      Enumeration serviceFactories = serviceRegistry.getCardServiceFactories();
      while (serviceFactories.hasMoreElements()) {
        serviceRegistry.remove((CardServiceFactory) serviceFactories.nextElement());
      }

      // Indicate that OpenCard is down.
      OCFisStarted = false;
    }
  }
  /**
   * Initializes the entire OpenCard Framework and is meant to be the first method to
   * be invoked in any application in order to setup OpenCard properly.
   * This process tries to get the OpenCard properties and fill the
   * <TT>CardTerminalRegistry</TT> and <TT>CardServiceRegistry</TT>by means of the according property
   * entries.
   *
   * @exception ClassNotFoundException
   *            thrown when one of the required classes is not found
   * @exception OpenCardPropertyLoadingException
   *            thrown if something goes wrong during the the property loading
   *            process
   */
  public synchronized static void start()
  throws OpenCardPropertyLoadingException,
  ClassNotFoundException,
  CardServiceException,
  CardTerminalException
  {
    if (!OCFisStarted) {
      ctracer.debug ("<start>", "startup opencard");

      // check whether the default value for "OpenCard.loaderClassName" was overwritten
      String loaderClassName =
        SystemAccess.getSystemAccess().getProperty ("OpenCard.loaderClassName",
          OpenCardConstants.DEFAULT_OPENCARD_LOADER_CLASSNAME);

      // if we have a class name of a property loading class
      if (loaderClassName != null && loaderClassName.length() > 0) {
        ctracer.debug ("<start>", "use loader class: " + loaderClassName);

        try {
          Class loaderClass = Class.forName (loaderClassName);
          OpenCardConfigurationProvider loader =
          (OpenCardConfigurationProvider) loaderClass.newInstance();

          // al right, try to get OpenCard's properties
          loader.loadProperties();

          ctracer.debug ("<start>", "loader loaded properties: ");
        } catch (InstantiationException ie) {
          throw new CardServiceOperationFailedException(ie.toString());
        } catch (IllegalAccessException iae) {
          throw new CardServiceOperationFailedException(iae.toString());
        }
      } else {
        ctracer.debug ("<start>", "did not use a loader class!");
      }

      Tracer.init();
      EventGenerator.getGenerator();
      configureTerminalRegistry();
      configureServiceRegistry();

      OCFisStarted = true;
    } else {
      ctracer.debug ("<start>", "already configured");
    }
    refCount_++;
    ctracer.debug ("<start>", "finished");
  } // start
  /**
   * Initializes the entire OpenCard Framework and is meant to be the first method to
   * be invoked in any application in order to setup OpenCard properly.
   * This does not fill the <TT>CardTerminalRegistry</TT> and <TT>CardServiceRegistry</TT>,
   * which is left to the application.
   *
   * @exception ClassNotFoundException
   *            thrown when one of the required classes is not found
   * @exception OpenCardPropertyLoadingException
   *            thrown if something goes wrong during the the property loading
   *            process
   */
  public synchronized static void startup()
  throws OpenCardPropertyLoadingException,
  ClassNotFoundException,
  CardServiceException,
  CardTerminalException
  {
    if (!OCFisStarted) {
      ctracer.debug ("<start>", "startup opencard");

      EventGenerator.getGenerator();

      OCFisStarted = true;
    } else {
      ctracer.debug ("<start>", "already configured");
    }
    refCount_++;
    ctracer.debug ("<start>", "finished");
  } // start
  /**
   * Waits for a card to be inserted into any of the card terminals
   * attached to the system. Returns a SmartCard object if the provided
   * CardRequest matches the inserted SmartCard.
   * Returns null if a SmartCard is inserted that doesn't match the CardRequest.
   * This method must not be called from the event
   * handling methods <tt>cardInserted</tt> or <tt>cardRemoved</tt> of a
   * <tt>CTListener</tt>, since this may result in a deadlock. If a card
   * request for a card inserted event has to be satisfied,
   * <tt>getSmartCard</tt> has to be used instead.
   *
   * @param     req
   *            A <tt>CardRequest</tt> object describing the kind of
   *            smart card that we are interested in.
   * @return    A <tt>SmartCard</tt> object if the request could be satisfied;
   *            <tt>null</tt> if it could not, because a timeout occurred.
   *
   * @see CardRequest
   * @see opencard.core.event.CTListener
   * @see opencard.core.event.CTListener#cardInserted
   * @see opencard.core.event.CTListener#cardRemoved
   * @see #getSmartCard(opencard.core.event.CardTerminalEvent, opencard.core.service.CardRequest)
   */
  public static SmartCard waitForCard(CardRequest req)
  throws CardTerminalException
  {
    return waitForCard(req, null);
  }

  /**
   * Waits for a card to be inserted into any of the locked card terminals
   * attached to the system. Returns a SmartCard object if the provided
   * CardRequest matches the inserted SmartCard.
   *
   * @param     req
   *            A <tt>CardRequest</tt> object describing the kind of
   *            smart card that we are interested in.
   * @param     lockHandle
   *            handle obtained by lock owner when locking a terminal. This
   *            parameter is only necessary for locked terminals / slots.
   * @return    A <tt>SmartCard</tt> object if the request could be satisfied;
   *            <tt>null</tt> if it could not, because a timeout occurred.
   *
   * @see CardRequest
   * @see opencard.core.event.CTListener
   * @see opencard.core.event.CTListener#cardInserted
   * @see opencard.core.event.CTListener#cardRemoved
   * @see #getSmartCard(opencard.core.event.CardTerminalEvent, opencard.core.service.CardRequest)
   */
  public static SmartCard waitForCard(CardRequest req, Object lockHandle)
  throws CardTerminalException
  {
    ctracer.debug("waitForCard", "passing request " + req + " to CardServiceRegistry");

    CardWaiter cardWaiter = new CardWaiter(req, lockHandle);
    SmartCard newCard = cardWaiter.waitForCard();
    if (newCard != null) {
//      smartCardCache.addElement(newCard);
      ctracer.debug("waitForCard", smartCardCache.size() + " elements in cache");
    }
    
    return newCard;
  }
  
  /**
   * Reset inserted card
   * 
   * @return CardID of card in reader
   * 
   * @throws CardTerminalException
   */
  public CardID reset(boolean warm) throws CardTerminalException {
      cid = scheduler.reset(null, warm, false);
      return cid;
  }
  
  /**
   * Sets the APDU tracer that monitors all APDU to and from the card-
   * 
   * @param tracer the APDUTracer
   */
  public void setAPDUTracer(APDUTracer tracer) {
     aPDUTracer = tracer;
     scheduler.getSlotChannel().setAPDUTracer(tracer);
  }
  
  /**
   * Return the current APDUTracer
   * @return
   */
  public APDUTracer getAPDUTracer() {
     return aPDUTracer;
  }
}
