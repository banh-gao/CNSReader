package opencard.opt.security;

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


import java.security.Key;


/**
 * Encapsulates a DES key.
 *
 * @author  Michael Baentsch (mib@zurich.ibm.com)
 * @author  Roland Weber (rolweber@de.ibm.com)
 * @version $Id: DESKey.java,v 1.2 1999/10/20 13:15:00 damke Exp $
 */
public class DESKey implements Key
{
  /** The key encapsulated by this object. */
  private byte[] data = null;

  /**
   * Instantiates a <tt>DESKey</tt> from the given byte array.
   * A clone of the array is stored for later access.
   *
   * @param    data     byte array holding the key to encapsulate
   */
  public DESKey(byte[] data) {
    this.data = (byte[]) data.clone();
  }

  /** Conformance to the java.security interface */
  public String getAlgorithm() {
    return ("DES");
  }

  /** Conformance to the java.security interface */
  public String getFormat() {
    return null;
  }

  /** Conformance to the java.security interface */
  public byte[] getEncoded() {
    return null;
  }

  /**
   * Returns the key data.
   * The key data returned here must not be changed.
   *
   * @return byte array holding the encapsulated DES key
   */
  public byte[] getBytes()
  {
    return data;
  }

  /**
   * Returns the key data.
   * The key data returned here must not be changed.
   *
   * @return byte array holding the encapsulated DES key
   */
  public byte[] body() {
    return data;
  }

}
