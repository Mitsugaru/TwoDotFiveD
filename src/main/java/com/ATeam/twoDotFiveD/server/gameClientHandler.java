package com.ATeam.twoDotFiveD.server;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class gameClientHandler {
	private InetAddress address;
	private int port;
	public void set(InetAddress anAddress, int aPort){
		address=anAddress;
		port=aPort;
	}
	public DatagramPacket message(byte[] data){
		return new DatagramPacket(data, data.length, address, port);
	}
	public boolean active() {
		return address!=null;
	}
}
