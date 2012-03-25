/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 * 
 * Bullet Continuous Collision Detection and Physics Library Copyright (c)
 * 2003-2008 Erwin Coumans http://www.bulletphysics.com/
 * 
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not claim
 * that you wrote the original software. If you use this software in a product,
 * an acknowledgment in the product documentation would be appreciated but is
 * not required. 2. Altered source versions must be plainly marked as such, and
 * must not be misrepresented as being the original software. 3. This notice may
 * not be removed or altered from any source distribution.
 */

package com.bulletphysics.demos.bsp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import com.ATeam.twoDotFiveD.chatclient.chatClient;
import com.ATeam.twoDotFiveD.debug.Logging;
import com.ATeam.twoDotFiveD.entity.Entity;
import com.ATeam.twoDotFiveD.event.Event;
import com.ATeam.twoDotFiveD.event.Event.Type;
import com.ATeam.twoDotFiveD.event.EventDispatcher;
import com.ATeam.twoDotFiveD.event.block.BlockCollisionEvent;
import com.ATeam.twoDotFiveD.event.block.BlockCollisionResolvedEvent;
import com.ATeam.twoDotFiveD.event.block.BlockCreateEvent;
import com.ATeam.twoDotFiveD.event.block.BlockDestroyedEvent;
import com.ATeam.twoDotFiveD.event.block.BlockListener;
import com.ATeam.twoDotFiveD.event.block.BlockPhysicsChangeEvent;
import com.ATeam.twoDotFiveD.music.MusicPlayer;
import com.ATeam.twoDotFiveD.udp.Client.EventPackage;
import com.bulletphysics.BulletGlobals;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.SimpleBroadphase;
import com.bulletphysics.collision.dispatch.CollisionAlgorithmCreateFunc;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConeShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.demos.opengl.DemoApplication;
import com.bulletphysics.demos.opengl.GLDebugDrawer;
import com.bulletphysics.demos.opengl.IGL;
import com.bulletphysics.demos.opengl.LWJGL;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;

import static com.bulletphysics.demos.opengl.IGL.*;

/**
 * BspDemo shows the convex collision detection, by converting a Quake BSP file
 * into convex objects and allowing interaction with boxes.
 * 
 * @author jezek2
 */
public class BspDemo extends DemoApplication
{
	private static BspDemo					demo;
	private static final float				CUBE_HALF_EXTENTS	= 1;
	private static final float				EXTRA_HEIGHT		= -20f;
	
	// keep the collision shapes, for deletion/cleanup
	public Map<RigidBody, Entity>			entityList			= new HashMap<RigidBody, Entity>();
	public BroadphaseInterface				broadphase;
	public CollisionDispatcher				dispatcher;
	public ConstraintSolver					solver;
	public DefaultCollisionConfiguration	collisionConfiguration;
	private static EventDispatcher			eventDispatcher		= new EventDispatcher();
	private static EventDispatcher			remoteDispatcher	= new EventDispatcher();
	private static chatClient				client;
	int										count				= 0;
	
	public BspDemo(IGL gl)
	{
		super(gl);
	}
	
	public void initPhysics() throws Exception
	{
		// cameraUp.set(0f, 0f, 1f);
		// forwardAxis = 1;
		
		setCameraDistance(22f);
		// Setup a Physics Simulation Environment
		
		collisionConfiguration = new DefaultCollisionConfiguration();
		// btCollisionShape* groundShape = new btBoxShape(btVector3(50,3,50));
		dispatcher = new CollisionStuff(collisionConfiguration);
		Vector3f worldMin = new Vector3f(-1000f, -1000f, -1000f);
		Vector3f worldMax = new Vector3f(1000f, 1000f, 1000f);
		// broadphase = new AxisSweep3(worldMin, worldMax);
		// broadphase = new SimpleBroadphase();
		broadphase = new DbvtBroadphase();
		// btOverlappingPairCache* broadphase = new btSimpleBroadphase();
		solver = new SequentialImpulseConstraintSolver();
		// ConstraintSolver* solver = new OdeConstraintSolver;
		dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase,
				solver, collisionConfiguration);
		
