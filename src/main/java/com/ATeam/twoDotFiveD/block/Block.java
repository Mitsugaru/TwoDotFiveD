package com.ATeam.twoDotFiveD.block;

import lib.lwjgl.glmodel.GL_Vector;

public class Block {
	public float ID = 000;
	public GL_Vector position = new GL_Vector (0f, -1f, 0f);
	public GL_Vector size = new GL_Vector (15f, .01f, 15f);
	public String image = "src/main/resources/com/lovetextures/ground.jpg";
	
	public Block() {
	}
	
	/**
	 * Create a static world block that is given an ID, world position, and image path
	 * @param ID		
	 * @param x			xyz world coordinates
	 * @param y
	 * @param z
	 * @param image		image path string
	 */
	public Block(float ID, float x, float y, float z, String image) {
		this.ID = ID;
		position.x = x;
		position.y = y;
		position.z = z;
		this.image = image;
	}
}
