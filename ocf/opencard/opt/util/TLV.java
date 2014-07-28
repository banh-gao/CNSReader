package opencard.opt.util;

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


import java.util.Hashtable;

import opencard.core.util.HexString;


/** This class represents a TLV (Tag Length Value) structure. There are methods
 * for creating trees consisting of TLV objects from ASN.1 BER encoded byte
 * sequences and for creating byte sequences from TLV object trees.
 * All manipulations are done on the tree structure.
 * @author   Thomas Schaeck
 *
 * @version  $Id: TLV.java,v 1.2 2005/09/19 10:21:22 asc Exp $
 *
 * @see opencard.opt.util.Tag
 */

public class TLV {
  private Tag     tag;                 // Tag of this TLV
  private int     length;              // Length of this TLV's value
  private byte[]  value;               // Value of this TLV
  private TLV     parent;              // Parent of this TLV
  private TLV     sibling;             // Next sibling of this TLV
  private TLV     child;               // First child of this TLV
  private TLV     lastChild;           // Last child of this TLV

  /** Create an empty TLV.
   */
  public TLV() {
	tag = new Tag(0, (byte) 0, false);  // This is a primitive TLV
	length      = 0;
	value       = null;
	parent      = null;           // The new TLV has no parent,
	sibling     = null;           // no sibling,
	child       = null;           // no child
	lastChild   = null;           // and no last child.
  }  
  /** Create a <tt>TLV</tt> object from an ASN.1 BER encoded byte array.<p>
   *
   * @param binary
   *        A byte array containing the binary representation of a TLV
   *        structure, encoded conforming to the ASN.1 Basic Encoding
   *        Rules defined in ISO 8825.
   */
  public TLV(byte[] binary) {
	int[] offset = {0};
	tag = new Tag(0, (byte) 0, false); // This is a primitive TLV
	length      = 0;
	value       = null;
	parent      = null;                // The new TLV has no parent,
	sibling     = null;                // no sibling,
	child       = null;                // no child
	lastChild   = null;                // and no last child.
	fromBinary(binary, offset, this, null);
  }  
  /** Create a <tt>TLV</tt> object from an ASN.1 BER encoded byte array.<p>
  *
  * @param binary
  *        A byte array containing the binary representation of a TLV
  *        structure, encoded conforming to the ASN.1 Basic Encoding
  *        Rules defined in ISO 8825.
  */
 public TLV(byte[] binary, int[] offset) {
    tag = new Tag(0, (byte) 0, false); // This is a primitive TLV
    length      = 0;
    value       = null;
    parent      = null;                // The new TLV has no parent,
    sibling     = null;                // no sibling,
    child       = null;                // no child
    lastChild   = null;                // and no last child.
    fromBinary(binary, offset, this, null);
 }  
  /** Create a TLV object from the given <tt>Tag</tt> object and
   * data.<p>
   *
   * If the given <tt>Tag</tt> object has the constructed bit set,
   * the result will be a TLV tree, otherwise it's just a
   * primitive TLV that contains the data given in value field.<p>
   *
   * @param tag
   *        An instance of class <tt>Tag</tt> representing the tag
   *        field of the TLV to be created.
   * @param value
   *        An array of bytes representing the Value field of the TLV
   *        to be created.
   */
  public TLV(Tag tag, byte[] value)
  {
	int[] offset = {0};
	TLV newTLV = new TLV();

	this.tag = new Tag(tag);
	if (this.tag.isConstructed()) {
	  while (offset[0] < value.length)
		fromBinary(value, offset, newTLV, this);
	}
	else {
	  if (value != null)
		this.length    = value.length;
	  else
		this.length    = 0;
	  this.value       = value;
	  this.child       = null;         // no child and
	  this.lastChild   = null;         // no last child.
	}
	this.parent      = null;           // The new TLV has no parent,
	this.sibling     = null;           // no sibling,
  }    
  /** Create a primitive TLV object from a given tag and positive integer.<p>
   *
   * @param tag
   *        An instance of class <tt>Tag</tt> representing the tag
   *        field of the TLV to be created.
   * @param number
   *        An integer representing the Value field of the TLV
   *        to be created.
   */
  public TLV(Tag tag, int number) {
	int i = 0;

	this.tag = new Tag(tag);

	// Find out how many bytes we need.
	if      (number < 0x100)     value = new byte[1];
	else if (number < 0x10000)   value = new byte[2];
	else if (number < 0x1000000) value = new byte[3];
	else                         value = new byte[4];

	// Do conversion
	for (i = value.length-1; i >= 0; i--) {
	  value[i] = (byte) (number % 0x100);
	  number /= 0x100;
	}

	this.length      = value.length;
	this.child       = null;         // no child and
	this.lastChild   = null;         // no last child.
	this.parent      = null;         // The new TLV has no parent,
	this.sibling     = null;         // no sibling,
  }    
  /** Create a constructed TLV object from the given <tt>Tag</tt> object
   * and <tt>TLV</tt> object to be contained.
   *
   * @param tag
   *        An instance of class <tt>Tag</tt> representing the tag
   *        field of the TLV to be created.
   * @param tlv
   *        An instance of class <tt>TLV</tt> representing the Value
   *        field of the TLV to be created.
   */
  public TLV(Tag tag, TLV tlv) {
	this.tag         = new Tag(tag);
	this.tag.setConstructed(true);     // This is a constructed TLV
	this.value       = null;           // therefore it has no direct value
	this.parent      = null;           // The new TLV has no parent.
	this.sibling     = null;           // The new TLV has no sibling.
	this.child       = tlv;            // TLV becomes child
	this.lastChild   = tlv;            // and last child (even if it's null).

	if (tlv != null)
	  this.length=tlv.tag.size()+tlv.lenBytes()+tlv.length;
	else
	  this.length=0;            // empty TLV has length 0.
  }    
  /** Add the given <tt>TLV</tt> object to this <tt>TLV</tt> instance
   * (only if constructed).<p>
   *
   * @param tlv
   *        The <tt>TLV</tt> object to be concatenated to this
   *        <tt>TLV</tt> instance.
   */
  public TLV add(TLV tlv) {
	TLV iterTLV;
	int originalReprLength=0;
	int deltaReprLength= 0;

	if (tag.isConstructed() == true) {
	  tlv.parent        = this;        // make this the parent of added tlv
	  tlv.sibling       = null;        // last child has no sibling
	  if (lastChild != null)           // if there already has been a child,
		lastChild.sibling = tlv;       // it gets tlv as a new sibling
	  lastChild         = tlv;         // tlv becomes last child

	  // update length of this TLV and all it's ancestors
	  iterTLV           = this;
	  while (iterTLV != null) {
		originalReprLength = iterTLV.lenBytes();
		iterTLV.length += tlv.length + tlv.tag.size() + tlv.lenBytes() + deltaReprLength;
		deltaReprLength += iterTLV.lenBytes() - originalReprLength;
		iterTLV = iterTLV.parent;
	  }
	  return this;
	}
	else {
	  return null;
	}
  }  
  /** Search for a given tag value and return the first TLV found.<p>
   *
   * @param tag
   *        The <tt>Tag</tt> object representing the tag to be searched for,
   *        <tt>null</tt> for any tag.
   * @param cursor
   *        A reference to a <tt>TLV</tt> object where the search should start;
   *        if <tt>null</tt>, the search is started with the child of this
   *        <tt>TLV</tt> instance.
   * @return The first <tt>TLV</tt> object found, which has the given tag value;
   *         <tt>null</tt> if no match is found.
   */
  public TLV findTag(Tag tag, TLV cursor) {
	TLV iterTLV;

	if (cursor == null)
	  iterTLV = child;                 // start with the first child
	else
	  iterTLV = cursor.sibling;        // start with cursor's successor

	if (tag == null)
	  return iterTLV;                  // null is wildcard

	while (iterTLV != null) {
	  if (iterTLV.tag.equals(tag))
		return iterTLV;
	  iterTLV = iterTLV.sibling;
	}
	return null;
  }  
  /** Read a <tt>TLV</tt> object from a binary representation.<p>
   *
   * @param binary
   *        A byte array containing the binary representation of a TLV
   *        structure, encoded conforming to the ASN.1 Basic Encoding
   *        Rules defined in ISO 8825.
   *
   * @param offset
   *        An integer value giving the offset, where the binary
   *        representation starts.
   * @param tlv
   *        The <tt>TLV</tt> object to be read from the binary representation.
   * @param parent
   *        The <tt>TLV</tt> object representing the parent of the object to be read.
   */
  public static void fromBinary(byte[] binary, int[] offset, TLV tlv, TLV parent) {
	int i= 0;
	int oldOffset=offset[0];
	TLV iterTLV=null;

	// Get the Tag from binary representation.
	tlv.tag.fromBinary(binary, offset);
	// Get the length from binary representation.
	tlv.length = 0;


	if ((binary[offset[0]] & (byte) 0x80) == (byte) 0x00)
	{
	  tlv.length+=(int) binary[offset[0]];
	}
	else {
	  int numBytes = (binary[offset[0]] & (byte) 0x7F);
	  int j=0;
	  while (numBytes > 0) {
		offset[0]++;
		j=binary[offset[0]];
		tlv.length += ( j<0 ? j+=256 : j);

		if (numBytes > 1) tlv.length *= 256;
		numBytes--;
	  }
	}
	offset[0]++;

	if (tlv.tag.isConstructed()) {
	  tlv.value   = null;
      if (tlv.length > 0) {         // Fixed ASC: Allow empty constructed objects
        tlv.child   = new TLV();
	    fromBinary(binary, offset, tlv.child, tlv);

	    iterTLV = tlv.child;
	    while (offset[0] <= oldOffset + tlv.length) {
	  	  iterTLV.sibling = new TLV();
		  fromBinary(binary, offset, iterTLV.sibling, tlv);
		  iterTLV = iterTLV.sibling;
	    }
	    tlv.lastChild = iterTLV;
      } else {
        tlv.child = null;
      }
	}
	else {
	  tlv.child   = null;
	  tlv.sibling = null;            // The new TLV has no sibling.
	  tlv.value   = new byte[tlv.length];
	  System.arraycopy(binary, offset[0], tlv.value, 0, tlv.length);
	  offset[0] += tlv.length;
	}
	tlv.parent = parent;
  }        
  /**
   * Return the number of bytes required for the coding of the length of
   * this TLV as described in the ASN.1 Basic Encoding Rules.<p>
   *
   * @return An integer value giving the number of bytes.
   */
  private int lenBytes() {
	if      (length < 0x80)        return 1;
	else if (length < 0x100)       return 2;
	else if (length < 0x10000)     return 3;
	else if (length < 0x1000000)   return 4;
	else                           return 5;
  }  
  /** Return the number of bytes required for coding the passed integer
   * value as described in the ASN.1 Basic Encoding Rules.<p>
   *
   * @param length
   *        An integer value.
   * @return An integer value giving the number of bytes.
   */
  public static int lenBytes(int length) {
	if      (length < 0x80)        return 1;
	else if (length < 0x100)       return 2;
	else if (length < 0x10000)     return 3;
	else if (length < 0x1000000)   return 4;
	else                           return 5;
  }  
  /** Get the length of this TLV's value field in bytes.<p>
  *
  * @return An integer giving the length.
  */
  public int length() {
	return length;
  }  
  /** BER-code the length of this TLV.<p>
   *
   * @param length
   *        The length to be encoded
   * @return The BER-coded length field
   */
  public static byte[] lengthToBinary(int length) {
	byte[] binary = new byte[lenBytes(length)];
	if (length < 0x80) {
	  binary[0] = (byte) length;
	}
	else if (length < 0x100) {
	  binary[0] = (byte) 0x81;
	  binary[1] = (byte) length;
	}
	else if (length < 0x10000) {
	  binary[0] = (byte) 0x82;
	  binary[1] = (byte) (length / 0x100);
	  binary[2] = (byte) (length % 0x100);
	}
	else if (length < 0x1000000) {
	  binary[0] = (byte) 0x83;
	  binary[1] = (byte) (length / 0x10000);
	  binary[2] = (byte) (length / 0x100);
	  binary[3] = (byte) (length % 0x100);
	}
	return binary;
  }  
  /** Set the value field of this TLV from the byte array.<p>
  *
  * @param newValue
  *        The byte array for the value field.
  */
  public void setValue(byte [] newValue) {
	int originalReprLength = 0;
	int deltaReprLength = 0;

	originalReprLength = this.lenBytes();

	int oldLength = length;
	value = newValue;
	if (newValue != null)
	  length = value.length;
	else
	  length = 0;

	deltaReprLength = this.lenBytes() - originalReprLength;

	// update length of this TLV and all it's ancestors
	TLV iterTLV = this.parent;
	while (iterTLV != null) {
	  originalReprLength = iterTLV.lenBytes();
	  iterTLV.length += (length - oldLength) + deltaReprLength;;
	  deltaReprLength += iterTLV.lenBytes() - originalReprLength;
	  iterTLV = iterTLV.parent;
	}
  }  
  /** Get the tag of this TLV.<p>
  *
  * @return The <tt>Tag</tt> object of this <tt>TLV</tt> object.
  */
  public Tag tag() {
	return tag;
  }  
  /** BER-code this TLV.<p>
   *
   * @return A byte array containing the BER-coded representation of this
   *         <tt>TLV</tt> instance.
   */
  public byte[] toBinary() {
	int[] offset = {0};
	int totalLength = tag.size() + lenBytes() + length;
	byte binary[] = new byte[totalLength];
	this.toBinaryHelper(binary, offset, totalLength);
	return binary;
  }  
  /** BER-code this TLV's value field.<p>
   *
   * @return A byte array containing the BER-coded binary representation
   *          of the value field of this <tt>TLV</tt> instance.
   */
  public byte[] toBinaryContent() {
	int[] offset = {0};
	int totalLength = length;
	byte binary[] = new byte[totalLength];
	this.toBinaryHelperContent(binary, offset, totalLength);
	return binary;
  }  
  /** Convert this TLV to it's BER-coded representation.<p>
   *
   * @param binary
   *        The byte array to which the BER-coded representation shall
   *        be written.
   * @param offset
   *        An integer giving the offset into the byte array, from
   *        where the binary representation shall start.
   * @param max
   *        An integer giving the index of the last valid byte.
   */
  private void toBinaryHelper(byte[] binary, int[] offset, int max) {
	int i = 0;

	tag.toBinary(binary, offset);
	toBinaryLength(binary, offset);
	if (child != null)
	  child.toBinaryHelper(binary, offset, max);
	else if (value != null) {
	  System.arraycopy(value, 0, binary, offset[0], value.length);
	  offset[0] += value.length;
	}

	// We must check if offset is less than max, because when the TLV that is converted
	// to binary has siblings, we would run into trouble (array out of bounds)
	if (sibling != null && offset[0] < max) {
	  sibling.toBinaryHelper(binary, offset, max);
	}
  }  
  /** Convert this TLV's value field to it's BER-coded representation.<p>
   *
   * @param binary
   *        The byte array to which the BER-coded representation of this
   *        <tt>TLV</tt> instance shall be written.
   * @param offset
   *        An integer giving the offset into the byte array, from where the
   *        BER-coded representation shall start.
   * @param max
   *        An integer giving the index of the last valid byte.
   */
  private void toBinaryHelperContent(byte[] binary, int[] offset, int max) {
	int i = 0;

	if (child != null)
	  child.toBinaryHelper(binary, offset, max);
	else {
	  if (value != null) {
		System.arraycopy(value, 0, binary, offset[0], value.length);
		offset[0] += value.length;
	  }
	}
  }  
  /** Convert the length of this TLV to it's binary representation
  * according to the ASN.1 Basic Encoding Rules defined in ISO 8825.<p>
  *
  * @param binary
  *        The byte array to which the BER-coded length field shall be added.
  * @param offset
  *        The offset, where the BER-coded length field shall be added.
  */
  private void toBinaryLength(byte[] binary, int[] offset) {
	if (length < 0x80) {
	  binary[offset[0]] = (byte) length;
	}
	else if (length < 0x100) {
	  binary[offset[0]] = (byte) 0x81;
	  offset[0]++;
	  binary[offset[0]] = (byte) length;
	}
	else if (length < 0x10000) {
	  binary[offset[0]] = (byte) 0x82;
	  offset[0]++;
	  binary[offset[0]] = (byte) (length / 0x100);
	  offset[0]++;
	  binary[offset[0]] = (byte) (length % 0x100);
	}
	else if (length < 0x1000000) {
	  binary[offset[0]] = (byte) 0x83;
	  offset[0]++;
	  binary[offset[0]] = (byte) (length / 0x10000);
	  offset[0]++;
	  binary[offset[0]] = (byte) (length / 0x100);
	  offset[0]++;
	  binary[offset[0]] = (byte) (length % 0x100);
	}
	offset[0]++;
  }  
  /** Convert a TLV to a string.<p>
   * @return A <tt>String</tt> object representing this <tt>TLV</tt> object.
   */
  public String toString() {
	return toString(null, 0);
  }  
  /** Convert a TLV to a string.<p>
   *
   * @param ht
   *        A <tt>Hashtable</tt> object mapping <tt>Tag</tt> objects to
   *        <String> objects.
   * @param level
   *        An integer value giving the indention leve to be used.
   * @return A <tt>String</tt> object representing this <tt>TLV</tt> object.
   */
  public String toString(Hashtable ht, int level) {
	String s = new String("");
	int i = 0;
	for (i=0; i<level; i++)
	  s = s + " ";

	if (ht == null)
	  s = s + "["+tag+" "+length+"] ";
	else
	  s = s + ht.get(tag) + " ";

	if (tag.isConstructed()) {
	  s = s + "\n";
	  for (i=0; i<level; i++)
		s = s + " ";
	}
	s = s + "( ";

	if (tag.isConstructed()) {
	  s = s + "\n";
	  s = s + child.toString(ht, level+2);
	  for (i=0; i<level; i++)  s = s + " ";
	  s = s + ")\n";
	}
	else {
	  boolean fPrintable = true;
	  if (value != null) {
		for (i = 0; i < value.length; i++)
		  if (value[i] < 32) fPrintable = false;
		if (fPrintable)
		  s = s + "\""+new String(value)+"\"";
		else {
		  s = s + "'";
		  for (i = 0; i < value.length; i++)
			s = s + HexString.hexify(value[i]);
		  s = s + "'";
		}
	  }
	  s = s + " )\n";
	}

	if (sibling != null)
	  s = s + sibling.toString(ht, level);
	return s;
  }  
  /** Get the value field of this TLV as a byte array.<p>
  *
  * @return A byte array representing the value field of this
  *         <tt>TLV</tt> instance; <tt>null</tt> if the TLV is constructed.
  */
  public byte[] valueAsByteArray() {
	return value;
  }  
  /** Get the value of this TLV as a positive integer number.<p>
   *
   * @return An integer representing the value (unsigned int) of this
   *         <tt>TLV</tt> instance's value field.
   */
  public int valueAsNumber() {
	int i=0;
	int j=0;
	int number = 0;

	for (i = 0; i < value.length; i++) {
	  j=value[i];
	  number = number * 256 + ( j<0 ? j+=256 : j);
	}
	return number;
  }  
} // class TLV