package com.ATeam.twoDotFiveD.masterServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class MasterPlayerInfo implements Runnable {
	int port;
	boolean run;
	public MasterPlayerInfo(int playerInfoPort) {init(playerInfoPort);}
	public MasterPlayerInfo(){init(10001);}
	public void init(int aPort){
		port=aPort;
		run=true;
	}

	@Override
	public void run() {
		try {
			ServerSocket server = new ServerSocket(port);
			while(run){
				new Thread(new MasterPlayerThread(server.accept())).run();
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
