package com.ATeam.twoDotFiveD.event;

import com.ATeam.twoDotFiveD.Debug.Logging;
import com.ATeam.twoDotFiveD.event.player.*;

/**
 * Class to represent an event.
 * <p>
 * Fusion of the example from http://www.stackoverflow.com/questions/937302/simple-java-message-dispatching-system
 * and Bukkit's implementation of an event system
 *
 * @author Mitsugaru
 *
 */
public abstract class Event<L> {
	public abstract void notify(final L listener);
	private final Type type;
	private final String name;

	public Event(){
		this.type = Type.FIXED_EVENT;
		this.name = getClass().getName();
	}

	public Event(Type type) {
		if(type == null)
		{
			Logging.log.severe("Class '"+ getClass().getName() + "' gave a null type!");
			throw new IllegalArgumentException("type is null");
		}
		this.type = type;
		this.name = getClass().getName();
	}

	public Type getType()
	{
		return type;
	}

	public String getName()
	{
		return name;
	}

	//TODO add more events
	@SuppressWarnings("rawtypes")
	public enum Type{
		/**
		 * Player Events
		 */
		/**
		 * Called when a player moves
		 */
		PLAYER_MOVE(Category.PLAYER, PlayerMoveEvent.class),
		/**
		 * Miscellaneous Event
		 */
		FIXED_EVENT(Category.MISCELLANEOUS, Event.class);

		private final Category category;
		private final Class<? extends Event> origin;

		private Type(Category category, Class<? extends Event> origin)
		{
			this.category = category;
			this.origin = origin;
		}

		public Category getCategory()
		{
			return category;
		}

		public Class<? extends Event> getEventClass()
		{
			return origin;
		}
	}

	public enum Category
	{
		PLAYER,BLOCK,MISCELLANEOUS;
	}

	//TODO implement prioritized listeners
	public enum Priority
	{

	}
}
