package com.ATeam.twoDotFiveD.gui;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import demo.lwjgl.basic.GLApp;

public class LayerManager extends GLApp
{
	private static final List<Screener> layers = new ArrayList<Screener>();
	
	public synchronized void draw()
	{
		for(Screener layer : layers)
		{
			layer.draw();
		}
	}
	
	public void initGL()
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
	
	public void setup()
	{
		this.addLayer(new NiftyMainGUI());
		this.addLayer(new TriangleLayer());
	}
	
	public void addLayer(Screener layer)
	{
		layers.add(layer);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		LayerManager manager = new LayerManager();
		manager.window_title = "Hello World";
		manager.displayWidth = 800;
		manager.displayHeight = 600;
		manager.run();
	}
	
}
