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
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
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
    private static BspDemo demo;

    private static final float CUBE_HALF_EXTENTS = 1;

    private static final float EXTRA_HEIGHT = -20f;

    // keep the collision shapes, for deletion/cleanup
    public Map<RigidBody, Entity> entityList = new HashMap<RigidBody, Entity>();

    public BroadphaseInterface broadphase;

    public CollisionDispatcher dispatcher;

    public ConstraintSolver solver;

    public DefaultCollisionConfiguration collisionConfiguration;

    private static EventDispatcher eventDispatcher = new EventDispatcher();

    private static EventDispatcher remoteDispatcher = new EventDispatcher();

    private static chatClient client;

    int count = 0;

    private static boolean connected = false;
    
    private RigidBody player;


    public BspDemo( IGL gl )
    {
        super( gl );
    }


    public synchronized void initPhysics() throws Exception
    {
        // cameraUp.set(0f, 0f, 1f);
        // forwardAxis = 1;

        setCameraDistance( 22f );
        // Setup a Physics Simulation Environment

        collisionConfiguration = new DefaultCollisionConfiguration();
        // btCollisionShape* groundShape = new btBoxShape(btVector3(50,3,50));
        dispatcher = new CollisionStuff( collisionConfiguration );
        // the maximum size of the collision world. Make sure objects stay
        // within these boundaries. Don't make the world AABB size too large, it
        // will harm simulation quality and performance
        Vector3f worldMin = new Vector3f( -10f, -10f, -10f );
        Vector3f worldMax = new Vector3f( 10f, 10f, 10f );
        // maximum number of objects
        final int maxProxies = 1024;
        // Broadphase computes an conservative approximate list of colliding
        // pairs
        broadphase = new AxisSweep3( worldMin, worldMax, maxProxies );
        // broadphase = new SimpleBroadphase();
        // broadphase = new DbvtBroadphase();
        // btOverlappingPairCache* broadphase = new btSimpleBroadphase();
        solver = new SequentialImpulseConstraintSolver();
        // ConstraintSolver* solver = new OdeConstraintSolver;
        dynamicsWorld = new DiscreteDynamicsWorld( dispatcher,
            broadphase,
            solver,
            collisionConfiguration );

        Vector3f gravity = new Vector3f();
        gravity.negate( cameraUp );
        gravity.scale( 10f );
        dynamicsWorld.setGravity( gravity );
        // new BspToBulletConverter().convertBsp(getClass().getResourceAsStream(
        // "exported.bsp.txt"));
        // populate();
        BulletGlobals.setDeactivationTime( 0.1f );

    }


    public void populate()
    {
        try
        {
            new BspYamlToBulletConverter().convertBspYaml( getClass()
            // TODO changer here
            .getResourceAsStream( "EntryScene.yml" ) );
        }
        catch ( IOException e )
        {
            Logging.log.log( Level.SEVERE,
                "Could not close InputStream for: scene.yml",
                e );
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
        gl.glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
        float dt = getDeltaTimeMicroseconds() * 0.000001f;
        try
        {
            // TODO May need custom DynamicsWorld to catch exceptions per step
            dynamicsWorld.stepSimulation( dt );
        }
        catch ( NullPointerException e )
        {
            System.out.println( "Simulation had null at some point" );
            // WARN this is very serious
            // TODO figure out how to fix this...
        }
        catch ( ArrayIndexOutOfBoundsException arr )
        {
            System.out.println( "Index Out of Bounds in Simulation" );
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
        gl.glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

        renderme();

        // glFlush();
        // glutSwapBuffers();
    }


    @Override
    public synchronized void specialKeyboard(
        int key,
        int x,
        int y,
        int modifiers )
    {
        switch ( key )
        {
            case Keyboard.KEY_R:
            {
                // Remove all objects
                for ( CollisionObject a : dynamicsWorld.getCollisionObjectArray()
                    .toArray( new CollisionObject[0] ) )
                {
                    Entity e = null;
                    for ( RigidBody r : entityList.keySet() )
                    {
                        if ( r.getCollisionShape()
                            .equals( a.getCollisionShape() ) )
                        {
                            e = entityList.get( r );
                            break;
                        }
                    }
                    try
                    {
                        dynamicsWorld.removeCollisionObject( a );
                        if ( e != null )
                        {
                            eventDispatcher.notify( new BlockDestroyedEvent( e ) );
                            entityList.remove( e );
                        }
                    }
                    catch ( NullPointerException n )
                    {
                        System.out.println( "Tried to remove object that is not there" );
                    }
                    catch ( ArrayIndexOutOfBoundsException b )
                    {
                        System.out.println( "ArrayIndexOutOfBounds in simulation" );
                    }
                }
                // repopulate world
                populate();
                break;
            }
            default:
            {
                super.specialKeyboard( key, x, y, modifiers );
                break;
            }
        }
    }


    @Override
    public synchronized void shootBox( Vector3f destination )
    {
        if ( dynamicsWorld != null )
        {
            float mass = 50f;
            Transform startTransform = new Transform();
            startTransform.setIdentity();
            Vector3f camPos = new Vector3f( getCameraPosition() );
            startTransform.origin.set( camPos );

            if ( shapeType.equals( "BOX" ) )
            {
                shootBoxShape = new BoxShape( new Vector3f( 1f, 1f, 1f ) );
            }
            else if ( shapeType.equals( "SPHERE" ) )
            {
                shootBoxShape = new SphereShape( 1f );
            }
            else if ( shapeType.equals( "TRIANGLE" ) )
            {
                // TODO implement a pyramid
                shootBoxShape = new ConeShape( 1f, 3f );
                // shootBoxShape = new TriangleShape(new Vector3f(1f, 1f, 1f),
                // new Vector3f(1f, 0f, 0f), new Vector3f(0f, -1f, 0f));
            }
            else if ( shapeType.equals( "CYLINDER" ) )
            {
                shootBoxShape = new CylinderShape( new Vector3f( 1f, 1f, 1f ) );
            }

            RigidBody body = this.localCreateRigidBody( mass,
                startTransform,
                shootBoxShape );

            Vector3f linVel = new Vector3f( destination.x - camPos.x,
                destination.y - camPos.y,
                destination.z - camPos.z );
            linVel.normalize();
            linVel.scale( ShootBoxInitialSpeed );
            Transform worldTrans = body.getWorldTransform( new Transform() );
            worldTrans.origin.set( camPos );
            worldTrans.setRotation( new Quat4f( 0f, 0f, 0f, 1f ) );
            body.setWorldTransform( worldTrans );

            body.setLinearVelocity( linVel );
            body.setAngularVelocity( new Vector3f( 0f, 0f, 0f ) );

            body.setCcdMotionThreshold( 1f );
            body.setCcdSweptSphereRadius( 0.2f );
            Entity entity = new Entity( body.getCollisionShape().getName(),
                body );
            // Dynamic gravity for object
            // TODO consolidate setgravity into one method, let entity set its
            // tied rigidbody gravity
            if ( !bodyGravityType.equals( "NORMAL" ) )
            {
                if ( bodyGravityType.equals( "ANTIGRAVITY" ) )
                {
                    entity.getRigidBody()
                        .setGravity( new Vector3f( 0f, 30f, 0f ) );
                    entity.setGravity( new Vector3f( 0f, 30f, 0f ) );
                }
                else if ( bodyGravityType.equals( "STASIS" ) )
                {
                    entity.getRigidBody()
                        .setGravity( new Vector3f( 0f, 0f, 0f ) );
                    entity.setGravity( new Vector3f( 0f, 0f, 0f ) );
                }
            }
            else
            {
                entity.getRigidBody()
                    .setGravity( dynamicsWorld.getGravity( new Vector3f() ) );
                entity.setGravity( dynamicsWorld.getGravity( new Vector3f() ) );
            }
            entityList.put( body, entity );
            eventDispatcher.notify( new BlockCreateEvent( entity ) );
        }
    }


    public static void main( String[] args ) throws Exception
    {
        demo = new BspDemo( LWJGL.getGL() );
//        try
//        {
//            client = new chatClient( null,
//                "137.155.2.153",
//                "BASE",
//                remoteDispatcher );
//            if ( client.connect() )
//            {
//                client.start();
//                connected = true;
//            }
//            Thread.sleep( 2000 );
//        }
//        catch ( Exception e )
//        {
//            // No networking
//            connected = false;
//        }
        demo.initListener();
        demo.initPhysics();
        demo.getDynamicsWorld()
            .setDebugDrawer( new GLDebugDrawer( LWJGL.getGL() ) );
        // demo.debugMode = 1;
        LWJGL.main( args,
            800,
            600,
            "Bullet Physics Demo. http://bullet.sf.net",
            demo );
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
        eventDispatcher.registerListener( Type.BLOCK_CREATE, blockListener );
        eventDispatcher.registerListener( Type.BLOCK_DESTROYED, blockListener );
        eventDispatcher.registerListener( Type.BLOCK_COLLISION, blockListener );
        eventDispatcher.registerListener( Type.BLOCK_COLLISION_RESOLVED,
            blockListener );
        /**
         * Remote events
         */
        if ( connected )
        {
            BlockListener remoteListener = new BlockListener()
            {
                @Override
                public void onBlockCreate( BlockCreateEvent event )
                {
                    float mass = ( 1f / event.getEntity()
                        .getRigidBody()
                        .getInvMass() );
                    // System.out.println("Event mass: " + mass);
                    // System.out.println("Event transform: " +
                    // event.getEntity().getRigidBody().getWorldTransform(new
                    // Transform()).toString());
                    // System.out.println("Event CollisionShape: " +
                    // event.getEntity().getRigidBody().getCollisionShape().toString());
                    RigidBody body = demo.localCreateRigidBody( mass,
                        event.getEntity()
                            .getRigidBody()
                            .getWorldTransform( new Transform() ),
                        event.getEntity().getRigidBody().getCollisionShape() );
                    body.setAngularFactor( event.getEntity()
                        .getRigidBody()
                        .getAngularFactor() );
                    body.setAngularVelocity( event.getEntity()
                        .getRigidBody()
                        .getAngularVelocity( new Vector3f() ) );
                    body.setLinearVelocity( event.getEntity()
                        .getRigidBody()
                        .getLinearVelocity( new Vector3f() ) );
                    body.setDamping( event.getEntity()
                        .getRigidBody()
                        .getLinearDamping(), event.getEntity()
                        .getRigidBody()
                        .getAngularDamping() );
                    body.setGravity( event.getEntity().getGravity() );
                    // System.out.println("Remote gravity: "
                    // +event.getEntity().getGravity());
                    // System.out.println(event.getEntity().getRigidBody().getLinearVelocity(new
                    // Vector3f()).toString());
                    Entity e = new Entity( event.getEntity().getID(), body );
                    e.setGravity( event.getEntity().getGravity() );
                    entityList.put( body, e );
                    // System.out.println("Added block");
                }


                @Override
                public synchronized void onBlockDestroyed(
                    BlockDestroyedEvent event )
                {
                    // System.out.println("Received destroyed event");
                    Entity removed = null;
                    for ( Entity e : entityList.values() )
                    {
                        if ( e.getID().equals( event.getEntity().getID() ) )
                        {
                            removed = e;
                            break;
                        }
                    }
                    if ( removed != null )
                    {
                        // System.out.println("Found in list");
                        removed = entityList.remove( removed.getRigidBody() );
                        final CollisionShape shape = removed.getRigidBody()
                            .getCollisionShape();
                        CollisionObject toRemove = null;
                        for ( CollisionObject o : dynamicsWorld.getCollisionObjectArray() )
                        {
                            if ( o.getCollisionShape().equals( shape ) )
                            {
                                // System.out.println("found in dynamics world");
                                toRemove = o;
                                break;
                            }
                        }
                        if ( toRemove != null )
                        {
                            // System.out.println("Removed");
                            try
                            {
                                dynamicsWorld.removeCollisionObject( toRemove );
                            }
                            catch ( NullPointerException e )
                            {
                                System.out.println( "Attempted to remove object taht no longer exists." );
                            }
                            catch ( ArrayIndexOutOfBoundsException a )
                            {
                                System.out.println( "Attempted to remove object taht no longer exists." );
                            }
                        }

                    }
                }
            };
            remoteDispatcher.registerListener( Type.BLOCK_CREATE,
                remoteListener );
            remoteDispatcher.registerListener( Type.BLOCK_DESTROYED,
                remoteListener );
        }
        // MusicPlayer mp = new MusicPlayer(eventDispatcher);
    }


    // //////////////////////////////////////////////////////////////////////////

    public class BspYamlToBulletConverter extends BspYamlConverter
    {

        @Override
        public synchronized void addConvexVerticesCollider(
            String name,
            ObjectArrayList<Vector3f> vertices,
            float mass,
            Vector3f acceleration,
            String image,
            String[] description )
        {
            Transform startTransform = new Transform();
            // can use a shift
            startTransform.setIdentity();
            startTransform.origin.set( 0, 0, 0 );

            // this create an internal copy of the vertices
            CollisionShape shape = new ConvexHullShape( vertices );
            RigidBody body = localCreateRigidBody( mass, startTransform, shape );
            // body.setActivationState(RigidBody.ACTIVE_TAG);
            final Transform center = new Transform();
            center.setIdentity();
            center.origin.set( 1f, 1f, 1f );
            body.setCenterOfMassTransform( center );
            addEntity( name, body, image, description, acceleration );
        }


        @Override
        public void addShapeCollider(
            String name,
            String type,
            Vector3f localscaling,
            Vector3f transform,
            float mass,
            Vector3f acceleration,
            String image,
            String[] description )
        {
            CollisionShape shape = new BoxShape( new Vector3f( 1f, 1f, 1f ) );
            if ( type.contains( "box" ) )
            {
                shape = new BoxShape( localscaling );
            }
            else if ( type.contains( "sphere" ) )
            {
                shape = new SphereShape( localscaling.x );
            }
            else if ( type.contains( "cylinder" ) )
            {
                shape = new CylinderShape( localscaling );
            }
            else if ( type.contains( "cone" ) )
            {
                shape = new ConeShape( localscaling.x, localscaling.y );
            }
            else
            {
                Logging.log.warning( "Unknown type '" + type + "' for shape: "
                    + name );
                return;
            }
            Transform origin = new Transform();
            origin.setIdentity();
            origin.origin.set( transform.x, transform.y, transform.z );
            RigidBody body = localCreateRigidBody( mass, origin, shape );
            addEntity( name, body, image, description, acceleration );
        }


        public void addEntity(
            String name,
            RigidBody body,
            String image,
            String[] description,
            Vector3f acceleration )
        {
            Entity e = new Entity( null, null );
            if ( description != null )
            {
                e = new Entity( name, body, image, description );
            }
            else
            {
                e = new Entity( name, body, image );
            }
            if ( acceleration != null )
            {
                body.setGravity( acceleration );
                e.setGravity( acceleration );
            }
            else
            {
                // set default gravity;
                e.setGravity( dynamicsWorld.getGravity( new Vector3f() ) );
            }
            entityList.put( body, e );
            eventDispatcher.notify( new BlockCreateEvent( e ) );
        }
    }


    public class BlockCollisionListener extends BlockListener
    {
        @Override
        public void onBlockCreate( BlockCreateEvent event )
        {
            sendToRemote( event );
        }


        @Override
        public void onBlockDestroyed( BlockDestroyedEvent event )
        {
            sendToRemote( event );
        }


        @Override
        public synchronized void onBlockCollision( BlockCollisionEvent event )
        {
            final PersistentManifold pm = event.getPersistentManifold();
            if ( pm.getBody0() instanceof RigidBody
                && pm.getBody1() instanceof RigidBody )
            {
                final Entity entityA = entityList.get( (RigidBody)pm.getBody0() );
                final Entity entityB = entityList.get( (RigidBody)pm.getBody1() );
                if ( entityA != null && entityB != null )
                {
//                    if ( entityA.getID().equals( "object2" ) )
//                    {
//                        entityA.getRigidBody().setAngularFactor( 0f );
//                        entityA.getRigidBody()
//                            .setAngularVelocity( new Vector3f( 0f, 0f, 0f ) );
//                        entityA.getRigidBody()
//                            .setLinearVelocity( new Vector3f( 0f, 0f, 0f ) );
//                        entityA.getRigidBody().setGravity( new Vector3f( 0f,
//                            10f,
//                            0f ) );
//                        System.out.println( "OMG" );
//                    }
//                    else if ( entityB.getID().equals( "object2" ) )
//                    {
//                        entityB.getRigidBody().setAngularFactor( 0f );
//                        entityB.getRigidBody()
//                            .setAngularVelocity( new Vector3f( 0f, 0f, 0f ) );
//                        entityB.getRigidBody()
//                            .setLinearVelocity( new Vector3f( 0f, 0f, 0f ) );
//                        entityB.getRigidBody().setGravity( new Vector3f( 0f,
//                            10f,
//                            0f ) );
//                        System.out.println( "OMA" );
//                    }
                    // TODO CLIFF STUFF
                    if ( ( entityA.getID().equals( "elevatorsmash1" ) || entityB.getID()
                        .equals( "elevatorsmash1" ) )
                        &&( entityA.getID()
                            .equals( "sidepathleftelevatorshaftfix" )
                        || entityB.getID()
                            .equals( "sidepathleftelevatorshaftfix" )) )
                    {
                        if ( entityA.getID().equals( "elevatorsmash1" ) )
                        {
                            // entityA.getRigidBody().setAngularVelocity( new
                            // Vector3f(0f, 0f, 7f) );
                            entityA.freeze();
                            final RigidBody body = entityA.getRigidBody();
                            final Vector3f vector = body.getCenterOfMassPosition(new Vector3f() );
                            body.translate( new Vector3f(80f - vector.x , -5f - vector.y , -11f - vector.z) );
                            body
                                .setGravity( new Vector3f( 0f, 7f, 0f ) );
                            
                        }
                        else
                        {
                            // entityB.getRigidBody().setAngularVelocity( new
                            // Vector3f(0f, 0f, 7f) );
                            entityB.freeze();
                            final RigidBody body = entityB.getRigidBody();
                            final Vector3f vector = body.getCenterOfMassPosition(new Vector3f() );
                            body.translate( new Vector3f(80f - vector.x , -5f - vector.y , -11f - vector.z) );
                            body
                                .setGravity( new Vector3f( 0f, 7f, 0f ) );
                        }
                    }
                    if ( ( entityA.getID().equals( "elevatorsmash1" ) || entityB.getID()
                        .equals( "elevatorsmash1" ) )
                        && (entityA.getID()
                            .equals( "elevatorshaftcieling" )
                        || entityB.getID()
                            .equals( "elevatorshaftcieling" )) )
                    {
                        if ( entityA.getID().equals( "elevatorsmash1" ) )
                        {
                            // entityA.getRigidBody().setAngularVelocity( new
                            // Vector3f(0f, 0f, 7f) );
                            entityA.freeze();
                            final RigidBody body = entityA.getRigidBody();
                            final Vector3f vector = body.getCenterOfMassPosition(new Vector3f() );
                            body.translate( new Vector3f(80f - vector.x , 1.5f - vector.y , -11f - vector.z) );
                            body
                                .setGravity( new Vector3f( 0f, -7f, 0f ) );
                        }
                        else
                        {
                            // entityB.getRigidBody().setAngularVelocity( new
                            // Vector3f(0f, 0f, 7f) );
                            entityB.freeze();
                            final RigidBody body = entityB.getRigidBody();
                            final Vector3f vector = body.getCenterOfMassPosition(new Vector3f() );
                            body.translate( new Vector3f(80f - vector.x , 1.5f - vector.y , -11f - vector.z) );
                            body
                                .setGravity( new Vector3f( 0f, -7f, 0f ) );
                        }
                    }
                    if (( entityA.getID().equals( "sidewayselevator1" ) || entityB.getID()
                                    .equals( "sidewayselevator1" ) )
                                    && (entityA.getID()
                                        .equals( "headingunder" )
                                    || entityB.getID()
                                        .equals( "headingunder" ))){
                        Vector3f reset = new Vector3f (-1.3f, -25.25f, 3.75f);
                        if (entityA.getID().equals( "sidewayselevator1" )){
                            elevatorshiftxlow(entityA, reset);
                        }else{
                            elevatorshiftxlow(entityB, reset);
                        }                        
                    }
                    if (( entityA.getID().equals( "sidewayselevator1" ) || entityB.getID()
                                    .equals( "sidewayselevator1" ) )
                                    && (entityA.getID()
                                        .equals( "underelevatorplatform1" )
                                    || entityB.getID()
                                        .equals( "underelevatorplatform1" ))){
                        Vector3f reset = new Vector3f (-21f, -25.25f, 3.75f);
                        if (entityA.getID().equals( "sidewayselevator1" )){
                            System.out.println(entityA.getID() +" "+ entityB.getID());
                            elevatorshiftxhigh(entityA, reset);
                        }else{
                            System.out.println(entityA.getID() +" "+ entityB.getID());
                            elevatorshiftxhigh(entityB, reset);
                        }                        
                    }
                    // TODO also, when a block is collided, check if they need
                    // to be "refrozen"
//                    if ( entityA.isFrozen() )
//                    {
//                        entityA.unfreeze();
//                        entityA.getRigidBody().translate( new Vector3f( 0f,
//                            5f,
//                            0f ) );
//                    }
//                    if ( entityB.isFrozen() )
//                    {
//                        entityB.unfreeze();
//                        entityA.getRigidBody().translate( new Vector3f( 0f,
//                            5f,
//                            0f ) );
//                    }
                    // Entites are known and exist, so we can act upon them
                    if ( entityA.getRigidBody()
                        .getCollisionShape()
                        .getName()
                        .equalsIgnoreCase( "sphere" ) )
                    {
                        if ( setGravity( entityB, new Vector3f( 0f, 30f, 0f ) ) )
                        {
                            // entityA.setGravity(new Vector3f(0f, 30f, 0f));
                            eventDispatcher.notify( new BlockDestroyedEvent( entityA ) );
                            dynamicsWorld.removeCollisionObject( entityA.getRigidBody() );
                            entityList.remove( entityA );
                            entityB.getRigidBody().translate( new Vector3f( 5f,
                                0f,
                                0f ) );
                        }
                    }
                    else if ( entityB.getRigidBody()
                        .getCollisionShape()
                        .getName()
                        .equalsIgnoreCase( "sphere" ) )
                    {
                        if ( setGravity( entityA, new Vector3f( 0f, 30f, 0f ) ) )
                        {
                            eventDispatcher.notify( new BlockDestroyedEvent( entityB ) );
                            dynamicsWorld.removeCollisionObject( entityB.getRigidBody() );
                            entityA.getRigidBody().translate( new Vector3f( 5f,
                                0f,
                                0f ) );
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
        public void onBlockCollisionResolved( BlockCollisionResolvedEvent event )
        {

            final PersistentManifold pm = event.getPersistentManifold();
            if ( pm.getBody0() instanceof RigidBody
                && pm.getBody1() instanceof RigidBody )
            {
                final Entity entityA = entityList.get( (RigidBody)pm.getBody0() );
                final Entity entityB = entityList.get( (RigidBody)pm.getBody1() );
                if ( entityA != null && entityB != null )
                {
//                    if ( entityA.getRigidBody().isActive()
//                        && entityB.getRigidBody().isActive() )
//                    {
//                        if ( entityA.getID().equalsIgnoreCase( "box" )
//                            && entityB.getID().equalsIgnoreCase( "box" ) )
//                        {
//                            // TODO block freeze event
//                            entityA.freeze();
//                            entityB.freeze();
//                        }
//                    }
                }
            }

        }


        public boolean setGravity( Entity target, Vector3f direction )
        {
            if ( !target.getRigidBody().isStaticObject() )
            {
                eventDispatcher.notify( new BlockPhysicsChangeEvent( target,
                    direction ) );
                target.getRigidBody().setGravity( direction );
                target.getRigidBody().activate();

                return true;
            }
            return false;
        }


        public void sendToRemote( Event<?> event )
        {
            if ( connected )
            {
                try
                {
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    final ObjectOutputStream oos = new ObjectOutputStream( baos );
                    oos.writeObject( new EventPackage( event ) );
                    oos.flush();
                    byte[] data = baos.toByteArray();
                    // System.out.println(data.length);
                    client.sendMessage( data );
                    oos.close();
                    baos.close();
                }
                catch ( IOException e )
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
        public CollisionStuff( CollisionConfiguration arg0 )
        {
            super( arg0 );
        }


        /**
         * Called when a new collision between objects occurred
         */
        @Override
        public PersistentManifold getNewManifold( Object b0, Object b1 )
        {
            // TODO maybe check and negate if one of the objects is frozen?
            // don't know the effect it would cause
            final PersistentManifold pm = super.getNewManifold( b0, b1 );
            // Throw event
            eventDispatcher.notify( new BlockCollisionEvent( pm ) );
            return pm;
        }


        /**
         * Called when a collision has been resolved: when the objects are no
         * longer in contact
         */
        @Override
        public void releaseManifold( PersistentManifold manifold )
        {
            // Throw event
            eventDispatcher.notify( new BlockCollisionResolvedEvent( manifold ) );
            super.releaseManifold( manifold );
        }
    }
    
    private void elevatorshiftxhigh(Entity a, Vector3f v){
        a.freeze();
        final RigidBody body = a.getRigidBody();
        final Vector3f vector = body.getCenterOfMassPosition(new Vector3f() );
        body.translate( new Vector3f(v.x - vector.x , v.y - vector.y , v.z - vector.z) );
        body
            .setGravity( new Vector3f( 7f, 0f, 0f ) );
    }
    
    private void elevatorshiftxlow(Entity a, Vector3f v){
        a.freeze();
        final RigidBody body = a.getRigidBody();
        final Vector3f vector = body.getCenterOfMassPosition(new Vector3f() );
        body.translate( new Vector3f(v.x - vector.x , v.y - vector.y , v.z - vector.z) );
        body
            .setGravity( new Vector3f( -7f, 0f, 0f ) );
        
    }
    
    private void elevatorshiftyhigh(Entity a, Vector3f v){
        a.freeze();
        final RigidBody body = a.getRigidBody();
        final Vector3f vector = body.getCenterOfMassPosition(new Vector3f() );
        body.translate( new Vector3f(v.x - vector.x , v.y - vector.y , v.z - vector.z) );
        body
            .setGravity( new Vector3f( 0f, 7f, 0f ) );
        
    }
    
    private void elevatorshiftylow(Entity a, Vector3f v){
        a.freeze();
        final RigidBody body = a.getRigidBody();
        final Vector3f vector = body.getCenterOfMassPosition(new Vector3f() );
        body.translate( new Vector3f(v.x - vector.x , v.y - vector.y , v.z - vector.z) );
        body
            .setGravity( new Vector3f( 0f, -7f, 0f ) );
        
    }
    
    private void elevatorshiftzhigh(Entity a, Vector3f v){
        a.freeze();
        final RigidBody body = a.getRigidBody();
        final Vector3f vector = body.getCenterOfMassPosition(new Vector3f() );
        body.translate( new Vector3f(v.x - vector.x , v.y - vector.y , v.z - vector.z) );
        body
            .setGravity( new Vector3f( 0f, 0f, 7f ) );
        
    }
    
    private void elevatorshiftzlow(Entity a, Vector3f v){
        a.freeze();
        final RigidBody body = a.getRigidBody();
        final Vector3f vector = body.getCenterOfMassPosition(new Vector3f() );
        body.translate( new Vector3f(v.x - vector.x , v.y - vector.y , v.z - vector.z) );
        body
            .setGravity( new Vector3f( 0f, 0f, -7f ) );
        
    }

}
