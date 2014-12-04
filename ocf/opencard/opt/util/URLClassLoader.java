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


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import opencard.core.util.Tracer;


/**
  * Special class loader for loading classes from a URL.
  * This class loader is very much like the <code>AppletClassLoader</code>
  * which is not part of the official Java API.
  *
  * @author  Thomas Schaeck (schaeck@de.ibm.com)
  * @author  Peter Trommler (trp@zurich.ibm.com)
  * @version $Id: URLClassLoader.java,v 1.1.1.1 1999/10/05 15:08:48 damke Exp $
  */
public class URLClassLoader extends ClassLoader {
  private Tracer itracer = new Tracer(this, URLClassLoader.class);

  protected Hashtable classCache = new Hashtable();
  protected Hashtable byteCache = new Hashtable();
  protected URL url = null;
  protected String archive = null;
  protected boolean archiveLoaded = false;

  /** Create a new instance.
    *
    * @param url the <code>URL</code> from which this class loader loads
    * classes.
    */
  public URLClassLoader(URL url) {
    super();
    this.url = url;
  }

  /** Try to load classes from an archive first.
    *
    * @param url
    *        codebase of the archive and classes not found in the archive
    * @param archive
    *        path to the archive relative to <code>url</code>
    */
  public URLClassLoader(URL url, String archive) {
    this(url);
    this.archive = archive;
  }

  /** Load a class with the given name.
  *
  * @param name - the name of the class to be loaded.
  * @param resolve - indicates wether the class is to be resolved or not.
  *
  * @return The class that has been loaded.
  *
  * @exception ClassNotFoundException 
  *            Could not find class.
  */
  protected Class loadClass(String name, boolean resolve) 
  throws ClassNotFoundException {
    Class c;
    SecurityManager security = System.getSecurityManager();
    int lastDot = name.lastIndexOf('.');
    String packageName = (lastDot >= 0) ? name.substring(0, lastDot) : null;
    boolean needSecurityCheck = security != null && packageName != null;

    if (needSecurityCheck) {
      security.checkPackageAccess(packageName);
    }

    synchronized (classCache) {
      if (classCache.containsKey(name)) {
        return (Class)classCache.get(name);
      }

      try {
        c = findSystemClass(name);
        itracer.debug("loadClass", "Found System Class: " + name);
        classCache.put(name, c);
        return c;
      } catch (ClassNotFoundException e) {
        // ignore exception and load from URL	
        
      }

      if (needSecurityCheck) {
        security.checkPackageDefinition(packageName);
      }

      byte data[] = loadClassData(name);
      c = defineClass(name, data, 0, data.length);
      classCache.put(name, c);
      itracer.debug("loadClass", "Loaded class from URL: " + name);
    }

    if (resolve)
      resolveClass(c);
    return c;
  }

  /** Get the bytes for the class with the given name.
    *
    * @param name the class name
    *
    * @return The bytes for the class.
    * @exception ClassNotFoundException the archive or class file could not be read
    */
  protected synchronized byte[] loadClassData(String name)
  throws ClassNotFoundException {
    // we modify a number of internal data structures...
    String classPath = name.replace('.', '/');
    String classFile = classPath + ".class";

    if (archive != null) {
      // load from archive
      if (!archiveLoaded) {
        loadArchive(url,archive);
      }

      // XXX: add signature handling!
      if (byteCache.containsKey(classFile)) {
        byte[] bytes = (byte [])byteCache.get(classFile);
        // and remove the data to free up some memory (the Class is cached!)
        byteCache.remove(classFile);
        return bytes;
      } else {
        throw new ClassNotFoundException("archive: "+archive+" does not contain:"+classFile);
      }
    } else {
      // load individual class file
      BufferedInputStream bis = null;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
  
      try {
        URL classURL = new URL(url, classFile);
        InputStream is = classURL.openStream();
        bis = new BufferedInputStream(is);
  
        return inputStreamToByteArray(bis);
      } catch (MalformedURLException mue) {
        throw new ClassNotFoundException(mue.toString());
      } catch (IOException ioe) {
        throw new ClassNotFoundException(ioe.toString());
      } catch (Throwable t) {
        t.printStackTrace();
        throw new ClassNotFoundException(t.toString());
      } finally {
        try {
          if (bis!=null) {
            bis.close();
          }
        } catch (IOException e) {
          // ignore
          
        }
      }
    }


  }

  protected void loadArchive(URL url, String archive) throws ClassNotFoundException {
    ZipInputStream zis = null;

    try {
      URL archiveURL = new URL(url, archive);
      InputStream is = archiveURL.openStream();
      BufferedInputStream bis = new BufferedInputStream(is); // buffer the data
      zis = new ZipInputStream(bis);

      // XXX: handle duplicate entries...
      // synchronization!
      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        String name = ze.getName();
        byte[] data = inputStreamToByteArray(zis);
        byteCache.put(name, data);
      }
      archiveLoaded = true;

    } catch (MalformedURLException e) {
      throw new ClassNotFoundException(e.toString());
    } catch (IOException e) {
      throw new ClassNotFoundException(e.toString());
    } finally {
      if (zis != null)
        try {
          zis.close();
        } catch (IOException e) {
          // ignore
          
        }
    }
  }

  protected byte[] inputStreamToByteArray(InputStream is) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int nextByte;

    try {
      while ((nextByte = is.read()) != -1)
        baos.write(nextByte);

      return baos.toByteArray();
    } finally {
      baos.close();
    }
  }

} // class URLClassLoader
