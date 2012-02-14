package com.ATeam.twoDotFiveD.chatclient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


// This class is just a temporary class to create a visual for testing
public class tempDisplay
{
    private JTextField write;

    private JTextArea read;

    private chatClient client;


    public static void main( String[] args )
    {
        new tempDisplay();
    }


    public tempDisplay()
    {
        // Window for viewing
        JFrame frame = new JFrame( "Client Window" );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        Dimension d = new Dimension( 500, 500 );
        frame.setSize( d );
        frame.setPreferredSize( d );
        frame.setMinimumSize( d );
        frame.setLayout( new BorderLayout() );
        JPanel panel = new JPanel();
        panel.setLayout( new BorderLayout() );
        // The panel for all the messages
        read = new JTextArea();
        read.setEditable( false );
        read.setLineWrap( true );
        read.setWrapStyleWord( true );
        Font font = read.getFont();
        read.setFont( new Font(font.getName(), font.getStyle() ,font.getSize() + 2) );
        JScrollPane scroll = new JScrollPane( read );
        scroll.setSize( read.getSize() );
        panel.add( scroll, BorderLayout.CENTER );
        // The area you write in
        write = new JTextField();
        write.setEnabled( true );
        panel.add( write, BorderLayout.SOUTH );
        // What you do when you press enter!
        write.addKeyListener( new KeyListener()
        {
            @Override
            public void keyPressed( KeyEvent arg0 )
            {

            }


            @Override
            public void keyReleased( KeyEvent arg0 )
            {

            }


            @Override
            public void keyTyped( KeyEvent arg0 )
            {
                if ( arg0.getKeyChar() == KeyEvent.VK_ENTER )
                {
                    String text = write.getText();
                    client.send( text );
                    write.setText( "" );
                }

            }
        } );
        // Add the panel and look at it
        frame.add( panel );
        client = new chatClient( this, "localhost", "Clifford" );
        if ( client.connect() )
        {
            client.start();
            frame.setVisible( true );
        }
    }


    // this gets called any time you need to put a message on the screen
    public void updatetext( String text )
    {
        read.setText(text + System.getProperty( "line.separator" ) + read.getText());
    }
}
