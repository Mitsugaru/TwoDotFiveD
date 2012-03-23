package com.ATeam.twoDotFiveD.udp.Client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.ATeam.twoDotFiveD.event.Event;
import com.ATeam.twoDotFiveD.event.EventDispatcher;
import com.ATeam.twoDotFiveD.event.block.BlockCreateEvent;
import com.ATeam.twoDotFiveD.gui.MainStartScreen;

public class UDPclient implements Runnable{
	InetAddress serverAddress;
	int serverPort;
	DatagramSocket socket;
	boolean run;
	MainStartScreen pntr;
	EventDispatcher eventdispatcher;
	int ID;
	public UDPclient(InetAddress anAddress,int aPort,MainStartScreen dur,int ID){
		serverAddress=anAddress;
		serverPort=aPort;
		pntr=dur;
		this.ID=ID;
		run=true;
		try {
			socket=new DatagramSocket();
		} catch (SocketException e) {e.printStackTrace();}
	}
	public UDPclient(InetAddress anAddress,int aPort,MainStartScreen dur,int ID, EventDispatcher event){
		serverAddress=anAddress;
		serverPort=aPort;
		pntr=dur;
		this.ID=ID;
		eventdispatcher=event;
		run=true;
		try {
			socket=new DatagramSocket();
		} catch (SocketException e) {e.printStackTrace();}
	}


	public DatagramPacket makeMessage(byte[] message){
		byte[] tmp = new byte[message.length+1];
		System.arraycopy(message, 0, tmp, 1, message.length);
		tmp[0]=(byte) ID;
		return new DatagramPacket(tmp,tmp.length,serverAddress,serverPort);
	}
	public void sendMessage(byte[] message) {
		send(makeMessage(message));
	}
	public void send(DatagramPacket packet){
		try {
			socket.send(packet);
		} catch (IOException e) {e.printStackTrace();}
	}
	public void quit(){
		run=false;
		try {
			socket.send(new DatagramPacket(new byte[] {0x00},1,InetAddress.getLocalHost(),9999));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		System.out.println("hi");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("hi2");
		DatagramPacket receivePacket;
		byte[] receiveData = new byte[512];
		//this line for demo purposes, it simulates the program sending data
		new Thread(new temp(this)).start();
		while(run){
			//this is where data will be received need to know where to send it
			System.out.println("CLIENT LISTENING");
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				socket.receive(receivePacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
//		      try {
//		    	  ByteArrayInputStream baos = new ByteArrayInputStream(receiveData);
//				ObjectInputStream oos = new ObjectInputStream(baos);
//				BlockCreateEvent event = (BlockCreateEvent)oos.readObject();
//				eventdispatcher.notify(event);
			//} 
//			catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		      
			
		//	eventdispatcher.notify(event)
			pntr.updateText(String.format("UDP-recieved from: %c:%c",(byte) receiveData[0],(char) receiveData[1]));
			System.out.println("revcieved");
		}
		socket.close();
	}
}