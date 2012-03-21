package com.ATeam.twoDotFiveD.event.block;

import com.ATeam.twoDotFiveD.block.Block;
import com.ATeam.twoDotFiveD.entity.Entity;

public class BlockMoveEvent extends BlockEvent {

	//TODO is this even needed anymore?
	public BlockMoveEvent(Entity entity) {
		super(Type.BLOCK_MOVE, entity);
		// TODO Auto-generated constructor stub
	}
	
	public void notify(BlockListener listener)
	{
		listener.onBlockMove(this);
	}

}
