/*
 * Trace Command and Response APDUs
 */
package opencard.core.util;

import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.SlotChannel;

/**
 * Interface used to trace the communication with a smart card
 */
public interface APDUTracer {
	
	/**
	 * Trace a command APDU send over the defined slot channel
	 * 
	 * @param sc the slot channel or null if unknown
	 * @param capdu the command APDU
	 */
	void traceCommandAPDU(SlotChannel sc, CommandAPDU capdu);



	/**
	 * Trace a response APDU received over the defined slot channel
	 * 
	 * @param sc the slot channel or null if unknown
	 * @param rapdu the response APDU
	 */
	void traceResponseAPDU(SlotChannel sc, ResponseAPDU rapdu);
}
