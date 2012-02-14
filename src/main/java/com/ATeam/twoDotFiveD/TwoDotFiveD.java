package com.ATeam.twoDotFiveD;

import java.io.IOException;
import java.util.logging.Level;

import com.ATeam.twoDotFiveD.debug.*;
import com.ATeam.twoDotFiveD.entity.Player;
import com.ATeam.twoDotFiveD.event.Dispatcher;
import com.ATeam.twoDotFiveD.event.Event;
import com.ATeam.twoDotFiveD.event.player.PlayerMoveEvent;
import com.ATeam.twoDotFiveD.gui.DisplayStuff;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyMouse;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice;
import de.lessvoid.nifty.sound.openal.OpenALSoundDevice;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;
import de.niftygui.examples.LwjglInitHelper;

public class TwoDotFiveD
{
	// Class variables
	public static final String	homeDir			= System.getProperty("user.home")
														+ System.getProperty("file.separator")
														+ ".TwoDotFiveD";
	public static Logging		logger			= new Logging(homeDir);
	public static Config		config			= new Config(homeDir);
	private static Debug		debug			= new Debug();
	private static Dispatcher	dispatcher		= new Dispatcher();
	private static final String	XML				= "com/ATeam/twoDotFiveD/layout/main.xml";
	private static final String	MOUSE_CURSOR	= "nifty-cursor.png";
	
	/**
	 * Main constructor
	 */
	public TwoDotFiveD()
	{
		// Log info
		debug.systemInfo();
		debug.memoryInfo();
		// Event test
		dispatcher.registerListener(Event.Type.PLAYER_MOVE, new ListenerTest());
		final Player player = new Player();
		dispatcher.notify(new PlayerMoveEvent(player));
		// TODO initialize screen
		DisplayStuff.initSubSystems("2.5D");
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
			Logging.log.log(Level.SEVERE, "Failed to load mouse cursor!", e);
		}
		
		// change the cursor to the one we've loaded before
		niftyMouse.enableMouseCursor("mouseId");
		
		// we could set the position like so
		niftyMouse.setMousePosition(20, 20);
		
		DisplayStuff.renderLoop(nifty, null);
		DisplayStuff.destroy();
		// TODO initialize nifty-gui main menu
	}
	
	// TODO create a method that saves all info and safely stop the program
	
	/**
	 * Starts the program
	 */
	public void start()
	{
		// TODO create run method stuff here
	}
	
	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		// Create instance
		TwoDotFiveD program = new TwoDotFiveD();
		// Run/start instance
		program.start();
	}
	
}
