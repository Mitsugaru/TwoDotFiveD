package com.ATeam.twoDotFiveD.event.player;

import com.ATeam.twoDotFiveD.entity.Player;
import com.ATeam.twoDotFiveD.event.Event;

public class PlayerEvent extends Event<PlayerListener>{
	protected Player player;

	/**
	 * Constructor
	 *
	 * @param Player associated with event
	 */
	public PlayerEvent(final Event.Type type, final Player player)
	{
		super(type);
		this.player = player;
	}

	/**
	 * Returns player associated with event
	 *
	 * @return Player
	 */
	public Player getPlayer()
	{
		return player;
	}

	/**
	 * This method is to be overridden by all subclasses
	 */
	public void notify(PlayerListener listener) {
	}
}
