package com.ATeam.twoDotFiveD.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class gameServer implements Runnable {
	//NEW ID included as second hex
	public static final byte NEW = (byte) 0xC0;
	//INIT welcome message returned to client, second hex is ID
	public static final byte INIT=(byte) 0x80;
	//DATA second half is ID, followed by data
	public static final byte DATA = 0x40;
	//Ping second half ID, used to check latency
	public static final byte PING = 0x00;
	public static final int DEFAULTPORT=9876;

	private ArrayList<client> clients;
	private DatagramSocket socket;
	private int port;
	private boolean run;

	public gameServer(ArrayList<client> players) {
		clients=players;
		port=DEFAULTPORT;
		try {
			socket=new DatagramSocket(port);
		} 
		catch (SocketException e) {e.printStackTrace();}
	}
	public gameServer(ArrayList<client> players,  int aPort) {
		clients=players;
		port=aPort;
	}

	public void quit() {
		// TODO Auto-generated method stub

	}
	@Override
	public void run() {
		byte[] receiveData = new byte[512];
		DatagramPacket receivePacket;
		byte id;
		client pntr;
		try {
			while(run)
			{
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				socket.receive(receivePacket);
				id=(byte) (receiveData[0]&0x0F);
				pntr = getByID(id);
				switch((byte) (receiveData[0]&0xC0)){

				default: System.out.println("default");

				case NEW:
					if(pntr!=null){
						pntr.initUDP(receivePacket.getAddress(), receivePacket.getPort());
						socket.send(pntr.sendUDP(new byte[] {(byte) ( INIT|id)}));
						pntr.updateAliveTime();
					}
					break;
				case INIT:break;
				case PING:
					if(pntr!=null){
						pntr.updateLatency();
						pntr.updateAliveTime();
					}
				case DATA:
					if(pntr!=null)
						pntr.updateAliveTime();
						for(client c: clients)
							if(c.getID()!=id)
								socket.send(pntr.sendUDP(receivePacket.getData()));
				}
			}
		} catch (IOException e) {e.printStackTrace();}
	}
	public client getByID(byte ID){
		for(client c: clients)
			if((byte) c.getID() == ID)
				return c;
		return null;			
	}

}
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
						server.send(c.sendUDP(new byte[] {(byte) (gameServer.PING|c.getID())}));
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

}
