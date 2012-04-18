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

package com.ATeam.twoDotFiveD;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import com.ATeam.twoDotFiveD.world.BspYamlConverter;
import com.bulletphysics.BulletGlobals;
import com.bulletphysics.BulletStats;
import com.bulletphysics.util.ObjectArrayList;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.SimpleBroadphase;
import com.bulletphysics.collision.dispatch.CollisionAlgorithmCreateFunc;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConeShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.demos.opengl.DemoApplication;
import com.bulletphysics.demos.opengl.FastFormat;
import com.bulletphysics.demos.opengl.GLDebugDrawer;
import com.bulletphysics.demos.opengl.GLShapeDrawer;
import com.bulletphysics.demos.opengl.IGL;
import com.bulletphysics.demos.opengl.LWJGL;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
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

import demo.lwjgl.basic.GLApp;
import demo.lwjgl.basic.GLCam;
import demo.lwjgl.basic.GLCamera;

import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import lib.lwjgl.glmodel.GL_Vector;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import static com.bulletphysics.demos.opengl.IGL.*;


/**
 * BspDemo shows the convex collision detection, by converting a Quake BSP file
 * into convex objects and allowing interaction with boxes.
 * 
 * @author Vincent Deloso, Andrew Tucker,
 */
@SuppressWarnings("unused")

public class TwoDotFiveDBsp extends DemoApplication {
	Entity elevator1;
	Entity elevator2;
	Entity elevator3;
	Entity elevator4;
	Entity elevator5;
	private boolean dead = false;
	public KinematicCharacterController character;
	public PairCachingGhostObject ghostObject;
	private GLCam cam = new GLCam();
	private GLCamera camera = new GLCamera();

	GL_Vector ViewPoint;

	private float characterScale = 2f;
	private boolean down = true;
	private static int gForward = 0;
	private static int gBackward = 0;
	private static int gLeft = 0;
	private static int gRight = 0;
	private static int gJump = 0;

	private static TwoDotFiveDBsp demo;
	private float dt;

	private static final float CUBE_HALF_EXTENTS = 1;

	private static final float EXTRA_HEIGHT = -20f;

	// keep the collision shapes, for deletion/cleanup
	// Need to set this back to set / hashset
	public static Map<Entity, Entity> entityList = new HashMap<Entity, Entity>();
	public BroadphaseInterface broadphase;

	public CollisionDispatcher dispatcher;

	public ConstraintSolver solver;

	public DefaultCollisionConfiguration collisionConfiguration;

	private static EventDispatcher eventDispatcher = new EventDispatcher();

	private static EventDispatcher remoteDispatcher = new EventDispatcher();

	private static chatClient client;

	int count = 0;

    private static boolean connected = false;

    private Entity player;

	public TwoDotFiveDBsp(IGL gl) {
		super(gl);
	}

	public synchronized void initPhysics() throws Exception {
		// cameraUp.set(0f, 0f, 1f);
		// forwardAxis = 1;

		setCameraDistance(22f);
        collisionConfiguration = new DefaultCollisionConfiguration();
        // btCollisionShape* groundShape = new btBoxShape(btVector3(50,3,50));
        dispatcher = new CollisionStuff( collisionConfiguration );
        // the maximum size of the collision world. Make sure objects stay
        // within these boundaries. Don't make the world AABB size too large, it
        // will harm simulation quality and performance
        Vector3f worldMin = new Vector3f( -10f, -10f, -10f );
        Vector3f worldMax = new Vector3f( 10f, 10f, 10f );
        // maximum number of objects
        final int maxProxies = 10024;
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

        // here
        // TODO replace ghost object with rigid body and player can manipulate
        // that
        // using keys
        // http://bulletphysics.org/Bullet/phpBB3/viewtopic.php?t=7592
        Transform startTransform = new Transform();
        startTransform.setIdentity();
        // startTransform.origin.set(84.0f, 50.0f, -10.0f);
        CollisionShape colShape = new BoxShape( new Vector3f( 1, 1, 1 ) );
        float mass = 100f;
        Vector3f localInertia = new Vector3f( 0, 0, 0 );
        // colShape.calculateLocalInertia(mass, localInertia);
        startTransform.origin.set( 1, 2, 1 );
        DefaultMotionState myMotionState = new DefaultMotionState( startTransform );
        player = new Entity( mass,
            myMotionState,
            colShape,
            localInertia,
            "PLAYER",
            new Vector3f(1f, 1f, 1f),
            new String[]{""} );
        dynamicsWorld.addRigidBody( player );
        player.setActivationState( RigidBody.ISLAND_SLEEPING );
        entityList.put( player, player );
        dynamicsWorld.addRigidBody( player );
        player.setActivationState( RigidBody.ISLAND_SLEEPING );
        player.setWorldTransform( startTransform );
        player.setFriction( 5f );
        // entityList.put((Entity) player, (Entity)player );
        makeboxes();
        populate();

    }


