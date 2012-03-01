package com.ATeam.twoDotFiveD.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.ATeam.twoDotFiveD.Player;
import com.ATeam.twoDotFiveD.chatServer.clientHandler;


public class chatServer implements Runnable
{
	public final static int DEFAULTCHATPORT=1337;
	private ArrayList<client> players;
	//private ArrayList<chatClientHandler> clientlist = new ArrayList<chatClientHandler>();
	private ArrayList<chatRoom> rooms = new ArrayList<chatRoom>();
	private chatRoom Default;
	private int port;
	private boolean run;

	// Start the server, create a room called default, accept connections
	public chatServer(ArrayList<client> thePlayers, int aPort)
	{
		init(thePlayers,aPort);
	}
	public chatServer(ArrayList<client> thePlayers){
		init(thePlayers,DEFAULTCHATPORT);
	}
	private void init(ArrayList<client> thePlayers, int aPort){
		port=aPort;
		players=thePlayers;
		run=true;
	}

	@Override
	public void run() {
		try
		{
			ServerSocket server = new ServerSocket( port );
			Default = new chatRoom( "Default", "", this );
			rooms.add( Default );
			while ( run )
			{
				Socket socket = server.accept();
				client c = new client(this, socket);
				newID(c);
				players.add(c);
				new Thread(c).run();

				//chatClientHandler client = new chatClientHandler( this, socket );
				//client.start();
			}
			server.close();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	// If the room already exists, return false. If the room doesn't exist, add
	// it to the list and return true
	public boolean createRoom(
			String name,
			chatClientHandler client,
			String password )
	{
		chatRoom temp = new chatRoom( name, password, this );
		if ( rooms.contains( temp ) )
		{
			temp = null;
			return false;
		}
		else
		{
			rooms.add( temp );
			temp.addPlayer( client, password );
			client.addRoom( temp );
			sendToAll( "[[Servermessage]] roomadd [" + name + "]" );
			return true;
		}
	}


	public boolean joinRoom( String name, chatClientHandler client, String password )
	{
		for ( chatRoom r : rooms )
		{
			if ( r.getName().equals( name ) )
			{
				if ( r.addPlayer( client, password ) )
				{
					client.addRoom( r );
					return true;
				}
			}
		}
		return false;
	}






	public boolean sendToPlayer( String playername, String message )
	{
		for ( clientHandler c : clientlist )
		{
			if ( c.getname().equals( playername ) )
			{
				c.send( message );
				return true;
			}
		}
		return false;
	}


	public void removeRoom( chatRoom r )
	{
		sendToAll( "[[Servermessage]] roomdelete [" + r.getName() + "]" );
		rooms.remove( r );
	}


	public void sendToAll( String message )
	{
		for ( chatClientHandler c : clientlist )
		{
			c.send( message );
		}
	}


	public String[] getClientList()
	{
		String[] list = new String[clientlist.size()];
		for ( int i = 0; i < clientlist.size(); i++ )
		{
			list[i] = clientlist.get( i ).getname();
		}
		return list;
	}


	public String[] getRoomList()
	{
		String[] list = new String[rooms.size()];
		for ( int i = 0; i < rooms.size(); i++ )
		{
			list[i] = rooms.get( i ).getName();
		}
		return list;
	}


	public void removePlayer( chatClientHandler client )
	{
		clientlist.remove( client );
		for ( chatRoom r : rooms )
		{
			r.removePlayer( client );
		}
	}


	public chatRoom getDefault()
	{
		return Default;
	}
	//sets the run variable to false
	//connects a null client and exits
	public void quit(){
		run=false;
		try {
			Socket nullclient=new Socket("127.0.0.1",port);
			nullclient.close();			
		} 
		catch (UnknownHostException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();}
	}
	public void newID(client c,boolean Override){
		if(Override||c.getID()==-1)
			c.setID(findAvailableID());
	}
	//sets the ID to an available ID only if the ID has yet to be initialized
	public void newID(client c){
		newID(c,false);
	}
	//finds an available ID number.
	private int findAvailableID(){
		if(players.isEmpty())
			return 1;
		else if(players.get(players.size()-1).getID()==players.size())
			return players.size();
		else
			for(int i=0;i<players.size();i++)
				if(players.get(i).getID()!=i+1)
					return i+1;
		return -1;
	}
}