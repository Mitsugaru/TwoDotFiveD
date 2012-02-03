package com.ATeam.twoDotFiveD.event.player;

import com.ATeam.twoDotFiveD.entity.Player;

public class PlayerQuitEvent extends PlayerEvent {

	public PlayerQuitEvent(Player player) {
		super(Type.PLAYER_QUIT, player);
		// TODO Auto-generated constructor stub
	}

	public void notify(PlayerListener listener)
	{
		listener.onPlayerQuit(this);
	}
}
