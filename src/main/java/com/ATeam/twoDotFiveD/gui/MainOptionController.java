package com.ATeam.twoDotFiveD.gui;

import com.ATeam.twoDotFiveD.Config;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.controls.TextFieldChangedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

//TODO implement
public class MainOptionController implements ScreenController {
	Nifty nifty;
	Screen screen;

	//variables of the gui 
	private TextField optionsTextField;
	@SuppressWarnings("rawtypes")
	private DropDown dropDown1;

	//getting info from gui to move to config
	private String dis;
	int tempWidth=Config.displayWidth;
	int tempHeight=Config.displayHeight;
	int tempFreq=Config.displayFrequency;
	

	@SuppressWarnings("unchecked")
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


	}

	@NiftyEventSubscriber(id = "ip")
	public void onoptionsTextFieldChanged(final String id,
			final TextFieldChangedEvent event) {
		dis = optionsTextField.getText();// reads from the text field
	
		changeConfigVars();
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

	@SuppressWarnings("rawtypes")
	private DropDown findDropDownControl(final String id) {
		return screen.findNiftyControl(id, DropDown.class);
	}

	public void changeConfigVars() {
		int disint = Integer.parseInt(dis);
		int opt = dropDown1.getSelectedIndex();
		
		if (opt == 0) {
			Config.displayWidth = disint;
			
		} else if (opt == 1) {
			Config.displayHeight = disint;
			
		} else {
			Config.displayFrequency = disint;
			
		}
	}

}
