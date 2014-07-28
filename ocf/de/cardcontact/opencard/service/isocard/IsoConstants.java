/*
 *  ---------
 * |.**> <**.|  CardContact Software & System Consulting
 * |*       *|  32429 Minden, Germany (www.cardcontact.de)
 * |*       *|  Copyright (c) 1999-2004. All rights reserved
 * |'**> <**'|  See file COPYING for details on licensing
 *  --------- 
 *
 * $Log: IsoConstants.java,v $
 * Revision 1.1  2005/09/19 19:22:30  asc
 * Added support for ISO file systems
 *
 * Revision 1.5  2005/09/19 10:20:31  asc
 * Added whitelist for contactless cards
 *
 * Revision 1.4  2005/02/11 13:25:19  asc
 * Added support for Starcos signature operations
 *
 * Revision 1.3  2004/05/17 15:40:05  asc
 * Added SV-Signature viewer and associated functions
 *
 * Revision 1.2  2004/04/05 06:35:25  asc
 * Version 0.1 and first release to ChipBE
 *
 *
 */

package de.cardcontact.opencard.service.isocard;

/**
 * ISO 7816 defined constants
 * 
 * @author asc
 *
 */

public interface IsoConstants {
	public final static byte CLA_ISO              = (byte)0x00;		/* ISO command class                */
	public final static byte CLA_ETSI             = (byte)0xA4;     /* GSM command class                */
	public final static byte CLA_ASC              = (byte)0xA5;     /* Application specific commands    */
	public final static byte CLA_EMV              = (byte)0x80;     /* EMV proprietary                  */
	public final static byte CLA_HSM			  = (byte)0x80;     /* SmartCard HSM                    */
	public final static byte CLA_PRO              = (byte)0xE0;     /* SVP proprietary                  */
	public final static byte CLA_CHAIN			  = (byte)0x10;		/* Chaining indicator				*/
	
	public final static byte SC_FID               = (byte)0x00;     /* SELECT FID                       */
	public final static byte SC_MF                = (byte)0x00;     /* SELECT MF                        */
	public final static byte SC_DF                = (byte)0x01;     /* SELECT DF                        */
	public final static byte SC_EF                = (byte)0x02;     /* SELECT EF                        */
	public final static byte SC_PARENT            = (byte)0x03;     /* SELECT Parent Directory          */
	public final static byte SC_AID               = (byte)0x04;     /* SELECT AID                       */
	public final static byte SO_RETURNFCI         = (byte)0x00;     /* Return File Control Information  */
	public final static byte SO_RETURNFCP         = (byte)0x04;     /* Return File Control Parameter    */
	public final static byte SO_NONE              = (byte)0x0C;     /* Return nothing in SELECT         */

	public final static byte RC_FIRST             = (byte)0x00;     /* READ first record                */
	public final static byte RC_LAST              = (byte)0x01;     /* READ last record                 */
	public final static byte RC_NEXT              = (byte)0x02;     /* READ next record                 */
	public final static byte RC_PREVIOUS          = (byte)0x03;     /* READ previous record             */
	public final static byte RC_ABSOLUTE          = (byte)0x04;     /* READ record indicated in P1      */

