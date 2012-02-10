package com.ATeam.twoDotFiveD.gui;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.logging.Level;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.ATeam.twoDotFiveD.debug.Logging;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyMouse;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice;
import de.lessvoid.nifty.sound.openal.OpenALSoundDevice;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;

public class NiftyMainGUI extends Screener
{
	private Nifty nifty;
	private static final String	XML				= "src/main/resources/com/ATeam/twoDotFiveD/layout/main.xml";
	private static final String	MOUSE_CURSOR	= "src/main/resources/nifty-cursor.png";
	public NiftyMainGUI()
	{
		super();
		LwjglRenderDevice render = new LwjglRenderDevice();
		nifty = new Nifty(render, new OpenALSoundDevice(),
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
	}
	
	public void init()
	{
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
	}
	
	public void render()
	{
		if (!nifty.update())
		{
			nifty.render(true);
		}
	}
}
