package com.ATeam.twoDotFiveD.event.block;

import javax.vecmath.Vector3f;

import com.ATeam.twoDotFiveD.block.Block;
import com.ATeam.twoDotFiveD.entity.Entity;

public class BlockPhysicsChangeEvent extends BlockEvent {
	private Vector3f direction;
	//TODO does this need the specific physics? IDK how we're handling that
	public BlockPhysicsChangeEvent(Entity entity, Vector3f direction) {
		super(Type.BLOCK_PHYSICS_CHANGE, entity);
		this.direction = direction;
	}
	
	public Vector3f getDirection()
	{
		return direction;
	}
	
	public void notify(BlockListener listener)
	{
		listener.onBlockPhysicsChange(this);
	}

}
