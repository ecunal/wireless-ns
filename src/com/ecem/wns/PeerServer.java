package com.ecem.wns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServer {

	private int port;

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
        
        out.println("Hi from server!");
        int count = 1;

        while((inputLine = in.readLine()) != null) {
        	
        	if(count == 5) {
        		System.out.println("that's the end!");
        		break;
        	}
        	
        	System.out.println("I'm server and I just read " + inputLine + ", also ctr = " + count);
        	out.println(inputLine + "...");
        	count++;
        }
        
        out.close();
        in.close();
        clientSocket.close();
        socket.close();
	}

}
