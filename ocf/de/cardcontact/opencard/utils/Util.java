package de.cardcontact.opencard.utils;

import java.io.ByteArrayOutputStream;

public final class Util {
	public static long extractLongFromByteArray(byte[] buffer, int offset, int length) {
		if((offset + length) > buffer.length) {
			throw new IndexOutOfBoundsException("Length exceeds buffer size");
		}
		if(length > 8) {
			throw new IllegalArgumentException("More than 8 byte cannot be encoded");
		}
		
		long c = 0;
		while (length-- > 0) {
			c <<= 8;
			c |= buffer[offset++] & 0xFF;		
		}
		return c;
	}
	
}

