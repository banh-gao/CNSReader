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

import opencard.core.util.HexString;
import opencard.core.util.Tracer;
import opencard.opt.applet.AppletID;
import opencard.opt.applet.AppletInfo;
import opencard.opt.applet.mgmt.InvalidAppletInfoException;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.util.TLV;


/** The <tt>EMVAppletInfo</tt> encapsulates the information
 * describing an application which is stored on an EMV'96 card.<p>
 * It extends the generic AppletInfo class.<p>
 *
 * @author   Thomas Stober (tstober@de.ibm.com)
 * @version  $Id: EMVAppletInfo.java,v 1.1 1999/11/23 10:24:17 damke Exp $
 *
 * @see opencard.opt.applet.AppletInfo
 */

public class EMVAppletInfo extends AppletInfo implements EMVTags {

  // The instance tracer
  private Tracer itracer = new Tracer(this, EMVAppletInfo.class);


  /** Preferred name retrieved from an EMV EF_DIR (optional) */
  protected byte[] prefName = null;

  /** Priority Index retrieved from an EMV EF_DIR (optional) */
  protected byte[] prioInd = null;

  /** Discretionary Data retrieved from an EMV EF_DIR (optional) */
  protected byte[] discrData = null;


  /** Auxillary string array. */
  protected final static String[] hexChars = {"0", "1", "2", "3",
                                              "4", "5", "6", "7",
                                              "8", "9", "A", "B",
                                              "C", "D", "E", "F"};


  /////////////// constructing ///////////////////////////////////////


  /** Instantiate an <tt>EMVAppletInfo</tt>.
   * (hidden, contains "defaults" for all instances set by each constructor)
   */
  protected EMVAppletInfo() {
    // default domain is the root MF
    setDomain(new CardFilePath (MASTER_FILE));
  }



  public EMVAppletInfo(AppletID aid, String label) {
    setAppletID(aid);
    setLabel(label);
  }




  /** Instantiate an <tt>EMVAppletInfo</tt> from a TLV object.
   * @param info
   *        A <tt>TLV</tt> object from which to instantiate.
   */
  public EMVAppletInfo(TLV info)
    throws InvalidAppletInfoException {

    // set defaults
    this();

    // analyse the given Context and write the contained data into the
    // attributes of this class
    fromEMVTLV(info);
  }


  /////////////// access  ///////////////////////////////////////////


  /** Get the application preferred name; Tag 0x9F12.<p>
   *
   *@return A byte array of length 1-16 containing the application preferred name
   *        or null if preferred name is not present.
   */
  public byte[] getPreferredName() {return prefName;}


  /** Get the application priority indicator; Tag 0x87.<p>
   *
   * @return A single byte indicating the application priority or null
   *         if priority is not present.
   */
  public byte[] getPriority() {return prioInd;}


  /** Get the Discretionary Data.<p>
   *
   * @return A single byte indicating the discretionary Data.
   */
  public byte[] getDiscretionaryData() {return discrData;}




  /////////////// System /////////////////////////////////////////


  /**
   * Analyses the given EMV compliant application description TLV and
   * writes the data into the attributes of this class
   *
   * @param info    description of this application packed in an EMV TLV structure
   */
  public void fromEMVTLV(TLV info)
    throws InvalidAppletInfoException {
    setAppletID(info);
    setLabel(info);
    setDomain(info);
    setPreferredName(info);
    setDiscretionaryData(info);
    setPriority(info);
    }



  /** Set the application ID*/
  private void setAppletID(TLV info)
    throws InvalidAppletInfoException {

    // retrieve the Application ID
    TLV tlv = info.findTag(TAG_EMV_ADF_NAME, null);
    if (tlv == null) throw new InvalidAppletInfoException(); // is mandatory
    setAppletID(new AppletID(tlv.valueAsByteArray()));
  }

  /** Set the label */
  private void setLabel(TLV info)
    throws InvalidAppletInfoException {

    // retrieve the Label
    TLV tlv = info.findTag(TAG_EMV_APP_LABEL, null);
    if (tlv == null) throw new InvalidAppletInfoException(); // is mandatory
    setLabel(new String(tlv.valueAsByteArray()));
  }


