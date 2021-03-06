package com.ecem.wns;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		
		final int port = 8845;
		
		Thread server = new Thread() {
			public void run() {
				try {
					(new PeerServer(port)).connect();
				} catch (IOException | GeneralSecurityException e) {
					e.printStackTrace();
				}
			}
		};
		
		Thread client = new Thread() {
			public void run() {
				try {
					(new PeerClient(port)).connect();
				} catch (IOException | GeneralSecurityException e) {
					e.printStackTrace();
				}
			}
		};
		
		server.start();
		
		Thread.sleep(1000);
		
		client.start();
	}

}
