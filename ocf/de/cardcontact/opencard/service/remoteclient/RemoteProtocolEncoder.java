/*
 *  ---------
 * |.##> <##.|  Open Smart Card Development Platform (www.openscdp.org)
 * |#       #|  
 * |#       #|  Copyright (c) 1999-2013 CardContact Software & System Consulting
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

package de.cardcontact.opencard.service.remoteclient;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import opencard.core.terminal.CardID;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.CommandAPDU;
import opencard.core.terminal.ResponseAPDU;

import de.cardcontact.opencard.service.remoteclient.RemoteProtocolUnit.Action;
import de.cardcontact.tlv.ConstructedTLV;
import de.cardcontact.tlv.GPTLV_Generic;
import de.cardcontact.tlv.NativeTLVList;
import de.cardcontact.tlv.PrimitiveTLV;
import de.cardcontact.tlv.TLV;
import de.cardcontact.tlv.TLVDataSizeException;
import de.cardcontact.tlv.TLVEncodingException;
import de.cardcontact.tlv.TagSizeException;

/**
 * Encode a list of RemoteProtocolUnits for transfer using RAMOverHttp encoding
 * 
 * @author asc
 *
 */
public class RemoteProtocolEncoder {
	
	private static final int COMMAND_SCRIPTING_TEMPLATE = 0xAA;
	private static final int COMMAND_APDU = 0x22;
	private static final int RESPONSE_SCRIPTING_TEMPLATE = 0xAB;
	private static final int INITIATION_TEMPLATE = 0xE8;
	private static final int EXECUTED_COMMANDS = 0x80;
	private static final int RESPONSE_APDU = 0x23;
	private static final int INTEGER = 0x02;
	private static final int UTF8String = 0x0C;
	private static final int RESET = 0xC0;
	private static final int ATR = 0xC0;
	private static final int NOTIFY = 0xE0;
	private static final int CLOSE = 0xE1;
	private static final int VERSION = 0xE2;

	private int executedCommands = 1;

	ArrayList<RemoteProtocolUnit> rpus= new ArrayList<RemoteProtocolUnit>(1);

	public RemoteProtocolEncoder() {
	}



	public void add(RemoteProtocolUnit rpu) {
		rpus.add(rpu);
	}



	public List<RemoteProtocolUnit> getRemoteProtocolUnits() {
		return rpus;
	}



	private RemoteProtocolUnit decodeRPU(RemoteProtocolUnit.Action action, byte[] rpu) throws TLVEncodingException {
		int id = 0;
		String str = null;

		try {
			NativeTLVList list = new NativeTLVList(rpu);

			for (int i = 0; i < list.getLength(); i++) {
				GPTLV_Generic rpuenc = list.get(i);

				switch(rpuenc.getTag()) {
				case INTEGER:
					id = (new BigInteger(rpuenc.getValue())).intValue();
					break;
				case UTF8String:
					str = new String(rpuenc.getValue(), "UTF-8");
					break;
				}
			}
		} catch (TagSizeException e) {
			throw new TLVEncodingException();
		} catch (TLVDataSizeException e) {
			throw new TLVEncodingException();
		} catch (UnsupportedEncodingException e) {
			throw new TLVEncodingException();
		}
		
		return new RemoteProtocolUnit(action, id, str);
	}



