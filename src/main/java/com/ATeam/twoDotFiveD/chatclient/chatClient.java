package com.ATeam.twoDotFiveD.chatclient;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import com.ATeam.twoDotFiveD.gui.MainStartScreen;


public class chatClient extends Thread
{
    // This is going to have to be adjusted for the game
    private MainStartScreen display;

    private String ip;

    private Socket socket;

    private Scanner in;

    private PrintWriter out;

    private boolean stop;

    private String name;


    // Tells the client where to connect, and what it's name is. The name must
    // be decided by the game before the chat client connects
    public chatClient( MainStartScreen whatIDisplay, String ip, String name )
    {
        display = whatIDisplay;
        this.ip = ip;
        stop = false;
        this.name = name;
    }


    // Returns true on successful connect, false otherwise.
    public boolean connect()
    {
        try
        {
            socket = new Socket( ip, 1337 );
            return true;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return false;
        }
    }


    // close the socket.
    public void close()
    {
        stop = true;
        in.close();
        out.close();
        try
        {
            socket.close();
        }
        catch ( Exception e )
        {

        }
    }

    // Just send the message, the server handles it.
    public void send( String message )
    {
        out.println( message );
    }


    // make the input and output, send the client name, and keep throwing up on
    // the screen what is sent
    public void run()
    {
        try
        {
            in = new Scanner( socket.getInputStream() );
            out = new PrintWriter( socket.getOutputStream(), true );
            out.println( name );
            int ID;
            //TODO get ID
            //new Thread(new UPDClient(ID)).run();
            while ( !stop )
            {
                if ( in.hasNext() )
                {
                    display.processText( in.nextLine() );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

    }
}
