package com.ATeam.twoDotFiveD.chatclient;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import com.ATeam.twoDotFiveD.gui.MainStartScreen;
import com.ATeam.twoDotFiveD.udp.Client.UDPclient;
import com.ATeam.twoDotFiveD.udp.server.UDPServer;


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
    
    private int ID;


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
        out.println(message );
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
            //recieve ID from server
            int ID=Integer.parseInt(in.nextLine());
            //TODO get ID
            System.out.println(socket.getInetAddress());
            System.out.println(UDPServer.DEFAULTPORT);
            System.out.println(ID);
            UDPclient hi = new UDPclient(socket.getInetAddress(),UDPServer.DEFAULTPORT,display,ID);
            Thread t = new Thread(hi);
            //everything breaks when I run this
           // t.run();
            System.out.println("Thread go");
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