    private void makeboxes()
    {
        Transform startTransform1 = new Transform();
        startTransform1.setIdentity();
        CollisionShape colShape1 = new BoxShape( new Vector3f( 1.25f,
            .25f,
            1.25f ) );
        float mass = 0f;
        startTransform1.origin.set( 80f, 2.5f, -10 );
        elevator1 = localCreateEntity( mass,
            startTransform1,
            colShape1,
            "elevatorsmash1",
            new Vector3f(1f, 0f, 0f),
            null );
        dynamicsWorld.addRigidBody( elevator1 );
        elevator1.setWorldTransform( startTransform1 );
        elevator1.setFriction( 5f );
        entityList.put( elevator1, elevator1 );

        Transform startTransform2 = new Transform();
        startTransform2.setIdentity();
        CollisionShape colShape2 = new BoxShape( new Vector3f( 1.25f,
            .25f,
            1.25f ) );
        startTransform2.origin.set( 85f, 2.5f, 20 );
        elevator2 = localCreateEntity( mass,
            startTransform2,
            colShape2,
            "elevatorsmash1",
            new Vector3f(1f, 0f, 0f),
            null );
        dynamicsWorld.addRigidBody( elevator2 );
        elevator2.setWorldTransform( startTransform2 );
        elevator2.setFriction( 5f );
        entityList.put( elevator2, elevator2 );

        Transform startTransform3 = new Transform();
        startTransform3.setIdentity();
        CollisionShape colShape3 = new BoxShape( new Vector3f( 1.25f,
            .25f,
            1.25f ) );
        startTransform3.origin.set( 85f, 2.5f, 10 );
        elevator3 = localCreateEntity( mass,
            startTransform3,
            colShape3,
            "elevatorsmash1",
            new Vector3f(1f, 0f, 0f),
            null );
        dynamicsWorld.addRigidBody( elevator3 );
        elevator3.setWorldTransform( startTransform3 );
        elevator3.setFriction( 5f );
        entityList.put( elevator3, elevator3 );

        Transform startTransform4 = new Transform();
        startTransform4.setIdentity();
        CollisionShape colShape4 = new BoxShape( new Vector3f( 1.25f,
            .25f,
            1.25f ) );
        startTransform4.origin.set( 85f, 2.5f, 0 );
        elevator4 = localCreateEntity( mass,
            startTransform4,
            colShape4,
            "elevatorsmash1",
            new Vector3f(1f, 0f, 0f),
            null );
        dynamicsWorld.addRigidBody( elevator4 );
        elevator4.setWorldTransform( startTransform4 );
        elevator4.setFriction( 5f );
        entityList.put( elevator4, elevator4 );

        Transform startTransform5 = new Transform();
        startTransform5.setIdentity();
        CollisionShape colShape5 = new BoxShape( new Vector3f( 1.25f,
            .25f,
            1.25f ) );
        startTransform5.origin.set( 85f, 2.5f, -8 );
        elevator5 = localCreateEntity( mass,
            startTransform5,
            colShape5,
            "elevatorsmash1",
            new Vector3f(1f, 0f, 0f),
            null );
        dynamicsWorld.addRigidBody( elevator5 );
        elevator5.setWorldTransform( startTransform5 );
        elevator5.setFriction( 5f );
        entityList.put( elevator5, elevator5 );
    }


    public synchronized Entity localCreateEntity(
        float mass,
        Transform startTransform,
        CollisionShape shape,
        String ID,
        Vector3f image,
        String[] description )
    {
        // rigidbody is dynamic if and only if mass is non zero, otherwise
        // static
        boolean isDynamic = ( mass != 0f );

        Vector3f localInertia = new Vector3f( 0f, 0f, 0f );
        if ( isDynamic )
        {
            shape.calculateLocalInertia( mass, localInertia );
        }

        // using motionstate is recommended, it provides interpolation
        // capabilities, and only synchronizes 'active' objects
        DefaultMotionState myMotionState = new DefaultMotionState( startTransform );
        Entity e = new Entity( mass,
            myMotionState,
            shape,
            localInertia,
            ID,
            image,
            description );
        dynamicsWorld.addRigidBody( e );

        // Dynamic gravity for object
        if ( !bodyGravityType.equals( "NORMAL" ) )
        {
            if ( bodyGravityType.equals( "ANTIGRAVITY" ) )
            {
                e.setGravity( new Vector3f( 0f, 30f, 0f ) );
            }
            else if ( bodyGravityType.equals( "STASIS" ) )
            {
                e.setGravity( new Vector3f( 0f, 0f, 0f ) );
            }
        }
        return e;
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
        // resetScene();
    }


