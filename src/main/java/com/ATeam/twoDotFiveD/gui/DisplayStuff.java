package com.ATeam.twoDotFiveD.gui;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import com.ATeam.twoDotFiveD.debug.Logging;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyMouse;
import de.lessvoid.nifty.render.NiftyRenderEngine;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice;
import de.lessvoid.nifty.sound.openal.OpenALSoundDevice;
import de.lessvoid.nifty.spi.render.RenderDevice;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;
import de.lessvoid.nifty.tools.Color;
import de.niftygui.examples.LoggerShortFormat;
import de.niftygui.examples.LwjglInitHelper;
import de.niftygui.examples.LwjglInitHelper.RenderLoopCallback;
import demo.lwjgl.basic.GLApp;

public class DisplayStuff
{
	// private Nifty nifty;
	// private static final String XML =
	// "src/main/resources/com/ATeam/twoDotFiveD/layout/main.xml";
	// private static final String MOUSE_CURSOR =
	// "src/main/resources/nifty-cursor.png";
	private static boolean	renderNifty	= true;
	
	/*
	 * @Override public void run() { // hold onto application class in case we
	 * need to load images from jar (see getInputStream()) setRootClass(); try {
	 * // Init Display, Keyboard, Mouse, OpenGL, load config file init(); //
	 * Main loop while (!finished) { if (!Display.isVisible()) { // window is
	 * minimized Thread.sleep(200L); } else if (Display.isCloseRequested()) { //
	 * window X button clicked finished = true; } else { // yield a little so
	 * other threads can work Thread.sleep(1); } updateTimer(); // track when
	 * frame was drawn (see secondsSinceLastFrame) handleEvents(); // call
	 * key...() and mouse...() functions based on input events update(); // do
	 * program logic here (subclass may override this) draw(); // redraw the
	 * screen (subclass overrides this) } } catch (Exception e) {
	 * err("GLApp.run(): " + e); e.printStackTrace(System.out); } // prepare to
	 * exit cleanup(); System.exit(0); }
	 * 
	 * @Override public void init() { // load settings from config file (display
	 * size, resolution, etc.) loadSettings(configFilename); initDisplay();
	 * initInput(); initGL(); setup(); // subclass usually overrides this
	 * updateTimer(); LwjglRenderDevice render = new LwjglRenderDevice(); nifty
	 * = new Nifty(render, new OpenALSoundDevice(), new LwjglInputSystem(), new
	 * AccurateTimeProvider()); nifty.fromXml(XML, "intro"); // get the
	 * NiftyMouse interface that gives us access to all mouse // cursor related
	 * stuff NiftyMouse niftyMouse = nifty.getNiftyMouse();
	 * 
	 * // register/load a mouse cursor (this would be done somewhere at the //
	 * beginning) try { niftyMouse.registerMouseCursor("mouseId", MOUSE_CURSOR,
	 * 0, 0); } catch (IOException e) { Logging.log.log(Level.SEVERE,
	 * "Failed to load mouse cursor!", e); }
	 * 
	 * // change the cursor to the one we've loaded before
	 * niftyMouse.enableMouseCursor("mouseId");
	 * 
	 * // we could set the position like so niftyMouse.setMousePosition(20, 20);
	 * }
	 * 
	 * @Override public void draw() { if (renderNifty) { //nifty.update();
	 * Display.update(); nifty.render(true); } else { // Clear screen and depth
	 * buffer GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	 * // Select The Modelview Matrix (controls model orientation)
	 * GL11.glMatrixMode(GL11.GL_MODELVIEW); // Reset the coordinate system to
	 * center of screen GL11.glLoadIdentity(); // Place the viewpoint
	 * GLU.gluLookAt(0f, 0f, 10f, // eye position (10 units in front of the //
	 * origin) 0f, 0f, 0f, // target to look at (the origin) 0f, 1f, 0f); //
	 * which way is up (Y axis) // draw a triangle centered around 0,0,0
	 * GL11.glBegin(GL11.GL_TRIANGLES); // draw triangles GL11.glVertex3f(0.0f,
	 * 1.0f, 0.0f); // Top GL11.glVertex3f(-1.0f, -1.0f, 0.0f); // Bottom Left
	 * GL11.glVertex3f(1.0f, -1.0f, 0.0f); // Bottom Right GL11.glEnd();
	 * Display.update(); } }
	 */
	
