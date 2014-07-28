/*
 * Copyright © 1998 Gemplus SCA
 * Av. du Pic de Bertagne - Parc d'Activités de Gémenos
 * BP 100 - 13881 Gémenos CEDEX
 * 
 * "Code derived from the original OpenCard Framework".
 * 
 * Everyone is allowed to redistribute and use this source  (source
 * code)  and binary (object code),  with or  without modification,
 * under some conditions:
 * 
 *  - Everyone  must  retain  and/or  reproduce the above copyright
 *    notice,  and the below  disclaimer of warranty and limitation
 *    of liability  for redistribution and use of these source code
 *    and object code.
 * 
 *  - Everyone  must  ask a  specific prior written permission from
 *    Gemplus to use the name of Gemplus.
 * 
 * DISCLAIMER OF WARRANTY
 * 
 * THIS CODE IS PROVIDED "AS IS",  WITHOUT ANY WARRANTY OF ANY KIND
 * (INCLUDING,  BUT  NOT  LIMITED  TO,  THE IMPLIED  WARRANTIES  OF
 * MERCHANTABILITY  AND FITNESS FOR  A  PARTICULAR PURPOSE)  EITHER
 * EXPRESS OR IMPLIED.  GEMPLUS DOES NOT WARRANT THAT THE FUNCTIONS
 * CONTAINED  IN THIS SOFTWARE WILL MEET THE USER'S REQUIREMENTS OR
 * THAT THE OPERATION OF IT WILL BE UNINTERRUPTED OR ERROR-FREE. NO
 * USE  OF  ANY  CODE  IS  AUTHORIZED  HEREUNDER EXCEPT UNDER  THIS
 * DISCLAIMER.
 * 
 * LIMITATION OF LIABILITY
 * 
 * GEMPLUS SHALL NOT BE LIABLE FOR INFRINGEMENTS OF  THIRD  PARTIES
 * RIGHTS. IN NO EVENTS, UNLESS REQUIRED BY APPLICABLE  LAW,  SHALL
 * GEMPLUS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES OF ANY CHARACTER  INCLUDING,
 * WITHOUT LIMITATION, DAMAGES FOR LOSS OF GOODWILL, WORK STOPPAGE,
 * COMPUTER FAILURE OR MALFUNCTION, OR ANY AND ALL OTHER DAMAGES OR
 * LOSSES, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. ALSO,
 * GEMPLUS IS  UNDER NO  OBLIGATION TO MAINTAIN,  CORRECT,  UPDATE, 
 * CHANGE, MODIFY, OR OTHERWISE SUPPORT THIS SOFTWARE.
 */
package opencard.opt.database;

import opencard.core.terminal.CardTerminalException;
import opencard.opt.service.CardServiceInterface;

/**
 * This interface defines the features that are necessary
 * for a CardService to be able to access smartcards that
 * provide database functionalities as defined by ISO 7816-7.
 * 
 * @author  HAMEL Arnaud
 * @author  DANGREMONT Cedric
 * @author  Christophe.Muller@research.gemplus.com
 * @version $Id: DatabaseCardService.java,v 1.1 1999/12/06 15:46:05 damke Exp $
 * @since   OCF1.2
 * 
 */

public interface DatabaseCardService extends CardServiceInterface {

    /**
     * The <code>Constants</code> inner interface allows to use
     * standardized constants.<BR>
     * It provides constants used to deal with database smartcards
     * such as constants which describes SCQL operation or code
     * used to manage SCQL exceptions.<BR>
     */
    public interface Constants {
   
	/**
	 * INS coding constants
	 * (SCQL constant to perform SCQL operations such as createTable,
	 *  createView, grant, revoke ...)
	 */  
    
	public final static byte SCQL_OPERATION = (byte)0x10;
	public final static byte TRANSACTION_OPERATION = (byte)0x12;
	public final static byte USER_OPERATION = (byte)0x14;
   
	// P2 coding for SCQL operations
	public final static byte CREATE_TABLE = (byte)0x80;
	public final static byte CREATE_VIEW = (byte)0x81;
	public final static byte CREATE_DICTIONARY = (byte)0x82;
	public final static byte DROP_TABLE = (byte)0x83;
	public final static byte DROP_VIEW= (byte)0x84;
	public final static byte GRANT = (byte)0x85;
	public final static byte REVOKE = (byte)0x86;
	public final static byte DECLARE_CURSOR = (byte)0x87;
	public final static byte OPEN = (byte)0x88;
	public final static byte NEXT = (byte)0x89;
	public final static byte FETCH = (byte)0x8A;
	public final static byte FETCH_NEXT= (byte)0x8B;
	public final static byte INSERT = (byte)0x8C;
	public final static byte UPDATE = (byte)0x8D;
	public final static byte DELETE = (byte)0x8E;
	
	// P2 coding for transaction operations
	public final static byte BEGIN = (byte)0x80;
	public final static byte COMMIT = (byte)0x81;
	public final static byte ROLLBACK = (byte)0x82;
	
	// P2 coding for user operations
	public final static byte PRESENT_USER = (byte)0x80;
	public final static byte CREATE_USER = (byte)0x81;
	public final static byte DELETE_USER = (byte)0x82;
	
	// Coding for comparison operators
	public final static byte EQUAL_TO = (byte)0x3D;
	public final static byte LESS_THAN = (byte)0x3C;
	public final static byte GREATER_THAN = (byte)0x3E;
	public final static byte LESS_THAN_OR_EQUAL_TO = (byte)0x4C;
	public final static byte GREATER_THAN_OR_EQUAL_TO = (byte)0x47;
	public final static byte NOT_EQUAL_TO = (byte)0x23;
	
