package com.ATeam.twoDotFiveD.gui;

public abstract class Screener
{
	protected boolean initialize = true;
	
	/**
	 * To be overriden by subclass.
	 * Has all the initial OpenGL stuff that is necessary to render correctly.
	 */
	public void init()
	{
		
	}
	
	/**
	 * Render stuff, calls render()
	 */
	public void draw()
	{
		render();
	}
	
	/**
	 * Render stuff
	 */
	public void render()
	{
		
	}
}
