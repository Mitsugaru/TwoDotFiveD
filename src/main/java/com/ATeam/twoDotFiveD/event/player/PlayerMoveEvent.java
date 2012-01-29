package com.ATeam.twoDotFiveD.event.player;

import com.ATeam.twoDotFiveD.entity.Player;

public class PlayerMoveEvent extends PlayerEvent {

	public PlayerMoveEvent(Player player)
	{
		super(Type.PLAYER_MOVE, player);
	}

	public void notify(PlayerListener listener) {
		listener.onPlayerMove(this);
	}

}
