package com.ATeam.twoDotFiveD.event;

import java.util.ArrayList;
import java.util.HashMap;

import com.ATeam.twoDotFiveD.debug.Logging;

/**
 * Class to dispatch Events to the registered listeners
 * <p>
 * Based on the example from http://www.stackoverflow.com/questions/937302/simple-java-message-dispatching-system
 * @author Tokume
 *
 */
public class EventDispatcher {
	@SuppressWarnings("rawtypes")
	private final HashMap<Class, ArrayList> map = new HashMap<Class, ArrayList>(
			10);

	/**
	 * Add a listener to an event class
	 *
	 * @param Event Type
	 * @param Listener
	 *            to be added
	 */
	@SuppressWarnings("unchecked")
	public <L> void registerListener(Event.Type type, L listener) {
		final ArrayList<L> listeners = listenersOf((Class<? extends Event<L>>) type.getEventClass());
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			} else {
				// They tried to re-register for the same event, which they
				// should not do...
				Logging.log.warning("Listener '"
						+ listener.getClass().getName()
						+ "' tried to register for event '"
						+ type
						+ "', but it was already registered!");
			}
		}
	}

	/**
	 * Add a listener to an event class
	 *
	 * @param Class
	 *            of event
	 * @param Listener
	 *            to be added
	 */
	@Deprecated
	public <L> void registerListener(Class<? extends Event<L>> eventClass,
			L listener) {
		final ArrayList<L> listeners = listenersOf(eventClass);
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			} else {
				// They tried to re-register for the same event, which they
				// should not do...
				Logging.log.warning("Listener '"
						+ listener.getClass().getName()
						+ "' tried to register for event '"
						+ eventClass.getName()
						+ "', but it was already registered!");
			}
		}
	}

	/**
	 * Remove a listener from getting events for a class
	 *
	 * @param Event Type
	 * @param Listener
	 *            to be removed
	 */
	@SuppressWarnings("unchecked")
	public <L> void removeListener(Event.Type type, L listener) {
		final ArrayList<L> listeners = listenersOf((Class<? extends Event<L>>) type.getEventClass());
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Remove a listener from getting events for a class
	 *
	 * @param Class
	 *            of event
	 * @param Listener
	 *            to be removed
	 */
	@Deprecated
	public <L> void removeListener(Class<? extends Event<L>> eventClass,
			L listener) {
		final ArrayList<L> listeners = listenersOf(eventClass);
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Removes all listeners for a given event class
	 *
	 * @param Event Type
	 */
	@SuppressWarnings("unchecked")
	public <L> void removeAllForEvent(Event.Type type)
	{
		final ArrayList<L> listeners = listenersOf((Class<? extends Event<L>>) type.getEventClass());
		synchronized(listeners)
		{
			for(L listener : listeners)
			{
				listeners.remove(listener);
			}
		}
	}


	/**
	 * Removes all listeners for a given event class
	 *
	 * @param Class of event
	 */
	@Deprecated
	public <L> void removeAllForEvent(Class<? extends Event<L>> eventClass)
	{
		final ArrayList<L> listeners = listenersOf(eventClass);
		synchronized(listeners)
		{
			for(L listener : listeners)
			{
				listeners.remove(listener);
			}
		}
	}

	/**
	 * Removes all listeners and events
	 */
	synchronized public void removeAll()
	{
		map.clear();
	}

	/**
	 * Get listeners for a given event class
	 *
	 * @param Class
	 *            of the event
	 * @return ArrayList of listeners. If no list is found, an empty one is
	 *         generated and added to the HashMap for the class
	 */
	private <L> ArrayList<L> listenersOf(Class<? extends Event<L>> eventClass) {
		synchronized (map) {
			@SuppressWarnings("unchecked")
			final ArrayList<L> existing = map.get(eventClass);
			if (existing != null) {
				return existing;
			}

			final ArrayList<L> emptyList = new ArrayList<L>(5);
			map.put(eventClass, emptyList);
			return emptyList;
		}
	}

	/**
	 * Notify a new event to registered listeners for the event's class
	 *
	 * @param Event
	 *            that occurred
	 */
	public <L> void notify(final Event<L> event) {
		@SuppressWarnings("unchecked")
		Class<Event<L>> eventClass = (Class<Event<L>>) event.getClass();
		for (L listener : listenersOf(eventClass)) {
			event.notify(listener);
		}
	}
}
