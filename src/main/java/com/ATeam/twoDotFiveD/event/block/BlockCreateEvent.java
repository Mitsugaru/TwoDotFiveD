package com.ATeam.twoDotFiveD.event.block;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.ATeam.twoDotFiveD.block.Block;
import com.ATeam.twoDotFiveD.entity.Entity;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConeShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.linearmath.Transform;

public class BlockCreateEvent extends BlockEvent
{
	
	public BlockCreateEvent(Entity entity)
	{
		// TODO replace block with entity;
		super(Type.BLOCK_CREATE, entity);
	}
	
	public void notify(BlockListener listener)
	{
		listener.onBlockCreate(this);
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
		data.put("entity.ID", getEntity().getID());
		//Image, if any
		if(getEntity().getImage() != null)
		{
			data.put("entity.image", getEntity().getImage());
		}
		/**
		 * Rigid body
		 */
		//Gravity
		data.put("entity.gravity", getEntity().getGravity().toString());
		//System.out.println("Event gravity: " + getEntity().getGravity().toString());
		//Mass
		data.put("entity.rigidbody.rigidbodyconstructioninfo.mass",
				new Float((1f / getEntity().getRigidBody().getInvMass())));
		//TODO see if local inertia is actually necessary
		//Start transform
		/*final Transform worldTransform = getEntity().getRigidBody()
				.getMotionState().getWorldTransform(new Transform());
		final Matrix4f transformMatrix = worldTransform
				.getMatrix(new Matrix4f());
		data.put("entity.rigidbody.motionstate.transform", new float[] {
				transformMatrix.m00, transformMatrix.m01, transformMatrix.m02,
				transformMatrix.m03, transformMatrix.m10, transformMatrix.m11,
				transformMatrix.m12, transformMatrix.m13, transformMatrix.m20,
				transformMatrix.m21, transformMatrix.m22, transformMatrix.m23,
				transformMatrix.m30, transformMatrix.m31, transformMatrix.m32,
				transformMatrix.m33 });*/
		data.put("entity.rigidbody.angularfactor", getEntity().getRigidBody().getAngularFactor());
		data.put("entity.rigidbody.angularvelocity", getEntity().getRigidBody().getAngularVelocity(new Vector3f()).toString());
		data.put("entity.rigidbody.linearvelocity", getEntity().getRigidBody().getLinearVelocity(new Vector3f()).toString());
		//System.out.println(getEntity().getRigidBody().getLinearVelocity(new Vector3f()).toString());
		data.put("entity.rigidbody.lineardampening", getEntity().getRigidBody().getLinearDamping());
		data.put("entity.rigidbody.angulardampening", getEntity().getRigidBody().getAngularDamping());
		data.put("entity.rigidbody.center", getEntity().getRigidBody().getCenterOfMassPosition(new Vector3f()).toString());
		/**
		 * Collision Shape
		 */
		final CollisionShape shape = getEntity().getRigidBody()
				.getCollisionShape();
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
