package com.ATeam.twoDotFiveD.event.player;

import com.ATeam.twoDotFiveD.entity.Entity;
import com.ATeam.twoDotFiveD.event.Event;

public class PlayerEvent extends Event<PlayerListener>{
	protected Entity player;

	/**
	 * Constructor
	 *
	 * @param Event type
	 * @param Player associated with event
	 */
	public PlayerEvent(final Event.Type type, final Entity player)
	{
		super(type);
		this.player = player;
	}

	/**
	 * Returns player associated with event
	 *
	 * @return Player
	 */
	public Entity getPlayer()
	{
		return player;
	}

	/**
	 * This method is to be overridden by all subclasses
	 */
	public void notify(PlayerListener listener) {
	}
}
