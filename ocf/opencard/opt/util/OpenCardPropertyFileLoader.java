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

package opencard.opt.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import opencard.core.OpenCardConstants;
import opencard.core.util.OpenCardConfigurationProvider;
import opencard.core.util.OpenCardPropertyLoadingException;
import opencard.core.util.SystemAccess;



/**
 * <tt>OpenCardPropertyFileLoader</tt> is a utility class that loads the
 * <tt>opencard.properties</tt> file and merges the properties found in there
 * with the system properties. Usually system properties will override properties found
 * in the <tt>opencard.properties</tt> file. In case a property from the
 * <tt>opencard.properties</tt> should override a system property you will have to use a second
 * property using the first property's name plus the suffix <tt>.override</tt> and set it to
 * <TT>true</TT>, e.g.<p>
 *
 * <pre>OpenCard.trace = opencard.core:8</pre>
 *
 * To let this property always override the system property of the same name add<p>
 *
 * <pre>OpenCard.trace.override = true</pre>
 *
 * @author  Mike Wendler (mwendler@de.ibm.com)
 * @version $Id: OpenCardPropertyFileLoader.java,v 1.2 1999/11/03 12:37:19 damke Exp $
 *
 * @see opencard.core.util.OpenCardConfigurationProvider
 */

public class OpenCardPropertyFileLoader implements OpenCardConfigurationProvider {


  /**
   * Tries to load a property file from <tt>location</tt>.
   *
   * @param    location
   *           The path to the property file location.
   */
  protected void load (String location) {
    try {
      SystemAccess sys=SystemAccess.getSystemAccess();

      Properties props = sys.loadProperties(location);
      loadingDone = true;

      // ... make sure that System properties are not overridden accidentally
      Properties sysProps = sys.getProperties ();

      // iterate over the properties that were just read in
      Enumeration propertyNames = props.propertyNames ();
      while (propertyNames.hasMoreElements () ) {
        String
          key      = (String) propertyNames.nextElement (),
          override = (String) props.get (key + ".override");

        // if we have a system property with the same name and it is not
        // specified that it should be overridden -> just skip it
        if (sysProps.containsKey (key) &&
            ! (Boolean.valueOf (override). booleanValue ( ) ) )
	    continue;

        sysProps.put (key, props.get (key) );
      }
    }
    // just omit the exceptions -> calling method will throw an OpenCard specific one
    catch (FileNotFoundException fnfe) {}
    catch (IOException ioe) {}
  } // load


  /**
   * Loads OpenCard properties from a property file. The following locations will
   * be searched for a properties file (in that order):
   * <P><ul>
   *    <li><tt>[java.home]/lib/opencard.properties</tt>
   *    <li><tt>[user.home]/.opencard.properties</tt>
   *    <li><tt>[user.dir]/opencard.properties</tt>
   *    <li><tt>[user.dir]/.opencard.properties</tt>
   * </ul>
   * <BR>NOTE that if a property file exists in several of these locations each one is
   * being read. Conceptually this makes it possible to have a general property file, say
   * in <TT>user.home</TT>, and more specific ones in the current directories which
   * extend the general one.
   * <BR><TT>java.home</TT> is the directory of your java installation, <TT>user.home</TT>
   * the user's home directory, and <TT>user.dir</TT> the current working directory.
   *
   * @exception opencard.core.util.OpenCardPropertyLoadingException
   *    thrown when no property file could be found
   */
  public void loadProperties () throws OpenCardPropertyLoadingException {
    if (! loadingDone) {
      String dotPropStr = OpenCardConstants.OPENCARD_DOTPROPERTIES;
      String propStr    = OpenCardConstants.OPENCARD_PROPERTIES;

      SystemAccess sys=SystemAccess.getSystemAccess();

      String [] locations = {
        sys.getProperty ("java.home","") + File.separator + "lib" + File.separator + propStr,
        sys.getProperty ("user.home","") + File.separator + dotPropStr,
        sys.getProperty ("user.dir","")  + File.separator + propStr,
        sys.getProperty ("user.dir","")  + File.separator + dotPropStr };

      // iterate over all potential locations for 'opencard.properties' files starting
      // with the most general one and proceeding with the more specific ones in order
      // to be able to 'overwrite' more general properties
      for (int index = 0; index < locations.length; index++)  {
        //System.out.println("property file "+locations[index]);
        load (locations [index]);
      }
        

      if (! loadingDone)  // not even one 'opencard.properties' file found
        throw new OpenCardPropertyLoadingException ("property file not found");
    }
  } // loadProperties


  // data fields ----------------------------------------------------------------

  private boolean loadingDone = false;

} // class OpenCardPropertyFileLoader

