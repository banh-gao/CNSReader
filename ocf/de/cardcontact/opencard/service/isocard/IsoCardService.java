/*
 *  ---------
 * |.**> <**.|  CardContact Software & System Consulting
 * |*       *|  32429 Minden, Germany (www.cardcontact.de)
 * |*       *|  Copyright (c) 1999-2004. All rights reserved
 * |'**> <**'|  See file COPYING for details on licensing
 *  --------- 
 *
 * $Log: IsoCardService.java,v $
 * Revision 1.1  2005/09/19 19:22:30  asc
 * Added support for ISO file systems
 *
 * Revision 1.5  2005/09/19 10:18:58  asc
 * Added support for APPEND_RECORD and UPDATE_RECORD
 *
 *
 */

package de.cardcontact.opencard.service.isocard;

import opencard.core.service.CardChannel;
import opencard.core.service.CardService;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceInabilityException;
import opencard.core.service.CardServiceInvalidParameterException;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.InvalidCardChannelException;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CHVControl;
import opencard.core.terminal.CHVEncoder;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CardTerminalIOControl;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.terminal.SlotChannel;
import opencard.core.util.APDUTracer;
import opencard.core.util.Tracer;
import opencard.opt.iso.fs.CardFileAppID;
import opencard.opt.iso.fs.CardFileFileID;
import opencard.opt.iso.fs.CardFileInfo;
import opencard.opt.iso.fs.CardFilePath;
import opencard.opt.iso.fs.CardFilePathComponent;
import opencard.opt.iso.fs.FileAccessCardService;
import opencard.opt.security.CredentialBag;
import opencard.opt.security.SecureService;
import opencard.opt.security.SecurityDomain;
import opencard.opt.service.CardServiceObjectNotAvailableException;
import de.cardcontact.opencard.security.IsoCredentialStore;
import de.cardcontact.opencard.security.SecureChannel;
import de.cardcontact.opencard.security.SecureChannelCredential;
import de.cardcontact.opencard.service.CardServiceUnexpectedStatusWordException;
import de.cardcontact.opencard.service.isocard.CHVCardServiceWithControl.PasswordStatus;


/**
 * File access card service for ISO7816-4,-8 and -9 cards.
 * 
 * First implemented and tested with ORGA Micardo 2.x operating systems
 * 
 * @author Andreas Schwier
 *
 */

public class IsoCardService extends CardService implements FileAccessCardService, IsoFileSystemCardService, CHVCardServiceWithControl, FileSystemSendAPDU, SecureService {
	private final static CardFilePath root_path = new CardFilePath(":3F00");
	private final static Tracer ctracer = new Tracer(IsoCardService.class);

	CardFilePath credentialDomain = null;
	CredentialBag credentialBag;

	/**
	 * Create the IsoCardSelector object in the card channel if it
	 * does not yet exist.
	 * 
	 * Overwrites #opencard.core.service.CardService#initialize
	 */
	public void initialize(CardServiceScheduler scheduler,
			SmartCard smartcard,
			boolean blocking)
	throws CardServiceException {

		super.initialize(scheduler, smartcard, blocking);

		ctracer.debug("initialize", "called");

		try	{
			allocateCardChannel();

			IsoCardSelector cardSelector = (IsoCardSelector)getCardChannel().getState();

			if (cardSelector == null) {
				cardSelector = new IsoCardSelector(getRoot());
				getCardChannel().setState(cardSelector);
			}
		} finally {
			releaseCardChannel();
		}
	}



	/**
	 * Determine if file exists
	 * 
	 * @see opencard.opt.iso.fs.FileAccessCardService#exists(opencard.opt.iso.fs.CardFilePath)
	 */
	public boolean exists(CardFilePath file)
	throws CardServiceException, CardTerminalException {

		try	{
			getFileInfo(file);
		}
		catch(CardServiceObjectNotAvailableException e) {
			return false;
		}
		return true;
	}



	/**
	 * Obtain file information as returned in the SELECT command
	 * 
	 * @see opencard.opt.iso.fs.FileAccessCardService#getFileInfo(opencard.opt.iso.fs.CardFilePath)
	 */
	public CardFileInfo getFileInfo(CardFilePath file)
	throws CardServiceException, CardTerminalException {

		CardFileInfo fci;

		try	{
			allocateCardChannel();
			CardChannel channel = getCardChannel();
			IsoCardSelector cardSelector = (IsoCardSelector)channel.getState();

			cardSelector.selectFile(channel, getSecureChannelCredential(file, IsoCredentialStore.SELECT), file, true);
			fci = cardSelector.getFCI();
		} finally {
			releaseCardChannel();
		}

		return fci;
	}



