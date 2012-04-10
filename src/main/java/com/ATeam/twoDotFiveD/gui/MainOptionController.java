package com.ATeam.twoDotFiveD.gui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

//TODO implement
public class MainOptionController implements ScreenController {
	Nifty nifty;
	Screen screen;
	private TextField optionsTextField;
	private DropDown dropDown1;
	private String dis;

	@Override
	public void bind(Nifty newNifty, Screen newScreen) {
		screen = newScreen;
		nifty = newNifty;

		dropDown1 = findDropDownControl("dropDown1");
		if (dropDown1 != null) {
			dropDown1.addItem("Display Width");
			dropDown1.addItem("Display Height");
			dropDown1.addItem("Display Frequency");

			dropDown1.selectItemByIndex(0);
		}
		
		optionsTextField= screen.findNiftyControl("mainTextField", TextField.class);
		dis=optionsTextField.getText();
	}

	@Override
	public void onStartScreen() {
		// TODO Auto-generated method stub
		System.out.println("in options");
	}

	@Override
	public void onEndScreen() {
		// TODO Auto-generated method stub

	}

	public void changeScreen(String nextScreen) {
		nifty.gotoScreen(nextScreen); // switch to another screen
		// start the game and do some more stuff...
	}

	private DropDown findDropDownControl(final String id) {
		return screen.findNiftyControl(id, DropDown.class);
	}

}
