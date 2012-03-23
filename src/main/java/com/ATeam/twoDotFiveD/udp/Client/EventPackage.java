package com.ATeam.twoDotFiveD.udp.Client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.ATeam.twoDotFiveD.event.Event;
import com.ATeam.twoDotFiveD.event.Event.Type;
import com.ATeam.twoDotFiveD.event.block.BlockCreateEvent;

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
	
	public void getEvent()
	{
		String className = (String)data.get("class");
		System.out.println(className);
		System.out.println(data.get("entity.gravity.x") + " " + data.get("entity.gravity.y") + " " + data.get("entity.gravity.z"));
	}
}