	/**
	 * Return the root path (:3F00) of this card service
	 * 
	 * @see opencard.opt.iso.fs.FileAccessCardService#getRoot()
	 */
	public CardFilePath getRoot() {
		return root_path;
	}



	/**
	 * Obtain a secure channel credential, if any is defined for the given file and access mode
	 * 
	 * @param file File for which a secure channel credential should be obtained
	 * 
	 * @param accessMode Desired mode of access (READ, UPDATE or APPEND)
	 *  
	 * @return null or SecureChannelCredential object
	 */
	protected SecureChannelCredential getSecureChannelCredential(CardFilePath file, int accessMode) {
		SecureChannelCredential secureChannelCredential = null;

		//      if ((credentialDomain != null) && (file.startsWith(credentialDomain))) {
		if (credentialDomain != null) {
			IsoCredentialStore ics = (IsoCredentialStore)credentialBag.getCredentialStore(null, IsoCredentialStore.class);

			if (ics != null) {
				secureChannelCredential = ics.getSecureChannelCredential(file, accessMode);
			}
		}
		return(secureChannelCredential);
	}



	/**
	 * Exchange APDU with card, optionally transforming the APDU with a secure channel
	 * 
	 * @param channel
	 * @param secureChannelCredential
	 * @param uq usage Qualifier
	 * @param com
	 * @return the response APDU
	 * @throws InvalidCardChannelException
	 * @throws CardTerminalException
	 */
	protected ResponseAPDU sendCommandAPDU(CardChannel channel, SecureChannelCredential secureChannelCredential, int uq, CommandAPDU com) throws InvalidCardChannelException, CardTerminalException {
		ResponseAPDU res;

		// Transport command and response APDU using the secure channel
		if (secureChannelCredential != null) {
			SlotChannel slc = channel.getSlotChannel();
			APDUTracer tracer = slc.getAPDUTracer();
			if ((tracer != null) && (com.getLength() > 5)) {
				tracer.traceCommandAPDU(slc, com);
			}
			//            int uq = secureChannelCredential.getUsageQualifier();
			SecureChannel sc = secureChannelCredential.getSecureChannel();
			com = sc.wrap(com, uq);
			res = channel.sendCommandAPDU(com);
			res = sc.unwrap(res, uq);
			if ((tracer != null) && (res.getLength() > 2)) {
				tracer.traceResponseAPDU(slc, res);
			}
		} else {
			res = channel.sendCommandAPDU(com);
		}

		return res;
	}



	/**
	 * Exchange APDU with card, optionally transforming the APDU with a secure channel
	 * 
	 * @param channel
	 * @param secureChannelCredential
	 * @param com
	 * @return the response APDU
	 * @throws InvalidCardChannelException
	 * @throws CardTerminalException
	 */
	protected ResponseAPDU sendCommandAPDU(CardChannel channel, SecureChannelCredential secureChannelCredential, CommandAPDU com) throws InvalidCardChannelException, CardTerminalException {
		int uq = 0;
		if (secureChannelCredential != null) {
			uq = secureChannelCredential.getUsageQualifier();
		}
		return sendCommandAPDU(channel, secureChannelCredential, uq, com);
	}



