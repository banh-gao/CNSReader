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

package opencard.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import opencard.core.OpenCardConstants;
import opencard.core.event.TracerEvent;
import opencard.core.event.TracerListener;

/**
 * Utility class for tracing. Each class that wants its operations traced
 * needs to instantiate a <tt>Tracer</tt> object specifying a tag to enable
 * it by. Tracing at runtime is controlled through the set of tags configured
 * in the opencard.tracer property:
 * <dl>
 *    <dt><tt>tag</tt></dt>
 *    <dd>Trace all messages submitted under <tt>tag</tt> or any tag beginning
 *        with <tt>tag</tt></dd>
 *    <dt><tt>tag:level</tt></dt>
 *    <dd>Trace all message with a level greater or equal to <tt>level</tt> submitted
 *        under <tt>tag</tt> or any tag beginning with <tt>tag</tt></dd>
 * </dl>
 *
 * @author  Peter Trommler      (trp@zurich.ibm.com)
 * @author  Mike Wendler        (mwendler@de.ibm.com)
 * @author  Stephan Breideneich (sbreiden@de.ibm.com)
 * @version $Id: Tracer.java,v 1.1.1.1 1999/10/05 15:34:32 damke Exp $
 */

public class Tracer {

  private static String[] traceList = new String[0];
  private static int[] traceLevels=new int[0];
  private static boolean eventsOnly = false;
  private static boolean condensed = false;
  private static Hashtable<TracerListener, TracerListener> tracerListeners = new Hashtable<TracerListener, TracerListener>();

  private String myClass;
  private Object me;


  /**
   * Constructs a tracer for a certain object with the name of its class.
   *
   * @param me
   *        The object that instantiates the tracer.
   * @param className
   *        Name identifying the type of trace
   */
  public Tracer (Object me, String className) {
    myClass = className;
    this.me = me;

  }

  /**
   * Initialize tracing by parsing trace properties into tracelist
   */
   public static void init () {
    try {
      
    SystemAccess sys = SystemAccess.getSystemAccess();
    String traceProperty = sys.getProperty(OpenCardConstants.OPENCARD_PROPERTY + "trace", "");
    eventsOnly = sys.getBoolean(OpenCardConstants.OPENCARD_PROPERTY + "trace.eventsOnly");
    condensed = sys.getBoolean(OpenCardConstants.OPENCARD_PROPERTY + "trace.condensed");
    StringTokenizer st = new StringTokenizer(traceProperty);


    int tokenCount = st.countTokens();
    traceList = new String [tokenCount];
    traceLevels = new int [tokenCount];

    for (int i = 0; i < tokenCount; i++) {
      StringTokenizer sst = new StringTokenizer(st.nextToken(), ":");
      traceList[i] = sst.nextToken();

      try {
        traceLevels[i] = sst.hasMoreTokens() ?
		      Integer.parseInt(sst.nextToken()) : TraceLevels.LOWEST;
      }
      catch (NumberFormatException nfe) {
        traceLevels[i] = TraceLevels.LOWEST;
      }
    }


    } catch (Throwable t) {
      t.printStackTrace();
    }
   }


  /**
   * Traces a certain class.
   *
   * @param me
   *        The object that instantiates the tracer.
   * @param clazz
   *        the class to be traced
   */
  public Tracer (Object me, Class<?> clazz) {
    this (me, clazz.getName () );
  }


  /**
   * Traces a certain class.
   *
   * @param clazz
   *        the class to be traced
   */
  public Tracer (Class<?> clazz) {
    this (clazz, clazz.getName () );
  }


  /**
   * Gets the current trace level of the tracer of a certain class/object.
   *
   * @return the trace level of this' tracer's class or <TT>-1</TT> if this
   *   class is not to be traced at all
   */
  public int getTraceLevel () {
    int traceLevel = -1;

    for (int i = 0; i < traceList.length; i++)
      if (traceList [i] != null &&
          myClass.startsWith (traceList [i]) )
        traceLevel = traceLevels [i];

    return (traceLevel);
  }


  /**
   * Adds a message to the trace.<p>
   *
   * @param    level
   *           The debug level.
   * @param    className
   *           The class name of the source.
   * @param    me
   *           The source object.
   * @param    meth
   *           The method name.
   * @param    message
   *           The trace message
   * @param    frisbee
   *           The <tt>Trowable</tt> to trace
   */
  public static void trace(Object me, String className, int level, String meth,
    String message, Throwable frisbee) {

    if (message == null && frisbee == null)
      return;

    if (className != null) {

      if (traceList == null)
        return;

      for (int i = 0; i < traceList.length; i++) {
        if (traceList [i] != null &&
            className.startsWith(traceList [i]) &&
            level <= traceLevels [i]) {

          TracerEvent te = message != null ?
            new TracerEvent(level, me, meth, Thread.currentThread(), message) :
            new TracerEvent(level, me, meth, Thread.currentThread(), frisbee);
          
          if (!eventsOnly) {
            StringBuffer sb = new StringBuffer(te.getLevelName()).append(" ");

            if (te.getSource() instanceof Class)
              sb.append(((Class<?>)te.getSource()).getName()).append(".");
            else
              sb.append(te.getSource().getClass().getName()).append(".");

            if (condensed) {
              sb.append(te.getMethodName());
              
              if (te.getMessage() != null) {
                sb.append(":").append(te.getMessage());
              } else {
                Throwable boomerang = te.getThrowable();
                // cannot use StringWriter directly...
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                boomerang.printStackTrace(pw);
                pw.flush();			// docu says it should flush() sw as well
                sb.append("--- throwable ").append(sw.toString());              }
              
            } else {
              sb.append(te.getMethodName()).append("\n");
              
              if (te.getMessage() != null) {
                sb.append("--- message   ").append(te.getMessage()).append("\n");
              } else {
                Throwable boomerang = te.getThrowable();
                // cannot use StringWriter directly...
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                boomerang.printStackTrace(pw);
                pw.flush();			// docu says it should flush() sw as well
                sb.append("--- throwable ").append(sw.toString()).append("\n");
                te = new TracerEvent(level, me, meth, Thread.currentThread(), sw.toString());
              }
              
            }

          }

          if (!tracerListeners.isEmpty()) {
            TracerListener tl = null;
            for (Enumeration<TracerListener> e = tracerListeners.elements(); e.hasMoreElements(); ) {
              tl = e.nextElement();
              tl.traceEvent(te);
            }
          }

        break;
        }
      }
    }
  }