	public final static byte INS_DEACTIVATE_FILE  =	(byte)0x04;
	public final static byte INS_DEACTIVATE_RECORD=	(byte)0x06;
	public final static byte INS_ACTIVATE_RECORD  =	(byte)0x08;
	public final static byte INS_ERASE_RECORD     = (byte)0x0C;
	public final static byte INS_ERASE_BINARY1    = (byte)0x0E;
	public final static byte INS_ERASE_BINARY2    = (byte)0x0F;
	public final static byte INS_PERFORM_SCQL_OP  = (byte)0x10;
	public final static byte INS_PERFORM_TRANS_OP = (byte)0x12;
	public final static byte INS_PERFORM_USER_OP  = (byte)0x14;
	public final static byte INS_VERIFY           = (byte)0x20;
	public final static byte INS_MANAGE_SE        = (byte)0x22;
	public final static byte INS_CHANGE_CHV       = (byte)0x24;
	public final static byte INS_DISABLE_CHV      = (byte)0x26;
	public final static byte INS_ENABLE_CHV       = (byte)0x28;
	public final static byte INS_PSO              = (byte)0x2A;
	public final static byte INS_UNBLOCK_CHV      = (byte)0x2C;
	public final static byte INS_ACTIVATE_FILE    = (byte)0x44;
	public final static byte INS_GENERATE_KEYPAIR = (byte)0x46;
	public final static byte INS_MANAGE_CHANNEL   = (byte)0x70;
	public final static byte INS_EXTAUTHENTICATE  = (byte)0x82;
	public final static byte INS_LOAD_KEYS        = (byte)0x82;
	public final static byte INS_GET_CHALLENGE    = (byte)0x84;
	public final static byte INS_GENERAL_AUTH1    = (byte)0x86;
	public final static byte INS_GENERAL_AUTH2    = (byte)0x87;
	public final static byte INS_INTAUTH          = (byte)0x88;
	public final static byte INS_SEARCH_BINARY1   = (byte)0xA0;
	public final static byte INS_SEARCH_BINARY2   = (byte)0xA1;
	public final static byte INS_SEARCH_RECORD    = (byte)0xA2;
	public final static byte INS_SELECT_FILE      = (byte)0xA4;
	public final static byte INS_GENERATE_AC      = (byte)0xAE;
	public final static byte INS_READ_BINARY      = (byte)0xB0;
	public final static byte INS_READ_BINARY_ODD  = (byte)0xB1;
	public final static byte INS_READ_RECORD      = (byte)0xB2;
	public final static byte INS_GET_RESPONSE     = (byte)0xC0;
	public final static byte INS_GET_DATA         = (byte)0xCA;
	public final static byte INS_WRITE_BINARY     = (byte)0xD0;
	public final static byte INS_WRITE_RECORD     = (byte)0xD2;
	public final static byte INS_UPDATE_BINARY    = (byte)0xD6;
	public final static byte INS_UPDATE_BINARY_ODD= (byte)0xD7;
	public final static byte INS_PUT_DATA         = (byte)0xDA;
	public final static byte INS_UPDATE_RECORD    = (byte)0xDC;
	public final static byte INS_CREATE_FILE      = (byte)0xE0;
	public final static byte INS_APPEND_RECORD    = (byte)0xE2;
	public final static byte INS_DELETE_FILE      = (byte)0xE4;
	public final static byte INS_TERMINATE_DF     = (byte)0xE6;
	public final static byte INS_TERMINATE_EF     = (byte)0xE8;
	public final static byte INS_LOAD_APPLICATION = (byte)0xEA;
	public final static byte INS_TERMINATE_CARD   = (byte)0xFE;

	// Global Platform Card Specification (CLA=80)
	public final static byte INS_INIT_UPDATE      = (byte)0x50;
	public final static byte INS_PUT_KEY          = (byte)0xD8;
	public final static byte INS_STORE_DATA       = (byte)0xE2;
	public final static byte INS_DELETE           = (byte)0xE4;
	public final static byte INS_INSTALL          = (byte)0xE6;
	public final static byte INS_LOAD             = (byte)0xE8;
	public final static byte INS_SET_STATUS       = (byte)0xF0;
	public final static byte INS_GET_STATUS       = (byte)0xF2;

	// SmartCard HSM (CLA=80)
	public final static byte INS_ENUM_OBJECTS     = (byte)0x58;
	public final static byte INS_ENCIPHER         = (byte)0x60;
	public final static byte INS_DECIPHER         = (byte)0x62;
	public final static byte INS_SIGN             = (byte)0x68;
	public final static byte INS_VERIFY_MAC       = (byte)0x6A;
	public final static byte INS_INITIALIZE       = (byte)0x50;
	
	
	// Codes für PSO command
	public final static byte P1_PSO_HASH		  = (byte)0x90;
	public final static byte P2_PSO_HASH_PLAIN	  = (byte)0x80;
	public final static byte P2_PSO_HASH_TLV	  = (byte)0xA0;
	
	public final static byte P1_PSO_CDS			  = (byte)0x9E;
	public final static byte P2_PSO_CDS_DTBS      = (byte)0x9A;

	// ISO Usage Qualifier
	public final static byte UQ_VER_ENC_EXTAUT	  = (byte)0x80;		// Verification, Encryption and External Authentication
	public final static byte UQ_COM_DEC_INTAUT	  = (byte)0x40;		// Computation, Decryption and Internalt Authentication
	public final static byte UQ_SM_RESPONSE		  = (byte)0x20;		// Secure Messaging Response
	public final static byte UQ_SM_COMMAND		  = (byte)0x20;		// Secure Messaging Command
	public final static byte UQ_USER_AUT		  = (byte)0x10;		// User Authentication

	// Codes for MANAGE SE command
	public final static byte P1_MSE_SET			  = (byte)0x01;
	public final static byte P1_MSE_STORE		  = (byte)0xF2;
	public final static byte P1_MSE_RESTORE		  = (byte)0xF3;
	public final static byte P1_MSE_ERASE		  = (byte)0xF4;

