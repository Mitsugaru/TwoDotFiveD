package com.ATeam.twoDotFiveD.event.block;

import com.ATeam.twoDotFiveD.block.Block;

public class BlockCreateEvent extends BlockEvent {

	public BlockCreateEvent(Block block) {
		super(Type.BLOCK_CREATE, block);
		// TODO Auto-generated constructor stub
	}
	
	public void notify(BlockListener listener)
	{
		listener.onBlockCreate(this);
	}
}
