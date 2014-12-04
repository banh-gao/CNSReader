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

/** Small utility class to hexify bytes and shorts.
  *
  * @author   Michael Baentsch (mib@zurich.ibm.com)
  * @author   Dirk Husemann (hud@zurich.ibm.com)
  *
  * @version  $Id: HexString.java,v 1.1.1.1 2004/04/08 10:29:27 asc Exp $
  */
public class HexString {

  /** Auxillary string array. */
  protected final static String[] hexChars = {"0", "1", "2", "3", "4", "5", "6", "7",
                                              "8", "9", "A", "B", "C", "D", "E", "F"};

  /** Hex-dump a byte array (offset and printable ASCII included)<p>
    *
    * @param data  Byte array to convert to HexString
    *
    * @return HexString
    */
  public static String dump(byte[] data) {
          return dump(data,0,data.length);
  }


 /** Hex-dump a byte array (offset and printable ASCII included)<p>
   *
   * @param data  Byte array to convert to HexString
   * @param offset Start dump here
   * @param len Number of bytes to be dumped.
   * @return HexString
   */
  public static String dump(byte[] data,int offset, int len) {
      if(data == null) return "null";

      char[] ascii = new char[16];

      StringBuffer out = new StringBuffer(256);

      if (offset + len > data.length) {
          len = data.length - offset;
      }
      
      for(int i=offset; i<offset+len; ) {
          // offset
          out.append(hexify((i>>>8) & 0xff));
          out.append(hexify(i & 0xff));
          out.append(":  ");

          // hexbytes
          for(int j=0; j<16; j++, i++) {
              if(i < (offset + len)) {
                  int b = data[i] & 0xff;
                  out.append(hexify(b)).append(' ');
                  ascii[j] = (b>=32 && b<127) ? (char)b : '.';
              }
              else {
                  out.append("   ");
                  ascii[j] = ' ';
              }
          }

          //ASCII
          out.append(' ').append(ascii).append("\n");
      }

      return out.toString();
  }


  /** Hexify a byte array.<p>
    *
    * @param data  Byte array to convert to HexString
    *
    * @return HexString
    */
  public static String hexify(byte[] data) {
        if(data == null) return "null";

        StringBuffer out = new StringBuffer(256);
        int n = 0;

        for(int i=0; i<data.length; i++) {
          if(n>0) out.append(' ');

          out.append(hexChars[(data[i]>>4) & 0x0f]);
          out.append(hexChars[data[i] & 0x0f]);

          if(++n == 16) {
                out.append('\n');
                n = 0;
          }
        }

        return out.toString();
  }


  /** Hexify a byte value.<p>
    *
    * @param val
    *        Byte value to be displayed as a HexString.
    *
    * @return HexString
    */
  public static String hexify(int val) {
        return hexChars[((val & 0xff) & 0xf0)>>>4] + hexChars[val & 0x0f];
  }


  /** Hexify short value encoded in two bytes.<p>
    *
    * @param a
    *        High byte of short value to be hexified
    * @param b
    *        Low byte of short value to be hexified
    *
    * @return HexString
    */
  public static String hexifyShort(byte a, byte b) {
        return hexifyShort(a & 0xff, b & 0xff);
  }


  /** Hexify a short value.<p>
    *
    * @param val
    *        Short value to be displayed as a HexString.
    *
    * @return HexString
    */
  public static String hexifyShort(int val) {
        return hexChars[((val & 0xffff) & 0xf000)>>>12] +
          hexChars[((val & 0xfff) & 0xf00)>>>8] +
          hexChars[((val & 0xff) & 0xf0)>>>4] + hexChars[val & 0x0f];
  }


  /** Hexify short value encoded in two (int-encoded)bytes.<p>
    *
    * @param a
    *        High byte of short value to be hexified
    * @param b
    *        Low byte of short value to be hexified
    *
    * @return HexString
    */
  public static String hexifyShort(int a, int b) {
        return hexifyShort(((a & 0xff)<<8) + (b & 0xff));
  }


  /** Parse bytes encoded as Hexadecimals into a byte array.<p>
    *
    * @param byteString
    *        String containing HexBytes.
    *
    * @return byte array containing the parsed values of the given string.
    */
  public static byte[] parseHexString(String byteString) {
        byte[] result = new byte[byteString.length()/2];
        for (int i=0; i<byteString.length(); i+=2) {
          String toParse = byteString.substring(i, i+2);
          result[i/2] = (byte)Integer.parseInt(toParse, 16);
        }
        return result;
  }


  /** Parse string of Hexadecimals into a byte array suitable for
    * unsigned BigInteger computations. Reverse the order of the
    * parsed data on the fly (input data little endian).<p>
    *
    * @param byteString
    *        String containing HexBytes.
    *
    * @return byte array containing the parsed values of the given string.
    */
  public static byte[] parseLittleEndianHexString(String byteString) {
        byte[] result = new byte[byteString.length()/2+1];
        for (int i=0; i<byteString.length(); i+=2) {
          String toParse = byteString.substring(i, i+2);
          result[(byteString.length()-i)/2] =
        (byte)Integer.parseInt(toParse, 16);
        }
        result[0] = (byte)0; // just to make it a positive value
        return result;
  }
}