	private static Logger	log			= Logger.getLogger(LwjglInitHelper.class
												.getName());
	private static int		WIDTH		= 1024;
	private static int		HEIGHT		= 768;
	
	public static void setRenderNifty(boolean i)
	{
		renderNifty = i;
	}
	
	public static void renderLoop(final Nifty nifty,
			final RenderLoopCallback callback)
	{
		boolean done = false;
		while (!Display.isCloseRequested() && !done)
		{
			if (renderNifty)
			{
				if (callback != null)
				{
					callback.process();
				}
				if (nifty.update())
				{
					done = true;
				}
				nifty.render(true);
			}
			else
			{
				// Select The Modelview Matrix (controls model orientation)
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				// Reset the coordinate system to center of screen
				GL11.glLoadIdentity();
				// draw a triangle centered around 0,0,0
				GL11.glBegin(GL11.GL_TRIANGLES); // draw triangles
				GL11.glVertex3f(0.0f, 1.0f, 0.0f); // Top
				GL11.glVertex3f(-1.0f, -1.0f, 0.0f); // Bottom Left
				GL11.glVertex3f(1.0f, -1.0f, 0.0f); // Bottom Right
				GL11.glEnd();
			}
			Display.update();
			// check gl error at least ones per frame
			int error = GL11.glGetError();
			if (error != GL11.GL_NO_ERROR)
			{
				String glerrmsg = GLU.gluErrorString(error);
				Logging.log
						.warning("OpenGL Error: (" + error + ") " + glerrmsg);
			}
		}
	}
	
	private static LwjglInputSystem	inputSystem;
	
	public static LwjglInputSystem getInputSystem()
	{
		return inputSystem;
	}
	
	/**
	 * RenderLoopCallback.
	 * 
	 * @author void
	 */
	public interface RenderLoopCallback
	{
		/**
		 * process.
		 */
		void process();
	}
	
