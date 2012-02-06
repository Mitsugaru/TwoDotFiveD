package com.ATeam.twoDotFiveD.gui;

import org.bushe.swing.event.EventTopicSubscriber;

import com.ATeam.twoDotFiveD.debug.Logging;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.SizeValue;
import de.lessvoid.nifty.controls.Menu;
import de.lessvoid.nifty.controls.MenuItemActivatedEvent;

public class HUDController implements ScreenController, KeyInputHandler
{
	private Nifty	nifty;
	private Screen screen;
	private Element rightClickMenu;
	
	@Override
	public boolean keyEvent(NiftyInputEvent inputEvent)
	{
		// TODO handle key events
		return false;
	}
	
	@Override
	public void bind(Nifty nifty, Screen screen)
	{
		this.nifty = nifty;
		this.screen = screen;
		createRightClickMenu();
	}
	
	private void createRightClickMenu() {
	    this.rightClickMenu = nifty.createPopup("niftyPopupMenu");
	  
	    Menu<RightClickMenu> popupMenu = rightClickMenu.findNiftyControl("#menu", Menu.class);
	  
	    popupMenu.setWidth(new SizeValue("250px"));
	    popupMenu.addMenuItem("MenuItem 1", "src/main/resources/button-red.png", new RightClickMenu("SomeId1", "You've clicked MenuItem 1"));
	    popupMenu.addMenuItem("MenuItem 4000000000000000000", "menu/stop.png", new RightClickMenu("SomeId2", "You've clicked a very odd MenuItem"));
	    popupMenu.addMenuItemSeparator();
	    popupMenu.addMenuItem("MenuItem 5", new RightClickMenu("SomeId5", "You've clicked MenuItem 5 (Where is 3?)"));
	    popupMenu.addMenuItem("MenuItem 6", new RightClickMenu("SomeId6", "You've clicked MenuItem 6"));
	    popupMenu.addMenuItemSeparator();
	    popupMenu.addMenuItem("Exit", new RightClickMenu("exit", "Good Bye! :)"));
	  }
	
	public void showMenu()
	{
		nifty.showPopup(screen, rightClickMenu.getId(), null);
		nifty.subscribe(screen, rightClickMenu.findNiftyControl("#menu", Menu.class).getId(), MenuItemActivatedEvent.class, new MenuItemActivatedEventSubscriber());
	}
	
	@Override
	public void onEndScreen()
	{
		// TODO cleanup?
	}
	
	@Override
	public void onStartScreen()
	{
		// TODO Auto-generated method stub
		
	}
	
	private class MenuItemActivatedEventSubscriber implements EventTopicSubscriber<MenuItemActivatedEvent> {
	    @Override
	    public void onEvent(final String id, final MenuItemActivatedEvent event) {
	      final RightClickMenu item = (RightClickMenu) event.getItem();

	      Logging.log.info("Right clicked: " + item.key + " :: " + item.text);

	      nifty.closePopup(rightClickMenu.getId(), new EndNotify() {
	        
	        @Override
	        public void perform() {
	          if ("exit".equals(item.key)) {
	            nifty.gotoScreen("mainmenu");
	          }
	        }
	      });
	    }
	  }
	
	private static class RightClickMenu 
	{
		public String key;
		public String text;
		
		public RightClickMenu(final String key, final String text)
		{
			this.key = key;
			this.text = text;
		}
	}
}
