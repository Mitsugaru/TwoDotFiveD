package com.ATeam.twoDotFiveD.masterServer;

import java.util.ArrayList;
import java.util.Collections;

public class Master implements Runnable{

	
	private static ArrayList<MasterServerThread> servers = new ArrayList<MasterServerThread>();
	private MasterServer ms;
	private MasterPlayerInfo ps;
	boolean quit=false;
	public Master(int ServerPort, int PlayerInfoPort){
		init(ServerPort, PlayerInfoPort);
	}
	public Master() {
		init(10000,10001);
	}
	public void init(int ServerPort, int PlayerInfoPort){
		ms=new MasterServer(ServerPort, this);
		ps=new MasterPlayerInfo(PlayerInfoPort);
	}
	public static void main(String args[]){
		Master dur=new Master();
		Thread Tdur=new Thread(dur);
		Tdur.run();
	}
	@Override
	public void run() {
		Thread Tms = new Thread(ms);
		Thread Tps = new Thread(ps);
		Tms.run();
		Tps.run();
		while(!quit){
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {e.printStackTrace();}
		}
		ms.quit();
		ps.quit();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {e.printStackTrace();}
	}
	public void quit(){
		quit=true;
	}
	public static synchronized void newServer(MasterServerThread newServer){
		if(servers.isEmpty())
			newServer.setID(1);
		else if(servers.get(servers.size()-1).getID()==servers.size())
			newServer.setID(servers.size());
		else
			for(int i=0;i<servers.size();i++)
				if(servers.get(i).getID()!=i+1){
					servers.get(i).setID(i+1);
					break;
				}
		servers.add(newServer);
		Collections.sort(servers);
	}
	public static synchronized void ServerQuit(MasterServerThread closingServer){
		servers.remove(closingServer);
	}
	public static synchronized MasterServerThread get(int index){return servers.get(index);}
	public static synchronized int size(){return servers.size();}
	
}
