package com.ATeam.twoDotFiveD;

import static com.bulletphysics.demos.opengl.IGL.GL_COLOR_BUFFER_BIT;
import static com.bulletphysics.demos.opengl.IGL.GL_DEPTH_BUFFER_BIT;
import static com.bulletphysics.demos.opengl.IGL.GL_LIGHTING;
import static com.bulletphysics.demos.opengl.IGL.GL_MODELVIEW;
import static com.bulletphysics.demos.opengl.IGL.GL_PROJECTION;

import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import lib.lwjgl.glmodel.GL_Vector;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;

import com.bulletphysics.BulletGlobals;
import com.bulletphysics.BulletStats;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
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
import com.bulletphysics.collision.shapes.ConvexShape;
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
import com.bulletphysics.linearmath.VectorUtil;
import com.bulletphysics.util.ObjectArrayList;

import demo.lwjgl.basic.GLApp;
import demo.lwjgl.basic.GLCam;
import demo.lwjgl.basic.GLCamera;

public class TwoDotFiveDCameraBullet extends DemoApplication{
	
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
    int groundTextureHandle = 0;


	public TwoDotFiveDCameraBullet(IGL gl) {
		super(gl);
	}

	public void setup() {
      cam.setCamera(camera);
      camera.setCamera(0f,0f,15f,  0f,0f,-1f, 0f,1f,0f);

	}
	
	public void initPhysics(){
        
		CollisionShape groundShape = new BoxShape(new Vector3f(10f, 0f, 10f));
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
		startTransform.origin.set(0.0f, 4.0f, 0.0f);
		
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


			startObj.origin.set(0,0,0);

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

			CollisionShape colShape = new BoxShape(new Vector3f(4, 1.5f, 0.3f));
			//CollisionShape colShape = new SphereShape(1f);
			collisionShapes.add(colShape);

			// Create Dynamic Objects
			Transform startObj = new Transform();
			startObj.setIdentity();

			//-mass  = phasing
			float mass = 0f;

			// rigidbody is dynamic if and only if mass is non zero, otherwise static
			boolean isDynamic = (mass != 0f);

			Vector3f localInertia = new Vector3f(0, -1, 0);
			if (isDynamic) {
				colShape.calculateLocalInertia(mass, localInertia);
			}


			startObj.origin.set(-6,0.3f,-8);

			// using motionstate is recommended, it provides interpolation capabilities, and only synchronizes 'active' objects
			DefaultMotionState myMotionState = new DefaultMotionState(startObj);
			RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, myMotionState, colShape, localInertia);
			RigidBody body = new RigidBody(rbInfo);
			body.setActivationState(RigidBody.ISLAND_SLEEPING);

			dynamicsWorld.addRigidBody(body);
			body.setActivationState(RigidBody.ISLAND_SLEEPING);
		}
		
		resetScene();		
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
			
			System.out.println(walkDirection);			
			System.out.println(globalTrans);
			
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

				GLShapeDrawer.drawOpenGL(gl, m, colObj.getCollisionShape(), wireColor, getDebugMode());
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
    	System.out.println(cameraPosition.x + " " + cameraPosition.y + " " + cameraPosition.z + " ... " +
    						cameraTargetPosition.x + " " + cameraTargetPosition.y + " " + cameraTargetPosition.z + " ... " +
    						cameraUp.x + " " + cameraUp.y + " " + cameraUp.z);
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

}
