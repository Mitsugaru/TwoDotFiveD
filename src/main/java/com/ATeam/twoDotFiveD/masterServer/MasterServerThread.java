package com.ATeam.twoDotFiveD.masterServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class MasterServerThread implements Comparable, Runnable {
	public static final String Handshake1 = "Oh Hi";
	public static final String HandshakeR1 = "Hey There";
	public static final String Handshake2 = "Can Has Name?";
	public static final String HandshakeR2 = "For sure!";
	public static final String HandshakeF = "Have Fun";
	public static final String HandshakeP = "port please";
	public static final String Update = "Can has status?";
	public static final String UpdateRF = "no mas";
	public static final String UpdateF = "tnx";
	public static final String Ping = "Ping";
	public static final String Pong = "Pong";
	public static final String GetName = "name?";

	static private Scanner in;
	static private PrintWriter out;
	private Socket gameServer;
	byte alive;
	private int port;
	private InetAddress address;
	private String name;
	private int ID;

	public MasterServerThread(Socket connection) {
		gameServer = connection;
		address = gameServer.getInetAddress();
		alive=(byte) 0xFF;
		try {
			in = new Scanner(connection.getInputStream());
			out = new PrintWriter(connection.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		if(handshake()){
			out.println(GetName);
			name=in.nextLine();			
			while(!in.nextLine().equals("BYE")){
				try {
					Thread.sleep(10000);
					update();
				} catch (InterruptedException e) {e.printStackTrace();}
			}
		}
		in.close();
		out.close();
		try {
			gameServer.close();
		} catch (IOException e) {e.printStackTrace();}
	}
	public void update(){
		out.println(Update);
		String cmd;
		String value;
		do{
			cmd=in.nextLine();
			if(!cmd.equals(UpdateRF)){
				value=in.nextLine();
				// TODO will we have statistics?  Otherwise this can go away!
			}
		} while(!cmd.equals(UpdateRF));
	}
	public boolean handshake() {
		out.println(Handshake1);
		if(in.nextLine().equals(HandshakeR1)){
			out.println(Handshake2);
			if(in.nextLine().equals(HandshakeR2)){
				out.println(HandshakeF);
				out.println(HandshakeP);
				port=Integer.parseInt(in.nextLine());
				return true;
			}
		}
		return false;
	}
	@Override
	public int compareTo(Object o) {
		if (o instanceof MasterServerThread) {
			return ID - ((MasterServerThread) o).getID();
		}
		return 0;
	}
	public boolean equals(Object o){
		if (o instanceof MasterServerThread) {
			return ID==((MasterServerThread) o).getID();
		}
		return false;
	}
	public int getID() {return ID;}
	public void setID(int newID) {ID=newID;}
	public String toString(){return String.format("%s:%d",address.toString(),port);}
}
