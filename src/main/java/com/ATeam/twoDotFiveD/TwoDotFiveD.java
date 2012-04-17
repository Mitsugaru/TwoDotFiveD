package com.ATeam.twoDotFiveD;

import java.io.IOException;
import java.util.logging.Level;

import lib.Mitsugaru.SQLibrary.MySQL;

import com.ATeam.twoDotFiveD.debug.*;
import com.ATeam.twoDotFiveD.entity.Player;
import com.ATeam.twoDotFiveD.event.EventDispatcher;
import com.ATeam.twoDotFiveD.event.Event;
import com.ATeam.twoDotFiveD.event.player.PlayerMoveEvent;
import com.ATeam.twoDotFiveD.gui.DisplayStuff;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyMouse;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice;
import de.lessvoid.nifty.sound.openal.OpenALSoundDevice;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;

public class TwoDotFiveD {
	// Class variables
	public static final String homeDir = System.getProperty("user.home")
			+ System.getProperty("file.separator") + ".TwoDotFiveD";
	public static Logging logger = new Logging(homeDir);
	public static Config config = new Config(homeDir);
	private static EventDispatcher dispatcher = new EventDispatcher();
	private static final String SPLASH_XML = "com/ATeam/twoDotFiveD/layout/splash.xml";
	private static final String MOUSE_CURSOR = "nifty-cursor.png";

	/**
	 * Main constructor
	 */
	public TwoDotFiveD() {
		
		// Log info
		Logging.systemInfo();
		Logging.memoryInfo();
		// Event test
		// dispatcher.registerListener(Event.Type.PLAYER_MOVE, new
		// ListenerTest());
		final Player player = new Player();
		dispatcher.notify(new PlayerMoveEvent(player));
		// TODO initialize screen
		DisplayStuff.initSubSystems("2.5D");
		LwjglRenderDevice render = new LwjglRenderDevice();
		Nifty nifty = new Nifty(render, new OpenALSoundDevice(),
				DisplayStuff.getInputSystem(), new AccurateTimeProvider());
		nifty.fromXml(SPLASH_XML, "intro");
		
		// get the NiftyMouse interface that gives us access to all mouse
		// cursor related stuff
		NiftyMouse niftyMouse = nifty.getNiftyMouse();

		// register/load a mouse cursor (this would be done somewhere at the
		// beginning)
		try {
			niftyMouse.registerMouseCursor("mouseId", MOUSE_CURSOR, 0, 0);
		} catch (IOException e) {
			Logging.log.log(Level.SEVERE, "Failed to load mouse cursor!", e);
		}

		// change the cursor to the one we've loaded before
		niftyMouse.enableMouseCursor("mouseId");

		// we could set the position like so
		// niftyMouse.setMousePosition(20, 20);
		// Set logger of Nifty to only important stuff
		java.util.logging.Logger.getAnonymousLogger().getParent()
				.setLevel(java.util.logging.Level.SEVERE);
		java.util.logging.Logger.getLogger("de.lessvoid.nifty.*").setLevel(
				java.util.logging.Level.SEVERE);
		// Render loop
		DisplayStuff.renderLoop(nifty, null);
		DisplayStuff.destroy();
		// TODO initialize nifty-gui main menu
		/*
		 * MySQL database = new MySQL(Logging.log, "TEST", "dakani.no-ip.org",
		 * "3306", "minecraft", "minecraft", "mc");
		 * if(database.checkConnection()) { System.out.println("CONNECTED"); }
		 * else { System.out.println("NOPE"); }
		 */
	}

	// TODO create a method that saves all info and safely stop the program

	/**
	 * Starts the program
	 */
	public void start() {
		// TODO create run method stuff here
	}

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Create instance
		
		TwoDotFiveD program = new TwoDotFiveD();
		// Run/start instance
		program.start();
	    
			}



}
