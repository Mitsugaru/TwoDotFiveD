/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http://www.bulletphysics.com/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose, 
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package com.bulletphysics.demos.character;

import java.io.IOException;
import java.util.logging.Level;

import com.ATeam.twoDotFiveD.debug.Logging;
import com.bulletphysics.BulletGlobals;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.demos.bsp.BspConverter;
import com.bulletphysics.demos.bsp.BspYamlConverter;
import com.bulletphysics.demos.opengl.DemoApplication;
import com.bulletphysics.demos.opengl.GLDebugDrawer;
import com.bulletphysics.demos.opengl.IGL;
import com.bulletphysics.demos.opengl.LWJGL;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.character.KinematicCharacterController;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.VectorUtil;

import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import org.lwjgl.input.Keyboard;
import static com.bulletphysics.demos.opengl.IGL.*;

/**
 * 
 * @author tomrbryn
 */
public class CharacterDemo extends DemoApplication {

	private final int maxProxies = 32766;
	private final int maxOverlap = 65535;
	private static int gForward = 0;
	private static int gBackward = 0;
	private static int gLeft = 0;
	private static int gRight = 0;
	private static int gJump = 0;

	public KinematicCharacterController character;
	public PairCachingGhostObject ghostObject;

	public float cameraHeight = 4f;

	public float minCameraDistance = 3f;
	public float maxCameraDistance = 10f;

	// JAVA NOTE: the original demo scaled the bsp room, we scale up the
	// character
	private float characterScale = 2f;

	// keep the collision shapes, for deletion/cleanup
	public ObjectArrayList<CollisionShape> collisionShapes = new ObjectArrayList<CollisionShape>();
	public BroadphaseInterface overlappingPairCache;
	public CollisionDispatcher dispatcher;
	public ConstraintSolver constraintSolver;
	public DefaultCollisionConfiguration collisionConfiguration;

	public CharacterDemo(IGL gl) {
		super(gl);
	}

	public void initPhysics() throws Exception {
		CollisionShape groundShape = new BoxShape(new Vector3f(50, 3, 50));
		collisionShapes.add(groundShape);
		forwardAxis = 1;

		collisionConfiguration = new DefaultCollisionConfiguration();
		dispatcher = new CollisionDispatcher(collisionConfiguration);
		Vector3f worldMin = new Vector3f(-1000f, -1000f, -1000f);
		Vector3f worldMax = new Vector3f(1000f, 1000f, 1000f);
		AxisSweep3 sweepBP = new AxisSweep3(worldMin, worldMax);
		overlappingPairCache = sweepBP;

		constraintSolver = new SequentialImpulseConstraintSolver();
		dynamicsWorld = new DiscreteDynamicsWorld(dispatcher,
				overlappingPairCache, constraintSolver, collisionConfiguration);

		Transform startTransform = new Transform();
		startTransform.setIdentity();
		startTransform.origin.set(0.0f, 4.0f, 0.0f);

		ghostObject = new PairCachingGhostObject();
		ghostObject.setWorldTransform(startTransform);
		sweepBP.getOverlappingPairCache().setInternalGhostPairCallback(
				new GhostPairCallback());
		float characterHeight = 1.75f * characterScale;
		float characterWidth = 1.75f * characterScale;
		ConvexShape capsule = new CapsuleShape(characterWidth, characterHeight);
		ghostObject.setCollisionShape(capsule);
		ghostObject.setCollisionFlags(CollisionFlags.CHARACTER_OBJECT);

		float stepHeight = 0.35f * characterScale;
		character = new KinematicCharacterController(ghostObject, capsule,
				stepHeight);
		/*
		 * 0: pos x
		 * 1: down y
		 * 2: neg z
		 * character.setUpAxis();
		 */

		//new BspToBulletConverter().convertBsp(getClass().getResourceAsStream("/com/bulletphysics/demos/bsp/exported.bsp.txt"));
		try
        {
            new BspYamlToBulletConverter().convertBspYaml(getClass().getResourceAsStream("scene.yml"));
        }
        catch(IOException e)
        {
            Logging.log.log(Level.SEVERE, "Could not close InputStream for: scene.yml", e);
        }
		dynamicsWorld.addCollisionObject(ghostObject, CollisionFilterGroups.CHARACTER_FILTER, (short)(CollisionFilterGroups.STATIC_FILTER | CollisionFilterGroups.DEFAULT_FILTER));

		dynamicsWorld.addAction(character);

		clientResetScene();

		setCameraDistance(56f);
	}