	public byte[] encodeCommandScriptingTemplate() {

		ConstructedTLV cst;
		
		try	{
			cst = new ConstructedTLV(COMMAND_SCRIPTING_TEMPLATE);
		
			for (RemoteProtocolUnit rpu : rpus) {
				switch(rpu.getAction()) {
				case APDU:
					CommandAPDU com = (CommandAPDU)rpu.getPayload();
					cst.add(new PrimitiveTLV(COMMAND_APDU, com.getBytes()));
					break;
				case RESET:
					cst.add(new PrimitiveTLV(RESET, new byte[] {} ));
					break;
				case NOTIFY:
					ConstructedTLV notify = new ConstructedTLV(NOTIFY);
					notify.add(new PrimitiveTLV(INTEGER, BigInteger.valueOf(rpu.getId()).toByteArray()));
					notify.add(new PrimitiveTLV(UTF8String, rpu.getMessage().getBytes("UTF-8")));
					cst.add(notify);
					break;
				case CLOSE:
					ConstructedTLV closemsg = new ConstructedTLV(CLOSE);
					closemsg.add(new PrimitiveTLV(UTF8String, rpu.getMessage().getBytes("UTF-8")));
					cst.add(closemsg);
					break;
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return cst.getBytes();
	}



	public void decodeCommandScriptingTemplate(byte[] cst) throws TLVEncodingException {
		try {
			NativeTLVList list = new NativeTLVList(cst);
			GPTLV_Generic csttlv = list.get(0);

			if (csttlv.getTag() != COMMAND_SCRIPTING_TEMPLATE) {
				throw new TLVEncodingException("Expected tag 'AA' in Command Scripting Template");
			}
			
			list = new NativeTLVList(csttlv.getValue());

			for (int i = 0; i < list.getLength(); i++) {
				GPTLV_Generic rpuenc = list.get(i);
				
				switch(rpuenc.getTag()) {
				case COMMAND_APDU:
					CommandAPDU apdu = new CommandAPDU(rpuenc.getValue());
					rpus.add(new RemoteProtocolUnit(apdu));
					break;
				case RESET:
					rpus.add(new RemoteProtocolUnit(RemoteProtocolUnit.Action.RESET));
					break;
				case NOTIFY:
					rpus.add(decodeRPU(RemoteProtocolUnit.Action.NOTIFY, rpuenc.getValue()));
					break;
				case CLOSE:
					rpus.add(decodeRPU(RemoteProtocolUnit.Action.CLOSE, rpuenc.getValue()));
					break;
				}
			}
		} catch (TagSizeException e) {
			throw new TLVEncodingException();
		} catch (TLVDataSizeException e) {
			throw new TLVEncodingException();
		}
	}



	public byte[] encodeResponseScriptingTemplate() {

		ConstructedTLV cst;
		
		try	{
			cst = new ConstructedTLV(RESPONSE_SCRIPTING_TEMPLATE);
		
			for (RemoteProtocolUnit rpu : rpus) {
				switch(rpu.getAction()) {
				case APDU:
					ResponseAPDU com = (ResponseAPDU)rpu.getPayload();
					cst.add(new PrimitiveTLV(RESPONSE_APDU, com.getBytes()));
					break;
				case RESET:
					cst.add(new PrimitiveTLV(ATR, ((CardID)rpu.getPayload()).getATR()));
					break;
				case CLOSE:
					ConstructedTLV closemsg = new ConstructedTLV(CLOSE);
					closemsg.add(new PrimitiveTLV(UTF8String, rpu.getMessage().getBytes("UTF-8")));
					cst.add(closemsg);
					break;
				}
			}
			cst.add(new PrimitiveTLV(EXECUTED_COMMANDS, new byte[] { (byte) this.executedCommands } ));
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return cst.getBytes();
	}



	public void decodeResponseScriptingTemplate(byte[] cst) throws TLVEncodingException {
		try {
			NativeTLVList list = new NativeTLVList(cst);
			GPTLV_Generic csttlv = list.get(0);
			
			if (csttlv.getTag() != RESPONSE_SCRIPTING_TEMPLATE) {
				throw new TLVEncodingException("Expected tag 'AB' in Response Scripting Template");
			}
			
			list = new NativeTLVList(csttlv.getValue());

			for (int i = 0; i < list.getLength(); i++) {
				GPTLV_Generic rpuenc = list.get(i);

				switch(rpuenc.getTag()) {
				case RESPONSE_APDU:
					ResponseAPDU apdu = new ResponseAPDU(rpuenc.getValue());
					rpus.add(new RemoteProtocolUnit(apdu));
					break;
				case EXECUTED_COMMANDS:
					this.executedCommands = rpuenc.getValue()[0] & 0xFF;
					break;
				case ATR:
					CardID cardID = new CardID(rpuenc.getValue());
					rpus.add(new RemoteProtocolUnit(cardID));
					break;
				case CLOSE:
					rpus.add(decodeRPU(RemoteProtocolUnit.Action.CLOSE, rpuenc.getValue()));
					break;
				}
			}
		} catch (TagSizeException e) {
			throw new TLVEncodingException();
		} catch (CardTerminalException e) {
			throw new TLVEncodingException();
		} catch (TLVDataSizeException e) {
			throw new TLVEncodingException();
		}
	}



	public byte[] encodeInitiationTemplate() {

		ConstructedTLV pit;
		
		try	{
			pit = new ConstructedTLV(INITIATION_TEMPLATE);
		
			for (RemoteProtocolUnit rpu : rpus) {
				switch(rpu.getAction()) {
				case RESET:
					pit.add(new PrimitiveTLV(ATR, ((CardID)rpu.getPayload()).getATR()));
					break;
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return pit.getBytes();
	}



	public void decodeInitiationTemplate(byte[] pit) throws TLVEncodingException {
		try {
			NativeTLVList list = new NativeTLVList(pit);
			GPTLV_Generic pittlv = list.get(0);
			
			if (pittlv.getTag() != INITIATION_TEMPLATE) {
				throw new TLVEncodingException("Expected tag 'E8' in Initiation Template");
			}
			
			list = new NativeTLVList(pittlv.getValue());

			for (int i = 0; i < list.getLength(); i++) {
				GPTLV_Generic rpuenc = list.get(i);

				switch(rpuenc.getTag()) {
				case ATR:
					CardID cardID = new CardID(rpuenc.getValue());
					rpus.add(new RemoteProtocolUnit(cardID));
					break;
				}
			}
		} catch (TagSizeException e) {
			throw new TLVEncodingException();
		} catch (CardTerminalException e) {
			throw new TLVEncodingException();
		} catch (TLVDataSizeException e) {
			throw new TLVEncodingException();
		}
	}
	
	
	
	static public boolean isInitiation(byte[] pit) {
		return ((pit[0] & 0xFF) == INITIATION_TEMPLATE);
	}
}
