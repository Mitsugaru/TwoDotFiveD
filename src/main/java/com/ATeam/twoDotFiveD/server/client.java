package com.ATeam.twoDotFiveD.server;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

public class client implements Comparable, Runnable{
	private int ID=-1;
	private chatClientHandler chatClient;
	private gameClientHandler gameClient;
	private boolean run;
	private long ping;
	private long pingInitial;
	private long lastMessageTime;
	public client(chatServer chatServer, Socket socket) {
		chatClient=new chatClientHandler(chatServer,socket);
		gameClient=new gameClientHandler();
		run=true;
		updateAliveTime();
	}
	@Override
	public int compareTo(Object arg0) {
		if(arg0 instanceof client){
			return ID-((client) arg0).ID;
		}
		return 0;
	}
	public int getID(){
		return ID;
	}
	public void setID(int id){
		ID=id;
	}
	@Override
	public void run() {
		while(run){
			new Thread(chatClient).run();
			try {
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {e.printStackTrace();}
		}
	}
	public void initUDP(InetAddress address, int port){
		gameClient.set(address, port);
	}
	public DatagramPacket sendUDP(byte[] message){
		return gameClient.message(message);
	}
	public void updateAliveTime(){
		lastMessageTime=System.currentTimeMillis()/1000L;
	}
	public void pinginit(){
		pingInitial=System.currentTimeMillis()/1000L;
	}
	public void updateLatency(){
		ping=pingInitial-System.currentTimeMillis()/1000L;
	}
	public boolean active(){
		return gameClient.active();
	}
	public Long getLatency(){
		return ping;
	}
}