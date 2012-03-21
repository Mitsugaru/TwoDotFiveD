package com.ATeam.twoDotFiveD.event.block;

import com.ATeam.twoDotFiveD.block.Block;
import com.ATeam.twoDotFiveD.entity.Entity;

public class BlockDestroyedEvent extends BlockEvent {

	public BlockDestroyedEvent(Entity entity) {
		//TODO replace block with entity;
		super(Type.BLOCK_DESTROYED, entity);
	}

	public void notify(BlockListener listener)
	{
		listener.onBlockDestroyed(this);
	}
}
