package com.ATeam.twoDotFiveD.event.player;

import com.ATeam.twoDotFiveD.event.Listener;

/**
 * Handles all events thrown in relation to a Player
 * @author Tokume
 *
 */
public abstract class PlayerListener implements Listener {

	public PlayerListener(){}

	/**
	 * Called when a player moves
	 * @param Event details
	 */
	public void onPlayerMove(PlayerMoveEvent playerMoveEvent){}

	/**
	 * Called when a player joins
	 * @param Event details
	 */
	public void onPlayerJoin(PlayerJoinEvent playerJoinEvent){}
	
	/**
	 * Called when a player quits
	 * @param Event details
	 */
	public void onPlayerQuit(PlayerQuitEvent playerQuitEvent){}
}
