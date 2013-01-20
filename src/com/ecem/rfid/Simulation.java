package com.ecem.rfid;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class Simulation {

	public static void main(String[] args) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		
		

		Reader reader = new Reader(new BigInteger[] { BigInteger.ONE });

		Tag tag = new Tag(BigInteger.ONE, reader.n);

		byte[] s = reader.generateS();

		tag.xtm(s);

		long start = System.currentTimeMillis();
		reader.solveXT(tag.getBigX(), tag.getBigT(), tag.getM());
		
		long end = System.currentTimeMillis();
		
		System.out.println(end-start);

	}
}
