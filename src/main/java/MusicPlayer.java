import java.util.ArrayList;
import java.util.Scanner;

import lib.lwjgl.glsound.*;

public class MusicPlayer {
	Runnable runn = new MusicPlayerRun();

	Scanner in = new Scanner(System.in);
	static MusicPlayerRun[] ths;

	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);
		SoundScape.create();
		boolean loop = true;

		ArrayList<String> sound = new ArrayList<String>();// where to get? File?
		sound.add("16. Unknown Artist - Track16.wav");
		sound.add("FootSteps.wav");
		sound.add("hit.wav");
		//sound.add();

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
			if (num == 999) {
				loop = false;
			}
			toDo = null;
			num = -1;
		}

		MusicPlayerRun t = new MusicPlayerRun();
		t.start();

		t.setSong(sound.get(num));

		String inp = in.nextLine();
		t.setStop(inp);

		// for (int i = 0; i < sound.size(); i++) {
		// t.setSong(sound.get(i));
		// }

	}

	public static void change(int ss, float pitch) {
		SoundScape.setPitch(ss, pitch);
	}

}
