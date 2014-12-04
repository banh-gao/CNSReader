package opencard.core.service;

import opencard.core.terminal.CHVControl;
import opencard.core.terminal.CHVEncoder;
import opencard.core.terminal.CardTerminalException;

public class CHVUtils {

	/**
	 * @param args
	 * @throws CardTerminalException 
	 */
	public static byte[] encodeCHV(CHVControl  control, String password) throws CardTerminalException {

		if (!control.passwordEncoding().equals(CHVEncoder.STRING_ENCODING)
				&& !control.passwordEncoding().equals(CHVEncoder.F2B_ENCODING))
			throw new CardTerminalException("verification type not supported: "
					+ control.toString());
		
	
		// ASC: Added support for F2B Format
		byte[] passbytes;
		int length;
		
		if (control.passwordEncoding().equals(CHVEncoder.STRING_ENCODING)) {
		    passbytes = password.getBytes();
			length = control.ioControl().maxInputChars();

			if (passbytes.length < length)
			    length = passbytes.length;
		} else {
		    length = 8;						// F2B has 8 bytes
		    passbytes = new byte[length];

		    int i;
		    
		    for (i = 1; i < length; i++) {	// Preset with FF
		        passbytes[i] = (byte)0xFF;
		    }
		    
		    // Encode up to 14 BCD coded digits
		    for (i = 0; (i < password.length()) && (i < 14); i++) {
		        char ch = password.charAt(i);
		        
		        if ((ch < '0') && (ch > '9')) {
		    	    throw new CardTerminalException("CHV must only contain digits");
		        }
		        
		        if ((i & 1) == 0) {
		            passbytes[1 + (i >> 1)] = (byte)(((ch - '0') << 4) | 0x0F);  
		        } else {
		            passbytes[1 + (i >> 1)] = (byte)(passbytes[1 + (i >> 1)] & 0xF0 | (ch - '0'));
		        }
		    }
		    
		    passbytes[0] = (byte)(0x20 | i);
		}

		return passbytes;
	}

}
