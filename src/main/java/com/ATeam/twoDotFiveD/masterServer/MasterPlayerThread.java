package com.ATeam.twoDotFiveD.masterServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MasterPlayerThread implements Runnable{
	public static final String HandshakeR1 = "can has server list?";
	public static final String Handshake1 = "here it comes";
	public static final String HandshakeDone = "k thnxs bye";
	static private Scanner in;
	static private PrintWriter out;
	private Socket client;
	public MasterPlayerThread(Socket accept) {
		client=accept;
		try {
			in = new Scanner(client.getInputStream());
			out = new PrintWriter(client.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		if(in.nextLine().equals(HandshakeR1)){
			out.println(Handshake1);
			for(int i=0;i<Master.size();i++){
				out.println(Master.get(i).toString());
			}
			out.println(HandshakeDone);
		}
		in.close();
		out.close();
		try {
			client.close();
		} catch (IOException e) {e.printStackTrace();}
	}
}