    public void resetScene()
    {
        BulletStats.gNumDeepPenetrationChecks = 0;
        BulletStats.gNumGjkChecks = 0;

        int numObjects = 0;
        if ( dynamicsWorld != null )
        {
            dynamicsWorld.stepSimulation( 1f / 60f, 0 );
            numObjects = dynamicsWorld.getNumCollisionObjects();
        }

        for ( int i = 0; i < numObjects; i++ )
        {
            CollisionObject colObj = dynamicsWorld.getCollisionObjectArray()
                .getQuick( i );
            RigidBody body = RigidBody.upcast( colObj );
            if ( body != null )
            {
                if ( body.getMotionState() != null )
                {
                    DefaultMotionState myMotionState = (DefaultMotionState)body.getMotionState();
                    myMotionState.graphicsWorldTrans.set( myMotionState.startWorldTrans );
                    colObj.setWorldTransform( myMotionState.graphicsWorldTrans );
                    colObj.setInterpolationWorldTransform( myMotionState.startWorldTrans );
                    colObj.activate();
                }
                // removed cached contact points
                dynamicsWorld.getBroadphase()
                    .getOverlappingPairCache()
                    .cleanProxyFromPairs( colObj.getBroadphaseHandle(),
                        dynamicsWorld.getDispatcher() );

                body = RigidBody.upcast( colObj );
                if ( body != null && !body.isStaticObject() )
                {
                    RigidBody.upcast( colObj )
                        .setLinearVelocity( new Vector3f( 0f, 0f, 0f ) );
                    RigidBody.upcast( colObj )
                        .setAngularVelocity( new Vector3f( 0f, 0f, 0f ) );
                }
            }
        }
    }

    @Override
    public void renderme()
    {
        updateCamera();

        if ( dynamicsWorld != null )
        {
            int numObjects = dynamicsWorld.getNumCollisionObjects();
            wireColor.set( 1f, 0f, 0f );
            for ( int i = 0; i < numObjects; i++ )
            {
                CollisionObject colObj = dynamicsWorld.getCollisionObjectArray()
                    .getQuick( i );
                RigidBody body = RigidBody.upcast( colObj );

                // System.out.println(colObj.getWorldTransform(new
                // Transform()).origin);

                // if (body != null && body.getMotionState() != null)
                // {
                // DefaultMotionState myMotionState = (DefaultMotionState) body
                // .getMotionState();
                // m.set(myMotionState.graphicsWorldTrans);
                // }
                // else
                // {
                // colObj.getWorldTransform(m);
                // }
                colObj.getWorldTransform( m );
                // System.out.println(m.origin);

                wireColor.set( 1f, 1f, 0.5f ); // wants deactivation
                if ( ( i & 1 ) != 0 )
                {
                    wireColor.set( 0f, 0f, 1f );
                }

                // color differently for active, sleeping, wantsdeactivation
                // states
                if ( colObj.getActivationState() == 1 ) // active
                {
                    if ( ( i & 1 ) != 0 )
                    {
                        // wireColor.add(new Vector3f(1f, 0f, 0f));
                        wireColor.x += 1f;
                    }
                    else
                    {
                        // wireColor.add(new Vector3f(0.5f, 0f, 0f));
                        wireColor.x += 0.5f;
                    }
                }
                if ( colObj.getActivationState() == 2 ) // ISLAND_SLEEPING
                {
                    if ( ( i & 1 ) != 0 )
                    {
                        // wireColor.add(new Vector3f(0f, 1f, 0f));
                        wireColor.y += 1f;
                    }
                    else
                    {
                        // wireColor.add(new Vector3f(0f, 0.5f, 0f));
                        wireColor.y += 0.5f;
                    }
                }
                
                Vector3f colorVec = new Vector3f(0f, 0f, 0f);
                for(Entity e : entityList.values()){
                 if(e.getCollisionShape().equals( colObj.getCollisionShape() ))
                 {
                     colorVec = e.getImage();
                }

                int a = 0;
                GLShapeDrawer.drawOpenGL( gl,
                    m,
                    colObj.getCollisionShape(),
                    wireColor,
                    getDebugMode(), colorVec );
            }

            float xOffset = 10f;
            float yStart = 20f;
            float yIncr = 20f;

            gl.glDisable( GL_LIGHTING );
            gl.glColor3f( 0f, 0f, 0f );

            if ( ( debugMode & DebugDrawModes.NO_HELP_TEXT ) == 0 )
            {
                setOrthographicProjection();

                yStart = showProfileInfo( xOffset, yStart, yIncr );

                // #ifdef SHOW_NUM_DEEP_PENETRATIONS
                buf.setLength( 0 );
                buf.append( "gNumDeepPenetrationChecks = " );
                FastFormat.append( buf, BulletStats.gNumDeepPenetrationChecks );
                drawString( buf,
                    Math.round( xOffset ),
                    Math.round( yStart ),
                    TEXT_COLOR );
                yStart += yIncr;

                buf.setLength( 0 );
                buf.append( "gNumGjkChecks = " );
                FastFormat.append( buf, BulletStats.gNumGjkChecks );
                drawString( buf,
                    Math.round( xOffset ),
                    Math.round( yStart ),
                    TEXT_COLOR );
                yStart += yIncr;

                buf.setLength( 0 );
                buf.append( "gNumSplitImpulseRecoveries = " );
                FastFormat.append( buf, BulletStats.gNumSplitImpulseRecoveries );
                drawString( buf,
                    Math.round( xOffset ),
                    Math.round( yStart ),
                    TEXT_COLOR );
                yStart += yIncr;

                // buf = String.format("gNumAlignedAllocs = %d",
                // BulletGlobals.gNumAlignedAllocs);
                // TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
                // yStart += yIncr;

                // buf = String.format("gNumAlignedFree= %d",
                // BulletGlobals.gNumAlignedFree);
                // TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
                // yStart += yIncr;

                // buf = String.format("# alloc-free = %d",
                // BulletGlobals.gNumAlignedAllocs -
                // BulletGlobals.gNumAlignedFree);
                // TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
                // yStart += yIncr;

                // enable BT_DEBUG_MEMORY_ALLOCATIONS define in
                // Bullet/src/LinearMath/btAlignedAllocator.h for memory leak
                // detection
                // #ifdef BT_DEBUG_MEMORY_ALLOCATIONS
                // glRasterPos3f(xOffset,yStart,0);
                // sprintf(buf,"gTotalBytesAlignedAllocs = %d",gTotalBytesAlignedAllocs);
                // BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
                // yStart += yIncr;
                // #endif //BT_DEBUG_MEMORY_ALLOCATIONS

                if ( getDynamicsWorld() != null )
                {
                    buf.setLength( 0 );
                    buf.append( "# objects = " );
                    FastFormat.append( buf,
                        getDynamicsWorld().getNumCollisionObjects() );
                    drawString( buf,
                        Math.round( xOffset ),
                        Math.round( yStart ),
                        TEXT_COLOR );
                    yStart += yIncr;

                    buf.setLength( 0 );
                    buf.append( "# pairs = " );
                    FastFormat.append( buf, getDynamicsWorld().getBroadphase()
                        .getOverlappingPairCache()
                        .getNumOverlappingPairs() );
                    drawString( buf,
                        Math.round( xOffset ),
                        Math.round( yStart ),
                        TEXT_COLOR );
                    yStart += yIncr;

                }
                // #endif //SHOW_NUM_DEEP_PENETRATIONS

                // JAVA NOTE: added
                int free = (int)Runtime.getRuntime().freeMemory();
                int total = (int)Runtime.getRuntime().totalMemory();
                buf.setLength( 0 );
                buf.append( "heap = " );
                FastFormat.append( buf, (float)( total - free )
                    / ( 1024 * 1024 ) );
                buf.append( " / " );
                FastFormat.append( buf, (float)( total ) / ( 1024 * 1024 ) );
                buf.append( " MB" );
                drawString( buf,
                    Math.round( xOffset ),
                    Math.round( yStart ),
                    TEXT_COLOR );
                yStart += yIncr;

                resetPerspectiveProjection();
            }

            gl.glEnable( GL_LIGHTING );
        }

        updateCamera();
    }
    }

