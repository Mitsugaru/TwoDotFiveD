package com.ATeam.twoDotFiveD.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

//TODO implement
public class MainOptionController implements ScreenController
{
	Nifty nifty;
	@Override
	public void bind(Nifty nifty, Screen screen)
	{
		this.nifty = nifty;
	}

	@Override
	public void onStartScreen()
	{
		// TODO Auto-generated method stub
		System.out.println("in options");
	}

	@Override
	public void onEndScreen()
	{
		// TODO Auto-generated method stub
		
	}
	
	public void changeScreen(String nextScreen)
	{
		nifty.gotoScreen(nextScreen); // switch to another screen
		// start the game and do some more stuff...
	}
	
}
