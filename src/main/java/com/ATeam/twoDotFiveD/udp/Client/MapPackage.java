package com.ATeam.twoDotFiveD.udp.Client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MapPackage implements Serializable
{
	final private Map<String, Object> data = new HashMap<String, Object>();
	
	public MapPackage()
	{
		
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
