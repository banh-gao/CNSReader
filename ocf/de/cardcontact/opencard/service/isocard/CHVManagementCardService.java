package de.cardcontact.opencard.service.isocard;

import opencard.core.service.CardServiceException;
import opencard.core.terminal.CHVControl;
import opencard.core.terminal.CardTerminalException;
import opencard.opt.security.SecurityDomain;
import opencard.opt.service.CardServiceInterface;

public interface CHVManagementCardService extends CardServiceInterface {
	
	public boolean changeReferenceData(SecurityDomain domain, int number, CHVControl cc, byte[] currentPassword, byte[] newPassword)
			throws CardTerminalException, CardServiceException;

	public boolean resetRetryCounter(SecurityDomain domain, int number, CHVControl cc, byte[] unblockingCode, byte[] newPassword)
			throws CardTerminalException, CardServiceException;
}