	/**
	 * Read binary data from transparent file
	 *
	 * @see opencard.opt.iso.fs.FileAccessCardService#read(opencard.opt.iso.fs.CardFilePath, int, int)
	 */
	public byte[] read(CardFilePath file, int offset, int length)
	throws CardServiceException, CardTerminalException {

		CardChannel channel;
		CommandAPDU com = new CommandAPDU(5);
		ResponseAPDU res;
		int remaining, expected, maxapdulen;
		byte[] response = null;

		// Check parameter
		if ((offset < 0) || (offset > 0x7FFF) || ((length != READ_SEVERAL) && (length < 0))) {
			throw new CardServiceInvalidParameterException
			("read: offset = " + offset + ", length = " + length);
		}

		// Obtain secure channel, if any is specified for this object and access method
		SecureChannelCredential secureChannelCredential = getSecureChannelCredential(file, IsoCredentialStore.READ);

		try	{
			allocateCardChannel();
			channel = getCardChannel();
			IsoCardSelector ics = (IsoCardSelector)channel.getState();

			// Select the object in question, if not already selected
			// Returns short file identifier, if one is specified
			int sfi = ics.selectFile(channel, getSecureChannelCredential(file, IsoCredentialStore.SELECT), file); 

			// With SFI we can not use the full offset range
			if ((sfi > 0) && (offset > 255)) {
				throw new CardServiceInvalidParameterException
				("read: offset = " + offset + " out of range when reading with short file identifier");
			}

			if (length == READ_SEVERAL) {
				remaining = 0x8000;
			} else {
				remaining = length;
			}

			// When a secureChannel is defined, then we restrict the
			// maximum data read to 223. This allows for secure messaging
			// objects to be included in the response buffer.
			// T87(4) + data(223) + pad(1) + T99(4) + T8E(10) = 242
			maxapdulen = (secureChannelCredential == null ? 256 : 223);

			// Try and read everything
			while (remaining > 0) {
				com.setLength(0);
				com.append(IsoConstants.CLA_ISO);
				com.append(IsoConstants.INS_READ_BINARY);

				// Encode short file identifier
				if (sfi > 0) {
					com.append((byte)(0x80 | sfi));
				} else {
					com.append((byte)(offset >> 8));
				}

				com.append((byte)offset);

				if (length == READ_SEVERAL) {
					expected = maxapdulen;
				} else {
					expected = remaining > maxapdulen ? maxapdulen : remaining;
				}

				// The (byte) cast transforms 256 into 0
				com.append((byte)expected);

				res = sendCommandAPDU(channel, secureChannelCredential, com);

				if ((res.sw() == IsoConstants.RC_WRONGLENGTH) && (expected == 256)) {
					// Card does not support Le=0 / ACOS EMV03
					expected = 255;
					maxapdulen = 255;
					com.setLength(4);
					com.append((byte)expected);
					res = channel.sendCommandAPDU(com);
				}

				// Resend if card specifies a Le value 
				if (res.sw1() == IsoConstants.RC_INVLE) {
					expected = res.sw2() & 0xFF; 
					com.setLength(4);
					com.append(res.sw2());
					res = channel.sendCommandAPDU(com);
				}

				/* This handles a special case, when we read with READ_SEVERAL
				 * and read exactly to the end of file (e.g. file size is multiple
				 * of 256. In that case the offset moves exactly one byte
				 * after the end of file.
				 * As a side effect this return 0 bytes if the offset was
				 * behind the file in the first read.
				 */
				if (((res.sw() == IsoConstants.RC_INVP1P2) || (res.sw() == IsoConstants.RC_INCP1P2)) && (length == READ_SEVERAL)) {
					break;
				}

				if ((res.sw() == IsoConstants.RC_OK) || (res.sw() == IsoConstants.RC_EOF)) {
					// If we were successfull reading with a short file identifier, then
					// this file will become the currently selected file
					if (sfi > 0) {
						ics.setImplicitlySelectedBySFI(file);
						sfi = 0;
					}

					int len = res.getLength() - 2;		/* Ignore SW1/SW2 */

					if (response == null) {
						response = new byte[len];
						System.arraycopy(res.getBuffer(), 0, response, 0, len);
					} else {
						byte[] buff = new byte[response.length + len];
						System.arraycopy(response, 0, buff, 0, response.length);
						System.arraycopy(res.getBuffer(), 0, buff, response.length, len);
						response = buff;
					}
					offset += len;
					remaining -= len;
					if ((res.sw() == IsoConstants.RC_EOF) || (len < expected))
						break;
				} else {
					throw new CardServiceUnexpectedStatusWordException("READ_BINARY", res.sw());
				}
			}
		} finally {
			releaseCardChannel();
		}

		if (response == null) {
			response = new byte[0];
		}
		return response;
	}



	/**
	 * Read record from linear file
	 * 
	 * @see opencard.opt.iso.fs.FileAccessCardService#readRecord(opencard.opt.iso.fs.CardFilePath, int)
	 */
	public byte[] readRecord(CardFilePath file, int recordNumber)
	throws CardServiceException, CardTerminalException {

		CardChannel channel;
		CommandAPDU com = new CommandAPDU(5);
		ResponseAPDU res = new ResponseAPDU(258);
		byte[] response = null;

		// Obtain secure channel, if any is specified for this object and access method
		SecureChannelCredential secureChannelCredential = getSecureChannelCredential(file, IsoCredentialStore.READ);

		try	{
			allocateCardChannel();
			channel = getCardChannel();
			IsoCardSelector ics = (IsoCardSelector)channel.getState();

			// Select the object in question, if not already selected
			// Returns short file identifier, if one is specified
			int sfi = ics.selectFile(channel, getSecureChannelCredential(file, IsoCredentialStore.SELECT), file); 

			com.setLength(0);
			com.append(IsoConstants.CLA_ISO);
			com.append(IsoConstants.INS_READ_RECORD);
			com.append((byte)recordNumber);
			if (sfi > 0) {
				com.append((byte)((sfi << 3) + 4));
			} else {
				com.append((byte)0x04);
			}
			com.append((byte)0x00);

			res = sendCommandAPDU(channel, secureChannelCredential, com);

			if (res.sw() != IsoConstants.RC_OK) {
				throw new CardServiceUnexpectedStatusWordException("READ_RECORD", res.sw());
			}

			// If we were successfull reading with a short file identifier, then
			// this file will become the currently selected file
			if (sfi > 0) {
				ics.setImplicitlySelectedBySFI(file);
				sfi = 0;
			}

			response = new byte[res.data().length];
			System.arraycopy(res.data(), 0, response, 0, res.data().length);

		} finally {
			releaseCardChannel();
		}

		return response;
	}



