package com.ATeam.twoDotFiveD;

import java.io.IOException;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyMouse;
import de.lessvoid.nifty.renderer.lwjgl.input.LwjglInputSystem;
import de.lessvoid.nifty.renderer.lwjgl.render.LwjglRenderDevice;
import de.lessvoid.nifty.sound.openal.OpenALSoundDevice;
import de.lessvoid.nifty.spi.time.impl.AccurateTimeProvider;
import de.niftygui.examples.LwjglInitHelper;

public class NiftyTest {

	public void start() {
		if (LwjglInitHelper.initSubSystems("NiftyTest")) {
			LwjglRenderDevice render = new LwjglRenderDevice();
			Nifty nifty = new Nifty(render, new OpenALSoundDevice(),
					new LwjglInputSystem(), new AccurateTimeProvider());
			nifty.fromXml(
					"src/main/resources/com/ATeam/twoDotFiveD/layout/main.xml",
					"start");
			// get the NiftyMouse interface that gives us access to all mouse
			// cursor related stuff
			NiftyMouse niftyMouse = nifty.getNiftyMouse();

			// register/load a mouse cursor (this would be done somewhere at the
			// beginning)
			//niftyMouse.registerMouseCursor("mouseId","src/main/resources/nifty-cursor.png", 0, 0);

			// change the cursor to the one we've loaded before
			niftyMouse.enableMouseCursor("mouseId");

			// we could set the position like so
			niftyMouse.setMousePosition(20, 20);

			LwjglInitHelper.renderLoop(nifty, null);
			LwjglInitHelper.destroy();
		}
	}

	public static void main(final String[] args) throws IOException {
		NiftyTest test = new NiftyTest();
		test.start();

	}
}
