/*
 *  ---------
 * |.**> <**.|  CardContact Software & System Consulting
 * |*       *|  32429 Minden, Germany (www.cardcontact.de)
 * |*       *|  Copyright (c) 1999-2004. All rights reserved
 * |'**> <**'|  See file COPYING for details on licensing
 *  --------- 
 *
 * $Log: ISSSSignatureService.java,v $
 * Revision 1.1  2005/09/19 19:22:30  asc
 * Added support for ISO file systems
 *
 * Revision 1.2  2005/02/11 13:25:19  asc
 * Added support for Starcos signature operations
 *
 * Revision 1.1  2004/05/17 15:40:05  asc
 * Added SV-Signature viewer and associated functions
 *
 *
 */

package de.cardcontact.opencard.service.signature;

import java.security.InvalidKeyException;

import opencard.core.service.CardChannel;
import opencard.core.service.CardService;
import opencard.core.service.CardServiceException;
import opencard.core.service.CardServiceOperationFailedException;
import opencard.core.service.CardServiceScheduler;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.HexString;
import opencard.core.util.Tracer;
import opencard.opt.security.CredentialBag;
import opencard.opt.security.PrivateKeyFile;
import opencard.opt.security.PrivateKeyRef;
import opencard.opt.security.PublicKeyRef;
import opencard.opt.security.SecurityDomain;
import opencard.opt.signature.SignatureCardService;
import de.cardcontact.opencard.service.isocard.IsoCardState;
import de.cardcontact.opencard.service.isocard.IsoConstants;

/**
 * 
 * @author Andreas Schwier (info@cardcontact.de)
 * @version $Id: ISSSSignatureService.java,v 1.1 2005/09/19 19:22:30 asc Exp $
 */
public class ISSSSignatureService extends CardService implements SignatureCardService {
	private final static Tracer ctracer = new Tracer(ISSSSignatureService.class);


	/**
	 * Instantiate ISSSSignatureService
	 * 
	 * Initialization is done in #ISSSSignatureService.initialize
	 *
	 */
	public ISSSSignatureService() {
	}

	/**
	 * Check that the IsoCardState object exists in the card channel
	 * 
	 * Overwrites #opencard.core.service.CardService#initialize
	 */
	protected void initialize(CardServiceScheduler scheduler,
							  SmartCard smartcard,
							  boolean blocking)
					   throws CardServiceException {
	
		super.initialize(scheduler, smartcard, blocking);

		ctracer.debug("initialize", "called");

		try	{
			allocateCardChannel();
			
			IsoCardState cardState = (IsoCardState)getCardChannel().getState();
			
			if (cardState == null) {
				throw new CardServiceException("SignatureCardService requires open card channel");
			}
		} finally {
			releaseCardChannel();
		}
	}


	/**
	 * Implement signData
	 * 
	 * @see opencard.opt.signature.SignatureCardService#signData(opencard.opt.security.PrivateKeyRef, java.lang.String, byte[])
	 */
	 
	public byte[] signData(PrivateKeyRef privateKey, String signAlgorithm, byte[] data) throws CardServiceException, InvalidKeyException, CardTerminalException {
		return signData(privateKey, signAlgorithm, "", data);
	}

	
	
	/**
	 * Implement signData
	 * 
	 * @see opencard.opt.signature.SignatureCardService#signData(opencard.opt.security.PrivateKeyRef, java.lang.String, java.lang.String, byte[])
	 */
	public byte[] signData(PrivateKeyRef privateKey, String signAlgorithm, String padAlgorithm, byte[] data) throws CardServiceException, InvalidKeyException, CardTerminalException {
		CardChannel channel;
		CommandAPDU com = new CommandAPDU(256);
		ResponseAPDU res = new ResponseAPDU(258);
		byte[] signature = null;
		
		try	{
			allocateCardChannel();
			channel = getCardChannel();

			cardSelectKey(channel, privateKey);
			cardSelectAlgorithm(channel, signAlgorithm);
			cardHash(channel, data);
			signature = cardSign(channel, null);
//			signature = cardSignDummy(channel, null);

		} finally {
			releaseCardChannel();
		}

		return signature;
	}



	/* (non-Javadoc)
	 * @see opencard.opt.signature.SignatureCardService#signHash(opencard.opt.security.PrivateKeyRef, java.lang.String, byte[])
	 */
	public byte[] signHash(PrivateKeyRef privateKey, String signAgorithm, byte[] hash) throws CardServiceException, InvalidKeyException, CardTerminalException {
		return signHash(privateKey, signAgorithm, "", hash);
	}



	/* (non-Javadoc)
	 * @see opencard.opt.signature.SignatureCardService#signHash(opencard.opt.security.PrivateKeyRef, java.lang.String, java.lang.String, byte[])
	 */
	public byte[] signHash(PrivateKeyRef privateKey, String signAgorithm, String padAlgorithm, byte[] hash) throws CardServiceException, InvalidKeyException, CardTerminalException {
		// TODO Auto-generated method stub
		return null;
	}



