package demo.jbullet.javagaming;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.MotionState;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Material;
import javax.media.j3d.PointLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.universe.SimpleUniverse;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;

/**
 * Demonstrates JBullet with Java3D. Shows a box falling on plane. Scene can be
 * reset by pressing the R key.
 */
public class BouncingBox /*implements FrameListener*/ {

    // contains all the physics objects and performs the simulation
    protected DynamicsWorld dynamicsWorld = null;

    // TransformGroups to the bouncing box and the non moving ground
    private TransformGroup boxTG;
    private TransformGroup groundTG;

    // size of bouncing box
    private static final float BOX_DIM = 0.3f;


    /**
     * Starts the application
     */
    public static void main(String[] args) throws Exception {
        new BouncingBox();
    }

    /**
     * Shows a JFrame containing a 3d canvas showing a box bouncing on a plane.
     */
    public BouncingBox() {
        Canvas3D canvas3D = initJava3DAndCreateScene();

        initPhysics();

        // reset the physics scene when R key is pressed
        canvas3D.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_R) {
                    resetScene();
                }
            }
        });

        // show a JFrame containing the Canvas3D
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(BorderLayout.CENTER, canvas3D);
        frame.setSize(640, 480);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Initialises Java3D and does the following:
     * - positions the view
     * - creates a box and ground
     * - creates a point light where the view is located
     * - creates a behavior that invokes the nextFrame method every frame
     * - adds the objects to the universe
     * @return the Java3D canvas that can be added to a frame
     */
    private Canvas3D initJava3DAndCreateScene() {
        // create canvas and universe
        Canvas3D canvas3D = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        SimpleUniverse simpleUniverse = new SimpleUniverse(canvas3D);
        simpleUniverse.getViewer().getView().setFieldOfView(Math.toRadians(90));

        // position view
        TransformGroup viewTG = simpleUniverse.getViewingPlatform()
                .getMultiTransformGroup().getTransformGroup(0);
        Transform3D viewT3D = new Transform3D();
        Point3d viewPos = new Point3d(-5, 4, 1);
        Point3d viewTarget = new Point3d(0, 1, 0);
        Vector3d up = new Vector3d(0, 1, 0);
        viewT3D.lookAt(viewPos, viewTarget, up);
        viewT3D.invert();
        viewTG.setTransform(viewT3D);

        // use appearance with default white material
        Appearance app = new Appearance();
        app.setMaterial(new Material());

        // create a box with BOX_DIM as half dimensions located above ground
        Transform3D boxT3D = new Transform3D();
        boxT3D.rotX((float) Math.toRadians(60));
        boxT3D.setTranslation(new Vector3f(0.75f, 3, 0));
        boxTG = new TransformGroup(boxT3D);
        boxTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        boxTG.addChild(new Box(BOX_DIM, BOX_DIM, BOX_DIM, app));

        // create a large flat box to represent the ground plane
        groundTG = new TransformGroup();
        groundTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        groundTG.addChild(new Box(4, .0001f, 4, app));

        // create white point light at view position
        Color3f white = new Color3f(Color.WHITE);
        Point3f attenuation = new Point3f(1, 0, 0);
        Point3f lightPos = new Point3f(viewPos);
        PointLight pointLight = new PointLight(white, lightPos, attenuation);
        //INFO pointLight.setInfluencingBounds(SGUtils.INFINITE_BOUNDING_SPHERE);

        // create behavior that calls nextFrame before each frame
        //INFO FrameBehavior frameBehavior = new FrameBehavior(this);

        // add it all to the universe
        BranchGroup root = new BranchGroup();
        root.addChild(pointLight);
        root.addChild(boxTG);
        root.addChild(groundTG);
        //INFO root.addChild(frameBehavior);
        simpleUniverse.addBranchGraph(root);

        return canvas3D;
    }

    /**
     * Creates dynamicsWorld and adds RigidBodies
     */
    private void initPhysics() {
        dynamicsWorld = createDynamicsWorld();
        dynamicsWorld.setGravity(new Vector3f(0f, -10f, 0f));
        dynamicsWorld.addRigidBody(createBoxBody());
        dynamicsWorld.addRigidBody(createGroundBody());
    }

    /**
     * Creates a dynamics world
     */
    private DynamicsWorld createDynamicsWorld() {
        // collision configuration contains default setup for memory, collision setup
        DefaultCollisionConfiguration collisionConfiguration
                = new DefaultCollisionConfiguration();
        // calculates exact collision given a list possible colliding pairs
        CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

        // the maximum size of the collision world. Make sure objects stay 
        // within these boundaries. Don't make the world AABB size too large, it
        // will harm simulation quality and performance
        Vector3f worldAabbMin = new Vector3f(-1000, -1000, -1000);
        Vector3f worldAabbMax = new Vector3f( 1000,  1000,  1000);
        // maximum number of objects
        final int maxProxies = 1024;
        // Broadphase computes an conservative approximate list of colliding pairs
        BroadphaseInterface broadphase = new AxisSweep3(
                worldAabbMin, worldAabbMax, maxProxies);

        // constraint (joint) solver
        ConstraintSolver solver = new SequentialImpulseConstraintSolver();

        // provides discrete rigid body simulation
        return new DiscreteDynamicsWorld(
                dispatcher, broadphase, solver, collisionConfiguration);
    }

    /**
     * Creates a rigid body representing a box
     */
    private RigidBody createBoxBody() {
        // must be same as Java3D box
        CollisionShape colShape = new BoxShape(new Vector3f(BOX_DIM, BOX_DIM, BOX_DIM));

        // any positive non zero value would do
        float mass = 1f;

        return createRigidBody(colShape, mass, boxTG);
    }

    /**
     * Creates a RigidBody representing the ground.
     */
    private RigidBody createGroundBody() {
        // collision object is a horisantal plane located at y = 0
        Vector3f normal = new Vector3f(0, 1, 0);
        CollisionShape colShape = new StaticPlaneShape(normal, 0);

        // 0 mass means body is static
        float mass = 0f;

        return createRigidBody(colShape, mass, groundTG);
    }

    /**
     * Creates a RigidBody from the specified CollisionShape and mass.
     * A TGMotionState is used to bind the rigid body to the specified TransformGroup.
     */
    private RigidBody createRigidBody(CollisionShape shape, float mass, TransformGroup tg) {
        // calculates moment of inertia, the rotational "mass"
        Vector3f localInertia = new Vector3f(0, 0, 0);
        shape.calculateLocalInertia(mass, localInertia);

        MotionState boxMotionState = new TGMotionState(tg);

        // create and return
        RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(
                mass, boxMotionState, shape, localInertia);
        return new RigidBody(rbInfo);
    }

    /**
     * Resets the scene to its start state
     */
    public void resetScene() {
        // iterate rigid bodies
        for (int i = 0; i < dynamicsWorld.getNumCollisionObjects(); i++) {
            CollisionObject colObj = dynamicsWorld.getCollisionObjectArray().get(i);
            RigidBody body = RigidBody.upcast(colObj);
            if (body != null) {
                if (body.getMotionState() instanceof TGMotionState) {
                    // reset body to its start position
                    ((TGMotionState) body.getMotionState()).reset(body);
                }

                // removed cached contact points
                dynamicsWorld.getBroadphase().getOverlappingPairCache().cleanProxyFromPairs(
                        colObj.getBroadphaseHandle(), dynamicsWorld.getDispatcher());

                // stop the body from moving and spinning
                if (!body.isStaticObject()) {
                    body.setLinearVelocity(new Vector3f(0f, 0f, 0f));
                    body.setAngularVelocity(new Vector3f(0f, 0f, 0f));
                }
            }
        }
    }

    /**
     * Updates the simulation.
     * @param frameTimeSec the duration of the last frame in seconds
     */
    /*@Override
    public void nextFrame(float frameTimeSec) {
        // will increment simulation in 1/60 second steps and interpolate
        // between the two last steps to get smooth animation
        dynamicsWorld.stepSimulation(frameTimeSec);
    }*/
}
