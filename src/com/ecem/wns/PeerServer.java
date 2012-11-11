package com.ecem.wns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServer {

	private int port;
	
	private final String[] messages = {"yes", "no", "alright", "good", "bad", "goodbye"};
	private final String rekey = "rekey";

	public PeerServer(int port) {
		this.port = port;
	}

	public void connect() throws IOException {

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
		
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
				new InputStreamReader(
				clientSocket.getInputStream()));
        
        String inputLine;
        int count = 0;

        System.out.println("Started!");
        
        out.println(messages[count]);
        count++;
        
        while((inputLine = in.readLine()) != null) {
        	
        	System.out.println("I'm server and I just read " + inputLine);
        	
        	if(inputLine.equals("bye")) {
        		System.out.println("that's the end!");
        		break;
        	}
        	else if(inputLine.equals(rekey)) {
        		// do key altering stuff
        		continue;
        	}
        	
        	out.println(messages[count]);
        	
        	//out.println("rekey");
        	count++;
        }
        
        out.close();
        in.close();
        clientSocket.close();
        socket.close();
	}
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		new PeerServer(Integer.parseInt(args[0])).connect();
	}

}
