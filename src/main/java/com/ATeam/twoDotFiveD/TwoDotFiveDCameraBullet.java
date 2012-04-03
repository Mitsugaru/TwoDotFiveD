package com.ATeam.twoDotFiveD;

import static com.bulletphysics.demos.opengl.IGL.GL_AMBIENT;
import static com.bulletphysics.demos.opengl.IGL.GL_COLOR_BUFFER_BIT;
import static com.bulletphysics.demos.opengl.IGL.GL_DEPTH_BUFFER_BIT;
import static com.bulletphysics.demos.opengl.IGL.GL_DEPTH_TEST;
import static com.bulletphysics.demos.opengl.IGL.GL_DIFFUSE;
import static com.bulletphysics.demos.opengl.IGL.GL_LESS;
import static com.bulletphysics.demos.opengl.IGL.GL_LIGHT0;
import static com.bulletphysics.demos.opengl.IGL.GL_LIGHT1;
import static com.bulletphysics.demos.opengl.IGL.GL_LIGHTING;
import static com.bulletphysics.demos.opengl.IGL.GL_LINES;
import static com.bulletphysics.demos.opengl.IGL.GL_MODELVIEW;
import static com.bulletphysics.demos.opengl.IGL.GL_POSITION;
import static com.bulletphysics.demos.opengl.IGL.GL_PROJECTION;
import static com.bulletphysics.demos.opengl.IGL.GL_SMOOTH;
import static com.bulletphysics.demos.opengl.IGL.GL_SPECULAR;
import static com.bulletphysics.demos.opengl.IGL.GL_TRIANGLES;

import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import lib.lwjgl.glmodel.GL_Vector;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.ConcaveShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.InternalTriangleIndexCallback;
import com.bulletphysics.collision.shapes.PolyhedralConvexShape;
import com.bulletphysics.collision.shapes.ShapeHull;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.collision.shapes.TriangleCallback;
import com.bulletphysics.demos.basic.BasicDemo;
import com.bulletphysics.demos.opengl.DemoApplication;
import com.bulletphysics.demos.opengl.FastFormat;
import com.bulletphysics.demos.opengl.GLDebugDrawer;
import com.bulletphysics.demos.opengl.GLShapeDrawer;
import com.bulletphysics.demos.opengl.IGL;
import com.bulletphysics.demos.opengl.LWJGL;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DebugDrawModes;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.TransformUtil;
import com.bulletphysics.linearmath.VectorUtil;
import com.bulletphysics.util.IntArrayList;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.util.ObjectPool;

import demo.lwjgl.basic.GLApp;
import demo.lwjgl.basic.GLCam;
import demo.lwjgl.basic.GLCamera;

public class TwoDotFiveDCameraBullet extends DemoApplication{
	int cubeTextureHandle;
	
	private ObjectArrayList<CollisionShape> collisionShapes = new ObjectArrayList<CollisionShape>();
	private BroadphaseInterface broadphase;
	private CollisionDispatcher dispatcher;
	private ConstraintSolver solver;
	private DefaultCollisionConfiguration collisionConfiguration;
	
	public KinematicCharacterController character;
	public PairCachingGhostObject ghostObject;
	
	private GLCam cam = new GLCam();
	private GLCamera camera = new GLCamera();
	GL_Vector ViewPoint;
	
	private float characterScale = 2f;

	private static int gForward = 0;
	private static int gBackward = 0;
	private static int gLeft = 0;
	private static int gRight = 0;
	private static int gJump = 0;
	
	private Vector3f globalTrans = new Vector3f();

    
    //texture
	int[] textures = new int[4];

	public TwoDotFiveDCameraBullet(IGL gl) {
		super(gl);
	}

	public void setup() {
      cam.setCamera(camera);
      camera.setCamera(0f,0f,15f,  0f,0f,-1f, 0f,1f,0f);

	}
	
	
	
	public void initPhysics(){
		CollisionShape groundShape = new BoxShape(new Vector3f(10f, 0.001f, 10f));
		collisionShapes.add(groundShape);
		
		collisionConfiguration = new DefaultCollisionConfiguration();
		dispatcher = new CollisionDispatcher(collisionConfiguration);
		Vector3f worldMin = new Vector3f(-1000f,-1000f,-1000f);
		Vector3f worldMax = new Vector3f(1000f,1000f,1000f);
		AxisSweep3 sweepBP = new AxisSweep3(worldMin, worldMax);
		broadphase = sweepBP;
		
		solver = new SequentialImpulseConstraintSolver();
		dynamicsWorld = new DiscreteDynamicsWorld(dispatcher,broadphase,solver,collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0f, -10f, 0f));
		
		Transform startTransform = new Transform();
		startTransform.setIdentity();
		startTransform.origin.set(4.0f, 4.0f, 0.0f);
		
		ghostObject = new PairCachingGhostObject();
		ghostObject.setWorldTransform(startTransform);
		sweepBP.getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		
		BoxShape box = new BoxShape(new Vector3f(1f, 1f, 1f));
		ghostObject.setCollisionShape(box);
		ghostObject.setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);

		character = new KinematicCharacterController(ghostObject, box, 1f);

		dynamicsWorld.addCollisionObject(ghostObject, CollisionFilterGroups.CHARACTER_FILTER, (short)(CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.DEFAULT_FILTER));
		dynamicsWorld.addAction(character);

		
		Transform groundTransform = new Transform();
		groundTransform.setIdentity();
		groundTransform.origin.set(0, -1, 0);

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
			Transform startObj = new Transform();
			startObj.setIdentity();

			//-mass  = phasing
			float mass = 1f;

			// rigidbody is dynamic if and only if mass is non zero, otherwise static
			boolean isDynamic = (mass != 0f);

			Vector3f localInertia = new Vector3f(0, -1, 0);
			if (isDynamic) {
				colShape.calculateLocalInertia(mass, localInertia);
			}


			startObj.origin.set(5,4,0);

			// using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
			DefaultMotionState myMotionState = new DefaultMotionState(startObj);
			RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, colShape, localInertia);
			RigidBody body = new RigidBody(rbInfo);
			body.setActivationState(RigidBody.ISLAND_SLEEPING);

			dynamicsWorld.addRigidBody(body);
			body.setActivationState(RigidBody.ISLAND_SLEEPING);
		}

		{
			// create a few dynamic rigidbodies
			// Re-using the same collision is better for memory usage and performance

			CollisionShape colShape = new BoxShape(new Vector3f(1.5f, 1.5f, 1.5f));
			//CollisionShape colShape = new SphereShape(1f);
			collisionShapes.add(colShape);

			// Create Dynamic Objects
			Transform startObj = new Transform();
			startObj.setIdentity();

			//-mass  = phasing
			float mass = 1f;

			// rigidbody is dynamic if and only if mass is non zero, otherwise static
			boolean isDynamic = (mass != 0f);

			Vector3f localInertia = new Vector3f(0, -1, 0);
			if (isDynamic) {
				colShape.calculateLocalInertia(mass, localInertia);
			}


			startObj.origin.set(-4,4,4);

			// using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
			DefaultMotionState myMotionState = new DefaultMotionState(startObj);
			RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, colShape, localInertia);
			RigidBody body = new RigidBody(rbInfo);
			body.setActivationState(RigidBody.ISLAND_SLEEPING);

			dynamicsWorld.addRigidBody(body);
			body.setActivationState(RigidBody.ISLAND_SLEEPING);
		}
