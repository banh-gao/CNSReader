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

import opencard.core.terminal.ResponseAPDU;

public class DataObject implements DatabaseCardService.Constants {
   
    // Additional constants for separator signs
    public final static char INHIBITOR = '/';
    public final static char SEPARATOR = ',';

    protected static void isIdentifier(String s)
	throws WrongIdentifierSyntaxException {
	int i;
	boolean b = true;
	if (s.charAt(0)=='_') {
	    b = false;
	} else {
            for (i=0; (i < s.length()) && (b); i++) {
		if (!(((Character.isUpperCase (s.charAt(i)))
		       && (Character.isLetter (s.charAt(i))))
		      || (Character.isDigit (s.charAt(i)))
		      || (s.charAt(i)=='_'))) {
		    b = false;
		}
	    }
	}
	if (!b) {
	    throw new WrongIdentifierSyntaxException(s);
	}
    }
   
    protected static void isDictionaryIdentifier(String s)
	throws WrongIdentifierSyntaxException {
	isIdentifier (s);
	if (s.length() > 6) {
	    throw new WrongIdentifierSyntaxException(s);
	}
    }
   
    /**
     * Test if the character passed as parameter is an operator of
     * comparison or not. Returns <code>True</code> if the character
     * is an operator and <code>False</code> otherwise.
     * 
     * @param c The character to be tested.
     * @return True or False.
     */
   
    protected static boolean isOperator(char c) {
	return ((c=='<') || (c=='>') || (c=='=') || (c=='!'));
    }
   
    /**
     * This method parses a <code>String</code> describing the privileges
     * and builds a corresponding byte value.<BR>
     * Privileges must be as follow :<BR><UL>
     * <LI> "A" stands for "ALL".</LI>
     * <LI> "I" stands for "INSERT".</LI>
     * <LI> "S" stands for "SELECT".</LI>
     * <LI> "U" stands for "UPDATE".</LI>
     * <LI> "D" stands for "DELETE".</LI>
     *</UL>
     * 
     * @param priv The list of privileges, each privilege is separated from the next one by a coma.
     * @return the byte value of the privileges. 
     */
   
    protected static byte parsePrivilege(String priv) {
	byte inter = 0;
	priv=priv.toUpperCase();
	if (priv.indexOf("A")>-1) {
	    return PRIV_ALL;
	}
	if (priv.indexOf("I")>-1) {
	    inter = (byte) (inter | PRIV_INSERT);
	}
	if (priv.indexOf("S")>-1) {
	    inter = (byte) (inter | PRIV_SELECT);
	}
	if (priv.indexOf("U")>-1) {
	    inter = (byte) (inter | PRIV_UPDATE);
	}
	if (priv.indexOf("D")>-1) {
	    inter = (byte) (inter | PRIV_DELETE);
	}
	return inter;
    }
   
    /**
     * This method converts a string value of an operator
     * into its byte value.<BR>
     * 
     * @param condition The operator to determine the value, coded
     * on 1 or 2 characters.
     * @return The byte value which represents the operator.
     */
   
    protected static byte operator(String condition) {
	condition+=" ";
	char a = condition.charAt (0);
	char b = condition.charAt (1);
      
	switch (a)
	    {
            case '<' : 
		switch (b)
		    {
		    case '=' : 
			return (LESS_THAN_OR_EQUAL_TO);
		    default : 
			return (LESS_THAN);
		    }
            case '>' :
		switch (b)
		    {
		    case '=': 
			return (GREATER_THAN_OR_EQUAL_TO);
		    default : 
			return (GREATER_THAN);
		    }
            case '=': 
		return (EQUAL_TO);
		
            case '!':
		if (b=='=') 
		    return (NOT_EQUAL_TO);
		
            default : 
		return ((byte) 0);
	    }
    }
   
