package com.ATeam.twoDotFiveD.music;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import lib.lwjgl.glsound.*;

public class MusicPlayer {
	Runnable runn = new MusicPlayerRun();

	Scanner in = new Scanner(System.in);
	static ArrayList<String> sound = new ArrayList<String>();
	// static ArrayList<String> music=new ArrayList<String>();//later when
	// focusing on levels
	static MusicPlayerRun[] ths;

	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);
		SoundScape.create();
		boolean loop = true;

		getSound();

		ths = new MusicPlayerRun[sound.size()];
		int num = 0;// default
		String toDo;

		while (loop) {
			num = in.nextInt();// what noise
			toDo = in.next();// play, pause, stop

			if (toDo.equals("play")) {
				ths[num] = new MusicPlayerRun();
				ths[num].start();// start a thread
				ths[num].setSong(sound.get(num));
			}
			if (toDo.equals("pause")) {
				ths[num].pause();
			}
			if (toDo.equals("stop")) {
				ths[num].setStop("quit");
			}
			if(toDo.equals("vol")){
				changeVol(ths[num]);
			}
			if(toDo.equals("pit")){
				changePitch(ths[num]);
			}
			if (num == 999) {
				loop = false;
			}
			toDo = null;
			num = -1;
			// changeVol(ths[num]);
		}

	}

	public MusicPlayer() {

	}

	public void createThread(int i) {//problem
		ths[i] = new MusicPlayerRun();
		ths[i].start();// start a thread
		ths[i].setSong(sound.get(i));
		System.out.println(sound.get(i));
	}

	public static void getSound() {
		try {
			FileReader reader = new FileReader("Sounds.txt");
			Scanner scanner = new Scanner(reader);
			while (scanner.hasNextLine()) {
				sound.add(scanner.nextLine());
			}
			scanner.close();
		} catch (IOException e) {
		}

	}

	public static void changeVol(MusicPlayerRun ths2) {
		float vol=1;
		try {
			FileReader reader = new FileReader("Sounds.txt");
			Scanner scanner = new Scanner(reader);
			vol = scanner.nextFloat();
			System.out.println(vol);
			scanner.close();
		} catch (IOException e) {
		}
		ths2.changeVolume(vol);
	}

	public static void changePitch(MusicPlayerRun ths2) {
		float pit = 1;// 1 is default value
		try {
			FileReader reader = new FileReader("Sounds.txt");
			Scanner scanner = new Scanner(reader);
			pit = scanner.nextFloat();
			scanner.close();
		} catch (IOException e) {
		}
		ths2.changePitch(pit);
		System.out.println(pit);
	}

}
