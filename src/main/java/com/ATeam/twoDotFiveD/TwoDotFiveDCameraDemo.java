package com.ATeam.twoDotFiveD;



import static com.bulletphysics.demos.opengl.IGL.GL_LIGHTING;

import java.awt.Color;
import java.nio.FloatBuffer;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.*;
import org.lwjgl.input.*;
import org.lwjgl.util.glu.*;

import com.ATeam.twoDotFiveD.block.Block;
import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.demos.opengl.FastFormat;
import com.bulletphysics.demos.opengl.GLShapeDrawer;
import com.bulletphysics.demos.opengl.IGL;
import com.bulletphysics.demos.opengl.LwjglGL;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Clock;
import com.bulletphysics.linearmath.DebugDrawModes;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.util.ObjectPool;
import com.sun.j3d.utils.geometry.Box;

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
public class TwoDotFiveDCameraDemo extends GLApp{
	GL_Vector worldChangeVector;
	GL_Vector playerPosition;
	Player player = new Player();
	
	private ObjectArrayList<CollisionShape> collisionShapes = new ObjectArrayList<CollisionShape>();
    protected DynamicsWorld dynamicsWorld = null;
   	protected Clock clock = new Clock();
    private static final float BOX_DIM = 0.3f;
    private Transform m = new Transform();
	
	
	//not used
	Block ground = new Block(0f, 0f, -1.01f, 0f, null);

	
    // Handle for texture
    int groundTextureHandle = 0;
    // Light position: if last value is 0, then this describes light direction.  If 1, then light position.
    float lightPosition[]= { -2f, 2f, 2f, 0f };
    // Camera position
    float[] cameraPos = {0f,3f,15f};

    GLCamera camera1 = new GLCamera();
    GLCam cam = new GLCam(camera1);

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
         
    	initializePhysics();
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

        groundTextureHandle = makeTexture("src/main/resources/com/lovetextures/ground.jpg",true,true);
	    camera1.setCamera(0,0,15, 0,0,-1, 0,1,0);
      

