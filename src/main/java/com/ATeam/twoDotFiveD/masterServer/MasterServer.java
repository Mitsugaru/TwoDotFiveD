package com.ATeam.twoDotFiveD.masterServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class MasterServer implements Runnable{
	int port;
	boolean run=true;
	Master pntr;
	public MasterServer(int aPort, Master aPntr){init(aPort);}
	public MasterServer(Master aPntr){init(10000);}
	public void init(int aPort){
		port=aPort;
	}
	@Override
	public void run() {
		try {
			ServerSocket server=new ServerSocket(port);
			while(run){
				new Thread(new MasterServerThread(server.accept())).run();
			}
			server.close();
		} 
		catch (IOException e) {e.printStackTrace();}
	}
	public void quit(){
		try {
			run=false;
			Socket socket = new Socket("127.0.0.1", port);
			socket.close();
		} 
		catch (UnknownHostException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
	}
}