    @Override
    public void updateCamera()
    {

        gl.glMatrixMode( GL_PROJECTION );
        gl.glLoadIdentity();
        // System.out.println(cameraTargetPosition);
        float rele = ele * 0.01745329251994329547f; // rads per deg
        float razi = azi * 0.01745329251994329547f; // rads per deg

        Quat4f rot = new Quat4f();
        QuaternionUtil.setRotation( rot, cameraUp, razi );

        Vector3f eyePos = new Vector3f();
        eyePos.set( 0f, 0f, 0f );
        VectorUtil.setCoord( eyePos, forwardAxis, -cameraDistance );

        Vector3f forward = new Vector3f();
        forward.set( eyePos.x, eyePos.y, eyePos.z );
        if ( forward.lengthSquared() < BulletGlobals.FLT_EPSILON )
        {
            forward.set( 1f, 0f, 0f );
        }
        Vector3f right = new Vector3f();
        right.cross( cameraUp, forward );
        Quat4f roll = new Quat4f();
        QuaternionUtil.setRotation( roll, right, -rele );

        Matrix3f tmpMat1 = new Matrix3f();
        Matrix3f tmpMat2 = new Matrix3f();
        tmpMat1.set( rot );
        tmpMat2.set( roll );
        tmpMat1.mul( tmpMat2 );
        tmpMat1.transform( eyePos );

        cameraPosition.set( eyePos );

        if ( glutScreenWidth > glutScreenHeight )
        {
            float aspect = glutScreenWidth / (float)glutScreenHeight;
            gl.glFrustum( -aspect, aspect, -1.0, 1.0, 1.0, 10000.0 );
        }
        else
        {
            float aspect = glutScreenHeight / (float)glutScreenWidth;
            gl.glFrustum( -1.0, 1.0, -aspect, aspect, 1.0, 10000.0 );
        }

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity();
        // System.out.println("camUP "+cameraUp);

        cam.updatePan( cam.getDirection() );
        ViewPoint = GL_Vector.add( camera.Position, camera.ViewDir );
        cameraPosition.x = camera.Position.x;
        cameraPosition.y = camera.Position.y;
        cameraPosition.z = camera.Position.z;
        cameraTargetPosition.x = ViewPoint.x;
        cameraTargetPosition.y = ViewPoint.y;
        cameraTargetPosition.z = ViewPoint.z;
        cameraUp.x = camera.UpVector.x;
        cameraUp.y = camera.UpVector.y;
        cameraUp.z = camera.UpVector.z;

        for ( CollisionObject o : dynamicsWorld.getCollisionObjectArray() )
        {
            if ( o.equals( player ) )
            {
                // System.out.println("player found");
                Transform t = o.getWorldTransform( new Transform() );
                // System.out.println(t.origin);
                gl.gluLookAt( cameraPosition.x + t.origin.x,
                    cameraPosition.y + t.origin.y,
                    cameraPosition.z + t.origin.z,
                    t.origin.x,
                    t.origin.y,
                    t.origin.z,
                    cameraUp.x,
                    cameraUp.y,
                    cameraUp.z );
                if ( t.origin.y < -30 )
                {
                    // TODO do something...
                    System.out.println( "Fail condition!!" );
                    Vector3f splode = new Vector3f( 0f, 1f, 0f );
                    shootBox( splode );
                }
                if ( t.origin.y < -31 )
                {
                    t.origin.set( 1, 2, 1 );
                    o.setWorldTransform( t );
                }
                break;
            }
        }
        if ( down )
        {
            elevator1.setLinearVelocity( new Vector3f( 0, 0, 0 ) );
            elevator2.setLinearVelocity( new Vector3f( 0, 0, 0 ) );
            elevator3.setLinearVelocity( new Vector3f( 0, 0, 0 ) );
            elevator4.setLinearVelocity( new Vector3f( 0, 0, 0 ) );
            elevator5.setLinearVelocity( new Vector3f( 0, 0, 0 ) );
            Transform one = elevator1.getWorldTransform( new Transform() );
            float y = one.origin.y - ( 1f * dt );
            if ( y <= -3.5f )
            {
                down = false;
            }
            one.origin.set( 80f, y, -10f );
            Transform two = elevator2.getWorldTransform( new Transform() );
            two.origin.set( 85f, y, 20f );
            Transform three = elevator3.getWorldTransform( new Transform() );
            three.origin.set( 85f, y, 10f );
            Transform four = elevator4.getWorldTransform( new Transform() );
            four.origin.set( 85f, y, 0f );
            Transform five = elevator5.getWorldTransform( new Transform() );
            five.origin.set( 85f, y, -8f );
            elevator1.setWorldTransform( one );
            elevator2.setWorldTransform( two );
            elevator3.setWorldTransform( three );
            elevator4.setWorldTransform( four );
            elevator5.setWorldTransform( five );
        }
        else
        {
            elevator1.setLinearVelocity( new Vector3f( 0, 0, 0 ) );
            elevator2.setLinearVelocity( new Vector3f( 0, 0, 0 ) );
            elevator3.setLinearVelocity( new Vector3f( 0, 0, 0 ) );
            elevator4.setLinearVelocity( new Vector3f( 0, 0, 0 ) );
            elevator5.setLinearVelocity( new Vector3f( 0, 0, 0 ) );
            Transform one = elevator1.getWorldTransform( new Transform() );
            float y = one.origin.y + ( 1f * dt );
            if ( y >= 2.5f )
            {
                down = true;
            }
            one.origin.set( 80f, y, -10f );
            Transform two = elevator2.getWorldTransform( new Transform() );
            two.origin.set( 85f, y, 20f );
            Transform three = elevator3.getWorldTransform( new Transform() );
            three.origin.set( 85f, y, 10f );
            Transform four = elevator4.getWorldTransform( new Transform() );
            four.origin.set( 85f, y, 0f );
            Transform five = elevator5.getWorldTransform( new Transform() );
            five.origin.set( 85f, y, -8f );
            elevator1.setWorldTransform( one );
            elevator2.setWorldTransform( two );
            elevator3.setWorldTransform( three );
            elevator4.setWorldTransform( four );
            elevator5.setWorldTransform( five );
        }

        // gl.gluLookAt( eyex, eyey, eyez, RigidBody.upcast( ghostObject
        // ).getCenterOfMassPosition( new Vector3f() ).x, centery, centerz, upx,
        // upy, upz );
    }

