package com.ecem.wns.hw2;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.xml.bind.DatatypeConverter;

public class CRCCalculator {

	public static String calculate(String hex) {

		byte[] hexByte = hexStringToByteArray(hex);

		/* Prepare the input */

		int i = 0;

		for (byte b : hexByte) {

			if (i < 4) {
				hexByte[i] = complementByte(reverseByte(b));
			} else {
				hexByte[i] = reverseByte(b);
			}
			i++;
		}

		System.out.println("--CRC input:\t\t" + hex);
		System.out.println("--Prepared input:\t" + byteArrayToString(hexByte));

		/* Calculate the CRC32 checksum */

		Checksum crc32 = new CRC32();

		crc32.update(hexByte, 0, hexByte.length);

		int checksum = (int) crc32.getValue();

		String stringcrc = Long.toHexString(checksum).toUpperCase();
		
		if(stringcrc.length() > 8) {
			stringcrc = stringcrc.substring(stringcrc.length() - 8,
					stringcrc.length());
		}
		
		System.out.println("--Produced output:\t"
				+ stringcrc);
		
		/* Process the output */

		checksum = complementInt(reverseInt(checksum));
		
		stringcrc = Long.toHexString(checksum).toUpperCase();

		if(stringcrc.length() > 8) {
			stringcrc = stringcrc.substring(stringcrc.length() - 8,
					stringcrc.length());
		}
		
		System.out.println("--Processed output:\t" + stringcrc);

		return stringcrc;
	}

	public static byte[] hexStringToByteArray(String s) {

		if (s.length() % 2 == 1) {
			s = "0" + s;
		}

		return DatatypeConverter.parseHexBinary(s);
	}

	public static String byteArrayToString(byte[] b) {
		return DatatypeConverter.printHexBinary(b);
	}

	public static byte reverseByte(byte b) {

		String reversedBits = new StringBuffer(byteToString(b)).reverse()
				.toString();

		return (byte) Integer.parseInt(reversedBits, 2);
	}

	public static int reverseInt(int i) {
		return Integer.reverse(i);
	}

	public static byte complementByte(byte b) {
		return (byte) (b ^ 0xFF);
	}

	public static int complementInt(int i) {
		return i ^ 0xFFFFFFFF;
	}

	public static String byteToString(byte b) {

		String bits = Integer.toBinaryString(b);
		if (bits.length() <= 8) {

			int diff = 8 - bits.length();

			while (diff > 0) {
				bits = "0" + bits;
				diff--;
			}
		} else {
			bits = bits.substring(bits.length() - 8, bits.length());
		}

		return bits;
	}
}
