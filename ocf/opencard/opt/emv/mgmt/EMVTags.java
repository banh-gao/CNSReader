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

package opencard.opt.emv.mgmt;

import opencard.opt.util.Tag;

/**  <tt>EMVTags</tt> definitions.<p>
 *
 * @author   Thomas Stober (tstober@de.ibm.com)
 * @version  $Id: EMVTags.java,v 1.1 1999/11/23 10:24:17 damke Exp $
 *
 */

public interface EMVTags {
  // EMV Tags
  public final static String MASTER_FILE           = ":3F00";
  public final static String EMV_EF_DIR            = ":2F00";
  public final static Tag TAG_EMV_ADF_NAME         = new Tag(  15, (byte)1, false); // tag 0x4F
  public final static Tag TAG_EMV_APP_LABEL        = new Tag(  16, (byte)1, false); // tag 0x50
  public final static Tag TAG_EMV_CMD_TO_PERFORM   = new Tag(  18, (byte)1, false); // tag 0x52
  public final static Tag TAG_EMV_APP_TEMPLATE     = new Tag(   1, (byte)1, true);  // tag 0x61
  public final static Tag TAG_EMV_APP_DISCR_DATA   = new Tag(  19, (byte)1, true);  // tag 0x73
  public final static Tag TAG_EMV_APP_PRIO_IND     = new Tag(   7, (byte)2, false); // tag 0x87
  public final static Tag TAG_EMV_SFI              = new Tag(   8, (byte)2, false); // tag 0x88
  public final static Tag TAG_EMV_DDF_NAME         = new Tag(  29, (byte)2, false); // tag 0x9D
  public final static Tag TAG_EMV_APP_PREF_NAME    = new Tag(7954, (byte)2, false); // tag 0x9F12
}

