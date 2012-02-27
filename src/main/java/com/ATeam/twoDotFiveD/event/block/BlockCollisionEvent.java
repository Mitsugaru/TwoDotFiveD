package com.ATeam.twoDotFiveD.event.block;

import javax.vecmath.Vector3f;

import com.ATeam.twoDotFiveD.block.Block;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.dynamics.RigidBody;

public class BlockCollisionEvent extends BlockEvent
{
	private PersistentManifold persistentManifold;
	
	public BlockCollisionEvent(PersistentManifold persistentManifold)
	{
		//TODO grab main block?
		super(Type.BLOCK_COLLISION, null);
		this.persistentManifold = persistentManifold;
		// TODO Auto-generated constructor stub
		System.out.println("Collsion of " + persistentManifold.getBody0().toString() + " and " + persistentManifold.getBody1().toString());
		if (persistentManifold.getBody0() instanceof RigidBody
				&& persistentManifold.getBody1() instanceof RigidBody)
		{
			RigidBody objA = (RigidBody) persistentManifold
					.getBody0();
			RigidBody objB = (RigidBody) persistentManifold
					.getBody1();
			
			System.out.println("Collision with: " + objA.toString()
					+ " and " + objB.toString());
			System.out.println("ObjectA state: " + objA.getActivationState());
			System.out.println("ObjectB state: " + objB.getActivationState());
			int numContacts = persistentManifold.getNumContacts();
			for (int j = 0; j < numContacts; j++)
			{
				ManifoldPoint point = persistentManifold.getContactPoint(j);
				if (point.getDistance() < 0f)
				{
					final Vector3f pointA = point.positionWorldOnA;
					final Vector3f pointB = point.positionWorldOnB;
					final Vector3f normal = point.normalWorldOnB;
					System.out.println("Contact at: "
							+ pointA.toString().toString() + ","
							+ pointB.toString().toString() + " Normal:"
							+ normal.toString().toString());
				}
			}
		}
		else
		{
			System.out.println("Body 0 class: " + persistentManifold.getBody0().getClass().toString());
			System.out.println("Body 1 class: " + persistentManifold.getBody1().getClass().toString());
		}
	}

	
	
}
