package com.ATeam.twoDotFiveD.chat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class chatServer
{
    private ArrayList<clientHandler> clientlist = new ArrayList<clientHandler>();

    private ArrayList<Room> rooms = new ArrayList<Room>();

    private JTextArea read;

    private JFrame panel;

    public void throwup(String text){
        read.setText( read.getText() + text
            + System.getProperty( "line.separator" ) );
    }

    public static void main( String[] args )
    {
        new chatServer();
    }


    // If the room already exists, return false. If the room doesn't exist, add
    // it to the list and return true
    public boolean createRoom( String name, clientHandler client, String password )
    {
        Room temp = new Room( name, password, this );
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
            return true;
        }
    }


    public boolean joinRoom( String name, clientHandler client, String password )
    {
        for ( Room r : rooms )
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


    // Start the server, create a room called default, accept connections
    public chatServer()
    {
        //TODO all this is optional
        JFrame frame = new JFrame( "Server Window" );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        Dimension d = new Dimension( 500, 500 );
        frame.setSize( d );
        frame.setPreferredSize( d );
        frame.setMinimumSize( d );
        frame.setVisible( true );
        frame.setLayout( new BorderLayout() );
        JPanel panel = new JPanel();
        panel.setLayout( new BorderLayout() );
        // The panel for all the messages
        read = new JTextArea();
        read.setEditable( false );
        read.setLineWrap( true );
        read.setWrapStyleWord( true );
        JScrollPane scroll = new JScrollPane( read );
        scroll.setSize( read.getSize() );
        panel.add( scroll, BorderLayout.CENTER );
        frame.add( panel );
        //TODO down to here!
        try
        {
            ServerSocket server = new ServerSocket( 1337 );
            Room Default = new Room("Default", "", this);
            rooms.add( Default );
            while ( true )
            {
                Socket socket = server.accept();
                clientHandler client = new clientHandler( this, socket );
                clientlist.add( client );
                client.addRoom( Default );
                Default.addPlayer( client, "" );
                client.start();
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
    public boolean sendToPlayer(String playername, String message){
        for (clientHandler c: clientlist){
            if (c.getname().equals( playername )){
                c.send( message );
                return true;
            }
        }
        return false;
    }
    public void removeRoom(Room r){
        rooms.remove( r );
    }
}
