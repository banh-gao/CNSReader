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


package opencard.core.event;

import opencard.core.OpenCardConstants;
import opencard.core.util.TraceLevels;

/** A <tt>TracerEvent</tt> signals a trace event as send by the <tt>Tracer</tt>.
 *
 * @author  Dirk Husemann (hud@zurich.ibm.com), Mike Wendler (mwendler@de.ibm.com)
 * @version $Id: TracerEvent.java,v 1.1.1.1 1999/10/05 15:34:31 damke Exp $
 *
 * @see opencard.core.util.Tracer
 * @see opencard.core.util.TraceLevels
 */
public class TracerEvent extends OpenCardEvent implements OpenCardConstants, TraceLevels {
    public final static int TRACE_EVENT = 0x01;
    private int level = -1;
    private String levelName = null;
    private Thread thread = null;
    private Throwable frisbee = null;
    private String meth = null;
    private String msg = null;

    /** Basic constructor */
    private TracerEvent(int level, Object source, String meth, Thread thread) {
	super(source, TRACE_EVENT);
	this.level = level;
	this.levelName = levelAsString[level];
	this.meth = meth;
	this.thread = thread;
    }	
    
    /** Instantiate a <tt>CardTerminalEvent</tt>. */
    public TracerEvent(int level, Object source, String meth, Thread thread, Throwable frisbee) {
	this(level, source, meth, thread);
	this.frisbee = frisbee;
    }
  
    /** Instantiate a <tt>CardTerminalEvent</tt>. */
    public TracerEvent(int level, Object source, String meth, Thread thread, String msg) {
	this(level, source, meth, thread);
	this.msg = msg;
    }

    /** Return the thread that caused this event. */
    public Thread getThread() {
	return thread;
    }

    /** Return the <tt>Throwable</tt> that caused this event. */
    public Throwable getThrowable() {
	return frisbee;
    }

    /** Return the message contained in this event. */
    public String getMessage() {
	return msg;
    }

    /** Return the method contained in this event. */
    public String getMethodName() {
	return meth;
    }

    /** Return the level. */
    public int getLevel() {
	return level;
    }

    /** Return the level as a string. */
    public String getLevelName() {
	return levelName;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer(super.toString());
	sb.append("\n---level     ").append(levelName);
	sb.append("\n---method    ").append(meth);
	sb.append("\n---thread    ").append(thread);
	if (frisbee != null)
	    sb.append("\n---throwable ").append(frisbee);
	if (msg != null)
	    sb.append("\n---msg       ").append(msg);
	return sb.toString();
    }
}
