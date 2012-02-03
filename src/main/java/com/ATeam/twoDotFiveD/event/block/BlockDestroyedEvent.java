package com.ATeam.twoDotFiveD.event.block;

import com.ATeam.twoDotFiveD.block.Block;

public class BlockDestroyedEvent extends BlockEvent {

	public BlockDestroyedEvent(final Block block) {
		super(Type.BLOCK_DESTROYED, block);
		// TODO Auto-generated constructor stub
	}

	public void notify(BlockListener listener)
	{
		listener.onBlockDestroyed(this);
	}
}