	// Coding of privileges
	public final static byte PRIV_INSERT = (byte)0x41;
	public final static byte PRIV_SELECT = (byte)0x42;
	public final static byte PRIV_UPDATE = (byte)0x44;
	public final static byte PRIV_DELETE = (byte)0x48;
	public final static byte PRIV_ALL = (byte)0x4F;
	
	// Coding for SW1 and SW2 Normal Processing
	public final static byte SW1_OK = (byte)0x90;
	public final static byte SW1_OK_WITH_RETURN = (byte)0x61;
	public final static byte SW2_OK = (byte)0x00;
	
	// Coding for SW1 and SW2 Warning Processing
	public final static byte SW1_WARNING = (byte)0x62;
	public final static byte SW2_WARNING = (byte)0x82;
	
	// Coding for SW1 and SW2 Execution errors
	public final static byte SW1_EXEC_ERROR = (byte)0x65;
	public final static byte SW2_EXEC_ERROR = (byte)0x00;
	public final static byte SW2_MEM_FAILURE = (byte)0x62;
	
	// Coding for SW1 and SW2 Checking errors
	public final static byte SW1_CHECK_ERROR = (byte)0x67;
	public final static byte SW2_CHECK_ERROR = (byte)0x00;
	
	// Coding for SW1 and SW2 Command not allowed
	public final static byte SW1_COMMAND_NOT_ALLOWED = (byte)0x69;
	public final static byte SW2_COMMAND_NOT_ALLOWED = (byte)0x00;
	public final static byte SW2_SECURITY = (byte)0x82;
	public final static byte SW2_REQUIRED_OPERATION = (byte)0x85;
	
	// Coding for SW1 and SW2 Wrong parameters
	public final static byte SW1_WRONG_PARAM = (byte)0x6A;
	public final static byte SW2_WRONG_PARAM = (byte)0x80;
	public final static byte SW2_INCORRECT_PARAM = (byte)0x81;
	public final static byte SW2_OPERATION_NOT_SUPPORTED = (byte)0x84;
	public final static byte SW2_OBJECT_NOT_FOUND = (byte)0x88;
	public final static byte SW2_OBJECT_ALREADY_EXISTS = (byte)0x89;
	
	// Coding for SW1 and SW2 Wrong length Le  
	public final static byte SW1_WRONG_LENGTH = (byte)0x6C;
	
	// Coding for SW1 and SW2 Instruction code not supported
	public final static byte SW1_INSTRUCTION_CODE_NOT_SUPPORTED
	    = (byte)0x6D;
	public final static byte SW2_INSTRUCTION_CODE_NOT_SUPPORTED
	    = (byte)0x00;

	// Coding of Tags for Card Holder Certificate & Name (from ISO 7816-6).
	public final static byte TAG_CH_CERTIFICATE_1 = (byte)0x7F;
	public final static byte TAG_CH_CERTIFICATE_2 = (byte)0x21;
	public final static byte TAG_CH_NAME_1 = (byte)0x5F;
	public final static byte TAG_CH_NAME_2 = (byte)0x20;
    }

    public void createTable(String tableName,
			    String columnsList, 
			    byte maxnumberOfRows, 
			    SecurityAttribute securityattribute) 
	throws CardTerminalException, SCQLException;
    
    public void createView(String viewName, 
			   String tableName, 
			   String colunmNames, 
			   String conditions, 
			   SecurityAttribute securityattribute) 
	throws CardTerminalException, SCQLException;
    
    public void createDictionary(String dictionary) 
	throws CardTerminalException, SCQLException;
    
    public void dropTable(String tableName) 
	throws CardTerminalException, SCQLException;
    
    public void dropView(String viewName) 
	throws CardTerminalException, SCQLException;
    
    public void grant(String privileges, 
		      String objectName, 
		      String userID) 
	throws CardTerminalException, SCQLException;
    
    public void revoke(String privilege, 
		       String objectName, 
		       String userID) 
	throws CardTerminalException, SCQLException;
    
    public void declareCursor(String objectName, 
			      String columnsName, 
			      String conditions) 
	throws CardTerminalException, SCQLException;
    
    public void open() 
	throws CardTerminalException, SCQLException;
    
    public void next() 
	throws CardTerminalException, SCQLException;
    
    public String[] fetch(byte maxLength) 
	throws CardTerminalException, SCQLException;
    
    public String[] fetchNext(byte maxLength) 
	throws CardTerminalException, SCQLException;
    
    public String[] fetch() 
	throws CardTerminalException, SCQLException;
    
    public String[] fetchNext() 
	throws CardTerminalException, SCQLException;
    
    public void insert(String tableName, String values) 
	throws CardTerminalException, SCQLException;
    
    public void update(String values) 
	throws CardTerminalException, SCQLException;
    
    public void delete() 
	throws CardTerminalException, SCQLException;
    
    public void begin() 
	throws CardTerminalException, SCQLException; 
    
    public void commit() 
	throws CardTerminalException, SCQLException;
    
    public void rollback() 
	throws CardTerminalException, SCQLException;
    
    public void presentUser(String userID)
	throws CardTerminalException, SCQLException;
    
    public void presentUser(String userID,
			    SecurityAttribute securityAttribute) 
	throws CardTerminalException, SCQLException;
    
    public void createUser(String userID, 
			   String userProfile, 
			   SecurityAttribute securityattribute) 
	throws CardTerminalException, SCQLException;
    
    public void deleteUser(String userID) 
	throws CardTerminalException, SCQLException;

}