	// Codes for Control Reference Templates
	public final static byte CRT_AT 			  = (byte)0xA4;		// Authentication
	public final static byte CRT_HT 			  = (byte)0xAA;		// Hash
	public final static byte CRT_CCT			  = (byte)0xB4;		// Cryptographic Checksum
	public final static byte CRT_DST			  = (byte)0xB6;		// Digital Signature
	public final static byte CRT_CT 			  = (byte)0xB8;		// Confidentiality

	// Codes for SM templates
	public final static byte SM_PLAIN			  = (byte)0x80;		// Plain value not TLV encoded

	public final static byte SM_HASH_INPUT		  = (byte)0xA0;		// Input template for HASH calculation
	public final static byte SM_VERIFY_CERT1	  = (byte)0xAE;		// Input template for certificate verification (concatenated fields are certified)
	public final static byte SM_VERIFY_CERT2	  = (byte)0xBE;		// Input template for certificate verification (template is certified)
	
	
	// ISO return codes

	public final static int RC_OK                 = 0x9000;      	/* Process completed                 */

	public final static int RC_TIMEOUT            = 0x6401;      	/* Exec error: Command timeout       */

	public final static byte RC_OKMOREDATA        = (byte)0x61;   	/*-Process completed, more data available*/
	public final static int RC_WARNING            = 0x6200;      	/*-Warning: NV-Ram not changed       */
	public final static int RC_WARNING1           = 0x6201;      	/*-Warning: NV-Ram not changed 1     */
	public final static int RC_DATAINV            = 0x6281;      	/*-Warning: Part of data corrupted   */
	public final static int RC_EOF                = 0x6282;      	/*-Warning: End of file reached      */
	public final static int RC_INVFILE            = 0x6283;      	/* Warning: Invalidated file         */
	public final static int RC_INVFORMAT          = 0x6284;      	/* Warning: Invalid file control     */
	public final static byte RC_WARNINGNVCHGED    = (byte)0x63;     /* Warning: Non volatile memory changed */
	public final static int RC_WARNINGNVCHG       = 0x6300;      	/*-Warning: NV-Ram changed           */
	public final static int RC_WARNINGCOUNT       = 0x63C0;      	/*-Warning: Warning with counter     */
	public final static int RC_WARNING0LEFT       = 0x63C0;      	/*-Warning: Verify fail, no try left */
	public final static int RC_WARNING1LEFT       = 0x63C1;      	/*-Warning: Verify fail, 1 try left  */
	public final static int RC_WARNING2LEFT       = 0x63C2;      	/*-Warning: Verify fail, 2 tries left*/
	public final static int RC_WARNING3LEFT       = 0x63C3;      	/*-Warning: Verify fail, 3 tries left*/
	public final static int RC_EXECERR            = 0x6400;      	/*-Exec error: NV-Ram not changed    */
	public final static int RC_MEMERR             = 0x6501;      	/*-Exec error: Memory failure        */
	public final static int RC_MEMERRWRITE        = 0x6581;      	/*-Exec error: Memory failure        */
	public final static byte RC_INVLEN            = (byte)0x67;   	/*-Checking error: Wrong length      */
	public final static int RC_WRONGLENGTH        = 0x6700;     	/*-Checking error: Wrong length      */

	public final static int RC_CLANOTSUPPORTED    = 0x6800;      	/*-Checking error: Function in CLA byte not supported */
	public final static int RC_LCNOTSUPPORTED     = 0x6881;      	/*-Checking error: Logical channel not supported */
	public final static int RC_SMNOTSUPPORTED     = 0x6882;      	/*-Checking error: Secure Messaging not supported */
	public final static int RC_LASTCMDEXPECTED    = 0x6883;      	/*-Checking error: Last command of the chain expected */
	public final static int RC_CHAINNOTSUPPORTED  = 0x6884;      	/*-Checking error: Command chaining not supported */

	public final static int RC_COMNOTALLOWED      = 0x6900;      	/*-Checking error: Command not allowed */
	public final static int RC_COMINCOMPATIBLE    = 0x6981;      	/*-Checking error: Command incompatible with file structure */
	public final static int RC_SECSTATNOTSAT      = 0x6982;      	/*-Checking error: Security condition not satisfied */
	public final static int RC_AUTHMETHLOCKED     = 0x6983;      	/*-Checking error: Authentication method locked */
	public final static int RC_REFDATANOTUSABLE   = 0x6984;      	/*-Checking error: Reference data not usable */
	public final static int RC_CONDOFUSENOTSAT    = 0x6985;      	/*-Checking error: Condition of use not satisfied */
	public final static int RC_COMNOTALLOWNOEF    = 0x6986;      	/*-Checking error: Command not allowed (no current EF) */
	public final static int RC_SMOBJMISSING       = 0x6987;      	/*-Checking error: Expected secure messaging object missing */
	public final static int RC_INCSMDATAOBJECT    = 0x6988;      	/*-Checking error: Incorrect secure messaging data object */

