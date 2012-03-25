package com.ATeam.twoDotFiveD.udp.Client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.ATeam.twoDotFiveD.entity.Entity;
import com.ATeam.twoDotFiveD.event.Event;
import com.ATeam.twoDotFiveD.event.Event.Type;
import com.ATeam.twoDotFiveD.event.block.BlockCreateEvent;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;

public class EventPackage implements Serializable
{
	final private Map<String, Object> data = new HashMap<String, Object>();
	
	public EventPackage(Event<?> event)
	{
		for(Map.Entry<String, Object> entry : event.getData().entrySet())
		{
			put(entry.getKey(), entry.getValue());
		}
	}
	
	public void put(String path, Object o)
	{
		data.put(path, o);
	}
	
	public Map<String, Object> getData()
	{
		return data;
	}
	
	public Event<?> getEvent() throws ClassNotFoundException
	{
		String className = (String)data.get("class");
		if(className.contains("BlockCreateEvent"))
		{
			final float mass = ((Float) data.get("entity.rigidbody.rigidbodyconstructioninfo.mass")).floatValue();
			//final Matrix4f transformMatrix = new Matrix4f((float[])(data.get("entity.rigidbody.motionstate.transform")));
			//System.out.println(transformMatrix.toString());
			final Transform startTransform = new Transform();
			startTransform.setIdentity();
			String centerString = (String) data.get("entity.rigidbody.center");
			centerString = centerString.replace("(", "");
			centerString = centerString.replace(",","");
			centerString = centerString.replace(")", "");
			final String[] centerCut = centerString.split(" ");
			final Vector3f center = new Vector3f(Float.parseFloat(centerCut[0]), Float.parseFloat(centerCut[1]), Float.parseFloat(centerCut[2]));
			startTransform.origin.set(center);
			//System.out.println(startTransform.toString());
			Vector3f inertia = new Vector3f(0f, 0f, 0f);
			final String shapeClass = (String)data.get("entity.rigidbody.collisionshape.class");
			CollisionShape c = new BoxShape(new Vector3f(1f, 1f, 1f));
			//System.out.println(shapeClass);
			//System.out.println("BoxShape: " + shapeClass.contains("BoxShape"));
			//System.out.println("Convex: " + shapeClass.contains("ConvexHullShape"));
			if(shapeClass.contains("BoxShape"))
			{
				//TODO parse localscaling string to be Vector3f
				c = new BoxShape(new Vector3f(1f, 1f, 1f));
			}
			else if(shapeClass.contains("ConvexHullShape"))
			{
				//Make new shape and parse all points to be added to shape
				ObjectArrayList<Vector3f> list = new ObjectArrayList<Vector3f>();
				int size = ((Integer)data.get("entity.rigidbody.collisionshape.size")).intValue();
				//System.out.println(size);
				for(int i = 0; i < size; i++)
				{
					String point = ((String) data.get("entity.rigidbody.collisionshape.point" + i));
					point = point.replace("(", "");
					point = point.replace(",","");
					point = point.replace(")", "");
					final String[] cut = point.split(" ");
					list.add(new Vector3f(Float.parseFloat(cut[0]), Float.parseFloat(cut[1]), Float.parseFloat(cut[2])));
				}
				c = new ConvexHullShape(list);
				startTransform.origin.set(0, 0, 0);
			}
			c.calculateLocalInertia(mass, inertia);
			DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
			//System.out.println("Mass:" + mass);
			//System.out.println("myMotionState" + myMotionState.toString());
			//System.out.println("Transform: " + startTransform.toString());
			//System.out.println("Shape: " + c.toString());
			//System.out.println("Inertia: " + inertia.toString());
			final RigidBodyConstructionInfo info = new RigidBodyConstructionInfo(mass, myMotionState, c, inertia);
			RigidBody body = new RigidBody(info);
			body.setAngularFactor(((Float)data.get("entity.rigidbody.angularfactor")).floatValue());
			String angular = (String) data.get("entity.rigidbody.angularvelocity");
			angular = angular.replace("(", "");
			angular = angular.replace(",","");
			angular = angular.replace(")", "");
			final String[] angularCut =  angular.split(" ");
			body.setAngularVelocity(new Vector3f(Float.parseFloat(angularCut[0]), Float.parseFloat(angularCut[1]), Float.parseFloat(angularCut[2])));
			String linear = (String) data.get("entity.rigidbody.linearvelocity");
			linear = linear.replace("(", "");
			linear = linear.replace(",","");
			linear = linear.replace(")", "");
			final String[] linearCut =  linear.split(" ");
			body.setLinearVelocity(new Vector3f(Float.parseFloat(linearCut[0]), Float.parseFloat(linearCut[1]), Float.parseFloat(linearCut[2])));
			body.setDamping(((Float)data.get("entity.rigidbody.lineardampening")).floatValue(), ((Float)data.get("entity.rigidbody.angulardampening")).floatValue());
			String gravityString = (String) data.get("entity.gravity");
			//System.out.println(gravityString);
			gravityString = gravityString.replace("(", "");
			gravityString = gravityString.replace(",","");
			gravityString = gravityString.replace(")", "");
			final String[] gravityCut =  linear.split(" ");
			final Vector3f gravity = new Vector3f(Float.parseFloat(gravityCut[0]), Float.parseFloat(gravityCut[1]), Float.parseFloat(gravityCut[2]));
			body.setGravity(gravity);
			final String ID = (String) data.get("entity.ID");
			Entity e = new Entity(ID, body);
			e.setGravity(gravity);
			return(new BlockCreateEvent(e));
		}
		else
		{
			//TODO do something?
			return(new BlockCreateEvent(null));
		}
		//System.out.println(className);
		//System.out.println(data.get("entity.gravity.x") + " " + data.get("entity.gravity.y") + " " + data.get("entity.gravity.z"));
	}
}
