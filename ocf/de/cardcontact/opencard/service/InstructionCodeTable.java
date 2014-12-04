/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2010 CardContact Software & System Consulting
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

package de.cardcontact.opencard.service;

import de.cardcontact.opencard.service.isocard.IsoConstants;

/**
 * Decoder to visualize instruction codes
 * 
 * @author Andreas Schwier
 *
 */
public class InstructionCodeTable {



	/**
	 * Parse the INS byte and decode into a ISO 7816 command name
	 * 
	 * @param header Command APDU header
	 * 
	 * @return ISO 7816-4 command name
	 */
	public static String instructionNameFromHeader(byte[] header) {
		String s = "UNKNOWN_INS";

		if (header[0] == (byte)0xFF) {		// Commands from PC/SC 2.01 Part 3 Synchronous storage cards
			switch(header[1]) {
			case IsoConstants.INS_GET_DATA:
				s = "GET DATA"; break;
			case IsoConstants.INS_LOAD_KEYS:
				s = "LOAD KEYS"; break;
			case IsoConstants.INS_INTAUTH:
				s = "AUTHENTICATE"; break;
			case IsoConstants.INS_GENERAL_AUTH1:
				s = "GENERAL AUTHENTICATE"; break;
			case IsoConstants.INS_VERIFY:
				s = "VERIFY"; break;
			case IsoConstants.INS_READ_BINARY:
				s = "READ BINARY"; break;
			case IsoConstants.INS_UPDATE_BINARY:
				s = "UPDATE BINARY"; break;
			}
		} else if ((header[0] & 0x80) == 0x80) {
			switch(header[1]) {
			case IsoConstants.INS_INIT_UPDATE:
				s = "INITIALIZE UPDATE"; break;
			case IsoConstants.INS_EXTAUTHENTICATE:
				s = "EXTERNAL AUTHENTICATE"; break;
			case IsoConstants.INS_PUT_KEY:
				s = "PUT KEY"; break;
			case IsoConstants.INS_STORE_DATA:
				s = "STORE DATA"; break;
			case IsoConstants.INS_DELETE:
				s = "DELETE"; break;
			case IsoConstants.INS_INSTALL:
				s = "INSTALL"; break;
			case IsoConstants.INS_LOAD:
				s = "LOAD"; break;
			case IsoConstants.INS_SET_STATUS:
				s = "SET STATUS"; break;
			case IsoConstants.INS_GET_STATUS:
				s = "GET STATUS"; break;
			case IsoConstants.INS_ENUM_OBJECTS:
				s = "ENUMERATE OBJECTS"; break;
			case IsoConstants.INS_ENCIPHER:
				s = "ENCIPHER"; break;
			case IsoConstants.INS_DECIPHER:
				s = "DECIPHER"; break;
			case IsoConstants.INS_SIGN:
				s = "SIGN"; break;
			case IsoConstants.INS_VERIFY_MAC:
				s = "VERIFY MAC"; break;
			}
		} else {
			switch(header[1] & ~1) {		// Unmask bit 0 for odd/even instruction bytes
			case IsoConstants.INS_DEACTIVATE_FILE:
				s = "DEACTIVATE FILE"; break;
			case IsoConstants.INS_DEACTIVATE_RECORD:
				s = "DEACTIVATE RECORD"; break;
			case IsoConstants.INS_ACTIVATE_RECORD:
				s = "ACTIVATE RECORD"; break;
			case IsoConstants.INS_ERASE_RECORD:
				s = "ERASE RECORD"; break;
			case IsoConstants.INS_ERASE_BINARY1:
				s = "ERASE BINARY 1"; break;
			case IsoConstants.INS_ERASE_BINARY2:
				s = "ERASE BINARY 1"; break;
			case IsoConstants.INS_PERFORM_SCQL_OP:
				s = "PERFORM SCQL OPERATION"; break;
			case IsoConstants.INS_PERFORM_TRANS_OP:
				s = "PERFORM TRANSACTION OPERATION"; break;
			case IsoConstants.INS_PERFORM_USER_OP:
				s = "PERFORM USER OPERATION"; break;
			case IsoConstants.INS_VERIFY:
				s = "VERIFY"; break;
			case IsoConstants.INS_MANAGE_SE:
				s = "MANAGE SECURITY ENVIRONMENT"; break;
			case IsoConstants.INS_CHANGE_CHV:
				s = "CHANGE REFERENCE DATA"; break;
			case IsoConstants.INS_DISABLE_CHV:
				s = "DISABLE VERIFICATION REQUIREMENT"; break;
			case IsoConstants.INS_ENABLE_CHV:
				s = "ENABLE VERIFICATION REQUIREMENT"; break;
			case IsoConstants.INS_PSO:
				switch(header[2]) {
				case IsoConstants.P1_PSO_HASH:
					s = "PSO: HASH"; break;
				case IsoConstants.P1_PSO_CDS:
					s = "PSO: COMPUTE DIGITAL SIGNATURE"; break;
					//			case IsoConstants.P1_PSO_CDS:
					//				s = "PSO: COMPUTE DIGITAL SIGNATURE"; break;
				case 0:
					switch(header[3]) {
					case IsoConstants.SM_VERIFY_CERT1:
					case IsoConstants.SM_VERIFY_CERT2:
						s = "PSO: VERIFY CERTIFICATE"; break;
					default: 
						s = "PERFORM SECURITY OPERATION"; break;
					}
					break;
				default: 
					s = "PERFORM SECURITY OPERATION"; break;
				}
				break;
			case IsoConstants.INS_UNBLOCK_CHV:
				s = "RESET RETRY COUNTER"; break;
			case IsoConstants.INS_ACTIVATE_FILE:
				s = "ACTIVATE FILE"; break;
			case IsoConstants.INS_GENERATE_KEYPAIR:
				s = "GENERATE ASYMMETRIC KEY PAIR"; break;
			case IsoConstants.INS_MANAGE_CHANNEL:
				s = "MANAGE CHANNEL"; break;
			case IsoConstants.INS_EXTAUTHENTICATE:
				s = "EXTERNAL AUTHENTICATE"; break;
			case IsoConstants.INS_GET_CHALLENGE:
				s = "GET CHALLENGE"; break;
			case IsoConstants.INS_GENERAL_AUTH1:
				s = "GENERAL AUTHENTICATE 1"; break;
			case IsoConstants.INS_GENERAL_AUTH2:
				s = "GENERAL AUTHENTICATE 2"; break;
			case IsoConstants.INS_INTAUTH:
				s = "INTERNAL AUTHENTICATE"; break;
			case IsoConstants.INS_SEARCH_BINARY1:
				s = "SEARCH BINARY 1"; break;
			case IsoConstants.INS_SEARCH_BINARY2:
				s = "SEARCH BINARY 2"; break;
			case IsoConstants.INS_SEARCH_RECORD:
				s = "SEARCH RECORD"; break;
			case IsoConstants.INS_SELECT_FILE:
				s = "SELECT"; break;
			case IsoConstants.INS_GENERATE_AC:
				s = "GENERATE AC"; break;
			case IsoConstants.INS_READ_BINARY:
				s = "READ BINARY"; break;
			case IsoConstants.INS_READ_RECORD:
				s = "READ RECORD"; break;
			case IsoConstants.INS_GET_RESPONSE:
				s = "GET RESPONSE"; break;
			case IsoConstants.INS_GET_DATA:
				s = "GET DATA"; break;
			case IsoConstants.INS_WRITE_BINARY:
				s = "WRITE BINARY"; break;
			case IsoConstants.INS_WRITE_RECORD:
				s = "WRITE RECORD"; break;
			case IsoConstants.INS_UPDATE_BINARY:
				s = "UPDATE BINARY"; break;
			case IsoConstants.INS_PUT_DATA:
				s = "PUT DATA"; break;
			case IsoConstants.INS_UPDATE_RECORD:
				s = "UPDATE RECORD"; break;
			case IsoConstants.INS_CREATE_FILE:
				s = "CREATE FILE"; break;
			case IsoConstants.INS_APPEND_RECORD:
				s = "APPEND RECORD"; break;
			case IsoConstants.INS_DELETE_FILE:
				s = "DELETE FILE"; break;
			case IsoConstants.INS_TERMINATE_DF:
				s = "TERMINATE DF"; break;
			case IsoConstants.INS_TERMINATE_EF:
				s = "TERMINATE EF"; break;
			case IsoConstants.INS_LOAD_APPLICATION:
				s = "LOAD APPLICATION"; break;
			case IsoConstants.INS_TERMINATE_CARD:
				s = "TERMINATE CARD USAGE"; break;
			}
		}
		return s;
	}
}