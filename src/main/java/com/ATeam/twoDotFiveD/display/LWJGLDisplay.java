package com.ATeam.twoDotFiveD.display;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import com.ATeam.twoDotFiveD.Config;
import com.ATeam.twoDotFiveD.debug.Logging;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import demo.lwjgl.basic.GLApp;

public class LWJGLDisplay extends GLApp
{
	private boolean					renderNifty	= true;
	private Nifty					nifty;
	private static LwjglInputSystem	inputSystem;
	
	public static LwjglInputSystem getInputSystem()
	{
		if (inputSystem == null)
		{
			try
			{
				inputSystem = new LwjglInputSystem();
				inputSystem.startup();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Logging.log.warning("Unable to create keyboard!, exiting...");
			}
		}
		return inputSystem;
	}
	
	@Override
	public void run()
	{
		// hold onto application class in case we need to load images from jar
		// (see getInputStream())
		setRootClass();
		try
		{
			// Main loop
			while (!finished)
			{
				if (!Display.isVisible())
				{ // window is minimized
					Thread.sleep(200L);
				}
				else if (Display.isCloseRequested())
				{ // window X button clicked
					finished = true;
				}
				else
				{ // yield a little so other threads can work
					Thread.sleep(1);
				}
				updateTimer(); // track when frame was drawn (see
								// secondsSinceLastFrame)
				handleEvents(); // call key...() and mouse...() functions based
								// on input events
				update(); // do program logic here (subclass may override this)
				render(); // redraw the screen (subclass overrides this)
				Display.update();
				if (renderNifty)
				{
					nifty.render(true);
				}
				// check gl error at least ones per frame
				int error = GL11.glGetError();
				if (error != GL11.GL_NO_ERROR)
				{
					String glerrmsg = GLU.gluErrorString(error);
					Logging.log.warning("OpenGL Error: (" + error + ") "
							+ glerrmsg);
				}
			}
		}
		catch (Exception e)
		{
			err("GLApp.run(): " + e);
			e.printStackTrace(System.out);
		}
		// prepare to exit
		cleanup();
		System.exit(0);
	}
	
	public LWJGLDisplay()
	{
		// Empty constructor
	}
	
	public LWJGLDisplay(Config config)
	{
		fullScreen = config.getFullScreen();
		VSyncEnabled = config.getVSync();
	}
	
	public void setNifty(Nifty nifty)
	{
		this.nifty = nifty;
	}
	
	public void render()
	{
		if (renderNifty)
		{
			
		}
		else
		{
			// Clear screen and depth buffer
			// GL11.glClear(GL11.GL_COLOR_BUFFER_BIT |
			// GL11.GL_DEPTH_BUFFER_BIT);
			// Select The Modelview Matrix (controls model orientation)
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			// Reset the coordinate system to center of screen
			GL11.glLoadIdentity();
			// Place the viewpoint
			GLU.gluLookAt(0f, 0f, 10f, // eye position (10 units in
										// front of
										// the
										// origin)
					0f, 0f, 0f, // target to look at (the origin)
					0f, 1f, 0f); // which way is up (Y axis)
			// draw a triangle centered around 0,0,0
			GL11.glBegin(GL11.GL_TRIANGLES); // draw triangles
			GL11.glVertex3f(0.0f, 1.0f, 0.0f); // Top
			GL11.glVertex3f(-1.0f, -1.0f, 0.0f); // Bottom Left
			GL11.glVertex3f(1.0f, -1.0f, 0.0f); // Bottom Right
		}
	}
	
	public void destroy()
	{
		Display.destroy();
	}
	
	public void setRenderNifty(boolean renderNifty)
	{
		this.renderNifty = renderNifty;
	}
}