	@Override
	public synchronized void clientMoveAndDisplay() {
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		dt = getDeltaTimeMicroseconds() * 0.000001f;
		try {
			player.activate(true);
			// TODO May need custom DynamicsWorld to catch exceptions per step
			int maxSimSubSteps = idle ? 1 : 2;
			if (gLeft != 0) {
				player.activate(true);
				Vector3f curr = new Vector3f();
				player.getLinearVelocity(curr);
				player.setLinearVelocity(new Vector3f(-20f, curr.y, curr.z));
			}

			if (gRight != 0) {
				player.activate(true);
				Vector3f curr = new Vector3f();
				player.getLinearVelocity(curr);
				player.setLinearVelocity(new Vector3f(20f, curr.y, curr.z));

			}
			if (gForward != 0) {
				player.activate(true);
				Vector3f curr = new Vector3f();
				player.getLinearVelocity(curr);
				player.setLinearVelocity(new Vector3f(curr.x, curr.y, -20f));
			}
			if (gBackward != 0) {
				player.activate(true);
				Vector3f curr = new Vector3f();
				player.getLinearVelocity(curr);
				player.setLinearVelocity(new Vector3f(curr.x, curr.y, 20f));
			}

			// TODO
			// fix this ish
			if (gJump != 0) {
				player.activate(true);
				Vector3f curr = new Vector3f();
				player.getLinearVelocity(curr);
				if (curr.y <= .5) {
					Vector3f jump = new Vector3f(0, 10, 0);
					player.setLinearVelocity(new Vector3f(curr.x + jump.x,
							curr.y + jump.y, curr.z + jump.z));
				}

			}
			dynamicsWorld.stepSimulation(dt);
			// optional but useful: debug drawing
			dynamicsWorld.debugDrawWorld();
		} catch (NullPointerException e) {
			System.out.println("Simulation had null at some point");
			// WARN this is very serious
			// TODO figure out how to fix this...
		} catch (ArrayIndexOutOfBoundsException arr) {
			System.out.println("Index Out of Bounds in Simulation");
		}

		// optional but useful: debug drawing
		cam.resetClock();
		dynamicsWorld.debugDrawWorld();
		renderme();

		// glFlush();
		// glutSwapBuffers();

	}

