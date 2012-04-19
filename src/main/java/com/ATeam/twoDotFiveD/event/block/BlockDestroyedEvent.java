package com.ATeam.twoDotFiveD.event.block;

import java.util.HashMap;
import java.util.Map;

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
	
	@Override
	public Map<String, Object> getData()
	{
		//System.out.println("Get data for: " + name);
		final Map<String, Object> data = new HashMap<String, Object>();
		/**
		 * Entity
		 */
		//Class
		data.put("class", name);
		//Entity
		data.put("entity.ID", getEntity().getID());
		return data;
	}
}
