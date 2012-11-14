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

	/**
	 * Generate forward hash chain and store it inside forwardHashChain array.
	 * Uses SHA-256 cryptographic hash function.
	 * 
	 * @param key
	 * 			First key string to start the hashing 
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
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

	/**
	 * Generate the hash chain and store it inside backwardHashChain backwards.
	 * Uses SHA-256 cryptographic hashing.
	 * 
	 * @param key
	 * 		First key string to hash.
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
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

}
