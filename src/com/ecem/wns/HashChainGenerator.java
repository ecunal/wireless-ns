package com.ecem.wns;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashChainGenerator {

	private int n, /* array length */ counter /* which key to return now */;

	private byte[][] forwardHashChain, backwardHashChain;

	public HashChainGenerator(String forwardKey, String backwardKey, int n) {

		this.n = n;
		counter = 0;
		
		forwardHashChain = new byte[n][];
		backwardHashChain = new byte[n][];

		try {
			generateForwardChain(forwardKey);
		} catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		try {
			generateBackwardChain(backwardKey);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] getNextForwardKey() {
		return forwardHashChain[counter];
	}
	
	public byte[] getNextBackwardKey() {
		return backwardHashChain[counter];
	}
	
	public void incrementCounter() {
		counter++;
	}

	private void generateForwardChain(String key)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(key.getBytes("UTF-8"));
		byte[] digest = md.digest();
		forwardHashChain[0] = digest;

		for (int i = 1; i < n; i++) {
			md.update(digest);
			digest = md.digest();
			forwardHashChain[i] = digest;
		}
	}

	private void generateBackwardChain(String key)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(key.getBytes("UTF-8"));
		byte[] digest = md.digest();
		backwardHashChain[n - 1] = digest;

		for (int i = n - 2; i >= 0; i--) {
			md.update(digest);
			digest = md.digest();
			backwardHashChain[i] = digest;
		}
	}

/*		public static String toBinaryString(byte[] digest) {

		StringBuilder sb = new StringBuilder();

		for (byte b : digest) {

			String temp = Integer.toBinaryString(b);
			int difference = 8 - temp.length();

			if (difference < 0) {
				sb.append(temp.subSequence(temp.length() - 9, temp.length() - 1));
			} else {
				while (difference > 0) {
					sb.append(0);
					difference--;
				}
				sb.append(temp);
			}
		}

		return sb.toString();
	}*/

}
