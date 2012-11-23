package com.ecem.wns.hw2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

	private static String msg;
	private static String deltamsg;

	public static void main(String[] args) {

		Scanner s = new Scanner(System.in);

		System.out.println("Enter the message in hex format:");

		msg = s.nextLine().toUpperCase();
		
		if(msg.length()%2 == 1) {
			msg = "0" + msg;
		}

		System.out.println("How many bits will change in the output?");

		int count = Integer.parseInt(s.nextLine());
		Map<Integer, Integer> taps = new HashMap<Integer, Integer>();

		System.out.println("Enter the bit locations:");

		for (int i = 0; i < count; i++) {

			String input = s.nextLine();

			if (Integer.parseInt(input) > msg.length() * 4) {
				System.out
						.println("Specified bit location does not exist in the message." +
								"\nEnter another location:");
				i--;
				continue;
			}

			taps.put(Integer.parseInt(input), 1);
		}
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < msg.length() * 4; i++) {
			if (taps.containsKey((msg.length() * 4) - i)) {
				sb.append(1);
			} else {
				sb.append(0);
			}
		}

		deltamsg = Long.toHexString(Long.parseLong(sb.toString(), 2)).toUpperCase();

		if (deltamsg.length() < msg.length()) {
			int diff = msg.length() - deltamsg.length();

			while (diff > 0) {
				deltamsg = "0" + deltamsg;
				diff--;
			}
		}

		receiver(attacker(sender(msg), deltamsg));

		s.close();
	}

	public static String sender(String msg) {

		System.out.println("***\tSENDER\t***\n");

		System.out.println("Message to send:\t" + msg);

		String crc = CRCCalculator.calculate(msg);

		System.out.println("Native CRC32 checksum:\t" + crc);
		System.out.println("Message | CRC:\t\t" + msg + crc);

		byte[] msgBytes = CRCCalculator.hexStringToByteArray(msg);
		byte[] crcBytes = CRCCalculator.hexStringToByteArray(crc);
		byte[] cipherBytes = new byte[msgBytes.length + crcBytes.length];

		for (int i = 0; i < msgBytes.length; i++) {

			cipherBytes[i] = (byte) (msgBytes[i] ^ 0xFF);
		}

		for (int i = 0; i < crcBytes.length; i++) {
			cipherBytes[i + msgBytes.length] = (byte) (crcBytes[i] ^ 0xFF);
		}

		String cipher = CRCCalculator.byteArrayToString(cipherBytes);
		System.out.println("Encrypted message:\t" + cipher + "\n(M | CRC(M) XOR K)");

		return cipher;
	}

	public static String attacker(String cipher, String deltaM) {

		System.out.println("\n\n***\tATTACKER\t***\n");

		System.out.println("Attacker received:\t" + cipher);
		System.out.println("Hex of bits to change:\t" + deltaM);

		String crc = CRCCalculator.calculate(deltaM);
		
		System.out.println("CRC of delta M:\t\t" + crc);

		deltaM += crc;

		System.out.println("Delta M | CRC(Delta M):\t" + deltaM);

		byte[] deltaMBytes = CRCCalculator.hexStringToByteArray(deltaM);
		byte[] cipherBytes = CRCCalculator.hexStringToByteArray(cipher);
		byte[] newCipherBytes = new byte[deltaMBytes.length];

		for (int i = 0; i < deltaMBytes.length; i++) {
			newCipherBytes[i] = (byte) (deltaMBytes[i] ^ cipherBytes[i]);
		}

		String newCipher = CRCCalculator.byteArrayToString(newCipherBytes);

		System.out.println("New ciphertext to send:\t" + newCipher);

		return newCipher;
	}

	public static String receiver(String cipher) {

		System.out.println("\n\n***\tRECEIVER\t***\n");

		System.out.println("Ciphertext received:\t" + cipher);

		byte[] cipherBytes = CRCCalculator.hexStringToByteArray(cipher);
		byte[] messageBytes = new byte[cipherBytes.length];

		for (int i = 0; i < cipherBytes.length; i++) {

			messageBytes[i] = (byte) (cipherBytes[i] ^ 0xFF);
		}

		System.out.println("Ciphertext XOR Key:\t"
				+ CRCCalculator.byteArrayToString(messageBytes));

		byte[] plainBytes = Arrays.copyOfRange(messageBytes, 0,
				messageBytes.length - 4);

		String plain = CRCCalculator.byteArrayToString(plainBytes);
		String crc = CRCCalculator.byteArrayToString(Arrays.copyOfRange(
				messageBytes, messageBytes.length - 4, messageBytes.length));

		System.out.println("Received message:\t" + plain
				+ "\nReceived CRC:\t\t" + crc);
		
		String newcrc = CRCCalculator.calculate(plain);
		
		System.out.println("Calculated CRC:\t\t"
				+ newcrc);

		if (crc.equals(newcrc))
			System.out.println("They are the same!");

		System.out.println("Original message were:\t" + msg);
		System.out.println("Received message is:\t" + plain);

		byte[] originalBytes = CRCCalculator.hexStringToByteArray(msg);

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < originalBytes.length; i++) {
			sb.append(CRCCalculator
					.byteToString((byte) (originalBytes[i] ^ plainBytes[i])));
		}

		String bits = sb.toString();

		System.out.print("Changed bit locations:\t");

		int index = bits.indexOf('1');
		while (index >= 0) {
			System.out.print((bits.length() - index) + " ");
			index = bits.indexOf('1', index + 1);
		}

		System.out.println();

		return plain;
	}

}
