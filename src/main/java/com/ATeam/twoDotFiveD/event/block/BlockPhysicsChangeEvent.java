package com.ATeam.twoDotFiveD.event.block;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3f;

import com.ATeam.twoDotFiveD.entity.Entity;

public class BlockPhysicsChangeEvent extends BlockEvent {
	private Vector3f direction;
	//TODO does this need the specific physics? IDK how we're handling that
	public BlockPhysicsChangeEvent(Entity entity, Vector3f direction) {
		super(Type.BLOCK_PHYSICS_CHANGE, entity);
		this.direction = direction;
	}
	
	public Vector3f getDirection()
	{
		return direction;
	}
	
	public void notify(BlockListener listener)
	{
		listener.onBlockPhysicsChange(this);
	}
	
	@Override
	public Map<String, Object> getData()
	{
		final Map<String, Object> data = new HashMap<String, Object>();
		/**
		 * Entity
		 */
		//Class
		data.put("class", name);
		//Entity
		data.put("entity.ID", getEntity().getID());
		/**
		 * Direction
		 */
		data.put("entity.direction", direction.toString());
		return data;
	}

}
