package com.ATeam.twoDotFiveD.event.block;

import com.ATeam.twoDotFiveD.event.Listener;

public class BlockListener implements Listener{
	
	public BlockListener(){}
	
	public void onBlockDestroyed(BlockDestroyedEvent blockDestroyedEvent){}

	public void onBlockCreate(BlockCreateEvent blockCreateEvent) {}
	
	public void onBlockMove(BlockMoveEvent blockMoveEvent){}
	
	public void onBlockPhysicsChange(BlockPhysicsChangeEvent blockPhysicsChangeEvent){}
	
	public void onBlockCollision(BlockCollisionEvent blockCollisionEvent){}
	
	public void onBlockCollisionResolved(BlockCollisionResolvedEvent blockCollisionResolvedEvent){}
}
