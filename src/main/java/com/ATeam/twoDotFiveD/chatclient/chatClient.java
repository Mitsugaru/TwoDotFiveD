package com.ATeam.twoDotFiveD.chatclient;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import com.ATeam.twoDotFiveD.event.EventDispatcher;
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
    EventDispatcher link;
    UDPclient hi;


    // Tells the client where to connect, and what it's name is. The name must
    // be decided by the game before the chat client connects
    public chatClient( MainStartScreen whatIDisplay, String ip, String name, EventDispatcher t )
    {
    	link=t;
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
        	ip="192.168.1.2";
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
    
    public void sendMessage(byte[] data)
    {
    	hi.sendMessage(data);
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
            System.out.println("ID: "+ID);
            System.out.println(socket.getInetAddress());
            System.out.println(UDPServer.DEFAULTPORT);
            //display.processText(String.valueOf(ID));
            hi = new UDPclient(InetAddress.getByName(ip),UDPServer.DEFAULTPORT,display,ID, null);
            Thread t = new Thread(hi);
            t.start();
            //everything breaks when I run this
            //t.run();
            System.out.println("Thread go");
            while ( !stop )
            {
                if ( in.hasNext() )
                {
                    //display.processText( in.nextLine() );
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

    }
}
