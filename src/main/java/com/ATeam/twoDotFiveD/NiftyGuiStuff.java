package com.ATeam.twoDotFiveD;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

public class NiftyGuiStuff implements ScreenController{
	private Nifty nifty;
	private Screen screen;
	
	public NiftyGuiStuff(){
		System.out.println("HUI");
	}
	
	public void bind(final Nifty newNifty, final Screen newScreen) {
		screen = newScreen;
		nifty = newNifty;
		System.out.println("HUI2c");
		DropDown dropDown1 = findDropDownControl("dropDown1");
		//if (dropDown1 != null) {
		{
			dropDown1.addItem("Nifty GUI");
			dropDown1.addItem("Slick2d");
			dropDown1.addItem("Lwjgl");

			dropDown1.selectItemByIndex(0);
		}

	}

	private DropDown findDropDownControl(final String id) {
		return screen.findNiftyControl(id, DropDown.class);
	}

	
	public void onStartScreen() {
		System.out.println("HUI2");
		
	}

	public void onEndScreen() {
		// TODO Auto-generated method stub
	}
}