  /**
   * Adds a trace message.
   *
   * @param    message
   *           The trace message.
   */
  protected void trace (int level, String meth, String message) {
    Tracer.trace(me, myClass, level, meth, message, null);
  }


  /**
   * Traces the stack trace of the <tt>Throwable</tt>.
   *
   * @param    level
   *           The trace level for this trace event.
   * @param    meth
   *           The name of the method that generates this trace event.
   * @param    frisbee
   *           The <tt>Throwable</tt> to trace.
   */
  protected void trace(int level, String meth, Throwable frisbee) {
    Tracer.trace(me, myClass, level, meth, null, frisbee);
  }


  /**
   * Traces a <tt>DEBUG</tt> level message.
   */
  public void debug(String meth, String msg) {
    trace(TraceLevels.DEBUG, meth, msg);
  }


  /**
   * Traces a <tt>DEBUG</tt> throwable
   */
  public void debug(String meth, Throwable frisbee) {
    trace(TraceLevels.DEBUG, meth, frisbee);
  }


  /**
   * Traces a <tt>INFO</tt> level message.
   */
  public void info(String meth, String msg) {
    trace(TraceLevels.INFO, meth, msg);
  }


  /**
   * Traces a <tt>INFO</tt> throwable
   */
  public void info(String meth, Throwable frisbee) {
    trace(TraceLevels.INFO, meth, frisbee);
  }


  /**
   * Traces a <tt>NOTICE</tt> level message.
   */
  public void notice(String meth, String msg) {
    trace(TraceLevels.NOTICE, meth, msg);
  }


  /**
   * Traces a <tt>NOTICE</tt> throwable
   */
  public void notice(String meth, Throwable frisbee) {
    trace(TraceLevels.NOTICE, meth, frisbee);
  }


  /**
   * Traces a <tt>WARNING</tt> level message.
   */
  public void warning(String meth, String msg) {
    trace(TraceLevels.WARNING, meth, msg);
  }


  /**
   * Trace a <tt>WARNING</tt> throwable
   */
  public void warning(String meth, Throwable frisbee) {
    trace(TraceLevels.WARNING, meth, frisbee);
  }


  /**
   * Traces a <tt>ERROR</tt> level message.
   */
  public void error(String meth, String msg) {
    trace(TraceLevels.ERROR, meth, msg);
  }

  /**
   * Traces a <tt>ERROR</tt> throwable
   */
  public void error(String meth, Throwable frisbee) {
    trace(TraceLevels.ERROR, meth, frisbee);
  }


  /**
   * Traces a <tt>CRITICAL</tt> level message.
   */
  public void critical(String meth, String msg) {
    trace(TraceLevels.CRITICAL, meth, msg);
  }


  /**
   * Traces a <tt>CRITICAL</tt> throwable
   */
  public void critical(String meth, Throwable frisbee) {
    trace(TraceLevels.CRITICAL, meth, frisbee);
  }

  /**
   * Traces a <tt>ALERT</tt> level message.
   */
  public void alert(String meth, String msg) {
    trace(TraceLevels.ALERT, meth, msg);
  }


  /**
   * Traces a <tt>ALERT</tt> throwable
   */
  public void alert(String meth, Throwable frisbee) {
    trace(TraceLevels.ALERT, meth, frisbee);
  }


  /**
   * Traces a <tt>EMERGENCY</tt> level message.
   */
  public void emergency(String meth, String msg) {
    trace(TraceLevels.EMERGENCY, meth, msg);
  }


  /**
   * Trace a <tt>EMERGENCY</tt> throwable
   */
  public void emergency(String meth, Throwable frisbee) {
    trace(TraceLevels.EMERGENCY, meth, frisbee);
  }


  /**
   * Adds a <tt>TracerListener</tt>.
   */
  public static void addTracerListener(TracerListener listener) {
  	Tracer.trace(Tracer.class, Tracer.class.getName(), TraceLevels.DEBUG, "addTracerListener",
		     "adding " + listener, null);
	  tracerListeners.put(listener, listener);
    Tracer.trace(Tracer.class, Tracer.class.getName(), TraceLevels.DEBUG, "addTracerListener",
		     "tracerListener " + tracerListeners, null);
  }


  /**
   * Removes a <tt>TracerListener</tt>.
   */
  public static void removeTracerListener(TracerListener listener) {
    tracerListeners.remove(listener);
  }

}
