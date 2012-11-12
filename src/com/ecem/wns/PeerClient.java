package com.ecem.wns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerClient {

	private int port;
	
	private final String[] messages = {"hi", "hello", "hey", "yey", "bye"};
	private final String rekey = "rekey";

	public PeerClient(int port) {
		this.port = port;
	}

	public void connect() throws IOException {

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

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(
				System.in));
		String fromServer;
		//String fromUser;
		int count = 0;

		while ((fromServer = in.readLine()) != null) {
			
			System.out.println("I'm client and I just read " + fromServer);

			if (count == 5) {
				System.out.println("that's the end! -c");
				break;
			}
			
			if(fromServer.equals(rekey)) {
				// do key altering stuff
				continue;
			}
			
			out.println(rekey);
			out.println(messages[count]);

//			getting input from user and stuff
			
//			fromUser = stdIn.readLine();
//			if (fromUser != null) {
//				out.println(fromUser);
//			}
			
			count++;
		}

		out.close();
		in.close();
		stdIn.close();
		socket.close();

	}
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		new PeerClient(Integer.parseInt(args[0])).connect();
	}

}
