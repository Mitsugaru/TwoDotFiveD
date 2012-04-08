/*
 * Edited version for TwoDotFiveD capstone project
 * 
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
package com.ATeam.twoDotFiveD;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
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
import com.bulletphysics.demos.bsp.BspDemo;
import com.bulletphysics.demos.bsp.BspYamlConverter;
import com.bulletphysics.demos.opengl.DemoApplication;
import com.bulletphysics.demos.opengl.GLDebugDrawer;
import com.bulletphysics.demos.opengl.IGL;
import com.bulletphysics.demos.opengl.LWJGL;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

import javax.vecmath.Vector3f;

import org.lwjgl.input.Keyboard;

import static com.bulletphysics.demos.opengl.IGL.*;

@SuppressWarnings("unused")
public class TwoDotFiveDBsp extends DemoApplication
{
	private static TwoDotFiveDBsp					demo;
	private static final float				CUBE_HALF_EXTENTS	= 1;
	private static final float				EXTRA_HEIGHT		= -20f;
	
	// keep the collision shapes, for deletion/cleanup
	public Set<Entity>						entityList			= new HashSet<Entity>();
	public BroadphaseInterface				broadphase;
	public CollisionDispatcher				dispatcher;
	public ConstraintSolver					solver;
	public DefaultCollisionConfiguration	collisionConfiguration;
	private static EventDispatcher			eventDispatcher		= new EventDispatcher();
	private static EventDispatcher			remoteDispatcher	= new EventDispatcher();
	private static chatClient				client;
	int										count				= 0;
	private static boolean					connected			= false;
	
	public TwoDotFiveDBsp(IGL gl)
	{
		super(gl);
	}
	
	public synchronized void initPhysics() throws Exception
	{
		// cameraUp.set(0f, 0f, 1f);
		// forwardAxis = 1;
		
		setCameraDistance(22f);
		// Setup a Physics Simulation Environment
		
		collisionConfiguration = new DefaultCollisionConfiguration();
		// btCollisionShape* groundShape = new btBoxShape(btVector3(50,3,50));
		dispatcher = new CollisionStuff(collisionConfiguration);
		// the maximum size of the collision world. Make sure objects stay
		// within these boundaries. Don't make the world AABB size too large, it
		// will harm simulation quality and performance
		Vector3f worldMin = new Vector3f(-10f, -10f, -10f);
		Vector3f worldMax = new Vector3f(10f, 10f, 10f);
		// maximum number of objects
		final int maxProxies = 1024;
		// Broadphase computes an conservative approximate list of colliding
		// pairs
		broadphase = new AxisSweep3(worldMin, worldMax, maxProxies);
		// broadphase = new SimpleBroadphase();
		// broadphase = new DbvtBroadphase();
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
		/*
		 * Transform startTransform = new Transform();
		 * startTransform.setIdentity();
		 * 
		 * float start_x = 0 - 5 / 2; float start_y = 0; float start_z = 0 - 5 /
		 * 2; final ObjectArrayList<Vector3f> points = new
		 * ObjectArrayList<Vector3f>(); points.add(new Vector3f(0f, 0f, 0f));
		 * points.add(new Vector3f(0f, 0f, 2f)); points.add(new Vector3f(0f, 2f,
		 * 0f)); points.add(new Vector3f(0f, 2f, 2f)); points.add(new
		 * Vector3f(2f, 0f, 0f)); points.add(new Vector3f(2f, 0f, 2f));
		 * points.add(new Vector3f(2f, 2f, 0f)); points.add(new Vector3f(2f, 2f,
		 * 2f)); final CollisionShape shape = new ConvexHullShape(points); for
		 * (int k = 0; k < 4; k++) { for (int i = 0; i < 4; i++) { for (int j =
		 * 0; j < 4; j++) { startTransform.origin.set(2f * i + start_x, 10f + 2f
		 * * k + start_y, 2f * j + start_z); RigidBody body =
		 * localCreateRigidBody(1f, startTransform, shape); // TODO figure out
		 * why setting the center of mass transform // doesn't want to work
		 * Transform center = new Transform(); center.setIdentity();
		 * center.origin.set(0.5f, 0.5f, 0.5f);
		 * body.setCenterOfMassTransform(center); // eventDispatcher.notify(new
		 * BlockCreateEvent(new // Entity("Box", body))); } } }
		 */
		clientResetScene();
	}
	
	@Override
	public synchronized void clientMoveAndDisplay()
	{
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		float dt = getDeltaTimeMicroseconds() * 0.000001f;
		try
		{
			// TODO May need custom DynamicsWorld to catch exceptions per step
			dynamicsWorld.stepSimulation(dt);
		}
		catch (NullPointerException e)
		{
			System.out.println("Simulation had null at some point");
			// WARN this is very serious
			// TODO figure out how to fix this...
		}
		catch (ArrayIndexOutOfBoundsException arr)
		{
			System.out.println("Index Out of Bounds in Simulation");
		}
		
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
	public synchronized void specialKeyboard(int key, int x, int y,
			int modifiers)
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
					Entity e = null;
					for (Entity r : entityList.toArray(new Entity[0]))
					{
						if (r.getCollisionShape().equals(a.getCollisionShape()))
						{
							e = r;
							break;
						}
					}
					try
					{
						dynamicsWorld.removeCollisionObject(a);
						if (e != null)
						{
							eventDispatcher.notify(new BlockDestroyedEvent(e));
							entityList.remove(e);
						}
					}
					catch (NullPointerException n)
					{
						System.out
								.println("Tried to remove object that is not there");
					}
					catch (ArrayIndexOutOfBoundsException b)
					{
						System.out
								.println("ArrayIndexOutOfBounds in simulation");
					}
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
	
	public synchronized Entity localCreateEntity(float mass,
			Transform startTransform, CollisionShape shape, String ID,
			String image, String[] description)
	{
		// rigidbody is dynamic if and only if mass is non zero, otherwise
		// static
		boolean isDynamic = (mass != 0f);
		
		Vector3f localInertia = new Vector3f(0f, 0f, 0f);
		if (isDynamic)
		{
			shape.calculateLocalInertia(mass, localInertia);
		}
		
		// using motionstate is recommended, it provides interpolation
		// capabilities, and only synchronizes 'active' objects
		DefaultMotionState myMotionState = new DefaultMotionState(
				startTransform);
		Entity e = new Entity(mass, myMotionState, shape, localInertia, ID,
				image, description);
		dynamicsWorld.addRigidBody(e);
		
		// Dynamic gravity for object
		if (!bodyGravityType.equals("NORMAL"))
		{
			if (bodyGravityType.equals("ANTIGRAVITY"))
			{
				e.setGravity(new Vector3f(0f, 30f, 0f));
			}
			else if (bodyGravityType.equals("STASIS"))
			{
				e.setGravity(new Vector3f(0f, 0f, 0f));
			}
		}
		return e;
	}
	
	@Override
	public synchronized void shootBox(Vector3f destination)
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
			final Random r = new Random();
			Entity entity = localCreateEntity(mass, startTransform,
					shootBoxShape, shootBoxShape.getName() + r.nextFloat(),
					null, null);
			Vector3f linVel = new Vector3f(destination.x - camPos.x,
					destination.y - camPos.y, destination.z - camPos.z);
			linVel.normalize();
			linVel.scale(ShootBoxInitialSpeed);
			
			entity.setLinearVelocity(linVel);
			entity.setAngularVelocity(new Vector3f(0f, 0f, 0f));
			
			entity.setCcdMotionThreshold(1f);
			entity.setCcdSweptSphereRadius(0.2f);
			// Dynamic gravity for object
			if (!bodyGravityType.equals("NORMAL"))
			{
				if (bodyGravityType.equals("ANTIGRAVITY"))
				{
					entity.setEntityGravity(new Vector3f(0f, 30f, 0f));
					entity.setGravity(new Vector3f(0f, 30f, 0f));
				}
				else if (bodyGravityType.equals("STASIS"))
				{
					entity.setEntityGravity(new Vector3f(0f, 0f, 0f));
				}
			}
			else
			{
				entity.setEntityGravity(dynamicsWorld
						.getGravity(new Vector3f()));
				entity.setGravity(dynamicsWorld.getGravity(new Vector3f()));
			}
			entityList.add(entity);
			eventDispatcher.notify(new BlockCreateEvent(entity));
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		demo = new TwoDotFiveDBsp(LWJGL.getGL());
		try
		{
			client = new chatClient(null, "137.155.38.254", "ASDF",
					remoteDispatcher);
			if (client.connect())
			{
				client.start();
				connected = true;
			}
			Thread.sleep(2000);
		}
		catch (Exception e)
		{
			// No networking
			connected = false;
		}
		demo.initListener();
		demo.initPhysics();
		demo.getDynamicsWorld()
				.setDebugDrawer(new GLDebugDrawer(LWJGL.getGL()));
		// demo.debugMode = 1;
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
		eventDispatcher.registerListener(Type.BLOCK_DESTROYED, blockListener);
		eventDispatcher.registerListener(Type.BLOCK_COLLISION, blockListener);
		eventDispatcher.registerListener(Type.BLOCK_COLLISION_RESOLVED,
				blockListener);
		eventDispatcher.registerListener(Type.BLOCK_PHYSICS_CHANGE, blockListener);
		/**
		 * Remote events
		 */
		if (connected)
		{
			BlockListener remoteListener = new BlockListener() {
				@Override
				public void onBlockCreate(BlockCreateEvent event)
				{
					float mass = (1f / event.getEntity().getInvMass());
					// System.out.println("Event mass: " + mass);
					// System.out.println("Event transform: " +
					// event.getEntity().getRigidBody().getWorldTransform(new
					// Transform()).toString());
					// System.out.println("Event CollisionShape: " +
					// event.getEntity().getRigidBody().getCollisionShape().toString());
					Entity entity = localCreateEntity(mass, event.getEntity()
							.getWorldTransform(new Transform()), event
							.getEntity().getCollisionShape(), event.getEntity()
							.getID(), "", new String[] { "" });
					entity.setAngularFactor(event.getEntity()
							.getAngularFactor());
					entity.setAngularVelocity(event.getEntity()
							.getAngularVelocity(new Vector3f()));
					entity.setLinearVelocity(event.getEntity()
							.getLinearVelocity(new Vector3f()));
					entity.setDamping(event.getEntity().getLinearDamping(),
							event.getEntity().getAngularDamping());
					entity.setEntityGravity(event.getEntity()
							.getEntityGravity());
					// System.out.println("Remote gravity: "
					// +event.getEntity().getGravity());
					// System.out.println(event.getEntity().getRigidBody().getLinearVelocity(new
					// Vector3f()).toString());
					entityList.add(entity);
					// System.out.println("Added block");
				}
				
				@Override
				public synchronized void onBlockDestroyed(
						BlockDestroyedEvent event)
				{
					// System.out.println("Received destroyed event");
					Entity removed = null;
					for (Entity e : entityList.toArray(new Entity[0]))
					{
						if (e.getID().equals(event.getEntity().getID()))
						{
							removed = e;
							break;
						}
					}
					if (removed != null)
					{
						// System.out.println("Found in list");
						final CollisionShape shape = removed
								.getCollisionShape();
						CollisionObject toRemove = null;
						for (CollisionObject o : dynamicsWorld
								.getCollisionObjectArray())
						{
							if (o.getCollisionShape().equals(shape))
							{
								// System.out.println("found in dynamics world");
								toRemove = o;
								break;
							}
						}
						if (toRemove != null)
						{
							// System.out.println("Removed");
							try
							{
								dynamicsWorld.removeCollisionObject(toRemove);
							}
							catch (NullPointerException e)
							{
								System.out
										.println("Attempted to remove object that no longer exists.");
							}
							catch (ArrayIndexOutOfBoundsException a)
							{
								System.out
										.println("Attempted to remove object that no longer exists.");
							}
						}
						
					}
				}
				
				@Override
				public void onBlockPhysicsChange(BlockPhysicsChangeEvent event)
				{
					for (Entity e : entityList.toArray(new Entity[0]))
					{
						if (e.getID().equals(event.getEntity().getID()))
						{
							e.setEntityGravity(event.getDirection());
							break;
						}
					}
				}
			};
			remoteDispatcher
					.registerListener(Type.BLOCK_CREATE, remoteListener);
			remoteDispatcher.registerListener(Type.BLOCK_DESTROYED,
					remoteListener);
			remoteDispatcher.registerListener(Type.BLOCK_PHYSICS_CHANGE,
					remoteListener);
		}
		// MusicPlayer mp = new MusicPlayer(eventDispatcher);
	}
	
	// //////////////////////////////////////////////////////////////////////////
	
	public class BspYamlToBulletConverter extends BspYamlConverter
	{
		
		@Override
		public synchronized void addConvexVerticesCollider(String name,
				ObjectArrayList<Vector3f> vertices, float mass,
				Vector3f acceleration, String image, String[] description)
		{
			Transform startTransform = new Transform();
			// can use a shift
			startTransform.setIdentity();
			startTransform.origin.set(0, 0, 0);
			
			// this create an internal copy of the vertices
			CollisionShape shape = new ConvexHullShape(vertices);
			// body.setActivationState(RigidBody.ACTIVE_TAG);
			final Transform center = new Transform();
			center.setIdentity();
			center.origin.set(1f, 1f, 1f);
			addEntity(mass, center, shape, name, image, description,
					acceleration);
		}
		
		@Override
		public void addShapeCollider(String name, String type,
				Vector3f localscaling, Vector3f transform, float mass,
				Vector3f acceleration, String image, String[] description)
		{
			CollisionShape shape = new BoxShape(new Vector3f(1f, 1f, 1f));
			if (type.contains("box"))
			{
				shape = new BoxShape(localscaling);
			}
			else if (type.contains("sphere"))
			{
				shape = new SphereShape(localscaling.x);
			}
			else if (type.contains("cylinder"))
			{
				shape = new CylinderShape(localscaling);
			}
			else if (type.contains("cone"))
			{
				shape = new ConeShape(localscaling.x, localscaling.y);
			}
			else
			{
				Logging.log.warning("Unknown type '" + type + "' for shape: "
						+ name);
				return;
			}
			Transform origin = new Transform();
			origin.setIdentity();
			origin.origin.set(transform.x, transform.y, transform.z);
			addEntity(mass, origin, shape, name, image, description,
					acceleration);
		}
		
		public void addEntity(float mass, Transform origin,
				CollisionShape shape, String name, String image,
				String[] description, Vector3f acceleration)
		{
			Entity e = localCreateEntity(mass, origin, shape, name, image,
					description);
			if (acceleration != null)
			{
				e.setEntityGravity(acceleration);
			}
			else
			{
				// set default gravity;
				e.setGravity(dynamicsWorld.getGravity(new Vector3f()));
			}
			entityList.add(e);
			eventDispatcher.notify(new BlockCreateEvent(e));
		}
	}
	
	public class BlockCollisionListener extends BlockListener
	{
		@Override
		public void onBlockCreate(BlockCreateEvent event)
		{
			sendToRemote(event);
		}
		
		@Override
		public void onBlockDestroyed(BlockDestroyedEvent event)
		{
			sendToRemote(event);
		}
		
		@Override
		public synchronized void onBlockCollision(BlockCollisionEvent event)
		{
			final PersistentManifold pm = event.getPersistentManifold();
			if (pm.getBody0() instanceof Entity
					&& pm.getBody1() instanceof Entity)
			{
				final Entity entityA = (Entity) pm.getBody0();
				final Entity entityB = (Entity) pm.getBody1();
				if (entityA != null && entityB != null)
				{
					
					if (entityA.getID().equals("object2"))
					{
						entityA.setAngularFactor(0f);
						entityA.setAngularVelocity(new Vector3f(0f, 0f, 0f));
						entityA.setLinearVelocity(new Vector3f(0f, 0f, 0f));
						entityA.setEntityGravity(new Vector3f(0f, 10f, 0f));
					}
					else if (entityB.getID().equals("object2"))
					{
						entityB.setAngularFactor(0f);
						entityB.setAngularVelocity(new Vector3f(0f, 0f, 0f));
						entityB.setLinearVelocity(new Vector3f(0f, 0f, 0f));
						entityB.setEntityGravity(new Vector3f(0f, 10f, 0f));
					}
					// TODO also, when a block is collided, check if they need
					// to be "refrozen"
					if (entityA.isFrozen())
					{
						entityA.unfreeze();
						//entityA.translate(new Vector3f(0f, 5f, 0f));
					}
					if (entityB.isFrozen())
					{
						entityB.unfreeze();
						//entityA.translate(new Vector3f(0f, 5f, 0f));
					}
					// Entites are known and exist, so we can act upon them
					if (entityA.getCollisionShape().getName()
							.equalsIgnoreCase("sphere"))
					{
						if (setGravity(entityB, new Vector3f(0f, 30f, 0f)))
						{
							// entityA.setGravity(new Vector3f(0f, 30f, 0f));
							eventDispatcher.notify(new BlockDestroyedEvent(
									entityA));
							dynamicsWorld.removeCollisionObject(entityA);
							entityList.remove(entityA);
							//entityB.translate(new Vector3f(5f, 0f, 0f));
						}
					}
					else if (entityB.getCollisionShape().getName()
							.equalsIgnoreCase("sphere"))
					{
						if (setGravity(entityA, new Vector3f(0f, 30f, 0f)))
						{
							eventDispatcher.notify(new BlockDestroyedEvent(
									entityB));
							dynamicsWorld.removeCollisionObject(entityB);
							//entityA.translate(new Vector3f(5f, 0f, 0f));
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
			else
			{
				System.out.println("MF");
			}
		}
		
		@Override
		public void onBlockCollisionResolved(BlockCollisionResolvedEvent event)
		{
			
			final PersistentManifold pm = event.getPersistentManifold();
			if (pm.getBody0() instanceof Entity
					&& pm.getBody1() instanceof Entity)
			{
				final Entity entityA = (Entity) pm.getBody0();
				final Entity entityB = (Entity) pm.getBody1();
				if (entityA != null && entityB != null)
				{
					/*
					 * if (entityA.isActive() && entityB.isActive()) { if
					 * (entityA.getID().equalsIgnoreCase("box") &&
					 * entityB.getID().equalsIgnoreCase("box")) { // TODO block
					 * freeze event entityA.freeze(); entityB.freeze(); } }
					 */
				}
			}
			
		}
		
		@Override
		public void onBlockPhysicsChange(BlockPhysicsChangeEvent event)
		{
			sendToRemote(event);
		}
		
		public boolean setGravity(Entity target, Vector3f direction)
		{
			if (!target.isStaticObject())
			{
				eventDispatcher.notify(new BlockPhysicsChangeEvent(target,
						direction));
				target.setEntityGravity(direction);
				target.activate();
				return true;
			}
			return false;
		}
		
		public void sendToRemote(Event<?> event)
		{
			if (connected)
			{
				try
				{
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					final ObjectOutputStream oos = new ObjectOutputStream(baos);
					oos.writeObject(new EventPackage(event));
					oos.flush();
					byte[] data = baos.toByteArray();
					// System.out.println(data.length);
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
		 * Called when a new collision between objects occurred
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
