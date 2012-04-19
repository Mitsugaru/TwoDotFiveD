package com.ATeam.twoDotFiveD.udp.Client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.ATeam.twoDotFiveD.entity.Entity;
import com.ATeam.twoDotFiveD.event.Event;
import com.ATeam.twoDotFiveD.event.block.BlockCreateEvent;
import com.ATeam.twoDotFiveD.event.block.BlockDestroyedEvent;
import com.ATeam.twoDotFiveD.event.block.BlockPhysicsChangeEvent;
import com.ATeam.twoDotFiveD.event.player.PlayerMoveEvent;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConeShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

public class EventPackage implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -7908810705660283805L;
    final private Map<String, Object> data = new HashMap<String, Object>();

    public EventPackage(Event<?> event) {
	for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
	    put(entry.getKey(), entry.getValue());
	}
    }

    public void put(String path, Object o) {
	data.put(path, o);
    }

    public Map<String, Object> getData() {
	return data;
    }

    public Event<?> getEvent() throws ClassNotFoundException {
	// Because we cannot have a null RigidBody, this is a default rigid body
	// to be used for when we don't care about the rigidbody at all
	final RigidBodyConstructionInfo baseInfo = new RigidBodyConstructionInfo(
		0, new DefaultMotionState(), new BoxShape(new Vector3f(1f, 1f,
			1f)));
	String className = (String) data.get("class");
	if (className.contains("BlockCreateEvent")) {
	    final float mass = ((Float) data
		    .get("entity.rigidbody.rigidbodyconstructioninfo.mass"))
		    .floatValue();
	    final Matrix4f transformMatrix = new Matrix4f(
		    (float[]) (data
			    .get("entity.rigidbody.motionstate.transform")));
	    // System.out.println(transformMatrix.toString());
	    final Transform startTransform = new Transform(transformMatrix);
	    // startTransform.setIdentity();
	    String centerString = (String) data.get("entity.rigidbody.center");
	    centerString = centerString.replace("(", "");
	    centerString = centerString.replace(",", "");
	    centerString = centerString.replace(")", "");
	    final String[] centerCut = centerString.split(" ");
	    // TODO not a perfect translation
	    final Vector3f center = new Vector3f(
		    Float.parseFloat(centerCut[0]),
		    Float.parseFloat(centerCut[1]),
		    Float.parseFloat(centerCut[2]));
	    startTransform.origin.set(center);
	    // System.out.println(startTransform.toString());

	    final String shapeClass = (String) data
		    .get("entity.rigidbody.collisionshape.class");
	    CollisionShape c = new BoxShape(new Vector3f(1f, 1f, 1f));
	    // System.out.println(shapeClass);
	    // System.out.println("BoxShape: " +
	    // shapeClass.contains("BoxShape"));
	    // System.out.println("Convex: " +
	    // shapeClass.contains("ConvexHullShape"));
	    if (shapeClass.contains("BoxShape")) {
		// parse localscaling string to be Vector3f
		String localscaling = (String) data
			.get("entity.rigidbody.collisionshape.localscaling");
		localscaling = localscaling.replace("(", "");
		localscaling = localscaling.replace(",", "");
		localscaling = localscaling.replace(")", "");
		final String[] localscalingcut = localscaling.split(" ");
		c = new BoxShape(new Vector3f(
			Float.parseFloat(localscalingcut[0]),
			Float.parseFloat(localscalingcut[1]),
			Float.parseFloat(localscalingcut[2])));
	    } else if (shapeClass.contains("SphereShape")) {

		c = new SphereShape(
			((Float) data
				.get("entity.rigidbody.collisionshape.radius"))
				.floatValue());
	    } else if (shapeClass.contains("ConeShape")) {
		c = new ConeShape(
			((Float) data.get("entity.rigidbody.collisionshape.radius"))
				.floatValue(), ((Float) data
				.get("entity.rigidbody.collisionshape.height"))
				.floatValue());
	    } else if (shapeClass.contains("CylinderShape")) {
		String localscaling = (String) data
			.get("entity.rigidbody.collisionshape.localscaling");
		localscaling = localscaling.replace("(", "");
		localscaling = localscaling.replace(",", "");
		localscaling = localscaling.replace(")", "");
		final String[] localscalingcut = localscaling.split(" ");
		c = new CylinderShape(new Vector3f(
			Float.parseFloat(localscalingcut[0]),
			Float.parseFloat(localscalingcut[1]),
			Float.parseFloat(localscalingcut[2])));
	    } else if (shapeClass.contains("ConvexHullShape")) {
		// Make new shape and parse all points to be added to shape
		ObjectArrayList<Vector3f> list = new ObjectArrayList<Vector3f>();
		int size = ((Integer) data
			.get("entity.rigidbody.collisionshape.size"))
			.intValue();
		// System.out.println(size);
		for (int i = 0; i < size; i++) {
		    String point = ((String) data
			    .get("entity.rigidbody.collisionshape.point" + i));
		    point = point.replace("(", "");
		    point = point.replace(",", "");
		    point = point.replace(")", "");
		    final String[] cut = point.split(" ");
		    list.add(new Vector3f(Float.parseFloat(cut[0]), Float
			    .parseFloat(cut[1]), Float.parseFloat(cut[2])));
		}
		c = new ConvexHullShape(list);
		startTransform.origin.set(0, 0, 0);
	    }
	    Vector3f inertia = new Vector3f(0f, 0f, 0f);
	    if (mass != 0f) {
		c.calculateLocalInertia(mass, inertia);
	    }
	    DefaultMotionState myMotionState = new DefaultMotionState(
		    startTransform);
	    // System.out.println("Mass:" + mass);
	    // System.out.println("myMotionState" + myMotionState.toString());
	    // System.out.println("Transform: " + startTransform.toString());
	    // System.out.println("Shape: " + c.toString());
	    // System.out.println("Inertia: " + inertia.toString());
	    final String ID = (String) data.get("entity.ID");
	    final RigidBodyConstructionInfo info = new RigidBodyConstructionInfo(
		    mass, myMotionState, c, inertia);
	    // TODO image?
	    Entity body = new Entity(info, ID, new Vector3f(1f, 1f, 1f), null);
	    body.setAngularFactor(((Float) data
		    .get("entity.rigidbody.angularfactor")).floatValue());
	    String angular = (String) data
		    .get("entity.rigidbody.angularvelocity");
	    angular = angular.replace("(", "");
	    angular = angular.replace(",", "");
	    angular = angular.replace(")", "");
	    final String[] angularCut = angular.split(" ");
	    body.setAngularVelocity(new Vector3f(Float
		    .parseFloat(angularCut[0]),
		    Float.parseFloat(angularCut[1]), Float
			    .parseFloat(angularCut[2])));
	    String linear = (String) data
		    .get("entity.rigidbody.linearvelocity");
	    linear = linear.replace("(", "");
	    linear = linear.replace(",", "");
	    linear = linear.replace(")", "");
	    final String[] linearCut = linear.split(" ");
	    body.setLinearVelocity(new Vector3f(Float.parseFloat(linearCut[0]),
		    Float.parseFloat(linearCut[1]), Float
			    .parseFloat(linearCut[2])));
	    body.setDamping(((Float) data
		    .get("entity.rigidbody.lineardampening")).floatValue(),
		    ((Float) data.get("entity.rigidbody.angulardampening"))
			    .floatValue());
	    String gravityString = (String) data.get("entity.gravity");
	    // System.out.println(gravityString);
	    gravityString = gravityString.replace("(", "");
	    gravityString = gravityString.replace(",", "");
	    gravityString = gravityString.replace(")", "");
	    final String[] gravityCut = gravityString.split(" ");
	    final Vector3f gravity = new Vector3f(
		    Float.parseFloat(gravityCut[0]),
		    Float.parseFloat(gravityCut[1]),
		    Float.parseFloat(gravityCut[2]));
	    // System.out.println("EP Gravity: " + gravity.toString());
	    body.setEntityGravity(gravity);
	    return (new BlockCreateEvent(body));
	} else if (className.contains("BlockDestroyedEvent")) {
	    // System.out.println("BlockDestroyedEvent package");
	    // TODO not sure if this will error out or not...
	    Entity e = new Entity(baseInfo, (String) data.get("entity.ID"),
		    null, null);
	    return (new BlockDestroyedEvent(e));
	} else if (className.contains("BlockPhysicsChangeEvent")) {
	    Entity e = new Entity(baseInfo, (String) data.get("entity.ID"),
		    null, null);
	    String gravityString = (String) data.get("entity.direction");
	    gravityString = gravityString.replace("(", "");
	    gravityString = gravityString.replace(",", "");
	    gravityString = gravityString.replace(")", "");
	    final String[] gravityCut = gravityString.split(" ");
	    final Vector3f gravity = new Vector3f(
		    Float.parseFloat(gravityCut[0]),
		    Float.parseFloat(gravityCut[1]),
		    Float.parseFloat(gravityCut[2]));
	    return (new BlockPhysicsChangeEvent(e, gravity));
	} else if (className.contains("PlayerMoveEvent")) {
	    Entity e = new Entity(baseInfo, (String) data.get("entity.ID"),
		    null, null);
	    final Matrix4f transformMatrix = new Matrix4f(
		    (float[]) (data
			    .get("entity.rigidbody.motionstate.transform")));
	    final Transform startTransform = new Transform(transformMatrix);
	    PlayerMoveEvent playerMoveEvent = new PlayerMoveEvent(e, startTransform);
	    return playerMoveEvent;
	} else {
	    // TODO do something?
	    return (new BlockCreateEvent(null));
	}
	// System.out.println(className);
	// System.out.println(data.get("entity.gravity.x") + " " +
	// data.get("entity.gravity.y") + " " + data.get("entity.gravity.z"));
    }
}
