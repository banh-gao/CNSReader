/*
 *  ---------
 * |.**> <**.|  CardContact Software & System Consulting
 * |*       *|  32429 Minden, Germany (www.cardcontact.de)
 * |*       *|  Copyright (c) 1999-2004. All rights reserved
 * |'**> <**'|  See file COPYING for details on licensing
 *  --------- 
 *
 * $Log: IsoCardState.java,v $
 * Revision 1.1  2005/09/19 19:22:30  asc
 * Added support for ISO file systems
 *
 * Revision 1.2  2004/04/05 06:35:25  asc
 * Version 0.1 and first release to ChipBE
 *
 *
 */

package de.cardcontact.opencard.service.isocard;

import opencard.opt.iso.fs.CardFileInfo;
import opencard.opt.iso.fs.CardFilePath;

/**
 * Object to hold information card related card service information
 * 
 * @author Andreas Schwier
 */
public class IsoCardState {
	private CardFilePath currentPath;
	private CardFileInfo currentFCI;
	private boolean isElementaryFile;
	private byte selectFCI;
	private boolean leInSelectEnabled = true;
	
	/**
	 * CTOR for IsoCardState object 
	 */
	public IsoCardState() {
		currentPath = null;
		currentFCI = null;
		selectFCI = IsoConstants.SO_RETURNFCP;
	}

	public CardFilePath getPath() {
		return currentPath;
	}
	
	public void setPath(CardFilePath newPath) {
		currentPath = new CardFilePath(newPath);
	}

	public CardFileInfo getFCI() {
		return currentFCI;
	}
	
	public void setFCI(CardFileInfo newFCI, boolean isEF) {
		currentFCI = newFCI;
		isElementaryFile = isEF;
	}
	
	public boolean elementaryFileSelected() {
		return isElementaryFile;
	}
	
	public void setSelectCommandResponseQualifier(byte p2) {
	    selectFCI = p2;
	}
	
	public byte getSelectCommandResponseQualifier() {
		return selectFCI;
	}
	
	public void setLeInSelectFlag(boolean flag) {
	    this.leInSelectEnabled = flag;
	}

	public boolean isLeInSelectEnabled() {
	    return this.leInSelectEnabled;
	}
}
