package com.houseforest.matchthree;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by Tom on 14.01.2017.
 */

public class Util {

    public static final void log(String message) {
        Gdx.app.log("matchthree", message);
    }

    public static final <T> T randomArrayElement(T[] array) {
        return array[MathUtils.random(array.length - 1)];
    }

    public static void drawTextureFlipped(SpriteBatch batch,
                                          Texture texture,
                                          float x,
                                          float y,
                                          float w,
                                          float h){
        batch.draw(texture, x, Game.RESOLUTION.y - y, w, -h);
    }
}
