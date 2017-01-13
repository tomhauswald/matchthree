package com.houseforest.matchthree.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.houseforest.matchthree.Game;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = Game.RESOLUTION.x;
        config.height = Game.RESOLUTION.y;
        config.foregroundFPS = 60;
        config.resizable = false;
		new LwjglApplication(new Game(), config);
	}
}
