package com.ATeam.twoDotFiveD.gui;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class TriangleLayer extends Screener
{
	public void draw()
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
}
