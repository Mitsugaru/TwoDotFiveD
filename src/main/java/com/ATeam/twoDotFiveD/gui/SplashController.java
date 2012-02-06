package com.ATeam.twoDotFiveD.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class SplashController implements ScreenController, KeyInputHandler
{
	private Nifty	nifty;
	
	@Override
	public boolean keyEvent(NiftyInputEvent inputEvent)
	{
		// TODO fix
		if (inputEvent == NiftyInputEvent.Escape)
		{
			nifty.setAlternateKey("exit");
			nifty.gotoScreen("introcredits");
			return true;
		}
		return false;
	}
	
	@Override
	public void bind(final Nifty nifty, final Screen screen)
	{
		this.nifty = nifty;
	}
	
	@Override
	public void onEndScreen()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStartScreen()
	{
		nifty.gotoScreen("introcredits");
	}
	
}
