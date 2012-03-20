package com.ATeam.twoDotFiveD.event.block;

import javax.vecmath.Vector3f;
import com.ATeam.twoDotFiveD.music.*;
import com.ATeam.twoDotFiveD.block.Block;
import com.ATeam.twoDotFiveD.event.player.PlayerListener;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.dynamics.RigidBody;

public class BlockCollisionEvent extends BlockEvent
{
	private PersistentManifold	persistentManifold;
	private int					objectAActivationState;
	private int					objectBActivationState;
	private MusicPlayer mp=new MusicPlayer();
	
	public BlockCollisionEvent(PersistentManifold persistentManifold)
	{
		
		// TODO grab main block?
		super(Type.BLOCK_COLLISION, null);
		//Set PersistentManifold
		this.persistentManifold = persistentManifold;
		//Check object types
		if (persistentManifold.getBody0() instanceof RigidBody
				&& persistentManifold.getBody1() instanceof RigidBody)
		{
			RigidBody objA = (RigidBody) persistentManifold.getBody0();
			RigidBody objB = (RigidBody) persistentManifold.getBody1();
			//Grab object activation state
			objectAActivationState = objA.getActivationState();
			objectBActivationState = objB.getActivationState();
			
			//mp.createThread(4);//number in array, this case Explosion.wav 
			
			// If both object states are 2, then they are both deactivated... so
			// we shouldn't care? Maybe we might care, but probably not?
			/*if (objectAActivationState == 2 && objectBActivationState == 2)
			{
				// More than likely duplicate / spam event. Ignore?
			}
			else
			{
				System.out.println(objA.getCollisionShape().getName());
				System.out.println(objB.getCollisionShape().getName());
				//System.out.println("Collision with: " + objA.toString() + " and "
				//		+ objB.toString());
				/*int numContacts = persistentManifold.getNumContacts();
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
			*/
		}
		else
		{
			//Some other type of class. Need appropriate way to deal with them.
			//Other type of physics object other than RigidBody
			System.out.println("Body 0 class: "
					+ persistentManifold.getBody0().getClass().toString());
			System.out.println("Body 1 class: "
					+ persistentManifold.getBody1().getClass().toString());
		}
	}
	
	public PersistentManifold getPersistentManifold()
	{
		return persistentManifold;
	}
	public void notify(BlockListener listener) {
		listener.onBlockCollision(this);
	}
}
