package com.ATeam.twoDotFiveD.server;

import java.util.ArrayList;
import java.util.Collections;

import com.Ateam.twoDotFived.udp.server.UDPServer;

public class ServerMain implements Runnable{
	private ArrayList<client> players=new ArrayList<client>();
	private boolean run=false;
	private String name;
	private int chatport;
	private int gamestateport;
	private UDPServer gs;
	private chatServer cs;
	public static void main(String[] a){
		new Thread(new ServerMain()).run();
	}
	public ServerMain(String aName,int Achatport, int aGameStatePort){
		init(aName,Achatport,aGameStatePort);
	}
	public ServerMain(String aName){
		init(aName,9999,9998);
	}
	public ServerMain(){
		init("HERP DERP NO NAME",9999,9998);
	}
	private void init(String aName, int Achatport, int aGameStatePort){
		name=aName;
		run=true;
		chatport=Achatport;
		gamestateport=aGameStatePort;
	}
	@Override
	public void run() {
//		cs=new chatServer(players);
//		gs=new UDPServer(players);
		new Thread(cs).run();
		new Thread(gs).run();
		try {
		while(run){
				Thread.sleep(1000);
			} 
		}
		catch (InterruptedException e) {e.printStackTrace();}
		//this is not a graceful exit;  will need to develop a graceful protocol;
		cs.quit();
		gs.quit();
	}
	public void register(){
		//TODO register with master servers
	}
	public void quit(){
		run=false;
	}
	public boolean getState(){
		return run;
	}
}