	/* (non-Javadoc)
	 * @see opencard.opt.iso.fs.FileAccessCardService#readRecords(opencard.opt.iso.fs.CardFilePath, int)
	 */
	public byte[][] readRecords(CardFilePath file, int number)
	throws CardServiceException, CardTerminalException {
		// TODO Auto-generated method stub
		return null;
	}



	/**
	 * Write binary data to transparent file
	 * 
	 * @see opencard.opt.iso.fs.FileAccessCardService#write(opencard.opt.iso.fs.CardFilePath, int, byte[], int, int)
	 */
	public void write(
			CardFilePath file,
			int foffset,
			byte[] source,
			int soffset,
			int length)
	throws CardServiceException, CardTerminalException {

		CardChannel channel;
		CommandAPDU com = new CommandAPDU(261);
		ResponseAPDU res = new ResponseAPDU(2);
		int lc;

		if ((foffset < 0) || (foffset > 0x7FFF) || (length < 0)) {
			throw new CardServiceInvalidParameterException
			("write: offset = " + foffset + ", length = " + length);
		}

		// Obtain secure channel, if any is specified for this object and access method
		SecureChannelCredential secureChannelCredential = getSecureChannelCredential(file, IsoCredentialStore.UPDATE);

		try	{
			allocateCardChannel();
			channel = getCardChannel();
			IsoCardSelector ics = (IsoCardSelector)channel.getState();

			// Select the object in question, if not already selected
			// Returns short file identifier, if one is specified
			int sfi = ics.selectFile(channel, getSecureChannelCredential(file, IsoCredentialStore.SELECT), file); 

			if ((sfi > 0) && (foffset > 255)) {
				throw new CardServiceInvalidParameterException
				("write: offset = " + foffset + " out of range when writing with short file identifier");
			}

			while (length > 0) {
				lc = length > 220 ? 220 : length;

				com.setLength(0);
				com.append(IsoConstants.CLA_ISO);
				com.append(IsoConstants.INS_UPDATE_BINARY);

				if (sfi > 0) {
					com.append((byte)(0x80 | sfi));
				} else {
					com.append((byte)(foffset >> 8));
				}

				com.append((byte)foffset);
				com.append((byte)lc);
				System.arraycopy(source, soffset, com.getBuffer(), 5, lc);
				com.setLength(5 + lc);

				res = sendCommandAPDU(channel, secureChannelCredential, com);

				if (res.sw() != IsoConstants.RC_OK) {
					throw new CardServiceUnexpectedStatusWordException("UPDATE_BINARY" ,res.sw());
				}

				if (sfi > 0) {
					ics.setImplicitlySelectedBySFI(file);
					sfi = 0;
				}

				foffset += lc;
				soffset += lc;
				length -= lc;
			}
		} finally {
			releaseCardChannel();
		}
	}



	/**
	 * Write binary data to transparent file
	 *
	 * @see opencard.opt.iso.fs.FileAccessCardService#write(opencard.opt.iso.fs.CardFilePath, int, byte[])
	 */
	public void write(CardFilePath file, int offset, byte[] data)
	throws CardServiceException, CardTerminalException {

		write(file, offset, data, 0, data.length);
	}



