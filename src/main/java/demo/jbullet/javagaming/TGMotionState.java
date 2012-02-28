package demo.jbullet.javagaming;

import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

/**
 * Custom MotionState that gets the world transform from a TransformGroup
 */
public class TGMotionState extends MotionState {

    private Transform3D startTransform = new Transform3D();
    private TransformGroup tg;

    /**
     * The current TransformGroup transform will be used when reset.
     * @param tg the TransformGroup that is wrapped
     */
    public TGMotionState(TransformGroup tg) {
        this.tg = tg;
        tg.getTransform(startTransform);
    }

    /**
     * Gets a reference to the wrapped TransformGroup.
     * @return the transformGroup
     */
    public TransformGroup getTransformGroup() {
        return tg;
    }

    /**
     * Gives bullet the world transform of tg.
     */
    @Override
    public Transform getWorldTransform(Transform out) {
        Transform3D t3d = new Transform3D();//INFO SGUtils.getWorldTransform(tg);
        t3d.get(out.origin);
        t3d.get(out.basis);
        return out;
    }

    /**
     * Sets the world transform of tg from the transform provided by bullet.
     */
    @Override
    public void setWorldTransform(Transform worldTrans) {
        Transform3D t3d = new Transform3D();
        t3d.setTranslation(worldTrans.origin);
        t3d.setRotationScale(worldTrans.basis);
        //INFO SGUtils.setWorldTransform(tg, t3d);
    }

    /**
     * Resets the wrapped TransformGroup and the specified body to where it
     * was when this TGMotionState was created.
     */
    public void reset(RigidBody body) {
        tg.setTransform(startTransform);
        body.setWorldTransform(getWorldTransform(new Transform()));
        body.setInterpolationWorldTransform(getWorldTransform(new Transform()));
        body.activate();
    }
}
