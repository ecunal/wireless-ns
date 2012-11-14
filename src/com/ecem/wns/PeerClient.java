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

	/* Messages to send to other peer */
	private final String[] messages = { "Hi", "Alright", "And you?", "Well, then", "bye" };
	
	/* Value of rekey string */
	private final String rekey = "rekey";

	public PeerClient(int port) {
		this.port = port;
	}

	public void connect() throws IOException, GeneralSecurityException {
		
		/* Establishing the connection */

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

		/*
		 * Connection established, reader and writer are ready for communication.
		 */
		
		String fromServer;
		int count = 0;
		
		/* Generating the first key from hash chains. */

		HashChainGenerator generator = new HashChainGenerator("firstkey",
				"secondkey", 100);
		byte[] forward = generator.getNextForwardKey();
		byte[] backward = generator.getNextBackwardKey();
		generator.incrementCounter();

		byte[] key = new byte[forward.length];
		for (int i = 0; i < key.length; i++) {
			key[i] = (byte) (forward[i] ^ backward[i]);
		}
		
		/* Beginning reading from other peer. */

		while ((fromServer = in.readLine()) != null) {
			
			System.out.println("Peer1: encrypted data: " + fromServer);

			/* Decrypt the received message */
			String msg = "";
			try {
				msg = decrypt(fromServer, key);
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}

			/* If the PeerServer does not end the communication,
			 * since it is the end of possible messages, we should end it.
			 */
			if (count == 5) {
				System.out.println("that's the end! -c");
				break;
			}
			/* If rekey command is sent, then get the next key */
			if (msg.equals(rekey)) {

				forward = generator.getNextForwardKey();
				backward = generator.getNextBackwardKey();
				generator.incrementCounter();

				key = new byte[forward.length];
				for (int i = 0; i < key.length; i++) {
					key[i] = (byte) (forward[i] ^ backward[i]);
				}
				
				System.out.println("Peer1: command: rekey\n");

				continue;
			}
			
			System.out.println("Peer1: message: \"" + msg + "\"\n");

			/*
			 * Since we've just read a message, we won't be sending data with
			 * the same key again. Therefore we are sending rekey command and
			 * updating the key ourselves.
			 */

			out.println(encrypt(rekey, key));
			
			forward = generator.getNextForwardKey();
			backward = generator.getNextBackwardKey();
			generator.incrementCounter();

			key = new byte[forward.length];
			for (int i = 0; i < key.length; i++) {
				key[i] = (byte) (forward[i] ^ backward[i]);
			}
			
			/* Sending a message with the newly constructed key */
			
			out.println(encrypt(messages[count], key));

			count++;
		}

		out.close();
		in.close();
		socket.close();

	}

	/**
	 * Symmetric key encryption function.
	 * Encrypts with AES algorithm using CBC mode and PKCS5 padding.
	 * 
	 * @param message
	 * 			String input to be encrypted.
	 * @param key
	 * 			byte[] key. It can be only 128, 192 and 256 bits long.
	 * @return
	 * 			Encrypted cipher text.
	 * @throws GeneralSecurityException
	 * @throws UnsupportedEncodingException
	 */
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

	/**
	 * Symmetric key decryption function. AES + CBC mode with PKCS5 padding scheme.
	 * 
	 * @param c
	 * 			Ciphertext to be decrypted.
	 * @param key
	 * 			byte[] key. Should be the same with encryption key.
	 * @return
	 * 			Decrypted plain text.
	 * @throws UnsupportedEncodingException
	 * @throws GeneralSecurityException
	 */
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

}