	@Override
	public void displayCallback() {
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		renderme();

		// glFlush();
		// glutSwapBuffers();
	}

	private StringBuilder buf = new StringBuilder();
	private final Transform m = new Transform();
	private Vector3f wireColor = new Vector3f();
	protected Color3f TEXT_COLOR = new Color3f(0f, 0f, 0f);

	public void setup() {
		cam.setCamera(camera);
		camera.setCamera(0f, 0f, 15f, 0f, 0f, -1f, 0f, 1f, 0f);

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
		case Keyboard.KEY_SPACE: {
			gJump = 0;
			break;
		}
		}
	}

	@Override
	public synchronized void specialKeyboard(int key, int x, int y,
			int modifiers) {
		switch (key) {
		case Keyboard.KEY_SPACE: {
			gJump = 1;
			break;
		}
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

		case Keyboard.KEY_R: {
			// Remove all objects
			for (CollisionObject a : dynamicsWorld.getCollisionObjectArray()
					.toArray(new CollisionObject[0])) {
				Entity e = null;
				for (RigidBody r : entityList.keySet()) {
					if (r.getCollisionShape().equals(a.getCollisionShape())) {
						e = entityList.get(r);
						break;
					}
				}
				try {
					dynamicsWorld.removeCollisionObject(a);
					if (e != null) {
						eventDispatcher.notify(new BlockDestroyedEvent(e));
						entityList.remove(e);
					}
				} catch (NullPointerException n) {
					System.out
							.println("Tried to remove object that is not there");
				} catch (ArrayIndexOutOfBoundsException b) {
					System.out.println("ArrayIndexOutOfBounds in simulation");
				}
			}
			// repopulate world
			populate();
			break;
		}
		default: {
			super.specialKeyboard(key, x, y, modifiers);
			break;
		}
		}
	}
	
	@Override
	public synchronized void shootBox(Vector3f destination) {
		if (dynamicsWorld != null) {
			float mass = 50f;
			Transform startTransform = new Transform();
			startTransform.setIdentity();
			Vector3f camPos = new Vector3f(getCameraPosition());
			// Fix stuff?
			Vector3f shootfrom = player.getCenterOfMassPosition(new Vector3f());
			shootfrom.y = shootfrom.y + 1;
			startTransform.origin.set(shootfrom);

			if (shapeType.equals("BOX")) {
				shootBoxShape = new BoxShape(new Vector3f(1f, 1f, 1f));
			} else if (shapeType.equals("SPHERE")) {
				shootBoxShape = new SphereShape(1f);
			} else if (shapeType.equals("TRIANGLE")) {
				shootBoxShape = new ConeShape(1f, 3f);
			} else if (shapeType.equals("CYLINDER")) {
				shootBoxShape = new CylinderShape(new Vector3f(1f, 1f, 1f));
			}

            Vector3f linVel = new Vector3f( destination.x - camPos.x,
                destination.y - camPos.y,
                destination.z - camPos.z );
            linVel.normalize();
            linVel.scale( ShootBoxInitialSpeed );
            final Random r = new Random();
            Entity entity = localCreateEntity( mass,
                startTransform,
                shootBoxShape,
                shootBoxShape.getName() + r.nextFloat(),
                new Vector3f(1f, 1f, 1f),
                null );
            Transform worldTrans = entity.getWorldTransform( new Transform() );
            worldTrans.origin.set( shootfrom );
            worldTrans.setRotation( new Quat4f( 0f, 0f, 0f, 1f ) );
            entity.setWorldTransform( worldTrans );

            entity.setLinearVelocity( linVel );
            entity.setAngularVelocity( new Vector3f( 0f, 0f, 0f ) );

            entity.setCcdMotionThreshold( 1f );
            entity.setCcdSweptSphereRadius( 0.2f );
            // Dynamic gravity for object
            // TODO consolidate setgravity into one method, let entity set its
            // tied rigidbody gravity
            if ( !bodyGravityType.equals( "NORMAL" ) )
            {
                if ( bodyGravityType.equals( "ANTIGRAVITY" ) )
                {
                    entity.setEntityGravity( new Vector3f( 0f, 30f, 0f ) );
                }
                else if ( bodyGravityType.equals( "STASIS" ) )
                {
                    entity.setEntityGravity( new Vector3f( 0f, 0f, 0f ) );
                }
            }
            else
            {
                entity.setEntityGravity( dynamicsWorld.getGravity( new Vector3f() ) );
            }
            entityList.put( entity, entity );
            eventDispatcher.notify( new BlockCreateEvent( entity ) );
        }
    }


    public static void main( String[] args ) throws Exception
    {
        demo = new TwoDotFiveDBsp( LWJGL.getGL() );
//         try
//         {
//         client = new chatClient( null,
//         "137.155.2.153",
//         "BASE",
//         remoteDispatcher );
//         if ( client.connect() )
//         {
//         client.start();
//         connected = true;
//         }
//         Thread.sleep( 2000 );
//         }
//         catch ( Exception e )
//         {
//         // No networking
//         connected = false;
//         }
        demo.initListener();
        demo.setup();
        demo.initPhysics();
        demo.getDynamicsWorld()
            .setDebugDrawer( new GLDebugDrawer( LWJGL.getGL() ) );
        // demo.debugMode = 1;
        LWJGL.main( args,
            Config.displayWidth,
            Config.displayHeight,
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
                    float mass = ( 1f / event.getEntity().getInvMass() );
                    // System.out.println("Event mass: " + mass);
                    // System.out.println("Event transform: " +
                    // event.getEntity().getRigidBody().getWorldTransform(new
                    // Transform()).toString());
                    // System.out.println("Event CollisionShape: " +
                    // event.getEntity().getRigidBody().getCollisionShape().toString());
                    Entity entity = localCreateEntity( mass,
                        event.getEntity().getWorldTransform( new Transform() ),
                        event.getEntity().getCollisionShape(),
                        event.getEntity().getID(),
                        event.getEntity().getImage(),
                        new String[] { "" } );
                    entity.setAngularFactor( event.getEntity()
                        .getAngularFactor() );
                    entity.setAngularVelocity( event.getEntity()
                        .getAngularVelocity( new Vector3f() ) );
                    entity.setLinearVelocity( event.getEntity()
                        .getLinearVelocity( new Vector3f() ) );
                    entity.setDamping( event.getEntity().getLinearDamping(),
                        event.getEntity().getAngularDamping() );
                    entity.setEntityGravity( event.getEntity()
                        .getEntityGravity() );
                    // System.out.println("Remote gravity: "
                    // +event.getEntity().getGravity());
                    // System.out.println(event.getEntity().getRigidBody().getLinearVelocity(new
                    // Vector3f()).toString());
                    entityList.put( entity, entity );
                    // System.out.println("Added block");
                }


                @Override
                public synchronized void onBlockDestroyed(
                    BlockDestroyedEvent event )
                {
                    // System.out.println("Received destroyed event");
                    for ( Entity e : entityList.values() )
                    {
                        if ( e.getID().equals( event.getEntity().getID() ) )
                        {
                            final CollisionShape shape = e.getCollisionShape();
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
                                catch ( NullPointerException n )
                                {
                                    System.out.println( "Attempted to remove object taht no longer exists." );
                                }
                                catch ( ArrayIndexOutOfBoundsException a )
                                {
                                    System.out.println( "Attempted to remove object taht no longer exists." );
                                }
                            }
                            break;
                        }
                    }
                }
            };
            remoteDispatcher.registerListener( Type.BLOCK_CREATE,
                remoteListener );
            remoteDispatcher.registerListener( Type.BLOCK_DESTROYED,
                remoteListener );
        }
        MusicPlayer mp = new MusicPlayer( eventDispatcher );
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
            Vector3f image,
            String[] description )
        {
            Transform startTransform = new Transform();
            // can use a shift
            startTransform.setIdentity();
            startTransform.origin.set( 0, 0, 0 );

            // this create an internal copy of the vertices
            CollisionShape shape = new ConvexHullShape( vertices );
            // body.setActivationState(RigidBody.ACTIVE_TAG);
            final Transform center = new Transform();
            center.setIdentity();
            center.origin.set( 1f, 1f, 1f );
            addEntity( mass,
                center,
                shape,
                name,
                image,
                description,
                acceleration );
        }


        @Override
        public void addShapeCollider(
            String name,
            String type,
            Vector3f localscaling,
            Vector3f transform,
            float mass,
            Vector3f acceleration,
            Vector3f image,
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
            addEntity( mass,
                origin,
                shape,
                name,
                image,
                description,
                acceleration );
        }


        public void addEntity(
            float mass,
            Transform origin,
            CollisionShape shape,
            String name,
            Vector3f image,
            String[] description,
            Vector3f acceleration )
        {
            Entity e = localCreateEntity( mass,
                origin,
                shape,
                name,
                image,
                description );
            if ( acceleration != null )
            {
                e.setEntityGravity( acceleration );
            }
            else
            {
                // set default gravity;
                e.setGravity( dynamicsWorld.getGravity( new Vector3f() ) );
            }
            entityList.put( e, e );
            eventDispatcher.notify( new BlockCreateEvent( e ) );
        }
    }

	public class BlockCollisionListener extends BlockListener {
		@Override
		public void onBlockCreate(BlockCreateEvent event) {
			sendToRemote(event);
		}

		@Override
		public void onBlockDestroyed(BlockDestroyedEvent event) {
			sendToRemote(event);
		}

		@Override
		public synchronized void onBlockCollision(BlockCollisionEvent event) {
			final PersistentManifold pm = event.getPersistentManifold();
			if (pm.getBody0() instanceof RigidBody
					&& pm.getBody1() instanceof RigidBody) {
				final Entity entityA = entityList
						.get((RigidBody) pm.getBody0());
				final Entity entityB = entityList
						.get((RigidBody) pm.getBody1());
				if (entityA != null && entityB != null) {
					// if ( entityA.getID().equals( "object2" ) )
					// {
					// entityA.getRigidBody().setAngularFactor( 0f );
					// entityA.getRigidBody()
					// .setAngularVelocity( new Vector3f( 0f, 0f, 0f ) );
					// entityA.getRigidBody()
					// .setLinearVelocity( new Vector3f( 0f, 0f, 0f ) );
					// entityA.getRigidBody().setGravity( new Vector3f( 0f,
					// 10f,
					// 0f ) );
					// System.out.println( "OMG" );
					// }
					// else if ( entityB.getID().equals( "object2" ) )
					// {
					// entityB.getRigidBody().setAngularFactor( 0f );
					// entityB.getRigidBody()
					// .setAngularVelocity( new Vector3f( 0f, 0f, 0f ) );
					// entityB.getRigidBody()
					// .setLinearVelocity( new Vector3f( 0f, 0f, 0f ) );
					// entityB.getRigidBody().setGravity( new Vector3f( 0f,
					// 10f,
					// 0f ) );
					// System.out.println( "OMA" );
					// }
					// TODO also, when a block is collided, check if they need
					// to be "refrozen"
					// if ( entityA.isFrozen() )
					// {
					// entityA.unfreeze();
					// entityA.getRigidBody().translate( new Vector3f( 0f,
					// 5f,
					// 0f ) );
					// }
					// if ( entityB.isFrozen() )
					// {
					// entityB.unfreeze();
					// entityA.getRigidBody().translate( new Vector3f( 0f,
					// 5f,
					// 0f ) );
					// }
					// Entites are known and exist, so we can act upon them
					if (entityA.getCollisionShape().getName()
							.equalsIgnoreCase("sphere")) {
						if (setGravity(entityB, new Vector3f(0f, 30f, 0f))) {
							// entityA.setGravity(new Vector3f(0f, 30f, 0f));
							eventDispatcher.notify(new BlockDestroyedEvent(
									entityA));
							dynamicsWorld.removeCollisionObject(entityA);
							entityList.remove(entityA);
							entityB.translate(new Vector3f(5f, 0f, 0f));
						}
					} else if (entityB.getCollisionShape().getName()
							.equalsIgnoreCase("sphere")) {
						if (setGravity(entityA, new Vector3f(0f, 30f, 0f))) {
							eventDispatcher.notify(new BlockDestroyedEvent(
									entityB));
							dynamicsWorld.removeCollisionObject(entityB);
							entityA.translate(new Vector3f(5f, 0f, 0f));
						}
					} else {
						// System.out.println("objA: " + entityA.getID()
						// + " objB: " + entityB.getID());
					}
				} else {
					// TODO Somehow it is known.... how to handle?
				}
			}
		}

		public boolean setGravity(Entity target, Vector3f direction) {
			if (!target.isStaticObject()) {
				eventDispatcher.notify(new BlockPhysicsChangeEvent(target,
						direction));
				target.setEntityGravity(direction);
				target.activate();
				return true;
			}
			return false;
		}
		
		public void sendToRemote(Event<?> event) {
			if (connected) {
				try {
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					final ObjectOutputStream oos = new ObjectOutputStream(baos);
					oos.writeObject(new EventPackage(event));
					oos.flush();
					byte[] data = baos.toByteArray();
					// System.out.println(data.length);
					client.sendMessage(data);
					oos.close();
					baos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public class CollisionStuff extends CollisionDispatcher {
	    
	    public CollisionStuff(CollisionConfiguration arg0)
	    {
	        super(arg0);
	    }

		/**
		 * Called when a new collision between objects occurred
		 */
		@Override
		public PersistentManifold getNewManifold(Object b0, Object b1) {
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
		public void releaseManifold(PersistentManifold manifold) {
			// Throw event
			eventDispatcher.notify(new BlockCollisionResolvedEvent(manifold));
			super.releaseManifold(manifold);
		}
	}
}