  /** Set the application preferred name; Tag 0x9F12.<p>*/
  private void setPreferredName(TLV info) {
    // retrieve the Preferred Name
    TLV tlv = info.findTag(TAG_EMV_APP_PREF_NAME, null);
    prefName = (tlv == null ? null : tlv.valueAsByteArray());
  }


  /** Set the application priority indicator; Tag 0x87.<p> */
  private void setPriority(TLV info) {
    // retrieve the Priority Index
    TLV tlv = info.findTag(TAG_EMV_APP_PRIO_IND, null);
    prioInd = (tlv == null ? null : tlv.valueAsByteArray());
  }


  /** Set the discretionary data.<p> */
  private void setDiscretionaryData(TLV info) {
    // retrieve the discretionary data
    TLV tlv = info.findTag(TAG_EMV_APP_DISCR_DATA, null);
    discrData = (tlv == null ? null : tlv.valueAsByteArray());
  }

  /** Set the applications directory (using the AID); Tag 0x73.<p> */
  private void setDomain(TLV info) {
    // This Attribute of the TLV structure is not used!
    // Instead of storing a command-to-perform for selecting the SecurityDomain (e.g. DF)
    // OCF uses the Select-by-AID for selecting the CardFilePath.

    // Take the AID to select the Security Domain (e.g. DF)by AID
//  CardFilePath path = new CardFilePath("#"+getAppletID().toCompactString()); // go back to root first!

    CardFilePath path = new CardFilePath("#"+hexify(getAppletID().getBytes(), false)); // go back to root first!
    setDomain(path);
  }



    /** Helper method to hexify a byte array.<p>
    *
    * @param data       Byte array to convert to HexString
    * @param seperator  true, if Byte array should be inserted
    *                   between each HexByte
    *
    * @return HexString
    */
    private static String hexify(byte[] data, boolean seperator) {

        if(data == null) return "null";

        StringBuffer out = new StringBuffer(256);
        int n = 0;

        for(int i=0; i<data.length; i++) {

          if (seperator) {if(n>0) out.append(' '); }

          out.append(hexChars[(data[i]>>4) & 0x0f]);
          out.append(hexChars[data[i] & 0x0f]);

          if(++n == 16) {
                out.append('\n');
                n = 0;
          }
        }

        return out.toString();
  }



  /**
   * Packs the attributes of this application info class into an
   * EMV compliant TLV structure
   *
   * @return TLV    description of this application packed in an EMV TLV structure
   */
  public TLV toEMVTLV()
  {
    if (aid==null) return null;

    TLV infoTLV=null;

    TLV nameTLV = new TLV ( TAG_EMV_ADF_NAME , getAppletID().getBytes()) ;
    infoTLV = new TLV (TAG_EMV_APP_TEMPLATE, nameTLV);

    if (label!=null)
    {
      TLV labelTLV = new TLV ( TAG_EMV_APP_LABEL , getLabel().getBytes()) ;
      infoTLV.add(labelTLV);
    }
    if (prefName!=null)
    {
      TLV prefNameTLV = new TLV ( TAG_EMV_APP_PREF_NAME , prefName) ;
      infoTLV.add(prefNameTLV);
    }
    if (getDomain()!=null)
    {
        // This Attribute of the TLV structure is not used!
        // Instead of storing a command-to-perform for selecting the SecurityDomain (e.g. DF)
        // OCF uses the Select-by-AID for selecting the CardFilePath.
    }
    if (discrData!=null)
    {
      TLV dataTLV = new TLV ( TAG_EMV_APP_DISCR_DATA , discrData) ;
      infoTLV.add(dataTLV);
    }
    if (prioInd!=null)
    {
      TLV prioIndTLV = new TLV ( TAG_EMV_APP_PRIO_IND , prioInd) ;
      infoTLV.add(prioIndTLV);
    }

    System.out.println("I have built an app TLV: "+infoTLV.toString());

    return infoTLV;
  }



  public String toString() {
    StringBuffer at = new StringBuffer("EMV AppletInfo:\n\t");
      at.append(super.toString());
    if (prefName != null)
      at.append("Preferred Name: ").append(new String(prefName)).append("\n\t");
    if (prioInd != null)
      at.append("Priority indicator: ").append(HexString.hexify(prioInd)).append("\n\t");
    if (discrData != null)
      at.append("Discretionary data: ").append(discrData.toString()).append("\n");
    return at.toString();
 }
}
