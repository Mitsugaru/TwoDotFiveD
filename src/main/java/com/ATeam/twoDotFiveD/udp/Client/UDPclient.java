package com.ATeam.twoDotFiveD.udp.Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.ATeam.twoDotFiveD.gui.MainStartScreen;

public class UDPclient implements Runnable{
	InetAddress serverAddress;
	int serverPort;
	DatagramSocket socket;
	boolean run;
	MainStartScreen pntr;
	public UDPclient(InetAddress anAddress,int aPort,MainStartScreen dur){
		serverAddress=anAddress;
		serverPort=aPort;
		pntr=dur;
		try {
			socket=new DatagramSocket();
		} catch (SocketException e) {e.printStackTrace();}
	}

	public DatagramPacket makeMessage(byte[] message){
		return new DatagramPacket(message,message.length,serverAddress,serverPort);
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
			socket.send(new DatagramPacket(new byte[] {0x00},1,InetAddress.getLocalHost(),socket.getPort()));
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
		DatagramPacket receivePacket;
		byte[] receiveData = new byte[10];
		//this line for demo purposes, it simulates the program sending data
		new Thread(new temp(this)).run();
		while(run){
			//this is where data will be received need to know where to send it
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			pntr.updateText("UDP-recieved: "+(char) receiveData[0]);
		}
		socket.close();
	}
}