package com.ATeam.twoDotFiveD;

import com.ATeam.twoDotFiveD.Debug.*;

public class TwoDotFiveD {
	//Class variables
	public static Logging logger = new Logging(TwoDotFiveD.class.getName());
	private static Debug debug = new Debug();

	/**
	 * Main constructor
	 */
	public TwoDotFiveD()
	{
		//Log info
		debug.systemInfo();
		debug.memoryInfo();
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