    protected static String[] dictionaryResponseToString(char systemTableType,
							 ResponseAPDU resp) {
	byte[] t = resp.data();
	
	if (systemTableType=='U') {
	    char c=hexToChar(t[1]);
	    String tmp[] = new String[4];
	    int i, l=((int)t[2])+3;
	    switch (c)
		{
		case 'A' : tmp[0]="DB_O";
		case 'E' : tmp[0]="DBBO";
		case 'U' : tmp[0]="DBBU";
		}
	    tmp[1]="";
	    for (i=3;i<l;i++) tmp[1]+=hexToChar(t[i]);
	    int m=(int)t[l++]+l;tmp[2]="";
	    for (i=l;i<m;i++) tmp[2]+=hexToChar(t[i]);
	    tmp[3]="";l=(int)t[++m]+m;m++;
	    for (i=m;i<l;i++) tmp[3]+=hexToChar(t[i]);
	    return tmp;
	}
      
	if (systemTableType=='P') {
	    String tmp[] = new String[4];
	    int i, l=((int)t[2])+3;
	    tmp[0]=""+hexToChar(t[1]);
	    tmp[1]="";
	    for (i=3;i<l;i++) tmp[1]+=hexToChar(t[i]);
	    int m=(int)t[l++]+l;tmp[2]="";
	    for (i=l;i<m;i++) tmp[2]+=hexToChar(t[i]);
	    tmp[3]="";l=(int)t[m++]+m;
	    for (i=m;i<l;i++) tmp[3]+=hexToChar(t[i]);
	    return tmp;
	}
      
	if (systemTableType=='O') {
	    if (resp.data()[0]==(byte) 0x03) {
		String tmp[] = new String[19];
		int i, l=((int)t[2])+3;
		tmp[0]="VIEW";
		tmp[1]="";
		for (i=3;i<l;i++) tmp[1]+=hexToChar(t[i]);
		l++;
		tmp[2]="";
		for (i=l;i<t.length;i++) tmp[2]+=hexToChar(t[i]);
		for (i=3;i<19;i++) tmp[i]="";
		return tmp;
	    }
	    if (resp.data()[0]==(byte) 0x05) {
		String tmp[] = new String[19];
		char c=hexToChar(t[1]);
		switch (c)
		    {
		    case 'V' : tmp[0]="VIEW";
		    case 'T' : tmp[0]="TABLE";
		    }
		int i, l=((int)t[2])+3;
		tmp[1]="";
		for (i=3;i<l;i++) tmp[1]+=hexToChar(t[i]);
		tmp[2]="";
		int m=l+(int)t[l++]+1;
		for (i=l;i<m;i++) tmp[2]+=hexToChar(t[i]);
		tmp[3]="";
		l=m+(int)t[m]+1;
		for (i=m;i<l;i++) tmp[3]+=hexToChar(t[i]);
		int n = (int)t[l++],k;
		for (i=0;i<n;i++) {
		    tmp[i+3]="";
		    m=(int)t[l++]+l;
		    for (k=l;k<m;k++) tmp[i+3]+=hexToChar(t[k]);
		    l=m;
		}
		for (i=m+3;i<19;i++) tmp[i]="";
		return tmp;
	    }
	}
	return null;
    }
    
   
    /**
     * Converts a response APDU sent back by the smartcard into a string.
     * @param resp The response APDU to get the string value of.
     * @return The array of strings that contains the parsed resp parameter.
     */
   
    protected static String[] responseToString(ResponseAPDU resp) {
	byte t[] = resp.data();
	String resparse[] = new String[(byte) t[0]];
	byte i, j=0, p=(byte)(t[1]+2);
	for (i=0;i<((byte) t[0]);i++) resparse[i] = "";
	for (i=2;i<t.length;i++) {
	    if (i!=p) {
		resparse[j]+=hexToChar(t[i]);
	    } else {
		j++;
		p+=(byte)(t[i]+1);
	    }
	}
	return resparse;
    }
   
    /**
     * Allows to parse a string composed of severals parameters
     * into an array of Strings.
     * 
     * @param s The String to be parsed.
     * @return an array which contains the list of the parameters
     * which composed the string s.
     */
   