	/**
	 * Update record in linear file
	 * 
	 * @see opencard.opt.iso.fs.FileAccessCardService#writeRecord(opencard.opt.iso.fs.CardFilePath, int, byte[])
	 */
	public void writeRecord(CardFilePath file, int recordNumber, byte[] data)
	throws CardServiceException, CardTerminalException {

		CardChannel channel;
		CommandAPDU com = new CommandAPDU(261);
		ResponseAPDU res = new ResponseAPDU(2);

		if ((recordNumber < 0) || (recordNumber > 254)) {
			throw new CardServiceInvalidParameterException
			("writeRecord: recordNumber = " + recordNumber);
		}

		if (data.length > 255) {
			throw new CardServiceInvalidParameterException
			("writeRecord: length of data = " + data.length);
		}

		// Obtain secure channel, if any is specified for this object and access method
		SecureChannelCredential secureChannelCredential = getSecureChannelCredential(file, IsoCredentialStore.UPDATE);

		try	{
			allocateCardChannel();
			channel = getCardChannel();
			IsoCardSelector ics = (IsoCardSelector)channel.getState();

			// Select the object in question, if not already selected
			// Returns short file identifier, if one is specified
			int sfi = ics.selectFile(channel, getSecureChannelCredential(file, IsoCredentialStore.SELECT), file); 

			com.setLength(0);
			com.append(IsoConstants.CLA_ISO);
			com.append(IsoConstants.INS_UPDATE_RECORD);

			com.append((byte)(recordNumber + 1));
			if (sfi > 0) {
				com.append((byte)((sfi << 3) + 4));
			} else {
				com.append((byte)0x04);
			}

			com.append((byte)data.length);
			System.arraycopy(data, 0, com.getBuffer(), 5, data.length);
			com.setLength(5 + data.length);

			res = sendCommandAPDU(channel, secureChannelCredential, com);

			if (res.sw() != IsoConstants.RC_OK) {
				throw new CardServiceUnexpectedStatusWordException("UPDATE_RECORD" ,res.sw());
			}

			if (sfi > 0) {
				ics.setImplicitlySelectedBySFI(file);
				sfi = 0;
			}
		} finally {
			releaseCardChannel();
		}
	}



	/**
	 * Append record to linear file
	 * 
	 * @see opencard.opt.iso.fs.FileAccessCardService#appendRecord(opencard.opt.iso.fs.CardFilePath, byte[])
	 */
	public void appendRecord(CardFilePath file, byte[] data)
	throws CardServiceException, CardTerminalException {

		CardChannel channel;
		CommandAPDU com = new CommandAPDU(261);
		ResponseAPDU res = new ResponseAPDU(2);

		if (data.length > 255) {
			throw new CardServiceInvalidParameterException
			("appendRecord: length of data = " + data.length);
		}

		// Obtain secure channel, if any is specified for this object and access method
		SecureChannelCredential secureChannelCredential = getSecureChannelCredential(file, IsoCredentialStore.APPEND);

		try	{
			allocateCardChannel();
			channel = getCardChannel();
			IsoCardSelector ics = (IsoCardSelector)channel.getState();

			// Select the object in question, if not already selected
			// Returns short file identifier, if one is specified
			int sfi = ics.selectFile(channel, getSecureChannelCredential(file, IsoCredentialStore.SELECT), file); 

			com.setLength(0);
			com.append(IsoConstants.CLA_ISO);
			com.append(IsoConstants.INS_APPEND_RECORD);

			com.append((byte)0x00);
			if (sfi > 0) {
				com.append((byte)(sfi << 3));
			} else {
				com.append((byte)0x00);
			}

			com.append((byte)data.length);
			System.arraycopy(data, 0, com.getBuffer(), 5, data.length);
			com.setLength(5 + data.length);

			res = sendCommandAPDU(channel, secureChannelCredential, com);

			if (res.sw() != IsoConstants.RC_OK) {
				throw new CardServiceUnexpectedStatusWordException("APPEND_RECORD", res.sw());
			}

			if (sfi > 0) {
				ics.setImplicitlySelectedBySFI(file);
				sfi = 0;
			}
		} finally {
			releaseCardChannel();
		}
	}



	/**
	 * Send APDU making sure that the object referenced by path is selected
	 * 
	 * @param path the DF which should be the active DF for this APDU
	 * @param com the command APDU
	 * @param usageQualifier a combination of SecureChannel.CPRO / CENC / RPRO / RENC to control the transformation of the APDU
	 *                       for secure messaging. Use 0 for plain transmission.
	 * @return Response APDU the response from the card
	 * @throws CardServiceException
	 * @throws CardTerminalException
	 */
	@Override
	public ResponseAPDU sendCommandAPDU(CardFilePath path, CommandAPDU com, int usageQualifier)
	throws CardServiceException, CardTerminalException {

		CardChannel channel;
		ResponseAPDU res = null;

		// Obtain secure channel, if any is specified for this object and access method
		SecureChannelCredential secureChannelCredential = getSecureChannelCredential(path, IsoCredentialStore.SELECT);

		try {
			allocateCardChannel();
			channel = getCardChannel();
			IsoCardSelector ics = (IsoCardSelector)channel.getState();

			// Select the object in question, if not already selected
			// Returns short file identifier, if one is specified
			int sfi = ics.selectFile(channel, secureChannelCredential, path); 

			if (sfi > 0) {
				throw new CardServiceInvalidParameterException("Can't send APDU to file referenced by short file identifier");
			}

			if (com != null) {
				res = sendCommandAPDU(channel, secureChannelCredential, usageQualifier, com);
			}

		} finally {
			releaseCardChannel();
		}
		return res;
	}



