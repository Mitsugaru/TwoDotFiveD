package com.ATeam.twoDotFiveD.chatServer;

import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class clientHandler extends Thread
{
    private String name;

    private Scanner in;

    private PrintWriter out;

    private boolean stop;

    private String defaultroom;

    private chatServer server;

    private ArrayList<Room> rooms = new ArrayList<Room>();

    private InetAddress address;

    private int port;


    public void set( InetAddress anAddress, int aPort )
    {
        address = anAddress;
        port = aPort;
    }


    public DatagramPacket message( byte[] data )
    {
        return new DatagramPacket( data, data.length, address, port );
    }


    public boolean active()
    {
        return address != null;
    }


    public clientHandler( chatServer server, Socket socket )
    {
        stop = false;
        this.server = server;
        defaultroom = "Default";
        try
        {
            in = new Scanner( socket.getInputStream() );
            out = new PrintWriter( socket.getOutputStream(), true );
        }
        catch ( Exception e )
        {

        }
    }


    @Override
    public void run()
    {
        String[] playerList = server.getClientList();
        if ( playerList != null )
        {
            for ( int i = 0; i < playerList.length; i++ )
            {
                out.println( "[[Servermessage]] add " + playerList[i] );
            }
        }
        name = in.nextLine();
        server.addClient( this );
        // TODO send ID
        // TODO something UDP
        String[] roomList = server.getRoomList();
        if ( roomList != null )
        {
            for ( int i = 0; i < roomList.length; i++ )
            {
                out.println( "[[Servermessage]] roomadd [" + roomList[i] + "]" );
            }
        }
        send( "Type /help for commands" );
        server.sendToAll( "[[Servermessage]] add " + name );
        addRoom( server.getDefault() );
        server.getDefault().addPlayer( this, "" );
        while ( !stop )
        {
            try
            {
                String text = in.nextLine();
                server.throwup( name + ": " + text );
                handle( text );
            }
            catch ( NoSuchElementException e )
            {
                e.printStackTrace();
                close();
            }
        }
    }


    public String getname()
    {
        return name;
    }


    public void close()
    {
        stop = true;
        server.sendToAll( "[[Servermessage]] remove " + name );
        in.close();
        out.close();
        server.removePlayer( this );
    }


    public void handle( String text )
    {
        if ( text.length() < 1 )
        {

        }
        else
        {
            if ( text.charAt( 0 ) != '/' )
            {
                for ( Room r : rooms )
                {
                    if ( r.getName().equals( defaultroom ) )
                    {
                        r.send( name + ": " + text );
                    }
                }
            }
            else
            {
                Scanner temp = new Scanner( text ).useDelimiter( " " );
                String command = temp.next();
                if ( command.equals( "/create" ) || command.equals( "/join" )
                    || command.equals( "/leave" ) || command.equals( "/room" )
                    || command.equals( "/setdefault" )
                    || command.equals( "/whisper" ) || command.equals( "/list" )
                    || command.equals( "/who" ) || command.equals( "/whoall" )
                    || command.equals( "/listall" ) || command.equals( "/help" )
                    || command.equals( "/////exit" ) )
                {
                    if ( command.equals( "/////exit" ) )
                    {
                        close();
                    }
                    if ( command.equals( "/help" ) )
                    {
                        send( "Command list:" );
                        send( "/help lists all available commands." );
                        send( "/create <roomname> <password> creates a room with the given name and password. Password is optional." );
                        send( "/join <roomname> <password> Joins a room. Passwords must match. You do not need to enter a password for a room that doesn't have one." );
                        send( "/leave <roomname> Leave a room." );
                        send( "/room <message> Send a message to the players in that room, if you are in that room." );
                        send( "/setdefault <roomname> Sets your default room to a specific room. All messages not preceeded by a command will go to that room." );
                        send( "/whisper <playername> <message> whispers a player" );
                        send( "/list lists the rooms that you are in." );
                        send( "/listall lists all of the rooms on the server." );
                        send( "/who lists all of the players in the rooms you are in." );
                        send( "/whoall lists all of the players in the game." );
                    }
                    if ( command.equals( "/listall" ) )
                    {
                        String[] list = server.getRoomList();
                        String message = "The rooms in the game are: ";
                        for ( int i = 0; i < list.length; i++ )
                        {
                            if ( i == 0 )
                            {
                                message += list[i];
                            }
                            else
                            {
                                message += ", " + list[i];
                            }
                        }
                        send( message );
                    }
                    if ( command.equals( "/who" ) )
                    {
                        String message = "";
                        for ( Room r : rooms )
                        {
                            message += "In [" + r.getName() + "]:";
                            String[] list = r.getPlayers();
                            for ( int i = 0; i < list.length; i++ )
                            {
                                if ( i == 0 )
                                {
                                    message += list[i];
                                }
                                else
                                {
                                    message += ", " + list[i];
                                }
                            }
                            send( message );
                            message = "";
                        }
                    }

                    if ( command.equals( "/whoall" ) )
                    {
                        String[] list = server.getClientList();
                        String message = "";
                        for ( int i = 0; i < list.length; i++ )
                        {
                            if ( i == 0 )
                            {
                                message += list[i];
                            }
                            else
                            {
                                message += ", " + list[i];
                            }
                        }
                        send( message );
                    }
                    if ( command.equals( "/create" ) )
                    {
                        if ( temp.hasNext() )
                        {
                            String arg = temp.nextLine();
                            String[] args = arg.split( " " );
                            if ( args.length < 2 || args.length > 3 )
                            {
                                send( "Invalid command" );
                            }
                            else
                            {
                                if ( args.length == 2 )
                                {
                                    if ( server.createRoom( args[1], this, "" ) )
                                    {
                                        send( args[1] + " has been created" );
                                        send( "You are now in " + args[1] );
                                    }
                                    else
                                    {
                                        send( "Room cannot be created" );
                                    }
                                }
                                if ( args.length == 3 )
                                {
                                    if ( server.createRoom( args[1],
                                        this,
                                        args[2] ) )
                                    {
                                        send( args[1] + " has been created." );
                                        send( "You are now in " + args[1] );
                                    }
                                    else
                                    {
                                        send( "Room cannot be created" );
                                    }
                                }
                            }
                        }
                        else
                        {
                            send( "Invalid command structure1" );
                        }
                    }
                    if ( command.equals( "/join" ) )
                    {
                        if ( temp.hasNext() )
                        {
                            String arg = temp.nextLine();
                            String[] args = arg.split( " " );
                            if ( args.length < 2 || args.length > 3 )
                            {
                                send( "Invalid command" );
                            }
                            else
                            {
                                if ( args.length == 2 )
                                {
                                    if ( server.joinRoom( args[1], this, "" ) )
                                    {
                                        send( "You are now part of " + args[1] );
                                    }
                                    else
                                    {
                                        send( "Could not join room" );
                                    }
                                }
                                if ( args.length == 3 )
                                {
                                    if ( server.joinRoom( args[1],
                                        this,
                                        args[2] ) )
                                    {
                                        send( "You are now part of " + args[1] );
                                    }
                                    else
                                    {
                                        send( "Could not join room" );
                                    }
                                }
                            }
                        }
                        else
                        {
                            send( "Invalid command structure2" );
                        }
                    }
                    if ( command.equals( "/leave" ) )
                    {
                        if ( temp.hasNext() )
                        {
                            String arg = temp.nextLine();
                            String[] args = arg.split( " " );
                            if ( args.length != 2 )
                            {
                                send( "Invalid command" );
                            }
                            else
                            {
                                for ( Room r : rooms )
                                {
                                    if ( r.getName().equals( args[1] ) )
                                    {
                                        if ( r.getName().equals( defaultroom ) )
                                        {
                                            send( "You may not leave your default room" );
                                        }
                                        else
                                        {
                                            r.removePlayer( this );
                                            removeRoom( r );
                                            send( "You are no longer in "
                                                + r.getName() );
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            send( "Invalid command structure3" );
                        }
                    }
                    if ( command.equals( "/room" ) )
                    {
                        if ( temp.hasNext() )
                        {
                            String arg = temp.nextLine();
                            String[] args = arg.split( " " );
                            if ( args.length < 3 )
                            {
                                send( "Invalid command" );
                            }
                            else
                            {
                                for ( Room r : rooms )
                                {
                                    if ( r.getName().equals( args[1] ) )
                                    {
                                        String message = name + ":";
                                        for ( int i = 2; i < args.length; i++ )
                                        {
                                            message = message + " " + args[i];
                                        }
                                        r.send( message );
                                    }
                                }
                            }
                        }
                        else
                        {
                            send( "Invalid command structure4" );
                        }
                    }
                    if ( command.equals( "/setdefault" ) )
                    {
                        if ( temp.hasNext() )
                        {
                            String arg = temp.nextLine();
                            String[] args = arg.split( " " );
                            if ( args.length != 2 )
                            {
                                send( "Invalid command" );
                            }
                            else
                            {
                                boolean found = false;
                                for ( Room r : rooms )
                                {
                                    if ( r.getName().equals( args[1] ) )
                                    {
                                        defaultroom = args[1];
                                        found = true;
                                        send( "Your default room is now "
                                            + args[1] );
                                    }
                                }
                                if ( !found )
                                {
                                    send( "You are not in that room" );
                                }
                            }
                        }
                        else
                        {
                            send( "Invalid command structure5" );
                        }
                    }
                    if ( command.equals( "/whisper" ) )
                    {
                        if ( temp.hasNext() )
                        {
                            String arg = temp.nextLine();
                            String[] args = arg.split( " " );
                            if ( args.length < 3 )
                            {
                                send( "Invalid command" );
                            }
                            else
                            {
                                String message = args[2];
                                for ( int i = 3; i < args.length; i++ )
                                {
                                    message += " " + args[i];
                                }
                                String sendmessage = name + " whispers '"
                                    + message + "' to you";
                                if ( server.sendToPlayer( args[1], sendmessage ) )
                                {
                                    send( "You whisper '" + message + "' to "
                                        + args[1] );
                                }
                                else
                                {
                                    send( "Could not send message to player" );
                                }
                            }
                        }
                        else
                        {
                            send( "Invalid command structure6" );
                        }
                    }
                    if ( command.equals( "/list" ) )
                    {
                        for ( Room r : rooms )
                        {
                            send( r.getName() );
                        }
                    }
                }
                else
                {
                    send( "Invalid command" );
                }
            }
        }
    }


    public void send( String message )
    {
        out.println( message );
    }


    public void addRoom( Room room )
    {
        this.rooms.add( room );
    }


    public void removeRoom( Room room )
    {
        this.rooms.remove( room );
    }


    public boolean equals( Object o )
    {
        if ( o instanceof clientHandler )
        {
            clientHandler temp = (clientHandler)o;
            if ( temp.name.equals( this.name ) )
            {
                return true;
            }
        }
        return false;
    }

}
