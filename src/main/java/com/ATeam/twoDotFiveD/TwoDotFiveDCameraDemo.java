package com.ATeam.twoDotFiveD;



import java.awt.Color;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.*;
import org.lwjgl.input.*;
import org.lwjgl.util.glu.*;

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
    // Handle for texture
    int cubeTextureHandle = 0;
    int sphereTextureHandle = 0;
    int groundTextureHandle = 0;
    int cubeOtherTextureHandle = 0;
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

        
        
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        cubeTextureHandle = makeTexture("src/main/resources/com/lovetextures/cube.png");
        sphereTextureHandle = makeTexture("src/main/resources/com/lovetextures/sphere.png");
        cubeOtherTextureHandle = makeTexture("src/main/resources/com/lovetextures/grey.png");
        groundTextureHandle = makeTexture("src/main/resources/com/lovetextures/ground.jpg",true,true);
	    camera1.setCamera(0,0,20, 0,0,0, 0,1,0);

	    cubeTestBottom = beginDisplayList(); {
       	renderCubeCoord(-11f, -1.0f, -7f, 2.0f);
	    //	drawCube(10f);
        }
        endDisplayList();
        
        cubeTestTop = beginDisplayList(); {
        	renderCubeCoord(-10f, 1.0f, -6f, 2.0f);
        }
        endDisplayList();
        
        cubeTestRot = beginDisplayList(); {
        	renderCubeCoord(-9f, -1.0f, -5f, 2.0f);
        }
        endDisplayList();

        // make a cube display list
        cube = beginDisplayList(); {
        	renderCube();
        }
        endDisplayList();
        
        // make a sphere display list
        sphere = beginDisplayList(); {
        	renderSphere();
        }
        endDisplayList();
        
        

        // make a shadow handler
        // params:
        //		the light position,
        //		the plane the shadow will fall on,
        //		the color of the shadow,
        // 		this application,
        // 		the function that draws all objects that cast shadows
        objectsShadow = new GLShadowOnPlane(lightPosition, new float[] {0f,1f,0f,2f}, null, this, method(this,"drawObjects"));
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
    	degrees += 90f * GLApp.getSecondsPerFrame();
    	spherePos = GL_Vector.rotationVector(degrees).mult(8);
    	
    	//update the camera pan based on direction
    	cam.updatePan(cam.getDirection());
    	//handle camera input keys
    	cam.handleRotKeysPan();
       
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        cam.render();
        
        //ground
        GL11.glPushMatrix();
        {
            GL11.glTranslatef(0f, -2f, 0f); 
            GL11.glScalef(15f, .01f, 15f);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, groundTextureHandle);
            renderCube();
        }
        GL11.glPopMatrix();
        
//        GL11.glPushMatrix();
//        {
//        	float x,y,z;
//        	x=y=z=0.0f;
//        	y = 13f;
//        	GL11.glBegin(GL11.GL_QUADS);
//        	float hs = 30f/2f;
//        	//front face
//        	GL11.glNormal3f( 0.0f, 0.0f, 1.0f);
//            GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(x-hs, y-hs, z-hs);	// Bottom Left
//            GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( x+hs, y-hs,  z-hs);	// Bottom Right
//            GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( x+hs,  y+hs,  z-hs);	// Top Right
//            GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(x-hs,  y+hs,  z-hs);	// Top Left
//            
//         // Back Face
//            GL11.glNormal3f( 0.0f, 0.0f, -1.0f);
//            GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(x-hs, y-hs, z+hs);	// Bottom Right
//            GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(x-hs,  y+hs, z+hs);	// Top Right
//            GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f( x+hs,  y+hs, z+hs);	// Top Left
//            GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f( x+hs, y-hs, z+hs);	// Bottom Left
//            
//         // Right face
//            GL11.glNormal3f( 1.0f, 0.0f, 0.0f);
//            GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f( x-hs, y-hs, z-hs);	// Bottom Right
//            GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f( x-hs,  y+hs, z-hs);	// Top Right
//            GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f( x-hs,  y+hs,  z+hs);	// Top Left
//            GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f( x-hs, y-hs,  z+hs);	// Bottom Left
//            
//         // Left Face
//            GL11.glNormal3f( -1.0f, 0.0f, 0.0f);
//            GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(x+hs, y-hs, z-hs);	// Bottom Left
//            GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(x+hs, y-hs,  z+hs);	// Bottom Right
//            GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(x+hs,  y+hs,  z+hs);	// Top Right
//            GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(x+hs,  y+hs, z-hs);	// Top Left
//            GL11.glEnd();
//        }

        objectsShadow.drawShadow();
        drawObjects();

        setLightPosition(GL11.GL_LIGHT1, lightPosition);
        //setLightPosition( GL11.GL_LIGHT2, -2f, 2f, 0f);

        //display user directions
        print( 30, viewportH- 40, "Left-Right arrows rotate camera during sidescrolling view", 1);
        print( 30, viewportH- 60, "Up arrow to engage top-down view", 1);
        print( 30, viewportH- 80, "Down to return to sidescrolling view", 1);
        print( 30, viewportH-100, "Running AVG FPS: " + Double.toString(GLApp.getFramesPerSecond()), 1);
    }

    public void drawObjects() {
        //sphere
        GL11.glPushMatrix();
        {
        	billboardPoint(spherePos, ORIGIN, UP);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, sphereTextureHandle);
            callDisplayList(sphere);
            setMaterial( new float[] {.8f, .8f, .7f, 1f}, .4f);
        }
        GL11.glPopMatrix();

    	//cube
        GL11.glPushMatrix();
        {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, cubeTextureHandle);
            GL11.glScalef(2f, 2f, 2f);          
            callDisplayList(cube);
        }
        GL11.glPopMatrix();
        
        //cube test bottom
        GL11.glPushMatrix();
        {
        	GL11.glBindTexture(GL11.GL_TEXTURE_2D, cubeOtherTextureHandle);
        	//GL11.glScalef(2f, 2f, 2f);
        	callDisplayList(cubeTestBottom);
        }
        GL11.glPopMatrix();
        
        //cube test top
        GL11.glPushMatrix();
        {
        	GL11.glBindTexture(GL11.GL_TEXTURE_2D, cubeOtherTextureHandle);
        	//GL11.glScalef(2f, 2f, 2f);
        	callDisplayList(cubeTestTop);
        }
        GL11.glPopMatrix();
        
      //cube test rot
        GL11.glPushMatrix();
        {
        	GL11.glBindTexture(GL11.GL_TEXTURE_2D, cubeOtherTextureHandle);
        	//GL11.glScalef(2f, 2f, 2f);
        	callDisplayList(cubeTestRot);
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