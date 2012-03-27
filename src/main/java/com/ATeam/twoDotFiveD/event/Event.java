package com.ATeam.twoDotFiveD.event;

import java.util.HashMap;
import java.util.Map;

import com.ATeam.twoDotFiveD.debug.Logging;
import com.ATeam.twoDotFiveD.event.block.BlockCollisionEvent;
import com.ATeam.twoDotFiveD.event.block.BlockCollisionResolvedEvent;
import com.ATeam.twoDotFiveD.event.block.BlockCreateEvent;
import com.ATeam.twoDotFiveD.event.block.BlockDestroyedEvent;
import com.ATeam.twoDotFiveD.event.block.BlockMoveEvent;
import com.ATeam.twoDotFiveD.event.block.BlockPhysicsChangeEvent;
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
	protected final Type type;
	protected final String name;
	protected boolean cancelled;

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
	
	public Map<String, Object> getData()
	{
		final Map<String, Object> data = new HashMap<String, Object>();
		data.put("class", name);
		return data;
	}

	public Type getType()
	{
		return type;
	}

	public String getName()
	{
		return name;
	}
	
	public boolean isCancelled()
	{
		return cancelled;
	}
	
	public void setCancelled(boolean c)
	{
		cancelled = c;
	}

	//TODO add more events
	@SuppressWarnings("rawtypes")
	public enum Type{
		/**
		 * Player Events
		 */
		/**
		 * Called when a player joins
		 */
		PLAYER_JOIN(Category.PLAYER, PlayerJoinEvent.class),
		/**
		 * Called when a player quits
		 */
		PLAYER_QUIT(Category.PLAYER, PlayerQuitEvent.class),
		/**
		 * Called when a player moves
		 */
		PLAYER_MOVE(Category.PLAYER, PlayerMoveEvent.class),
		/**
		 * Block Events
		 */
		/**
		 * Called when a block is created
		 */
		BLOCK_CREATE(Category.BLOCK, BlockCreateEvent.class),
		/**
		 * Called when a block is destroyed
		 */
		BLOCK_DESTROYED(Category.BLOCK, BlockDestroyedEvent.class),
		/**
		 * Called when a block is moved
		 */
		BLOCK_MOVE(Category.BLOCK, BlockMoveEvent.class),
		/**
		 * Called when a block's physics has changed
		 */
		BLOCK_PHYSICS_CHANGE(Category.BLOCK, BlockPhysicsChangeEvent.class),
		/**
		 * Called when blocks collide
		 */
		BLOCK_COLLISION(Category.BLOCK, BlockCollisionEvent.class),
		/**
		 * Called when contact between block collisions have been fully resolved
		 */
		BLOCK_COLLISION_RESOLVED(Category.BLOCK, BlockCollisionResolvedEvent.class),
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
		LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR;
	}
}
