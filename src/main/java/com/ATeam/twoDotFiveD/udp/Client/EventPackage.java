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
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

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
			final Matrix4f transformMatrix = new Matrix4f((float[])(data.get("entity.rigidbody.motionstate.transform")));
			final Transform startTransform = new Transform(transformMatrix);
			Vector3f inertia = new Vector3f(0f, 0f, 0f);
			final String shapeClass = (String)data.get("entity.rigidbody.collisionshape.class");
			CollisionShape c = null;
			if(shapeClass.contains("BoxShape"))
			{
				//TODO parse localscaling string to be Vector3f
				c = new BoxShape(new Vector3f(0f, 0f, 0f));
			}
			else if(shapeClass.contains("ConvexHullShape"))
			{
				//TODO make new shape and parse all points to be added to shape
			}
			c.calculateLocalInertia(mass, inertia);
			DefaultMotionState myMotionState = new DefaultMotionState(startTransform);
			final RigidBodyConstructionInfo info = new RigidBodyConstructionInfo(mass, myMotionState, c, inertia);
			RigidBody body = new RigidBody(info);
			final String ID = (String) data.get("entity.ID");
			Entity e = new Entity(ID, body);
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
