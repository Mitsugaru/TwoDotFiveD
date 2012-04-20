package com.ATeam.twoDotFiveD.event.player;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.ATeam.twoDotFiveD.entity.Entity;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConeShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.linearmath.Transform;


public class PlayerJoinEvent extends PlayerEvent {

	public PlayerJoinEvent(Entity player) {
		super(Type.PLAYER_JOIN, player);
		// TODO Auto-generated constructor stub
	}
	
	public void notify(PlayerListener listener)
	{
		listener.onPlayerJoin(this);
	}
	
	@Override
	public Map<String, Object> getData()
	{
		final Map<String, Object> data = new HashMap<String, Object>();
		/**
		 * Entity
		 */
		//Class
		data.put("class", name);
		//Entity
		data.put("entity.ID", getPlayer().getID());
		//Image, if any
			data.put("entity.image", getPlayer().getImage().toString());
		/**
		 * Rigid body
		 */
		//Gravity
		data.put("entity.gravity", getPlayer().getEntityGravity().toString());
		//System.out.println("Event gravity: " + getEntity().getGravity().toString());
		//Mass
		data.put("entity.rigidbody.rigidbodyconstructioninfo.mass",
				new Float((1f / getPlayer().getInvMass())));
		//TODO see if local inertia is actually necessary
		//Start transform
		final Transform worldTransform = getPlayer()
				.getMotionState().getWorldTransform(new Transform());
		final Matrix4f transformMatrix = worldTransform
				.getMatrix(new Matrix4f());
		data.put("entity.rigidbody.motionstate.transform", new float[] {
				transformMatrix.m00, transformMatrix.m01, transformMatrix.m02,
				transformMatrix.m03, transformMatrix.m10, transformMatrix.m11,
				transformMatrix.m12, transformMatrix.m13, transformMatrix.m20,
				transformMatrix.m21, transformMatrix.m22, transformMatrix.m23,
				transformMatrix.m30, transformMatrix.m31, transformMatrix.m32,
				transformMatrix.m33 });
		data.put("entity.rigidbody.angularfactor", getPlayer().getAngularFactor());
		data.put("entity.rigidbody.angularvelocity", getPlayer().getAngularVelocity(new Vector3f()).toString());
		data.put("entity.rigidbody.linearvelocity", getPlayer().getLinearVelocity(new Vector3f()).toString());
		//System.out.println(getEntity().getRigidBody().getLinearVelocity(new Vector3f()).toString());
		data.put("entity.rigidbody.lineardampening", getPlayer().getLinearDamping());
		data.put("entity.rigidbody.angulardampening", getPlayer().getAngularDamping());
		data.put("entity.rigidbody.center", getPlayer().getCenterOfMassPosition(new Vector3f()).toString());
		/**
		 * Collision Shape
		 */
		final CollisionShape shape = getPlayer().getCollisionShape();
		//Class
		data.put("entity.rigidbody.collisionshape.class", shape.getClass().toString());
		//Shape
		if(shape instanceof BoxShape)
		{
			//BoxShape
			data.put("entity.rigidbody.collisionshape.localscaling", ((BoxShape) shape).getLocalScaling(new Vector3f()).toString());
			//System.out.println((String)data.get("entity.rigidbody.collisionshape.localscaling"));
		}
		else if(shape instanceof SphereShape)
		{
			data.put("entity.rigidbody.collisionshape.radius", ((SphereShape) shape).getRadius());
			//System.out.println((String)data.get("entity.rigidbody.collisionshape.localscaling"));
		}
		else if(shape instanceof ConeShape)
		{
			data.put("entity.rigidbody.collisionshape.height",((ConeShape) shape).getHeight());
			data.put("entity.rigidbody.collisionshape.radius",((ConeShape) shape).getRadius());
		}
		else if(shape instanceof CylinderShape)
		{
			data.put("entity.rigidbody.collisionshape.localscaling", ((CylinderShape) shape).getLocalScaling(new Vector3f()).toString());
		}
		else if(shape instanceof ConvexHullShape)
		{
			//All points of a ConvexHullShape
			int i = 0;
			for(Vector3f point : ((ConvexHullShape) shape).getPoints())
			{
				data.put("entity.rigidbody.collisionshape.point" + i, point.toString());
				i++;
			}
			System.out.println(i);
			data.put("entity.rigidbody.collisionshape.size", i);
		}
		return data;
	}
}
