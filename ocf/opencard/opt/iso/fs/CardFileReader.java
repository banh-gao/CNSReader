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

package opencard.opt.iso.fs;

import java.io.IOException;
import java.io.InputStreamReader;

import opencard.core.terminal.CardTerminalException;

/** <tt>CardFileReader</tt> is a subclass of <tt>InputStreamReader</tt>
  * that can be used to read text data from a <tt>CardFile</tt>.
  * The constructor of this class assumes that the default character encoding
  * and the default byte-buffer size are appropriate.
  * 
  * @author Peter Trommler (trp@zurich.ibm.com)
  * @version $Id: CardFileReader.java,v 1.2 1999/11/03 12:37:17 damke Exp $
  *
  * @see java.io.FileReader
  * @see opencard.opt.iso.fs.CardFile
  * @see opencard.opt.iso.fs.CardFileInputStream
  */
public class CardFileReader extends InputStreamReader {
  /** Instantiate a <tt>CardFileReader</tt> for the specified <tt>CardFile</tt>
    * object.<p>
    *
    * @param     file
    *            The <tt>CardFile</tt> object to read from.
    * @exception java.io.IOException
    *            Thrown if the file is not found or file type does not match.
    * @exception opencard.core.terminal.CardTerminalException
    *            Thrown when the smart card has been removed.
    */
  public CardFileReader(CardFile file)
    throws IOException, CardTerminalException {
      super(new CardFileInputStream(file));
  }
}
	    
