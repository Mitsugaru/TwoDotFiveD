package com.ATeam.twoDotFiveD.gui;

import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyInputMapping;
import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;

public class HUDInputMapping implements NiftyInputMapping
{
	
	@Override
	public NiftyInputEvent convert(KeyboardInputEvent inputEvent)
	{
		if (inputEvent.isKeyDown()) {
		      if (inputEvent.getKey() == KeyboardInputEvent.KEY_F1) {
		        return NiftyInputEvent.ConsoleToggle;
		      } else if (inputEvent.getKey() == KeyboardInputEvent.KEY_RETURN) {
		        return NiftyInputEvent.Activate;
		      } else if (inputEvent.getKey() == KeyboardInputEvent.KEY_SPACE) {
		        return NiftyInputEvent.Activate;
		      } else if (inputEvent.getKey() == KeyboardInputEvent.KEY_TAB) {
		        if (inputEvent.isShiftDown()) {
		          return NiftyInputEvent.PrevInputElement;
		        } else {
		          return NiftyInputEvent.NextInputElement;
		        }
		      } else if (inputEvent.getKey() == KeyboardInputEvent.KEY_UP) {
		        return NiftyInputEvent.MoveCursorUp;
		      } else if (inputEvent.getKey() == KeyboardInputEvent.KEY_DOWN) {
		        return NiftyInputEvent.MoveCursorDown;
		      }
		    }
		return null;
	}
	
}
