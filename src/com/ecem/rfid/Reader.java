package com.ecem.rfid;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Reader {

	private BigInteger[] tagIDs;
	private byte[][] tagHashes;

	public BigInteger n;
	private BigInteger p, q;
	
	private byte[] s;

	public Reader(BigInteger[] tagIDs) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {

		this.tagIDs = tagIDs;

		tagHashes = new byte[tagIDs.length][];

		for (int i = 0; i < tagIDs.length; i++) {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(tagIDs[i].toString().getBytes("UTF-8"));
			tagHashes[i] = md.digest();
		}

		p = BigInteger.valueOf(2).pow(128);
		p = p.nextProbablePrime();

		while (!p.subtract(BigInteger.valueOf(3)).divideAndRemainder(
				BigInteger.valueOf(4))[1].equals(BigInteger.ZERO)) {
			p = p.nextProbablePrime();
		}

		q = p.nextProbablePrime();

		while (!q.subtract(BigInteger.valueOf(3)).divideAndRemainder(
				BigInteger.valueOf(4))[1].equals(BigInteger.ZERO)) {
			q = q.nextProbablePrime();
		}
//		
//		System.out.println(p);
//		System.out.println(q);

		n = p.multiply(q);
	}

	public byte[] generateS() throws NoSuchAlgorithmException {
		s = Protocol.generateRandomChallenge(32);
		return s;
	}

	public void solveXT(BigInteger bigX, BigInteger bigT, byte[] M)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {

		BigInteger[] x_i = Protocol.solveQuadratic(BigInteger.ONE,
				BigInteger.ZERO, bigX.negate(), p, q, 10);
		
		BigInteger[] t_i = Protocol.solveQuadratic(BigInteger.ONE,
				BigInteger.ZERO, bigT.negate(), p, q, 10);

		BigInteger x = null, t = null;

		MessageDigest mdd;

		for (BigInteger xi : x_i) {
			for (BigInteger ti : t_i) {
				
				String c = xi.toString() + ti.toString();
				
				mdd = MessageDigest.getInstance("SHA-256");
				mdd.update(c.getBytes("UTF-8"));
				byte[] h = mdd.digest();
				
				if (Arrays.toString(h).equals(Arrays.toString(M))) {
					System.out.println("burdaa");
					x = xi;
					t = ti;
					break;
				}
			}
		}
		
		if(x != null && t != null) {
			
			System.out.println(x.bitLength());
			System.out.println(t.bitLength());
			
			byte[] x_array = Protocol.bigIntToByte(x);
			byte[] t_array = Protocol.bigIntToByte(t);
			
			System.out.println(x_array.length);
			System.out.println(t_array.length);
			
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(t_array.toString().getBytes("UTF-8"));
			byte[] hasht = md.digest();
			
			byte[] hash_tid = new byte[hasht.length];

			for (int i = 0; i < hasht.length; i++) {

				hash_tid[i] = (byte) (x_array[i] ^ s[i] ^ hasht[i] ^ t_array[i]);
			}
			
			System.out.println(Arrays.toString(hash_tid));
			System.out.println(tagHashes.length);
			System.out.println(Arrays.toString(tagHashes[0]));
		}
		else {
			System.out.println("ATTAAACK!!!!!111");
		}
		
//		System.out.println(x);
//		System.out.println(t);
	}

	/* Getters & Setters */

	public BigInteger[] getTagIDs() {
		return tagIDs;
	}

	public void setTagIDs(BigInteger[] tagIDs) {
		this.tagIDs = tagIDs;
	}

}
