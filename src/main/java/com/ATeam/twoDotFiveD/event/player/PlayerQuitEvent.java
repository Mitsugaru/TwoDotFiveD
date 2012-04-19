package com.ATeam.twoDotFiveD.event.player;

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
}
