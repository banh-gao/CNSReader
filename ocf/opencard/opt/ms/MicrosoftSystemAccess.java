package opencard.opt.ms;

import java.util.Properties;

import opencard.core.util.SystemAccess;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;

/**
 * Singleton that provides access to protected system resources 
 * like file I/O, properties, native code under Microsoft browsers.
 * Thread safe: each thread can only access its own SystemAccess class.
 * <br>
 * To run OpenCard under a Microsoft browser install the browser specific
 * SystemAccess class in the applets init() method as follows:
 * <br>
 * opencard.core.util.SystemAccess sys = 
 *    opencard.opt.ms.MicrosoftSystemAccess();
 * opencard.core.util.SystemAccess.setSystemAccess(sys);
 *
 * @author Peter Bendel (peter_bendel@de.bm.com)
 *
 * @version $Id: MicrosoftSystemAccess.java,v 1.1.1.1 1999/10/05 15:08:47 damke Exp $
 *
 * @see opencard.core.util.SystemAccess
 * @see opencard.opt.netscape.NetscapeSystemAccess
 */

public class MicrosoftSystemAccess extends SystemAccess {
/**
 * make sure all privileges are present even in ctor to avoid further dialogs during execution
 */
  public MicrosoftSystemAccess() {
    PolicyEngine.assertPermission(PermissionID.SYSTEM);   

  }

/**
 * Access system properties
 */
  public boolean getBoolean(String key) {
    //System.out.println("using MicrosoftSystemAccess.getBoolean()");
    PolicyEngine.assertPermission(PermissionID.PROPERTY);
    return Boolean.getBoolean(key);
  }

/**
 * Access system properties
 */
  public Properties getProperties() {
    //System.out.println("using MicrosoftSystemAccess.getProperties()");
    PolicyEngine.assertPermission(PermissionID.PROPERTY);
    return System.getProperties();
  }

/**
 * Access system properties
 */
  public String getProperty(String key) {
    //System.out.println("using MicrosoftSystemAccess.getProperty()");
    PolicyEngine.assertPermission(PermissionID.PROPERTY);

    return System.getProperty(key);
  }

/**
 * Access system properties
 */
  public String getProperty(String key, String def) {
    //System.out.println("using MicrosoftSystemAccess.getProperty()");
    PolicyEngine.assertPermission(PermissionID.PROPERTY);

    return System.getProperty(key,def);
  }

/**
 * Link to a native DLL.
 */
  public void loadLibrary(String libName) {

    //System.out.println("using MicrosoftSystemAccess.loadLibrary()");
    PolicyEngine.assertPermission(PermissionID.SYSTEM);

    System.loadLibrary(libName);
  }

/**
 * Access system properties
 */
  public Properties loadProperties(String filename) throws java.io.FileNotFoundException, java.io.IOException{
    //System.out.println("using MicrosoftSystemAccess.loadProperties()");
    PolicyEngine.assertPermission(PermissionID.FILEIO);
    Properties props = new Properties ();
    props.load (new java.io.FileInputStream (new java.io.File (filename) ) );
    return props;
  }
}