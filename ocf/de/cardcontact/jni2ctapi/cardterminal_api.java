/*
 *  ---------
 * |.**> <**.|  CardContact
 * |*       *|  Software & System Consulting
 * |*       *|  Minden, Germany
 * |�**> <**�|  Copyright (c) 2000. All rights reserved
 *  --------- 
 *
 * See file LICENSE for details on licensing
 *
 * Abstract :       Implementation of a CTAPI Interface for Java.
 *
 * Author :         Frank Thater (FTH)
 *
 * Last modified:   08/04/2000
 *
 *****************************************************************************/

package de.cardcontact.jni2ctapi;

public class cardterminal_api
{
    public long ctInitPointer;
    public long ctClosePointer;
    public long ctDataPointer;
    	
    // maps to CT_Init
    public native int CT_Init (char ctn, char pn);
  
    // maps to CT_Close
    public native int CT_Close (char ctn);

    // maps to CT_Data
    public native int CT_Data (char ctn, byte dad, byte sad, int lenc, byte[] command, char lenr, byte[] response);

    // sets the name of the shared lib which holds the CTAPI references for a specific card terminal
    public native void setReader (String readername) throws UnsatisfiedLinkError;


    // get the native library
    static
    {
        String arch = System.getProperty("os.arch");
        System.loadLibrary("jni2ctapi-" + arch);
    }

    public cardterminal_api(String readername)
    {
        super();
        
        ctInitPointer = 0;
        ctClosePointer = 0;
        ctDataPointer = 0;
        
        setReader(System.mapLibraryName(readername));

    }
    
}



