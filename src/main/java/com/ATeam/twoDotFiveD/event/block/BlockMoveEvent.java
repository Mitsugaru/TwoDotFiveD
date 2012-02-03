package com.ATeam.twoDotFiveD.event.block;

import com.ATeam.twoDotFiveD.block.Block;

public class BlockMoveEvent extends BlockEvent {

	public BlockMoveEvent(Block block) {
		super(Type.BLOCK_MOVE, block);
		// TODO Auto-generated constructor stub
	}
	
	public void notify(BlockListener listener)
	{
		listener.onBlockMove(this);
	}

}
