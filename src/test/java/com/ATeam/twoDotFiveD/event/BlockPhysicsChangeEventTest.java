package com.ATeam.twoDotFiveD.event;


import java.util.Map;

import javax.vecmath.Vector3f;

import org.junit.Test;

import com.ATeam.twoDotFiveD.entity.Entity;
import com.ATeam.twoDotFiveD.event.block.BlockPhysicsChangeEvent;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.linearmath.DefaultMotionState;

import junit.framework.TestCase;

public class BlockPhysicsChangeEventTest extends TestCase
{
	private BlockPhysicsChangeEvent event;
	private Entity entity;
	private Vector3f gravity = new Vector3f(0f, 10f, 0f);
	
	public BlockPhysicsChangeEventTest(String name)
	{
		super(name);
		final DefaultMotionState state = new DefaultMotionState();
		final CollisionShape shape = new BoxShape(new Vector3f(1f, 1f, 1f));
		entity = new Entity(1f, state, shape, new Vector3f(0f, 0f, 0f), "TEST", new Vector3f(1f, 1f, 1f), new String[]{"description", "line2"});
		entity.setGravity(gravity);
		event = new BlockPhysicsChangeEvent(entity, gravity);
	}
	
	@Test
	public void testGetData()
	{
		final Map<String, Object> data = event.getData();
		try
		{
			final String className = (String) data.get("class");
			assertTrue("Class name needs to exist. Expected: '" + event.getClass().toString() + "' Actual: '" + className + "'", event.getClass().toString().contains(className));
			final String id = (String) data.get("entity.ID");
			assertTrue("Entity ID needs to be the same", entity.getID().equals(id));
			final String direction = (String) data.get("entity.direction");
			assertTrue("Direction needs to be the same", gravity.toString().equals(direction));
		}
		catch(Exception e)
		{
			fail("Exception: " + e.getMessage());
		}
	}
	
	@Test
	public void testModifyDirection()
	{
		gravity = new Vector3f(0f, 0f, 10f);
		assertFalse("Direction of event should not change", gravity.equals(event.getDirection()));
	}
	
	@Test
	public void testModifyEntity()
	{
		final DefaultMotionState state = new DefaultMotionState();
		final CollisionShape shape = new SphereShape(1f);
		entity = new Entity(10f, state, shape, new Vector3f(0f, 0f, 0f), "TEST", new Vector3f(1f, 1f, 1f), new String[]{"description", "line2"});
		assertFalse("Entity of an event should not be modified", event.getEntity().equals(entity));
	}
	
}
