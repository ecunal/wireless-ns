package com.ecem.rfid;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
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
			tagHashes[i] = Protocol.hash(this.tagIDs[i].toString());
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

		for (BigInteger xi : x_i) {
			for (BigInteger ti : t_i) {
				
				String c = xi.toString() + ti.toString();
				byte[] h = Protocol.hash(c);
				
				if (Arrays.toString(h).equals(Arrays.toString(M))) {
					
					x = xi;
					t = ti;
					
					break;
				}
			}
		}
		
		if(x != null && t != null) {
			
			byte[] x_array = Protocol.bigIntToByte(x);
			byte[] t_array = Protocol.bigIntToByte(t);
			
			byte[] hasht = Protocol.hash(t_array.toString());
			
			byte[] hash_tid = new byte[hasht.length];

			for (int i = 0; i < hasht.length; i++) {

				hash_tid[i] = (byte) (x_array[i] ^ s[i] ^ hasht[i] ^ t_array[i]);
			}
		}
		else {
			System.out.println("ATTAAACK!!!!!111");
		}
		
	}

}
