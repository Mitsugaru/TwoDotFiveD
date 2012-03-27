package com.ATeam.twoDotFiveD.event;


import java.util.Map;

import javax.vecmath.Vector3f;

import org.junit.Test;

import com.ATeam.twoDotFiveD.entity.Entity;
import com.ATeam.twoDotFiveD.event.block.BlockCreateEvent;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.DefaultMotionState;

import junit.framework.TestCase;

public class BlockCreateEventTest extends TestCase
{
	private BlockCreateEvent event;
	private Entity entity;
	private RigidBody rigidBody;
	private final Vector3f gravity = new Vector3f(0f, -10f, 0f);
	
	public BlockCreateEventTest(String name)
	{
		super(name);
		final DefaultMotionState state = new DefaultMotionState();
		final CollisionShape shape = new BoxShape(new Vector3f(1f, 1f, 1f));
		rigidBody = new RigidBody(1f, state, shape);
		entity = new Entity("TEST", rigidBody, "imagePath", new String[]{"description", "line2"});
		entity.setGravity(gravity);
		event = new BlockCreateEvent(entity);
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
			final String image = (String) data.get("entity.image");
			assertTrue("Entity image path needs to be the same", entity.getImage().equals(image));
			final String gravityString = (String) data.get("entity.gravity");
			assertTrue("Gravity string needs to be the same", gravity.toString().equals(gravityString));
			final float mass = ((Float) data.get("entity.rigidbody.rigidbodyconstructioninfo.mass")).floatValue();
			assertTrue("Mass needs to be the same", mass == (1f/entity.getRigidBody().getInvMass()));
			final float angularfactor = ((Float) data.get("entity.rigidbody.angularfactor")).floatValue();
			assertTrue("Angular Factor needs to be the same", event.getEntity().getRigidBody().getAngularFactor() == angularfactor);
			final String angularvelocity = (String) data.get("entity.rigidbody.angularvelocity");
			assertTrue("Angular Velocity needs to be the same", event.getEntity().getRigidBody().getAngularVelocity(new Vector3f()).toString().equals(angularvelocity));
			final String linearvelocity = (String) data.get("entity.rigidbody.linearvelocity");
			assertTrue("Linear Velocity needs to be the same", event.getEntity().getRigidBody().getLinearVelocity(new Vector3f()).toString().equals(linearvelocity));
			final float lineardampening = ((Float) data.get("entity.rigidbody.lineardampening")).floatValue();
			assertTrue("Linear damping needs to be the same", event.getEntity().getRigidBody().getLinearDamping() == lineardampening);
			final float angulardampening = ((Float) data.get("entity.rigidbody.angulardampening")).floatValue();
			assertTrue("Angular damping needs to be the same", event.getEntity().getRigidBody().getAngularDamping() == angulardampening);
			final String center = (String) data.get("entity.rigidbody.center");
			assertTrue("Center needs to be the same", event.getEntity().getRigidBody().getCenterOfMassPosition(new Vector3f()).toString().equals(center));
			final String shapeClass = (String) data.get("entity.rigidbody.collisionshape.class");
			assertTrue("CollisionShape class needs to exist", event.getEntity().getRigidBody().getCollisionShape().getClass().toString().contains(shapeClass));
		}
		catch(Exception e)
		{
			fail("Exception: " + e.getMessage());
		}
	}
	
	//TODO test getData for different shapes
	
	@Test
	public void testModifyRigidBody()
	{
		final DefaultMotionState state = new DefaultMotionState();
		final CollisionShape shape = new SphereShape(1f);
		rigidBody = new RigidBody(10f, state, shape);
		assertFalse("RigidBody of an event's entity should not be modified", event.getEntity().getRigidBody().equals(rigidBody));
	}
	
	@Test
	public void testModifyEntity()
	{
		entity = new Entity("TEST2", rigidBody);
		assertFalse("Entity of an event should not be modified", event.getEntity().equals(entity));
	}
	
}
