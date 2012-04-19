package com.ATeam.twoDotFiveD.event.player;

import com.ATeam.twoDotFiveD.entity.Entity;


public class PlayerJoinEvent extends PlayerEvent {

	public PlayerJoinEvent(Entity player) {
		super(Type.PLAYER_JOIN, player);
		// TODO Auto-generated constructor stub
	}
	
	public void notify(PlayerListener listener)
	{
		listener.onPlayerJoin(this);
	}
}
