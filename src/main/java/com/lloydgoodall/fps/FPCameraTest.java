package com.lloydgoodall.fps;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import demo.lwjgl.basic.GLApp;

public class FPCameraTest extends GLApp
{
	FPCameraController	camera;
	float				dx					= 0.0f;
	float				dy					= 0.0f;
	float				dt					= 0.0f;							// length
																				// of
																				// frame
	float				lastTime			= 0.0f;							// when
																				// the
																				// last
																				// frame
																				// was
	float				time				= 0.0f;
	
	float				mouseSensitivity	= 0.05f;
	float				movementSpeed		= 10.0f;							// move
																				// 10
																				// units
																				// per
																				// second
	// Handles for textures
	int					marbleTextureHandle	= 0;
	int					groundTextureHandle	= 0;
	// Light position: if last value is 0, then this describes light direction.
	// If 1, then light position.
	float				lightPosition1[]	= { 0f, 0f, 0f, 1f };
	float				lightPosition2[]	= { 0f, -10f, 0f, 0f };
	float				lightPosition3[]	= { 0f, 0f, 0f, 1f };
	// Rotation of sphere
	float				rotation			= 0f;
	// display lists
	int					sphereDL			= 0, cubeDL = 0;
	
	public void setup()
	{
		// hide the mouse
		Mouse.setGrabbed(true);
		camera = new FPCameraController(0, 0, 0);
		
		// Create sphere texture
		marbleTextureHandle = makeTexture("src/main/resources/com/lovetextures/moon.jpg");
		
		// Create texture for ground plane
		groundTextureHandle = makeTexture("src/main/resources/com/lovetextures/yellowflowers.jpg");
		
		// setup perspective
		//setPerspective();
		
		// Create a point light (white)
		setLight(GL11.GL_LIGHT1, new float[] { 1.0f, 1.0f, 1.0f, 1.0f }, // diffuse
																			// color
				new float[] { 0.2f, 0.2f, 0.2f, 1.0f }, // ambient
				new float[] { 1.0f, 1.0f, 1.0f, 1.0f }, // specular
				lightPosition1); // position
		
		// Create a directional light (dark red, to simulate reflection off wood
		// surface)
		setLight(GL11.GL_LIGHT2, new float[] { 0.95f, 0.35f, 0.15f, 1.0f }, // diffuse
																			// color
				new float[] { 0.0f, 0.0f, 0.0f, 1.0f }, // ambient
				new float[] { 0.03f, 0.0f, 0.0f, 1.0f }, // specular
				lightPosition2); // position (pointing up)
		
		// Create a point light (dark blue, to simulate reflection off wood
		// surface)
		setLight(GL11.GL_LIGHT3, new float[] { 0.35f, 0.45f, 0.95f, 1.0f }, // diffuse
																			// color
				new float[] { 0.0f, 0.0f, 0.0f, 1.0f }, // ambient
				new float[] { 0.3f, 0.4f, 0.7f, 1.0f }, // specular
				lightPosition3); // position (pointing up)
		
		// no overall scene lighting
		setAmbientLight(new float[] { 0.0f, 0.0f, 0.0f, 0.0f });
		
		sphereDL = beginDisplayList();
		renderSphere();
		endDisplayList();
		
		cubeDL = beginDisplayList();
		renderCube(10, 40);
		endDisplayList();
		
		// enable lighting and texture rendering
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		// select model view for subsequent transforms
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}
	
