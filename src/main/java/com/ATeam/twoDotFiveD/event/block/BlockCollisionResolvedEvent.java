package com.ATeam.twoDotFiveD.event.block;

import com.bulletphysics.collision.narrowphase.PersistentManifold;

public class BlockCollisionResolvedEvent extends BlockEvent
{
	private PersistentManifold pm;
	
	public BlockCollisionResolvedEvent(PersistentManifold pm)
	{
		super(Type.BLOCK_COLLISION_RESOLVED, null);
		this.pm = pm;
	}
	
	public PersistentManifold getPersistentManifold()
	{
		return pm;
	}
	
	public void notify(BlockListener listener)
	{
		listener.onBlockCollisionResolved(this);
	}
}
