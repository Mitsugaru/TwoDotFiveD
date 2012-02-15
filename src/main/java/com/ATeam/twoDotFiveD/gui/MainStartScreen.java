package com.ATeam.twoDotFiveD.gui;

import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class MainStartScreen implements ScreenController
{
	private Nifty				nifty;
	private Screen				screen;
	private static final String	HUD_XML	= "com/ATeam/twoDotFiveD/layout/hud.xml";
	
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
		// INFO nothing to do since its the main menu screen
	}
	
	/**
	 * Called to exit the screen
	 */
	public void exit()
	{
		nifty.createPopupWithId("popupExit", "popupExit");
		nifty.showPopup(screen, "popupExit", null);
	}
	
	public void beginGame()
	{
		nifty.fromXml(HUD_XML, "hud");
		//TODO check for existing game in progress and ask
		DisplayStuff.setRenderNifty(true);
	}
	
	public void changeScreen(String nextScreen)
	{
		nifty.gotoScreen(nextScreen); // switch to another screen
		// start the game and do some more stuff...
	}
	
	/**
	 * popupExit.
	 * 
	 * @param exit
	 *            exit string
	 */
	public void popupExit(final String exit)
	{
		nifty.closePopup("popupExit", new EndNotify() {
			public void perform()
			{
				if ("yes".equals(exit))
				{
					nifty.setAlternateKey("fade");
					nifty.exit();
					DisplayStuff.destroy();
				}
			}
		});
	}
	
}