	/**
	 * Set the camera position, field of view, depth.
	 */
	public static void setPerspective()
	{
		// select projection matrix (controls perspective)
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		// fovy, aspect ratio, zNear, zFar
		GLU.gluPerspective(30f, aspectRatio, 1f, 100f);
		// return to modelview matrix
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
	
	public void draw()
	{
		time = Sys.getTime();
		dt = (time - lastTime) / 1000.0f;
		lastTime = time;
		
		// distance in mouse movement from the last getDX() call.
		dx = Mouse.getDX();
		// distance in mouse movement from the last getDY() call.
		dy = Mouse.getDY();
		
		// controll camera yaw from x movement fromt the mouse
		camera.yaw(dx * mouseSensitivity);
		// controll camera pitch from y movement fromt the mouse
		camera.pitch(dy * mouseSensitivity);
		
		// when passing in the distance to move
		// we times the movementSpeed with dt this is a time scale
		// so if its a slow frame u move more then a fast frame
		// so on a slow computer you move just as fast as on a fast computer
		if (Keyboard.isKeyDown(Keyboard.KEY_W))// move forward
		{
			camera.walkForward(movementSpeed * dt);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S))// move backwards
		{
			camera.walkBackwards(movementSpeed * dt);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A))// strafe left
		{
			camera.strafeLeft(movementSpeed * dt);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D))// strafe right
		{
			camera.strafeRight(movementSpeed * dt);
		}
		
		// set the modelview matrix back to the identity
		GL11.glLoadIdentity();
		// look through the camera before you draw anything
		camera.lookThrough();
		// you would draw your scene here.
		renderLights();
	}
	
	public void renderLights()
	{
		// rotate 25 degrees per second
		rotation += 25f * getSecondsPerFrame();
		
		// clear depth buffer and color buffers
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		// select model view for subsequent transforms
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		
		// ---------------------------------------------------------------
		// desktop
		// ---------------------------------------------------------------
		
		// dark reddish material
		setMaterial(new float[] { 0.8f, 0.3f, 0.2f, 1.0f }, .2f);
		
		// enable texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, groundTextureHandle);
		
		// draw the ground plane
		GL11.glPushMatrix();
		{
			GL11.glTranslatef(0f, -7f, 0f); // down a bit
			callDisplayList(cubeDL);
		}
		GL11.glPopMatrix();
		
		// orbit
		GL11.glRotatef(rotation, 0, 1, 0);
		GL11.glTranslatef(3, 0, 0);
		
		GL11.glRotatef(rotation / 2, 0, 1, 0);
		GL11.glTranslatef(-2, 0, 0);
		
		// ---------------------------------------------------------------
		// glowing white ball
		// ---------------------------------------------------------------
		
		// for point lights set position each frame so light moves with scene
		// white light at same position as marble ball
		setLightPosition(GL11.GL_LIGHT1, lightPosition1);
		
		// glowing white material
		setMaterial(new float[] { 1.0f, 1.0f, 1.0f, 1.0f }, // diffuse color
				new float[] { 1.0f, 1.0f, 1.0f, 1.0f }, // ambient
				new float[] { 0.0f, 0.0f, 0.0f, 1.0f }, // specular
				new float[] { 1.0f, 1.0f, 1.0f, 1.0f }, // emissive
				0f); // not shiny
		
		// enable texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, marbleTextureHandle);
		
		// draw marble sphere
		GL11.glPushMatrix();
		{
			GL11.glScalef(.5f, .5f, .5f); // make it smaller
			callDisplayList(sphereDL); // draw the sphere display list
		}
		GL11.glPopMatrix();
		
		// orbit
		GL11.glRotatef(rotation * 1.4f, 0, 1, 0);
		GL11.glTranslatef(4, 0, 0);
		
		// ---------------------------------------------------------------
		// shiny blue ball
		// ---------------------------------------------------------------
		
		// shiny dark blue material
		setMaterial(new float[] { 0.3f, 0.3f, 0.6f, 1.0f }, .9f);
		
		// set blue light at same spot as blue sphere
		setLightPosition(GL11.GL_LIGHT3, lightPosition3);
		
		// no texture (texture handle 0)
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		// Draw sphere
		GL11.glPushMatrix();
		{
			GL11.glScalef(1.6f, 1.6f, 1.6f); // make it smaller
			callDisplayList(sphereDL);
		}
		GL11.glPopMatrix();
	}
	
	public static void main(String[] args)
	{
		FPCameraTest test = new FPCameraTest();
		// set title, window size
		test.window_title = "FPCamera";
		test.displayWidth = 800;
		test.displayHeight = 600;
		test.run();
	}
}
