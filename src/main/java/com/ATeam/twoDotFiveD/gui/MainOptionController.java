package com.ATeam.twoDotFiveD.gui;

import com.ATeam.twoDotFiveD.Config;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.controls.TextFieldChangedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.screen.ScreenController;

//TODO implement
public class MainOptionController implements ScreenController {
	Nifty nifty;
	Screen screen;
	private TextField optionsTextField;
	private DropDown dropDown1;
	private String dis;
	Config config = new Config();

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

		optionsTextField = screen.findNiftyControl("ip", TextField.class);// makes
																			// a
																			// textfield
		// passwordCharCheckBox =
		// screen.findNiftyControl("passwordCharCheckBox",
		// CheckBox.class);//checkbobx for fullsceen

		// optionsTextField.setText("%%%%");

	}

	@NiftyEventSubscriber(id = "ip")
	public void onoptionsTextFieldChanged(final String id,
			final TextFieldChangedEvent event) {
		dis = optionsTextField.getText();// reads from the text field
	
		displayText();
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

	public void displayText() {
		int disint = Integer.parseInt(dis);
		int opt = dropDown1.getSelectedIndex();
		
		if (opt == 0) {
			config.displayWidth = disint;
			
		} else if (opt == 1) {
			config.displayHeight = disint;
			
		} else {
			config.displayFrequency = disint;
			
		}
	}

}
