package com.ATeam.twoDotFiveD.event.player;

import java.util.HashMap;
import java.util.Map;

import com.ATeam.twoDotFiveD.entity.Entity;

public class PlayerQuitEvent extends PlayerEvent {

	public PlayerQuitEvent(Entity player) {
		super(Type.PLAYER_QUIT, player);
		// TODO Auto-generated constructor stub
	}

	public void notify(PlayerListener listener)
	{
		listener.onPlayerQuit(this);
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
		data.put("entity.ID", getPlayer().getID());
		return data;
	}
}
