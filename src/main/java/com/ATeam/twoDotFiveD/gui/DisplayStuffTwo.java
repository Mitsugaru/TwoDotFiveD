package com.ATeam.twoDotFiveD.gui;

import org.lwjgl.opengl.Display;

import demo.lwjgl.basic.GLApp;

public class DisplayStuffTwo extends GLApp
{
	public static String window_title = "2.5D";
	
	public void run()
	{
		setRootClass();
        try {
            // Main loop
            while (!finished) {
                if (!Display.isVisible()) {  // window is minimized
                    Thread.sleep(200L);
                }
                else if (Display.isCloseRequested()) {  // window X button clicked
                    finished = true;
                }
                else {   // yield a little so other threads can work
                    Thread.sleep(1);
                }
                updateTimer();      // track when frame was drawn (see secondsSinceLastFrame)
                handleEvents();     // call key...() and mouse...() functions based on input events
                update();           // do program logic here (subclass may override this)
                draw();             // redraw the screen (subclass overrides this)
                Display.update();
            }
        }
        catch (Exception e) {
            err("GLApp.run(): " + e);
            e.printStackTrace(System.out);
        }
        // prepare to exit
        cleanup();
        System.exit(0);
	}
}