	@Override
	public void clientMoveAndDisplay() {
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		float dt = getDeltaTimeMicroseconds() * 0.000001f;

		if (dynamicsWorld != null) {
			// during idle mode, just run 1 simulation step maximum
			int maxSimSubSteps = idle ? 1 : 2;
			if (idle) {
				dt = 1.0f / 420.f;
			}

			// set walkDirection for our character
			Transform xform = ghostObject.getWorldTransform(new Transform());

			Vector3f forwardDir = new Vector3f();
			xform.basis.getRow(2, forwardDir);
			// printf("forwardDir=%f,%f,%f\n",forwardDir[0],forwardDir[1],forwardDir[2]);
			Vector3f upDir = new Vector3f();
			xform.basis.getRow(1, upDir);
			Vector3f strafeDir = new Vector3f();
			xform.basis.getRow(0, strafeDir);
			forwardDir.normalize();
			upDir.normalize();
			strafeDir.normalize();

			Vector3f walkDirection = new Vector3f(0.0f, 0.0f, 0.0f);
			float walkVelocity = 1.1f * 150.0f; // 4 km/h -> 1.1 m/s
			float walkSpeed = walkVelocity * dt * characterScale;

			if (gLeft != 0) {
				walkDirection.add(strafeDir);
			}

			if (gRight != 0) {
				walkDirection.sub(strafeDir);
			}

			if (gForward != 0) {
				walkDirection.add(forwardDir);
			}

			if (gBackward != 0) {
				walkDirection.sub(forwardDir);
			}

			walkDirection.scale(walkSpeed);
			character.setWalkDirection(walkDirection);

			int numSimSteps = dynamicsWorld.stepSimulation(dt, maxSimSubSteps);

			// optional but useful: debug drawing
			dynamicsWorld.debugDrawWorld();
		}

		renderme();

		// glFlush();
		// glutSwapBuffers();
	}

	@Override
	public void displayCallback() {
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		renderme();

		if (dynamicsWorld != null) {
			dynamicsWorld.debugDrawWorld();
		}

		// glFlush();
		// glutSwapBuffers();
	}

	@Override
	public void clientResetScene() {
		dynamicsWorld
				.getBroadphase()
				.getOverlappingPairCache()
				.cleanProxyFromPairs(ghostObject.getBroadphaseHandle(),
						getDynamicsWorld().getDispatcher());

		character.reset();
		// /WTF
		character.warp(new Vector3f(0, -2, 0));
	}

	@Override
	public void specialKeyboardUp(int key, int x, int y, int modifiers) {
		switch (key) {
		case Keyboard.KEY_W: {
			gForward = 0;
			break;
		}
		case Keyboard.KEY_S: {
			gBackward = 0;
			break;
		}
		case Keyboard.KEY_A: {
			gLeft = 0;
			break;
		}
		case Keyboard.KEY_D: {
			gRight = 0;
			break;
		}
		default:
			super.specialKeyboardUp(key, x, y, modifiers);
			break;
		}
	}

	@Override
	public void specialKeyboard(int key, int x, int y, int modifiers) {
		switch (key) {
		case Keyboard.KEY_W: {
			gForward = 1;
			break;
		}
		case Keyboard.KEY_S: {
			gBackward = 1;
			break;
		}
		case Keyboard.KEY_A: {
			gLeft = 1;
			break;
		}
		case Keyboard.KEY_D: {
			gRight = 1;
			break;
		}
		case Keyboard.KEY_F1: {
			if (character != null && character.canJump()) {
				gJump = 1;
			}
			break;
		}
		case 'l': {
			character.jump();
		}
		default:
			super.specialKeyboard(key, x, y, modifiers);
			break;
		}
	}

