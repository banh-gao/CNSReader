/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2013 CardContact Software & System Consulting
 * |'##> <##'|  Andreas Schwier, 32429 Minden, Germany (www.cardcontact.de)
 *  --------- 
 *
 *  This file is part of OpenSCDP.
 *
 *  OpenSCDP is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2 as
 *  published by the Free Software Foundation.
 *
 *  OpenSCDP is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSCDP; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.certgate.android;


public class SmartCardJni {
	static {
		System.loadLibrary("sdx");
		System.loadLibrary("sdx_jni");
	}

	public static native int apdu(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2);

	public static native int close();

	public static native int[] enumerateCards();

	public static native SmartCardVersion getLibraryVersion();

	public static native String getReturncodeText(int paramInt);

	public static native int open();

	public static native int waitForStatusChange(int paramInt);
}
