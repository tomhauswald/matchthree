package com.houseforest.matchthree;

import com.badlogic.gdx.Gdx;
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
}