	/* (non-Javadoc)
	 * @see opencard.opt.signature.SignatureCardService#verifySignedData(opencard.opt.security.PublicKeyRef, java.lang.String, byte[], byte[])
	 */
	public boolean verifySignedData(PublicKeyRef publicKey, String signAlgorithm, byte[] data, byte[] signature) throws CardServiceException, InvalidKeyException, CardTerminalException {
		// TODO Auto-generated method stub
		return false;
	}



	/* (non-Javadoc)
	 * @see opencard.opt.signature.SignatureCardService#verifySignedData(opencard.opt.security.PublicKeyRef, java.lang.String, java.lang.String, byte[], byte[])
	 */
	public boolean verifySignedData(PublicKeyRef publicKey, String signAlgorithm, String padAlgorithm, byte[] data, byte[] signature) throws CardServiceException, InvalidKeyException, CardTerminalException {
		// TODO Auto-generated method stub
		return false;
	}



	/* (non-Javadoc)
	 * @see opencard.opt.signature.SignatureCardService#verifySignedHash(opencard.opt.security.PublicKeyRef, java.lang.String, byte[], byte[])
	 */
	public boolean verifySignedHash(PublicKeyRef publicKey, String signAlgorithm, byte[] hash, byte[] signature) throws CardServiceException, InvalidKeyException, CardTerminalException {
		// TODO Auto-generated method stub
		return false;
	}



	/* (non-Javadoc)
	 * @see opencard.opt.signature.SignatureCardService#verifySignedHash(opencard.opt.security.PublicKeyRef, java.lang.String, java.lang.String, byte[], byte[])
	 */
	public boolean verifySignedHash(PublicKeyRef publicKey, String signAlgorithm, String padAlgorithm, byte[] hash, byte[] signature) throws CardServiceException, InvalidKeyException, CardTerminalException {
		// TODO Auto-generated method stub
		return false;
	}



	/* (non-Javadoc)
	 * @see opencard.opt.security.SecureService#provideCredentials(opencard.opt.security.SecurityDomain, opencard.opt.security.CredentialBag)
	 */
	public void provideCredentials(SecurityDomain domain, CredentialBag creds) throws CardServiceException {
		// TODO Auto-generated method stub
		
	}
	
	

	protected void cardSelectKey(CardChannel channel, PrivateKeyRef privateKey) throws CardTerminalException, CardServiceOperationFailedException {
		CommandAPDU com = new CommandAPDU(20);
		ResponseAPDU res = new ResponseAPDU(2);

		com.setLength(0);
		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_MANAGE_SE);
		com.append((byte)(IsoConstants.P1_MSE_SET | IsoConstants.UQ_COM_DEC_INTAUT));
		com.append(IsoConstants.CRT_DST);
		com.append((byte)5);
		com.append(IsoConstants.TAG_PRKEYREFERENCE);
		com.append((byte)3);
		com.append((byte)0x80);
		com.append((byte)((PrivateKeyFile)privateKey).getKeyNumber());
		com.append((byte)0x00);
			
		res = channel.sendCommandAPDU(com);