	public final static int RC_INVPARA            = 0x6A00;      	/*-Checking error: Wrong parameter P1-P2 */
	public final static int RC_INVDATA            = 0x6A80;      	/*-Checking error: Incorrect parameter in the command data field*/
	public final static int RC_FUNCNOTSUPPORTED	  = 0x6A81;			/*-Checking error: Function not supported */
	public final static int RC_NOAPPL             = 0x6A82;      	/*-Checking error: File not found    */
	public final static int RC_FILENOTFOUND       = 0x6A82;      	/*-Checking error: File not found    */
	public final static int RC_RECORDNOTFOUND     = 0x6A83;      	/*-Checking error: Record not found    */
	public final static int RC_OUTOFMEMORY        = 0x6A84;      	/*-Checking error: Not enough memory space in the file   */
	public final static int RC_INVLCTLV           = 0x6A85;      	/*-Checking error: Nc inconsistent with TLV structure */
	public final static int RC_INVACC             = 0x6A85;      	/*-Checking error: Access cond. n/f  */
	public final static int RC_INCP1P2            = 0x6A86;      	/*-Checking error: Incorrect P1-P2   */
	public final static int RC_INVLC              = 0x6A87;      	/*-Checking error: Lc inconsistent with P1-P2 */
	public final static int RC_RDNOTFOUND         = 0x6A88;      	/*-Checking error: Reference data not found*/
	public final static int RC_FILEEXISTS         = 0x6A89;      	/*-Checking error: File already exists */
	public final static int RC_DFNAMEEXISTS       = 0x6A8A;      	/*-Checking error: DF name already exists */

	public final static int RC_INVP1P2            = 0x6B00;      	/*-Checking error: Wrong parameter P1-P2 */
	public final static byte RC_INVLE			  = (byte)0x6C;		/*-Checking error: Invalid Le        */
	public final static byte RC_INVINS            = (byte)0x6D; 	/*-Checking error: Wrong instruction */
	public final static int RC_INVCLA             = 0x6E00;      	/*-Checking error: Class not supported */
	public final static int RC_ACNOTSATISFIED     = 0x9804;      	/* Access conditions not satisfied   */
	public final static int RC_NOMORESTORAGE      = 0x9210;      	/* No more storage available         */
	public final static int RC_GENERALERROR		  = 0x6F00;			/*-Checking error: No precise diagnosis */

	/* ISO7816-6/9 defined TAGs ----------------------------------------------- */

	public final static byte TAG_FCP             = (byte)0x62;
	public final static byte TAG_FCI             = (byte)0x6F;
	public final static byte TAG_FCPFILETYPE     = (byte)0x82;
	public final static byte TAG_FCPFID          = (byte)0x83;
	public final static byte TAG_FCPDFNAME       = (byte)0x84;
	public final static byte TAG_FCPSFI          = (byte)0x88;
	public final static byte TAG_FCPLCSI         = (byte)0x8A;

	public final static byte TAG_KEYREFERENCE    = (byte)0x83;
	public final static byte TAG_PRKEYREFERENCE	 = (byte)0x84;
	public final static byte TAG_ALGOREFERENCE	 = (byte)0x89;
	
	/* Supported card types --------------------------------------------------- */
	
	public final static int	CARDTYPE_MICARDO20	  = 0x0120;
	public final static int CARDTYPE_MICARDO21	  = 0x0121;
	public final static int CARDTYPE_MICARDO23	  = 0x0123;
	
	public final static int CARDTYPE_STARCOS30	  = 0x0230;

	public final static int CARDTYPE_TCOSICAO30	  = 0x0330;

	public final static int CARDTYPE_JCOP41		  = 0x0441;
	
	public final static int CARDTYPE_EC			  = 0x0500;
	
	public final static int CARDTYPE_GEMXCOS	  = 0x0600;
    
    public final static int CARDTYPE_ACOS         = 0x0700;

    public final static int CARDTYPE_CARDOS       = 0x0800;
    
    public final static int CARDTYPE_SC_HSM       = 0x0900;
}
