package com.ATeam.twoDotFiveD.event.block;

import com.ATeam.twoDotFiveD.block.Block;
import com.ATeam.twoDotFiveD.event.Event;

public class BlockEvent extends Event<BlockListener> {
	private Block block;
	
	/**
	 * Constructor
	 *
	 * @param Event type
	 * @param Block associated with event
	 */
	public BlockEvent(final Event.Type type, final Block block)
	{
		super(type);
		this.block = block;
	}
	
	/**
	 * This method is to be overridden by all subclasses
	 */
	public void notify(BlockListener listener) {}
	
	/**
	 * Get block associated with event
	 * 
	 * @return Block associated with event
	 */
	public Block getBlock()
	{
		return block;
	}

}
