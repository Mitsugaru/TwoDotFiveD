package com.ATeam.twoDotFiveD.event.block;

import com.ATeam.twoDotFiveD.block.Block;
import com.ATeam.twoDotFiveD.entity.Entity;

public class BlockCreateEvent extends BlockEvent{

	public BlockCreateEvent(Entity entity) {
		//TODO replace block with entity;
		super(Type.BLOCK_CREATE, entity);
	}
	
	public void notify(BlockListener listener)
	{
		listener.onBlockCreate(this);
	}
}