	/**
	 * Init SubSystems.
	 * 
	 * @param title
	 *            title pf window
	 * @return true on success and false otherwise
	 */
	public static boolean initSubSystems(final String title)
	{
		LoggerShortFormat.intialize();
		if (!DisplayStuff.initGraphics(title))
		{
			return false;
		}
		
		// init input system
		if (!DisplayStuff.initInput())
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Init lwjgl graphics.
	 * 
	 * @param title
	 *            title of window
	 * @return true on success and false otherwise
	 */
	@SuppressWarnings("unused")
	private static boolean initGraphics(final String title)
	{
		try
		{
			DisplayMode currentMode = Display.getDisplayMode();
			log.fine("currentmode: " + currentMode.getWidth() + ", "
					+ currentMode.getHeight() + ", "
					+ currentMode.getBitsPerPixel() + ", "
					+ currentMode.getFrequency());
			
			// WIDTH = currentMode.getWidth();
			// HEIGHT = currentMode.getHeight();
			
			// get available modes, and print out
			DisplayMode[] modes = Display.getAvailableDisplayModes();
			log.fine("Found " + modes.length + " display modes");
			
			List<DisplayMode> matching = new ArrayList<DisplayMode>();
			for (int i = 0; i < modes.length; i++)
			{
				DisplayMode mode = modes[i];
				if (mode.getWidth() == WIDTH && mode.getHeight() == HEIGHT
						&& mode.getBitsPerPixel() == 32)
				{
					log.fine(mode.getWidth() + ", " + mode.getHeight() + ", "
							+ mode.getBitsPerPixel() + ", "
							+ mode.getFrequency());
					matching.add(mode);
				}
			}
			
			DisplayMode[] matchingModes = matching.toArray(new DisplayMode[0]);
			
			// find mode with matching freq
			boolean found = false;
			for (int i = 0; i < matchingModes.length; i++)
			{
				if (matchingModes[i].getFrequency() == currentMode
						.getFrequency())
				{
					log.fine("using mode: " + matchingModes[i].getWidth()
							+ ", " + matchingModes[i].getHeight() + ", "
							+ matchingModes[i].getBitsPerPixel() + ", "
							+ matchingModes[i].getFrequency());
					Display.setDisplayMode(matchingModes[i]);
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				Arrays.sort(matchingModes, new Comparator<DisplayMode>() {
					public int compare(final DisplayMode o1,
							final DisplayMode o2)
					{
						if (o1.getFrequency() > o2.getFrequency())
						{
							return 1;
						}
						else if (o1.getFrequency() < o2.getFrequency())
						{
							return -1;
						}
						else
						{
							return 0;
						}
					}
				});
				
				for (int i = 0; i < matchingModes.length; i++)
				{
					log.fine("using fallback mode: "
							+ matchingModes[i].getWidth() + ", "
							+ matchingModes[i].getHeight() + ", "
							+ matchingModes[i].getBitsPerPixel() + ", "
							+ matchingModes[i].getFrequency());
					Display.setDisplayMode(matchingModes[i]);
					break;
				}
			}
			
			int x = (WIDTH - Display.getDisplayMode().getWidth()) / 2;
			int y = (HEIGHT - Display.getDisplayMode().getHeight()) / 2;
			Display.setLocation(x, y);
			
			// Create the actual window
			try
			{
				Display.setFullscreen(false);
				Display.create();
				Display.setVSyncEnabled(false);
				Display.setTitle(title);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				log.warning("Unable to create window!, exiting...");
				System.exit(-1);
			}
			
			log.info("Width: " + Display.getDisplayMode().getWidth()
					+ ", Height: " + Display.getDisplayMode().getHeight()
					+ ", Bits per pixel: "
					+ Display.getDisplayMode().getBitsPerPixel()
					+ ", Frequency: " + Display.getDisplayMode().getFrequency()
					+ ", Title: " + Display.getTitle());
			
			// just output some infos about the system we're on
			log.info("plattform: " + LWJGLUtil.getPlatformName());
			log.info("opengl version: " + GL11.glGetString(GL11.GL_VERSION));
			log.info("opengl vendor: " + GL11.glGetString(GL11.GL_VENDOR));
			log.info("opengl renderer: " + GL11.glGetString(GL11.GL_RENDERER));
			String extensions = GL11.glGetString(GL11.GL_EXTENSIONS);
			if (extensions != null)
			{
				String[] ext = extensions.split(" ");
				for (int i = 0; i < ext.length; i++)
				{
					log.fine("opengl extensions: " + ext[i]);
				}
			}
			
			IntBuffer viewportBuffer = BufferUtils.createIntBuffer(4 * 4);
			GL11.glGetInteger(GL11.GL_VIEWPORT, viewportBuffer);
			int viewportWidth = viewportBuffer.get(2);
			int viewportHeight = viewportBuffer.get(3);
			
			// GL11.glViewport(0, 0, Display.getDisplayMode().getWidth(),
			// Display.getDisplayMode().getHeight());
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(0, viewportWidth, viewportHeight, 0, -9999, 9999);
			
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			
			// Prepare Rendermode
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_CULL_FACE);
			
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glAlphaFunc(GL11.GL_NOTEQUAL, 0);
			
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			
			GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			
			return true;
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Init input system.
	 * 
	 * @return true on success and false otherwise
	 */
	private static boolean initInput()
	{
		try
		{
			inputSystem = new LwjglInputSystem();
			inputSystem.startup();
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.warning("Unable to create keyboard!, exiting...");
			return false;
		}
	}
	
	/**
	 * destroy all and quit.
	 */
	public static void destroy()
	{
		inputSystem.shutdown();
		Display.destroy();
		System.exit(0);
	}
}