    protected static String[] parseString(String s) {
	int i=0, j=0;
	String[] tmp = new String[s.length()];
	
	String s_temp = new String("");
	if (s.equals("")) {
	    return new String[0];
	} else {
            for (i=0;i<s.length();i++) {
		if (s.charAt(i)==INHIBITOR) {
		    if (i<s.length()-1) {
			s_temp+=s.charAt(++i);
		    }
		} else {
		    if (s.charAt(i)==SEPARATOR) {
			tmp[j++]=s_temp;
			s_temp="";
		    } else {
			s_temp+=s.charAt(i);
		    }
		}
	    }
	    tmp[j]=s_temp;
	    String[] returnString = new String[++j];
	    for (i=0;i<j;i++) {
		returnString[i] = tmp[i];
	    }
	    return returnString;
	}
    }
    
    /**
     * Transforms a char into its hexadecimal value.<BR>
     * 
     * @param c The character to transform into its hexadecimal value.
     * 
     * @return The Hexadecimal value of the character.
     */
    
    protected static byte charToHex(char c) {
	return (byte)((byte)(c-' ')+0x20);
    }
   
    /**
     * Transforms a hexadecimal value into its char value.<BR>
     * 
     * @param c The hexadecimal value to transform into its character value.
     * 
     * @return The computed character.
     */
   
    protected static char hexToChar(byte b) {
	return (char)((char)(b-0x20)+' ');
    }
    
    /**
     * Builds the body of the APDU command from a String.<BR>
     * 
     * @param c The string used to build the ADPU command.
     * 
     * @return An array of bytes which represents the body of the APDU command.
     */
    
    public static byte[] bodyAPDU(String c) {
	byte[] command = new byte[c.length()+1];
	command[0]=(byte)c.length();
	
	int i;
	for (i=0; i < c.length();i++) {
	    command[i+1] = charToHex(c.charAt(i));
	}
	return command;
    }
    
    /**
     * Builds the body of the APDU command from a byte array.<BR>
     * It is similar to the method using a String.
     * 
     * @param b The byte array used to build the ADPU command.
     * 
     * @return An array of bytes which represents the body of the APDU command.
     */
    
    public static byte[] bodyAPDU(byte b[]) {
	byte[] command = new byte[b.length+1];
	command[0]=(byte)(b.length);
	
	int i;
	for (i=0 ; i < b.length ; i++) {
	    command[i+1] = b[i];
	}
	return command;
    }
    
    /**
     * Builds the header of the APDU command.<BR>
     * 
     * @param CLA Represents the CLA byte value of the APDU command.
     * @param INS Represents the INS byte value of the APDU command.
     * @param P1 Represents the P1 byte value of the APDU command.
     * @param P2 Represents the P2 byte value of the APDU command.
     * 
     * @return An array of bytes which represents the header
     * of the APDU command.
     * 
     * @see opencard.opt.database.DataObject#headerAPDU (byte, byte, byte, byte, byte)
     */
    
    public static byte[] headerAPDU(byte CLA, byte INS, byte P1, byte P2) {
	byte[] command = {CLA, INS, P1, P2, 00};
	return command;
    }
    
    /**
     * Builds the header of the APDU command.<BR>
     * 
     * @param CLA Represents the CLA byte value of the APDU command.
     * @param INS Represents the INS byte value of the APDU command.
     * @param P1 Represents the P1 byte value of the APDU command.
     * @param P2 Represents the P2 byte value of the APDU command.
     * @param le_size Represents the Le byte value of the APDU
     * command, i.e. the expected length of returned data.
     *
     * @return An array of bytes which represents the header
     * of the APDU command.
     * 
     * @see opencard.opt.database.DataObject#headerAPDU (byte, byte, byte, byte)
     */
    
    public static byte[] headerAPDU(byte CLA, byte INS, byte P1, byte P2, byte lc_size) {
	if (lc_size > 0) {
	    byte[] command1 = {CLA, INS, P1, P2, lc_size};
	    return command1;
	} else {
	    byte[] command2 = {CLA, INS, P1, P2};
	    return command2;
	}
    }
}