	@Override
	public void updateCamera() {
		// if (useDefaultCamera) {
		if (false) {
			super.updateCamera();
			return;
		}

		gl.glMatrixMode(gl.GL_PROJECTION);
		gl.glLoadIdentity();
		// look at the vehicle
		Transform characterWorldTrans = ghostObject
				.getWorldTransform(new Transform());
		Vector3f up = new Vector3f();
		characterWorldTrans.basis.getRow(1, up);
		Vector3f backward = new Vector3f();
		characterWorldTrans.basis.getRow(2, backward);
		backward.scale(-1);
		up.normalize();
		backward.normalize();

		cameraTargetPosition.set(characterWorldTrans.origin);

		Vector3f cameraPosition = new Vector3f();
		cameraPosition.scale(2, up);
		cameraPosition.add(cameraTargetPosition);
		backward.scale(12);
		cameraPosition.add(backward);
		
		//rotation
		float rele = ele * 0.01745329251994329547f; // rads per deg
		float razi = azi * 0.01745329251994329547f; // rads per deg
		Quat4f rot = new Quat4f();
		QuaternionUtil.setRotation(rot, cameraUp, razi);
		Vector3f right = new Vector3f();
		Vector3f eyePos = new Vector3f();
		//eyePos.set(0f, 0f, 0f);
		VectorUtil.setCoord(eyePos, forwardAxis, -cameraDistance);
		Vector3f forward = new Vector3f();
		forward.set(eyePos.x, eyePos.y, eyePos.z);
		if (forward.lengthSquared() < BulletGlobals.FLT_EPSILON) {
			forward.set(1f, 0f, 0f);
		}

		right.cross(cameraUp, forward);
		Quat4f roll = new Quat4f();
		QuaternionUtil.setRotation(roll, right, -rele);

		Matrix3f tmpMat1 = new Matrix3f();
		Matrix3f tmpMat2 = new Matrix3f();
		tmpMat1.set(rot);
		tmpMat2.set(roll);
		tmpMat1.mul(tmpMat2);
		tmpMat1.transform(eyePos);
		//cameraPosition.set(eyePos);

		// update OpenGL camera settings
		gl.glFrustum(-1.0, 1.0, -1.0, 1.0, 1.0, 10000.0);

		gl.glMatrixMode(IGL.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.gluLookAt(cameraPosition.x, cameraPosition.y, cameraPosition.z,
				cameraTargetPosition.x, cameraTargetPosition.y,
				cameraTargetPosition.z, cameraUp.x, cameraUp.y, cameraUp.z);
	}

	public static void main(String[] args) throws Exception {
		CharacterDemo demo = new CharacterDemo(LWJGL.getGL());
		demo.initPhysics();
		demo.getDynamicsWorld()
				.setDebugDrawer(new GLDebugDrawer(LWJGL.getGL()));

		LWJGL.main(args, 800, 600,
				"Bullet Character Demo. http://bullet.sf.net", demo);
	}

	// //////////////////////////////////////////////////////////////////////////

	private class BspToBulletConverter extends BspConverter {
		@Override
		public void addConvexVerticesCollider(ObjectArrayList<Vector3f> vertices) {
			if (vertices.size() > 0) {
				float mass = 0f;
				Transform startTransform = new Transform();
				// can use a shift
				startTransform.setIdentity();
				// JAVA NOTE: port change, we want y to be up.
				startTransform.basis.rotX((float) -Math.PI / 2f);
				startTransform.origin.set(0, -10, 0);
				// startTransform.origin.set(0, 0, -10f);

				// this create an internal copy of the vertices
				CollisionShape shape = new ConvexHullShape(vertices);
				collisionShapes.add(shape);

				// btRigidBody* body = m_demoApp->localCreateRigidBody(mass,
				// startTransform,shape);
				localCreateRigidBody(mass, startTransform, shape);
			}
		}
	}
	
	private class BspYamlToBulletConverter extends BspYamlConverter
	{

		@Override
		public void addConvexVerticesCollider(String name, ObjectArrayList<Vector3f> vertices, float mass, Vector3f acceleration)
		{
			Transform startTransform = new Transform();
			// can use a shift
			startTransform.setIdentity();
			startTransform.origin.set(0, 0, -10f);
			
			// this create an internal copy of the vertices
			CollisionShape shape = new ConvexHullShape(vertices);
			collisionShapes.add(shape);
			
			// btRigidBody* body = m_demoApp->localCreateRigidBody(mass,
			// startTransform,shape);
			RigidBody body = localCreateRigidBody(mass, startTransform, shape);
			if(acceleration != null)
			{
				body.setGravity(acceleration);
			}
		}
		
	}
}
