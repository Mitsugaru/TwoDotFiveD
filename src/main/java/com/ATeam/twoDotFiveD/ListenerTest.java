package com.ATeam.twoDotFiveD;

import com.ATeam.twoDotFiveD.debug.Logging;
import com.ATeam.twoDotFiveD.event.player.PlayerListener;
import com.ATeam.twoDotFiveD.event.player.PlayerMoveEvent;

public class ListenerTest extends PlayerListener {

	@Override
	public void onPlayerMove(PlayerMoveEvent event)
	{
		//Log info on event
		Logging.log.info("Received event");
		Logging.log.info("Event name: " + event.getName());
		Logging.log.info("Event type: " + event.getType());
		Logging.log.info("Event type category: " + event.getType().getCategory());
		Logging.log.info("Event type class: " + event.getType().getEventClass().getName());
	}
}
