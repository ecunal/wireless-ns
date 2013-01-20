package com.ecem.rfid;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.xml.bind.DatatypeConverter;

public class Protocol {

	public static final BigInteger ZERO = BigInteger.ZERO,
			ONE = BigInteger.ONE, TWO = new BigInteger("2"),
			THREE = new BigInteger("3"), FOUR = new BigInteger("4");

	public static byte[] hash(String s) throws UnsupportedEncodingException,
			NoSuchAlgorithmException {

		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(s.getBytes("UTF-8"));
		return md.digest();
	}

	public static byte[] concat(byte[] x, byte[] t) {

		byte[] m = new byte[x.length + t.length];
		System.arraycopy(x, 0, m, 0, x.length);
		System.arraycopy(t, 0, m, x.length, t.length);

		return m;
	}

	public static byte[] bigIntToByte(BigInteger b) {
		byte[] array = b.toByteArray();

		if (array[0] == 0) {
			byte[] tmp = new byte[array.length - 1];
			System.arraycopy(array, 1, tmp, 0, tmp.length);
			array = tmp;
		}
		return array;
	}

	/**
	 * Generates random bit string of length a * 8 (a is the number of bytes)
	 * 
	 * @param a
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] generateRandomChallenge(int a)
			throws NoSuchAlgorithmException {

		SecureRandom r = SecureRandom.getInstance("SHA1PRNG");

		byte[] result = new byte[a];
		r.nextBytes(result);

		return result;
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

	public static String byteArrayToString(byte[] b) {
		return DatatypeConverter.printHexBinary(b);
	}

	// Solves quadratic congruences ax^2+bx+c congruent to 0 mod n=pq
	// Returns four solutions when they exist
	public static BigInteger[] solveQuadratic(BigInteger a, BigInteger b,
			BigInteger c, BigInteger p, BigInteger q, int primeTolerance) {

		// Check that the factors of the modulus are distinct
		if (p.equals(q))
			throw new IllegalArgumentException(
					"The modulus factors are not unique!");

		// Check that the factors are congruent to 3 modulo 4
		BigInteger n = p.multiply(q);
		if (!lnr(p.mod(FOUR), n).equals(THREE))
			throw new IllegalArgumentException(p + " is not of form 4k+3!");
		if (!lnr(q.mod(FOUR), n).equals(THREE))
			throw new IllegalArgumentException(q + " is not of form 4k+3!");

		// Check that the factors of the modulus are prime
		if (!p.isProbablePrime(primeTolerance))
			throw new IllegalArgumentException(p + " is not prime!");
		if (!q.isProbablePrime(primeTolerance))
			throw new IllegalArgumentException(q + " is not prime!");

		// Create the array of solutions
		BigInteger[] result = new BigInteger[4];

		// Start forming the terms
		BigInteger aInv = a.modInverse(n);
		BigInteger pInv = p.modInverse(q);
		BigInteger qInv = q.modInverse(p);
		BigInteger twoInv = TWO.modInverse(n);
		BigInteger term1 = aInv.multiply(twoInv.multiply(b).modPow(TWO, n)
				.multiply(aInv).subtract(c));
		BigInteger term2 = twoInv.multiply(aInv).multiply(b);
		BigInteger t1 = lnr(
				term1.modPow(p.add(ONE).divide(FOUR), n).subtract(term2)
						.multiply(q).multiply(qInv), n);
		BigInteger t2 = lnr(
				term1.modPow(q.add(ONE).divide(FOUR), n).subtract(term2)
						.multiply(p).multiply(pInv), n);
		BigInteger t3 = lnr(term1.modPow(p.add(ONE).divide(FOUR), n).negate()
				.subtract(term2).multiply(q).multiply(qInv), n);
		BigInteger t4 = lnr(term1.modPow(q.add(ONE).divide(FOUR), n).negate()
				.subtract(term2).multiply(p).multiply(pInv), n);

		// Form the solutions
		result[0] = lnr(t1.add(t2), n);
		result[1] = lnr(t1.add(t4), n);
		result[2] = lnr(t3.add(t2), n);
		result[3] = lnr(t3.add(t4), n);

		// Check the solutions; if any are bad, throw an exception
		BigInteger x;
		for (int i = 0; i < 4; i++) {
			x = result[i];
			if (!lnr(a.multiply(x.multiply(x)).add(b.multiply(x)).add(c), n)
					.equals(ZERO))
				throw new IllegalArgumentException("Solution x=" + x
						+ " does not check!");
		}

		return result;
	}

	// Computes the least nonnegative residue of b mod m, where m>0.
	public static BigInteger lnr(BigInteger b, BigInteger m) {
		if (m.compareTo(ZERO) <= 0)
			throw new IllegalArgumentException("Modulus must be positive.");
		BigInteger answer = b.mod(m);
		return (answer.compareTo(ZERO) < 0) ? answer.add(m) : answer;
	}

}
