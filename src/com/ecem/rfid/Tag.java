package com.ecem.rfid;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
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

		/* Generate hash of T_ID */

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(T_ID.toString().getBytes("UTF-8"));
		hash_TID = md.digest();
	}

	public void xtm(byte[] s) throws UnsupportedEncodingException,
			NoSuchAlgorithmException {
		
		byte[] t = Protocol.generateRandomChallenge(32);
		
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(t.toString().getBytes("UTF-8"));
		byte[] hasht = md.digest();
		
		byte[] x = new byte[t.length];

		for (int i = 0; i < t.length; i++) {

			x[i] = (byte) (t[i] ^ s[i] ^ hasht[i] ^ hash_TID[i]);
		}

		bigX = new BigInteger(x);
		bigX = bigX.mod(n);

		bigT = new BigInteger(t);
		bigT = bigT.mod(n);
		
		String c = bigX.toString() + bigT.toString();
		
		md = MessageDigest.getInstance("SHA-256");
		md.update(c.getBytes("UTF-8"));
		M = md.digest();
		
		// x kare t kare hesaplanan yer
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

	public BigInteger getT_ID() {
		return T_ID;
	}

	protected void setT_ID(BigInteger t_ID) {
		T_ID = t_ID;
	}

	public BigInteger getN() {
		return n;
	}

	protected void setN(BigInteger n) {
		this.n = n;
	}

	public byte[] getHash_TID() {
		return hash_TID;
	}

	protected void setHash_TID(byte[] hash_TID) {
		this.hash_TID = hash_TID;
	}

}