//		{
//			// create a few dynamic rigidbodies
//			// Re-using the same collision is better for memory usage and performance
//
//			CollisionShape colShape = new BoxShape(new Vector3f(1, 1, 1));
//			//CollisionShape colShape = new SphereShape(1f);
//			collisionShapes.add(colShape);
//
//			// Create Dynamic Objects
//			Transform startObj = new Transform();
//			startObj.setIdentity();
//
//			//-mass  = phasing
//			float mass = 1f;
//
//			// rigidbody is dynamic if and only if mass is non zero, otherwise static
//			boolean isDynamic = (mass != 0f);
//
//			Vector3f localInertia = new Vector3f(0, -1, 0);
//			if (isDynamic) {
//				colShape.calculateLocalInertia(mass, localInertia);
//			}
//
//
//			startObj.origin.set(0,0,0);
//
//			// using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
//			DefaultMotionState myMotionState = new DefaultMotionState(startObj);
//			RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, colShape, localInertia);
//			RigidBody body = new RigidBody(rbInfo);
//			body.setActivationState(RigidBody.ISLAND_SLEEPING);
//
//			dynamicsWorld.addRigidBody(body);
//			body.setActivationState(RigidBody.ISLAND_SLEEPING);
//		}
	
//		{
//			// create a few dynamic rigidbodies
//			// Re-using the same collision is better for memory usage and performance
//
//			CollisionShape colShape = new BoxShape(new Vector3f(4, 1.5f, 0.3f));
//			//CollisionShape colShape = new SphereShape(1f);
//			collisionShapes.add(colShape);
//
//			// Create Dynamic Objects
//			Transform startObj = new Transform();
//			startObj.setIdentity();
//
//			//-mass  = phasing
//			float mass = 0f;
//
//			// rigidbody is dynamic if and only if mass is non zero, otherwise static
//			boolean isDynamic = (mass != 0f);
//
//			Vector3f localInertia = new Vector3f(0, -1, 0);
//			if (isDynamic) {
//				colShape.calculateLocalInertia(mass, localInertia);
//			}
//
//
//			startObj.origin.set(-6,0.3f,-8);
//
//			// using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
//			DefaultMotionState myMotionState = new DefaultMotionState(startObj);
//			RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, colShape, localInertia);
//			RigidBody body = new RigidBody(rbInfo);
//			body.setActivationState(RigidBody.ISLAND_SLEEPING);
//
//			dynamicsWorld.addRigidBody(body);
//			body.setActivationState(RigidBody.ISLAND_SLEEPING);
//		}
		for(CollisionObject o : dynamicsWorld.getCollisionObjectArray())
		{
			if(o.getCollisionShape().equals(ghostObject.getCollisionShape()))
			{
				//TODO WE FOUND IT OD UOUR STUFFF
				System.out.println("stuff");
				Transform t = o.getWorldTransform(new Transform());
				System.out.println(t.getMatrix(new Matrix4f()).toString());
				System.out.println("x: " + t.origin.x + " y: " + t.origin.y + " z: " + t.origin.z);
			}
		}
		resetScene();		
	}
	@Override
	public void myinit() {
		float[] light_ambient = new float[] { 0.2f, 0.2f, 0.2f, 1.0f };
		float[] light_diffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] light_specular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		/* light_position is NOT default value */
		float[] light_position0 = new float[] { 1.0f, 10.0f, 1.0f, 0.0f };
		//float[] light_position1 = new float[] { -1.0f, -10.0f, -1.0f, 0.0f };

		gl.glLight(GL_LIGHT0, GL_AMBIENT, light_ambient);
		gl.glLight(GL_LIGHT0, GL_DIFFUSE, light_diffuse);
		gl.glLight(GL_LIGHT0, GL_SPECULAR, light_specular);
		gl.glLight(GL_LIGHT0, GL_POSITION, light_position0);

		gl.glLight(GL_LIGHT1, GL_AMBIENT, light_ambient);
		gl.glLight(GL_LIGHT1, GL_DIFFUSE, light_diffuse);
		gl.glLight(GL_LIGHT1, GL_SPECULAR, light_specular);
		//gl.glLight(GL_LIGHT1, GL_POSITION, light_position1);

		gl.glEnable(GL_LIGHTING);
		gl.glEnable(GL_LIGHT0);
		gl.glEnable(GL_LIGHT1);

		gl.glShadeModel(GL_SMOOTH);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LESS);

		gl.glClearColor(0.7f, 0.7f, 0.7f, 0f);
        gl.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		cubeTextureHandle = 0;
		cubeTextureHandle = GLApp.makeTexture("src/main/resources/com/lovetextures/cube.png");
		//cube
		textures[0] = 0;
		textures[0] = GLApp.makeTexture("src/main/resources/com/lovetextures/cube.png");
		

		//glEnable(GL_CULL_FACE);
		//glCullFace(GL_BACK);
	}

	@Override
	public void clientMoveAndDisplay() {
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


		// simple dynamics world doesn't handle fixed-time-stepping
		float ms = getDeltaTimeMicroseconds();

		// step the simulation
		if (dynamicsWorld != null) {
			int maxSimSubSteps = idle ? 1 : 2;
			if (idle) {
				ms = 1.0f / 420.f;
			}

			// set walkDirection for our character
			Transform xform = ghostObject.getWorldTransform(new Transform());

			Vector3f forwardDir = new Vector3f();
			xform.basis.getRow(2, forwardDir);
			//printf("forwardDir=%f,%f,%f\n",forwardDir[0],forwardDir[1],forwardDir[2]);
			Vector3f upDir = new Vector3f();
			xform.basis.getRow(1, upDir);
			Vector3f strafeDir = new Vector3f();
			xform.basis.getRow(0, strafeDir);
			forwardDir.normalize();
			upDir.normalize();
			strafeDir.normalize();

			Vector3f walkDirection = new Vector3f(0.0f, 0.0f, 0.0f);
			float walkVelocity = 30;// 4 km/h -> 1.1 m/s
			float walkSpeed = walkVelocity * (ms/1000000f) * characterScale;
			
			if (gLeft != 0) {
				walkDirection.sub(strafeDir);
			}

			if (gRight != 0) {
				walkDirection.add(strafeDir);
			}

			if (gForward != 0) {
				walkDirection.sub(forwardDir);
			}

			if (gBackward != 0) {
				walkDirection.add(forwardDir);
			}
		
			walkDirection.scale(walkSpeed);
		
			character.setWalkDirection(walkDirection);
			globalTrans.x += walkDirection.x;
			globalTrans.y += walkDirection.y;
			globalTrans.z += walkDirection.z;
			
			
		//	int numSimSteps = dynamicsWorld.stepSimulation(ms, maxSimSubSteps);

			dynamicsWorld.stepSimulation(ms / 1000000f);
			// optional but useful: debug drawing
			dynamicsWorld.debugDrawWorld();
		}
		cam.resetClock();
		renderme();
		
	}
	


	@Override
	public void renderme() {
		updateCamera();

		if (dynamicsWorld != null) {
			int numObjects = dynamicsWorld.getNumCollisionObjects();
			wireColor.set(1f, 0f, 0f);
			for (int i = 0; i < numObjects; i++) {
				CollisionObject colObj = dynamicsWorld.getCollisionObjectArray().getQuick(i);
				RigidBody body = RigidBody.upcast(colObj);

				if (body != null && body.getMotionState() != null) {
					DefaultMotionState myMotionState = (DefaultMotionState) body.getMotionState();
					m.set(myMotionState.graphicsWorldTrans);
				}
				else {
					colObj.getWorldTransform(m);
				}
				System.out.println(m.origin);
				
				
				wireColor.set(1f, 1f, 0.5f); // wants deactivation
				if ((i & 1) != 0) {
					wireColor.set(0f, 0f, 1f);
				}

				// color differently for active, sleeping, wantsdeactivation states
				if (colObj.getActivationState() == 1) // active
				{
					if ((i & 1) != 0) {
						//wireColor.add(new Vector3f(1f, 0f, 0f));
						wireColor.x += 1f;
					}
					else {
						//wireColor.add(new Vector3f(0.5f, 0f, 0f));
						wireColor.x += 0.5f;
					}
				}
				if (colObj.getActivationState() == 2) // ISLAND_SLEEPING
				{
					if ((i & 1) != 0) {
						//wireColor.add(new Vector3f(0f, 1f, 0f));
						wireColor.y += 1f;
					}
					else {
						//wireColor.add(new Vector3f(0f, 0.5f, 0f));
						wireColor.y += 0.5f;
					}
				}

				int a = 0;
				drawOpenGL(gl, m, colObj.getCollisionShape(), wireColor, getDebugMode(), a);
			}
			
			

			float xOffset = 10f;
			float yStart = 20f;
			float yIncr = 20f;

			gl.glDisable(GL_LIGHTING);
			gl.glColor3f(0f, 0f, 0f);

			if ((debugMode & DebugDrawModes.NO_HELP_TEXT) == 0) {
				setOrthographicProjection();

				yStart = showProfileInfo(xOffset, yStart, yIncr);
				
								//#ifdef SHOW_NUM_DEEP_PENETRATIONS
				buf.setLength(0);
				buf.append("gNumDeepPenetrationChecks = ");
				FastFormat.append(buf, BulletStats.gNumDeepPenetrationChecks);
				drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
				yStart += yIncr;

				buf.setLength(0);
				buf.append("gNumGjkChecks = ");
				FastFormat.append(buf, BulletStats.gNumGjkChecks);
				drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
				yStart += yIncr;

				buf.setLength(0);
				buf.append("gNumSplitImpulseRecoveries = ");
				FastFormat.append(buf, BulletStats.gNumSplitImpulseRecoveries);
				drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
				yStart += yIncr;

				//buf = String.format("gNumAlignedAllocs = %d", BulletGlobals.gNumAlignedAllocs);
				// TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
				//yStart += yIncr;

				//buf = String.format("gNumAlignedFree= %d", BulletGlobals.gNumAlignedFree);
				// TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
				//yStart += yIncr;

				//buf = String.format("# alloc-free = %d", BulletGlobals.gNumAlignedAllocs - BulletGlobals.gNumAlignedFree);
				// TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
				//yStart += yIncr;

				//enable BT_DEBUG_MEMORY_ALLOCATIONS define in Bullet/src/LinearMath/btAlignedAllocator.h for memory leak detection
				//#ifdef BT_DEBUG_MEMORY_ALLOCATIONS
				//glRasterPos3f(xOffset,yStart,0);
				//sprintf(buf,"gTotalBytesAlignedAllocs = %d",gTotalBytesAlignedAllocs);
				//BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
				//yStart += yIncr;
				//#endif //BT_DEBUG_MEMORY_ALLOCATIONS

				if (getDynamicsWorld() != null) {
					buf.setLength(0);
					buf.append("# objects = ");
					FastFormat.append(buf, getDynamicsWorld().getNumCollisionObjects());
					drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
					yStart += yIncr;

					buf.setLength(0);
					buf.append("# pairs = ");
					FastFormat.append(buf, getDynamicsWorld().getBroadphase().getOverlappingPairCache().getNumOverlappingPairs());
					drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
					yStart += yIncr;

				}
				//#endif //SHOW_NUM_DEEP_PENETRATIONS

				// JAVA NOTE: added
				int free = (int)Runtime.getRuntime().freeMemory();
				int total = (int)Runtime.getRuntime().totalMemory();
				buf.setLength(0);
				buf.append("heap = ");
				FastFormat.append(buf, (float)(total - free) / (1024*1024));
				buf.append(" / ");
				FastFormat.append(buf, (float)(total) / (1024*1024));
				buf.append(" MB");
				drawString(buf, Math.round(xOffset), Math.round(yStart), TEXT_COLOR);
				yStart += yIncr;

				resetPerspectiveProjection();
			}

			gl.glEnable(GL_LIGHTING);
		}

		updateCamera();
	}
	

	@Override 
	public void specialKeyboard(int key, int x, int y, int modifiers) {
		switch (key) {
		case Keyboard.KEY_UP: {
			cam.handleRotKeysPan();
			break;
		}
		case Keyboard.KEY_DOWN: {
			cam.handleRotKeysPan();
			break;
		}
		case Keyboard.KEY_LEFT: {
			cam.handleRotKeysPan();
			break;
		}
		case Keyboard.KEY_RIGHT: {
			cam.handleRotKeysPan();
			break;
		}
		case Keyboard.KEY_W: {
			if (cam.getQuadrant() == 1) {
				gForward = 1;
			}
			if (cam.getQuadrant() == 2) {
				gLeft = 1;
			}
			if (cam.getQuadrant() == 3) {
				gBackward = 1;
			}
			if (cam.getQuadrant() == 4) {
				gRight = 1;
			}
			break;
		}
		case Keyboard.KEY_S: {
			if (cam.getQuadrant() == 1) {
				gBackward = 1;
			}
			if (cam.getQuadrant() == 2) {
				gRight = 1;
			}
			if (cam.getQuadrant() == 3) {
				gForward = 1;
			}
			if (cam.getQuadrant() == 4) {
				gLeft = 1;
			}
			break;
		}
		case Keyboard.KEY_A: {
			if (cam.getQuadrant() == 1) {
				gLeft = 1;
			}
			if (cam.getQuadrant() == 2) {
				gBackward = 1;
			}
			if (cam.getQuadrant() == 3) {
				gRight = 1;
			}
			if (cam.getQuadrant() == 4) {
				gForward = 1;
			}
			break;
		}
		case Keyboard.KEY_D: {
			if (cam.getQuadrant() == 1) {
				gRight = 1;
			}
			if (cam.getQuadrant() == 2) {
				gForward = 1;
			}
			if (cam.getQuadrant() == 3) {
				gLeft = 1;
			}
			if (cam.getQuadrant() == 4) {
				gBackward = 1;
			}
			break;
		}

		default:
			super.specialKeyboard(key, x, y, modifiers);
			break;
		}
	}
	
	@Override
	public void specialKeyboardUp(int key, int x, int y, int modifiers) {
		switch (key) {
		case Keyboard.KEY_W: {
			if (cam.getQuadrant() == 1) {
				gForward = 0;
			}
			if (cam.getQuadrant() == 2) {
				gLeft = 0;
			}
			if (cam.getQuadrant() == 3) {
				gBackward = 0;
			}
			if (cam.getQuadrant() == 4) {
				gRight = 0;
			}
			break;
		}
		case Keyboard.KEY_S: {
			if (cam.getQuadrant() == 1) {
				gBackward = 0;
			}
			if (cam.getQuadrant() == 2) {
				gRight = 0;
			}
			if (cam.getQuadrant() == 3) {
				gForward = 0;
			}
			if (cam.getQuadrant() == 4) {
				gLeft = 0;
			}
			break;
		}
		case Keyboard.KEY_A: {
			if (cam.getQuadrant() == 1) {
				gLeft = 0;
			}
			if (cam.getQuadrant() == 2) {
				gBackward = 0;
			}
			if (cam.getQuadrant() == 3) {
				gRight = 0;
			}
			if (cam.getQuadrant() == 4) {
				gForward = 0;
			}
			break;
		}
		case Keyboard.KEY_D: {
			if (cam.getQuadrant() == 1) {
				gRight = 0;
			}
			if (cam.getQuadrant() == 2) {
				gForward = 0;
			}
			if (cam.getQuadrant() == 3) {
				gLeft = 0;
			}
			if (cam.getQuadrant() == 4) {
				gBackward = 0;
			}
			break;
		}
		}
	}
	
	
	@Override
	public void updateCamera() {
		
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		//System.out.println(cameraTargetPosition);
		float rele = ele * 0.01745329251994329547f; // rads per deg
		float razi = azi * 0.01745329251994329547f; // rads per deg

		Quat4f rot = new Quat4f();
		QuaternionUtil.setRotation(rot, cameraUp, razi);

		Vector3f eyePos = new Vector3f();
		eyePos.set(0f, 0f, 0f);
		VectorUtil.setCoord(eyePos, forwardAxis, -cameraDistance);

		Vector3f forward = new Vector3f();
		forward.set(eyePos.x, eyePos.y, eyePos.z);
		if (forward.lengthSquared() < BulletGlobals.FLT_EPSILON) {
			forward.set(1f, 0f, 0f);
		}
		Vector3f right = new Vector3f();
		right.cross(cameraUp, forward);
		Quat4f roll = new Quat4f();
		QuaternionUtil.setRotation(roll, right, -rele);

		Matrix3f tmpMat1 = new Matrix3f();
		Matrix3f tmpMat2 = new Matrix3f();
		tmpMat1.set(rot);
		tmpMat2.set(roll);
		tmpMat1.mul(tmpMat2);
		tmpMat1.transform(eyePos);

		cameraPosition.set(eyePos);

		if (glutScreenWidth > glutScreenHeight) {
			float aspect = glutScreenWidth / (float) glutScreenHeight;
			gl.glFrustum(-aspect, aspect, -1.0, 1.0, 1.0, 10000.0);
		}
		else {
			float aspect = glutScreenHeight / (float) glutScreenWidth;
			gl.glFrustum(-1.0, 1.0, -aspect, aspect, 1.0, 10000.0);
		}

		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		//System.out.println("camUP "+cameraUp);
		
    	cam.updatePan(cam.getDirection());
    	ViewPoint = GL_Vector.add(camera.Position, camera.ViewDir);
    	cameraPosition.x = camera.Position.x;
    	cameraPosition.y = camera.Position.y;
    	cameraPosition.z = camera.Position.z;
    	cameraTargetPosition.x = ViewPoint.x;
    	cameraTargetPosition.y = ViewPoint.y;
    	cameraTargetPosition.z = ViewPoint.z;
    	cameraUp.x = camera.UpVector.x;
    	cameraUp.y = camera.UpVector.y;
    	cameraUp.z = camera.UpVector.z;
//    	cameraPosition.x += globalTrans.x;
//    	cameraPosition.y += globalTrans.y;
//    	cameraPosition.z += globalTrans.z;
//		Transform characterWorldTrans = ghostObject.getWorldTransform(new Transform());

//    	cameraTargetPosition.set(characterWorldTrans.origin);
//    	System.out.println(cameraPosition.x + " " + cameraPosition.y + " " + cameraPosition.z + " ... " +
//    						cameraTargetPosition.x + " " + cameraTargetPosition.y + " " + cameraTargetPosition.z + " ... " +
//    						cameraUp.x + " " + cameraUp.y + " " + cameraUp.z);
		gl.gluLookAt(cameraPosition.x, cameraPosition.y, cameraPosition.z,
				cameraTargetPosition.x, cameraTargetPosition.y, cameraTargetPosition.z,
				cameraUp.x, cameraUp.y, cameraUp.z);
	}
	
	@Override
	public void displayCallback() {
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		renderme();

		// optional but useful: debug drawing to detect problems
		if (dynamicsWorld != null) {
			dynamicsWorld.debugDrawWorld();
		}

		//glFlush();
		//glutSwapBuffers();
	}
	
	
	private StringBuilder buf = new StringBuilder();
	private final Transform m = new Transform();
	private Vector3f wireColor = new Vector3f();
	protected Color3f TEXT_COLOR = new Color3f(0f, 0f, 0f);
	

	public void resetScene() {
    	BulletStats.gNumDeepPenetrationChecks = 0;
		BulletStats.gNumGjkChecks = 0;

		int numObjects = 0;
		if (dynamicsWorld != null) {
			dynamicsWorld.stepSimulation(1f / 60f, 0);
			numObjects = dynamicsWorld.getNumCollisionObjects();
		}

		for (int i = 0; i < numObjects; i++) {
			CollisionObject colObj = dynamicsWorld.getCollisionObjectArray().getQuick(i);
			RigidBody body = RigidBody.upcast(colObj);
			if (body != null) {
				if (body.getMotionState() != null) {
					DefaultMotionState myMotionState = (DefaultMotionState) body.getMotionState();
					myMotionState.graphicsWorldTrans.set(myMotionState.startWorldTrans);
					colObj.setWorldTransform(myMotionState.graphicsWorldTrans);
					colObj.setInterpolationWorldTransform(myMotionState.startWorldTrans);
					colObj.activate();
				}
				// removed cached contact points
				dynamicsWorld.getBroadphase().getOverlappingPairCache().cleanProxyFromPairs(colObj.getBroadphaseHandle(), dynamicsWorld.getDispatcher());

				body = RigidBody.upcast(colObj);
				if (body != null && !body.isStaticObject()) {
					RigidBody.upcast(colObj).setLinearVelocity(new Vector3f(0f, 0f, 0f));
					RigidBody.upcast(colObj).setAngularVelocity(new Vector3f(0f, 0f, 0f));
				}
			}
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
	
	
	public static void main(String[] args) throws LWJGLException {
		TwoDotFiveDCameraBullet demo = new TwoDotFiveDCameraBullet(LWJGL.getGL());
		demo.initPhysics();
		demo.setup();
		demo.getDynamicsWorld().setDebugDrawer(new GLDebugDrawer(LWJGL.getGL()));

		LWJGL.main(args, 800, 600, "2.5D camera, movement, and physics demo", demo);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//////////////////////////////////////////REWORK
	public void drawCube(float extent) {
		extent = extent * 0.5f;
        gl.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, cubeTextureHandle);

	    GL11.glBegin(GL11.GL_QUADS);
        GL11.glNormal3f( 1f, 0f, 0f); 
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(+extent,-extent,+extent); 
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(+extent,-extent,-extent); 
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(+extent,+extent,-extent); 
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(+extent,+extent,+extent);
        
        GL11.glNormal3f( 0f, 1f, 0f); 
        GL11.glTexCoord2f(0.0f, 1.0f);GL11.glVertex3f(+extent,+extent,+extent); 
        GL11.glTexCoord2f(1.0f, 1.0f);GL11.glVertex3f(+extent,+extent,-extent); 
        GL11.glTexCoord2f(1.0f, 0.0f);GL11.glVertex3f(-extent,+extent,-extent); 
        GL11.glTexCoord2f(0.0f, 0.0f);GL11.glVertex3f(-extent,+extent,+extent);
        
        GL11.glNormal3f( 0f, 0f, 1f); 
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(+extent,+extent,+extent); 
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-extent,+extent,+extent); 
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-extent,-extent,+extent); 
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(+extent,-extent,+extent);
        
        GL11.glNormal3f(-1f, 0f, 0f); 
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-extent,-extent,+extent); 
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-extent,+extent,+extent); 
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-extent,+extent,-extent); 
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-extent,-extent,-extent);
        
        GL11.glNormal3f( 0f,-1f, 0f); 
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(-extent,-extent,+extent); 
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(-extent,-extent,-extent); 
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(+extent,-extent,-extent); 
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(+extent,-extent,+extent);

        GL11.glNormal3f( 0f, 0f,-1f); 
        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex3f(-extent,-extent,-extent); 
        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex3f(-extent,+extent,-extent); 
        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex3f(+extent,+extent,-extent); 
        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex3f(+extent,-extent,-extent);
		GL11.glEnd();
	}
	
	public static void drawCoordSystem(IGL gl) {
		gl.glBegin(GL_LINES);
		gl.glColor3f(1, 0, 0);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(1, 0, 0);
		gl.glColor3f(0, 1, 0);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(0, 1, 0);
		gl.glColor3f(0, 0, 1);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(0, 0, 1);
		gl.glEnd();
	}

	private static float[] glMat = new float[16];
	
	public void drawOpenGL(IGL gl, Transform trans, CollisionShape shape, Vector3f color, int debugMode, int textureHandle) {
		ObjectPool<Transform> transformsPool = ObjectPool.get(Transform.class);
		ObjectPool<Vector3f> vectorsPool = ObjectPool.get(Vector3f.class);

		//System.out.println("shape="+shape+" type="+BroadphaseNativeTypes.forValue(shape.getShapeType()));

		gl.glPushMatrix();
		trans.getOpenGLMatrix(glMat);
		gl.glMultMatrix(glMat);
//		if (shape.getShapeType() == BroadphaseNativeTypes.UNIFORM_SCALING_SHAPE_PROXYTYPE.getValue())
//		{
//			const btUniformScalingShape* scalingShape = static_cast<const btUniformScalingShape*>(shape);
//			const btConvexShape* convexShape = scalingShape->getChildShape();
//			float	scalingFactor = (float)scalingShape->getUniformScalingFactor();
//			{
//				btScalar tmpScaling[4][4]={{scalingFactor,0,0,0},
//					{0,scalingFactor,0,0},
//					{0,0,scalingFactor,0},
//					{0,0,0,1}};
//
//				drawOpenGL( (btScalar*)tmpScaling,convexShape,color,debugMode);
//			}
//			glPopMatrix();
//			return;
//		}

		if (shape.getShapeType() == BroadphaseNativeType.COMPOUND_SHAPE_PROXYTYPE) {
			CompoundShape compoundShape = (CompoundShape) shape;
			Transform childTrans = transformsPool.get();
			for (int i = compoundShape.getNumChildShapes() - 1; i >= 0; i--) {
				compoundShape.getChildTransform(i, childTrans);
				CollisionShape colShape = compoundShape.getChildShape(i);
				//drawOpenGL(gl, childTrans, colShape, color, debugMode, );
			}
			transformsPool.release(childTrans);
		}
		else {
			//drawCoordSystem();

			//glPushMatrix();

			//gl.glEnable(GL_COLOR_MATERIAL);
			//gl.glColor3f(color.x, color.y, color.z);

			boolean useWireframeFallback = true;

			if ((debugMode & DebugDrawModes.DRAW_WIREFRAME) == 0) {
				// you can comment out any of the specific cases, and use the default
				// the benefit of 'default' is that it approximates the actual collision shape including collision margin
				
				switch (shape.getShapeType()) {
					case BOX_SHAPE_PROXYTYPE: {
						System.out.println(textureHandle);
						BoxShape boxShape = (BoxShape) shape;
						Vector3f halfExtent = boxShape.getHalfExtentsWithMargin(vectorsPool.get());
						gl.glScalef(2f * halfExtent.x, 2f * halfExtent.y, 2f * halfExtent.z);
						drawCube(1f);
						vectorsPool.release(halfExtent);
						useWireframeFallback = false;
						break;
					}
					case SPHERE_SHAPE_PROXYTYPE: {
						SphereShape sphereShape = (SphereShape) shape;
						float radius = sphereShape.getMargin(); // radius doesn't include the margin, so draw with margin
						// TODO: glutSolidSphere(radius,10,10);
						//sphere.draw(radius, 8, 8);
						gl.drawSphere(radius, 10, 10);
						/*
						glPointSize(10f);
						glBegin(GL_POINTS);
						glVertex3f(0f, 0f, 0f);
						glEnd();
						glPointSize(1f);
						*/
						useWireframeFallback = false;
						break;
					}
//				case CONE_SHAPE_PROXYTYPE:
//					{
//						const btConeShape* coneShape = static_cast<const btConeShape*>(shape);
//						int upIndex = coneShape->getConeUpIndex();
//						float radius = coneShape->getRadius();//+coneShape->getMargin();
//						float height = coneShape->getHeight();//+coneShape->getMargin();
//						switch (upIndex)
//						{
//						case 0:
//							glRotatef(90.0, 0.0, 1.0, 0.0);
//							break;
//						case 1:
//							glRotatef(-90.0, 1.0, 0.0, 0.0);
//							break;
//						case 2:
//							break;
//						default:
//							{
//							}
//						};
//
//						glTranslatef(0.0, 0.0, -0.5*height);
//						glutSolidCone(radius,height,10,10);
//						useWireframeFallback = false;
//						break;
//
//					}

					case STATIC_PLANE_PROXYTYPE:
					{
						StaticPlaneShape staticPlaneShape = (StaticPlaneShape)shape;
						float planeConst = staticPlaneShape.getPlaneConstant();
						Vector3f planeNormal = staticPlaneShape.getPlaneNormal(vectorsPool.get());
						Vector3f planeOrigin = vectorsPool.get();
						planeOrigin.scale(planeConst, planeNormal);
						Vector3f vec0 = vectorsPool.get();
						Vector3f vec1 = vectorsPool.get();
						TransformUtil.planeSpace1(planeNormal,vec0,vec1);
						float vecLen = 100f;
						
						Vector3f pt0 = vectorsPool.get();
						pt0.scaleAdd(vecLen, vec0, planeOrigin);

						Vector3f pt1 = vectorsPool.get();
						pt1.scale(vecLen, vec0);
						pt1.sub(planeOrigin, pt1);

						Vector3f pt2 = vectorsPool.get();
						pt2.scaleAdd(vecLen, vec1, planeOrigin);

						Vector3f pt3 = vectorsPool.get();
						pt3.scale(vecLen, vec1);
						pt3.sub(planeOrigin, pt3);
						
						gl.glBegin(gl.GL_LINES);
						gl.glVertex3f(pt0.x,pt0.y,pt0.z);
						gl.glVertex3f(pt1.x,pt1.y,pt1.z);
						gl.glVertex3f(pt2.x,pt2.y,pt2.z);
						gl.glVertex3f(pt3.x,pt3.y,pt3.z);
						gl.glEnd();
						
						vectorsPool.release(planeNormal);
						vectorsPool.release(planeOrigin);
						vectorsPool.release(vec0);
						vectorsPool.release(vec1);
						vectorsPool.release(pt0);
						vectorsPool.release(pt1);
						vectorsPool.release(pt2);
						vectorsPool.release(pt3);
						
						break;
					}
					
				case CYLINDER_SHAPE_PROXYTYPE:
					{
						CylinderShape cylinder = (CylinderShape) shape;
						int upAxis = cylinder.getUpAxis();

						float radius = cylinder.getRadius();
						Vector3f halfVec = vectorsPool.get();
						float halfHeight = VectorUtil.getCoord(cylinder.getHalfExtentsWithMargin(halfVec), upAxis);

						gl.drawCylinder(radius, halfHeight, upAxis);
						
						vectorsPool.release(halfVec);

						break;
					}
					default: {
						if (shape.isConvex())
						{
							ConvexShape convexShape = (ConvexShape)shape;
							if (shape.getUserPointer() == null)
							{
								// create a hull approximation
								ShapeHull hull = new ShapeHull(convexShape);

								// JAVA NOTE: not needed
								///// cleanup memory
								//m_shapeHulls.push_back(hull);

								float margin = shape.getMargin();
								hull.buildHull(margin);
								convexShape.setUserPointer(hull);

								//printf("numTriangles = %d\n", hull->numTriangles ());
								//printf("numIndices = %d\n", hull->numIndices ());
								//printf("numVertices = %d\n", hull->numVertices ());
							}

							if (shape.getUserPointer() != null)
							{
								//glutSolidCube(1.0);
								ShapeHull hull = (ShapeHull)shape.getUserPointer();
								
								Vector3f normal = vectorsPool.get();
								Vector3f tmp1 = vectorsPool.get();
								Vector3f tmp2 = vectorsPool.get();

								if (hull.numTriangles () > 0)
								{
									int index = 0;
									IntArrayList idx = hull.getIndexPointer();
									ObjectArrayList<Vector3f> vtx = hull.getVertexPointer();

									gl.glBegin (gl.GL_TRIANGLES);

									for (int i=0; i<hull.numTriangles (); i++)
									{
										int i1 = index++;
										int i2 = index++;
										int i3 = index++;
										assert(i1 < hull.numIndices () &&
											i2 < hull.numIndices () &&
											i3 < hull.numIndices ());

										int index1 = idx.get(i1);
										int index2 = idx.get(i2);
										int index3 = idx.get(i3);
										assert(index1 < hull.numVertices () &&
											index2 < hull.numVertices () &&
											index3 < hull.numVertices ());

										Vector3f v1 = vtx.getQuick(index1);
										Vector3f v2 = vtx.getQuick(index2);
										Vector3f v3 = vtx.getQuick(index3);
										tmp1.sub(v3, v1);
										tmp2.sub(v2, v1);
										normal.cross(tmp1, tmp2);
										normal.normalize();

										gl.glNormal3f(normal.x,normal.y,normal.z);
										gl.glVertex3f (v1.x, v1.y, v1.z);
										gl.glVertex3f (v2.x, v2.y, v2.z);
										gl.glVertex3f (v3.x, v3.y, v3.z);

									}
									gl.glEnd ();
								}
								
								vectorsPool.release(normal);
								vectorsPool.release(tmp1);
								vectorsPool.release(tmp2);
							}
						} else
						{
	//						printf("unhandled drawing\n");
						}						

					}

				}

			}

			if (useWireframeFallback) {
				// for polyhedral shapes
				if (shape.isPolyhedral()) {
					PolyhedralConvexShape polyshape = (PolyhedralConvexShape) shape;

					gl.glBegin(GL_LINES);

					Vector3f a = vectorsPool.get(), b = vectorsPool.get();
					int i;
					for (i = 0; i < polyshape.getNumEdges(); i++) {
						polyshape.getEdge(i, a, b);

						gl.glVertex3f(a.x, a.y, a.z);
						gl.glVertex3f(b.x, b.y, b.z);
					}
					gl.glEnd();
					
					vectorsPool.release(a);
					vectorsPool.release(b);

//					if (debugMode==btIDebugDraw::DBG_DrawFeaturesText)
//					{
//						glRasterPos3f(0.0,  0.0,  0.0);
//						//BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),polyshape->getExtraDebugInfo());
//
//						glColor3f(1.f, 1.f, 1.f);
//						for (i=0;i<polyshape->getNumVertices();i++)
//						{
//							btPoint3 vtx;
//							polyshape->getVertex(i,vtx);
//							glRasterPos3f(vtx.x(),  vtx.y(),  vtx.z());
//							char buf[12];
//							sprintf(buf," %d",i);
//							BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
//						}
//
//						for (i=0;i<polyshape->getNumPlanes();i++)
//						{
//							btVector3 normal;
//							btPoint3 vtx;
//							polyshape->getPlane(normal,vtx,i);
//							btScalar d = vtx.dot(normal);
//
//							glRasterPos3f(normal.x()*d,  normal.y()*d, normal.z()*d);
//							char buf[12];
//							sprintf(buf," plane %d",i);
//							BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
//
//						}
//					}


				}
			}

//		#ifdef USE_DISPLAY_LISTS
//
//		if (shape->getShapeType() == TRIANGLE_MESH_SHAPE_PROXYTYPE||shape->getShapeType() == GIMPACT_SHAPE_PROXYTYPE)
//			{
//				GLuint dlist =   OGL_get_displaylist_for_shape((btCollisionShape * )shape);
//				if (dlist)
//				{
//					glCallList(dlist);
//				}
//				else
//				{
//		#else		
			if (shape.isConcave())//>getShapeType() == TRIANGLE_MESH_SHAPE_PROXYTYPE||shape->getShapeType() == GIMPACT_SHAPE_PROXYTYPE)
			//		if (shape->getShapeType() == TRIANGLE_MESH_SHAPE_PROXYTYPE)
			{
				ConcaveShape concaveMesh = (ConcaveShape) shape;
				//btVector3 aabbMax(btScalar(1e30),btScalar(1e30),btScalar(1e30));
				//btVector3 aabbMax(100,100,100);//btScalar(1e30),btScalar(1e30),btScalar(1e30));

				//todo pass camera, for some culling
				Vector3f aabbMax = vectorsPool.get();
				aabbMax.set(1e30f, 1e30f, 1e30f);
				Vector3f aabbMin = vectorsPool.get();
				aabbMin.set(-1e30f, -1e30f, -1e30f);

				GlDrawcallback drawCallback = new GlDrawcallback(gl);
				drawCallback.wireframe = (debugMode & DebugDrawModes.DRAW_WIREFRAME) != 0;

				concaveMesh.processAllTriangles(drawCallback, aabbMin, aabbMax);
				
				vectorsPool.release(aabbMax);
				vectorsPool.release(aabbMin);
			}
			//#endif

			//#ifdef USE_DISPLAY_LISTS
			//		}
			//	}
			//#endif

//			if (shape->getShapeType() == CONVEX_TRIANGLEMESH_SHAPE_PROXYTYPE)
//			{
//				btConvexTriangleMeshShape* convexMesh = (btConvexTriangleMeshShape*) shape;
//
//				//todo: pass camera for some culling			
//				btVector3 aabbMax(btScalar(1e30),btScalar(1e30),btScalar(1e30));
//				btVector3 aabbMin(-btScalar(1e30),-btScalar(1e30),-btScalar(1e30));
//				TriangleGlDrawcallback drawCallback;
//				convexMesh->getMeshInterface()->InternalProcessAllTriangles(&drawCallback,aabbMin,aabbMax);
//
//			}

			// TODO: error in original sources GL_DEPTH_BUFFER_BIT instead of GL_DEPTH_TEST
			//gl.glDisable(GL_DEPTH_TEST);
			//glRasterPos3f(0, 0, 0);//mvtx.x(),  vtx.y(),  vtx.z());
			if ((debugMode & DebugDrawModes.DRAW_TEXT) != 0) {
				// TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),shape->getName());
			}

			if ((debugMode & DebugDrawModes.DRAW_FEATURES_TEXT) != 0) {
				//BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),shape->getExtraDebugInfo());
			}
			//gl.glEnable(GL_DEPTH_TEST);

			//glPopMatrix();
		}
		gl.glPopMatrix();
	}
	
	////////////////////////////////////////////////////////////////////////////
	
	private static class TriMeshKey {
		public CollisionShape shape;
		public int dlist; // OpenGL display list	
	}
	
	private static class GlDisplaylistDrawcallback extends TriangleCallback {
		private IGL gl;
		
		private final Vector3f diff1 = new Vector3f();
		private final Vector3f diff2 = new Vector3f();
		private final Vector3f normal = new Vector3f();

		public GlDisplaylistDrawcallback(IGL gl) {
			this.gl = gl;
		}
		
		public void processTriangle(Vector3f[] triangle, int partId, int triangleIndex) {
			diff1.sub(triangle[1], triangle[0]);
			diff2.sub(triangle[2], triangle[0]);
			normal.cross(diff1, diff2);

			normal.normalize();

			gl.glBegin(GL_TRIANGLES);
			gl.glColor3f(0, 1, 0);
			gl.glNormal3f(normal.x, normal.y, normal.z);
			gl.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);

			gl.glColor3f(0, 1, 0);
			gl.glNormal3f(normal.x, normal.y, normal.z);
			gl.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);

			gl.glColor3f(0, 1, 0);
			gl.glNormal3f(normal.x, normal.y, normal.z);
			gl.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
			gl.glEnd();

			/*glBegin(GL_LINES);
			glColor3f(1, 1, 0);
			glNormal3d(normal.getX(),normal.getY(),normal.getZ());
			glVertex3d(triangle[0].getX(), triangle[0].getY(), triangle[0].getZ());
			glNormal3d(normal.getX(),normal.getY(),normal.getZ());
			glVertex3d(triangle[1].getX(), triangle[1].getY(), triangle[1].getZ());
			glColor3f(1, 1, 0);
			glNormal3d(normal.getX(),normal.getY(),normal.getZ());
			glVertex3d(triangle[2].getX(), triangle[2].getY(), triangle[2].getZ());
			glNormal3d(normal.getX(),normal.getY(),normal.getZ());
			glVertex3d(triangle[1].getX(), triangle[1].getY(), triangle[1].getZ());
			glColor3f(1, 1, 0);
			glNormal3d(normal.getX(),normal.getY(),normal.getZ());
			glVertex3d(triangle[2].getX(), triangle[2].getY(), triangle[2].getZ());
			glNormal3d(normal.getX(),normal.getY(),normal.getZ());
			glVertex3d(triangle[0].getX(), triangle[0].getY(), triangle[0].getZ());
			glEnd();*/
		}
	}
	
	private static class GlDrawcallback extends TriangleCallback {
		private IGL gl;
		public boolean wireframe = false;

		public GlDrawcallback(IGL gl) {
			this.gl = gl;
		}
		
		public void processTriangle(Vector3f[] triangle, int partId, int triangleIndex) {
			if (wireframe) {
				gl.glBegin(GL_LINES);
				gl.glColor3f(1, 0, 0);
				gl.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);
				gl.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);
				gl.glColor3f(0, 1, 0);
				gl.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
				gl.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);
				gl.glColor3f(0, 0, 1);
				gl.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
				gl.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);
				gl.glEnd();
			}
			else {
				gl.glBegin(GL_TRIANGLES);
				gl.glColor3f(1, 0, 0);
				gl.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);
				gl.glColor3f(0, 1, 0);
				gl.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);
				gl.glColor3f(0, 0, 1);
				gl.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
				gl.glEnd();
			}
		}
	}
	
	private static class TriangleGlDrawcallback extends InternalTriangleIndexCallback {
		private IGL gl;

		public TriangleGlDrawcallback(IGL gl) {
			this.gl = gl;
		}
		
		public void internalProcessTriangleIndex(Vector3f[] triangle, int partId, int triangleIndex) {
			gl.glBegin(GL_TRIANGLES);//LINES);
			gl.glColor3f(1, 0, 0);
			gl.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);
			gl.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);
			gl.glColor3f(0, 1, 0);
			gl.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
			gl.glVertex3f(triangle[1].x, triangle[1].y, triangle[1].z);
			gl.glColor3f(0, 0, 1);
			gl.glVertex3f(triangle[2].x, triangle[2].y, triangle[2].z);
			gl.glVertex3f(triangle[0].x, triangle[0].y, triangle[0].z);
			gl.glEnd();
		}
	}

}
