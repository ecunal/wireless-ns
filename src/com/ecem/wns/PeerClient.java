package com.ecem.wns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PeerClient {

	private int port;

	private final String[] messages = { "hi", "hello", "hey", "yey", "bye" };
	private final String rekey = "rekey";

	public PeerClient(int port) {
		this.port = port;
	}

	public void connect() throws IOException, GeneralSecurityException {

		Socket socket = null;
		PrintWriter out = null;
		BufferedReader in = null;

		try {
			socket = new Socket("localhost", port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: localhost.");
			System.exit(1);
		} catch (IOException e) {
			System.err
					.println("Couldn't get I/O for the connection to: localhost.");
			System.exit(1);
		}

		String fromServer;
		int count = 0;

		HashChainGenerator generator = new HashChainGenerator("firstkey",
				"secondkey", 100);
		byte[] forward = generator.getNextForwardKey();
		byte[] backward = generator.getNextBackwardKey();
		generator.incrementCounter();

		byte[] key = new byte[forward.length];
		for (int i = 0; i < key.length; i++) {
			key[i] = (byte) (forward[i] ^ backward[i]);
		}

		while ((fromServer = in.readLine()) != null) {

			String msg = "";
			try {
				msg = decrypt(fromServer, key);
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}

			System.out.println("Client: cipher = " + fromServer
					+ "\nClient: msg = " + msg);

			if (count == 5) {
				System.out.println("that's the end! -c");
				break;
			}

			if (msg.equals(rekey)) {

				forward = generator.getNextForwardKey();
				backward = generator.getNextBackwardKey();
				generator.incrementCounter();

				key = new byte[forward.length];
				for (int i = 0; i < key.length; i++) {
					key[i] = (byte) (forward[i] ^ backward[i]);
				}

				continue;
			}

			out.println(encrypt(rekey, key));
			
			forward = generator.getNextForwardKey();
			backward = generator.getNextBackwardKey();
			generator.incrementCounter();

			key = new byte[forward.length];
			for (int i = 0; i < key.length; i++) {
				key[i] = (byte) (forward[i] ^ backward[i]);
			}
			
			out.println(encrypt(messages[count], key));

			count++;
		}

		out.close();
		in.close();
		socket.close();

	}

	public String encrypt(String message, byte[] key)
			throws GeneralSecurityException, UnsupportedEncodingException {

		byte[] input = message.getBytes("UTF-8");

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

		SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivspec = new IvParameterSpec(iv);

		cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
		byte[] encrypted = cipher.doFinal(input);

		return new sun.misc.BASE64Encoder().encode(encrypted);
	}

	public String decrypt(String c, byte[] key)
			throws UnsupportedEncodingException, GeneralSecurityException {

		byte[] cipherText = null;
		try {
			cipherText = new sun.misc.BASE64Decoder().decodeBuffer(c);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

		SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
		byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		IvParameterSpec ivspec = new IvParameterSpec(iv);

		cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
		byte[] plain = cipher.doFinal(cipherText);

		return new String(plain);
	}
	
//
//	public static void main(String[] args) throws NumberFormatException,
//			IOException {
//		new PeerClient(Integer.parseInt(args[0])).connect();
//	}

}
