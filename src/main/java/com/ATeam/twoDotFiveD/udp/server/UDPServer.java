package com.ATeam.twoDotFiveD.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import com.ATeam.twoDotFiveD.chatServer.clientHandler;

public class UDPServer implements Runnable {
	//NEW ID included as second hex
	public static final byte NEW = (byte) 0xC0;
	//INIT welcome message returned to client, second hex is ID
	public static final byte INIT=(byte) 0x80;
	//DATA second half is ID, followed by data
	public static final byte DATA = 0x40;
	//Ping second half ID, used to check latency
	public static final byte PING = 0x00;
	public static final int DEFAULTPORT=9876;

	private ArrayList<clientHandler> clients;
	private DatagramSocket socket;
	private int port;
	private boolean run;

	public UDPServer(ArrayList<clientHandler> players) {
		init(players,DEFAULTPORT);
	}
	public UDPServer(ArrayList<clientHandler> players,  int aPort) {
		init(players,port);
	}
	private void init(ArrayList<clientHandler> players,  int aPort){
		port=aPort;
		clients=players;
		run=true;
		try {
			socket=new DatagramSocket(port);
		} 
		catch (SocketException e) {e.printStackTrace();}
	}

	public void quit() {
		// TODO Auto-generated method stub

	}
	@Override
	public void run() {
		byte[] receiveData = new byte[2560];
		//System.out.println("UDP PORT"+port);
		DatagramPacket receivePacket;
		byte id;
		clientHandler pntr;
		try {
			//System.out.println("Server Ready");
			while(run)
			{
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				//System.out.println("Listen");
				socket.receive(receivePacket);

				id=(byte) (receiveData[0]&0x0F);
				//System.out.println("Server Recieve From:" + id);
				//System.out.println(id);
				pntr = getByID(id);
				if(!pntr.initialized()){
					pntr.init(receivePacket.getAddress(), receivePacket.getPort());
					//System.out.println("init:"+pntr.getID());
				}
				try
				{
				if(receiveData[1]!=(byte) 0xFF){
					for(clientHandler c :clients){
						//System.out.println(c.getname()+":"+c.getID());
						if((!c.equals(pntr))){
							if(c.initialized()){
								//System.out.println("Relayed message from:"+pntr.getID()+" to:"+c.getID());
								socket.send(c.message(receiveData));
							}
						}
					}
				}
				}
				catch(ConcurrentModificationException c)
				{
					//IGNORE
				}
				//				switch((byte) (receiveData[0]&0xC0)){
				//
				//				default: System.out.println("default");
				//
				//				case NEW:
				//					if(pntr!=null){
				//						pntr.init(receivePacket.getAddress(), receivePacket.getPort());
				//						socket.send(pntr.message(new byte[] {(byte) ( INIT|id)}));
				//						pntr.updateAliveTime();
				//					}
				//					break;
				//				case INIT:break;
				//				case PING:
				//					if(pntr!=null){
				//						pntr.updateLatency();
				//						pntr.updateAliveTime();
				//					}
				//				case DATA:
				//					if(pntr!=null)
				//						pntr.updateAliveTime();
				//						for(clientHandler c: clients)
				//							if(c.getID()!=id)
				//								socket.send(pntr.message(receivePacket.getData()));
				//				}
			}
		} catch (IOException e) {e.printStackTrace();}
	}
	public clientHandler getByID(byte ID){
		for(clientHandler c: clients)
			if((byte) c.getID() == ID)
				return c;
		return null;			
	}

}

