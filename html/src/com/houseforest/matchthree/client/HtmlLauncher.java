package com.houseforest.matchthree.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.houseforest.matchthree.Game;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(Game.RESOLUTION.x, Game.RESOLUTION.y);
        }

        @Override
        public ApplicationListener createApplicationListener () {
                return new Game();
        }
}