package com.ATeam.twoDotFiveD;



//import java.awt.Color;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.*;
//import org.lwjgl.input.*;
import org.lwjgl.util.glu.*;

import com.ATeam.twoDotFiveD.block.Block;

import demo.lwjgl.basic.GLApp;
import demo.lwjgl.basic.GLCam;
import demo.lwjgl.basic.GLCamera;
import demo.lwjgl.basic.GLShadowOnPlane;
import lib.lwjgl.glmodel.*;

/**
 * Demo a scene with one camera.  The camera orients around a fixed point.
 * Demos how the games ultimate camera will function -- currently adapted to move world around a fixed camera location
 * instead of moving camera around the world.  Uses the GLCamera class to
 * hold camera position and orientation, and the GLCam class to move the
 * current camera in response to arrow key events.
 * <P>
 * @see GLCamera.java
 * @see GLCam.java
 * <P>
 * @author - Andrew Tucker
 */
public class TwoDotFiveDCameraDemo extends GLApp {
	//added since demo to Dr. Game
	GL_Vector worldChangeVector;
	GL_Vector playerPosition;
	//Player player = new Player();


	//hardcoded blocks
	//need a method to do this automatically

	Block ground = new Block();
	Block groundLeft = new Block(0f, -20f, -1f, 0f, null);
	Block groundRight = new Block(0f, 20f, -1f, 0f, null);
	Block groundTop = new Block(0f, 0f,-1f, -20f, null);
	Block groundTopLeft = new Block(0f, -20f, -1f, -20f, null);
	Block groundTopRight = new Block(0f, 20f, -1f, -20f, null);
	Block groundBottom = new Block(0f, 0f, -1f, 20f, null);
	Block groundBottomLeft = new Block(0f, -20f, -1f, 20f, null);
	Block groundBottomRight = new Block(0f, 20f, -1f, 20f, null);
	Block cube1 = new Block(0f, -11f, 0f, -7f, null);
	Block cube2 = new Block(0f, -9f, 0f, -5f, null);
	Block cube3 = new Block(0f, -10f, 2f, -6f, null);
	Block sphere1 = new Block(0f, 15f, 4f, -10f, null);
	Block sphere2 = new Block(0f, 25f, 7f, -20f, null);
	Block sphere3 = new Block(0f, -20f, 4f, 10f, null);

    // Handle for texture
    int cubeTextureHandle = 0;
    int sphereTextureHandle = 0;
    int groundTextureHandle = 0;
    int cubeOtherTextureHandle = 0;
    int skyTextureHandle = 0;
    // Light position: if last value is 0, then this describes light direction.  If 1, then light position.
    float lightPosition[]= { -2f, 2f, 2f, 0f };
    // Camera position
    float[] cameraPos = {0f,3f,20f};

    GLCamera camera1 = new GLCamera();
    GLCam cam = new GLCam(camera1);

    // vectors used to orient sphere
    GL_Vector UP = new GL_Vector(0,1,0);
    GL_Vector ORIGIN = new GL_Vector(0,0,0);

    // for sphere rotation
    float degrees = 0;
    int cube;
    int sphere;
    int cubeTestBottom;
    int cubeTestTop;
    int cubeTestRot;
    GLShadowOnPlane objectsShadow;

    public GL_Vector spherePos;

	FloatBuffer bbmatrix = GLApp.allocFloats(16);

    /**
     * Initialize application and run main loop.
     */
    public static void main(String args[]) {
    	TwoDotFiveDCameraDemo demo = new TwoDotFiveDCameraDemo();
        demo.VSyncEnabled = true;
        demo.fullScreen = false;
        demo.displayWidth = 800;
        demo.displayHeight = 600;
        demo.run();  // will call init(), render(), mouse functions
    }

