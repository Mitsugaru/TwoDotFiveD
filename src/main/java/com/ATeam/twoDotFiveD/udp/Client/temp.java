package com.ATeam.twoDotFiveD.udp.Client;

import java.util.Random;

public class temp implements Runnable{
	UDPclient t;
	char[] abc={'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','z'};
	public temp(UDPclient t){
		this.t=t;
	}
	@Override
	public void run() {
		Random r=new Random();
		while(t.run){
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			char c=abc[r.nextInt(26)];
			t.sendMessage(new byte[] {(byte) c});
			t.pntr.updateText("UDP-sent: "+c);
		}
	}
}
