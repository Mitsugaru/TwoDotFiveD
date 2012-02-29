package com.ATeam.twoDotFiveD.server;

import java.util.ArrayList;


public class chatRoom
{
    private String password;
    private String name;
    private chatServer server;
    private ArrayList<chatClientHandler> clientlist = new ArrayList<chatClientHandler>();

    public String getName(){
        return name;
    }

    public chatRoom( String name, String password , chatServer server)
    {
        this.name = name;
        this.server = server;
        if(password!=null){
        this.password = password;
        }
        else{
            this.password = "";
        }
    }


    // absolutely necessary to use the contains method for arraylists
    public boolean equals( Object o )
    {
        if ( o instanceof chatRoom )
        {
            chatRoom room = (chatRoom)o;
            if ( this.name.equals(room.name ))
            {
                return true;
            }
        }
        return false;
    }


    // Called by chatServer, sends the message to the client. The chatServer
    // needs to append the sending players name!!!!!
    public synchronized void send( String message )
    {
        for ( chatClientHandler c : clientlist )
        {
            c.send( "[" + name + "] " + message );
        }
    }


    public boolean addPlayer( chatClientHandler client, String password )
    {
        if(clientlist.contains( client )){
            return false;
        }
        if ( this.password.equals( "") )
        {
            send(client.getname() + " has entered [" + name + "]" );
            clientlist.add( client );
            return true;
        }
        if ( password.equals( "") )
        {
            return false;
        }
        if ( this.password.equals(password) )
        {
            send(client.getname() + " has entered [" + name + "]" );
            clientlist.add( client );
            return true;
        }
        return false;
    }
    public void removePlayer(chatClientHandler client){
        clientlist.remove( client );
        if(clientlist.isEmpty() && name!= "Default"){
            server.removeRoom( this );
        }
    }
    
    public String[] getPlayers(){
        String[] list = new String[clientlist.size()];
        int i = 0;
        for (chatClientHandler c : clientlist){
            list[i] = c.getname();
            i++;
        }
        return list;
    }
}
