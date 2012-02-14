package demo.lwjgl.basic;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.*;
import org.lwjgl.input.*;
import org.lwjgl.util.glu.*;
import lib.lwjgl.glmodel.*;

/**
 * Render a scene with two cameras, one still and one moving.  Hit SPACE to
 * toggle viewpoints between the two cameras.  Uses the GLCamera class to
 * hold camera position and orientation, and the GLCam class to move the
 * current camera in response to arrow key events.
 * <P>
 * @see GLCamera.java
 * @see GLCam.java
 * <P>
 * napier at potatoland dot org
 */
public class GLAppDemoCamera extends GLApp {
    // Handle for texture
    int cubeTextureHandle = 0;
    int groundTextureHandle = 0;
    // Light position: if last value is 0, then this describes light direction.  If 1, then light position.
    float lightPosition[]= { -2f, 2f, 2f, 0f };

    // camera and a cam to move them around scene
    GLCamera camera1 = new GLCamera();
    GLCam cam = new GLCam(camera1);

    // vectors used to orient airplane motion
    GL_Vector UP = new GL_Vector(0,1,0);
    GL_Vector ORIGIN = new GL_Vector(0,0,0);

    // for earth rotation
    float degrees = 0;

    // model of airplane and sphere displaylist for earth
    GLModel airplane;
    int cube;
    int sphere;

    // shadow handler will draw a shadow on floor plane
    GLShadowOnPlane airplaneShadow;

    public GL_Vector spherePos;

	FloatBuffer bbmatrix = GLApp.allocFloats(16);

    /**
     * Initialize application and run main loop.
     */
    public static void main(String args[]) {
    	GLAppDemoCamera demo = new GLAppDemoCamera();
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
        // setup and enable perspective
        setPerspective();

        // Create a light (diffuse light, ambient light, position)
        setLight( GL11.GL_LIGHT1,
        		new float[] { 1f, 1f, 1f, 1f },
        		new float[] { 0.5f, 0.5f, .53f, 1f },
        		new float[] { 1f, 1f, 1f, 1f },
        		lightPosition );

        // Create a directional light (light green, to simulate reflection off grass)
        setLight( GL11.GL_LIGHT2,
        		new float[] { 0.15f, 0f, 0f, 1.0f },  // diffuse color
        		new float[] { 0.0f, 0.0f, 0.0f, 1.0f },   // ambient
        		new float[] { 0.0f, 0.0f, 0.0f, 1.0f },   // specular
        		new float[] { 0.0f, -10f, 0.0f, 0f } );   // direction (pointing up)

        // enable lighting and texture rendering
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Enable alpha transparency (so text will have transparent background)
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Create texture for cube
        cubeTextureHandle = makeTexture("src/main/resources/com/lovetextures/cube.png");

        // Create texture for ground plane
        groundTextureHandle = makeTexture("src/main/resources/com/lovetextures/ground.jpg",true,true);

        // set camera 1 position
	     camera1.setCamera(0,0,20, 0,0,0, 0,1,0);

        // load the airplane model and make it a display list
        //airplane = new GLModel("src/main/resources/com/oyonale/toyplane.obj");
        //airplane.mesh.regenerateNormals();
        //airplane.makeDisplayList();

        // make a cube display list
        cube = beginDisplayList(); {
        	renderCube();
        }
        endDisplayList();

        // make a shadow handler
        // params:
        //		the light position,
        //		the plane the shadow will fall on,
        //		the color of the shadow,
        // 		this application,
        // 		the function that draws all objects that cast shadows
        airplaneShadow = new GLShadowOnPlane(lightPosition, new float[] {0f,1f,0f,3f}, new float[] {0f, 0f, 0f, 0f}, this, method(this,"drawObjects"));
    }

    /**
     * set the field of view and view depth.
     */
    public static void setPerspective()
    {
        // select projection matrix (controls perspective)
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        // fovy, aspect ratio, zNear, zFar
        GLU.gluPerspective(50f,         // zoom in or out of view
                           aspectRatio, // shape of viewport rectangle
                           .1f,         // Min Z: how far from eye position does view start
                           500f);       // max Z: how far from eye position does view extend
        // return to modelview matrix
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    /**
     * Render one frame.  Called by GLApp.run().
     */
    public void draw() {
    	degrees += 90f * GLApp.getSecondsPerFrame();

        // place airplane in orbit around ball, and place camera slightly above airplane
    	spherePos = GL_Vector.rotationVector(degrees).mult(8);

    

        // user keystrokes adjust camera position
        //cam.handleRotKeys();
    	/*String direction = cam.direction;
    	
    	if(cam.isRunning){
    		cam.updatePan(direction);
    	}
    	else if (!cam.isRunning) {
    		cam.handleRotKeysPan();
  
    	}*/
    	
        

       
        // clear depth buffer and color
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // select model view for subsequent transforms
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        // do gluLookAt() with camera position, direction, orientation
        cam.render();

        // draw the ground plane
        GL11.glPushMatrix();
        {
            GL11.glTranslatef(0f, -3f, 0f); // down a bit
            GL11.glScalef(15f, .01f, 15f);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, groundTextureHandle);
            renderCube();
        }
        GL11.glPopMatrix();

        // invokes the drawObjects() method to create shadows for objects in the scene
        airplaneShadow.drawShadow();

        // draw sphere at center (rotate 10 degrees per second)
        rotation += 10f * getSecondsPerFrame();

        // draw the scene (after we draw the shadows, so everything layers correctly)
        drawObjects();

        // Place the light.  Light will move with the rest of the scene
        setLightPosition(GL11.GL_LIGHT1, lightPosition);
        

		// render some text using texture-mapped font
        print( 30, viewportH- 40, "Up arrow to return to origin for overhead", 1);
        print( 30, viewportH- 60, "Down to return to sidescrolling view", 1);

        print( 30, viewportH- 80, "Left-Right arrows rotate camera", 1);
        print( 30, viewportH-100, Double.toString(GLApp.getFramesPerSecond()), 1);
    }

    public void drawObjects() {
        // draw the airplane
        GL11.glPushMatrix();
        {
        	// place plane at orbit point, and orient it toward origin
        	billboardPoint(spherePos, ORIGIN, UP);
        	// turn plane toward direction of motion
            GL11.glRotatef(180, 0, 1, 0);
            // Make it smaller
            //GL11.glScalef(0.05f, 0.05f, 0.05f);
        	renderSphere();
        	// reset material, since model.render() will alter current material settings
            setMaterial( new float[] {.8f, .8f, .7f, 1f}, .4f);
        }
        GL11.glPopMatrix();

    	// draw the cube
        GL11.glPushMatrix();
        {
           	GL11.glColor4f(0f, .5f, 1f, 1f);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, cubeTextureHandle);
            GL11.glScalef(2f, 2f, 2f);
           	renderCube();
            callDisplayList(cube);
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