		Vector3f gravity = new Vector3f();
		gravity.negate(cameraUp);
		gravity.scale(10f);
		dynamicsWorld.setGravity(gravity);
		// new BspToBulletConverter().convertBsp(getClass().getResourceAsStream(
		// "exported.bsp.txt"));
		// populate();
		BulletGlobals.setDeactivationTime(0.1f);
		clientResetScene();
		
	}
	
	public void populate()
	{
		try
		{
			new BspYamlToBulletConverter().convertBspYaml(getClass()
					.getResourceAsStream("scene.yml"));
		}
		catch (IOException e)
		{
			Logging.log.log(Level.SEVERE,
					"Could not close InputStream for: scene.yml", e);
		}
	}
	
	@Override
	public void clientMoveAndDisplay()
	{
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		float dt = getDeltaTimeMicroseconds() * 0.000001f;
		
		dynamicsWorld.stepSimulation(dt);
		
		// optional but useful: debug drawing
		dynamicsWorld.debugDrawWorld();
		renderme();
		
		// glFlush();
		// glutSwapBuffers();
		
	}
	
	@Override
	public void displayCallback()
	{
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		renderme();
		
		// glFlush();
		// glutSwapBuffers();
	}
	
	@Override
	public void specialKeyboard(int key, int x, int y, int modifiers)
	{
		switch (key)
		{
			case Keyboard.KEY_R:
			{
				// Remove all objects
				for (CollisionObject a : dynamicsWorld
						.getCollisionObjectArray().toArray(
								new CollisionObject[0]))
				{
					dynamicsWorld.removeCollisionObject(a);
					entityList.remove(a);
				}
				// repopulate world
				populate();
				break;
			}
			default:
			{
				super.specialKeyboard(key, x, y, modifiers);
				break;
			}
		}
	}
	
	@Override
	public void shootBox(Vector3f destination)
	{
		if (dynamicsWorld != null)
		{
			float mass = 50f;
			Transform startTransform = new Transform();
			startTransform.setIdentity();
			Vector3f camPos = new Vector3f(getCameraPosition());
			startTransform.origin.set(camPos);
			
			if (shapeType.equals("BOX"))
			{
				shootBoxShape = new BoxShape(new Vector3f(1f, 1f, 1f));
			}
			else if (shapeType.equals("SPHERE"))
			{
				shootBoxShape = new SphereShape(1f);
			}
			else if (shapeType.equals("TRIANGLE"))
			{
				// TODO implement a pyramid
				shootBoxShape = new ConeShape(1f, 3f);
				// shootBoxShape = new TriangleShape(new Vector3f(1f, 1f, 1f),
				// new Vector3f(1f, 0f, 0f), new Vector3f(0f, -1f, 0f));
			}
			else if (shapeType.equals("CYLINDER"))
			{
				shootBoxShape = new CylinderShape(new Vector3f(1f, 1f, 1f));
			}
			
			RigidBody body = this.localCreateRigidBody(mass, startTransform,
					shootBoxShape);
			
			Vector3f linVel = new Vector3f(destination.x - camPos.x,
					destination.y - camPos.y, destination.z - camPos.z);
			linVel.normalize();
			linVel.scale(ShootBoxInitialSpeed);
			Transform worldTrans = body.getWorldTransform(new Transform());
			worldTrans.origin.set(camPos);
			worldTrans.setRotation(new Quat4f(0f, 0f, 0f, 1f));
			body.setWorldTransform(worldTrans);
			
			body.setLinearVelocity(linVel);
			body.setAngularVelocity(new Vector3f(0f, 0f, 0f));
			
			body.setCcdMotionThreshold(1f);
			body.setCcdSweptSphereRadius(0.2f);
			Entity entity = new Entity(body.getCollisionShape().getName(), body);
			// Dynamic gravity for object
			// TODO consolidate setgravity into one method, let entity set its
			// tied rigidbody gravity
			if (!bodyGravityType.equals("NORMAL"))
			{
				if (bodyGravityType.equals("ANTIGRAVITY"))
				{
					entity.getRigidBody().setGravity(new Vector3f(0f, 30f, 0f));
					entity.setGravity(new Vector3f(0f, 30f, 0f));
				}
				else if (bodyGravityType.equals("STASIS"))
				{
					entity.getRigidBody().setGravity(new Vector3f(0f, 0f, 0f));
					entity.setGravity(new Vector3f(0f, 0f, 0f));
				}
			}
			else
			{
				entity.getRigidBody().setGravity(
						dynamicsWorld.getGravity(new Vector3f()));
				entity.setGravity(dynamicsWorld.getGravity(new Vector3f()));
			}
			entityList.put(body, entity);
			eventDispatcher.notify(new BlockCreateEvent(entity));
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		demo = new BspDemo(LWJGL.getGL());
		try
		{
			client = new chatClient(null, "192.168.1.14", "BASE",
					remoteDispatcher);
			if (client.connect())
			{
				client.start();
			}
			Thread.sleep(2000);
		}
		catch (Exception e)
		{
			// No networking
			// TODO have variable on whether or not to use the networking stuff
		}
		demo.initListener();
		demo.initPhysics();
		demo.getDynamicsWorld()
				.setDebugDrawer(new GLDebugDrawer(LWJGL.getGL()));
		LWJGL.main(args, 800, 600, "Bullet Physics Demo. http://bullet.sf.net",
				demo);
	}
	
	/**
	 * Initialize event listeners
	 */
	public void initListener()
	{
		/**
		 * LocalEvents
		 */
		BlockCollisionListener blockListener = new BlockCollisionListener();
		eventDispatcher.registerListener(Type.BLOCK_CREATE, blockListener);
		eventDispatcher.registerListener(Type.BLOCK_COLLISION, blockListener);
		eventDispatcher.registerListener(Type.BLOCK_COLLISION_RESOLVED,
				blockListener);
		/**
		 * Remote events
		 */
		BlockListener remoteListener = new BlockListener() {
			@Override
			public void onBlockCreate(BlockCreateEvent event)
			{
				float mass = (1f / event.getEntity().getRigidBody()
						.getInvMass());
				// System.out.println("Event mass: " + mass);
				// System.out.println("Event transform: " +
				// event.getEntity().getRigidBody().getWorldTransform(new
				// Transform()).toString());
				// System.out.println("Event CollisionShape: " +
				// event.getEntity().getRigidBody().getCollisionShape().toString());
				RigidBody body = demo
						.localCreateRigidBody(mass,
								event.getEntity().getRigidBody()
										.getWorldTransform(new Transform()),
								event.getEntity().getRigidBody()
										.getCollisionShape());
				body.setAngularFactor(event.getEntity().getRigidBody()
						.getAngularFactor());
				body.setAngularVelocity(event.getEntity().getRigidBody()
						.getAngularVelocity(new Vector3f()));
				body.setLinearVelocity(event.getEntity().getRigidBody()
						.getLinearVelocity(new Vector3f()));
				body.setDamping(event.getEntity().getRigidBody()
						.getLinearDamping(), event.getEntity().getRigidBody()
						.getAngularDamping());
				// TODO gravity is still borked, at least at the remote side.
				body.setGravity(event.getEntity().getGravity());
				System.out.println("Remote gravity: " +event.getEntity().getGravity());
				// System.out.println(event.getEntity().getRigidBody().getLinearVelocity(new
				// Vector3f()).toString());
				Entity e = new Entity(event.getEntity().getID(), body);
				e.setGravity(event.getEntity().getGravity());
				entityList.put(body, e);
				System.out.println("Added block");
			}
		};
		remoteDispatcher.registerListener(Type.BLOCK_CREATE, remoteListener);
		// MusicPlayer mp = new MusicPlayer(eventDispatcher);
	}
	
	// //////////////////////////////////////////////////////////////////////////
	
	/*
	 * private class BspToBulletConverter extends BspConverter {
	 * 
	 * @Override public void addConvexVerticesCollider(ObjectArrayList<Vector3f>
	 * vertices) { if (vertices.size() > 0) { float mass = 0f; Transform
	 * startTransform = new Transform(); // can use a shift
	 * startTransform.setIdentity(); startTransform.origin.set(0, 0, -10f);
	 * 
	 * // this create an internal copy of the vertices CollisionShape shape =
	 * new ConvexHullShape(vertices); // collisionShapes.add(shape);
	 * 
	 * // btRigidBody* body = m_demoApp->localCreateRigidBody(mass, //
	 * startTransform,shape); localCreateRigidBody(mass, startTransform, shape);
	 * } } }
	 */
	
	public class BspYamlToBulletConverter extends BspYamlConverter
	{
		
		@Override
		public void addConvexVerticesCollider(String name,
				ObjectArrayList<Vector3f> vertices, float mass,
				Vector3f acceleration, String image, String[] description)
		{
			Transform startTransform = new Transform();
			// can use a shift
			startTransform.setIdentity();
			startTransform.origin.set(0, 0, 0);
			
			// this create an internal copy of the vertices
			CollisionShape shape = new ConvexHullShape(vertices);
			RigidBody body = localCreateRigidBody(mass, startTransform, shape);
			Entity e = new Entity(null, null);
			if (description != null)
			{
				e = new Entity(name, body, image, description);
			}
			else
			{
				e = new Entity(name, body, image);
			}
			if (acceleration != null)
			{
				body.setGravity(acceleration);
				e.setGravity(acceleration);
			}
			else
			{
				//set default gravity;
				e.setGravity(dynamicsWorld.getGravity(new Vector3f()));
			}
			entityList.put(body, e);
			eventDispatcher.notify(new BlockCreateEvent(e));
		}
		
	}
	
	public class BlockCollisionListener extends BlockListener
	{
		@Override
		public void onBlockCreate(BlockCreateEvent event)
		{
			// System.out.println("Created: " + event.getEntity().getID());
			
			try
			{
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				final ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(new EventPackage(event));
				oos.flush();
				byte[] data = baos.toByteArray();
				//System.out.println(data.length);
				client.sendMessage(data);
				oos.close();
				baos.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		@Override
		public void onBlockCollision(BlockCollisionEvent event)
		{
			final PersistentManifold pm = event.getPersistentManifold();
			if (pm.getBody0() instanceof RigidBody
					&& pm.getBody1() instanceof RigidBody)
			{
				final Entity entityA = entityList
						.get((RigidBody) pm.getBody0());
				final Entity entityB = entityList
						.get((RigidBody) pm.getBody1());
				if (entityA != null && entityB != null)
				{
					// TODO block unfreeze
					// TODO also, when a block is collided, check if they need
					// to be "refrozen"
					if (entityA.isFrozen())
					{
						entityA.unfreeze();
						entityA.getRigidBody().translate(
								new Vector3f(0f, 5f, 0f));
					}
					if (entityB.isFrozen())
					{
						entityB.unfreeze();
						entityA.getRigidBody().translate(
								new Vector3f(0f, 5f, 0f));
					}
					// Entites are known and exist, so we can act upon them
					if (entityA.getRigidBody().getCollisionShape().getName()
							.equalsIgnoreCase("sphere"))
					{
						if (setGravity(entityB, new Vector3f(0f, 30f, 0f)))
						{
							// entityA.setGravity(new Vector3f(0f, 30f, 0f));
							eventDispatcher.notify(new BlockDestroyedEvent(
									entityA));
							dynamicsWorld.removeCollisionObject(entityA
									.getRigidBody());
							entityList.remove(entityA);
							entityB.getRigidBody().translate(
									new Vector3f(5f, 0f, 0f));
						}
					}
					else if (entityB.getRigidBody().getCollisionShape()
							.getName().equalsIgnoreCase("sphere"))
					{
						if (setGravity(entityA, new Vector3f(0f, 30f, 0f)))
						{
							eventDispatcher.notify(new BlockDestroyedEvent(
									entityB));
							dynamicsWorld.removeCollisionObject(entityB
									.getRigidBody());
							entityA.getRigidBody().translate(
									new Vector3f(5f, 0f, 0f));
						}
					}
					else
					{
						// System.out.println("objA: " + entityA.getID()
						// + " objB: " + entityB.getID());
					}
				}
				else
				{
					// TODO Somehow it is known.... how to handle?
				}
			}
		}
		
		@Override
		public void onBlockCollisionResolved(BlockCollisionResolvedEvent event)
		{
			
			final PersistentManifold pm = event.getPersistentManifold();
			if (pm.getBody0() instanceof RigidBody
					&& pm.getBody1() instanceof RigidBody)
			{
				final Entity entityA = entityList
						.get((RigidBody) pm.getBody0());
				final Entity entityB = entityList
						.get((RigidBody) pm.getBody1());
				if (entityA != null && entityB != null)
				{
					if (entityA.getRigidBody().isActive()
							&& entityB.getRigidBody().isActive())
					{
						if (entityA.getID().equalsIgnoreCase("box")
								&& entityB.getID().equalsIgnoreCase("box"))
						{
							// TODO block freeze event
							entityA.freeze();
							entityB.freeze();
						}
					}
				}
			}
			
		}
		
		public boolean setGravity(Entity target, Vector3f direction)
		{
			if (!target.getRigidBody().isStaticObject())
			{
				eventDispatcher.notify(new BlockPhysicsChangeEvent(target,
						direction));
				target.getRigidBody().setGravity(direction);
				target.getRigidBody().activate();
				
				return true;
			}
			return false;
		}
	}
	
	public class CollisionStuff extends CollisionDispatcher
	{
		
		/*
		 * Standard constructor
		 */
		public CollisionStuff(CollisionConfiguration arg0)
		{
			super(arg0);
		}
		
		/**
		 * Called when a new collision between objects occured
		 */
		@Override
		public PersistentManifold getNewManifold(Object b0, Object b1)
		{
			// TODO maybe check and negate if one of the objects is frozen?
			// don't know the effect it would cause
			final PersistentManifold pm = super.getNewManifold(b0, b1);
			// Throw event
			eventDispatcher.notify(new BlockCollisionEvent(pm));
			return pm;
		}
		
		/**
		 * Called when a collision has been resolved: when the objects are no
		 * longer in contact
		 */
		@Override
		public void releaseManifold(PersistentManifold manifold)
		{
			// Throw event
			eventDispatcher.notify(new BlockCollisionResolvedEvent(manifold));
			super.releaseManifold(manifold);
		}
	}
	
}
