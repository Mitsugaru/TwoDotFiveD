package com.ATeam.twoDotFiveD.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class MainStartScreen implements ScreenController
{
	private Nifty	nifty;
	private Screen	screen;
	
	// http://jmonkeyengine.org/wiki/doku.php/jme3:advanced:nifty_gui_java_interaction
	@Override
	public void bind(Nifty nifty, Screen screen)
	{
		this.nifty = nifty;
		this.screen = screen;
	}
	
	@Override
	public void onStartScreen()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onEndScreen()
	{
		// TODO clean up / save?
	}
	
	/**
	 * Called to exit the screen
	 */
	public void exit()
	{
		nifty.exit();
	}
	
	public void changeScreen(String nextScreen)
	{
		nifty.gotoScreen(nextScreen); // switch to another screen
		// start the game and do some more stuff...
	}
	
}
