package com.ATeam.twoDotFiveD.event.block;

import java.util.HashMap;
import java.util.Map;

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

	@Override
	public Map<String, Object> getData()
	{
		final Map<String, Object> data = new HashMap<String, Object>();
		data.put("class", name);
		data.put("entity.gravity.x", getEntity().getGravity().x);
		data.put("entity.gravity.y", getEntity().getGravity().y);
		data.put("entity.gravity.z", getEntity().getGravity().z);
		return data;
	}
	
	
}
