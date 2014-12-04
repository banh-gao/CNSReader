
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


package opencard.core;


/**
 * Common constants for all <i>OpenCard Framework</i> classes.
 *
 * @author  Dirk Husemann (hud@zurich.ibm.com)
 * @author  Mike Wendler  (mwendler@de.ibm.com)
 * @version $Id: OpenCardConstants.java,v 1.1.1.1 1999/10/05 15:34:30 damke Exp $
 */

public interface OpenCardConstants {

  /** Prefix for accessing system properties. */
  public final static String OPENCARD_PROPERTY = "OpenCard.";

  /** properties file default name */
  public final static String OPENCARD_PROPERTIES    = "opencard.properties";

  /** alternative properties file default name */
  public final static String OPENCARD_DOTPROPERTIES = ".opencard.properties";

  /** class name of the default property loader class.*/
  public static final String DEFAULT_OPENCARD_LOADER_CLASSNAME = "opencard.opt.util.OpenCardPropertyFileLoader";

  /** card service registry tag. */
  public final static String CARD_SERVICE_REGISTRY_TAG  = OPENCARD_PROPERTY + "services";

  /** card terminal registry tag. */
  public final static String CARD_TERMINAL_REGISTRY_TAG = OPENCARD_PROPERTY + "terminals";

  /** Encoding used for application identifiers */
  public final static String APPID_ENCODING = "8859_1";

  /** SmartCard events 0x002. */
  public final static int SMARTCARD_REMOVED = 0x0021;

  /** for future use: card service download URL */
  public final static String CARD_SERVICE_URL = CARD_SERVICE_REGISTRY_TAG + ".URL";

  /** for future use: card service download : class implementing service list */
  public final static String CARD_SERVICE_LISTER_CLASS = CARD_SERVICE_REGISTRY_TAG + ".list";

  /** for future usecard service download: check signature of downloaded service archive */
  public final static String CARD_SERVICE_CHECK = CARD_SERVICE_REGISTRY_TAG + ".checkIntegrity";

  /** for future usecard service download: path where downloaded archives are cached */
  public final static String CARD_SERVICE_CACHEPATH = CARD_SERVICE_REGISTRY_TAG + ".cachePath";


  /** for future usecard service download: seconds downloaded archives are cached */
  public final static String CARD_SERVICE_CACHETIME = CARD_SERVICE_REGISTRY_TAG + ".cacheTime";


} // interface OpenCardConstants

// $Log: OpenCardConstants.java,v $
// Revision 1.1.1.1  1999/10/05 15:34:30  damke
// Import OCF1.1.1 from Zurich
//
// Revision 1.2  1999/08/09 11:18:24  ocfadmin
// replacing OCF 1.1 with updates of OCF 1.1.1 (aka Hudson) as of Mai 1999 (by J.Damke)
//
// Revision 1.23  1998/10/23 12:20:23  cvsusers
// test (pbendel)
//
// Revision 1.22  1998/10/21 11:05:21  cvsusers
// add persistent caching (pbendel)
//
// Revision 1.21  1998/09/18 09:57:31  cvsusers
// first prototype of cardservice download (pbendel)
//
// Revision 1.20  1998/07/28 13:00:14  cvsusers
// updated doc comments (wendler)
//