        // make a shadow handler
        // params:
        //		the light position,
        //		the plane the shadow will fall on,
        //		the color of the shadow,
        // 		this application,
        // 		the function that draws all objects that cast shadows
        //objectsShadow = new GLShadowOnPlane(lightPosition, new float[] {0f,1f,0f,0f}, null, this, method(this, "drawObjects()"));
    }
    
    public void initializePhysics() {
		
        dynamicsWorld = createDynamicsWorld();
        dynamicsWorld.setGravity(new Vector3f(0f, -10f, 0f));
        
		CollisionShape groundShape = new BoxShape(new Vector3f(5f, 5f, 5f));
		collisionShapes.add(groundShape);
		Transform groundTransform = new Transform();
		groundTransform.setIdentity();
		groundTransform.origin.set(0, -5, 0);

		{
			float mass = 0f;

			// rigidbody is dynamic if and only if mass is non zero, otherwise static
			boolean isDynamic = (mass != 0f);

			Vector3f localInertia = new Vector3f(0, 0, 0);
			if (isDynamic) {
				groundShape.calculateLocalInertia(mass, localInertia);
			}

			// using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
			DefaultMotionState myMotionState = new DefaultMotionState(groundTransform);
			RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, groundShape, localInertia);
			RigidBody body = new RigidBody(rbInfo);

			// add the body to the dynamics world
			dynamicsWorld.addRigidBody(body);
		}
		
		{
			// create a few dynamic rigidbodies
			// Re-using the same collision is better for memory usage and performance

			CollisionShape colShape = new BoxShape(new Vector3f(1, 1, 1));
			//CollisionShape colShape = new SphereShape(1f);
			collisionShapes.add(colShape);

			// Create Dynamic Objects
			Transform startTransform = new Transform();
			startTransform.setIdentity();

			//-mass  = phasing
			float mass = 1f;

			// rigidbody is dynamic if and only if mass is non zero, otherwise static
			boolean isDynamic = (mass != 0f);

			Vector3f localInertia = new Vector3f(0, -1, 0);
			if (isDynamic) {
				colShape.calculateLocalInertia(mass, localInertia);
			}


			startTransform.origin.set(0,5,0);

			// using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
			DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
			RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, colShape, localInertia);
			RigidBody body = new RigidBody(rbInfo);
			body.setActivationState(RigidBody.ISLAND_SLEEPING);

			dynamicsWorld.addRigidBody(body);
			body.setActivationState(RigidBody.ISLAND_SLEEPING);
		}
    }


    private DynamicsWorld createDynamicsWorld() {
        DefaultCollisionConfiguration collisionConfiguration
                = new DefaultCollisionConfiguration();
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

        Vector3f worldAabbMin = new Vector3f(-1000, -1000, -1000);
        Vector3f worldAabbMax = new Vector3f( 1000,  1000,  1000);
        final int maxProxies = 1024;
        BroadphaseInterface broadphase = new AxisSweep3(
                worldAabbMin, worldAabbMax, maxProxies);

        ConstraintSolver solver = new SequentialImpulseConstraintSolver();

        return new DiscreteDynamicsWorld(
                dispatcher, broadphase, solver, collisionConfiguration);
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
    	
//    	player.handleMovementKeys(cam.getDirection(), cam.getQuadrant());
//    	player.setWorldChangeVector();
//    	worldChangeVector = player.getWorldChangeVector();
//    	System.out.println("world worldChangeVector vector " + worldChangeVector);
//    	playerPosition = player.getPosition();
    	

    	
    	//update the camera pan based on direction
    	cam.updatePan(cam.getDirection());
    	//handle camera input keys
    	cam.handleRotKeysPan();
       
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        cam.render();
		float ms = getDeltaTimeMicroseconds();

        if (dynamicsWorld != null) {
			dynamicsWorld.stepSimulation(ms / 1000000f);
			// optional but useful: debug drawing
		}
    	
    	if(dynamicsWorld != null) {
    		int numObjects = dynamicsWorld.getNumCollisionObjects();
    		for (int i = 0; i < numObjects; i++) {
    			CollisionObject colObj = dynamicsWorld.getCollisionObjectArray().getQuick(i);
    			RigidBody body = RigidBody.upcast(colObj);
    			System.out.println(body.isActive());
    			if (body != null && body.getMotionState() != null) {
    				DefaultMotionState myMotionState = (DefaultMotionState) body.getMotionState();
    				m.set(myMotionState.graphicsWorldTrans);
    			}
    			else {
					colObj.getWorldTransform(m);
				}
    			
    			Vector3f trans = m.origin;
    			GL11.glPushMatrix();
    			GL11.glTranslatef(trans.x, trans.y, trans.z);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, groundTextureHandle);
                renderCube();
                GL11.glPopMatrix();
    		    
    		}
    	}
      
        
        setLightPosition(GL11.GL_LIGHT1, lightPosition);

        //display user directions
        print( 30, viewportH- 40, "Left-Right arrows rotate camera during sidescrolling view", 1);
        print( 30, viewportH- 60, "Up arrow to engage top-down view", 1);
        print( 30, viewportH- 80, "Down to return to sidescrolling view", 1);
        print( 30, viewportH- 120, "W,A,S,D keys to move cube", 1);
        print( 30, viewportH- 140, "Space to jump", 1);
        print( 30, viewportH-180, "Running AVG FPS: " + Double.toString(GLApp.getFramesPerSecond()), 1);
    }
    
    
  
    
	public float getDeltaTimeMicroseconds() {
		//#ifdef USE_BT_CLOCK
		float dt = clock.getTimeMicroseconds();
		clock.reset();
		return dt;
		//#else
		//return btScalar(16666.);
		//#endif
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