    /**
     * Initialize the scene.  Called by GLApp.run()
     */
    public void setup()
    {
        setPerspective();

        // Create a light (diffuse light, ambient light, position)
        setLight( GL11.GL_LIGHT1,
        		new float[] { 1f, 1f, 1f, 1f },
        		new float[] { 0.5f, 0.5f, .53f, 1f },
        		new float[] { 1f, 1f, 1f, 1f },
        		lightPosition );

        // Create a directional light (light green, to simulate reflection off grass)
        /*setLight( GL11.GL_LIGHT2,
        		new float[] { 0.0f, 0f, 0f, 1.0f },  // diffuse color
        		new float[] { 0.7f, 0.0f, 0.0f, 1.0f },   // ambient
        		new float[] { 0.0f, 0.0f, 0.0f, 1.0f },   // specular
        		new float[] { 0f, -1f, 0.0f, 0f } );   // direction (pointing up)
        		*/

        
        
 
	    camera1.setCamera(0,0,20, 0,0,0, 0,1,0);

//	    cubeTestBottom = beginDisplayList(); {
//       	renderCubeCoord(-11f, -1.0f, -7f, 2.0f);
//	    //	drawCube(10f);
//        }
//        endDisplayList();
//        
//        cubeTestTop = beginDisplayList(); {
//        	renderCubeCoord(-10f, 1.0f, -6f, 2.0f);
//        }
//        endDisplayList();
//        
//        cubeTestRot = beginDisplayList(); {
//        	renderCubeCoord(-9f, -1.0f, -5f, 2.0f);
//        }
//        endDisplayList();

//        // make a cube display list
//        cube = beginDisplayList(); {
//        	renderCube();
//        }
//        endDisplayList();
        
        // make a sphere display list
//        sphere = beginDisplayList(); {
//        	renderSphere();
//        }
//        endDisplayList();
        
        

        // make a shadow handler
        // params:
        //		the light position,
        //		the plane the shadow will fall on,
        //		the color of the shadow,
        // 		this application,
        // 		the function that draws all objects that cast shadows
        objectsShadow = new GLShadowOnPlane(lightPosition, new float[] {0f,2f,0f,2f}, null, this, method(this,"drawObjects(worldChangeVector)"));
    }

