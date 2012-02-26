package com.ATeam.twoDotFiveD.event.block;

import net.phys2d.raw.Body;
import net.phys2d.raw.CollisionEvent;
import net.phys2d.raw.CollisionListener;

public class BlockCollisionListener implements CollisionListener
{
	//TODO add listener to PhysicsSpace in appropriate class
	//PhysicsSpace.getPhysicsSpace.addCOllisionListgenr(new BlockCollisionListener());
	@Override
	public void collisionOccured(CollisionEvent event)
	{
		// TODO implementation
		// Grab the objects that collided
		final Body bodyA = event.getBodyA();
		final Body bodyB = event.getBodyB();
		// TODO figure out what collided with what
		// TODO handle the interactions between specific types of bodies.
	}
	
}
