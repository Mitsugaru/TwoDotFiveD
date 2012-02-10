package com.ATeam.twoDotFiveD.chat;

import java.util.ArrayList;


public class Room
{
    private String password;

    private String name;
    
    private chatServer server;

    private ArrayList<clientHandler> clientlist = new ArrayList<clientHandler>();

    public String getName(){
        return name;
    }

    public Room( String name, String password , chatServer server)
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
        if ( o instanceof Room )
        {
            Room room = (Room)o;
            if ( this.name.equals(room.name ))
            {
                return true;
            }
        }
        return false;
    }


    // Called by chatServer, sends the message to the client. The chatServer
    // needs to append the sending players name!!!!!
    public void send( String message )
    {
        for ( clientHandler c : clientlist )
        {
            message = name + ": " + message;
            c.send( message );
        }
    }


    public boolean addPlayer( clientHandler client, String password )
    {
        if(clientlist.contains( client )){
            return false;
        }
        if ( this.password == "" )
        {
            clientlist.add( client );
            return true;
        }
        if ( password == "" )
        {
            return false;
        }
        if ( this.password == password )
        {
            clientlist.add( client );
            return true;
        }
        return false;
    }
    public void removePlayer(clientHandler client){
        clientlist.remove( client );
        if(clientlist.isEmpty()){
            server.removeRoom( this );
        }
    }
}
