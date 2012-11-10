package com.ecem.wns;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashChainGenerator {

	/**
	 * @param args
	 * @throws NoSuchAlgorithmException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		
		int n = 5;
		
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		String text = "initialkey";
		md.update(text.getBytes("UTF-8"));
		byte[] digest = md.digest();
		
		for(int i = 1; i < n; i++) {
			
			md.update(digest);
			digest = md.digest();
		}

	}

}
