package com.ATeam.twoDotFiveD.event.block;

import com.ATeam.twoDotFiveD.block.Block;
import com.ATeam.twoDotFiveD.entity.Entity;
import com.ATeam.twoDotFiveD.event.Event;

public class BlockEvent extends Event<BlockListener> {
	private Entity entity;
	
	/**
	 * Constructor
	 *
	 * @param Event type
	 * @param Block associated with event
	 */
	public BlockEvent(final Event.Type type, final Entity entity)
	{
		super(type);
		this.entity = entity;
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
	public Entity getEntity()
	{
		return entity;
	}

}
