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

import opencard.core.service.CardService;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.Tracer;

/**
 * This class is designed as a basic default class which allows
 * programmers to access ISO 7816-7-compliant smartcards. 
 * <p>It provides all methods necessary to access database smartcards,
 * as describes in the ISO 7816-7 norm.
 * <p>It is intended that the CardService developers for specific
 * card use this class as a base class and by inheriting its
 * functionalities 
 * 
 * 
 * @author  HAMEL Arnaud
 * @author  DANGREMONT Cedric
 * @author  Christophe.Muller@research.gemplus.com
 * @version $Id: BasicDatabase.java,v 1.1 1999/12/06 15:46:04 damke Exp $
 * @since   OCF1.2
 * 
 * @see opencard.opt.database.DatabaseCardService
 */

public class BasicDatabase
    extends CardService
    implements DatabaseCardService, DatabaseCardService.Constants {
   
    // A tracer for debugging output
    private static Tracer ctracer = new Tracer(BasicDatabase.class);

    /**
     * MAX_SIZE represents maximum size of a command APDU that can be built.
     */
    protected final int MAX_SIZE = 300;
   
    /**
     * Allows to initialize the cardservice. This method is called
     * when instanciating the cardservice (after the call of the
     * BasicDatabase constructor).
     *
     * @param scheduler The CardServiceScheduler.
     * @param smartcard The SmartCard.
     * @param blocking Boolean that defines if the smartcard blocks
     * access for other applications while working or not.
     * 
     * @see opencard.opt.database.BasicDatabase#BasicDatabase()
     */
    
    protected void initialize(CardServiceScheduler scheduler,
                              SmartCard smartcard,
                              boolean blocking)
        throws CardServiceException { 
        super.initialize(scheduler, smartcard, blocking); 
    }
    
    /**
     * Constructor called when creating a new BasicDatabase.
     */
    
    public BasicDatabase() {
        super();
    }
    
    /**
     * Defines a table with its columns and possibly with security
     * attributes. The table definition is added in the object
     * descriprion table.<P>
     * A table can only be created by users with the DB_O profile
     * (DataBase Owner) or DBOO (DataBase Object Owner).
     *
     * @param tablename The name of the table to be created.
     * @param columnslist The list of the columns of the table.
     * This parameter is a string. Each column is separated by a
     * semi-column from the next.
     * @param maxnumberofrows The maximum number of rows that can be
     * inserted in the table.
     * @param securityattribute The security attributes.
     * 
     * @exception opencard.opt.database.SCQLException#SCQLException()
     * @exception opencard.opt.database.SCQLException#SCQLException(java.lang.String)
     * 
     * @see opencard.opt.database.BasicDatabase#createView (java.lang.String, java.lang.String, java.lang.String, java.lang.String, byte);
     @see opencard.opt.database.BasicDatabase#createDictionary (java.lang.String)
    */
    
    public void createTable(String tableName, String columnsList, byte maxNumberOfRows, SecurityAttribute securityAttribute) throws SCQLException {
        int i;
        tableName=tableName.toUpperCase();
        DataObject.isIdentifier(tableName);

        String col[] = DataObject.parseString (columnsList.toUpperCase());
        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);
        cmd.append (DataObject.headerAPDU ((byte) 0x00,
                                           SCQL_OPERATION,
                                           (byte) 0x00,
                                           CREATE_TABLE));
        cmd.append (DataObject.bodyAPDU (tableName));
        cmd.append ((byte) col.length);
        for (i=0 ; i < col.length ; i++) {
            cmd.append (DataObject.bodyAPDU (col[i].trim()));
        }
        if (maxNumberOfRows>0) {
            cmd.append ((byte) 0x01);
            cmd.append (maxNumberOfRows);
        }
        if (securityAttribute!=null) {
            cmd.append (securityAttribute.getBytes());
        }
        sendAPDU (cmd);
    }
    
    public void createTable(String tableName, String columnsList, SecurityAttribute securityAttribute) throws SCQLException {
        createTable (tableName,columnsList,(byte) 0, securityAttribute);
    }
   
    /**
     * Defines a view on a table. The view definition is added to
     * the object description table.<BR>
     * A view can only be created by the owner of the referenced table.<BR>
     * If several conditions are present, they are implicitely
     * combinated with a logical AND.<BR>
     *
     * @param viewname The name of the view you want to create.
     * @param tablename The name of the object on which you want
     * to build your view.
     * @param colunmname The list of fields presents in the view.
     * Each field is separed from the next by a coma.
     * @param conditions The list of conditions used to build the view.
     * Each field is separed from the next by a coma.
     * @param securityattribute The security attribute used to create the view.
     *
     * @exception opencard.opt.database.SCQLException#SCQLException()
     * @exception opencard.opt.database.SCQLException#SCQLException(java.lang.String)
     *
     * @see opencard.opt.database.BasicDatabase#createTable (java.lang.String, java.lang.String, byte, byte)
     * @see opencard.opt.database.BasicDatabase#createDictionary (java.lang.String)
    */
    
    public void createView(String viewName,
                           String tableName,
                           String colunmNames,
                           String conditions,
                           SecurityAttribute securityAttribute)
        throws SCQLException {
        viewName=viewName.toUpperCase();
        DataObject.isIdentifier (viewName);
        tableName=tableName.toUpperCase();
        DataObject.isIdentifier (tableName);

        String colname[] = DataObject.parseString (colunmNames.toUpperCase());
        String cond[] = DataObject.parseString (conditions); 
        String condcolname[] = new String[cond.length];
        String ope[] = new String[cond.length];
        int i,j;

        for (i=0 ; i < cond.length ; i++) {
            j=0;
            while (!DataObject.isOperator(cond[i].charAt(j))) {
                j++;
            }
            condcolname[i]=(cond[i].substring(0,j)).toUpperCase().trim();
            cond[i]=cond[i].substring(j,cond[i].length()).trim();
            if (DataObject.isOperator(cond[i].charAt(1))) {
                j=2;
            } else {
                j=1;
            }
            ope[i]=cond[i].substring(0,j).trim();
            cond[i]=cond[i].substring(j,cond[i].length()).trim();
        }

        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU((byte) 0x00,
                                          SCQL_OPERATION,
                                          (byte) 0x00,
                                          CREATE_VIEW));
        cmd.append (DataObject.bodyAPDU(viewName));
        cmd.append (DataObject.bodyAPDU(tableName));

        if ((colname[0].trim()).equals("*")) {
            cmd.append ((byte) 0x00);
        } else {
            cmd.append ((byte) colname.length);
            for (i=0 ; i < colname.length ; i++)  {
                colname[i]=colname[i].trim();
                DataObject.isIdentifier (colname[i]);
                cmd.append (DataObject.bodyAPDU(colname[i]));
            }
        }
        if (condcolname.length>0) {
            cmd.append ((byte) condcolname.length);
            for (i=0 ; i < condcolname.length ; i++) {
                cmd.append (DataObject.bodyAPDU(condcolname[i].trim()));
                cmd.append ((byte) 0x01);
                cmd.append ((byte) DataObject.operator(ope[i].trim()));
                cmd.append (DataObject.bodyAPDU(cond[i])); // no trim().
            }
        } else {
            cmd.append ((byte) 0x00);
        }

        if (securityAttribute!=null) {
            cmd.append (securityAttribute.getBytes());
        }     
        sendAPDU (cmd);
    }
    
    /**
     * Defines a view on the system tables *O, *U and *P.
     * The fixed view definitions are added by the card in the object
     * descrition table.<BR>
     * A dictionary can only be created by the DB_O (DataBase Owner)
     * or a DBOO (DataBase Object Owner).<BR>
     * <BR>
     * <U>Note :</U> This command has no equivalence in SQL.<BR>
     *
     * @param dictionary The name of the dictionary you want to create.
     *
     * @exception opencard.opt.database.SCQLException#SCQLException()
     * @exception opencard.opt.database.SCQLException#SCQLException(java.lang.String)
     *
     * @see opencard.opt.database.BasicDatabase#createTable(java.lang.String, java.lang.String ,byte, byte)
     * @see opencard.opt.database.BasicDatabase#createView (java.lang.String, java.lang.String, java.lang.String, java.lang.String)
    */
    
    public void createDictionary(String dictionary) throws SCQLException {
        DataObject.isDictionaryIdentifier (dictionary);

        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU((byte) 0x00,
                                          SCQL_OPERATION,
                                          (byte) 0x00,
                                          CREATE_DICTIONARY));
        cmd.append (DataObject.bodyAPDU(dictionary));
        sendAPDU (cmd);
    }
    
    /**
     * Allows to drop a table.<BR>
     * A table can only be dropped by its owner. The privileges
     * associated to the table should be automatically be dropped.<BR>
     *
     * @param tablename The name of the table you want to drop
     *     
     * @exception opencard.opt.database.SCQLException#SCQLException()
     * @exception opencard.opt.database.SCQLException#SCQLException(java.lang.String)
     *
     * @see opencard.opt.database.BasicDatabase#dropView (java.lang.String)
     */
    
    public void dropTable(String tableName) throws SCQLException {
        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU ((byte) 0x00,
                                           SCQL_OPERATION,
                                           (byte) 0x00,
                                           DROP_TABLE));
        cmd.append (DataObject.bodyAPDU (tableName.toUpperCase()));
        sendAPDU (cmd);
    }
    
    /**
     * Allows to drop a view.<BR>
     * A table can only be dropped by its owner. The privileges
     * associated to the table should be automatically be dropped.<BR>
     *
     * @exception opencard.opt.database.SCQLException#SCQLException()
     * @exception opencard.opt.database.SCQLException#SCQLException(java.lang.String)
     *
     * @see opencard.opt.database.BasicDatabase#dropTable (java.lang.String)
     */
    
    public void dropView(String viewName) throws SCQLException {
        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU ((byte) 0x00,
                                           SCQL_OPERATION,
                                           (byte) 0x00,
                                           DROP_VIEW));
        cmd.append (DataObject.bodyAPDU (viewName.toUpperCase()));
        sendAPDU (cmd);
    }
    
    /**
     * Allows to grant privileges to a single user, to a user group
     * or to all users.<BR>
     * <p>The following rivileges may be granted.<BR>
     * <BR>
     * Privileges for table access :<BR><UL>
     * <LI>SELECT<BR>
     * <LI>INSERT<BR>
     * <LI>UPDATE<BR>
     * <LI>DELETE<BR>
     * <LI>ALL<BR>
     * </UL>
     *<BR>
     * Privileges for view access :<BR><UL>
     * <LI>SELECT<BR>
     * <LI>UPDATE<BR>
     *</UL><BR>
     * Privileges for dictionary access : <BR><UL>
     * <LI>SELECT<BR>
     *</UL><BR>
     *
     * @param privilege The list of privileges to grant. Each privilege is a
     * part of the string. Each privilege is separated from the next by a coma.
     * @param objectname The object you want to grant (a table, a view
     * or a dictionary).
     * @param userid The user(s) you want to be granted privileges.
     *
     * @exception opencard.opt.database.SCQLException#SCQLException()
     * @exception opencard.opt.database.SCQLException#SCQLException(java.lang.String)
     *
     * @see opencard.opt.database.BasicDatabase#revoke (java.lang.String, java.lang.String, java.lang.String)
     * @see opencard.opt.database.SecurityStatusNotSatisfiedException
     * @see opencard.opt.database.IncorrectParameterInDataFieldException
     * @see opencard.opt.database.OperationNotSupportedException
     * @see opencard.opt.database.ReferencedObjectNotFoundException
     */
    
    public void grant(String privileges, String objectName, String userID)
        throws SCQLException {

        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU ((byte) 0x00,
                                           SCQL_OPERATION,
                                           (byte) 0x00,
                                           GRANT));
        cmd.append ((byte) 0x01);
        cmd.append ((byte) DataObject.parsePrivilege (privileges));
        cmd.append (DataObject.bodyAPDU (objectName.toUpperCase()));
        cmd.append (DataObject.bodyAPDU (userID.toUpperCase()));
        sendAPDU (cmd);
    }
    
    /**
     * Allows to revoke privileges <strong>granted before</strong>.<BR>
     * Only the owner of the table or view can revoke privileges.
     *  
     * @param privilege The list of privileges to revoke. Each privilege
     * is a part of the string. Each privilege is separated from the next
     * by a coma.
     * @param objectname The object you want to revoke privileges on
     * (i.e., a table, a view or a dictionary).
     * @param userid The user(s) you want to be revoked privileges.
     *
     * @exception opencard.opt.database.SCQLException#SCQLException()
     * @exception opencard.opt.database.SCQLException#SCQLException(java.lang.String)
     *
     * @see opencard.opt.database.BasicDatabase#grant (java.lang.String, java.lang.String, java.lang.String)
     * @see opencard.opt.database.SecurityStatusNotSatisfiedException
     * @see opencard.opt.database.IncorrectParameterInDataFieldException
     * @see opencard.opt.database.ReferencedObjectNotFoundException
     */
    
    public void revoke(String privileges,
                       String objectName,
                       String userID)
        throws SCQLException {

        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU ((byte) 0x00,
                                           SCQL_OPERATION,
                                           (byte) 0x00,
                                           REVOKE));
        cmd.append ((byte) 0x01);
        cmd.append ((byte) DataObject.parsePrivilege (privileges));
        cmd.append (DataObject.bodyAPDU (objectName.toUpperCase()));
        cmd.append (DataObject.bodyAPDU (userID.toUpperCase()));
        sendAPDU (cmd);
    }
    
    /**
     * Cursor is used for pointing to a row on a table, view or dictionary.
     * This method is used for the declaration of a cursor.<BR>
     * The declaration of the cursor is only accepted if the actual user
     * is authorized to access the referenced table, view or dictionary.
     * The user has to be the owner of the referenced object or at least
     * one privilege for access to the referenced object.<BR>
     * Only one cursor can exist at a given time, i.e., if a new cursor
     * is declared then the previous is no longer valid.<BR>
     *
     * @param objectname The name of the object on which you want
     * to declare your cursor.
     * @param columnsname The list of the columns you want to build
     * your selection on.
     * @param condition The string that contains the conditions to
     * apply for the cursor.
     *
     * @see opencard.opt.database.BasicDatabase#fetch()
     * @see opencard.opt.database.BasicDatabase#fetch(byte)
     * @see opencard.opt.database.BasicDatabase#fetchNext()
     * @see opencard.opt.database.BasicDatabase#fetchNext(byte)
     * @see opencard.opt.database.BasicDatabase#open()
     * @see opencard.opt.database.BasicDatabase#next()
     */
    
    public void declareCursor(String objectName,
                              String columnsName,
                              String conditions)
        throws SCQLException {

        String colname[] = DataObject.parseString (columnsName.toUpperCase());
        String cond[] = DataObject.parseString (conditions);
        String condcolname[] = new String[cond.length];
        String ope[] = new String[cond.length];

        int i,j;
        for (i=0;i<cond.length;i++) {
            j=0;
            while (!DataObject.isOperator(cond[i].charAt(j))) {
                j++;
            }
            condcolname[i]=(cond[i].substring(0,j)).toUpperCase();
            cond[i]=cond[i].substring(j,cond[i].length());
            if (DataObject.isOperator(cond[i].charAt(1))) {
                j=2; 
            } else {
                j=1;
            }
            ope[i]=cond[i].substring(0,j);
            cond[i]=cond[i].substring(j,cond[i].length());
        }

        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU((byte) 0x00,
                                          SCQL_OPERATION,
                                          (byte) 0x00,
                                          DECLARE_CURSOR));
        cmd.append (DataObject.bodyAPDU(objectName.toUpperCase().trim()));
        if ((colname[0].trim()).equals("*")) {
            cmd.append ((byte) 0x00);
        } else {
            cmd.append ((byte) colname.length); 
            for (i=0;i<colname.length;i++) {
                DataObject.isIdentifier (colname[i].trim());
                cmd.append (DataObject.bodyAPDU(colname[i].trim()));
            }
        }

        if (condcolname.length>0) {
            cmd.append ((byte) condcolname.length);
            for (i=0;i<cond.length;i++) {
                cmd.append (DataObject.bodyAPDU(condcolname[i].trim()));
                cmd.append ((byte) 0x01);
                cmd.append ((byte) DataObject.operator(ope[i].trim()));
                cmd.append (DataObject.bodyAPDU(cond[i].trim()));
            }
        } else {
            cmd.append ((byte) 0x00);
        }
        sendAPDU (cmd);
    }
    
    /**
     * Opens a cursor, i.e., the cursor is positioned on the first row
     * which satisfies the selection previously defined with the
     * <code>declareCursor</code> method.
     *     
     * @see opencard.opt.database.BasicDatabase#fetch()
     * @see opencard.opt.database.BasicDatabase#fetch(byte)
     * @see opencard.opt.database.BasicDatabase#fetchNext()
     * @see opencard.opt.database.BasicDatabase#fetchNext(byte)
     * @see opencard.opt.database.BasicDatabase#declareCursor(java.lang.String, java.lang.String, java.lang.String)
     * @see opencard.opt.database.BasicDatabase#next()
     */
    
    public void open() throws SCQLException {
        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU((byte) 0x00,
                                          SCQL_OPERATION,
                                          (byte) 0x00,
                                          OPEN,
                                          (byte) 0));
        sendAPDU (cmd);
    }
    
    /**
     * Sets the current cursor on the next row satisfying the cursor
     * specification.<BR>
     * A cursor must have been opened before.<BR>
     *
     * @see opencard.opt.database.BasicDatabase#fetch()
     * @see opencard.opt.database.BasicDatabase#fetch(byte)
     * @see opencard.opt.database.BasicDatabase#fetchNext()
     * @see opencard.opt.database.BasicDatabase#fetchNext(byte)
     * @see opencard.opt.database.BasicDatabase#declareCursor(java.lang.String, java.lang.String, java.lang.String)
     * @see opencard.opt.database.BasicDatabase#open()
     */
    
    public void next() throws SCQLException {
        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU((byte) 0x00,
                                          SCQL_OPERATION,
                                          (byte) 0x00,
                                          NEXT,
                                          (byte) 0));
        sendAPDU (cmd);
    }
    
    /**
     * Allows to fetch a row or part of it. The cursor has to point on
     * the row to be fetched.<BR>
     * The operation can only be executed by the object owner or
     * a user with the <tt>SELECT</tt> privilege 
     * A cursor must have been opened before.<BR>
     *
     * @param maxlength The maximum length of expected data.
     *     
     * @see opencard.opt.database.BasicDatabase#fetch()
     * @see opencard.opt.database.BasicDatabase#fetchNext()
     * @see opencard.opt.database.BasicDatabase#fetchNext(byte)
     * @see opencard.opt.database.BasicDatabase#declareCursor(java.lang.String, java.lang.String, java.lang.String)
     * @see opencard.opt.database.BasicDatabase#open()
     * @see opencard.opt.database.BasicDatabase#next(byte)
     */
    
    public String[] fetch(byte maxLength) throws SCQLException {
        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU((byte) 0x00,
                                          SCQL_OPERATION,
                                          (byte) 0x00,
                                          FETCH,
                                          (byte) 0));
        if (maxLength == 0) {
            try {
                sendAPDU (cmd);
            }
            catch(WrongLengthLeException e) {
                cmd.append ((byte) e.getRightLength());
            }
        } else {
            cmd.append ((byte) maxLength);
        }
        return DataObject.responseToString(sendAPDU (cmd));
    }
    
    /**
     * Allows to fetch a row or part of it. The cursor has to point
     * on the row to be fetched.<BR>
     * The operation can only be executed by the object owner or
     * a user with the <tt>SELECT</tt> privilege.<BR>
     * A cursor must have been opened before.<BR>
     * No maximum length of data is expected.
     *
     * @see opencard.opt.database.BasicDatabase#fetch(byte)
     * @see opencard.opt.database.BasicDatabase#fetchNext()
     * @see opencard.opt.database.BasicDatabase#fetchNext(byte)
     * @see opencard.opt.database.BasicDatabase#declareCursor(java.lang.String, java.lang.String, java.lang.String)
     * @see opencard.opt.database.BasicDatabase#open()
     * @see opencard.opt.database.BasicDatabase#next(byte)
     */
    
    public String[] fetch() throws SCQLException {
        return fetch((byte) 0x00);
    }
    
    /**
     * Used for reading the logical next row from the cursor position.
     * The cursor is set to the row being fetched.<BR>
     * The operation can only be executed by the object owner or
     * a user with the <tt>SELECT</tt> privilege.<BR>
     * A cursor must have been opened before.<BR>
     *
     * @param maxlength The maximum length of expected data.
     *
     * @see opencard.opt.database.BasicDatabase#fetch(byte)
     * @see opencard.opt.database.BasicDatabase#fetch()
     * @see opencard.opt.database.BasicDatabase#fetchNext()
     * @see opencard.opt.database.BasicDatabase#declareCursor(java.lang.String, java.lang.String, java.lang.String)
     * @see opencard.opt.database.BasicDatabase#open()
     * @see opencard.opt.database.BasicDatabase#next(byte)
     */
    
    public String[] fetchNext(byte maxLength) throws SCQLException {
        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU((byte) 0x00,
                                          SCQL_OPERATION,
                                          (byte) 0x00,
                                          FETCH_NEXT,
                                          (byte) 0));
        if (maxLength == 0) {
            try {
                sendAPDU (cmd);
            }
            catch(WrongLengthLeException e) {
                cmd.append ((byte) e.getRightLength());
            }
        } else {
            cmd.append ((byte) maxLength);
        }
        return DataObject.responseToString(sendAPDU (cmd));
    }
    
    /**
     * Used for reading the logical next row from the cursor position.
     * The cursor is set to the row fetched.<BR>
     * The operation can only be executed by the object owner or a user
     * with the <tt>SELECT</tt> privilege.
     * A cursor must have been opened before.<BR>
     * No maximum length of data is expected.
     *
     * @see opencard.opt.database.BasicDatabase#fetch(byte)
     * @see opencard.opt.database.BasicDatabase#fetch()
     * @see opencard.opt.database.BasicDatabase#fetchNext(byte)
     * @see opencard.opt.database.BasicDatabase#declareCursor(java.lang.String, java.lang.String, java.lang.String)
     * @see opencard.opt.database.BasicDatabase#open()
     * @see opencard.opt.database.BasicDatabase#next(byte)
     */
    
    public String[] fetchNext() throws SCQLException {
        return fetchNext((byte) 0x00);
    }
    
    /**
     * Used to insert a row in a table. A new row is always added at the end
     * of a table. The cursor remains at its position.<BR>
     * The command can only be executed by the table owner or a user
     * with the <tt>INSERT</tt> privilege.<BR>
     * The value for the special column <tt>USER</tt>, if present,
     * is inserted by the card.<BR>
     *
     * @param tablename The name of the table you want to insert into.
     * @param values The String that contains the list of values to be
     * inserted.
     *
     * @see opencard.opt.database.BasicDatabase#update(java.lang.String)
     * @see opencard.opt.database.BasicDatabase#delete()
     */
    
    public void insert(String tableName, String values) throws SCQLException {
        String val[] = DataObject.parseString (values);
        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);
        int i;

        cmd.append (DataObject.headerAPDU ((byte) 0x00,
                                           SCQL_OPERATION,
                                           (byte) 0x00,
                                           INSERT));
        cmd.append (DataObject.bodyAPDU (tableName.toUpperCase()));
        cmd.append ((byte) val.length);
        for (i=0 ; i < val.length ; i++) {
            cmd.append (DataObject.bodyAPDU (val[i]));
        }
        sendAPDU (cmd);
    }
   
    /**
     * Updates one or more fields of a row in a table or view to which
     * the cursor points.<BR>
     * The command can only be executed by the table owner or a user
     * with the <tt>UPDATE</tt> privilege.<BR>
     * A cursor must be opened before.<BR>
     * The value for the special column USER, if present,
     * is inserted by the card.<BR>
     *
     * @param values The String that contains the list of values to be updated.
     *
     * @see opencard.opt.database.BasicDatabase#insert(java.lang.String, java.lang.String)
     * @see opencard.opt.database.BasicDatabase#delete()
     */
    
    public void update (String values) throws SCQLException {
        String col[] = DataObject.parseString (values);
        String val[] = new String[col.length];
        int i,j;

        for (i=0 ; i < col.length ; i++) {
            j=0;
            while (col[i].charAt(j)!='=') {
                j++;
            }
            val[i]=col[i].substring(j+1,col[i].length());
            col[i]=col[i].substring(0,j);
        }

        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU ((byte) 0x00,
                                           SCQL_OPERATION,
                                           (byte) 0x00,
                                           UPDATE));
        cmd.append ((byte) col.length);
        for (i=0 ; i < col.length ; i++) {
            cmd.append (DataObject.bodyAPDU (col[i].toUpperCase().trim()));
            cmd.append (DataObject.bodyAPDU (val[i]));
        }
        sendAPDU (cmd);
    }
    
    /**
     * Deletes a row in a table to which the cursor points. The cursor
     * is moved to the logical next row.<BR>
     * The command can only be executed by the table owner or a user
     * with the <tt>DELETE</tt> privilege for the referenced table.<BR>
     *
     * @see opencard.opt.database.BasicDatabase#insert(java.lang.String, java.lang.String)
     * @see opencard.opt.database.BasicDatabase#update(java.lang.String)
     */
    
    public void delete() throws SCQLException {
        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);
        cmd.append (DataObject.headerAPDU((byte) 0x00,
                                          SCQL_OPERATION,
                                          (byte) 0x00,
                                          DELETE,
                                          (byte) 0));
        sendAPDU (cmd);
    }
    
    /**
     * Allocates space for a memory image, e.g. a row.<BR>
     * The memory space which is provided is implementation dependent.
     * It is recommended that enough memory space for the buffering
     * of at least one row is allocated.<BR>
     *
     * @see opencard.opt.database.BasicDatabase#commit()
     * @see opencard.opt.database.BasicDatabase#rollback()
     */
    
    public void begin() throws SCQLException {
        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU((byte) 0x00,
                                          TRANSACTION_OPERATION,
                                          (byte) 0x00,
                                          BEGIN,
                                          (byte) 0));
        sendAPDU (cmd);
    }
    
    /**
     * Validates all the modifications made since the transaction
     * operation <tt>BEGIN</tt> as been executed.<BR>
     * The transaction operation <tt>BEGIN</tt> must have been previously
     * performed.<BR>
     *
     * @see opencard.opt.database.BasicDatabase#begin()
     * @see opencard.opt.database.BasicDatabase#rollback()
     */
    
    public void commit() throws SCQLException {
        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU((byte) 0x00,
                                          TRANSACTION_OPERATION,
                                          (byte) 0x00,
                                          COMMIT,
                                          (byte) 0));
        sendAPDU (cmd);
    }
    
    /**
     * Restores the context in the way it was before the transaction
     * operation <tt>BEGIN</tt> as been executed.<BR>
     * The transaction operation <tt>BEGIN</tt> must have been
     * previously performed.<BR>
     *
     * @see opencard.opt.database.BasicDatabase#begin()
     * @see opencard.opt.database.BasicDatabase#commit()
     */
    
    public void rollback() throws SCQLException {
        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU((byte) 0x00,
                                          TRANSACTION_OPERATION,
                                          (byte) 0x00,
                                          ROLLBACK,
                                          (byte) 0));
        sendAPDU (cmd);
    }
    
    /**
     * Checks the presented user id. If the user id is registred
     * in the system table *U, the user characterized by its user
     * id is set as current user.<BR>
     * There can only be one current user at a time per logical channel.<BR>
     * 
     * @param userid the login name of the presented user id.
     * @param securityAttribute the security attribute presented
     * for authentication.
     * 
     * @see opencard.opt.database.BasicDatabase#createUser(java.lang.String, java.lang.String, SecurityAttribute)
     * @see opencard.opt.database.BasicDatabase#deleteUser(java.lang.String)
     * @see opencard.opt.database.SecurityAttribute
     */
    
    public void presentUser(String userID, SecurityAttribute securityAttribute)
        throws SCQLException {

        userID=userID.toUpperCase();
        DataObject.isIdentifier(userID);

        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);
        cmd.append (DataObject.headerAPDU((byte) 0x00,
                                          USER_OPERATION,
                                          (byte) 0x00,
                                          PRESENT_USER));

        if (securityAttribute==null) {
            // First case: PRESENT USER of UserID only
	    // i.e., Lp 'userID' 
            cmd.append (DataObject.bodyAPDU(userID));
        } else {
            // Second case: PRESENT USER of Card Holder Certificate DataObject
            // i.e., '7F21' TotalLength '5F20' Lp 'userID' Lp SecurityAttribute
            int totalLength = userID.length()
                + securityAttribute.getDataLength() + 4;
            cmd.append (TAG_CH_CERTIFICATE_1);
            cmd.append (TAG_CH_CERTIFICATE_2);
            cmd.append ((byte)totalLength);
            cmd.append (TAG_CH_NAME_1);
            cmd.append (TAG_CH_NAME_2);
            cmd.append (DataObject.bodyAPDU(userID));
            cmd.append (securityAttribute.getBytes());
        }
        sendAPDU (cmd);
    }
    
    public void presentUser(String userID) throws SCQLException {
        presentUser(userID, null);
    }
    
    /**
     * Initiates the registration of a user. In a SCQL environment
     * a row in the user description table is inserted by the card.<BR>
     * This command can only be performed by users with profiles DB_O
     * (i.e., DataBase Owner) or DBOO (i.e., DataBase Object Owner)
     * with the right permissions. The user id has to be unique.
     *
     * @param userid The login name of the presented user id.
     * @param userprofile The profile of the presented user id.
     * @param securityAttribute the security attribute presented
     * for authentication.
     *
     * @see opencard.opt.database.BasicDatabase#presentUser(java.lang.String, SecurityAttribute)
     * @see opencard.opt.database.BasicDatabase#deleteUser(java.lang.String)
     */
    
    public void createUser(String userID,
                           String userProfile,
                           SecurityAttribute securityAttribute)
        throws SCQLException {
        userID.toUpperCase();
        DataObject.isIdentifier(userID);

        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU((byte) 0x00,
                                          USER_OPERATION,
                                          (byte) 0x00,
                                          CREATE_USER));

        cmd.append (DataObject.bodyAPDU(userID));

        if (userProfile.equals("DBOO") ||
            userProfile.equals("DBBU")) {
            cmd.append (DataObject.bodyAPDU(userProfile));
        } else {
            throw new WrongParameterException(userProfile);
        }

        if (securityAttribute!=null) {
            cmd.append (securityAttribute.getBytes());
        }
        sendAPDU (cmd);
    }
    
    /**
     * Allows a user to be deleted. The respective row in the user
     * description table is erased.<BR>
     * This operation can only be performed by the user owner.
     * In order to ensure database integrity, privileges associated
     * to this user should be automatically removed.
     *
     * @param userid The login name of the presented user id.
     *
     * @see opencard.opt.database.BasicDatabase#presentUser(java.lang.String, java.lang.String)
     * @see opencard.opt.database.BasicDatabase#createUser(java.lang.String, java.lang.String, java.lang.String)
    */
    
    public void deleteUser(String userID) throws SCQLException {
        CommandAPDU cmd = new CommandAPDU (MAX_SIZE);

        cmd.append (DataObject.headerAPDU((byte) 0x00,
                                          USER_OPERATION,
                                          (byte) 0x00,
                                          DELETE_USER));
        cmd.append (DataObject.bodyAPDU(userID.toUpperCase()));             
        sendAPDU (cmd);
    }
    
    /**
     * This method is used in this package to manage exceptions.
     * It analyzes the response sent back by the smartcard and raises
     * the appropriate exception.
     *
     * @param adpu The responseAPDU to be analyzed.
     */
    
    protected void throwException(ResponseAPDU apdu) throws SCQLException {
        byte sw1 = apdu.sw1(), sw2 = apdu.sw2();

        switch (sw1) {
        case SW1_WRONG_LENGTH: throw new WrongLengthLeException(sw2);

        case SW1_OK:
            switch (sw2) {
            case SW2_OK : 
                break;
            default:
                throw new SCQLException("Unknown error code : "+((sw1)&0xFF)+" "+((sw2)&0xFF));
            };
        case SW1_OK_WITH_RETURN: {
            break;
        }
        case SW1_WARNING:
            switch(sw2) {
            case SW2_WARNING :
                throw new EndOfTableReachedException();

            default:
                throw new SCQLException("Unknown error code : "+((sw1)&0xFF)+" "+((sw2)&0xFF));
            }
            
        case SW1_EXEC_ERROR:
            switch (sw2) {
            case SW2_EXEC_ERROR:
                throw new SCQLError();
            case SW2_MEM_FAILURE:
                throw new MemoryFailureError();

            default:
                throw new SCQLException("Unknown error code : "+((sw1)&0xFF)+" "+((sw2)&0xFF));
            }
            
        case SW1_CHECK_ERROR:
            
            switch (sw2) {
            case SW2_CHECK_ERROR:
                throw new WrongLengthException();

            default:
                throw new SCQLException("Unknown error code : "+((sw1)&0xFF)+" "+((sw2)&0xFF));
            }

        case SW1_COMMAND_NOT_ALLOWED:
            switch (sw2) {
            case SW2_COMMAND_NOT_ALLOWED:
                throw new CommandNotAllowedException();

            case SW2_SECURITY:
                throw new SecurityStatusNotSatisfiedException();

            case SW2_REQUIRED_OPERATION:
                throw new RequiredPrecedentCommandNotPerformedException();

            default:
                throw new SCQLException("Unknown error code : "+((sw1)&0xFF)+" "+((sw2)&0xFF));
            }
            
        case SW1_WRONG_PARAM:
            switch (sw2) {
            case SW2_WRONG_PARAM:
                throw new WrongParameterException();

            case SW2_INCORRECT_PARAM:
                throw new IncorrectParameterInDataFieldException();

            case SW2_OPERATION_NOT_SUPPORTED:
                throw new OperationNotSupportedException();

            case SW2_OBJECT_NOT_FOUND:
                throw new ReferencedObjectNotFoundException();

            case SW2_OBJECT_ALREADY_EXISTS:
                throw new ObjectAlreadyExistsException();

            default:
                throw new SCQLException("Unknown error code : "+((sw1)&0xFF)+" "+((sw2)&0xFF));
            }
            
        case SW1_INSTRUCTION_CODE_NOT_SUPPORTED:
            switch (sw2) {
            case SW2_INSTRUCTION_CODE_NOT_SUPPORTED:
                throw new InstructionCodeNotSupportedException();

            default:
                throw new SCQLException("Unknown error code : "+((sw1)&0xFF)+" "+((sw2)&0xFF));
            }
            
        default:
            throw new SCQLException("Unknown error code : "+((sw1)&0xFF)+" "+((sw2)&0xFF));
        }
    }
    
    /**
     * This method is used to send APDU commands to the smartcard.
     * 
     * @param cmd The APDU command to be sent.
     * @return The APDU response send back by the smartcard.
     */
    
    protected ResponseAPDU sendAPDU (CommandAPDU cmd) throws SCQLException {
        ResponseAPDU resp = null;
        SmartCard smartcard = null;

        try {
            allocateCardChannel();
            if (cmd.getLength()>5) {
                cmd.setByte (4,cmd.getLength()-5-cmd.getByte(4));
            }
            ctracer.debug("sendAPDU", "-> CommandAPDU : " + cmd.toString());
            resp = getCardChannel().sendCommandAPDU(cmd);
            ctracer.debug("sendAPDU", "<- ResponseAPDU : " + resp.toString());
            releaseCardChannel();
            throwException (resp);
        }

        catch (CardTerminalException e) {
            e.printStackTrace();
        }

        return resp;
    }

    /**
     * A static method to analyze a smartcard in order to
     * determine if it is an SCQL card, i.e., compliant with the
     * ISO7816-7 standard. It is not possible here to analyze only
     * the card ATR because the standard does not specify anything
     * about it. On the contrary the following method is applied:
     * <ol>
     *    <li>a temporary SlotChannel is allocated (using the scheduler),
     *    <li>a 'PRESENT USER ("PUBLIC")' command w/o password is sent, and
     *    <li>the response is analyzed (it should be 9000 for an SCQL card).
     * </ol><br>
     * This method complies with the following OCF naming pattern:<br>
     * "A CardService 'knows' how to recognize supported cards"
     *
     * @param cid         the ATR of the smartcard
     * @param sched       a CardServiceScheduler for temporary communication
     * @return            true or false
     */
    public static boolean knows(CardID cid, CardServiceScheduler sched) {
        boolean isSCQL = false;

        CommandAPDU presentCommand=null;
        ResponseAPDU presentResponse=null;

        presentCommand=new CommandAPDU(13);
        presentCommand.append(DataObject.headerAPDU((byte) 0x00,
                                                    (byte) 0x14,
                                                    (byte) 0x00,
                                                    (byte) 0x80,
                                                    (byte) 8));
        presentCommand.append(DataObject.bodyAPDU("PUBLIC"));
        presentCommand.append ((byte) 0x00);

        // Send 'PRESENT USER' Command
        // (Note: it is not possible to use the "presentUser()" method here
        // because a SlotChannel must be used instead of a CardChannel!)
        try {
            ctracer.debug("knows",
                          "PRESENT USER CommandAPDU : "
                          + presentCommand.toString());
            presentResponse = sched.getSlotChannel().sendAPDU(presentCommand);
            ctracer.debug("knows",
                          "PRESENT USER ResponseAPDU : "
                          + presentResponse.toString());
        } catch (Exception cte) {
            java.lang.System.err.println("Communication Problems During PresentUser.");
        }

        // Analyze response and returns result
        if (((byte)presentResponse.sw1() == (byte)0x90)
            && ((byte)presentResponse.sw2() == (byte)0x00)) {
            isSCQL = true;
            ctracer.info("knows", "DATABASE_CARDTYPE card found!");
        } else {
            isSCQL = false;
            ctracer.info("knows", "Not a DATABASE card.");
        }
        return isSCQL;
    }
}