		if (res.sw() != IsoConstants.RC_OK) {
			throw new CardServiceOperationFailedException("MANAGE_SE failed with SW1/SW2 = " + HexString.hexifyShort(res.sw()));
		}
	}
	
	
	
	protected void cardSelectAlgorithm(CardChannel channel, String signAlgorithm) throws CardTerminalException, CardServiceOperationFailedException, InvalidKeyException {
		CommandAPDU com = new CommandAPDU(20);
		ResponseAPDU res = new ResponseAPDU(2);

		if (!signAlgorithm.equals("SHA1withECDSA"))
			throw new java.security.InvalidKeyException("Only SHA1withECDSA supported");
			
		com.setLength(0);
		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_MANAGE_SE);
		com.append((byte)(IsoConstants.P1_MSE_SET | IsoConstants.UQ_COM_DEC_INTAUT));
		com.append(IsoConstants.CRT_DST);
		com.append((byte)5);
		com.append(IsoConstants.TAG_ALGOREFERENCE);
		com.append((byte)3);
		com.append((byte)0x13);
		com.append((byte)0x35);
		com.append((byte)0x10);

		res = channel.sendCommandAPDU(com);

		if (res.sw() != IsoConstants.RC_OK) {
			throw new CardServiceOperationFailedException("MANAGE_SE failed with SW1/SW2 = " + HexString.hexifyShort(res.sw()));
		}
	}
	
	
	
	/**
	 * Performed chained hash in card
	 * 
	 * The data is split into chunks of 64 bytes. The final hash is stored in the
	 * card for a subsequent sign operation
	 * 
	 * 
	 * @param channel
	 * 			Open channel for card communication
	 * @param data
	 * 			Data to be hashed
	 *
	 * @throws CardTerminalException
	 * 			
	 * @throws CardServiceOperationFailedException
	 */
	protected void cardHash(CardChannel channel, byte data[]) throws CardTerminalException, CardServiceOperationFailedException {
		CommandAPDU com = new CommandAPDU(74);
		ResponseAPDU res = new ResponseAPDU(2);
		int length;
		int blocksize, offset;
		boolean last, first;
		byte[] block;

		offset = 0;
		length = data.length;
		blocksize = 64;
		block = new byte[blocksize];

		com.setLength(0);
		com.append(IsoConstants.CLA_CHAIN);
		com.append(IsoConstants.INS_PSO);
		com.append(IsoConstants.P1_PSO_HASH);
		com.append(IsoConstants.P2_PSO_HASH_TLV);
		com.append((byte)0x02);
		com.append((byte)0x90);
		com.append((byte)0x00);

		res = channel.sendCommandAPDU(com);

		if (res.sw() != IsoConstants.RC_OK) {
			throw new CardServiceOperationFailedException("PSO_HASH failed with SW1/SW2 = " + HexString.hexifyShort(res.sw()));
		}
		
		while (length > 0) {
			last = length <= 64;
			
			if (length < 64) {
				blocksize = length;
				block = new byte[length];
			}

			System.arraycopy(data, offset, block, 0, blocksize);
			
			com.setLength(0);
			com.append(last ? IsoConstants.CLA_ISO : IsoConstants.CLA_CHAIN);
			com.append(IsoConstants.INS_PSO);
			com.append(IsoConstants.P1_PSO_HASH);
			com.append(IsoConstants.P2_PSO_HASH_TLV);
			
			com.append((byte)(block.length + 2));
			com.append((byte)0x80);
			com.append((byte)block.length);

			com.append(block);
			
			res = channel.sendCommandAPDU(com);

			if (res.sw() != IsoConstants.RC_OK) {
				throw new CardServiceOperationFailedException("PSO_HASH failed with SW1/SW2 = " + HexString.hexifyShort(res.sw()));
			}

			length -= blocksize;
			offset += blocksize;
		}		 
	}
	
	

	/**
	 * Sign data either provided or already hashed in the card
	 * 
	 * @param channel
	 * 			Open card channel
	 * @param hash
	 * 			Hash value or null if hashed in the card
	 * @return
	 * 			Signature
	 * 
	 * @throws CardTerminalException
	 * @throws CardServiceOperationFailedException
	 */	
	protected byte[] cardSign(CardChannel channel, byte[] hash) throws CardTerminalException, CardServiceOperationFailedException {
		CommandAPDU com = new CommandAPDU(5);
		ResponseAPDU res = new ResponseAPDU(258);
		
		com.setLength(0);
		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_PSO);
		com.append(IsoConstants.P1_PSO_CDS);
		com.append(IsoConstants.P2_PSO_CDS_DTBS);
		
		if (hash != null) {
			com.append((byte)hash.length);
			com.append(hash);
		}

		com.append((byte)48);
			
		res = channel.sendCommandAPDU(com);

		if (res.sw() != IsoConstants.RC_OK) {
			throw new CardServiceOperationFailedException("PSO_COMPUTE_DIGITAL_SIGNATURE failed with SW1/SW2 = " + HexString.hexifyShort(res.sw()));
		}
		
		int len = res.getLength() - 2;		/* Ignore SW1/SW2 */
		byte[] signature = new byte[len];
		
		System.arraycopy(res.getBuffer(), 0, signature, 0, len);
		return signature;
	}
	
	
	
	/**
	 * Sign data either provided or already hashed in the card (Dummy)
	 * 
	 * @param channel
	 * 			Open card channel
	 * @param hash
	 * 			Hash value or null if hashed in the card
	 * @return
	 * 			Signature
	 * 
	 * @throws CardTerminalException
	 * @throws CardServiceOperationFailedException
	 */	
	protected byte[] cardSignDummy(CardChannel channel, byte[] hash) throws CardTerminalException, CardServiceOperationFailedException {
		CommandAPDU com = new CommandAPDU(5);
		ResponseAPDU res = new ResponseAPDU(258);
		
		com.setLength(0);
		com.append(IsoConstants.CLA_ISO);
		com.append(IsoConstants.INS_GET_CHALLENGE);
		com.append((byte)0);
		com.append((byte)0);

		com.append((byte)48);
		
		res = channel.sendCommandAPDU(com);

		if (res.sw() != IsoConstants.RC_OK) {
			throw new CardServiceOperationFailedException("PSO_COMPUTE_DIGITAL_SIGNATURE failed with SW1/SW2 = " + HexString.hexifyShort(res.sw()));
		}
		
		int len = res.getLength() - 2;		/* Ignore SW1/SW2 */
		byte[] signature = new byte[len];
		
		System.arraycopy(res.getBuffer(), 0, signature, 0, len);
		return signature;
	}
	
	
	
}
