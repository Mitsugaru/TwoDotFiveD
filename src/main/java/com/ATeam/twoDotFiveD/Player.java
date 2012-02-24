package com.ATeam.twoDotFiveD;

import org.lwjgl.input.Keyboard;

import demo.lwjgl.basic.GLApp;

import lib.lwjgl.glmodel.GL_Vector;

public class Player {
	public Player player;
	private float cameraBoundary = 2.5f;
	private float speed = 20f;
	private float size = 2f;
	private float jumpHeight = size*1.8f;
	private float runningJumpHeight;
	private float remainingJumpHeight = jumpHeight;
	private GL_Vector worldChange;
	private GL_Vector position = new GL_Vector( 0f, 0f, 0f);
	private boolean isSpectator = false;
	private boolean jumping = false;
	private boolean jumpingUp = true;
	
	
	/**
	 * Default Player constructor - default position is origin, is not a spectator
	 */
	public Player() {
		
	}
	
	/**
	 * Constructor for Player - position is x y z, is not a spectator
	 * @param x  positions for rendering
	 * @param y
	 * @param z
	 */
	public Player(float x, float y, float z) {
		position = new GL_Vector( x, y, z );
	}
	
	/**
	 * Constructs a new Player at given postition, can also give spectator value
	 * @param x          postions for rendering
	 * @param y
	 * @param z
	 * @param spectator  is this player a spectator
	 */
	public Player(float x, float y, float z, boolean spectator) {
		position = new GL_Vector(x, y, z);
		isSpectator = spectator;
	}
	
	
	/**
	 * Constructs a new Player at given position, can also give spectator value and speed
	 * @param x			xyz world position
	 * @param y
	 * @param z
	 * @param spectator
	 * @param speed
	 */
	public Player(float x, float y, float z, boolean spectator, float speed) {
		position = new GL_Vector(x, y, z);
		isSpectator = spectator;
		this.speed = speed;
	}
	
	/**
	 * Constructs a new Player at given position, can also give spectator value, speed, and size
	 * Size is the GL11.glTranslatef() value for the x,y,z
	 * @param x			xyz world position
	 * @param y
	 * @param z
	 * @param spectator
	 * @param speed
	 * @param size
	 */
	public Player(float x, float y, float z, boolean spectator, float speed, float size) {
		position = new GL_Vector(x,y,z);
		isSpectator = spectator;
		this.speed = speed;
		this.size = size;
	}
	
	/**
	 * Get the player's positon
	 * @return GL_Vector containing player position
	 */
	public GL_Vector getPosition() {
		return position;
	}
	
	/**
	 * Set the player's position
	 * @param position - GL_Vector the x y z of the player position
	 */
	public void setPosition(GL_Vector position) {
		this.position = position;
	}
	
	/**
	 * Calculates how by how much to translate the x, y, and z positions for static world objects
	 */
	public void setWorldChangeVector() {
		float x =0 , y =0 ,z = 0;
		if(position.x-cameraBoundary > 0.0f) {
			x = position.x-cameraBoundary;
			position.x = cameraBoundary;
		}
		if(position.x+cameraBoundary < 0.0f) {
			x = position.x+cameraBoundary;
			position.x = -cameraBoundary;
		}
		
		//NOTE------------------------
		
		//need to fix
//		if(position.y > 0.0f) {
//			y = position.y;
//			position.y = 0f;
//		}
//		if(position.y< 0.0f) {
//			y -= position.y;
//			position.y = 0f;
//		}
		
		if(position.z-cameraBoundary > 0.0f) {
			z = position.z-cameraBoundary;
			position.z = cameraBoundary;
		}
		if(position.z+cameraBoundary < 0.0f) {
			z = position.z+cameraBoundary;
			position.z = -cameraBoundary;
		}
		worldChange = new GL_Vector(-x,-y,-z);
	}
	
	/**
	 * get the world change vector based on this player
	 * @return
	 */
	public GL_Vector getWorldChangeVector() {
		return worldChange;
	}
	
	/**
	 * Handle 'WASD' and SPACE keys for movement
	 * Movement direction is altered for each key depending on the current
	 * camera quadrant and direction
	 * 
	 * @param direction - String the current direction of the camera
	 * @param quadrant  - int the current quadrant of the camera
	 * 
	 * @see GLCam.java
	 */
	
