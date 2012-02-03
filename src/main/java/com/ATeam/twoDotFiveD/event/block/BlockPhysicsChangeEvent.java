package com.ATeam.twoDotFiveD.event.block;

import com.ATeam.twoDotFiveD.block.Block;

public class BlockPhysicsChangeEvent extends BlockEvent {

	//TODO does this need the specific physics? IDK how we're handling that
	public BlockPhysicsChangeEvent(Block block) {
		super(Type.BLOCK_PHYSICS_CHANGE, block);
		// TODO Auto-generated constructor stub
	}

}
