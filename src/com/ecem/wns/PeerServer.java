package com.ecem.wns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PeerServer {

	private int port;

	/* Messages to send to other peer */
	private final String[] messages = { "yes", "no", "alright", "good", "bad",
			"goodbye" };
	/* Value of rekey string */
	private final String rekey = "rekey";

	public PeerServer(int port) {
		this.port = port;
	}

	public void connect() throws IOException, GeneralSecurityException {

		/* Establishing the connection */
		
		ServerSocket socket = null;

		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Could not listen on port " + port);
			System.exit(1);
		}

		Socket clientSocket = null;

		try {
			clientSocket = socket.accept();
		} catch (IOException e) {
			System.err.println("Accept failed.");
			System.exit(1);
		}
		
		/* Connection established, initialize the reader and writer for communication. */

		PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));

		String inputLine;
		int count = 0;
		
		/* Generate the first key to start with. */

		HashChainGenerator generator = new HashChainGenerator("firstkey",
				"secondkey", 100);
		byte[] forward = generator.getNextForwardKey();
		byte[] backward = generator.getNextBackwardKey();
		generator.incrementCounter();

		byte[] key = new byte[forward.length];
		for (int i = 0; i < key.length; i++) {
			key[i] = (byte) (forward[i] ^ backward[i]);
		}

		System.out.println("Started!");
		
		/* Send first message, initiate the communication */

		out.println(encrypt(messages[count], key));
		count++;

		while ((inputLine = in.readLine()) != null) {

			String msg = "";
			try {
				msg = decrypt(inputLine, key);
			} catch (GeneralSecurityException e) {
				e.printStackTrace();
			}

			System.out.println("Server: cipher = " + inputLine
					+ "\nServer: msg = " + msg);

			if (msg.equals("bye")) {
				System.out.println("that's the end!");
				break;
			} else if (msg.equals(rekey)) {

				forward = generator.getNextForwardKey();
				backward = generator.getNextBackwardKey();
				generator.incrementCounter();

				key = new byte[forward.length];
				for (int i = 0; i < key.length; i++) {
					key[i] = (byte) (forward[i] ^ backward[i]);
				}

				continue;
			} else if (count == 5) {
				break;
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
		clientSocket.close();
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

	// public static void main(String[] args) throws NumberFormatException,
	// IOException {
	// new PeerServer(Integer.parseInt(args[0])).connect();
	// }

}
