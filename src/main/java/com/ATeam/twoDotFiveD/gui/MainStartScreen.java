package com.ATeam.twoDotFiveD.gui;

import com.ATeam.twoDotFiveD.chatclient.chatClient;

import de.lessvoid.nifty.EndNotify;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Chat;
import de.lessvoid.nifty.controls.ChatTextSendEvent;
import de.lessvoid.nifty.render.NiftyImage;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.spi.render.RenderImage;
import de.lessvoid.nifty.elements.Element;


public class MainStartScreen implements ScreenController
{
    private Nifty nifty;

    private Screen screen;

    private static final String HUD_XML = "com/ATeam/twoDotFiveD/layout/hud.xml";

    private chatClient client;

    private NiftyImage temp;


    // http://jmonkeyengine.org/wiki/doku.php/jme3:advanced:nifty_gui_java_interaction
    @Override
    public void bind( Nifty nifty, Screen screen )
    {
        this.nifty = nifty;
        this.screen = screen;
        temp = nifty.getRenderEngine()
            .createImage( "chat-icon-user.png", false );
    }


    @Override
    public void onStartScreen()
    {
        // TODO Auto-generated method stub
        // This will need to be changed later
        // Initialization of client. Hard coded.
        // WHY IS THIS LINE REQUIRED?! DO NOT DELETE OR THE CHAT WILL CRASH!
        updateText( "You have joined the game." );
        // TODO you're going to need to ask for a client to connect to and a
        // name.
        client = new chatClient( this, "localhost", "Cliff" );
        if ( client.connect() )
        {
            client.start();
        }
        else
        {
            System.out.println( "CLIENT DIDN'T CONNECT!" );
        }
    }


    @Override
    public void onEndScreen()
    {
        // INFO nothing to do since its the main menu screen
    }


    /**
     * Called to exit the screen
     */
    public void exit()
    {
        nifty.createPopupWithId( "popupExit", "popupExit" );
        nifty.showPopup( screen, "popupExit", null );
    }


    public void beginGame()
    {
        nifty.fromXml( HUD_XML, "hud" );
        // TODO check for existing game in progress and ask
        DisplayStuff.setRenderNifty( true );
    }


    public void changeScreen( String nextScreen )
    {
        nifty.gotoScreen( nextScreen ); // switch to another screen
        // start the game and do some more stuff...
    }


    /**
     * Send entered text to client.
     * 
     * @param text
     *            The text to send.
     */
    @NiftyEventSubscriber(id = "chatId")
    public final void onSendText( final String id, final ChatTextSendEvent event )
    {
        // TODO RESOURCE:
        // http://jmonkeyengine.org/groups/gui/forum/topic/nifty-chat-box/
        // this is an event when a player enters a message
        String text = event.getText();
        System.out.println( "chat event received: " + text );
        client.send( text );
    }


    /**
     * Post text on the window.
     * 
     * @param message
     */
    public void updateText( String message )
    {
        final Element chatPanel = nifty.getCurrentScreen()
            .findElementByName( "chatId" );
        final Chat chatController = chatPanel.findNiftyControl( "chatId",
            Chat.class );
        chatController.receivedChatLine( message, temp, null );
    }


    public void processText( String message )
    {
        final Element chatPanel = nifty.getCurrentScreen()
            .findElementByName( "chatId" );
        final Chat chatController = chatPanel.findNiftyControl( "chatId",
            Chat.class );
        if ( message.split( " " )[0].equals( "[[Servermessage]]" ) )
        {
            if ( message.split( " " )[1].equals( "add" ) )
            {
                chatController.addPlayer( message.split( " " )[2], temp );
            }
            if ( message.split( " " )[1].equals( "remove" ) )
            {
                chatController.removePlayer( message.split( " " )[2] );
            }
            if ( message.split( " " )[1].equals( "roomadd" ) )
            {
                chatController.addPlayer( message.split( " " )[2], temp );
            }
            if ( message.split( " " )[1].equals( "roomdelete" ) )
            {
                chatController.removePlayer( message.split( " " )[2] );
            }
        }
        else
        {
            updateText( message );
        }
    }


    /**
     * popupExit.
     * 
     * @param exit
     *            exit string
     */
    public void popupExit( final String exit )
    {
        nifty.closePopup( "popupExit", new EndNotify()
        {
            public void perform()
            {
                if ( "yes".equals( exit ) )
                {
                    nifty.setAlternateKey( "fade" );
                    nifty.exit();
                    DisplayStuff.destroy();
                }
            }
        } );
    }

}
