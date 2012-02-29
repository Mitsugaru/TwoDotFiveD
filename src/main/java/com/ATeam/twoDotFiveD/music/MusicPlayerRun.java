package com.ATeam.twoDotFiveD.music;
import lib.lwjgl.glsound.*;

public class MusicPlayerRun extends Thread implements Runnable {
	String g;
	String sto = "";
	boolean player = true;
	boolean playSong = true;
	int ss;

	@Override
	public void run() {
		SoundScape.create();
	}

	void setSong(String inp) {
		g = inp;
		System.out.println(g);
		int soundData = SoundScape.loadSoundData(g);
		ss = SoundScape.makeSoundSource(soundData);
		SoundScape.play(ss);
	}

	void setStop(String x) {
		System.out.println(x);
		sto = x;
		while (player) {
			if (sto.equals("quit")) {
				player = false;
			}
		}
		SoundScape.destroy();

	}

	void pause() {
		if (playSong) {
			SoundScape.pause(ss, playSong);
			playSong = false;
		} else {
			SoundScape.pause(ss, playSong);
			playSong = true;
		}
	}

	void changePitch(float pitch) {
		if (pitch >= 0) {// makes sure it doesnt go out of bounds
			SoundScape.setPitch(ss, pitch);// somewhere above 0, 1 unchanged,
											// 2x=6dBx
		}
	}
	
	void changeVolume(float vol){
		SoundScape.setGain(ss, vol);
	}

}
