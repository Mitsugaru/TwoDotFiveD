package com.ATeam.twoDotFiveD.gui;

import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Chat;
import de.lessvoid.nifty.controls.ChatTextSendEvent;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.spi.render.RenderImage;
import de.lessvoid.nifty.elements.Element;

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
     * Send the text to the server.
     * @param text The text to send.
     */
    @NiftyEventSubscriber(id="chatId")
    public final void onSendText(final String id, final ChatTextSendEvent event) {
    	//TODO RESOURCE: http://jmonkeyengine.org/groups/gui/forum/topic/nifty-chat-box/
    	//TODO this is an event when a player enters a message
    	//TODO CLIFF CLIFF CLIFF CLIFF OMGWTFBBQ
            System.out.println("chat event received: " + event.getText());
            final Element chatPanel = nifty.getCurrentScreen().findElementByName("chatId");
            if(chatPanel == null)
            {
            	System.out.println("ELEMENTSHIT");
            }
            final Chat chatController = chatPanel.findNiftyControl("chatId", Chat.class);
            if(chatController == null)
            {
            	System.out.println("CHATSHIT");
            }
            NiftyImage temp = nifty.getRenderEngine().createImage("chat-icon-user.png", false);
            if(temp == null)
            {
            	System.out.println("OHSHIT");
            }
            chatController.receivedChatLine(">" + event.getText(), temp);
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