	/* (non-Javadoc)
	 * @see opencard.opt.security.SecureService#provideCredentials(opencard.opt.security.SecurityDomain, opencard.opt.security.CredentialBag)
	 */
	public void provideCredentials(SecurityDomain domain, CredentialBag creds) throws CardServiceException {
		if (!(domain instanceof CardFilePath)) {
			throw new CardServiceInvalidParameterException("domain must be of class CardFilePath");
		}

		this.credentialDomain = (CardFilePath)domain;
		this.credentialBag = creds;
	}



	/* (non-Javadoc)
	 * @see opencard.opt.security.CHVCardService#getPasswordLength(opencard.opt.security.SecurityDomain, int)
	 */
	public int getPasswordLength(SecurityDomain domain, int number) throws CardServiceException, CardTerminalException {
		// TODO Auto-generated method stub
		return 0;
	}



	/*
	 * @see opencard.opt.security.CHVCardService#verifyPassword(opencard.opt.security.SecurityDomain, int, byte[])
	 */
	public boolean verifyPassword(SecurityDomain domain, int number, CHVControl cc, byte[] password) throws CardServiceException, CardTerminalException {

		boolean result = false;
		CardChannel channel;
		CommandAPDU com = new CommandAPDU(40);
		ResponseAPDU res = new ResponseAPDU(2);

		try	{
			allocateCardChannel();

			channel = getCardChannel();

			/*
			if (domain != null) {
			    CardFilePath path = (CardFilePath)domain;
			    if (selectFile(channel, path) == null)
					throw new CardIOException("File not found");
			}
			 */

			com.setLength(0);
			com.append(IsoConstants.CLA_ISO);
			com.append(IsoConstants.INS_VERIFY);
			com.append((byte)0);
			com.append(domain == null ? (byte)number : (byte)(number + 0x80));

			if (password != null) {
				com.append((byte)password.length);
				System.arraycopy(password, 0, com.getBuffer(), 5, password.length);
				com.setLength(5 + password.length);

				res = channel.sendCommandAPDU(com);
			} else {
				if (cc.passwordEncoding().equals(CHVEncoder.F2B_ENCODING)) {
					com.append((byte)8);
					com.append(new byte[] { 0x20, (byte)0xFF, (byte)0xFF, (byte)0xFF,
							(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF });

					res = channel.sendVerifiedAPDU(com, cc, getCHVDialog());

				} else {
					// BCD or ASCII encoding
					com.append((byte)8);
					com.append(new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
							(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF });

					res = channel.sendVerifiedAPDU(com, cc, getCHVDialog());
				}
			}

			if (res.sw() == IsoConstants.RC_OK) {
				result = true;
			} else if ((res.sw() & 0xFFF0) == IsoConstants.RC_WARNING0LEFT) {
				result = false;
			} else {
				throw new CardServiceUnexpectedStatusWordException("VERIFY" ,res.sw());
			}
		} finally {
			releaseCardChannel();
		}

		return result;
	}



	@Override
	public PasswordStatus getPasswordStatus(SecurityDomain domain, int number)
			throws CardServiceException, CardTerminalException {
		PasswordStatus status;
		CardChannel channel;
		CommandAPDU com = new CommandAPDU(4);
		ResponseAPDU res = new ResponseAPDU(2);

		try	{
			allocateCardChannel();

			channel = getCardChannel();

			com.append(IsoConstants.CLA_ISO);
			com.append(IsoConstants.INS_VERIFY);
			com.append((byte)0);
			com.append(domain == null ? (byte)number : (byte)(number + 0x80));

			res = channel.sendCommandAPDU(com);

			if (res.sw() == IsoConstants.RC_OK) {
				status = PasswordStatus.VERIFIED;
			} else if (res.sw() == IsoConstants.RC_WARNING1LEFT) {
				status = PasswordStatus.LASTTRY;
			} else if (res.sw() == IsoConstants.RC_WARNING2LEFT) {
				status = PasswordStatus.RETRYCOUNTERLOW;
			} else if ((res.sw() & 0xFFF0) == IsoConstants.RC_WARNING0LEFT) {
				status = PasswordStatus.NOTVERIFIED;
			} else if (res.sw() == IsoConstants.RC_AUTHMETHLOCKED) {
				status = PasswordStatus.BLOCKED;
			} else if (res.sw() == IsoConstants.RC_REFDATANOTUSABLE) {
				status = PasswordStatus.NOTINITIALIZED;
			} else {
				throw new CardServiceUnexpectedStatusWordException("VERIFY" ,res.sw());
			}
		} finally {
			releaseCardChannel();
		}
		
		return status;
	}



	/* (non-Javadoc)
	 * @see opencard.opt.security.CHVCardService#verifyPassword(opencard.opt.security.SecurityDomain, int, byte[])
	 */
	public boolean verifyPassword(SecurityDomain domain, int number, byte[] password) throws CardServiceException, CardTerminalException {
		CardTerminalIOControl ioctl = 
			new CardTerminalIOControl(8, 30, CardTerminalIOControl.IS_NUMBERS, "" );
		CHVControl cc =
			new CHVControl( "Enter your password", number, CHVEncoder.F2B_ENCODING, 0, ioctl);
		return verifyPassword(domain, number, cc, password);
	}



	/* (non-Javadoc)
	 * @see opencard.opt.security.CHVCardService#closeApplication(opencard.opt.security.SecurityDomain)
	 */
	public void closeApplication(SecurityDomain domain) throws CardServiceException, CardTerminalException {
		// TODO Auto-generated method stub

	}



	/**
	 * Create file in parent usind file information supplied as byte array
	 * 
	 * This is the original signature defined by OCF 
	 */
	public void create(CardFilePath parent, byte[] data) throws CardServiceException, CardTerminalException {
		create(parent, (byte)0, (byte)0, data);
	}



	/**
	 * Create file in parent usind file information supplied as byte array
	 * 
	 * This is the signature defined by OpenSCPD
	 */
	public void create(CardFilePath parent, byte fileDescriptorByte, byte shortFileIdentifier, byte[] data) throws CardServiceException, CardTerminalException {
		CardChannel channel;
		CommandAPDU com = new CommandAPDU(261);
		ResponseAPDU res = new ResponseAPDU(2);

		if (data.length > 255) {
			throw new CardServiceInvalidParameterException
			("create: length of data = " + data.length);
		}

		// Obtain secure channel, if any is specified for this object and access method
		SecureChannelCredential secureChannelCredential = getSecureChannelCredential(parent, IsoCredentialStore.CREATE);

		try {
			allocateCardChannel();
			channel = getCardChannel();
			IsoCardSelector ics = (IsoCardSelector)channel.getState();

			// Select the object in question, if not already selected
			// Returns short file identifier, if one is specified
			int sfi = ics.selectFile(channel, getSecureChannelCredential(parent, IsoCredentialStore.SELECT), parent); 

			if (sfi > 0) {
				throw new CardServiceInvalidParameterException("Can't create file in parent referenced by short file identifier");
			}

			com.setLength(0);
			com.append(IsoConstants.CLA_ISO);
			com.append(IsoConstants.INS_CREATE_FILE);
			com.append(fileDescriptorByte);
			com.append(shortFileIdentifier);

			com.append((byte)data.length);
			System.arraycopy(data, 0, com.getBuffer(), 5, data.length);
			com.setLength(5 + data.length);

			res = sendCommandAPDU(channel, secureChannelCredential, com);

			if (res.sw() != IsoConstants.RC_OK) {
				throw new CardServiceUnexpectedStatusWordException("CREATE_FILE", res.sw());
			}
		} finally {
			releaseCardChannel();
		}
	}



	/**
	 * Delete the file referenced
	 * 
	 * This is the original signature defined by OCF
	 */
	public void delete(CardFilePath file) throws CardServiceException, CardTerminalException {
		delete(file, null, false);
	}



	/**
	 * Delete the referenced file
	 * 
	 * This is the signature defined by OpenSCDP
	 * 
	 */
	public void delete(CardFilePath file, CardFilePathComponent child, boolean childIsDF) throws CardServiceException, CardTerminalException {
		CardChannel channel;
		CommandAPDU com = new CommandAPDU(261);
		ResponseAPDU res = new ResponseAPDU(2);

		// Obtain secure channel, if any is specified for this object and access method
		SecureChannelCredential secureChannelCredential = getSecureChannelCredential(file, IsoCredentialStore.DELETE);

		byte[] data = null;
		if (child != null) {
			if (child instanceof CardFileAppID) {
				data = ((CardFileAppID)child).toByteArray();
			} else if (child instanceof CardFileFileID) {
				data = ((CardFileFileID)child).toByteArray();
			} else {
				throw new CardServiceInvalidParameterException("Child argument must be of type CardFileAppID or CardFileFileID");
			}
		}
		try {
			allocateCardChannel();
			channel = getCardChannel();
			IsoCardSelector ics = (IsoCardSelector)channel.getState();

			// Select the object in question, if not already selected
			// Returns short file identifier, if one is specified
			int sfi = ics.selectFile(channel, getSecureChannelCredential(file, IsoCredentialStore.SELECT), file); 

			if (sfi > 0) {
				throw new CardServiceInvalidParameterException("Can't delete file referenced by short file identifier");
			}

			com.setLength(0);
			com.append(IsoConstants.CLA_ISO);
			com.append(IsoConstants.INS_DELETE_FILE);
			if (child != null) {
				com.append(childIsDF ? (byte)0x01 : (byte)0x02);
			} else {
				com.append((byte)0x00);
			}
			com.append((byte)0x00);

			if (child != null) {
				com.append((byte)data.length);
				com.append(data);
			}
			res = sendCommandAPDU(channel, secureChannelCredential, com);

			if (res.sw() != IsoConstants.RC_OK) {
				throw new CardServiceUnexpectedStatusWordException("DELETE_FILE", res.sw());
			}
		} finally {
			releaseCardChannel();
		}
	}



	/**
	 * Invalidate (Deactivate) the file specified
	 */
	public void invalidate(CardFilePath file) throws CardServiceInabilityException, CardServiceException, CardTerminalException {
		CardChannel channel;
		CommandAPDU com = new CommandAPDU(261);
		ResponseAPDU res = new ResponseAPDU(2);

		// Obtain secure channel, if any is specified for this object and access method
		SecureChannelCredential secureChannelCredential = getSecureChannelCredential(file, IsoCredentialStore.DEACTIVATE);

		try {
			allocateCardChannel();
			channel = getCardChannel();
			IsoCardSelector ics = (IsoCardSelector)channel.getState();

			// Select the object in question, if not already selected
			// Returns short file identifier, if one is specified
			int sfi = ics.selectFile(channel, getSecureChannelCredential(file, IsoCredentialStore.SELECT), file, true); 

			if (sfi > 0) {
				throw new CardServiceInvalidParameterException("Can't deactivate file referenced by short file identifier");
			}

			com.setLength(0);
			com.append(IsoConstants.CLA_ISO);
			com.append(IsoConstants.INS_DEACTIVATE_FILE);
			com.append((byte)0x00);
			com.append((byte)0x00);

			res = sendCommandAPDU(channel, secureChannelCredential, com);

			if (res.sw() != IsoConstants.RC_OK) {
				throw new CardServiceUnexpectedStatusWordException("DEACTIVATE_FILE", res.sw());
			}
		} finally {
			releaseCardChannel();
		}
	}



	/**
	 * Rehabilitate (Activate) the file specified
	 * 
	 */
	public void rehabilitate(CardFilePath file) throws CardServiceInabilityException, CardServiceException, CardTerminalException {
		CardChannel channel;
		CommandAPDU com = new CommandAPDU(261);
		ResponseAPDU res = new ResponseAPDU(2);

		// Obtain secure channel, if any is specified for this object and access method
		SecureChannelCredential secureChannelCredential = getSecureChannelCredential(file, IsoCredentialStore.ACTIVATE);

		try {
			allocateCardChannel();
			channel = getCardChannel();
			IsoCardSelector ics = (IsoCardSelector)channel.getState();

			// Select the object in question, if not already selected
			// Returns short file identifier, if one is specified
			int sfi = ics.selectFile(channel, getSecureChannelCredential(file, IsoCredentialStore.SELECT), file, true); 

			if (sfi > 0) {
				throw new CardServiceInvalidParameterException("Can't activate file referenced by short file identifier");
			}

			com.setLength(0);
			com.append(IsoConstants.CLA_ISO);
			com.append(IsoConstants.INS_ACTIVATE_FILE);
			com.append((byte)0x00);
			com.append((byte)0x00);

			res = sendCommandAPDU(channel, secureChannelCredential, com);

			if (res.sw() != IsoConstants.RC_OK) {
				throw new CardServiceUnexpectedStatusWordException("ACTIVATE_FILE", res.sw());
			}
		} finally {
			releaseCardChannel();
		}
	}
}