    /**
     * set the field of view and view depth.
     */
    public static void setPerspective()
    {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(50f,         // zoom in or out of view
                           aspectRatio, // shape of viewport rectangle
                           .1f,         // Min Z: how far from eye position does view start
                           500f);       // max Z: how far from eye position does view extend
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    /**
     * Render one frame.  Called by GLApp.run().
     */
    public void draw() {
    	//a bit wonky and obtuse -- may want to find a better way to do this
    	//ORDER IS IMPORTANT
    	//player.handleMovementKeys(cam.getDirection(), cam.getQuadrant());
    	//player.setWorldChangeVector();
    	//worldChangeVector = player.getWorldChangeVector();
    	//System.out.println("world change vector " + worldChangeVector);
    	//playerPosition = player.getPosition();

    	
    	degrees += 90f * GLApp.getSecondsPerFrame();
    	spherePos = GL_Vector.rotationVector(degrees).mult(8);
    	
    	//update the camera pan based on direction
    	cam.updatePanOld(cam.getDirection());
    	//handle camera input keys
    	cam.handleRotKeysPanOld();
       
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        cam.render();
        
    	drawWorldPlanes(worldChangeVector);
    	objectsShadow.drawShadow();

        drawObjects(worldChangeVector);

        setLightPosition(GL11.GL_LIGHT1, lightPosition);

        //display user directions
        print( 30, viewportH- 40, "Left-Right arrows rotate camera during sidescrolling view", 1);
        print( 30, viewportH- 60, "Up arrow to engage top-down view", 1);
        print( 30, viewportH- 80, "Down to return to sidescrolling view", 1);
        print( 30, viewportH- 120, "W,A,S,D keys to move cube", 1);
        print( 30, viewportH- 140, "Space to jump", 1);
        print( 30, viewportH-180, "Running AVG FPS: " + Double.toString(GLApp.getFramesPerSecond()), 1);
    }
    
    public void drawWorldPlanes(GL_Vector change) {
    	System.out.println("ground position vector " + ground.position);
  
        GL11.glPushMatrix();
        {
            GL11.glTranslatef(ground.position.x+= change.x, ground.position.y+=change.y, ground.position.z+=change.z); 
        	GL11.glScalef(10f, .01f, 10f);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, groundTextureHandle);
            renderCube();
        }
        GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        {
            GL11.glTranslatef(groundLeft.position.x+= change.x, groundLeft.position.y+=change.y, groundLeft.position.z+=change.z); 
        	GL11.glScalef(10f, .01f, 10f);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, groundTextureHandle);
            renderCube();
        }
        GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        {
            GL11.glTranslatef(groundRight.position.x+= change.x, groundRight.position.y+=change.y, groundRight.position.z+=change.z); 
        	GL11.glScalef(10f, .01f, 10f);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, groundTextureHandle);
            renderCube();
        }
        GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        {
            GL11.glTranslatef(groundTopLeft.position.x+= change.x, groundTopLeft.position.y+=change.y, groundTopLeft.position.z+=change.z); 
        	GL11.glScalef(10f, .01f, 10f);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, groundTextureHandle);
            renderCube();
        }
        GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        {
            GL11.glTranslatef(groundTop.position.x+= change.x, groundTop.position.y+=change.y, groundTop.position.z+=change.z); 
        	GL11.glScalef(10f, .01f, 10f);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, groundTextureHandle);
            renderCube();
        }
        GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        {
            GL11.glTranslatef(groundTopRight.position.x+= change.x, groundTopRight.position.y+=change.y, groundTopRight.position.z+=change.z); 
        	GL11.glScalef(10f, .01f, 10f);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, groundTextureHandle);
            renderCube();
        }
        GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        {
            GL11.glTranslatef(groundBottomLeft.position.x+= change.x, groundBottomLeft.position.y+=change.y, groundBottomLeft.position.z+=change.z); 
        	GL11.glScalef(10f, .01f, 10f);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, groundTextureHandle);
            renderCube();
        }
        GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        {
            GL11.glTranslatef(groundBottom.position.x+= change.x, groundBottom.position.y+=change.y, groundBottom.position.z+=change.z); 
        	GL11.glScalef(10f, .01f, 10f);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, groundTextureHandle);
            renderCube();
        }
        GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        {
            GL11.glTranslatef(groundBottomRight.position.x+= change.x, groundBottomRight.position.y+=change.y, groundBottomRight.position.z+=change.z); 
        	GL11.glScalef(10f, .01f, 10f);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, groundTextureHandle);
            renderCube();
        }
        GL11.glPopMatrix();
        
       //
    }

    public void drawObjects(GL_Vector change) {
        //sphere
//        GL11.glPushMatrix();
//        {
// 
//        	System.out.println("Sphere position " + spherePos);
//        	//billboardPoint(spherePos, ORIGIN, UP);
//            GL11.glBindTexture(GL11.GL_TEXTURE_2D, sphereTextureHandle);
//            GL11.glTranslatef(spherePos.x += change.x, spherePos.y += change.y, spherePos.z += change.z);
//            renderSphere();
//            setMaterial( new float[] {.8f, .8f, .7f, 1f}, .4f);
//        }
//        GL11.glPopMatrix();
//    	GL11.glPushMatrix();
//    	{
//    		GL11.glBindTexture(GL11.GL_TEXTURE_2D, sphereTextureHandle);
//    		//GL11.glTranslatef(x, y, z);
//    		renderSphere();
//
//    	}

    	//draw player
        GL11.glPushMatrix();
        {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, cubeTextureHandle);
            GL11.glTranslatef(playerPosition.x, playerPosition.y, playerPosition.z);
            //GL11.glScalef(2f, 2f, 2f);          
            renderCube();
            setMaterial( new float[] {.8f, .8f, .7f, 1f}, .4f);

        }
        GL11.glPopMatrix();
        
        //cube test bottom
        GL11.glPushMatrix();
        {
        	GL11.glBindTexture(GL11.GL_TEXTURE_2D, cubeOtherTextureHandle);
        	GL11.glTranslatef(cube1.position.x+=change.x, cube1.position.y+=change.y, cube1.position.z+=change.z);
        	//delete
        	System.out.println("cube position vector " + cube1.position);
        	//callDisplayList(cubeTestBottom); 
        	renderCube();
        }
        GL11.glPopMatrix();
        
        //cube test top
        GL11.glPushMatrix();
        {
        	GL11.glBindTexture(GL11.GL_TEXTURE_2D, cubeOtherTextureHandle);
        	//GL11.glScalef(2f, 2f, 2f);
        	//callDisplayList(cubeTestTop);
        	GL11.glTranslatef(cube3.position.x+=change.x, cube3.position.y+=change.y, cube3.position.z+=change.z);
        	renderCube();
        }
        GL11.glPopMatrix();
        
      //cube test bottom2
        GL11.glPushMatrix();
        {
        	GL11.glBindTexture(GL11.GL_TEXTURE_2D, cubeOtherTextureHandle);
        	//GL11.glScalef(2f, 2f, 2f);
        	//callDisplayList(cubeTestRot);
        	GL11.glTranslatef(cube2.position.x+=change.x, cube2.position.y+=change.y, cube2.position.z+=change.z);
        	renderCube();
        }
        GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        {
        	GL11.glBindTexture(GL11.GL_TEXTURE_2D, sphereTextureHandle);
        	//callDisplayList(cubeTestRot);
        	GL11.glTranslatef(sphere1.position.x+=change.x, sphere1.position.y+=change.y, sphere1.position.z+=change.z);
        	GL11.glScalef(5f, 5f, 5f);

        	renderSphere();
        }
        GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        {
        	GL11.glBindTexture(GL11.GL_TEXTURE_2D, sphereTextureHandle);
        	//callDisplayList(cubeTestRot);
        	GL11.glTranslatef(sphere2.position.x+=change.x, sphere2.position.y+=change.y, sphere2.position.z+=change.z);
        	GL11.glScalef(7f, 7f, 7f);

        	renderSphere();
        }
        GL11.glPopMatrix();
        
        GL11.glPushMatrix();
        {
        	GL11.glBindTexture(GL11.GL_TEXTURE_2D, sphereTextureHandle);
 
        	//callDisplayList(cubeTestRot);
        	GL11.glTranslatef(sphere3.position.x+=change.x, sphere3.position.y+=change.y, sphere3.position.z+=change.z);
           	GL11.glScalef(5f, 5f, 5f);
        	renderSphere();
        }
        GL11.glPopMatrix();
        
       
    }

	/**
	 * Given position of object and target, create matrix to
	 * orient object so it faces target.
	 */
	public void billboardPoint(GL_Vector bbPos, GL_Vector targetPos, GL_Vector targetUp)
	{
		// direction the billboard will be facing (looking):
		GL_Vector look = GL_Vector.sub(targetPos,bbPos).normalize();

		// billboard Right vector is perpendicular to Look and Up (the targets Up vector)
		GL_Vector right = GL_Vector.crossProduct(targetUp,look).normalize();

		// billboard Up vector is perpendicular to Look and Right
		GL_Vector up = GL_Vector.crossProduct(look,right).normalize();

		// Create a 4x4 matrix that will orient the object at bbPos to face targetPos
		GL_Matrix.createBillboardMatrix(bbmatrix, right, up, look, bbPos);

		// apply the billboard matrix
		GL11.glMultMatrix(bbmatrix);
	}

}