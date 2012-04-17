///*
// * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
// * 
// * Bullet Continuous Collision Detection and Physics Library Copyright (c)
// * 2003-2008 Erwin Coumans http://www.bulletphysics.com/
// * 
// * This software is provided 'as-is', without any express or implied warranty.
// * In no event will the authors be held liable for any damages arising from the
// * use of this software.
// * 
// * Permission is granted to anyone to use this software for any purpose,
// * including commercial applications, and to alter it and redistribute it
// * freely, subject to the following restrictions:
// * 
// * 1. The origin of this software must not be misrepresented; you must not claim
// * that you wrote the original software. If you use this software in a product,
// * an acknowledgment in the product documentation would be appreciated but is
// * not required. 2. Altered source versions must be plainly marked as such, and
// * must not be misrepresented as being the original software. 3. This notice may
// * not be removed or altered from any source distribution.
// */
//
//package com.bulletphysics.demos.bsp;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.ObjectOutputStream;
//import java.io.OutputStream;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.logging.Level;
//
//import com.ATeam.twoDotFiveD.chatclient.chatClient;
//import com.ATeam.twoDotFiveD.debug.Logging;
//import com.ATeam.twoDotFiveD.entity.Entity;
//import com.ATeam.twoDotFiveD.event.Event;
//import com.ATeam.twoDotFiveD.event.Event.Type;
//import com.ATeam.twoDotFiveD.event.EventDispatcher;
//import com.ATeam.twoDotFiveD.event.block.BlockCollisionEvent;
//import com.ATeam.twoDotFiveD.event.block.BlockCollisionResolvedEvent;
//import com.ATeam.twoDotFiveD.event.block.BlockCreateEvent;
//import com.ATeam.twoDotFiveD.event.block.BlockDestroyedEvent;
//import com.ATeam.twoDotFiveD.event.block.BlockListener;
//import com.ATeam.twoDotFiveD.event.block.BlockPhysicsChangeEvent;
//import com.ATeam.twoDotFiveD.music.MusicPlayer;
//import com.ATeam.twoDotFiveD.udp.Client.EventPackage;
//import com.bulletphysics.BulletGlobals;
//import com.bulletphysics.BulletStats;
//import com.bulletphysics.util.ObjectArrayList;
//import com.bulletphysics.collision.broadphase.AxisSweep3;
//import com.bulletphysics.collision.broadphase.BroadphaseInterface;
//import com.bulletphysics.collision.broadphase.BroadphaseNativeType;
//import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
//import com.bulletphysics.collision.broadphase.DbvtBroadphase;
//import com.bulletphysics.collision.broadphase.SimpleBroadphase;
//import com.bulletphysics.collision.dispatch.CollisionAlgorithmCreateFunc;
//import com.bulletphysics.collision.dispatch.CollisionConfiguration;
//import com.bulletphysics.collision.dispatch.CollisionDispatcher;
//import com.bulletphysics.collision.dispatch.CollisionFlags;
//import com.bulletphysics.collision.dispatch.CollisionObject;
//import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
//import com.bulletphysics.collision.dispatch.GhostPairCallback;
//import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
//import com.bulletphysics.collision.narrowphase.PersistentManifold;
//import com.bulletphysics.collision.shapes.BoxShape;
//import com.bulletphysics.collision.shapes.CollisionShape;
//import com.bulletphysics.collision.shapes.ConeShape;
//import com.bulletphysics.collision.shapes.ConvexHullShape;
//import com.bulletphysics.collision.shapes.CylinderShape;
//import com.bulletphysics.collision.shapes.SphereShape;
//import com.bulletphysics.demos.opengl.DemoApplication;
//import com.bulletphysics.demos.opengl.FastFormat;
//import com.bulletphysics.demos.opengl.GLDebugDrawer;
//import com.bulletphysics.demos.opengl.GLShapeDrawer;
//import com.bulletphysics.demos.opengl.IGL;
//import com.bulletphysics.demos.opengl.LWJGL;
//import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
//import com.bulletphysics.dynamics.RigidBody;
//import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
//import com.bulletphysics.dynamics.character.KinematicCharacterController;
//import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
//import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
//import com.bulletphysics.linearmath.DebugDrawModes;
//import com.bulletphysics.linearmath.DefaultMotionState;
//import com.bulletphysics.linearmath.QuaternionUtil;
//import com.bulletphysics.linearmath.Transform;
//import com.bulletphysics.linearmath.VectorUtil;
//
//import demo.lwjgl.basic.GLCam;
//import demo.lwjgl.basic.GLCamera;
//
//import javax.vecmath.Color3f;
//import javax.vecmath.Matrix3f;
//import javax.vecmath.Quat4f;
//import javax.vecmath.Vector3f;
//
//import lib.lwjgl.glmodel.GL_Vector;
//
//import org.lwjgl.input.Keyboard;
//
//import static com.bulletphysics.demos.opengl.IGL.*;
//
//
///**
// * BspDemo shows the convex collision detection, by converting a Quake BSP file
// * into convex objects and allowing interaction with boxes.
// * 
// * @author jezek2
// */
//public class BspDemo extends DemoApplication
//{
//    public KinematicCharacterController character;
//
//    public PairCachingGhostObject ghostObject;
//
//    private GLCam cam = new GLCam();
//
//    private GLCamera camera = new GLCamera();
//
//    GL_Vector ViewPoint;
//
//    private float characterScale = 2f;
//
//    private static int gForward = 0;
//
//    private static int gBackward = 0;
//
//    private static int gLeft = 0;
//
//    private static int gRight = 0;
//
//    private static int gJump = 0;
//
//    private static BspDemo demo;
//
//    private static final float CUBE_HALF_EXTENTS = 1;
//
//    private static final float EXTRA_HEIGHT = -20f;
//
//    // keep the collision shapes, for deletion/cleanup
//    public Map<RigidBody, Entity> entityList = new HashMap<RigidBody, Entity>();
//
//    public BroadphaseInterface broadphase;
//
//    public CollisionDispatcher dispatcher;
//
//    public ConstraintSolver solver;
//
//    public DefaultCollisionConfiguration collisionConfiguration;
//
//    private static EventDispatcher eventDispatcher = new EventDispatcher();
//
//    private static EventDispatcher remoteDispatcher = new EventDispatcher();
//
//    private static chatClient client;
//
//    int count = 0;
//
//    private static boolean connected = false;
//
//
//    public BspDemo( IGL gl )
//    {
//        super( gl );
//    }
//
//
//    public synchronized void initPhysics() throws Exception
//    {
//        // cameraUp.set(0f, 0f, 1f);
//        // forwardAxis = 1;
//
//        setCameraDistance( 22f );
//        // Setup a Physics Simulation Environment
//
//        collisionConfiguration = new DefaultCollisionConfiguration();
//        // btCollisionShape* groundShape = new btBoxShape(btVector3(50,3,50));
//        dispatcher = new CollisionStuff( collisionConfiguration );
//        // the maximum size of the collision world. Make sure objects stay
//        // within these boundaries. Don't make the world AABB size too large, it
//        // will harm simulation quality and performance
//        Vector3f worldMin = new Vector3f( -10f, -10f, -10f );
//        Vector3f worldMax = new Vector3f( 10f, 10f, 10f );
//        // maximum number of objects
//        final int maxProxies = 1024;
//        // Broadphase computes an conservative approximate list of colliding
//        // pairs
//        broadphase = new AxisSweep3( worldMin, worldMax, maxProxies );
//        // broadphase = new SimpleBroadphase();
//        // broadphase = new DbvtBroadphase();
//        // btOverlappingPairCache* broadphase = new btSimpleBroadphase();
//        solver = new SequentialImpulseConstraintSolver();
//        // ConstraintSolver* solver = new OdeConstraintSolver;
//        dynamicsWorld = new DiscreteDynamicsWorld( dispatcher,
//            broadphase,
//            solver,
//            collisionConfiguration );
//
//        Vector3f gravity = new Vector3f();
//        gravity.negate( cameraUp );
//        gravity.scale( 10f );
//        dynamicsWorld.setGravity( gravity );
//        // new BspToBulletConverter().convertBsp(getClass().getResourceAsStream(
//        // "exported.bsp.txt"));
//        // populate();
//        BulletGlobals.setDeactivationTime( 0.1f );
//
//        // here
//        Transform startTransform = new Transform();
//        startTransform.setIdentity();
//        startTransform.origin.set( 0f, 0f, 0f );
//
//        ghostObject = new PairCachingGhostObject();
//        ghostObject.setWorldTransform( startTransform );
//        // AxisSweep3 sweepBP = new AxisSweep3(worldMin, worldMax);
//
//        broadphase.getOverlappingPairCache()
//            .setInternalGhostPairCallback( new GhostPairCallback() );
//
//        BoxShape box = new BoxShape( new Vector3f( 1f, 1f, 1f ) );
//        ghostObject.setCollisionShape( box );
//        ghostObject.setCollisionFlags( CollisionFlags.CHARACTER_OBJECT );
//
//        character = new KinematicCharacterController( ghostObject, box, 1f );
//        dynamicsWorld.addCollisionObject( ghostObject,
//            CollisionFilterGroups.DEFAULT_FILTER,CollisionFilterGroups.ALL_FILTER );
//        dynamicsWorld.addAction( character );
//        populate();
//
//    }
//
//
//    public void populate()
//    {
//        try
//        {
//            new BspYamlToBulletConverter().convertBspYaml( getClass()
//            // TODO changer here
//            .getResourceAsStream( "EntryScene.yml" ) );
//        }
//        catch ( IOException e )
//        {
//            Logging.log.log( Level.SEVERE,
//                "Could not close InputStream for: scene.yml",
//                e );
//        }
//        /*
//         * Transform startTransform = new Transform();
//         * startTransform.setIdentity();
//         * 
//         * float start_x = 0 - 5 / 2; float start_y = 0; float start_z = 0 - 5 /
//         * 2; final ObjectArrayList<Vector3f> points = new
//         * ObjectArrayList<Vector3f>(); points.add(new Vector3f(0f, 0f, 0f));
//         * points.add(new Vector3f(0f, 0f, 2f)); points.add(new Vector3f(0f, 2f,
//         * 0f)); points.add(new Vector3f(0f, 2f, 2f)); points.add(new
//         * Vector3f(2f, 0f, 0f)); points.add(new Vector3f(2f, 0f, 2f));
//         * points.add(new Vector3f(2f, 2f, 0f)); points.add(new Vector3f(2f, 2f,
//         * 2f)); final CollisionShape shape = new ConvexHullShape(points); for
//         * (int k = 0; k < 4; k++) { for (int i = 0; i < 4; i++) { for (int j =
//         * 0; j < 4; j++) { startTransform.origin.set(2f * i + start_x, 10f + 2f
//         * * k + start_y, 2f * j + start_z); RigidBody body =
//         * localCreateRigidBody(1f, startTransform, shape); // TODO figure out
//         * why setting the center of mass transform // doesn't want to work
//         * Transform center = new Transform(); center.setIdentity();
//         * center.origin.set(0.5f, 0.5f, 0.5f);
//         * body.setCenterOfMassTransform(center); // eventDispatcher.notify(new
//         * BlockCreateEvent(new // Entity("Box", body))); } } }
//         */
//        clientResetScene();
//        // resetScene();
//    }
//
//
//    public void resetScene()
//    {
//        BulletStats.gNumDeepPenetrationChecks = 0;
//        BulletStats.gNumGjkChecks = 0;
//
//        int numObjects = 0;
//        if ( dynamicsWorld != null )
//        {
//            dynamicsWorld.stepSimulation( 1f / 60f, 0 );
//            numObjects = dynamicsWorld.getNumCollisionObjects();
//        }
//
//        for ( int i = 0; i < numObjects; i++ )
//        {
//            CollisionObject colObj = dynamicsWorld.getCollisionObjectArray()
//                .getQuick( i );
//            RigidBody body = RigidBody.upcast( colObj );
//            if ( body != null )
//            {
//                if ( body.getMotionState() != null )
//                {
//                    DefaultMotionState myMotionState = (DefaultMotionState)body.getMotionState();
//                    myMotionState.graphicsWorldTrans.set( myMotionState.startWorldTrans );
//                    colObj.setWorldTransform( myMotionState.graphicsWorldTrans );
//                    colObj.setInterpolationWorldTransform( myMotionState.startWorldTrans );
//                    colObj.activate();
//                }
//                // removed cached contact points
//                dynamicsWorld.getBroadphase()
//                    .getOverlappingPairCache()
//                    .cleanProxyFromPairs( colObj.getBroadphaseHandle(),
//                        dynamicsWorld.getDispatcher() );
//
//                body = RigidBody.upcast( colObj );
//                if ( body != null && !body.isStaticObject() )
//                {
//                    RigidBody.upcast( colObj )
//                        .setLinearVelocity( new Vector3f( 0f, 0f, 0f ) );
//                    RigidBody.upcast( colObj )
//                        .setAngularVelocity( new Vector3f( 0f, 0f, 0f ) );
//                }
//            }
//        }
//    }
//
//
//    @Override
//    public synchronized void clientMoveAndDisplay()
//    {
//        gl.glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
//        float dt = getDeltaTimeMicroseconds() * 0.000001f;
//        float ms = getDeltaTimeMicroseconds();
//        try
//        {
//            // TODO May need custom DynamicsWorld to catch exceptions per step
//            int maxSimSubSteps = idle ? 1 : 2;
//            if ( idle )
//            {
//                ms = 1.0f / 420.f;
//            }
//
//            // set walkDirection for our character
//            Transform xform = ghostObject.getWorldTransform( new Transform() );
//
//            Vector3f forwardDir = new Vector3f();
//            xform.basis.getRow( 2, forwardDir );
//            // printf("forwardDir=%f,%f,%f\n",forwardDir[0],forwardDir[1],forwardDir[2]);
//            Vector3f upDir = new Vector3f();
//            xform.basis.getRow( 1, upDir );
//            Vector3f strafeDir = new Vector3f();
//            xform.basis.getRow( 0, strafeDir );
//            forwardDir.normalize();
//            upDir.normalize();
//            strafeDir.normalize();
//
//            Vector3f walkDirection = new Vector3f( 0.0f, 0.0f, 0.0f );
//            float walkVelocity = 30;// 4 km/h -> 1.1 m/s
//            float walkSpeed = walkVelocity * ( dt ) * characterScale;
//
//            if ( gLeft != 0 )
//            {
//                walkDirection.sub( strafeDir );
//            }
//
//            if ( gRight != 0 )
//            {
//                walkDirection.add( strafeDir );
//            }
//
//            if ( gForward != 0 )
//            {
//                walkDirection.sub( forwardDir );
//            }
//
//            if ( gBackward != 0 )
//            {
//                walkDirection.add( forwardDir );
//            }
//            //System.out.println( "pre" + walkDirection );
//            walkDirection.scale( walkSpeed );
//            //System.out.println( "post" + walkDirection );
//
//            character.setWalkDirection( walkDirection );
//
//            // int numSimSteps = dynamicsWorld.stepSimulation(ms,
//            // maxSimSubSteps);
//            dynamicsWorld.stepSimulation( dt );
//
//            // optional but useful: debug drawing
//            dynamicsWorld.debugDrawWorld();
//            //TODO CLIFF stuff
//        }
//        catch ( NullPointerException e )
//        {
//            //System.out.println( "Simulation had null at some point" );
//            // WARN this is very serious
//            // TODO figure out how to fix this...
//        }
//        catch ( ArrayIndexOutOfBoundsException arr )
//        {
//            //System.out.println( "Index Out of Bounds in Simulation" );
//        }
//
//        // optional but useful: debug drawing
//        cam.resetClock();
//        dynamicsWorld.debugDrawWorld();
//        renderme();
//
//        // glFlush();
//        // glutSwapBuffers();
//
//    }
//
//
//    @Override
//    public void displayCallback()
//    {
//        gl.glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );
//
//        renderme();
//
//        // glFlush();
//        // glutSwapBuffers();
//    }
//
//    private StringBuilder buf = new StringBuilder();
//
//    private final Transform m = new Transform();
//
//    private Vector3f wireColor = new Vector3f();
//
//    protected Color3f TEXT_COLOR = new Color3f( 0f, 0f, 0f );
//
//
//    @Override
//    public void renderme()
//    {
//        updateCamera();
//
//        if ( dynamicsWorld != null )
//        {
//            int numObjects = dynamicsWorld.getNumCollisionObjects();
//            wireColor.set( 1f, 0f, 0f );
//            for ( int i = 0; i < numObjects; i++ )
//            {
//                CollisionObject colObj = dynamicsWorld.getCollisionObjectArray()
//                    .getQuick( i );
//                RigidBody body = RigidBody.upcast( colObj );
//
//                // System.out.println(colObj.getWorldTransform(new
//                // Transform()).origin);
//
//                if ( body != null && body.getMotionState() != null )
//                {
//                    DefaultMotionState myMotionState = (DefaultMotionState)body.getMotionState();
//                    m.set( myMotionState.graphicsWorldTrans );
//                }
//                else
//                {
//                    colObj.getWorldTransform( m );
//                }
//                // System.out.println(m.origin);
//
//                wireColor.set( 1f, 1f, 0.5f ); // wants deactivation
//                if ( ( i & 1 ) != 0 )
//                {
//                    wireColor.set( 0f, 0f, 1f );
//                }
//
//                // color differently for active, sleeping, wantsdeactivation
//                // states
//                if ( colObj.getActivationState() == 1 ) // active
//                {
//                    if ( ( i & 1 ) != 0 )
//                    {
//                        // wireColor.add(new Vector3f(1f, 0f, 0f));
//                        wireColor.x += 1f;
//                    }
//                    else
//                    {
//                        // wireColor.add(new Vector3f(0.5f, 0f, 0f));
//                        wireColor.x += 0.5f;
//                    }
//                }
//                if ( colObj.getActivationState() == 2 ) // ISLAND_SLEEPING
//                {
//                    if ( ( i & 1 ) != 0 )
//                    {
//                        // wireColor.add(new Vector3f(0f, 1f, 0f));
//                        wireColor.y += 1f;
//                    }
//                    else
//                    {
//                        // wireColor.add(new Vector3f(0f, 0.5f, 0f));
//                        wireColor.y += 0.5f;
//                    }
//                }
//
//                int a = 0;
//                GLShapeDrawer.drawOpenGL( gl,
//                    m,
//                    colObj.getCollisionShape(),
//                    wireColor,
//                    getDebugMode() );
//            }
//
//            float xOffset = 10f;
//            float yStart = 20f;
//            float yIncr = 20f;
//
//            gl.glDisable( GL_LIGHTING );
//            gl.glColor3f( 0f, 0f, 0f );
//
//            if ( ( debugMode & DebugDrawModes.NO_HELP_TEXT ) == 0 )
//            {
//                setOrthographicProjection();
//
//                yStart = showProfileInfo( xOffset, yStart, yIncr );
//
//                // #ifdef SHOW_NUM_DEEP_PENETRATIONS
//                buf.setLength( 0 );
//                buf.append( "gNumDeepPenetrationChecks = " );
//                FastFormat.append( buf, BulletStats.gNumDeepPenetrationChecks );
//                drawString( buf,
//                    Math.round( xOffset ),
//                    Math.round( yStart ),
//                    TEXT_COLOR );
//                yStart += yIncr;
//
//                buf.setLength( 0 );
//                buf.append( "gNumGjkChecks = " );
//                FastFormat.append( buf, BulletStats.gNumGjkChecks );
//                drawString( buf,
//                    Math.round( xOffset ),
//                    Math.round( yStart ),
//                    TEXT_COLOR );
//                yStart += yIncr;
//
//                buf.setLength( 0 );
//                buf.append( "gNumSplitImpulseRecoveries = " );
//                FastFormat.append( buf, BulletStats.gNumSplitImpulseRecoveries );
//                drawString( buf,
//                    Math.round( xOffset ),
//                    Math.round( yStart ),
//                    TEXT_COLOR );
//                yStart += yIncr;
//
//                // buf = String.format("gNumAlignedAllocs = %d",
//                // BulletGlobals.gNumAlignedAllocs);
//                // TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
//                // yStart += yIncr;
//
//                // buf = String.format("gNumAlignedFree= %d",
//                // BulletGlobals.gNumAlignedFree);
//                // TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
//                // yStart += yIncr;
//
//                // buf = String.format("# alloc-free = %d",
//                // BulletGlobals.gNumAlignedAllocs -
//                // BulletGlobals.gNumAlignedFree);
//                // TODO: BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
//                // yStart += yIncr;
//
//                // enable BT_DEBUG_MEMORY_ALLOCATIONS define in
//                // Bullet/src/LinearMath/btAlignedAllocator.h for memory leak
//                // detection
//                // #ifdef BT_DEBUG_MEMORY_ALLOCATIONS
//                // glRasterPos3f(xOffset,yStart,0);
//                // sprintf(buf,"gTotalBytesAlignedAllocs = %d",gTotalBytesAlignedAllocs);
//                // BMF_DrawString(BMF_GetFont(BMF_kHelvetica10),buf);
//                // yStart += yIncr;
//                // #endif //BT_DEBUG_MEMORY_ALLOCATIONS
//
//                if ( getDynamicsWorld() != null )
//                {
//                    buf.setLength( 0 );
//                    buf.append( "# objects = " );
//                    FastFormat.append( buf,
//                        getDynamicsWorld().getNumCollisionObjects() );
//                    drawString( buf,
//                        Math.round( xOffset ),
//                        Math.round( yStart ),
//                        TEXT_COLOR );
//                    yStart += yIncr;
//
//                    buf.setLength( 0 );
//                    buf.append( "# pairs = " );
//                    FastFormat.append( buf, getDynamicsWorld().getBroadphase()
//                        .getOverlappingPairCache()
//                        .getNumOverlappingPairs() );
//                    drawString( buf,
//                        Math.round( xOffset ),
//                        Math.round( yStart ),
//                        TEXT_COLOR );
//                    yStart += yIncr;
//
//                }
//                // #endif //SHOW_NUM_DEEP_PENETRATIONS
//
//                // JAVA NOTE: added
//                int free = (int)Runtime.getRuntime().freeMemory();
//                int total = (int)Runtime.getRuntime().totalMemory();
//                buf.setLength( 0 );
//                buf.append( "heap = " );
//                FastFormat.append( buf, (float)( total - free )
//                    / ( 1024 * 1024 ) );
//                buf.append( " / " );
//                FastFormat.append( buf, (float)( total ) / ( 1024 * 1024 ) );
//                buf.append( " MB" );
//                drawString( buf,
//                    Math.round( xOffset ),
//                    Math.round( yStart ),
//                    TEXT_COLOR );
//                yStart += yIncr;
//
//                resetPerspectiveProjection();
//            }
//
//            gl.glEnable( GL_LIGHTING );
//        }
//
//        updateCamera();
//    }
//
//
//    public void setup()
//    {
//        cam.setCamera( camera );
//        camera.setCamera( 0f, 0f, 15f, 0f, 0f, -1f, 0f, 1f, 0f );
//
//    }
//
//
//    @Override
//    public void updateCamera()
//    {
//
//        gl.glMatrixMode( GL_PROJECTION );
//        gl.glLoadIdentity();
//        // System.out.println(cameraTargetPosition);
//        float rele = ele * 0.01745329251994329547f; // rads per deg
//        float razi = azi * 0.01745329251994329547f; // rads per deg
//
//        Quat4f rot = new Quat4f();
//        QuaternionUtil.setRotation( rot, cameraUp, razi );
//
//        Vector3f eyePos = new Vector3f();
//        eyePos.set( 0f, 0f, 0f );
//        VectorUtil.setCoord( eyePos, forwardAxis, -cameraDistance );
//
//        Vector3f forward = new Vector3f();
//        forward.set( eyePos.x, eyePos.y, eyePos.z );
//        if ( forward.lengthSquared() < BulletGlobals.FLT_EPSILON )
//        {
//            forward.set( 1f, 0f, 0f );
//        }
//        Vector3f right = new Vector3f();
//        right.cross( cameraUp, forward );
//        Quat4f roll = new Quat4f();
//        QuaternionUtil.setRotation( roll, right, -rele );
//
//        Matrix3f tmpMat1 = new Matrix3f();
//        Matrix3f tmpMat2 = new Matrix3f();
//        tmpMat1.set( rot );
//        tmpMat2.set( roll );
//        tmpMat1.mul( tmpMat2 );
//        tmpMat1.transform( eyePos );
//
//        cameraPosition.set( eyePos );
//
//        if ( glutScreenWidth > glutScreenHeight )
//        {
//            float aspect = glutScreenWidth / (float)glutScreenHeight;
//            gl.glFrustum( -aspect, aspect, -1.0, 1.0, 1.0, 10000.0 );
//        }
//        else
//        {
//            float aspect = glutScreenHeight / (float)glutScreenWidth;
//            gl.glFrustum( -1.0, 1.0, -aspect, aspect, 1.0, 10000.0 );
//        }
//
//        gl.glMatrixMode( GL_MODELVIEW );
//        gl.glLoadIdentity();
//        // System.out.println("camUP "+cameraUp);
//
//        cam.updatePan( cam.getDirection() );
//        ViewPoint = GL_Vector.add( camera.Position, camera.ViewDir );
//        cameraPosition.x = camera.Position.x;
//        cameraPosition.y = camera.Position.y;
//        cameraPosition.z = camera.Position.z;
//        cameraTargetPosition.x = ViewPoint.x;
//        cameraTargetPosition.y = ViewPoint.y;
//        cameraTargetPosition.z = ViewPoint.z;
//        cameraUp.x = camera.UpVector.x;
//        cameraUp.y = camera.UpVector.y;
//        cameraUp.z = camera.UpVector.z;
//        // cameraPosition.x += globalTrans.x;
//        // cameraPosition.y += globalTrans.y;
//        // cameraPosition.z += globalTrans.z;
//        // Transform characterWorldTrans = ghostObject.getWorldTransform(new
//        // Transform());
//
//        // cameraTargetPosition.set(characterWorldTrans.origin);
//        // System.out.println(cameraPosition.x + " " + cameraPosition.y + " " +
//        // cameraPosition.z + " ... " +
//        // cameraTargetPosition.x + " " + cameraTargetPosition.y + " " +
//        // cameraTargetPosition.z + " ... " +
//        // cameraUp.x + " " + cameraUp.y + " " + cameraUp.z);
//
//        for ( CollisionObject o : dynamicsWorld.getCollisionObjectArray() )
//        {
//            if ( o.getCollisionShape().equals( ghostObject.getCollisionShape() ) )
//            {
//
//                Transform t = o.getWorldTransform( new Transform() );
//                //System.out.println( t.origin );
//                gl.gluLookAt( cameraPosition.x + t.origin.x,
//                    cameraPosition.y + t.origin.y,
//                    cameraPosition.z + t.origin.z,
//                    t.origin.x,
//                    t.origin.y,
//                    t.origin.z,
//                    cameraUp.x,
//                    cameraUp.y,
//                    cameraUp.z );
//                break;
//            }
//        }
//
//        // gl.gluLookAt( eyex, eyey, eyez, RigidBody.upcast( ghostObject
//        // ).getCenterOfMassPosition( new Vector3f() ).x, centery, centerz, upx,
//        // upy, upz );
//    }
//
//
//    @Override
//    public void specialKeyboardUp( int key, int x, int y, int modifiers )
//    {
//        switch ( key )
//        {
//            case Keyboard.KEY_W:
//            {
//                if ( cam.getQuadrant() == 1 )
//                {
//                    gForward = 0;
//                }
//                if ( cam.getQuadrant() == 2 )
//                {
//                    gLeft = 0;
//                }
//                if ( cam.getQuadrant() == 3 )
//                {
//                    gBackward = 0;
//                }
//                if ( cam.getQuadrant() == 4 )
//                {
//                    gRight = 0;
//                }
//                break;
//            }
//            case Keyboard.KEY_S:
//            {
//                if ( cam.getQuadrant() == 1 )
//                {
//                    gBackward = 0;
//                }
//                if ( cam.getQuadrant() == 2 )
//                {
//                    gRight = 0;
//                }
//                if ( cam.getQuadrant() == 3 )
//                {
//                    gForward = 0;
//                }
//                if ( cam.getQuadrant() == 4 )
//                {
//                    gLeft = 0;
//                }
//                break;
//            }
//            case Keyboard.KEY_A:
//            {
//                if ( cam.getQuadrant() == 1 )
//                {
//                    gLeft = 0;
//                }
//                if ( cam.getQuadrant() == 2 )
//                {
//                    gBackward = 0;
//                }
//                if ( cam.getQuadrant() == 3 )
//                {
//                    gRight = 0;
//                }
//                if ( cam.getQuadrant() == 4 )
//                {
//                    gForward = 0;
//                }
//                break;
//            }
//            case Keyboard.KEY_D:
//            {
//                if ( cam.getQuadrant() == 1 )
//                {
//                    gRight = 0;
//                }
//                if ( cam.getQuadrant() == 2 )
//                {
//                    gForward = 0;
//                }
//                if ( cam.getQuadrant() == 3 )
//                {
//                    gLeft = 0;
//                }
//                if ( cam.getQuadrant() == 4 )
//                {
//                    gBackward = 0;
//                }
//                break;
//            }
//        }
//    }
//
//
//    // @Override
//    // public synchronized void specialKeyboard(int key, int x, int y, int
//    // modifiers) {
//    // switch (key) {
//    // case Keyboard.KEY_UP: {
//    // cam.handleRotKeysPan();
//    // break;
//    // }
//    // case Keyboard.KEY_DOWN: {
//    // cam.handleRotKeysPan();
//    // break;
//    // }
//    // case Keyboard.KEY_LEFT: {
//    // cam.handleRotKeysPan();
//    // break;
//    // }
//    // case Keyboard.KEY_RIGHT: {
//    // cam.handleRotKeysPan();
//    // break;
//    // }
//    // case Keyboard.KEY_W: {
//    // if (cam.getQuadrant() == 1) {
//    // gForward = 1;
//    // }
//    // if (cam.getQuadrant() == 2) {
//    // gLeft = 1;
//    // }
//    // if (cam.getQuadrant() == 3) {
//    // gBackward = 1;
//    // }
//    // if (cam.getQuadrant() == 4) {
//    // gRight = 1;
//    // }
//    // break;
//    // }
//    // case Keyboard.KEY_S: {
//    // if (cam.getQuadrant() == 1) {
//    // gBackward = 1;
//    // }
//    // if (cam.getQuadrant() == 2) {
//    // gRight = 1;
//    // }
//    // if (cam.getQuadrant() == 3) {
//    // gForward = 1;
//    // }
//    // if (cam.getQuadrant() == 4) {
//    // gLeft = 1;
//    // }
//    // break;
//    // }
//    // case Keyboard.KEY_A: {
//    // if (cam.getQuadrant() == 1) {
//    // gLeft = 1;
//    // }
//    // if (cam.getQuadrant() == 2) {
//    // gBackward = 1;
//    // }
//    // if (cam.getQuadrant() == 3) {
//    // gRight = 1;
//    // }
//    // if (cam.getQuadrant() == 4) {
//    // gForward = 1;
//    // }
//    // break;
//    // }
//    // case Keyboard.KEY_D: {
//    // if (cam.getQuadrant() == 1) {
//    // gRight = 1;
//    // }
//    // if (cam.getQuadrant() == 2) {
//    // gForward = 1;
//    // }
//    // if (cam.getQuadrant() == 3) {
//    // gLeft = 1;
//    // }
//    // if (cam.getQuadrant() == 4) {
//    // gBackward = 1;
//    // }
//    // break;
//    // }
//    //
//    // default:
//    // super.specialKeyboard(key, x, y, modifiers);
//    // break;
//    // }
//    // }
//
//    @Override
//    public synchronized void specialKeyboard(
//        int key,
//        int x,
//        int y,
//        int modifiers )
//    {
//        switch ( key )
//        {
//            case Keyboard.KEY_UP:
//            {
//                cam.handleRotKeysPan();
//                break;
//            }
//            case Keyboard.KEY_DOWN:
//            {
//                cam.handleRotKeysPan();
//                break;
//            }
//            case Keyboard.KEY_LEFT:
//            {
//                cam.handleRotKeysPan();
//                break;
//            }
//            case Keyboard.KEY_RIGHT:
//            {
//                cam.handleRotKeysPan();
//                break;
//            }
//            case Keyboard.KEY_W:
//            {
//                if ( cam.getQuadrant() == 1 )
//                {
//                    gForward = 1;
//                }
//                if ( cam.getQuadrant() == 2 )
//                {
//                    gLeft = 1;
//                }
//                if ( cam.getQuadrant() == 3 )
//                {
//                    gBackward = 1;
//                }
//                if ( cam.getQuadrant() == 4 )
//                {
//                    gRight = 1;
//                }
//                break;
//            }
//            case Keyboard.KEY_S:
//            {
//                if ( cam.getQuadrant() == 1 )
//                {
//                    gBackward = 1;
//                }
//                if ( cam.getQuadrant() == 2 )
//                {
//                    gRight = 1;
//                }
//                if ( cam.getQuadrant() == 3 )
//                {
//                    gForward = 1;
//                }
//                if ( cam.getQuadrant() == 4 )
//                {
//                    gLeft = 1;
//                }
//                break;
//            }
//            case Keyboard.KEY_A:
//            {
//                if ( cam.getQuadrant() == 1 )
//                {
//                    gLeft = 1;
//                }
//                if ( cam.getQuadrant() == 2 )
//                {
//                    gBackward = 1;
//                }
//                if ( cam.getQuadrant() == 3 )
//                {
//                    gRight = 1;
//                }
//                if ( cam.getQuadrant() == 4 )
//                {
//                    gForward = 1;
//                }
//                break;
//            }
//            case Keyboard.KEY_D:
//            {
//                if ( cam.getQuadrant() == 1 )
//                {
//                    gRight = 1;
//                }
//                if ( cam.getQuadrant() == 2 )
//                {
//                    gForward = 1;
//                }
//                if ( cam.getQuadrant() == 3 )
//                {
//                    gLeft = 1;
//                }
//                if ( cam.getQuadrant() == 4 )
//                {
//                    gBackward = 1;
//                }
//                break;
//            }
//
//            case Keyboard.KEY_R:
//            {
//                // Remove all objects
//                for ( CollisionObject a : dynamicsWorld.getCollisionObjectArray()
//                    .toArray( new CollisionObject[0] ) )
//                {
//                    Entity e = null;
//                    for ( RigidBody r : entityList.keySet() )
//                    {
//                        if ( r.getCollisionShape()
//                            .equals( a.getCollisionShape() ) )
//                        {
//                            e = entityList.get( r );
//                            break;
//                        }
//                    }
//                    try
//                    {
//                        dynamicsWorld.removeCollisionObject( a );
//                        if ( e != null )
//                        {
//                            eventDispatcher.notify( new BlockDestroyedEvent( e ) );
//                            entityList.remove( e );
//                        }
//                    }
//                    catch ( NullPointerException n )
//                    {
//                        // System.out.println(
//                        // "Tried to remove object that is not there" );
//                    }
//                    catch ( ArrayIndexOutOfBoundsException b )
//                    {
//                        // System.out.println(
//                        // "ArrayIndexOutOfBounds in simulation" );
//                    }
//                }
//                // repopulate world
//                populate();
//                break;
//            }
//            default:
//            {
//                super.specialKeyboard( key, x, y, modifiers );
//                break;
//            }
//        }
//    }
//
//
//    @Override
//    public synchronized void shootBox( Vector3f destination )
//    {
//        if ( dynamicsWorld != null )
//        {
//            float mass = 50f;
//            Transform startTransform = new Transform();
//            startTransform.setIdentity();
//            Vector3f camPos = new Vector3f( getCameraPosition() );
//            startTransform.origin.set( camPos );
//
//            if ( shapeType.equals( "BOX" ) )
//            {
//                shootBoxShape = new BoxShape( new Vector3f( 1f, 1f, 1f ) );
//            }
//            else if ( shapeType.equals( "SPHERE" ) )
//            {
//                shootBoxShape = new SphereShape( 1f );
//            }
//            else if ( shapeType.equals( "TRIANGLE" ) )
//            {
//                // TODO implement a pyramid
//                shootBoxShape = new ConeShape( 1f, 3f );
//                // shootBoxShape = new TriangleShape(new Vector3f(1f, 1f, 1f),
//                // new Vector3f(1f, 0f, 0f), new Vector3f(0f, -1f, 0f));
//            }
//            else if ( shapeType.equals( "CYLINDER" ) )
//            {
//                shootBoxShape = new CylinderShape( new Vector3f( 1f, 1f, 1f ) );
//            }
//
//            RigidBody body = this.localCreateRigidBody( mass,
//                startTransform,
//                shootBoxShape );
//
//            Vector3f linVel = new Vector3f( destination.x - camPos.x,
//                destination.y - camPos.y,
//                destination.z - camPos.z );
//            linVel.normalize();
//            linVel.scale( ShootBoxInitialSpeed );
//            Transform worldTrans = body.getWorldTransform( new Transform() );
//            worldTrans.origin.set( camPos );
//            worldTrans.setRotation( new Quat4f( 0f, 0f, 0f, 1f ) );
//            body.setWorldTransform( worldTrans );
//
//            body.setLinearVelocity( linVel );
//            body.setAngularVelocity( new Vector3f( 0f, 0f, 0f ) );
//
//            body.setCcdMotionThreshold( 1f );
//            body.setCcdSweptSphereRadius( 0.2f );
//            Entity entity = new Entity( body.getCollisionShape().getName(),
//                body );
//            // Dynamic gravity for object
//            // TODO consolidate setgravity into one method, let entity set its
//            // tied rigidbody gravity
//            if ( !bodyGravityType.equals( "NORMAL" ) )
//            {
//                if ( bodyGravityType.equals( "ANTIGRAVITY" ) )
//                {
//                    entity.getRigidBody()
//                        .setGravity( new Vector3f( 0f, 30f, 0f ) );
//                    entity.setGravity( new Vector3f( 0f, 30f, 0f ) );
//                }
//                else if ( bodyGravityType.equals( "STASIS" ) )
//                {
//                    entity.getRigidBody()
//                        .setGravity( new Vector3f( 0f, 0f, 0f ) );
//                    entity.setGravity( new Vector3f( 0f, 0f, 0f ) );
//                }
//            }
//            else
//            {
//                entity.getRigidBody()
//                    .setGravity( dynamicsWorld.getGravity( new Vector3f() ) );
//                entity.setGravity( dynamicsWorld.getGravity( new Vector3f() ) );
//            }
//            entityList.put( body, entity );
//            eventDispatcher.notify( new BlockCreateEvent( entity ) );
//        }
//    }
//
//
//    public static void main( String[] args ) throws Exception
//    {
//        demo = new BspDemo( LWJGL.getGL() );
////         try
////         {
////         client = new chatClient( null,
////         "localhost",
////         "BASED",
////         remoteDispatcher );
////         if ( client.connect() )
////         {
////         client.start();
////         connected = true;
////         }
////         Thread.sleep( 2000 );
////         }
////         catch ( Exception e )
////         {
////         // No networking
////         connected = false;
////         }
//        demo.initListener();
//        demo.setup();
//        demo.initPhysics();
//        demo.getDynamicsWorld()
//            .setDebugDrawer( new GLDebugDrawer( LWJGL.getGL() ) );
//        // demo.debugMode = 1;
//        LWJGL.main( args,
//            800,
//            600,
//            "Bullet Physics Demo. http://bullet.sf.net",
//            demo );
//    }
//
//
//    /**
//     * Initialize event listeners
//     */
//    public void initListener()
//    {
//        /**
//         * LocalEvents
//         */
//        BlockCollisionListener blockListener = new BlockCollisionListener();
//        eventDispatcher.registerListener( Type.BLOCK_CREATE, blockListener );
//        eventDispatcher.registerListener( Type.BLOCK_DESTROYED, blockListener );
//        eventDispatcher.registerListener( Type.BLOCK_COLLISION, blockListener );
//        eventDispatcher.registerListener( Type.BLOCK_COLLISION_RESOLVED,
//            blockListener );
//        /**
//         * Remote events
//         */
//        if ( connected )
//        {
//            BlockListener remoteListener = new BlockListener()
//            {
//                @Override
//                public void onBlockCreate( BlockCreateEvent event )
//                {
//                    float mass = ( 1f / event.getEntity()
//                        .getRigidBody()
//                        .getInvMass() );
//                    // System.out.println("Event mass: " + mass);
//                    // System.out.println("Event transform: " +
//                    // event.getEntity().getRigidBody().getWorldTransform(new
//                    // Transform()).toString());
//                    // System.out.println("Event CollisionShape: " +
//                    // event.getEntity().getRigidBody().getCollisionShape().toString());
//                    RigidBody body = demo.localCreateRigidBody( mass,
//                        event.getEntity()
//                            .getRigidBody()
//                            .getWorldTransform( new Transform() ),
//                        event.getEntity().getRigidBody().getCollisionShape() );
//                    body.setAngularFactor( event.getEntity()
//                        .getRigidBody()
//                        .getAngularFactor() );
//                    body.setAngularVelocity( event.getEntity()
//                        .getRigidBody()
//                        .getAngularVelocity( new Vector3f() ) );
//                    body.setLinearVelocity( event.getEntity()
//                        .getRigidBody()
//                        .getLinearVelocity( new Vector3f() ) );
//                    body.setDamping( event.getEntity()
//                        .getRigidBody()
//                        .getLinearDamping(), event.getEntity()
//                        .getRigidBody()
//                        .getAngularDamping() );
//                    body.setGravity( event.getEntity().getGravity() );
//                    // System.out.println("Remote gravity: "
//                    // +event.getEntity().getGravity());
//                    // System.out.println(event.getEntity().getRigidBody().getLinearVelocity(new
//                    // Vector3f()).toString());
//                    Entity e = new Entity( event.getEntity().getID(), body );
//                    e.setGravity( event.getEntity().getGravity() );
//                    entityList.put( body, e );
//                    // System.out.println("Added block");
//                }
//
//
//                @Override
//                public synchronized void onBlockDestroyed(
//                    BlockDestroyedEvent event )
//                {
//                    // System.out.println("Received destroyed event");
//                    Entity removed = null;
//                    for ( Entity e : entityList.values() )
//                    {
//                        if ( e.getID().equals( event.getEntity().getID() ) )
//                        {
//                            removed = e;
//                            break;
//                        }
//                    }
//                    if ( removed != null )
//                    {
//                        // System.out.println("Found in list");
//                        removed = entityList.remove( removed.getRigidBody() );
//                        final CollisionShape shape = removed.getRigidBody()
//                            .getCollisionShape();
//                        CollisionObject toRemove = null;
//                        for ( CollisionObject o : dynamicsWorld.getCollisionObjectArray() )
//                        {
//                            if ( o.getCollisionShape().equals( shape ) )
//                            {
//                                // System.out.println("found in dynamics world");
//                                toRemove = o;
//                                break;
//                            }
//                        }
//                        if ( toRemove != null )
//                        {
//                            // System.out.println("Removed");
//                            try
//                            {
//                                dynamicsWorld.removeCollisionObject( toRemove );
//                            }
//                            catch ( NullPointerException e )
//                            {
//                                // System.out.println(
//                                // "Attempted to remove object taht no longer exists."
//                                // );
//                            }
//                            catch ( ArrayIndexOutOfBoundsException a )
//                            {
//                                // System.out.println(
//                                // "Attempted to remove object taht no longer exists."
//                                // );
//                            }
//                        }
//
//                    }
//                }
//            };
//            remoteDispatcher.registerListener( Type.BLOCK_CREATE,
//                remoteListener );
//            remoteDispatcher.registerListener( Type.BLOCK_DESTROYED,
//                remoteListener );
//        }
//        // MusicPlayer mp = new MusicPlayer(eventDispatcher);
//    }
//
//
//    // //////////////////////////////////////////////////////////////////////////
//
//    public class BspYamlToBulletConverter extends BspYamlConverter
//    {
//
//        @Override
//        public synchronized void addConvexVerticesCollider(
//            String name,
//            ObjectArrayList<Vector3f> vertices,
//            float mass,
//            Vector3f acceleration,
//            String image,
//            String[] description )
//        {
//            Transform startTransform = new Transform();
//            // can use a shift
//            startTransform.setIdentity();
//            startTransform.origin.set( 0, 0, 0 );
//
//            // this create an internal copy of the vertices
//            CollisionShape shape = new ConvexHullShape( vertices );
//            RigidBody body = localCreateRigidBody( mass, startTransform, shape );
//            // body.setActivationState(RigidBody.ACTIVE_TAG);
//            final Transform center = new Transform();
//            center.setIdentity();
//            center.origin.set( 1f, 1f, 1f );
//            body.setCenterOfMassTransform( center );
//            addEntity( name, body, image, description, acceleration );
//        }
//
//
//        @Override
//        public void addShapeCollider(
//            String name,
//            String type,
//            Vector3f localscaling,
//            Vector3f transform,
//            float mass,
//            Vector3f acceleration,
//            String image,
//            String[] description )
//        {
//            CollisionShape shape = new BoxShape( new Vector3f( 1f, 1f, 1f ) );
//            if ( type.contains( "box" ) )
//            {
//                shape = new BoxShape( localscaling );
//            }
//            else if ( type.contains( "sphere" ) )
//            {
//                shape = new SphereShape( localscaling.x );
//            }
//            else if ( type.contains( "cylinder" ) )
//            {
//                shape = new CylinderShape( localscaling );
//            }
//            else if ( type.contains( "cone" ) )
//            {
//                shape = new ConeShape( localscaling.x, localscaling.y );
//            }
//            else
//            {
//                Logging.log.warning( "Unknown type '" + type + "' for shape: "
//                    + name );
//                return;
//            }
//            Transform origin = new Transform();
//            origin.setIdentity();
//            origin.origin.set( transform.x, transform.y, transform.z );
//            RigidBody body = localCreateRigidBody( mass, origin, shape );
//            addEntity( name, body, image, description, acceleration );
//        }
//
//
//        public void addEntity(
//            String name,
//            RigidBody body,
//            String image,
//            String[] description,
//            Vector3f acceleration )
//        {
//            Entity e = new Entity( null, null );
//            if ( description != null )
//            {
//                e = new Entity( name, body, image, description );
//            }
//            else
//            {
//                e = new Entity( name, body, image );
//            }
//            if ( acceleration != null )
//            {
//                body.setGravity( acceleration );
//                e.setGravity( acceleration );
//            }
//            else
//            {
//                // set default gravity;
//                e.setGravity( dynamicsWorld.getGravity( new Vector3f() ) );
//            }
//            entityList.put( body, e );
//            eventDispatcher.notify( new BlockCreateEvent( e ) );
//        }
//    }
//
//
//    public class BlockCollisionListener extends BlockListener
//    {
//        @Override
//        public void onBlockCreate( BlockCreateEvent event )
//        {
//            sendToRemote( event );
//        }
//
//
//        @Override
//        public void onBlockDestroyed( BlockDestroyedEvent event )
//        {
//            sendToRemote( event );
//        }
//
//
//        @Override
//        public synchronized void onBlockCollision( BlockCollisionEvent event )
//        {
//            final PersistentManifold pm = event.getPersistentManifold();
//            if ( pm.getBody0() instanceof RigidBody
//                && pm.getBody1() instanceof RigidBody )
//            {
//                final Entity entityA = entityList.get( (RigidBody)pm.getBody0() );
//                final Entity entityB = entityList.get( (RigidBody)pm.getBody1() );
//                if ( entityA != null && entityB != null )
//                {
//                    // if ( entityA.getID().equals( "object2" ) )
//                    // {
//                    // entityA.getRigidBody().setAngularFactor( 0f );
//                    // entityA.getRigidBody()
//                    // .setAngularVelocity( new Vector3f( 0f, 0f, 0f ) );
//                    // entityA.getRigidBody()
//                    // .setLinearVelocity( new Vector3f( 0f, 0f, 0f ) );
//                    // entityA.getRigidBody().setGravity( new Vector3f( 0f,
//                    // 10f,
//                    // 0f ) );
//                    // System.out.println( "OMG" );
//                    // }
//                    // else if ( entityB.getID().equals( "object2" ) )
//                    // {
//                    // entityB.getRigidBody().setAngularFactor( 0f );
//                    // entityB.getRigidBody()
//                    // .setAngularVelocity( new Vector3f( 0f, 0f, 0f ) );
//                    // entityB.getRigidBody()
//                    // .setLinearVelocity( new Vector3f( 0f, 0f, 0f ) );
//                    // entityB.getRigidBody().setGravity( new Vector3f( 0f,
//                    // 10f,
//                    // 0f ) );
//                    // System.out.println( "OMA" );
//                    // }
//                    // TODO CLIFF STUFF
//                    if ( ( entityA.getRigidBody()
//                        .getCollisionShape()
//                        .equals( ghostObject.getCollisionShape() ) || 
//                        entityB.getRigidBody().getCollisionShape()
//                        .equals( ghostObject.getCollisionShape() ) ))
//                    {
//                        System.out.println("I can sense the player!");
//                    }
//                    if ( ( entityA.getID().equals( "elevatorsmash1" ) || entityB.getID()
//                        .equals( "elevatorsmash1" ) )
//                        && ( entityA.getID()
//                            .equals( "sidepathleftelevatorshaftfix" ) || entityB.getID()
//                            .equals( "sidepathleftelevatorshaftfix" ) ) )
//                    {
//                        if ( entityA.getID().equals( "elevatorsmash1" ) )
//                        {
//                            // entityA.getRigidBody().setAngularVelocity( new
//                            // Vector3f(0f, 0f, 7f) );
//                            entityA.freeze();
//                            final RigidBody body = entityA.getRigidBody();
//                            final Vector3f vector = body.getCenterOfMassPosition( new Vector3f() );
//                            body.translate( new Vector3f( 80f - vector.x, -5f
//                                - vector.y, -11f - vector.z ) );
//                            body.setGravity( new Vector3f( 0f, 7f, 0f ) );
//
//                        }
//                        else
//                        {
//                            // entityB.getRigidBody().setAngularVelocity( new
//                            // Vector3f(0f, 0f, 7f) );
//                            entityB.freeze();
//                            final RigidBody body = entityB.getRigidBody();
//                            final Vector3f vector = body.getCenterOfMassPosition( new Vector3f() );
//                            body.translate( new Vector3f( 80f - vector.x, -5f
//                                - vector.y, -11f - vector.z ) );
//                            body.setGravity( new Vector3f( 0f, 7f, 0f ) );
//                        }
//                    }
//                    if ( ( entityA.getID().equals( "elevatorsmash1" ) || entityB.getID()
//                        .equals( "elevatorsmash1" ) )
//                        && ( entityA.getID().equals( "elevatorshaftcieling" ) || entityB.getID()
//                            .equals( "elevatorshaftcieling" ) ) )
//                    {
//                        if ( entityA.getID().equals( "elevatorsmash1" ) )
//                        {
//                            // entityA.getRigidBody().setAngularVelocity( new
//                            // Vector3f(0f, 0f, 7f) );
//                            entityA.freeze();
//                            final RigidBody body = entityA.getRigidBody();
//                            final Vector3f vector = body.getCenterOfMassPosition( new Vector3f() );
//                            body.translate( new Vector3f( 80f - vector.x,
//                                1.5f - vector.y,
//                                -11f - vector.z ) );
//                            body.setGravity( new Vector3f( 0f, -7f, 0f ) );
//                        }
//                        else
//                        {
//                            // entityB.getRigidBody().setAngularVelocity( new
//                            // Vector3f(0f, 0f, 7f) );
//                            entityB.freeze();
//                            final RigidBody body = entityB.getRigidBody();
//                            final Vector3f vector = body.getCenterOfMassPosition( new Vector3f() );
//                            body.translate( new Vector3f( 80f - vector.x,
//                                1.5f - vector.y,
//                                -11f - vector.z ) );
//                            body.setGravity( new Vector3f( 0f, -7f, 0f ) );
//                        }
//                    }
//                    if ( ( entityA.getID().equals( "sidewayselevator1" ) || entityB.getID()
//                        .equals( "sidewayselevator1" ) )
//                        && ( entityA.getID().equals( "headingunder" ) || entityB.getID()
//                            .equals( "headingunder" ) ) )
//                    {
//                        Vector3f reset = new Vector3f( -1.3f, -25.25f, 3.75f );
//                        if ( entityA.getID().equals( "sidewayselevator1" ) )
//                        {
//                            elevatorshiftxlow( entityA, reset );
//                        }
//                        else
//                        {
//                            elevatorshiftxlow( entityB, reset );
//                        }
//                    }
//                    if ( ( entityA.getID().equals( "sidewayselevator1" ) || entityB.getID()
//                        .equals( "sidewayselevator1" ) )
//                        && ( entityA.getID().equals( "sidewayselevator2" ) || entityB.getID()
//                            .equals( "sidewayselevator2" ) ) )
//                    {
//                        // Vector3f reset = new Vector3f (0f, 0f, 0f);
//                        if ( entityA.getID().equals( "sidewayselevator1" ) )
//                        {
//                            // System.out.println(entityA.getID() +" "+
//                            // entityB.getID());
//                            elevatorshiftxhigh( entityA, entityA.getRigidBody()
//                                .getCenterOfMassPosition( new Vector3f() ) );
//                        }
//                        else
//                        {
//                            // System.out.println(entityA.getID() +" "+
//                            // entityB.getID());
//                            elevatorshiftxhigh( entityB, entityB.getRigidBody()
//                                .getCenterOfMassPosition( new Vector3f() ) );
//                        }
//                    }
//                    if ( ( entityA.getID().equals( "sidewayselevator1" ) || entityB.getID()
//                        .equals( "sidewayselevator1" ) )
//                        && ( entityA.getID().equals( "headingunder" ) || entityB.getID()
//                            .equals( "headingunder" ) ) )
//                    {
//                        Vector3f reset = new Vector3f( -1.3f, -25.25f, 3.75f );
//                        if ( entityA.getID().equals( "sidewayselevator1" ) )
//                        {
//                            elevatorshiftxlow( entityA, reset );
//                        }
//                        else
//                        {
//                            elevatorshiftxlow( entityB, reset );
//                        }
//                    }
//                    if ( ( entityA.getID().equals( "sidewayselevator2" ) || entityB.getID()
//                        .equals( "sidewayselevator2" ) )
//                        && ( entityA.getID().equals( "underelevatorplatform1" ) || entityB.getID()
//                            .equals( "underelevatorplatform1" ) ) )
//                    {
//                        Vector3f reset = new Vector3f( -20f, -25.25f, 3.75f );
//                        if ( entityA.getID().equals( "sidewayselevator2" ) )
//                        {
//                            elevatorshiftxhigh( entityA, reset );
//                        }
//                        else
//                        {
//                            elevatorshiftxhigh( entityB, reset );
//                        }
//                    }
//                    if ( ( entityA.getID().equals( "sidewayselevator2" ) || entityB.getID()
//                        .equals( "sidewayselevator2" ) )
//                        && ( entityA.getID().equals( "sidewayselevator1" ) || entityB.getID()
//                            .equals( "sidewayselevator1" ) ) )
//                    {
//                        // Vector3f reset = new Vector3f (0f, 0f, 0f);
//                        if ( entityA.getID().equals( "sidewayselevator2" ) )
//                        {
//                            elevatorshiftxlow( entityA, entityA.getRigidBody()
//                                .getCenterOfMassPosition( new Vector3f() ) );
//                        }
//                        else
//                        {
//                            elevatorshiftxlow( entityB, entityB.getRigidBody()
//                                .getCenterOfMassPosition( new Vector3f() ) );
//                        }
//                    }
//                    if ( ( entityA.getID().equals( "elevatorsmash2" ) || entityB.getID()
//                        .equals( "elevatorsmash2" ) )
//                        && ( entityA.getID().equals( "elevatorshaftbackpath" ) || entityB.getID()
//                            .equals( "elevatorshaftbackpath" ) ) )
//                    {
//                        Vector3f reset = new Vector3f( 83.75f, -5f, 20f );
//                        if ( entityA.getID().equals( "elevatorsmash2" ) )
//                        {
//                            elevatorshiftyhigh( entityA, reset );
//                        }
//                        else
//                        {
//                            elevatorshiftyhigh( entityB, reset );
//                        }
//                    }
//                    if ( ( entityA.getID().equals( "elevatorsmash2" ) || entityB.getID()
//                        .equals( "elevatorsmash2" ) )
//                        && ( entityA.getID()
//                            .equals( "elevatorshaftbackpathcieling" ) || entityB.getID()
//                            .equals( "elevatorshaftbackpathcieling" ) ) )
//                    {
//                        Vector3f reset = new Vector3f( 83.75f, 1.5f, 20f );
//                        if ( entityA.getID().equals( "elevatorsmash2" ) )
//                        {
//                            elevatorshiftylow( entityA, reset );
//                        }
//                        else
//                        {
//                            elevatorshiftylow( entityB, reset );
//                        }
//                    }
//                    if ( ( entityA.getID().equals( "elevatorsmash3" ) || entityB.getID()
//                        .equals( "elevatorsmash3" ) )
//                        && ( entityA.getID().equals( "elevatorshaftbackpath" ) || entityB.getID()
//                            .equals( "elevatorshaftbackpath" ) ) )
//                    {
//                        Vector3f reset = new Vector3f( 83.75f, -5f, 10f );
//                        if ( entityA.getID().equals( "elevatorsmash3" ) )
//                        {
//                            elevatorshiftyhigh( entityA, reset );
//                        }
//                        else
//                        {
//                            elevatorshiftyhigh( entityB, reset );
//                        }
//                    }
//                    if ( ( entityA.getID().equals( "elevatorsmash3" ) || entityB.getID()
//                        .equals( "elevatorsmash3" ) )
//                        && ( entityA.getID()
//                            .equals( "elevatorshaftbackpathcieling" ) || entityB.getID()
//                            .equals( "elevatorshaftbackpathcieling" ) ) )
//                    {
//                        Vector3f reset = new Vector3f( 83.75f, 1.5f, 10f );
//                        if ( entityA.getID().equals( "elevatorsmash3" ) )
//                        {
//                            elevatorshiftylow( entityA, reset );
//                        }
//                        else
//                        {
//                            elevatorshiftylow( entityB, reset );
//                        }
//                    }
//                    if ( ( entityA.getID().equals( "elevatorsmash4" ) || entityB.getID()
//                        .equals( "elevatorsmash4" ) )
//                        && ( entityA.getID().equals( "elevatorshaftbackpath" ) || entityB.getID()
//                            .equals( "elevatorshaftbackpath" ) ) )
//                    {
//                        Vector3f reset = new Vector3f( 83.75f, -5f, 0f );
//                        if ( entityA.getID().equals( "elevatorsmash4" ) )
//                        {
//                            elevatorshiftyhigh( entityA, reset );
//                        }
//                        else
//                        {
//                            elevatorshiftyhigh( entityB, reset );
//                        }
//                    }
//                    if ( ( entityA.getID().equals( "elevatorsmash4" ) || entityB.getID()
//                        .equals( "elevatorsmash4" ) )
//                        && ( entityA.getID()
//                            .equals( "elevatorshaftbackpathcieling" ) || entityB.getID()
//                            .equals( "elevatorshaftbackpathcieling" ) ) )
//                    {
//                        Vector3f reset = new Vector3f( 83.75f, 1.5f, 0f );
//                        if ( entityA.getID().equals( "elevatorsmash4" ) )
//                        {
//                            elevatorshiftylow( entityA, reset );
//                        }
//                        else
//                        {
//                            elevatorshiftylow( entityB, reset );
//                        }
//                    }
//                    if ( ( entityA.getID().equals( "elevatorsmash5" ) || entityB.getID()
//                        .equals( "elevatorsmash5" ) )
//                        && ( entityA.getID().equals( "elevatorshaftbackpath" ) || entityB.getID()
//                            .equals( "elevatorshaftbackpath" ) ) )
//                    {
//                        Vector3f reset = new Vector3f( 83.75f, -5f, -8f );
//                        if ( entityA.getID().equals( "elevatorsmash5" ) )
//                        {
//                            elevatorshiftyhigh( entityA, reset );
//                        }
//                        else
//                        {
//                            elevatorshiftyhigh( entityB, reset );
//                        }
//                    }
//                    if ( ( entityA.getID().equals( "elevatorsmash5" ) || entityB.getID()
//                        .equals( "elevatorsmash5" ) )
//                        && ( entityA.getID()
//                            .equals( "elevatorshaftbackpathcieling" ) || entityB.getID()
//                            .equals( "elevatorshaftbackpathcieling" ) ) )
//                    {
//                        Vector3f reset = new Vector3f( 83.75f, 1.5f, -8f );
//                        if ( entityA.getID().equals( "elevatorsmash5" ) )
//                        {
//                            elevatorshiftylow( entityA, reset );
//                        }
//                        else
//                        {
//                            elevatorshiftylow( entityB, reset );
//                        }
//                    }
//                    // TODO also, when a block is collided, check if they need
//                    // to be "refrozen"
//                    // if ( entityA.isFrozen() )
//                    // {
//                    // entityA.unfreeze();
//                    // entityA.getRigidBody().translate( new Vector3f( 0f,
//                    // 5f,
//                    // 0f ) );
//                    // }
//                    // if ( entityB.isFrozen() )
//                    // {
//                    // entityB.unfreeze();
//                    // entityA.getRigidBody().translate( new Vector3f( 0f,
//                    // 5f,
//                    // 0f ) );
//                    // }
//                    // Entites are known and exist, so we can act upon them
//                    if ( entityA.getRigidBody()
//                        .getCollisionShape()
//                        .getName()
//                        .equalsIgnoreCase( "sphere" ) )
//                    {
//                        if ( setGravity( entityB, new Vector3f( 0f, 30f, 0f ) ) )
//                        {
//                            // entityA.setGravity(new Vector3f(0f, 30f, 0f));
//                            eventDispatcher.notify( new BlockDestroyedEvent( entityA ) );
//                            dynamicsWorld.removeCollisionObject( entityA.getRigidBody() );
//                            entityList.remove( entityA );
//                            entityB.getRigidBody().translate( new Vector3f( 5f,
//                                0f,
//                                0f ) );
//                        }
//                    }
//                    else if ( entityB.getRigidBody()
//                        .getCollisionShape()
//                        .getName()
//                        .equalsIgnoreCase( "sphere" ) )
//                    {
//                        if ( setGravity( entityA, new Vector3f( 0f, 30f, 0f ) ) )
//                        {
//                            eventDispatcher.notify( new BlockDestroyedEvent( entityB ) );
//                            dynamicsWorld.removeCollisionObject( entityB.getRigidBody() );
//                            entityA.getRigidBody().translate( new Vector3f( 5f,
//                                0f,
//                                0f ) );
//                        }
//                    }
//                    else
//                    {
//                        // System.out.println("objA: " + entityA.getID()
//                        // + " objB: " + entityB.getID());
//                    }
//                    System.out.println(pm.getBody0().getClass().toString() + " " + pm.getBody1().getClass().toString());
//                }
//                else
//                {
//                    // TODO Somehow it is known.... how to handle?
//                    System.out.println(pm.getBody0().getClass().toString() + " " + pm.getBody1().getClass().toString());
//                }
//            }
//        }
//
//
//        @Override
//        public void onBlockCollisionResolved( BlockCollisionResolvedEvent event )
//        {
//
//            final PersistentManifold pm = event.getPersistentManifold();
//            if ( pm.getBody0() instanceof RigidBody
//                && pm.getBody1() instanceof RigidBody )
//            {
//                final Entity entityA = entityList.get( (RigidBody)pm.getBody0() );
//                final Entity entityB = entityList.get( (RigidBody)pm.getBody1() );
//                if ( entityA != null && entityB != null )
//                {
//                    // if ( entityA.getRigidBody().isActive()
//                    // && entityB.getRigidBody().isActive() )
//                    // {
//                    // if ( entityA.getID().equalsIgnoreCase( "box" )
//                    // && entityB.getID().equalsIgnoreCase( "box" ) )
//                    // {
//                    // // TODO block freeze event
//                    // entityA.freeze();
//                    // entityB.freeze();
//                    // }
//                    // }
//                }
//            }
//
//        }
//
//
//        public boolean setGravity( Entity target, Vector3f direction )
//        {
//            if ( !target.getRigidBody().isStaticObject() )
//            {
//                eventDispatcher.notify( new BlockPhysicsChangeEvent( target,
//                    direction ) );
//                target.getRigidBody().setGravity( direction );
//                target.getRigidBody().activate();
//
//                return true;
//            }
//            return false;
//        }
//
//
//        public void sendToRemote( Event<?> event )
//        {
//            if ( connected )
//            {
//                try
//                {
//                    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    final ObjectOutputStream oos = new ObjectOutputStream( baos );
//                    oos.writeObject( new EventPackage( event ) );
//                    oos.flush();
//                    byte[] data = baos.toByteArray();
//                    // System.out.println(data.length);
//                    client.sendMessage( data );
//                    oos.close();
//                    baos.close();
//                }
//                catch ( IOException e )
//                {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//
//    public class CollisionStuff extends CollisionDispatcher
//    {
//
//        /*
//         * Standard constructor
//         */
//        public CollisionStuff( CollisionConfiguration arg0 )
//        {
//            super( arg0 );
//        }
//
//
//        /**
//         * Called when a new collision between objects occurred
//         */
//        @Override
//        public PersistentManifold getNewManifold( Object b0, Object b1 )
//        {
//            // TODO maybe check and negate if one of the objects is frozen?
//            // don't know the effect it would cause
//            final PersistentManifold pm = super.getNewManifold( b0, b1 );
//            // Throw event
//            eventDispatcher.notify( new BlockCollisionEvent( pm ) );
//            return pm;
//        }
//
//
//        /**
//         * Called when a collision has been resolved: when the objects are no
//         * longer in contact
//         */
//        @Override
//        public void releaseManifold( PersistentManifold manifold )
//        {
//            // Throw event
//            eventDispatcher.notify( new BlockCollisionResolvedEvent( manifold ) );
//            super.releaseManifold( manifold );
//        }
//    }
//
//
//    private void elevatorshiftxhigh( Entity a, Vector3f v )
//    {
//        a.freeze();
//        final RigidBody body = a.getRigidBody();
//        final Vector3f vector = body.getCenterOfMassPosition( new Vector3f() );
//        body.translate( new Vector3f( v.x - vector.x, v.y - vector.y, v.z
//            - vector.z ) );
//        body.setGravity( new Vector3f( 7f, 0f, 0f ) );
//        body.setLinearVelocity( new Vector3f( 7f, 0f, 0f ) );
//    }
//
//
//    private void elevatorshiftxlow( Entity a, Vector3f v )
//    {
//        a.freeze();
//        final RigidBody body = a.getRigidBody();
//        final Vector3f vector = body.getCenterOfMassPosition( new Vector3f() );
//        body.translate( new Vector3f( v.x - vector.x, v.y - vector.y, v.z
//            - vector.z ) );
//        body.setGravity( new Vector3f( -7f, 0f, 0f ) );
//        body.setLinearVelocity( new Vector3f( -7f, 0f, 0f ) );
//
//    }
//
//
//    private void elevatorshiftyhigh( Entity a, Vector3f v )
//    {
//        a.freeze();
//        final RigidBody body = a.getRigidBody();
//        final Vector3f vector = body.getCenterOfMassPosition( new Vector3f() );
//        body.translate( new Vector3f( v.x - vector.x, v.y - vector.y, v.z
//            - vector.z ) );
//        body.setGravity( new Vector3f( 0f, 7f, 0f ) );
//        body.setLinearVelocity( new Vector3f( 0f, 7f, 0f ) );
//
//    }
//
//
//    private void elevatorshiftylow( Entity a, Vector3f v )
//    {
//        a.freeze();
//        final RigidBody body = a.getRigidBody();
//        final Vector3f vector = body.getCenterOfMassPosition( new Vector3f() );
//        body.translate( new Vector3f( v.x - vector.x, v.y - vector.y, v.z
//            - vector.z ) );
//        body.setGravity( new Vector3f( 0f, -7f, 0f ) );
//        body.setLinearVelocity( new Vector3f( 0f, -7f, 0f ) );
//
//    }
//
//
//    private void elevatorshiftzhigh( Entity a, Vector3f v )
//    {
//        a.freeze();
//        final RigidBody body = a.getRigidBody();
//        final Vector3f vector = body.getCenterOfMassPosition( new Vector3f() );
//        body.translate( new Vector3f( v.x - vector.x, v.y - vector.y, v.z
//            - vector.z ) );
//        body.setGravity( new Vector3f( 0f, 0f, 7f ) );
//
//    }
//
//
//    private void elevatorshiftzlow( Entity a, Vector3f v )
//    {
//        a.freeze();
//        final RigidBody body = a.getRigidBody();
//        final Vector3f vector = body.getCenterOfMassPosition( new Vector3f() );
//        body.translate( new Vector3f( v.x - vector.x, v.y - vector.y, v.z
//            - vector.z ) );
//        body.setGravity( new Vector3f( 0f, 0f, -7f ) );
//
//    }
//
//}
