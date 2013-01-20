package com.ecem.rfid;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class Tag {

	private BigInteger T_ID;
	private BigInteger n;
	private byte[] hash_TID;

	private BigInteger bigX, bigT;
	private byte[] M;

	public Tag(BigInteger T_ID, BigInteger n)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		this.T_ID = T_ID;
		this.n = n;

		hash_TID = Protocol.hash(this.T_ID.toString());
	}

	public void xtm(byte[] s) throws UnsupportedEncodingException,
			NoSuchAlgorithmException {
		
		byte[] t = Protocol.generateRandomChallenge(32);
		byte[] hasht = Protocol.hash(t.toString());
		byte[] x = new byte[t.length];

		for (int i = 0; i < t.length; i++) {

			x[i] = (byte) (t[i] ^ s[i] ^ hasht[i] ^ hash_TID[i]);
		}

		bigX = new BigInteger(x);
		bigX = bigX.mod(n);

		bigT = new BigInteger(t);
		bigT = bigT.mod(n);
		
		String c = bigX.toString() + bigT.toString();
		
		M = Protocol.hash(c);
		
		bigX = bigX.modPow(BigInteger.valueOf(2), n);
		bigT = bigT.modPow(BigInteger.valueOf(2), n);
	}

	/* Getters & Setters */

	public BigInteger getBigX() {
		return bigX;
	}

	public BigInteger getBigT() {
		return bigT;
	}

	public byte[] getM() {
		return M;
	}

}
