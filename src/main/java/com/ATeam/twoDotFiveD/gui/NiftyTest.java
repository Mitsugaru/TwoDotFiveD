package com.ATeam.twoDotFiveD.gui;

import java.io.IOException;
import java.util.logging.Level;

import com.ATeam.twoDotFiveD.debug.Logging;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyMouse;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice;
import de.lessvoid.nifty.sound.openal.OpenALSoundDevice;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;
import de.niftygui.examples.LwjglInitHelper;

public class NiftyTest
{
	// Class variables
	private static String[]		arguments		= new String[0];
	// Resouce links
	private static final String	XML				= "src/main/resources/com/ATeam/twoDotFiveD/layout/main.xml";
	private static final String	MOUSE_CURSOR	= "src/main/resources/nifty-cursor.png";
	
	public void start()
	{
		if (LwjglInitHelper.initSubSystems("2.5D"))
		{
			LwjglRenderDevice render = new LwjglRenderDevice();
			Nifty nifty = new Nifty(render, new OpenALSoundDevice(),
					new LwjglInputSystem(), new AccurateTimeProvider());
			nifty.fromXml(XML, "intro");
			// get the NiftyMouse interface that gives us access to all mouse
			// cursor related stuff
			NiftyMouse niftyMouse = nifty.getNiftyMouse();
			
			// register/load a mouse cursor (this would be done somewhere at the
			// beginning)
			try
			{
				niftyMouse.registerMouseCursor("mouseId", MOUSE_CURSOR, 0, 0);
			}
			catch (IOException e)
			{
				Logging.log
						.log(Level.SEVERE, "Failed to load mouse cursor!", e);
			}
			
			// change the cursor to the one we've loaded before
			niftyMouse.enableMouseCursor("mouseId");
			
			// we could set the position like so
			niftyMouse.setMousePosition(20, 20);
			
			LwjglInitHelper.renderLoop(nifty, null);
			LwjglInitHelper.destroy();
		}
		else
		{
			Logging.log
					.log(Level.SEVERE,
							"Could not initialize LWJGL helper and subsystems. Nothing to display.");
		}
	}
	
	public static void main(final String[] args) throws IOException
	{
		// Grab and save arguments, if any
		arguments = args.clone();
		NiftyTest test = new NiftyTest();
		test.start();
		
	}
}
