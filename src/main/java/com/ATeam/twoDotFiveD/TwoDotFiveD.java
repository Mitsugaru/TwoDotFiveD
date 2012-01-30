package com.ATeam.twoDotFiveD;

import com.ATeam.twoDotFiveD.debug.*;
import com.ATeam.twoDotFiveD.entity.Player;
import com.ATeam.twoDotFiveD.event.Dispatcher;
import com.ATeam.twoDotFiveD.event.Event;
import com.ATeam.twoDotFiveD.event.player.PlayerMoveEvent;

public class TwoDotFiveD {
	public static final String homeDir = System.getProperty("user.home") + System.getProperty("file.separator") + ".TwoDotFiveD";
	//Class variables
	public static Logging logger = new Logging(TwoDotFiveD.class.getName(), homeDir);
	private static Debug debug = new Debug();
	private static Dispatcher dispatcher = new Dispatcher();

	/**
	 * Main constructor
	 */
	public TwoDotFiveD()
	{
		//Log info
		debug.systemInfo();
		debug.memoryInfo();
		//Event test
		dispatcher.registerListener(Event.Type.PLAYER_MOVE, new ListenerTest());
		final Player player = new Player();
		dispatcher.notify(new PlayerMoveEvent(player));
		//TODO initialize screen
		//TODO initialize nifty-gui main menu
	}

	//TODO create a method that saves all info and safely stop the program

	/**
	 * Starts the program
	 */
	public void start()
	{
		//TODO create run method stuff here
	}

	/**
	 * Main method
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		//Create instance
		TwoDotFiveD program = new TwoDotFiveD();
		//Run/start instance
		program.start();
	}

}
