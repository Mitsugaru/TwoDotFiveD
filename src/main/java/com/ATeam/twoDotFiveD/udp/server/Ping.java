package com.ATeam.twoDotFiveD.udp.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.util.ArrayList;

import com.ATeam.twoDotFiveD.server.client;

class ping implements Runnable{
	private ArrayList<client> clients = new ArrayList<client>();
	private DatagramSocket server;
	private boolean run;
	public ping(ArrayList<client> theClients,DatagramSocket theServer){
		server=theServer;
		clients=theClients;
		run=true;
	}
	@Override
	public void run() {
		while(run){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {e.printStackTrace();}
			for(client c: clients){
				try {
					if(c.active()){
						c.pinginit();
						server.send(c.sendUDP(new byte[] {(byte) (UDPServer.PING|c.getID())}));
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}