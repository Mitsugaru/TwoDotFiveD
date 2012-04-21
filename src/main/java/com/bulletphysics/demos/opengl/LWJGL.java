/*
 * Java port of Bullet (c) 2008 Martin Dvorak <jezek2@advel.cz>
 *
 * Bullet Continuous Collision Detection and Physics Library
 * Copyright (c) 2003-2008 Erwin Coumans  http://www.bulletphysics.com/
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from
 * the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose, 
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 * 1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 *    misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package com.bulletphysics.demos.opengl;

import java.awt.event.KeyEvent;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import com.ATeam.twoDotFiveD.TwoDotFiveDBsp;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice;
import de.lessvoid.nifty.sound.openal.OpenALSoundDevice;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;

/**
 * 
 * @author jezek2
 */
public class LWJGL {

    @SuppressWarnings("unused")
    private static boolean redisplay = false;
    private static LwjglGL gl = new LwjglGL();
    private static final String SPLASH_XML = "com/ATeam/twoDotFiveD/layout/splash.xml";
    public static Nifty nifty;

    public static void postRedisplay() {
	redisplay = true;
    }

    public static IGL getGL() {
	return gl;
    }

    public static int main(String[] args, int width, int height, String title,
	    DemoApplication demoApp) throws LWJGLException {
	Display.setDisplayMode(new DisplayMode(width, height));
	Display.setTitle(title);
	Display.create(new PixelFormat(0, 24, 0));

	// Keyboard.create();
	// Keyboard.enableRepeatEvents(true);
	// Mouse.create();

	gl.init();
	// DisplayStuff.initSubSystems("2.5D");
	LwjglRenderDevice render = new LwjglRenderDevice();
	LwjglInputSystem inputSystem = new LwjglInputSystem();
	try {
	    inputSystem.startup();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	nifty = new Nifty(render, new OpenALSoundDevice(), inputSystem,
		new AccurateTimeProvider());
	nifty.fromXml(SPLASH_XML, "intro");
	demoApp.myinit();
	demoApp.reshape(width, height);

	boolean quit = false;

	long lastTime = System.currentTimeMillis();
	int frames = 0;

	while (!Display.isCloseRequested() && !quit) {
	    demoApp.moveAndDisplay();
	    Display.update();

	    int modifiers = 0;
	    if (!TwoDotFiveDBsp.nifty) {
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
			|| Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		    modifiers |= KeyEvent.SHIFT_DOWN_MASK;
		if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
			|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
		    modifiers |= KeyEvent.CTRL_DOWN_MASK;
		if (Keyboard.isKeyDown(Keyboard.KEY_LMETA)
			|| Keyboard.isKeyDown(Keyboard.KEY_RMETA))
		    modifiers |= KeyEvent.ALT_DOWN_MASK;

		while (Keyboard.next()) {
		    if (Keyboard.getEventCharacter() != '\0') {
			demoApp.keyboardCallback(Keyboard.getEventCharacter(),
				Mouse.getX(), Mouse.getY(), modifiers);
		    }

		    if (Keyboard.getEventKeyState()) {
			demoApp.specialKeyboard(Keyboard.getEventKey(),
				Mouse.getX(), Mouse.getY(), modifiers);
		    } else {
			demoApp.specialKeyboardUp(Keyboard.getEventKey(),
				Mouse.getX(), Mouse.getY(), modifiers);
		    }

		    if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE)
			quit = true;
		    if (Keyboard.getEventKey() == Keyboard.KEY_Q)
			quit = true;
		}

		while (Mouse.next()) {
		    if (Mouse.getEventButton() != -1) {
			int btn = Mouse.getEventButton();
			if (btn == 1) {
			    btn = 2;
			} else if (btn == 2) {
			    btn = 1;
			}
			demoApp.mouseFunc(btn, Mouse.getEventButtonState() ? 0
				: 1, Mouse.getEventX(),
				Display.getDisplayMode().getHeight() - 1
					- Mouse.getEventY());
		    }
		    demoApp.mouseMotionFunc(
			    Mouse.getEventX(),
			    Display.getDisplayMode().getHeight() - 1
				    - Mouse.getEventY());
		}
	    }

	    long time = System.currentTimeMillis();
	    if (time - lastTime < 1000) {
		frames++;
	    } else {
		Display.setTitle(title + " | FPS: " + frames);
		lastTime = time;
		frames = 0;
	    }

	}

	System.exit(0);
	return 0;
    }

}