	//NOTE--------------------------------
	//movement is currently rigid -- there is no inertia to movement after releasing key
	public void handleMovementKeys(String direction, int quadrant) {
		if(jumping) {
			if(jumpingUp) {
				if (remainingJumpHeight > jumpHeight*0.3f) {
					remainingJumpHeight = jumpHeight - runningJumpHeight;
					if (remainingJumpHeight < jumpHeight*0.3f) {
						remainingJumpHeight = jumpHeight*0.3f;
					}
					
				}
				runningJumpHeight += (remainingJumpHeight*(speed*0.5f))*GLApp.getSecondsSinceLastFrame();
				if (runningJumpHeight >=  jumpHeight) {
					runningJumpHeight = jumpHeight;
					jumpingUp = false;
				}
				position.y = runningJumpHeight;
			}
			
			if(!jumpingUp) {
				if (remainingJumpHeight <= jumpHeight*0.9f) {
					remainingJumpHeight = jumpHeight - (runningJumpHeight*0.9f);
				}
				runningJumpHeight -= (remainingJumpHeight*(speed*0.5f))*GLApp.getSecondsSinceLastFrame();
				if (runningJumpHeight <=  0f) {
					runningJumpHeight = 0f;
					remainingJumpHeight = jumpHeight;
					jumping = false;
				}
				position.y = runningJumpHeight;
			}
			
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_D)){
			if (quadrant == 1 || direction.equals("UP")) {
				position.x += speed*GLApp.getSecondsSinceLastFrame();
			}
			
			if (quadrant == 2 && !direction.equals("UP")) {
				position.z -= speed*GLApp.getSecondsSinceLastFrame();

			}
			
			if (quadrant == 3 && !direction.equals("UP")) {
				position.x -= speed*GLApp.getSecondsSinceLastFrame();

			}
			
			if (quadrant == 4 && !direction.equals("UP")) {
				position.z += speed*GLApp.getSecondsSinceLastFrame();

			}
			
			
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
			if (quadrant == 1 || direction.equals("UP")) {
				position.x -= speed*GLApp.getSecondsSinceLastFrame();
			}
			
			if (quadrant == 2 && !direction.equals("UP")) {
				position.z += speed*GLApp.getSecondsSinceLastFrame();

			}
			
			if (quadrant == 3 && !direction.equals("UP")) {
				position.x += speed*GLApp.getSecondsSinceLastFrame();

			}
			
			if (quadrant == 4 && !direction.equals("UP")) {
				position.z -= speed*GLApp.getSecondsSinceLastFrame();

			}

		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_W)){
			if (quadrant == 1 || direction.equals("UP")) {
				position.z -= speed*GLApp.getSecondsSinceLastFrame();
			}
			
			if (quadrant == 2 && !direction.equals("UP")) {
				position.x -= speed*GLApp.getSecondsSinceLastFrame();

			}
			
			if (quadrant == 3 && !direction.equals("UP")) {
				position.z += speed*GLApp.getSecondsSinceLastFrame();

			}
			
			if (quadrant == 4 && !direction.equals("UP")) {
				position.x += speed*GLApp.getSecondsSinceLastFrame();

			}

		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
			if (quadrant == 1 || direction.equals("UP")) {
				position.z += speed*GLApp.getSecondsSinceLastFrame();
			}
			
			if (quadrant == 2 && !direction.equals("UP")) {
				position.x += speed*GLApp.getSecondsSinceLastFrame();

			}
			
			if (quadrant == 3 && !direction.equals("UP")) {
				position.z -= speed*GLApp.getSecondsSinceLastFrame();

			}
			
			if (quadrant == 4 && !direction.equals("UP")) {
				position.x -= speed*GLApp.getSecondsSinceLastFrame();

			}

		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			if (!jumping) {
				jumpingUp = true;
				jumping = true;
				runningJumpHeight += jumpHeight*GLApp.getSecondsSinceLastFrame();
				if (runningJumpHeight >=  jumpHeight) {
					runningJumpHeight = jumpHeight;
					jumpingUp = false;
				}
				position.y = runningJumpHeight;
				
			}
		}
	}
}
