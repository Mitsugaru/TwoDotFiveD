package com.ATeam.twoDotFiveD.udp.Client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.ATeam.twoDotFiveD.event.Event;

public class EventPackage implements Serializable
{
	final private Map<String, Object> data = new HashMap<String, Object>();
	
	public EventPackage(Event<?> event)
	{
		for(Map.Entry<String, Object> entry : event.getData().entrySet())
		{
			put(entry.getKey(), entry.getValue());
		}
	}
	
	public void put(String path, Object o)
	{
		data.put(path, o);
	}
	
	public Map<String, Object> getData()
	{
		return data;
	}
}
